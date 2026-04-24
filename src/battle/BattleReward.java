package battle;

import brainrots.BrainRot;
import brainrots.LevelUpResult;
import items.Item;
import items.ItemRegistry;
import skills.Skill;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

import static utils.Constants.COIN_BASE;
import static utils.Constants.COIN_PER_LEVEL;
import static utils.Constants.SCROLL_DROP_PERCENT;
import static utils.Constants.XP_PER_LEVEL;

/**
 * Computes post-battle rewards (XP, coins, scroll drop).
 * {@link #calculate(BrainRot)} is a pure computation; the returned {@link Result}
 * is later enriched by BattleManager with the applied level-ups and scroll-inventory outcome.
 * Tunable economy constants live in {@link utils.Constants}.
 */
public final class BattleReward {

    private BattleReward() {} // utility class — not instantiable

    public static class Result {
        public final int    xp;
        public final int    coins;
        public final Item   scroll;
        public final String scrollSkillName;

        // Set by BattleManager.resolveRewards() after application.
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
        String skill  = rollScrollName(enemy);
        Item   scroll = skill != null ? ItemRegistry.getItem(skill.toUpperCase() + " SCROLL") : null;
        return new Result(xp, coins, scroll, skill);
    }

    // ── XP ────────────────────────────────────────────────────────────────────

    private static int calculateXp(BrainRot enemy) {
        return (int)(enemy.getLevel() * XP_PER_LEVEL * enemy.getTier().xpMultiplier());
    }

    // ── Coins ─────────────────────────────────────────────────────────────────

    private static int calculateCoins(BrainRot enemy) {
        // Flat base + level scaling so early game isn't punishing.
        int base = COIN_BASE + (enemy.getLevel() * COIN_PER_LEVEL);
        return (int)(base * enemy.getTier().coinMultiplier());
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    /**
     * Rolls the scroll drop. Returns the source skill name, or null if no drop
     * occurred (failed chance roll, or the enemy has no moves to sample from).
     */
    private static String rollScrollName(BrainRot enemy) {
        if (!RandomUtil.chance(SCROLL_DROP_PERCENT)) return null;
        List<Skill> moves = enemy.getMoves();
        if (moves.isEmpty()) return null;
        return moves.get(RandomUtil.range(0, moves.size() - 1)).getName();
    }
}