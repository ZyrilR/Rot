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

    public static final Tier getTier(int level) {
        if(level < GOLD_LEVEL)
            return NORMAL;
        else if(level < DIAMOND_LEVEL)
            return GOLD;
        else
            return DIAMOND;
    }

    // Evolution thresholds — tier reflects the BrainRot's evolution stage at a given level.
    public static final int GOLD_LEVEL    = 25;
    public static final int DIAMOND_LEVEL = 50;

    public static Tier fromLevel(int level) {
        if (level >= DIAMOND_LEVEL) return DIAMOND;
        if (level >= GOLD_LEVEL)    return GOLD;
        return NORMAL;
    }
}