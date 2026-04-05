package battle;

import brainrots.BrainRot;
import brainrots.ExperienceSystem;
import brainrots.LevelUpResult;
import items.Inventory;
import skills.Skill;
import skills.SkillEffect;

import java.util.List;

/**
 * Orchestrates a single battle between a player BrainRot and an enemy BrainRot.
 * Handles turn resolution, skill use, status effects, and capture attempts.
 *
 * Usage:
 *   BattleManager battle = new BattleManager(playerRot, enemyRot, playerTeam, playerInventory);
 *   battle.executePlayerTurn(chosenSkillIndex);   // player acts
 *   battle.executeEnemyTurn(chosenSkillIndex);    // AI/enemy acts
 *   battle.endTurn();                             // process end-of-turn effects
 */
public class BattleManager {

    public enum BattleResult { ONGOING, PLAYER_WIN, ENEMY_WIN, CAPTURED, FLED }

    private final BrainRot playerRot;
    private final BrainRot enemyRot;
    private final List<BrainRot> playerTeam;
    private final Inventory playerInventory;

    private BattleResult result = BattleResult.ONGOING;
    private boolean wildBattle; // true = wild BrainRot, can attempt capture
    private List<LevelUpResult> levelUpResults = new java.util.ArrayList<>();

    public BattleManager(BrainRot playerRot, BrainRot enemyRot, List<BrainRot> playerTeam, Inventory playerInventory, boolean wildBattle) {
        this.playerRot       = playerRot;
        this.enemyRot        = enemyRot;
        this.playerTeam      = playerTeam;
        this.playerInventory = playerInventory;
        this.wildBattle      = wildBattle;

        playerRot.restoreForBattle();
        enemyRot.restoreForBattle();
    }

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
        if (!playerRot.useSkill(skill)) return; // not enough SP / ultimate restriction

        System.out.println(playerRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
            int dmg = DamageCalculator.calculate(skill, playerRot, enemyRot);
            enemyRot.takeDamage(dmg);
            System.out.println(enemyRot.getName() + " took " + dmg + " damage! ("
                    + enemyRot.getCurrentHp() + "/" + enemyRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, playerRot, enemyRot);
        checkFainted();
    }

    /** Player attempts to capture a wild BrainRot using a capsule from inventory */
    public void executeCapture(int capsuleIndex) {
        if (!wildBattle) {
            System.out.println("You can't capture a trainer's BrainRot!");
            return;
        }

        // Pass playerTeam and active playerRot to the Capsule
        playerInventory.useItem(capsuleIndex, enemyRot, playerTeam, playerRot);

        if (playerTeam.contains(enemyRot)) {
            result = BattleResult.CAPTURED;
        }
    }


    // ── Enemy Action ──────────────────────────────────────────────────────────

    /**
     * Enemy uses a skill by index (AI passes in chosen index).
     */
    public void executeEnemyTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;

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
            System.out.println(playerRot.getName() + " took " + dmg + " damage! ("
                    + playerRot.getCurrentHp() + "/" + playerRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, enemyRot, playerRot);
        checkFainted();
    }

    // ── End of Turn ───────────────────────────────────────────────────────────

    /**
     * Processes end-of-turn effects (burn ticks, status countdowns).
     * Call once after both combatants have acted.
     */
    public void endTurn() {
        if (result != BattleResult.ONGOING) return;
        endTurnCleanup();
    }

    private void endTurnCleanup() {
        StatusEffectManager.processTurnEnd(playerRot);
        StatusEffectManager.processTurnEnd(enemyRot);
        checkFainted();
    }

    private void checkFainted() {
        if (enemyRot.isFainted()) {
            System.out.println(enemyRot.getName() + " fainted! Player wins!");
            result = BattleResult.PLAYER_WIN;
            awardXp();
        } else if (playerRot.isFainted()) {
            System.out.println(playerRot.getName() + " fainted! Enemy wins!");
            result = BattleResult.ENEMY_WIN;
        }
    }

    private void awardXp() {
        int xp = ExperienceSystem.xpYield(enemyRot);
        System.out.println(playerRot.getName() + " gained " + xp + " XP!");
        levelUpResults = playerRot.gainXp(xp);
        for (LevelUpResult lvl : levelUpResults) {
            System.out.println(playerRot.getName() + " grew to level " + lvl.newLevel + "!");
            System.out.println("  HP +" + lvl.hpGain + "  ATK +" + lvl.atkGain
                    + "  DEF +" + lvl.defGain + "  SPD +" + lvl.spdGain);
            if (lvl.skillUnlocked != null) {
                System.out.println(playerRot.getName() + " can learn " + lvl.skillUnlocked.getName() + "!");
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BattleResult getResult()              { return result; }
    public boolean isOver()                      { return result != BattleResult.ONGOING; }
    public BrainRot getPlayerRot()               { return playerRot; }
    public BrainRot getEnemyRot()                { return enemyRot; }
    /** Level-up events from the last battle win (empty if no level-up occurred). */
    public List<LevelUpResult> getLevelUpResults() { return levelUpResults; }
}