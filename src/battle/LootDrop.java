package battle;

import brainrots.BrainRot;
import brainrots.Tier;
import items.Item;
import items.ItemRegistry;
import skills.Skill;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates loot awarded to the player after winning a battle.
 *
 * Coins formula:  base * tierMultiplier, where base = enemy.level * 5
 *   NORMAL: ×1.0 | GOLD: ×1.5 | DIAMOND: ×2.5
 *
 * Scroll drop: 20% chance — picks randomly from the enemy's 4 moves,
 * then resolves to the matching scroll in ItemRegistry (if one exists).
 */
public class LootDrop {

    public static class Result {
        public final int    coins;
        public final Item   scroll;   // null if no scroll dropped
        public final String scrollSkillName; // display name even if item missing

        public Result(int coins, Item scroll, String scrollSkillName) {
            this.coins          = coins;
            this.scroll         = scroll;
            this.scrollSkillName = scrollSkillName;
        }

        public boolean hasScroll() { return scrollSkillName != null; }
    }

    /** Roll loot for defeating {@code enemy}. */
    public static Result roll(BrainRot enemy) {
        int coins = calculateCoins(enemy);
        Result scrollResult = rollScroll(enemy);
        return new Result(coins, scrollResult.scroll, scrollResult.scrollSkillName);
    }

    // ── Coins ─────────────────────────────────────────────────────────────────

    private static int calculateCoins(BrainRot enemy) {
        int base = enemy.getLevel() * 5;
        double mult = switch (enemy.getTier()) {
            case GOLD    -> 1.5;
            case DIAMOND -> 2.5;
            default      -> 1.0;
        };
        return Math.max(1, (int)(base * mult));
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    private static Result rollScroll(BrainRot enemy) {
        if (!RandomUtil.chance(20.0)) return new Result(0, null, null);

        List<Skill> moves = enemy.getMoves();
        if (moves == null || moves.isEmpty()) return new Result(0, null, null);

        // Pick a random move from the enemy's moveset
        Skill picked = moves.get(RandomUtil.range(0, moves.size() - 1));
        String skillName = picked.getName();

        // Look up the scroll in ItemRegistry by convention: "<SKILL NAME> SCROLL"
        String scrollName = skillName.toUpperCase() + " SCROLL";
        Item scroll = ItemRegistry.getItem(scrollName);

        // scroll may be null if no scroll exists for that skill — we still report the drop
        return new Result(0, scroll, skillName);
    }
}