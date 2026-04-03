package brainrots;

/**
 * Handles XP thresholds and battle XP yield.
 *
 * Formula: xpToNextLevel(L) = L * L * 5
 *   Level 1 → 2 :   5 XP
 *   Level 9 → 10: 405 XP
 *   Level 24→ 25: 2 880 XP
 *   Level 99→ 100: 49 005 XP
 *
 * XP yield = enemy.level * 12 * tierMultiplier
 *   NORMAL: ×1.0 | GOLD: ×1.5 | DIAMOND: ×2.5
 */
public class ExperienceSystem {

    public static final int MAX_LEVEL = 100;

    /** XP required to advance FROM {@code level} to {@code level + 1}. */
    public static int xpToNextLevel(int level) {
        if (level >= MAX_LEVEL) return Integer.MAX_VALUE;
        return level * level * 5;
    }

    /** XP awarded to the winner for defeating {@code enemy}. */
    public static int xpYield(BrainRot enemy) {
        double tierMult = switch (enemy.getTier()) {
            default      -> 1.0;
        };
        return (int)(enemy.getLevel() * 12 * tierMult);
    }
}
