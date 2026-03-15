package brainrots;

import skills.Skill;
import skills.SkillPool;
import skills.SkillType;
import java.util.List;
import java.util.Random;

/**
 * Creates BrainRot instances with randomized stats within tier ranges.
 * Stat ranges sourced from the game design document.
 */
public class BrainRotFactory {

    private static final Random rand = new Random();

    /**
     * Creates a BrainRot by registry name and desired tier.
     */
    public static BrainRot create(String name, Tier tier) {
        return switch (name.toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR" -> makeTungTung(tier);
            case "TRALALERO TRALALA"    -> makeTrala(tier);
            case "BOMBARDINO CROCODILO" -> makeBombardino(tier);
            case "LIRILI LARILA"        -> makeLirili(tier);
            case "BRR BRR PATAPIM"      -> makePatapim(tier);
            case "BONECA AMBALABU"      -> makeBoneca(tier);
            case "UDIN DIN DIN DIN DUN" -> makeUdin(tier);
            case "CAPUCCINO ASSASSINO"  -> makeCapuccino(tier);
            default -> throw new IllegalArgumentException("Unknown BrainRot: " + name);
        };
    }

    private static int rng(int min, int max) {
        return min + rand.nextInt(max - min + 1);
    }

    private static BrainRot build(String name, Type primary, Type secondary, Tier tier, int hp, int atk, int def, int spd, SkillType poolType) {
        BrainRot rot = new BrainRot(name, primary, secondary, tier, hp, atk, def, spd);
        assignStartingMoves(rot, name, poolType);
        return rot;
    }

    private static void assignStartingMoves(BrainRot rot, String name, SkillType poolType) {
        List<Skill> moves = SkillPool.getStartingMoves(name, poolType);
        for (Skill s : moves) rot.addMove(s);
    }

    private static BrainRot makeTungTung(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(80,120); spd = rng(18,28); def = rng(30,50); atk = rng(45,80); }
            case DIAMOND -> { hp = rng(120,170); spd = rng(28,35); def = rng(50,65); atk = rng(80,110); }
            default      -> { hp = rng(35,80);  spd = rng(8,18);  def = rng(15,30); atk = rng(20,45); }
        }
        return build("TUNG TUNG TUNG SAHUR", Type.FIGHTING, null, tier, hp, atk, def, spd, SkillType.FIGHTING);
    }

    private static BrainRot makeTrala(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(70,110); spd = rng(28,40); def = rng(20,35); atk = rng(50,85); }
            case DIAMOND -> { hp = rng(110,150); spd = rng(40,45); def = rng(35,50); atk = rng(85,115); }
            default      -> { hp = rng(30,70);  spd = rng(15,28); def = rng(8,20);  atk = rng(25,50); }
        }
        return build("TRALALERO TRALALA", Type.WATER, Type.PSYCHIC, tier, hp, atk, def, spd, SkillType.WATER);
    }

    private static BrainRot makeBombardino(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(85,130); spd = rng(22,32); def = rng(30,55); atk = rng(50,85); }
            case DIAMOND -> { hp = rng(130,175); spd = rng(32,42); def = rng(55,75); atk = rng(85,110); }
            default      -> { hp = rng(40,85);  spd = rng(12,22); def = rng(15,30); atk = rng(22,50); }
        }
        return build("BOMBARDINO CROCODILO", Type.WATER, Type.FLYING, tier, hp, atk, def, spd, SkillType.WATER);
    }

    private static BrainRot makeLirili(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(100,150); spd = rng(12,22); def = rng(40,65); atk = rng(35,70); }
            case DIAMOND -> { hp = rng(150,210); spd = rng(22,30); def = rng(65,85); atk = rng(70,95); }
            default      -> { hp = rng(55,100);  spd = rng(5,12);  def = rng(20,40); atk = rng(15,35); }
        }
        return build("LIRILI LARILA", Type.SAND, null, tier, hp, atk, def, spd, SkillType.SAND);
    }

    private static BrainRot makePatapim(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(95,145);  spd = rng(15,24); def = rng(35,60); atk = rng(40,75); }
            case DIAMOND -> { hp = rng(145,200); spd = rng(24,30); def = rng(60,85); atk = rng(75,100); }
            default      -> { hp = rng(50,95);   spd = rng(6,15);  def = rng(20,35); atk = rng(20,40); }
        }
        return build("BRR BRR PATAPIM", Type.GRASS, Type.ROCK, tier, hp, atk, def, spd, SkillType.GRASS);
    }

    private static BrainRot makeBoneca(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(95,145);  spd = rng(15,24); def = rng(35,60); atk = rng(40,75); }
            case DIAMOND -> { hp = rng(145,200); spd = rng(24,30); def = rng(60,85); atk = rng(75,100); }
            default      -> { hp = rng(50,95);   spd = rng(6,15);  def = rng(20,35); atk = rng(20,40); }
        }
        return build("BONECA AMBALABU", Type.FIRE, Type.ROCK, tier, hp, atk, def, spd, SkillType.FIRE);
    }

    private static BrainRot makeUdin(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(85,135);  spd = rng(18,28); def = rng(30,50); atk = rng(50,85); }
            case DIAMOND -> { hp = rng(135,180); spd = rng(28,35); def = rng(50,65); atk = rng(85,110); }
            default      -> { hp = rng(40,85);   spd = rng(8,18);  def = rng(15,30); atk = rng(25,50); }
        }
        return build("UDIN DIN DIN DIN DUN", Type.FIGHTING, null, tier, hp, atk, def, spd, SkillType.FIGHTING);
    }

    private static BrainRot makeCapuccino(Tier tier) {
        int hp, atk, def, spd;
        switch (tier) {
            case GOLD    -> { hp = rng(65,105);  spd = rng(30,42); def = rng(20,35); atk = rng(55,90); }
            case DIAMOND -> { hp = rng(105,145); spd = rng(42,45); def = rng(35,50); atk = rng(90,120); }
            default      -> { hp = rng(25,65);   spd = rng(18,30); def = rng(8,20);  atk = rng(30,55); }
        }
        return build("CAPUCCINO ASSASSINO", Type.DARK, Type.POISON, tier, hp, atk, def, spd, SkillType.DARK);
    }
}