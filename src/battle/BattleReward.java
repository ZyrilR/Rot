package battle;

import brainrots.BrainRot;
import brainrots.LevelUpResult;
import brainrots.Tier;
import items.Item;
import items.ItemRegistry;
import skills.Skill;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes post-battle rewards (XP, coins, scroll drop).
 * Pure calculation only — no state mutation.
 * All application of rewards is handled by BattleManager.resolveRewards().
 */
public class BattleReward {

    public static class Result {
        public final int    xp;
        public final int    coins;
        public final Item   scroll;
        public final String scrollSkillName;

        // Set by BattleManager.resolveRewards() after application
        public List<LevelUpResult> levelUps    = new ArrayList<>();
        public boolean             scrollAdded = false;

        public Result(int xp, int coins, Item scroll, String scrollSkillName) {
            this.xp              = xp;
            this.coins           = coins;
            this.scroll          = scroll;
            this.scrollSkillName = scrollSkillName;
        }

        public boolean hasScroll() { return scrollSkillName != null; }
    }

    /**
     * Pure computation — calculates what rewards the player should receive.
     * Does not mutate any state.
     */
    public static Result calculate(BrainRot enemy) {
        int    xp     = calculateXp(enemy);
        int    coins  = calculateCoins(enemy);
        String skill  = rollScrollSkill(enemy);
        Item   scroll = skill != null ? ItemRegistry.getItem(skill.toUpperCase() + " SCROLL") : null;
        return new Result(xp, coins, scroll, skill);
    }

    // ── XP ────────────────────────────────────────────────────────────────────

    private static int calculateXp(BrainRot enemy) {
        double tierMult = switch (enemy.getTier()) {
            case GOLD    -> 1.5;
            case DIAMOND -> 2.5;
            default      -> 1.0;
        };
        return Math.max(1, (int)(enemy.getLevel() * 12 * tierMult));
    }

    // ── Coins ─────────────────────────────────────────────────────────────────

    private static int calculateCoins(BrainRot enemy) {
        double tierMult = switch (enemy.getTier()) {
            case GOLD    -> 2.0;
            case DIAMOND -> 4.0;
            default      -> 1.0;
        };
        // base: flat 50 + level scaling, so early game isn't punishing
        int base = 50 + (enemy.getLevel() * 10);
        return (int)(base * tierMult);
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    /**
     * 20% chance to drop a scroll based on a random move from the enemy's moveset.
     * Returns the skill name, or null if no drop.
     */
    private static String rollScrollSkill(BrainRot enemy) {
        if (!RandomUtil.chance(20.0)) return null;
        List<Skill> moves = enemy.getMoves();
        if (moves == null || moves.isEmpty()) return null;
        return moves.get(RandomUtil.range(0, moves.size() - 1)).getName();
    }
}