package battle;

import brainrots.BrainRot;
import items.Inventory;
import items.Item;
import items.Capsule;
import skills.Skill;
import skills.SkillEffect;

import java.util.List;

public class BattleManager {

    public enum BattleResult { ONGOING, PLAYER_WIN, ENEMY_WIN, CAPTURED, FLED }

    private BrainRot playerRot; // Removed 'final' so we can swap BrainRots mid-battle!
    private final BrainRot enemyRot;
    private final List<BrainRot> playerTeam;
    private final Inventory playerInventory;

    private BattleResult result = BattleResult.ONGOING;
    private boolean wildBattle;

    public BattleManager(BrainRot playerRot, BrainRot enemyRot, List<BrainRot> playerTeam, Inventory playerInventory, boolean wildBattle) {
        this.playerRot       = playerRot;
        this.enemyRot        = enemyRot;
        this.playerTeam      = playerTeam;
        this.playerInventory = playerInventory;
        this.wildBattle      = wildBattle;

//        playerRot.restoreForBattle();
//        enemyRot.restoreForBattle();
    }

    // --- MISSING SETTER ADDED ---
    public void setPlayerRot(BrainRot rot) {
        this.playerRot = rot;
    }

    // --- MISSING GETTER ADDED ---
    public boolean isWildBattle() {
        return wildBattle;
    }

    public void executePlayerTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;

        if (!StatusEffectManager.canAct(playerRot)) {
            endTurnCleanup();
            return;
        }

        Skill skill = playerRot.getMoves().get(skillIndex);
        if (!playerRot.useSkill(skillIndex)) return;

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

    // --- FIXED SIGNATURE TO ACCEPT ITEM ---
    public boolean executeCapture(Item capsule) {
        if (!wildBattle) return false;

        // Use CaptureManager directly to calculate the math
        boolean success = CaptureManager.attempt((Capsule) capsule, enemyRot, playerRot, playerTeam);

        if (success) {
            result = BattleResult.CAPTURED;
        }
        return success;
    }

    public void executeEnemyTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;

        if (!StatusEffectManager.canAct(enemyRot)) {
            endTurnCleanup();
            return;
        }

        Skill skill = enemyRot.getMoves().get(skillIndex);
        if (!enemyRot.useSkill(skillIndex)) return;

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

    public BattleResult getResult()    { return result; }
    public boolean isOver()            { return result != BattleResult.ONGOING; }
    public BrainRot getPlayerRot()     { return playerRot; }
    public BrainRot getEnemyRot()      { return enemyRot; }
}