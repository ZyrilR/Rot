package brainrots;

/**
 * Defines stat caps and per-level growth rates based on type role.
 *
 * Type roles:
 *   Tank         (Sand, Rock)     — highest HP, capped ATK (100), slow
 *   Glass Cannon (Psychic, Dark)  — low HP, high ATK/SPD
 *   Balanced     (everything else)— middle ground
 *
 * CORE STAT CONSTRAINTS (at Level 100):
 *   Speed  ≤ 45 (buffs cannot push beyond 50)
 *   HP     ≤ 210 (tanks highest, glass cannons lowest)
 *   Attack ≤ 120 (≤115 if Speed > 40, ≤100 for tanks)
 *   Defense≤ 85  (buffs cannot exceed +40% of base)
 */
public class StatGrowth {

    // ── Type role helpers ────────────────────────────────────────────────────

    public static boolean isTank(Type type) {
        return type == Type.SAND || type == Type.ROCK;
    }

    public static boolean isGlassCannon(Type type) {
        return type == Type.PSYCHIC || type == Type.DARK;
    }

    // ── Hard caps ────────────────────────────────────────────────────────────

    public static int hpCap(Type primaryType) {
        if (isTank(primaryType))        return 210;
        if (isGlassCannon(primaryType)) return 160;
        return 185;
    }

    public static int atkCap(Type primaryType, int baseSpeed) {
        if (isTank(primaryType))  return 100;
        if (baseSpeed > 40)       return 115;
        return 120;
    }

    public static int defCap() {
        return 85;
    }

    public static int spdCap() {
        return 45;
    }

    /** Absolute ceiling for buffed speed (no modifier can push past this). */
    public static int spdBuffCap() {
        return 50;
    }

    // ── Per-level growth ─────────────────────────────────────────────────────

    /** HP grows every level; tanks gain 2, others gain 1. */
    public static int hpGrowth(Type primaryType) {
        return isTank(primaryType) ? 2 : 1;
    }

    /** Attack grows by 1 every level. */
    public static int atkGrowth() {
        return 1;
    }

    /** Defense grows by 1 every other level. */
    public static int defGrowth(int level) {
        return (level % 2 == 0) ? 1 : 0;
    }

    /** Speed grows by 1 every 4th level. */
    public static int spdGrowth(int level) {
        return (level % 4 == 0) ? 1 : 0;
    }
}
