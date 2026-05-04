package battle;

import brainrots.BrainRot;
import brainrots.LevelUpResult;
import items.Item;
import items.ItemRegistry;
import skills.Skill;
import skills.SkillType;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes post-battle rewards (XP, coins, scroll drop).
 * Pure calculation only — no state mutation.
 *
 * Cave bonus:
 *   - 2.0x XP
 *   - 1.5x coins
 *
 * Dark Tall Grass bonus:
 *   - 1.5x XP
 *   - 1.25x coins
 *
 * Both Cave & Dark Grass:
 *   - Scroll drop is guaranteed to be from FIGHTING, ROCK, or DARK skill pools
 */
public class BattleReward {

    public static class Result {
        public final int    xp;
        public final int    coins;
        public final Item   scroll;
        public final String scrollSkillName;

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

    // ── Special terrain scroll pool — FIGHTING, ROCK, DARK skills only ───────────
    // These match the skill names registered in SkillRegistry exactly.
    private static final String[] SPECIAL_ZONE_SCROLL_SKILLS = {
            // --FIGHTING
            "Wooden Thump", "Power Combo", "Counter Guard", "Battle Cry",
            "Sahur Chant", "Wake-Up Call", "Infinite Sahur",        // Tung Tung sigs
            "Frequency Pulse", "The Din-Stutter", "Brain Scramble", // Udin sigs
            // --ROCK
            "Rock Slam", "Stone Guard", "Boulder Crash", "Defense Curl",
            "Nose Snort",                                            // Patapim sig
            "Ribbit Roll",                                           // Boneca sig
            // --DARK
            "Shadow Jab", "Night Slash", "Fear Stare", "Ambush",
            "Metal Frother", "Double Shot"                          // Capuccino sigs
    };

    // ── Public API ────────────────────────────────────────────────────────────

    /** Normal battle rewards (no special terrain bonus). */
    public static Result calculate(BrainRot enemy) {
        return calculate(enemy, false, false);
    }

    /** Backwards compatibility for existing cave-only calls */
    public static Result calculate(BrainRot enemy, boolean inCave) {
        return calculate(enemy, inCave, false);
    }

    /**
     * Battle rewards with optional Cave or Dark Grass bonus.
     */
    public static Result calculate(BrainRot enemy, boolean inCave, boolean inDarkGrass) {
        int xp    = calculateXp(enemy);
        int coins = calculateCoins(enemy);

        if (inCave) {
            xp    = (int)(xp    * 2.0);
            coins = (int)(coins * 1.5);
            System.out.println("[BattleReward] Cave bonus: " + xp + " XP, " + coins + " coins");
        } else if (inDarkGrass) {
            xp    = (int)(xp    * 1.5);
            coins = (int)(coins * 1.25);
            System.out.println("[BattleReward] Dark Grass bonus: " + xp + " XP, " + coins + " coins");
        }

        boolean hasBonus = inCave || inDarkGrass;
        String skill  = hasBonus ? rollSpecialZoneScrollSkill() : rollScrollSkill(enemy);
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
        int base = 50 + (enemy.getLevel() * 10);
        return (int)(base * tierMult);
    }

    // ── Scroll drops ──────────────────────────────────────────────────────────

    /**
     * Route scroll drop — 30% chance, based on enemy's own moveset.
     */
    private static String rollScrollSkill(BrainRot enemy) {
        if (!RandomUtil.chance(30.0)) return null;
        List<Skill> moves = enemy.getMoves();
        if (moves == null || moves.isEmpty()) return null;
        return moves.get(RandomUtil.range(0, moves.size() - 1)).getName();
    }

    /**
     * Cave / Dark Grass scroll drop — 40% chance, always from FIGHTING / ROCK / DARK pool.
     * Thematic and useful for building cave-type teams.
     */
    private static String rollSpecialZoneScrollSkill() {
        if (!RandomUtil.chance(40.0)) return null;
        return SPECIAL_ZONE_SCROLL_SKILLS[RandomUtil.range(0, SPECIAL_ZONE_SCROLL_SKILLS.length - 1)];
    }

}