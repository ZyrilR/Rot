package brainrots;

public enum Tier {
    NORMAL,
    GOLD,
    DIAMOND;

    public static final Tier getTier(String tier) {
        return switch(tier.toUpperCase()) {
            case "NORMAL" -> NORMAL;
            case "GOLD" -> GOLD;
            case "DIAMOND" -> DIAMOND;
            default -> NORMAL;
        };
    }

    // Evolution thresholds — tier reflects the BrainRot's evolution stage at a given level.
    public static final int GOLD_LEVEL    = 25;
    public static final int DIAMOND_LEVEL = 50;

    public static Tier fromLevel(int level) {
        if (level >= DIAMOND_LEVEL) return DIAMOND;
        if (level >= GOLD_LEVEL)    return GOLD;
        return NORMAL;
    }

    // Reward multipliers applied when an enemy of this tier is defeated.
    public double xpMultiplier() {
        return switch (this) {
            case GOLD    -> 1.5;
            case DIAMOND -> 2.5;
            default      -> 1.0;
        };
    }

    public double coinMultiplier() {
        return switch (this) {
            case GOLD    -> 2.0;
            case DIAMOND -> 4.0;
            default      -> 1.0;
        };
    }
}