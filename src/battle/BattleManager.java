package battle;

import brainrots.BrainRot;
import brainrots.ExperienceSystem;
import brainrots.LevelUpResult;
import items.Inventory;
import progression.QuestSystem;
import progression.QuestSystem;
import skills.Skill;
import skills.SkillEffect;
import skills.SkillType;

import java.util.List;

/**
 * Orchestrates a single battle between a player BrainRot and an enemy BrainRot.
 * Handles turn resolution, skill use, status effects, capture attempts,
 * and achievement tracking.
 */
public class BattleManager {

    public enum BattleResult { ONGOING, PLAYER_WIN, ENEMY_WIN, CAPTURED, FLED }

    private final BrainRot   playerRot;
    private final BrainRot   enemyRot;
    private final List<BrainRot> playerTeam;
    private final Inventory  playerInventory;

    private BattleResult result     = BattleResult.ONGOING;
    private boolean      wildBattle;
    private List<LevelUpResult> levelUpResults = new java.util.ArrayList<>();

    // ── Per-battle achievement flags ──────────────────────────────────────────
    private boolean enemyActedThisBattle       = false;
    private boolean playerTookDamageThisBattle  = false;
    private boolean timelineRotUsedThisBattle   = false;
    private boolean usedOnlyGrassRockMoves      = true;  // flipped false if other move used
    private int     playerMinHP;                          // tracks lowest HP reached
    private int     itemsUsedThisBattle         = 0;
    private Skill   lastPlayerSkillUsed         = null;  // skill that landed killing blow

    // Trainer battle context for Imposter achievement
    private boolean isTrainerBattle   = false;
    private String  trainerLeadType   = null;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BattleManager(BrainRot playerRot, BrainRot enemyRot,
                         List<BrainRot> playerTeam, Inventory playerInventory,
                         boolean wildBattle) {
        this.playerRot       = playerRot;
        this.enemyRot        = enemyRot;
        this.playerTeam      = playerTeam;
        this.playerInventory = playerInventory;
        this.wildBattle      = wildBattle;

        playerRot.restoreForBattle();
        enemyRot.restoreForBattle();

        playerMinHP = playerRot.getMaxHp();
    }

    /**
     * Extended constructor for trainer battles — passes trainer lead type
     * so the Imposter achievement can be checked on win.
     */
    public BattleManager(BrainRot playerRot, BrainRot enemyRot,
                         List<BrainRot> playerTeam, Inventory playerInventory,
                         boolean wildBattle, boolean isTrainerBattle,
                         String trainerLeadType) {
        this(playerRot, enemyRot, playerTeam, playerInventory, wildBattle);
        this.isTrainerBattle = isTrainerBattle;
        this.trainerLeadType = trainerLeadType;
    }

    // ── Player Action ─────────────────────────────────────────────────────────

    /**
     * Player uses a skill by index from their moveset.
     */
    public void executePlayerTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;

        if (!StatusEffectManager.canAct(playerRot)) {
            endTurnCleanup();
            return;
        }

        Skill skill = playerRot.getMoves().get(skillIndex);
        if (!playerRot.useSkill(skill)) return;

        System.out.println(playerRot.getName() + " used " + skill.getName() + "!");

        // Track for Stomping Grounds — Patapim must only use GRASS or ROCK
        if (playerRot.getName().equalsIgnoreCase("BRR BRR PATAPIM")) {
            SkillType t = skill.getType();
            if (t != SkillType.GRASS && t != SkillType.ROCK) {
                usedOnlyGrassRockMoves = false;
            }
        }

        // Track Timeline Rot for Time Breaker
        if (skill.getName().equalsIgnoreCase("Timeline Rot")) {
            timelineRotUsedThisBattle = true;
        }

        lastPlayerSkillUsed = skill;

        if (skill.getPower() > 0) {
            int dmg = DamageCalculator.calculate(skill, playerRot, enemyRot);
            enemyRot.takeDamage(dmg);
            System.out.println(enemyRot.getName() + " took " + dmg + " damage! ("
                    + enemyRot.getCurrentHp() + "/" + enemyRot.getMaxHp() + " HP)");

            // Overkill check
            QuestSystem.getInstance().onDamageDealt(dmg, enemyRot.getMaxHp());
        }

        SkillEffect.apply(skill, playerRot, enemyRot);
        checkFainted();
    }

    // ── Capture ───────────────────────────────────────────────────────────────

    /**
     * Player attempts to capture a wild BrainRot using a capsule from inventory.
     */
    public void executeCapture(int capsuleIndex) {
        if (!wildBattle) {
            System.out.println("You can't capture a trainer's BrainRot!");
            return;
        }
        playerInventory.useItem(capsuleIndex, enemyRot, playerTeam, playerRot);
        if (playerTeam.contains(enemyRot)) {
            result = BattleResult.CAPTURED;
        }
    }

    // ── Item use (called from BattleUI when player uses item mid-battle) ──────

    /**
     * Register an item used during battle for Potion Hoarder tracking.
     */
    public void registerItemUsed() {
        itemsUsedThisBattle++;
        if (itemsUsedThisBattle >= 5) {
            QuestSystem.getInstance().onBattleItemThreshold();
        }
    }

    // ── Enemy Action ──────────────────────────────────────────────────────────

    /**
     * Enemy uses a skill by index (AI passes in chosen index).
     */
    public void executeEnemyTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;

        enemyActedThisBattle = true;

        if (!StatusEffectManager.canAct(enemyRot)) {
            endTurnCleanup();
            return;
        }

        Skill skill = enemyRot.getMoves().get(skillIndex);
        if (!enemyRot.useSkill(skill)) return;

        System.out.println(enemyRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
            int dmg = DamageCalculator.calculate(skill, enemyRot, playerRot);
            playerRot.takeDamage(dmg);

            // Track damage taken
            playerTookDamageThisBattle = true;
            int currentHp = playerRot.getCurrentHp();
            if (currentHp < playerMinHP) playerMinHP = currentHp;

            System.out.println(playerRot.getName() + " took " + dmg + " damage! ("
                    + playerRot.getCurrentHp() + "/" + playerRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, enemyRot, playerRot);
        checkFainted();
    }

    // ── End of Turn ───────────────────────────────────────────────────────────

    public void endTurn() {
        if (result != BattleResult.ONGOING) return;
        endTurnCleanup();
    }

    private void endTurnCleanup() {
        StatusEffectManager.processTurnEnd(playerRot);
        StatusEffectManager.processTurnEnd(enemyRot);
        checkFainted();
    }

    // ── Win / Loss ────────────────────────────────────────────────────────────

    private void checkFainted() {
        if (enemyRot.isFainted()) {
            System.out.println(enemyRot.getName() + " fainted! Player wins!");
            result = BattleResult.PLAYER_WIN;
            awardXp();
            fireWinAchievements();
        } else if (playerRot.isFainted()) {
            System.out.println(playerRot.getName() + " fainted! Enemy wins!");
            result = BattleResult.ENEMY_WIN;
            QuestSystem.getInstance().onBattleLost();
        }
    }

    private void fireWinAchievements() {
        QuestSystem.getInstance().onBattleWon(
                playerRot,
                enemyRot,
                enemyActedThisBattle,
                playerTookDamageThisBattle,
                playerMinHP,
                lastPlayerSkillUsed,
                usedOnlyGrassRockMoves,
                isTrainerBattle,
                trainerLeadType,
                timelineRotUsedThisBattle
        );
    }

    private void awardXp() {
        int xp = ExperienceSystem.xpYield(enemyRot);
        System.out.println(playerRot.getName() + " gained " + xp + " XP!");
        levelUpResults = playerRot.gainXp(xp);
        for (LevelUpResult lvl : levelUpResults) {
            System.out.println(playerRot.getName() + " grew to level " + lvl.newLevel + "!");
            System.out.println("  HP +" + lvl.hpGain + "  ATK +" + lvl.atkGain
                    + "  DEF +" + lvl.defGain + "  SPD +" + lvl.spdGain);

            // Level up achievements
            QuestSystem.getInstance().onLevelUp(lvl.newLevel);

            if (lvl.skillUnlocked != null)
                System.out.println(playerRot.getName() + " can learn " + lvl.skillUnlocked.getName() + "!");
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BattleResult          getResult()           { return result; }
    public boolean               isOver()              { return result != BattleResult.ONGOING; }
    public BrainRot              getPlayerRot()        { return playerRot; }
    public BrainRot              getEnemyRot()         { return enemyRot; }
    public List<LevelUpResult>   getLevelUpResults()   { return levelUpResults; }
}