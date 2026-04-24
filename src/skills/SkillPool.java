package skills;

import utils.RandomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups skills by SkillType and by BrainRot name (signature moves).
 * Used by BrainRotFactory to assign starting moves and handle level up unlocks.
 */
public class SkillPool {

    // Pools by SkillType
    private static final HashMap<SkillType, List<String>> typePools = new HashMap<>();

    // Signature pools by BrainRot name
    private static final HashMap<String, List<String>> signaturePools = new HashMap<>();

    static {
        typePools.put(SkillType.NORMAL,   List.of("Guard Up", "Focus Stance", "Quick Step", "Rest", "Speed Boost", "Fortify", "Evasion Up"));
        typePools.put(SkillType.FIGHTING, List.of("Wooden Thump", "Power Combo", "Counter Guard", "Battle Cry"));
        typePools.put(SkillType.WATER,    List.of("Aqua Dash", "Tidal Crash", "Aqua Engine", "Mist Veil"));
        typePools.put(SkillType.PSYCHIC,  List.of("Mind Pulse", "Rhythm Distort", "Mental Break", "Confusion Wave"));
        typePools.put(SkillType.FLYING,   List.of("Sky Dive", "Jetstream Slash", "Air Cutter", "Wind Guard"));
        typePools.put(SkillType.SAND,     List.of("Prickly Trunk", "Sand Surge", "Dune Crash", "Desert Wall"));
        typePools.put(SkillType.GRASS,    List.of("Vine Lash", "Leaf Strike", "Jungle Fever", "Root Bind"));
        typePools.put(SkillType.ROCK,     List.of("Rock Slam", "Stone Guard", "Boulder Crash", "Defense Curl"));
        typePools.put(SkillType.FIRE,     List.of("Flame Crash", "Heat Burst", "Inferno Dash", "Burn Rush"));
        typePools.put(SkillType.DARK,     List.of("Shadow Jab", "Night Slash", "Fear Stare", "Ambush"));
        typePools.put(SkillType.POISON,   List.of("Toxic Steam", "Venom Drip", "Acid Spray", "Poison Fang"));

        signaturePools.put("TUNG TUNG TUNG SAHUR", List.of("Sahur Chant", "Wake-Up Call", "Infinite Sahur"));
        signaturePools.put("TRALALERO TRALALA",     List.of("Sneaker Dash", "Glitchy Rhythm", "Neon Rave"));
        signaturePools.put("BOMBARDINO CROCODILO",  List.of("Caffeine Snap", "Tail Missile", "Ristretto Nuke"));
        signaturePools.put("LIRILI LARILA",         List.of("Sandal Stomp", "Clock Rewind", "Timeline Rot"));
        signaturePools.put("BRR BRR PATAPIM",       List.of("Patapim Stomp", "Nose Snort", "Forest Grunt"));
        signaturePools.put("BONECA AMBALABU",       List.of("Tire Burnout", "Ribbit Roll", "Licking Loop"));
        signaturePools.put("UDIN DIN DIN DIN DUN",  List.of("Frequency Pulse", "The Din-Stutter", "Brain Scramble"));
        signaturePools.put("CAPUCCINO ASSASSINO",   List.of("Steam Vent", "Metal Frother", "Double Shot"));
    }

    // ── Scroll validation helpers ─────────────────────────────────────────────

    /**
     * Returns true if {@code skillName} belongs to ANY signature pool.
     * Use this first to distinguish signature from type-pool skills.
     */
    public static boolean isSignatureMove(String skillName) {
        for (List<String> sigs : signaturePools.values()) {
            if (sigs.stream().anyMatch(s -> s.equalsIgnoreCase(skillName))) return true;
        }
        return false;
    }

    /**
     * Returns true if {@code skillName} is a signature move AND
     * {@code rotName} is its designated owner.
     *
     * Returns false if:
     *  - The skill is not a signature at all (caller should check isSignatureMove first).
     *  - The skill belongs to a different BrainRot.
     */
    public static boolean isSignatureOf(String skillName, String rotName) {
        List<String> ownerSigs = signaturePools.get(rotName.toUpperCase());
        if (ownerSigs == null) return false;
        return ownerSigs.stream().anyMatch(s -> s.equalsIgnoreCase(skillName));
    }

    // ── Starting moves ────────────────────────────────────────────────────────

    /**
     * Number of moves a BrainRot should have at a given level.
     *   Lv 1–9   → 2 moves
     *   Lv 10–14 → 3 moves
     *   Lv 15+   → 4 moves
     */
    public static int movesAtLevel(int level) {
        if (level < 10) return 2;
        if (level < 15) return 3;
        return 4;
    }

    /**
     * Returns the starting moveset for a BrainRot, trimmed to the count allowed
     * at {@code level} via {@link #movesAtLevel(int)}.
     * Order: 2 primary-type moves → 1 NORMAL utility → 1 signature.
     */
    public static List<Skill> getStartingMoves(String rotName, SkillType primaryType, int level) {
        int cap = movesAtLevel(level);
        List<Skill> moves = new ArrayList<>();

        List<String> typeSkills = typePools.getOrDefault(primaryType, new ArrayList<>());
        for (int i = 0; i < Math.min(2, typeSkills.size()) && moves.size() < cap; i++) {
            moves.add(SkillRegistry.get(typeSkills.get(i)));
        }

        if (moves.size() < cap) {
            List<String> normals = typePools.get(SkillType.NORMAL);
            if (normals != null && !normals.isEmpty()) moves.add(SkillRegistry.get(normals.get(0)));
        }

        if (moves.size() < cap) {
            List<String> sigs = signaturePools.get(rotName.toUpperCase());
            if (sigs != null && !sigs.isEmpty()) moves.add(SkillRegistry.get(sigs.get(0)));
        }

        return moves;
    }

    /**
     * Returns a randomized moveset for an enemy BrainRot, sized per
     * {@link #movesAtLevel(int)}, still respecting types and signature pool.
     * Fill priority (stops at the level cap):
     *   1. primary-type move
     *   2. signature move
     *   3. secondary-type move (primary if single-typed)
     *   4. NORMAL utility move
     * Duplicates across slots are avoided when possible.
     */
    public static List<Skill> getRandomMoves(String rotName, SkillType primary, SkillType secondary, int level) {
        int cap = movesAtLevel(level);
        List<Skill> moves = new ArrayList<>();
        Set<String> used = new HashSet<>();

        if (moves.size() < cap) pickRandomInto(moves, used, typePools.get(primary));
        if (moves.size() < cap) pickRandomInto(moves, used, signaturePools.get(rotName.toUpperCase()));
        if (moves.size() < cap) pickRandomInto(moves, used, typePools.get(secondary != null ? secondary : primary));
        if (moves.size() < cap) pickRandomInto(moves, used, typePools.get(SkillType.NORMAL));

        return moves;
    }

    private static void pickRandomInto(List<Skill> moves, Set<String> used, List<String> pool) {
        if (pool == null || pool.isEmpty()) return;
        List<String> candidates = new ArrayList<>();
        for (String n : pool) if (!used.contains(n)) candidates.add(n);
        if (candidates.isEmpty()) return;
        String chosen = candidates.get(RandomUtil.range(0, candidates.size() - 1));
        used.add(chosen);
        Skill skill = SkillRegistry.get(chosen);
        if (skill != null) moves.add(skill);
    }

    // ── Pool accessors ────────────────────────────────────────────────────────

    /**
     * Returns all type pool skills for a given SkillType.
     */
    public static List<Skill> getTypePool(SkillType type) {
        List<Skill> result = new ArrayList<>();
        for (String name : typePools.getOrDefault(type, new ArrayList<>())) {
            result.add(SkillRegistry.get(name));
        }
        return result;
    }

    /**
     * Returns all signature skills for a BrainRot by name.
     */
    public static List<Skill> getSignaturePool(String rotName) {
        List<Skill> result = new ArrayList<>();
        List<String> sigs = signaturePools.get(rotName.toUpperCase());
        if (sigs != null) {
            for (String name : sigs) result.add(SkillRegistry.get(name));
        }
        return result;
    }
}

//TODO ana si Din siya ra daw ani
//    public void getSkillAssets() {
//        for (int i = 1; i <= AssetManager.tiles; i++) {
//            tiles.add(new Tile(AssetManager.getImage("tiles_" + i)));
//            System.out.println("Added (Size|" + tiles.size() + "): " + ("tiles_" + i));
//        }
//    }
