package brainrots;

import skills.Skill;
import skills.SkillPool;
import skills.SkillType;
import utils.RandomUtil;

import java.util.List;

import static utils.Constants.getDescription;

/**
 * Creates BrainRot instances with randomized stats within tier ranges.
 */
public class BrainRotFactory {

    /**
     * Creates a BrainRot by registry name and level. The tier (evolution stage) is
     * derived from the level via {@link Tier#fromLevel(int)}.
     */
    public static BrainRot create(String name, int level) {
        Tier tier = Tier.fromLevel(level);
        return switch (name.toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR" -> makeTungTung(tier, level);
            case "TRALALERO TRALALA"    -> makeTrala(tier, level);
            case "BOMBARDINO CROCODILO" -> makeBombardino(tier, level);
            case "LIRILI LARILA"        -> makeLirili(tier, level);
            case "BRR BRR PATAPIM"      -> makePatapim(tier, level);
            case "BONECA AMBALABU"      -> makeBoneca(tier, level);
            case "UDIN DIN DIN DIN DUN" -> makeUdin(tier, level);
            case "CAPUCCINO ASSASSINO"  -> makeCapuccino(tier, level);
            default -> throw new IllegalArgumentException("Unknown BrainRot: " + name);
        };
    }

    private static BrainRot build(String name,
                                  Type primary, Type secondary, Tier tier, int level,
                                  int hp, int atk, int def, int spd, SkillType poolType) {
        BrainRot rot = new BrainRot(name, primary, secondary, tier, level, hp, atk, def, spd);
        assignStartingMoves(rot, name, poolType, level);
        return rot;
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

    // ---------- BrainRot creators ----------
    private static BrainRot makeTungTung(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(80,120);  spd = RandomUtil.range(18,28); def = RandomUtil.range(30,50); atk = RandomUtil.range(45,80);  }
            case DIAMOND -> { hp = RandomUtil.range(120,170); spd = RandomUtil.range(28,35); def = RandomUtil.range(50,65); atk = RandomUtil.range(80,110); }
            default      -> { hp = RandomUtil.range(35,80);   spd = RandomUtil.range(8,18);  def = RandomUtil.range(15,30); atk = RandomUtil.range(20,45);  }
        }
        return build("TUNG TUNG TUNG SAHUR",
                Type.FIGHTING, null, tier, level, hp, atk, def, spd, SkillType.FIGHTING);
    }

    private static BrainRot makeTrala(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(70,110);  spd = RandomUtil.range(28,40); def = RandomUtil.range(20,35); atk = RandomUtil.range(50,85);  }
            case DIAMOND -> { hp = RandomUtil.range(110,150); spd = RandomUtil.range(40,45); def = RandomUtil.range(35,50); atk = RandomUtil.range(85,115); }
            default      -> { hp = RandomUtil.range(30,70);   spd = RandomUtil.range(15,28); def = RandomUtil.range(8,20);  atk = RandomUtil.range(25,50);  }
        }
        return build("TRALALERO TRALALA",
                Type.WATER, Type.PSYCHIC, tier, level, hp, atk, def, spd, SkillType.WATER);
    }

    private static BrainRot makeBombardino(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(85,130);  spd = RandomUtil.range(22,32); def = RandomUtil.range(30,55); atk = RandomUtil.range(50,85);  }
            case DIAMOND -> { hp = RandomUtil.range(130,175); spd = RandomUtil.range(32,42); def = RandomUtil.range(55,75); atk = RandomUtil.range(85,110); }
            default      -> { hp = RandomUtil.range(40,85);   spd = RandomUtil.range(12,22); def = RandomUtil.range(15,30); atk = RandomUtil.range(22,50);  }
        }
        return build("BOMBARDINO CROCODILO",
                Type.WATER, Type.FLYING, tier, level, hp, atk, def, spd, SkillType.WATER);
    }

    private static BrainRot makeLirili(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(100,150); spd = RandomUtil.range(12,22); def = RandomUtil.range(40,65); atk = RandomUtil.range(35,70); }
            case DIAMOND -> { hp = RandomUtil.range(150,210); spd = RandomUtil.range(22,30); def = RandomUtil.range(65,85); atk = RandomUtil.range(70,95); }
            default      -> { hp = RandomUtil.range(55,100);  spd = RandomUtil.range(5,12);  def = RandomUtil.range(20,40); atk = RandomUtil.range(15,35); }
        }
        return build("LIRILI LARILA",
                Type.SAND, null, tier, level, hp, atk, def, spd, SkillType.SAND);
    }

    private static BrainRot makePatapim(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(95,145);  spd = RandomUtil.range(15,24); def = RandomUtil.range(35,60); atk = RandomUtil.range(40,75);  }
            case DIAMOND -> { hp = RandomUtil.range(145,200); spd = RandomUtil.range(24,30); def = RandomUtil.range(60,85); atk = RandomUtil.range(75,100); }
            default      -> { hp = RandomUtil.range(50,95);   spd = RandomUtil.range(6,15);  def = RandomUtil.range(20,35); atk = RandomUtil.range(20,40);  }
        }
        return build("BRR BRR PATAPIM",
                Type.GRASS, Type.ROCK, tier, level, hp, atk, def, spd, SkillType.GRASS);
    }

    private static BrainRot makeBoneca(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(95,145);  spd = RandomUtil.range(15,24); def = RandomUtil.range(35,60); atk = RandomUtil.range(40,75);  }
            case DIAMOND -> { hp = RandomUtil.range(145,200); spd = RandomUtil.range(24,30); def = RandomUtil.range(60,85); atk = RandomUtil.range(75,100); }
            default      -> { hp = RandomUtil.range(50,95);   spd = RandomUtil.range(6,15);  def = RandomUtil.range(20,35); atk = RandomUtil.range(20,40);  }
        }
        return build("BONECA AMBALABU",
                Type.FIRE, Type.ROCK, tier, level, hp, atk, def, spd, SkillType.FIRE);
    }

    private static BrainRot makeUdin(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(85,135);  spd = RandomUtil.range(18,28); def = RandomUtil.range(30,50); atk = RandomUtil.range(50,85);  }
            case DIAMOND -> { hp = RandomUtil.range(135,180); spd = RandomUtil.range(28,35); def = RandomUtil.range(50,65); atk = RandomUtil.range(85,110); }
            default      -> { hp = RandomUtil.range(40,85);   spd = RandomUtil.range(8,18);  def = RandomUtil.range(15,30); atk = RandomUtil.range(25,50);  }
        }
        return build("UDIN DIN DIN DIN DUN",
                Type.FIGHTING, null, tier, level, hp, atk, def, spd, SkillType.FIGHTING);
    }

    private static BrainRot makeCapuccino(Tier tier, int level) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = RandomUtil.range(65,105);  spd = RandomUtil.range(30,42); def = RandomUtil.range(20,35); atk = RandomUtil.range(55,90);  }
            case DIAMOND -> { hp = RandomUtil.range(105,145); spd = RandomUtil.range(42,45); def = RandomUtil.range(35,50); atk = RandomUtil.range(90,120); }
            default      -> { hp = RandomUtil.range(25,65);   spd = RandomUtil.range(18,30); def = RandomUtil.range(8,20);  atk = RandomUtil.range(30,55);  }
        }
        return build("CAPUCCINO ASSASSINO",
                Type.DARK, Type.POISON, tier, level, hp, atk, def, spd, SkillType.DARK);
    }
}
