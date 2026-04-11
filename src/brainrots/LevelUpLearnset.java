package brainrots;

import skills.Skill;
import skills.SkillRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines which skill each BrainRot learns at which level.
 *
 * BrainRots start with 4 moves (assigned by BrainRotFactory via SkillPool).
 * New skills are offered at levels 5, 10, 15, 20, 25, and 30.
 * If the moveset is already full (4/4) the skill is still surfaced in
 * LevelUpResult.skillUnlocked so the UI can prompt the player to swap.
 *
 * Layout per BrainRot:
 *   Lv 5  – 3rd type-pool move
 *   Lv 10 – 4th type-pool move (or secondary-type move)
 *   Lv 15 – 2nd signature move
 *   Lv 20 – utility (NORMAL pool)
 *   Lv 25 – 3rd signature move
 *   Lv 30 – secondary-type or extra utility
 */
public class LevelUpLearnset {

    // key = "ROTNAME|LEVEL"  value = skill name (exact key used in SkillRegistry)
    private static final Map<String, String> table = new HashMap<>();

    static {
        // ── TUNG TUNG TUNG SAHUR (FIGHTING) ─────────────────────────────────
        // Starting: Wooden Thump, Power Combo, Guard Up, Sahur Chant
        put("TUNG TUNG TUNG SAHUR",  5, "Counter Guard");
        put("TUNG TUNG TUNG SAHUR", 10, "Battle Cry");
        put("TUNG TUNG TUNG SAHUR", 15, "Wake-Up Call");
        put("TUNG TUNG TUNG SAHUR", 20, "Focus Stance");
        put("TUNG TUNG TUNG SAHUR", 25, "Infinite Sahur");
        put("TUNG TUNG TUNG SAHUR", 30, "Quick Step");

        // ── TRALALERO TRALALA (WATER / PSYCHIC) ──────────────────────────────
        // Starting: Aqua Dash, Tidal Crash, Guard Up, Sneaker Dash
        put("TRALALERO TRALALA",  5, "Aqua Engine");
        put("TRALALERO TRALALA", 10, "Mist Veil");
        put("TRALALERO TRALALA", 15, "Glitchy Rhythm");
        put("TRALALERO TRALALA", 20, "Focus Stance");
        put("TRALALERO TRALALA", 25, "Neon Rave");
        put("TRALALERO TRALALA", 30, "Mind Pulse");

        // ── BOMBARDINO CROCODILO (WATER / FLYING) ─────────────────────────────
        // Starting: Aqua Dash, Tidal Crash, Guard Up, Caffeine Snap
        put("BOMBARDINO CROCODILO",  5, "Aqua Engine");
        put("BOMBARDINO CROCODILO", 10, "Mist Veil");
        put("BOMBARDINO CROCODILO", 15, "Tail Missile");
        put("BOMBARDINO CROCODILO", 20, "Focus Stance");
        put("BOMBARDINO CROCODILO", 25, "Ristretto Nuke");
        put("BOMBARDINO CROCODILO", 30, "Sky Dive");

        // ── LIRILI LARILA (SAND) ──────────────────────────────────────────────
        // Starting: Prickly Trunk, Sand Surge, Guard Up, Sandal Stomp
        put("LIRILI LARILA",  5, "Dune Crash");
        put("LIRILI LARILA", 10, "Desert Wall");
        put("LIRILI LARILA", 15, "Clock Rewind");
        put("LIRILI LARILA", 20, "Focus Stance");
        put("LIRILI LARILA", 25, "Timeline Rot");
        put("LIRILI LARILA", 30, "Rest");

        // ── BRR BRR PATAPIM (GRASS / ROCK) ───────────────────────────────────
        // Starting: Vine Lash, Leaf Strike, Guard Up, Patapim Stomp
        put("BRR BRR PATAPIM",  5, "Jungle Fever");
        put("BRR BRR PATAPIM", 10, "Root Bind");
        put("BRR BRR PATAPIM", 15, "Nose Snort");
        put("BRR BRR PATAPIM", 20, "Focus Stance");
        put("BRR BRR PATAPIM", 25, "Forest Grunt");
        put("BRR BRR PATAPIM", 30, "Rock Slam");

        // ── BONECA AMBALABU (FIRE / ROCK) ────────────────────────────────────
        // Starting: Flame Crash, Heat Burst, Guard Up, Tire Burnout
        put("BONECA AMBALABU",  5, "Inferno Dash");
        put("BONECA AMBALABU", 10, "Burn Rush");
        put("BONECA AMBALABU", 15, "Ribbit Roll");
        put("BONECA AMBALABU", 20, "Focus Stance");
        put("BONECA AMBALABU", 25, "Licking Loop");
        put("BONECA AMBALABU", 30, "Rock Slam");

        // ── UDIN DIN DIN DIN DUN (FIGHTING) ──────────────────────────────────
        // Starting: Wooden Thump, Power Combo, Guard Up, Frequency Pulse
        put("UDIN DIN DIN DIN DUN",  5, "Counter Guard");
        put("UDIN DIN DIN DIN DUN", 10, "Battle Cry");
        put("UDIN DIN DIN DIN DUN", 15, "The Din-Stutter");
        put("UDIN DIN DIN DIN DUN", 20, "Focus Stance");
        put("UDIN DIN DIN DIN DUN", 25, "Brain Scramble");
        put("UDIN DIN DIN DIN DUN", 30, "Mind Pulse");

        // ── CAPUCCINO ASSASSINO (DARK / POISON) ───────────────────────────────
        // Starting: Shadow Jab, Night Slash, Guard Up, Steam Vent
        put("CAPUCCINO ASSASSINO",  5, "Fear Stare");
        put("CAPUCCINO ASSASSINO", 10, "Ambush");
        put("CAPUCCINO ASSASSINO", 15, "Metal Frother");
        put("CAPUCCINO ASSASSINO", 20, "Focus Stance");
        put("CAPUCCINO ASSASSINO", 25, "Double Shot");
        put("CAPUCCINO ASSASSINO", 30, "Toxic Steam");
    }

    private static void put(String rotName, int level, String skillName) {
        table.put(rotName.toUpperCase() + "|" + level, skillName);
    }

    /**
     * Returns the Skill offered at {@code level} for {@code rotName},
     * or {@code null} if no skill is scheduled for that level.
     */
    public static Skill getSkillAt(String rotName, int level) {
        String key = rotName.toUpperCase() + "|" + level;
        String skillName = table.get(key);
        if (skillName == null) return null;
        return SkillRegistry.get(skillName);
    }
}
