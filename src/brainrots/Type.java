package brainrots;

public enum Type {
    NORMAL,
    FIGHTING,
    WATER,
    PSYCHIC,
    FLYING,
    SAND,
    GRASS,
    ROCK,
    FIRE,
    DARK,
    POISON;

    public static final Type getType(String type) {
        return switch(type.toUpperCase()) {
            case "NORMAL" -> NORMAL;
            case "FIGHTING" -> FIGHTING;
            case "WATER" -> WATER;
            case "PSYCHIC" -> PSYCHIC;
            case "FLYING" -> FLYING;
            case "SAND" -> SAND;
            case "GRASS" -> GRASS;
            case "ROCK" -> ROCK;
            case "FIRE" -> FIRE;
            case "DARK" -> DARK;
            case "POISON" -> POISON;
            case "NULL" -> null;
            default -> NORMAL;
        };
    }
}
