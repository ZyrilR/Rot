package brainrots;

import skills.Skill;
import skills.SkillRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines which skill each BrainRot learns at which level.
 *
 * BrainRots start with 2 moves at levels 1–9 (assigned by BrainRotFactory via SkillPool).
 * New skills are offered at levels 10, 15, 20, 25, and 30:
 *   Lv 10 — 3rd move (fills free slot → moveset grows to 3)
 *   Lv 15 — 4th move (fills free slot → moveset grows to 4)
 *   Lv 20, 25, 30 — further unlocks; moveset is full so the UI prompts to replace.
 * If the moveset is already full (4/4) the skill is still surfaced in
 * LevelUpResult.skillUnlocked so the UI can prompt the player to swap.
 */
public class LevelUpLearnset {

    // key = "ROTNAME|LEVEL"  value = skill name (exact key used in SkillRegistry)
    private static final Map<String, String> table = new HashMap<>();

    static {
        // ── TUNG TUNG TUNG SAHUR (FIGHTING) ─────────────────────────────────
        // Starting (Lv 1): Wooden Thump, Power Combo
        put("TUNG TUNG TUNG SAHUR", 10, "Counter Guard");
        put("TUNG TUNG TUNG SAHUR", 15, "Battle Cry");
        put("TUNG TUNG TUNG SAHUR", 20, "Wake-Up Call");
        put("TUNG TUNG TUNG SAHUR", 25, "Focus Stance");
        put("TUNG TUNG TUNG SAHUR", 30, "Infinite Sahur");

        // ── TRALALERO TRALALA (WATER / PSYCHIC) ──────────────────────────────
        // Starting (Lv 1): Aqua Dash, Tidal Crash
        put("TRALALERO TRALALA", 10, "Aqua Engine");
        put("TRALALERO TRALALA", 15, "Mist Veil");
        put("TRALALERO TRALALA", 20, "Glitchy Rhythm");
        put("TRALALERO TRALALA", 25, "Focus Stance");
        put("TRALALERO TRALALA", 30, "Neon Rave");

        // ── BOMBARDINO CROCODILO (WATER / FLYING) ─────────────────────────────
        // Starting (Lv 1): Aqua Dash, Tidal Crash
        put("BOMBARDINO CROCODILO", 10, "Aqua Engine");
        put("BOMBARDINO CROCODILO", 15, "Mist Veil");
        put("BOMBARDINO CROCODILO", 20, "Tail Missile");
        put("BOMBARDINO CROCODILO", 25, "Focus Stance");
        put("BOMBARDINO CROCODILO", 30, "Ristretto Nuke");

        // ── LIRILI LARILA (SAND) ──────────────────────────────────────────────
        // Starting (Lv 1): Prickly Trunk, Sand Surge
        put("LIRILI LARILA", 10, "Dune Crash");
        put("LIRILI LARILA", 15, "Desert Wall");
        put("LIRILI LARILA", 20, "Clock Rewind");
        put("LIRILI LARILA", 25, "Focus Stance");
        put("LIRILI LARILA", 30, "Timeline Rot");

        // ── BRR BRR PATAPIM (GRASS / ROCK) ───────────────────────────────────
        // Starting (Lv 1): Vine Lash, Leaf Strike
        put("BRR BRR PATAPIM", 10, "Jungle Fever");
        put("BRR BRR PATAPIM", 15, "Root Bind");
        put("BRR BRR PATAPIM", 20, "Nose Snort");
        put("BRR BRR PATAPIM", 25, "Focus Stance");
        put("BRR BRR PATAPIM", 30, "Forest Grunt");

        // ── BONECA AMBALABU (FIRE / ROCK) ────────────────────────────────────
        // Starting (Lv 1): Flame Crash, Heat Burst
        put("BONECA AMBALABU", 10, "Inferno Dash");
        put("BONECA AMBALABU", 15, "Burn Rush");
        put("BONECA AMBALABU", 20, "Ribbit Roll");
        put("BONECA AMBALABU", 25, "Focus Stance");
        put("BONECA AMBALABU", 30, "Licking Loop");

        // ── UDIN DIN DIN DIN DUN (FIGHTING) ──────────────────────────────────
        // Starting (Lv 1): Wooden Thump, Power Combo
        put("UDIN DIN DIN DIN DUN", 10, "Counter Guard");
        put("UDIN DIN DIN DIN DUN", 15, "Battle Cry");
        put("UDIN DIN DIN DIN DUN", 20, "The Din-Stutter");
        put("UDIN DIN DIN DIN DUN", 25, "Focus Stance");
        put("UDIN DIN DIN DIN DUN", 30, "Brain Scramble");

        // ── CAPUCCINO ASSASSINO (DARK / POISON) ───────────────────────────────
        // Starting (Lv 1): Shadow Jab, Night Slash
        put("CAPUCCINO ASSASSINO", 10, "Fear Stare");
        put("CAPUCCINO ASSASSINO", 15, "Ambush");
        put("CAPUCCINO ASSASSINO", 20, "Metal Frother");
        put("CAPUCCINO ASSASSINO", 25, "Focus Stance");
        put("CAPUCCINO ASSASSINO", 30, "Double Shot");
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
