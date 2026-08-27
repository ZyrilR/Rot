package battle;

import brainrots.BrainRot;
import brainrots.Type;
import engine.GamePanel;
import skills.Skill;
import skills.SkillType;

/**
 * Computes damage dealt by a skill from attacker to defender.
 * Includes "Tutorial Plot Armor" for new players.
 */
public class DamageCalculator {

    /**
     * Non-random damage information used by the battle UI and tactical AI.
     * Keeping previews here guarantees they stay in sync with real combat math.
     */
    public static final class DamageRange {
        public final int min;
        public final int max;
        public final double effectiveness;

        private DamageRange(int min, int max, double effectiveness) {
            this.min = min;
            this.max = max;
            this.effectiveness = effectiveness;
        }

        public int average() {
            return (min + max) / 2;
        }
    }

    public static int calculate(Skill skill, BrainRot attacker, BrainRot defender, GamePanel gp) {
        if (skill.getPower() <= 0) return 0;

        double randomFactor = 0.90 + (Math.random() * 0.10);
        return calculateAtFactor(skill, attacker, defender, randomFactor);
    }

    /** Returns the possible damage range without consuming combat randomness. */
    public static DamageRange preview(Skill skill, BrainRot attacker, BrainRot defender) {
        if (skill == null || attacker == null || defender == null || skill.getPower() <= 0) {
            return new DamageRange(0, 0, 1.0);
        }

        int min = calculateAtFactor(skill, attacker, defender, 0.90);
        int max = calculateAtFactor(skill, attacker, defender, 1.00);
        return new DamageRange(Math.min(min, max), Math.max(min, max),
                effectiveness(skill, defender));
    }

    private static int calculateAtFactor(Skill skill, BrainRot attacker,
                                         BrainRot defender, double randomFactor) {
        if (skill.getPower() <= 0) return 0;

        double raw = ((double) skill.getPower() * attacker.getAttack()) / (defender.getDefense() + 15);
        double baseDamage = (raw * 0.85) + 5;

        double typeMultiplier = getTypeMultiplier(skill.getType(), defender.getPrimaryType(), defender.getSecondaryType());

        int finalDamage = (int)(baseDamage * typeMultiplier * randomFactor);
        finalDamage = Math.max(1, finalDamage);

        // ── TUTORIAL PLOT ARMOR (THE "ALWAYS WIN" RULE) ──
        // Check if player is on the beginner map
//        if (gp != null && gp.CURRENT_PATH.contains("Route131")) {
//            int totalRots = gp.player.getPCSYSTEM().getPartySize() + gp.player.getPCSYSTEM().getPCCount();
//
//            // If the player only owns their Starter BrainRot...
//            if (totalRots == 1) {
//                // Figure out who is attacking
//                boolean isPlayerAttacking = gp.player.getPCSYSTEM().getParty().contains(attacker);
//
//                if (isPlayerAttacking) {
//                    // 1. Player hits 50% harder!
//                    finalDamage = (int)(finalDamage * 1.5);
//                } else {
//                    // 2. Enemy hits 50% weaker!
//                    finalDamage = (int)(finalDamage * 0.5);
//
//                    // 3. Focus Sash Effect: Enemy CANNOT deal the killing blow!
//                    if (finalDamage >= defender.getCurrentHp()) {
//                        finalDamage = Math.max(0, defender.getCurrentHp() - 1);
//                        System.out.println("Tutorial Armor saved you from fainting!");
//                    }
//                }
//            }
//        }

        return finalDamage;
    }

    /** Convenience: effectiveness of a skill against a defender (1.0 = neutral, >1 SE, <1 NVE, 0 immune). */
    public static double effectiveness(Skill skill, BrainRot defender) {
        return getTypeMultiplier(skill.getType(), defender.getPrimaryType(), defender.getSecondaryType());
    }

    public static double getTypeMultiplier(SkillType attackType, Type defPrimary, Type defSecondary) {
        double multiplier = 1.0;
        multiplier *= singleMatchup(attackType, defPrimary);

        if (defSecondary != null) {
            multiplier *= singleMatchup(attackType, defSecondary);
        }
        return multiplier;
    }

    private static double singleMatchup(SkillType atk, Type def) {
        if (def == null) return 1.0;

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
