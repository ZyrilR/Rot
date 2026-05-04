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
    private final boolean        caveBonus;

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
        this.caveBonus  = player.gp.CURRENT_PATH.toLowerCase().contains("cave");

        // ── GUARD: if enemy is null or has no moves, end the battle immediately ──
        if (enemyRot == null) {
            System.err.println("[BattleManager] ERROR: enemyRot is null! Ending battle immediately.");
            result = BattleResult.ENEMY_WIN;
        } else if (enemyRot.getMoves() == null || enemyRot.getMoves().isEmpty()) {
            System.err.println("[BattleManager] ERROR: enemyRot has no moves! Ending battle.");
            result = BattleResult.ENEMY_WIN;
        }
    }

    public void setPlayerRot(BrainRot rot) { this.playerRot = rot; }
    public boolean isWildBattle()          { return wildBattle; }

    // ── Turn execution ────────────────────────────────────────────────────────

    public void executePlayerTurn(int skillIndex) {
        if (result != BattleResult.ONGOING) return;
        if (enemyRot == null) { result = BattleResult.ENEMY_WIN; return; }
        if (!StatusEffectManager.canAct(playerRot)) { endTurnCleanup(); return; }

        Skill skill = playerRot.getMoves().get(skillIndex);
        if (!playerRot.useSkill(skillIndex)) return;

        System.out.println(playerRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
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
        if (enemyRot == null) { result = BattleResult.ENEMY_WIN; return; }
        if (!StatusEffectManager.canAct(enemyRot)) { endTurnCleanup(); return; }

        List<Skill> moves = enemyRot.getMoves();
        if (moves == null || moves.isEmpty() || skillIndex >= moves.size()) {
            System.err.println("[BattleManager] Enemy has no valid skill at index " + skillIndex);
            endTurnCleanup();
            return;
        }

        Skill skill = moves.get(skillIndex);
        if (!enemyRot.useSkill(skillIndex)) return;

        System.out.println(enemyRot.getName() + " used " + skill.getName() + "!");

        if (skill.getPower() > 0) {
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
        if (enemyRot == null) return false;
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
            System.out.println(playerRot.getName() + " fainted! Enemy wins!");
            result = BattleResult.ENEMY_WIN;
        }
    }

    private void resolveRewards() {
        if (enemyRot == null) return;
        reward = BattleReward.calculate(enemyRot, caveBonus);
        QuestSystem.getInstance().onFirstBattleWon();
        QuestSystem.getInstance().onLongBattle(turnCount);

        reward.levelUps = playerRot.gainXp(reward.xp);
        player.earnRotCoins(reward.coins);

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