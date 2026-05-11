package brainrots;

import skills.Skill;
import skills.SkillPool;
import skills.SkillType;
import utils.RandomUtil;

import java.util.List;

import static utils.Constants.getDescription;

/**
 * Creates BrainRot instances with randomized stats within tier ranges.
 *
 * Stats are rolled at level 1 (small ranges around HP 15-20) and then
 * scaled up to the target level via the same per-level growth used by
 * normal level-ups, so a freshly-created level-N rot has the same stat
 * trajectory as one that grinded its way there.
 */
public class BrainRotFactory {

    /**
     * Creates a BrainRot by registry name and level. The tier (evolution stage) is
     * derived from the level via {@link Tier#fromLevel(int)}.
     */
    public static BrainRot create(String name, int level) {
        return switch (name.toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR" -> makeTungTung(level);
            case "TRALALERO TRALALA"    -> makeTrala(level);
            case "BOMBARDINO CROCODILO" -> makeBombardino(level);
            case "LIRILI LARILA"        -> makeLirili(level);
            case "BRR BRR PATAPIM"      -> makePatapim(level);
            case "BONECA AMBALABU"      -> makeBoneca(level);
            case "UDIN DIN DIN DIN DUN" -> makeUdin(level);
            case "CAPUCCINO ASSASSINO"  -> makeCapuccino(level);
            default -> throw new IllegalArgumentException("Unknown BrainRot: " + name);
        };
    }

    /**
     * Builds a BrainRot at level 1 with the supplied base stats, then scales it
     * up to {@code targetLevel} by simulating level-ups (applying the normal
     * StatGrowth gains), and finally assigns its starting moveset.
     */
    private static BrainRot build(String name,
                                  Type primary, Type secondary,
                                  int targetLevel,
                                  int hp, int atk, int def, int spd,
                                  SkillType poolType) {
        BrainRot rot = new BrainRot(name, primary, secondary, Tier.NORMAL, 1, hp, atk, def, spd);
        scaleToLevel(rot, targetLevel);
        assignStartingMoves(rot, name, poolType, rot.getLevel());
        rot.restoreForBattle();
        return rot;
    }

    /** Advances {@code rot} to {@code target} level using the in-game gainXp pipeline. */
    private static void scaleToLevel(BrainRot rot, int target) {
        int safety = 0;
        while (rot.getLevel() < target && safety++ < 200) {
            rot.gainXp(rot.getXpToNextLevel());
        }
    }

    private static void assignStartingMoves(BrainRot rot, String name, SkillType poolType, int level) {
        List<Skill> moves = SkillPool.getStartingMoves(name, poolType, level);
        for (Skill s : moves) rot.addMove(s);
    }

    /**
     * Creates an enemy BrainRot (wild encounter or trainer) with a randomized moveset
     * drawn from pools that match the rot's primary/secondary types and signature pool.
     * Move count is level-gated (2 moves at 1–9, 3 at 10–14, 4 at 15+).
     */
    public static BrainRot createEnemy(String name, int level) {
        BrainRot rot = create(name, level);
        rot.getMoves().clear();
        SkillType primary   = toSkillType(rot.getPrimaryType());
        SkillType secondary = toSkillType(rot.getSecondaryType());
        List<Skill> moves = SkillPool.getRandomMoves(rot.getName(), primary, secondary, level);
        for (Skill s : moves) rot.addMove(s);
        return rot;
    }

    private static SkillType toSkillType(Type t) {
        return t == null ? null : SkillType.valueOf(t.name());
    }

    // ── Level-1 base stat creators ────────────────────────────────────────────
    // HP is anchored at 15–20 per request; ATK/DEF/SPD nudge toward each rot's
    // type role (tank, glass cannon, balanced) while staying small.

    private static BrainRot makeTungTung(int level) {
        return build("TUNG TUNG TUNG SAHUR",
                Type.FIGHTING, null, level,
                RandomUtil.range(17, 20),  // hp
                RandomUtil.range(11, 13),  // atk
                RandomUtil.range(6, 8),    // def
                RandomUtil.range(6, 8),    // spd
                SkillType.FIGHTING);
    }

    private static BrainRot makeTrala(int level) {
        return build("TRALALERO TRALALA",
                Type.WATER, Type.PSYCHIC, level,
                RandomUtil.range(15, 18),
                RandomUtil.range(11, 13),
                RandomUtil.range(5, 7),
                RandomUtil.range(9, 11),
                SkillType.WATER);
    }

    private static BrainRot makeBombardino(int level) {
        return build("BOMBARDINO CROCODILO",
                Type.WATER, Type.FLYING, level,
                RandomUtil.range(18, 20),
                RandomUtil.range(11, 13),
                RandomUtil.range(7, 9),
                RandomUtil.range(7, 9),
                SkillType.WATER);
    }

    private static BrainRot makeLirili(int level) {
        return build("LIRILI LARILA",
                Type.SAND, null, level,
                RandomUtil.range(19, 20),
                RandomUtil.range(9, 11),
                RandomUtil.range(9, 11),
                RandomUtil.range(4, 6),
                SkillType.SAND);
    }

    private static BrainRot makePatapim(int level) {
        return build("BRR BRR PATAPIM",
                Type.GRASS, Type.ROCK, level,
                RandomUtil.range(18, 20),
                RandomUtil.range(10, 12),
                RandomUtil.range(8, 10),
                RandomUtil.range(5, 7),
                SkillType.GRASS);
    }

    private static BrainRot makeBoneca(int level) {
        return build("BONECA AMBALABU",
                Type.FIRE, Type.ROCK, level,
                RandomUtil.range(18, 20),
                RandomUtil.range(10, 12),
                RandomUtil.range(8, 10),
                RandomUtil.range(5, 7),
                SkillType.FIRE);
    }

    private static BrainRot makeUdin(int level) {
        return build("UDIN DIN DIN DIN DUN",
                Type.FIGHTING, null, level,
                RandomUtil.range(17, 20),
                RandomUtil.range(11, 13),
                RandomUtil.range(6, 8),
                RandomUtil.range(6, 8),
                SkillType.FIGHTING);
    }

    private static BrainRot makeCapuccino(int level) {
        return build("CAPUCCINO ASSASSINO",
                Type.DARK, Type.POISON, level,
                RandomUtil.range(15, 17),
                RandomUtil.range(12, 14),
                RandomUtil.range(5, 7),
                RandomUtil.range(9, 11),
                SkillType.DARK);
    }
}
