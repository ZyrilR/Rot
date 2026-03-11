package skills;

import java.util.HashMap;

/**
 * Central registry for all skills in the game.
 * Stores normal pool, type pools, and signature moves.
 */
public class SkillRegistry {

    private static final HashMap<String, Skill> registry = new HashMap<>();

    static {
        // ── Normal Pool ──────────────────────────────────────────────────────
        register(new Skill("Guard Up",      SkillType.NORMAL,  0, 10, "RAISE_DEF"));
        register(new Skill("Focus Stance",  SkillType.NORMAL,  0, 15, "RAISE_ATK"));
        register(new Skill("Quick Step",    SkillType.NORMAL,  0, 10, "RAISE_SPD"));
        register(new Skill("Rest",          SkillType.NORMAL,  0, 15, "HEAL"));
        register(new Skill("Speed Boost",   SkillType.NORMAL,  0, 15, "RAISE_SPD"));
        register(new Skill("Fortify",       SkillType.NORMAL,  0, 15, "RAISE_DEF"));
        register(new Skill("Evasion Up",    SkillType.NORMAL,  0, 10, "NONE"));

        // ── Fighting ─────────────────────────────────────────────────────────
        register(new Skill("Wooden Thump",  SkillType.FIGHTING, 20, 20, "NONE"));
        register(new Skill("Power Combo",   SkillType.FIGHTING, 15, 15, "FLINCH"));
        register(new Skill("Counter Guard", SkillType.FIGHTING,  0, 15, "NONE"));
        register(new Skill("Battle Cry",    SkillType.FIGHTING,  0, 15, "LOWER_ATK"));

        // ── Water ─────────────────────────────────────────────────────────────
        register(new Skill("Aqua Dash",     SkillType.WATER,   15, 15, "NONE"));
        register(new Skill("Tidal Crash",   SkillType.WATER,   20, 20, "LOWER_DEF"));
        register(new Skill("Aqua Engine",   SkillType.WATER,   15, 15, "RAISE_SPD"));
        register(new Skill("Mist Veil",     SkillType.WATER,    0, 15, "NONE"));

        // ── Psychic ──────────────────────────────────────────────────────────
        register(new Skill("Mind Pulse",      SkillType.PSYCHIC, 15, 15, "NONE"));
        register(new Skill("Rhythm Distort",  SkillType.PSYCHIC,  0, 15, "CONFUSE"));
        register(new Skill("Mental Break",    SkillType.PSYCHIC, 20, 20, "LOWER_DEF"));
        register(new Skill("Confusion Wave",  SkillType.PSYCHIC, 15, 15, "CONFUSE"));

        // ── Flying ───────────────────────────────────────────────────────────
        register(new Skill("Sky Dive",        SkillType.FLYING,  20, 20, "NONE"));
        register(new Skill("Jetstream Slash", SkillType.FLYING,  15, 15, "LOWER_SPD"));
        register(new Skill("Air Cutter",      SkillType.FLYING,  15, 15, "NONE"));
        register(new Skill("Wind Guard",      SkillType.FLYING,   0, 10, "NONE"));

        // ── Sand ─────────────────────────────────────────────────────────────
        register(new Skill("Prickly Trunk",   SkillType.SAND,   20, 20, "NONE"));
        register(new Skill("Sand Surge",      SkillType.SAND,   15, 15, "NONE"));
        register(new Skill("Dune Crash",      SkillType.SAND,   15, 15, "FLINCH"));
        register(new Skill("Desert Wall",     SkillType.SAND,    0, 10, "RAISE_DEF"));

        // ── Grass ────────────────────────────────────────────────────────────
        register(new Skill("Vine Lash",       SkillType.GRASS,  15, 15, "NONE"));
        register(new Skill("Leaf Strike",     SkillType.GRASS,  20, 20, "NONE"));
        register(new Skill("Jungle Fever",    SkillType.GRASS,   0, 15, "RAISE_ATK"));
        register(new Skill("Root Bind",       SkillType.GRASS,  15, 15, "NONE"));

        // ── Rock ─────────────────────────────────────────────────────────────
        register(new Skill("Rock Slam",       SkillType.ROCK,   20, 20, "FLINCH"));
        register(new Skill("Stone Guard",     SkillType.ROCK,    0, 15, "RAISE_DEF"));
        register(new Skill("Boulder Crash",   SkillType.ROCK,   15, 15, "NONE"));
        register(new Skill("Defense Curl",    SkillType.ROCK,    0, 10, "RAISE_DEF"));

        // ── Fire ─────────────────────────────────────────────────────────────
        register(new Skill("Flame Crash",     SkillType.FIRE,   20, 20, "BURN"));
        register(new Skill("Heat Burst",      SkillType.FIRE,   15, 15, "NONE"));
        register(new Skill("Inferno Dash",    SkillType.FIRE,   15, 15, "BURN"));
        register(new Skill("Burn Rush",       SkillType.FIRE,   15, 15, "BURN"));

        // ── Dark ─────────────────────────────────────────────────────────────
        register(new Skill("Shadow Jab",      SkillType.DARK,   15, 15, "NONE"));
        register(new Skill("Night Slash",     SkillType.DARK,   20, 20, "LOWER_ATK"));
        register(new Skill("Fear Stare",      SkillType.DARK,    0, 15, "LOWER_SPD"));
        register(new Skill("Ambush",          SkillType.DARK,   15, 15, "NONE"));

        // ── Poison ───────────────────────────────────────────────────────────
        register(new Skill("Toxic Steam",     SkillType.POISON, 20, 20, "NONE"));
        register(new Skill("Venom Drip",      SkillType.POISON, 15, 15, "NONE"));
        register(new Skill("Acid Spray",      SkillType.POISON, 15, 15, "LOWER_DEF"));
        register(new Skill("Poison Fang",     SkillType.POISON, 15, 15, "NONE"));

        // ── Signature Moves ──────────────────────────────────────────────────
        // Tung Tung Tung Sahur
        register(new Skill("Sahur Chant",     SkillType.FIGHTING, 15, 15, "LOWER_SPD"));
        register(new Skill("Wake-Up Call",    SkillType.FIGHTING, 10, 10, "NONE"));
        register(new Skill("Infinite Sahur",  SkillType.FIGHTING,  5,  5, "PARALYZE"));

        // Tralalero Tralala
        register(new Skill("Sneaker Dash",    SkillType.WATER,   20, 20, "NONE"));
        register(new Skill("Glitchy Rhythm",  SkillType.PSYCHIC, 15, 15, "CONFUSE"));
        register(new Skill("Neon Rave",       SkillType.PSYCHIC,  5,  5, "NONE"));

        // Bombardino Crocodilo
        register(new Skill("Caffeine Snap",   SkillType.WATER,   20, 20, "RAISE_SPD"));
        register(new Skill("Tail Missile",    SkillType.FLYING,  10, 10, "LOWER_DEF"));
        register(new Skill("Ristretto Nuke",  SkillType.FIRE,     5,  5, "BURN"));

        // Lirili Larila
        register(new Skill("Sandal Stomp",    SkillType.SAND,   15, 15, "FLINCH"));
        register(new Skill("Clock Rewind",    SkillType.SAND,    0, 10, "HEAL"));
        register(new Skill("Timeline Rot",    SkillType.SAND,    5,  5, "NONE"));

        // Brr Brr Patapim
        register(new Skill("Patapim Stomp",   SkillType.GRASS,  20, 20, "NONE"));
        register(new Skill("Nose Snort",      SkillType.ROCK,   15, 15, "PARALYZE"));
        register(new Skill("Forest Grunt",    SkillType.GRASS,   5,  5, "NONE"));

        // Boneca Ambalabu
        register(new Skill("Tire Burnout",    SkillType.FIRE,   20, 20, "BURN"));
        register(new Skill("Ribbit Roll",     SkillType.ROCK,   15, 15, "RAISE_DEF"));
        register(new Skill("Licking Loop",    SkillType.FIRE,   15, 15, "NONE"));

        // Udin Din Din Din Dun
        register(new Skill("Frequency Pulse", SkillType.PSYCHIC, 20, 20, "NONE"));
        register(new Skill("The Din-Stutter", SkillType.PSYCHIC, 15, 15, "LOWER_DEF"));
        register(new Skill("Brain Scramble",  SkillType.PSYCHIC, 15, 15, "NONE"));

        // Capuccino Assassino
        register(new Skill("Steam Vent",      SkillType.POISON, 20, 20, "NONE"));
        register(new Skill("Metal Frother",   SkillType.DARK,   15, 15, "NONE"));
        register(new Skill("Double Shot",     SkillType.DARK,   10, 10, "NONE"));
    }

    public static void register(Skill skill) {
        registry.put(skill.getName().toUpperCase(), skill);
    }

    public static Skill get(String name) {
        Skill skill = registry.get(name.toUpperCase());
        if (skill == null) System.err.println("Skill not found: " + name);
        return skill;
    }

    public static boolean has(String name) {
        return registry.containsKey(name.toUpperCase());
    }
}