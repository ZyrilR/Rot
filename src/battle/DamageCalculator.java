package battle;

import brainrots.BrainRot;
import brainrots.Type;
import skills.Skill;
import skills.SkillType;
import utils.MathUtil;

/**
 * Computes damage dealt by a skill from attacker to defender.
 * Formula: damage = (power * attack / defense) * typeMultiplier
 */
public class DamageCalculator {

    /**
     * Calculates and returns the final damage value.
     */
    public static int calculate(Skill skill, BrainRot attacker, BrainRot defender) {
        if (skill.getPower() <= 0) return 0; // non-damaging skill

        double base = (double) skill.getPower() * attacker.getAttack() / defender.getDefense();
        double typeMultiplier = getTypeMultiplier(skill.getType(), defender.getPrimaryType(), defender.getSecondaryType());
        double randomFactor   = 0.85 + Math.random() * 0.15; // 85–100% variance

        int damage = (int)(base * typeMultiplier * randomFactor);

        // Defense cannot reduce damage below 20% of the unmitigated value
        int rawDamage = (int)(skill.getPower() * attacker.getAttack() * typeMultiplier * randomFactor);
        int minDamage = Math.max(1, (int)(rawDamage * 0.20));
        damage = MathUtil.clamp(damage, minDamage, 9999);

        if (typeMultiplier > 1.0) System.out.println("It's super effective!");
        if (typeMultiplier < 1.0) System.out.println("It's not very effective...");

        return damage;
    }

    /**
     * Kato gihimo ni Chrisnel!!!!!!!!! ILY
     *
     * Type effectiveness chart.
     * Constraints: no type has more than 2 advantages
     *
     * FIGHTING > ROCK
     * ROCK     > FIRE
     * FIRE     > GRASS
     * GRASS    > WATER
     * WATER    > FIRE
     * PSYCHIC  > FIGHTING
     * DARK     > PSYCHIC
     * FLYING   > GRASS
     * SAND     > FIRE
     */
    private static double getTypeMultiplier(SkillType attackType, Type defPrimary, Type defSecondary) {
        double multiplier = 1.0;
        multiplier *= singleMatchup(attackType, defPrimary);
        if (defSecondary != null) {
            multiplier *= singleMatchup(attackType, defSecondary);
        }
        return multiplier;
    }

    private static double singleMatchup(SkillType atk, Type def) {
        return switch (atk) {
            case FIGHTING -> (def == Type.ROCK)     ? 1.5 : 1.0;
            case ROCK     -> (def == Type.FIRE)     ? 1.5 : 1.0;
            case FIRE     -> (def == Type.GRASS)    ? 1.5 : 1.0;
            case GRASS    -> (def == Type.WATER)    ? 1.5 : 1.0;
            case WATER    -> (def == Type.FIRE)     ? 1.5 : 1.0;
            case PSYCHIC  -> (def == Type.FIGHTING) ? 1.5 : 1.0;
            case DARK     -> (def == Type.PSYCHIC)  ? 1.5 : 1.0;
            case FLYING   -> (def == Type.GRASS)    ? 1.5 : 1.0;
            case SAND     -> (def == Type.FIRE)     ? 1.5 : 1.0;
            default       -> 1.0;
        };
    }
}