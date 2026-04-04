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
}
