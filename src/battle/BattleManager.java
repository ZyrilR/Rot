package battle;

import brainrots.BrainRot;
import brainrots.LevelUpResult;
import items.Item;
import items.Capsule;
import overworld.Player;
import progression.QuestSystem;
import skills.Skill;
import skills.SkillEffect;

import java.util.List;

public class BattleManager {

    public enum BattleResult { ONGOING, PLAYER_WIN, ENEMY_WIN, CAPTURED, FLED }

    private BrainRot             playerRot;
    private final BrainRot       enemyRot;
    private final List<BrainRot> playerTeam;
    private final Player         player;
    private final boolean        wildBattle;

    private BattleResult        result    = BattleResult.ONGOING;
    private BattleReward.Result reward    = null;
    private int                 turnCount = 0;

    public BattleManager(BrainRot playerRot, BrainRot enemyRot,
                         List<BrainRot> playerTeam, Player player, boolean wildBattle) {
        this.playerRot  = playerRot;
        this.enemyRot   = enemyRot;
        this.playerTeam = playerTeam;
        this.player     = player;
        this.wildBattle = wildBattle;
    }

    public void setPlayerRot(BrainRot rot) { this.playerRot = rot; }
    public boolean isWildBattle()          { return wildBattle; }

    // ── Turn execution ────────────────────────────────────────────────────────

    public void executePlayerTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;
        if (!StatusEffectManager.canAct(playerRot)) { endTurnCleanup(); return; }

        Skill skill = playerRot.getMoves().get(skillIndex);
        if (!playerRot.useSkill(skillIndex)) return;

        System.out.println(playerRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
            // FIX: Added player.gp so the Damage Calculator can check for Plot Armor!
            int dmg = DamageCalculator.calculate(skill, playerRot, enemyRot, player.gp);
            enemyRot.takeDamage(dmg);
            System.out.println(enemyRot.getName() + " took " + dmg + " damage! ("
                    + enemyRot.getCurrentHp() + "/" + enemyRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, playerRot, enemyRot);
        checkFainted();
    }

    public void executeEnemyTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;
        if (!StatusEffectManager.canAct(enemyRot)) { endTurnCleanup(); return; }

        Skill skill = enemyRot.getMoves().get(skillIndex);
        if (!enemyRot.useSkill(skillIndex)) return;

        System.out.println(enemyRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
            // FIX: Added player.gp so the Damage Calculator can check for Plot Armor!
            int dmg = DamageCalculator.calculate(skill, enemyRot, playerRot, player.gp);
            playerRot.takeDamage(dmg);
            System.out.println(playerRot.getName() + " took " + dmg + " damage! ("
                    + playerRot.getCurrentHp() + "/" + playerRot.getMaxHp() + " HP)");
        }

        SkillEffect.apply(skill, enemyRot, playerRot);
        checkFainted();
    }

    public boolean executeCapture(Item capsule) {
        if (!wildBattle) return false;
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
        StatusEffectManager.processTurnEnd(enemyRot);
        checkFainted();
    }

    private void checkFainted() {
        if (enemyRot.isFainted()) {
            System.out.println(enemyRot.getName() + " fainted! Player wins!");
            result = BattleResult.PLAYER_WIN;
            resolveRewards();
        } else if (playerRot.isFainted()) {
            System.out.println(playerRot.getName() + " fainted! Enemy wins!");
            result = BattleResult.ENEMY_WIN;
        }
    }

    private void resolveRewards() {
        reward = BattleReward.calculate(enemyRot);
        QuestSystem.getInstance().onFirstBattleWon();
        QuestSystem.getInstance().onLongBattle(turnCount);

        // Apply XP to the winning BrainRot
        reward.levelUps = playerRot.gainXp(reward.xp);

        // Apply coins to the player
        player.earnRotCoins(reward.coins);

        // Apply scroll to player inventory
        if (reward.hasScroll() && reward.scroll != null) {
            reward.scrollAdded = player.getInventory().addItem(reward.scroll);
        }

        System.out.println("[BattleManager] Rewards resolved — "
                + reward.xp + " XP, "
                + reward.coins + " coins"
                + (reward.hasScroll()
                ? ", " + reward.scrollSkillName + " scroll"
                + (reward.scrollAdded ? " added" : " (bag full)")
                : ""));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BattleResult         getResult()    { return result; }
    public boolean              isOver()       { return result != BattleResult.ONGOING; }
    public BrainRot             getPlayerRot() { return playerRot; }
    public BrainRot             getEnemyRot()  { return enemyRot; }
    public BattleReward.Result  getReward()    { return reward; }
}