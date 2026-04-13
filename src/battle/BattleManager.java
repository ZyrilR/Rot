package battle;

import brainrots.BrainRot;
import items.Inventory;
import items.Item;
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

    private BrainRot playerRot;
    private final BrainRot enemyRot;
    private final List<BrainRot> playerTeam;
    private final Inventory playerInventory;

    private BattleResult result = BattleResult.ONGOING;
    private boolean wildBattle; // true = wild BrainRot, can attempt capture

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
    /** Player attempts to capture using a specific item instance */
    public boolean executeCapture(Item capsule) {
        if (!wildBattle) return false;

        // Calculate catch chance based on missing HP
        double hpPercent = (double) enemyRot.getCurrentHp() / enemyRot.getMaxHp();
        double catchRate = 0.5; // Base 50% chance for a normal capsule

        // Better capsules have better rates
        if (capsule.getName().toUpperCase().contains("BLUE")) catchRate = 0.75;
        if (capsule.getName().toUpperCase().contains("MASTER")) catchRate = 1.0;

        // The lower the HP, the higher the bonus!
        double finalChance = catchRate + ((1.0 - hpPercent) * 0.5);

        boolean success = Math.random() <= finalChance;

        if (success) {
            result = BattleResult.CAPTURED;
        }

        return success;
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
        } else if (playerRot.isFainted()) {
            System.out.println(playerRot.getName() + " fainted! Enemy wins!");
            result = BattleResult.ENEMY_WIN;
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BattleResult getResult()    { return result; }
    public boolean isOver()            { return result != BattleResult.ONGOING; }
    public BrainRot getPlayerRot()     { return playerRot; }
    public BrainRot getEnemyRot()      { return enemyRot; }
    public boolean isWildBattle() { return wildBattle; }

    public void setPlayerRot(BrainRot rot) { this.playerRot = rot; }
}