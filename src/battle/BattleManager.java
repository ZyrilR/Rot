package battle;

import brainrots.BrainRot;
import brainrots.LevelUpResult;
import items.Item;
import items.Capsule;
import npc.TrainerNPC;
import overworld.Player;
import progression.QuestSystem;
import skills.Skill;
import skills.SkillEffect;
import skills.SkillRegistry;

import java.util.List;

public class BattleManager {

    public enum BattleResult { ONGOING, PLAYER_WIN, ENEMY_WIN, CAPTURED, FLED }

    private BrainRot             playerRot;
    private BrainRot             enemyRot;
    private final List<BrainRot> playerTeam;
    private final Player         player;
    private final boolean        wildBattle;
    private final boolean        caveBonus;
    private TrainerNPC           trainer;

    private BattleResult        result    = BattleResult.ONGOING;
    private BattleReward.Result reward    = null;
    private int                 turnCount = 0;

    // ── Per-battle stat tracking for quest hooks ─────────────────────────────
    private final BrainRot originalPlayerRot;
    private final BrainRot originalEnemyRot;
    private final String   originalEnemyType;
    private boolean        enemyActed         = false;
    private boolean        playerTookDamage   = false;
    private int            playerMinHp;
    private int            itemsUsedInBattle  = 0;
    private boolean        battleWonHookFired = false;

    public BrainRot getOriginalPlayerRot()  { return originalPlayerRot; }
    public BrainRot getOriginalEnemyRot()   { return originalEnemyRot; }
    public String   getOriginalEnemyType()  { return originalEnemyType; }
    public boolean  getEnemyActed()         { return enemyActed; }
    public boolean  getPlayerTookDamage()   { return playerTookDamage; }
    public int      getPlayerMinHp()        { return playerMinHp; }
    public int      getItemsUsedInBattle()  { return itemsUsedInBattle; }
    public void     incrementItemsUsed()    { itemsUsedInBattle++; }
    public boolean  hasFiredBattleWonHook() { return battleWonHookFired; }
    public void     markBattleWonHookFired(){ battleWonHookFired = true; }

    public BattleManager(BrainRot playerRot, BrainRot enemyRot,
                         List<BrainRot> playerTeam, Player player, boolean wildBattle) {
        this.playerRot  = playerRot;
        this.enemyRot   = enemyRot;
        this.playerTeam = playerTeam;
        this.player     = player;
        this.wildBattle = wildBattle;
        this.caveBonus  = player.gp.CURRENT_PATH.toLowerCase().contains("cave");

        this.originalPlayerRot = playerRot;
        this.originalEnemyRot  = enemyRot;
        this.originalEnemyType = (enemyRot != null && enemyRot.getPrimaryType() != null)
                ? enemyRot.getPrimaryType().name() : null;
        this.playerMinHp       = (playerRot != null) ? playerRot.getCurrentHp() : 0;

        // ── GUARD: if enemy is null or has no moves, end immediately ──────────
        if (enemyRot == null) {
            System.err.println("[BattleManager] enemyRot is null! Ending battle.");
            result = BattleResult.ENEMY_WIN;
        } else if (enemyRot.getMoves() == null || enemyRot.getMoves().isEmpty()) {
            System.err.println("[BattleManager] enemyRot has no moves! Ending battle.");
            result = BattleResult.ENEMY_WIN;
        }
    }

    public void setPlayerRot(BrainRot rot) { this.playerRot = rot; }
    public boolean isWildBattle()          { return wildBattle; }

    public void setTrainer(TrainerNPC t)   { this.trainer = t; }
    public TrainerNPC getTrainer()         { return trainer; }

    /** True if the trainer has another non-fainted BrainRot waiting after the current one. */
    public boolean trainerHasNextEnemy() {
        return trainer != null && trainer.getNextBrainRot(enemyRot) != null;
    }

    /**
     * Switches enemyRot to the trainer's next non-fainted BrainRot and resets the
     * battle to ONGOING so the player keeps fighting. Returns the new enemy or null.
     */
    public BrainRot advanceToNextTrainerEnemy() {
        if (trainer == null) return null;
        BrainRot next = trainer.getNextBrainRot(enemyRot);
        if (next == null) return null;
        this.enemyRot  = next;
        this.result    = BattleResult.ONGOING;
        return next;
    }

    // ── Turn execution ────────────────────────────────────────────────────────

    public void executePlayerTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;
        if (enemyRot == null) { result = BattleResult.ENEMY_WIN; return; }
        if (!StatusEffectManager.canAct(playerRot)) { endTurnCleanup(); return; }

        // Clamp skill index to player's actual move count
        int safeIndex = Math.min(skillIndex, playerRot.getMoves().size() - 1);
        if (safeIndex < 0) { endTurnCleanup(); return; }

        Skill skill = playerRot.getMoves().get(safeIndex);
        if (!playerRot.useSkill(safeIndex)) return;

        System.out.println(playerRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
            int dmg = DamageCalculator.calculate(skill, playerRot, enemyRot, player.gp);
            int enemyMaxHp = enemyRot.getMaxHp();
            enemyRot.takeDamage(dmg);
            QuestSystem.getInstance().onDamageDealt(dmg, enemyMaxHp);
            System.out.println(enemyRot.getName() + " took " + dmg + " damage! ("
                    + enemyRot.getCurrentHp() + "/" + enemyRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, playerRot, enemyRot);
        checkFainted();
    }

    public void executeEnemyTurn(int skillIndex) {
        enemyActed = true;
        if (result != BattleResult.ONGOING) return;
        if (enemyRot == null) { result = BattleResult.ENEMY_WIN; return; }
        if (!StatusEffectManager.canAct(enemyRot)) { endTurnCleanup(); return; }

        List<Skill> moves = enemyRot.getMoves();
        if (moves == null || moves.isEmpty()) { endTurnCleanup(); return; }

        // ── FIX: clamp index to enemy's ACTUAL move count ─────────────────────
        // This was the IndexOutOfBoundsException — enemyChosenIndex could be
        // calculated as (moveCount - 1) of a DIFFERENT rot than the one fighting.
        int safeIndex = Math.min(skillIndex, moves.size() - 1);
        if (safeIndex < 0) { endTurnCleanup(); return; }

        Skill skill = moves.get(safeIndex);
        if (!enemyRot.useSkill(safeIndex)) return;

        System.out.println(enemyRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
            int dmg = DamageCalculator.calculate(skill, enemyRot, playerRot, player.gp);
            playerRot.takeDamage(dmg);
            if (dmg > 0) playerTookDamage = true;
            if (playerRot.getCurrentHp() < playerMinHp) playerMinHp = playerRot.getCurrentHp();
            System.out.println(playerRot.getName() + " took " + dmg + " damage! ("
                    + playerRot.getCurrentHp() + "/" + playerRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, enemyRot, playerRot);
        checkFainted();
    }

    /** Sentinel index passed to executePlayerTurn/executeEnemyTurn to trigger Struggle. */
    public static final int STRUGGLE_INDEX = -10;

    /** Last struggle recoil amount, exposed so the UI can display it. */
    private int lastStruggleRecoil = 0;
    public int getLastStruggleRecoil() { return lastStruggleRecoil; }

    /**
     * Pokemon-style Struggle: deals normal damage to the defender and recoils 1/4 of
     * the damage back to the attacker. Used when a BrainRot has no remaining UP.
     */
    public void executeStruggleTurn(boolean isPlayerSide) {
        if (result != BattleResult.ONGOING) return;
        if (!isPlayerSide) enemyActed = true;
        BrainRot attacker = isPlayerSide ? playerRot : enemyRot;
        BrainRot defender = isPlayerSide ? enemyRot : playerRot;
        if (attacker == null || defender == null) return;
        if (attacker.isFainted()) return;
        if (!StatusEffectManager.canAct(attacker)) { endTurnCleanup(); return; }

        Skill struggle = SkillRegistry.struggle();
        int dmg = DamageCalculator.calculate(struggle, attacker, defender, player.gp);
        if (dmg < 1) dmg = 1;
        if (isPlayerSide) {
            int enemyMaxHp = defender.getMaxHp();
            defender.takeDamage(dmg);
            QuestSystem.getInstance().onDamageDealt(dmg, enemyMaxHp);
        } else {
            defender.takeDamage(dmg);
            playerTookDamage = true;
            if (defender.getCurrentHp() < playerMinHp) playerMinHp = defender.getCurrentHp();
        }

        // Recoil: 1/4 of damage dealt, minimum 1.
        int recoil = Math.max(1, dmg / 4);
        attacker.takeDamage(recoil);
        if (isPlayerSide && attacker == playerRot
                && playerRot.getCurrentHp() < playerMinHp) {
            playerMinHp = playerRot.getCurrentHp();
        }
        lastStruggleRecoil = recoil;

        System.out.println(attacker.getName() + " has no UP left and used Struggle! "
                + dmg + " dmg / " + recoil + " recoil");

        checkFainted();
    }

    public boolean executeCapture(Item capsule) {
        if (!wildBattle || enemyRot == null) return false;
        boolean success = CaptureManager.attempt((Capsule) capsule, enemyRot, playerRot, playerTeam);
        if (success) result = BattleResult.CAPTURED;
        return success;
    }

    public void endTurn() {
        if (result != BattleResult.ONGOING) return;
        turnCount++;
        endTurnCleanup();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void endTurnCleanup() {
        StatusEffectManager.processTurnEnd(playerRot);
        if (enemyRot != null) StatusEffectManager.processTurnEnd(enemyRot);
        checkFainted();
    }

    private void checkFainted() {
        if (enemyRot == null || enemyRot.isFainted()) {
            if (enemyRot != null)
                System.out.println(enemyRot.getName() + " fainted! Player wins!");
            result = BattleResult.PLAYER_WIN;
            resolveRewards();
        } else if (playerRot.isFainted()) {
            if (hasHealthyReserves()) {
                System.out.println(playerRot.getName() + " fainted! Awaiting replacement send-out...");
                // Battle stays ONGOING; BattleUI will force a TEAM_SELECT.
            } else {
                System.out.println(playerRot.getName() + " fainted! Enemy wins!");
                result = BattleResult.ENEMY_WIN;
            }
        }
    }

    /** True if the player has at least one non-fainted BrainRot besides the active one. */
    public boolean hasHealthyReserves() {
        if (playerTeam == null) return false;
        for (BrainRot rot : playerTeam) {
            if (rot != null && rot != playerRot && !rot.isFainted()) return true;
        }
        return false;
    }

    private void resolveRewards() {
        if (enemyRot == null) return;
        reward = BattleReward.calculate(enemyRot, caveBonus);
        QuestSystem.getInstance().onFirstBattleWon();
        QuestSystem.getInstance().onLongBattle(turnCount);

        reward.levelUps = playerRot.gainXp(reward.xp);

        // Trainer battles: per-rot coin/scroll drops are suppressed. Coins and items
        // come from the trainer's own stash, awarded once on full defeat (BattleUI).
        if (trainer == null) {
            player.earnRotCoins(reward.coins);
            if (reward.hasScroll() && reward.scroll != null)
                reward.scrollAdded = player.getInventory().addItem(reward.scroll);
        } else {
            reward.suppressDrops = true;
        }

        System.out.println("[BattleManager] Rewards: " + reward.xp + " XP, " + reward.coins + " coins"
                + (reward.hasScroll() && !reward.suppressDrops ? ", " + reward.scrollSkillName + " scroll"
                + (reward.scrollAdded ? " added" : " (bag full)") : ""));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BattleResult        getResult()    { return result; }
    public boolean             isOver()       { return result != BattleResult.ONGOING; }
    public BrainRot            getPlayerRot() { return playerRot; }
    public BrainRot            getEnemyRot()  { return enemyRot; }
    public BattleReward.Result getReward()    { return reward; }
}