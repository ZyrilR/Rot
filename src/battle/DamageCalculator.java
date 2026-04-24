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

    public static int calculate(Skill skill, BrainRot attacker, BrainRot defender, GamePanel gp) {
        if (skill.getPower() <= 0) return 0;

        double raw = ((double) skill.getPower() * attacker.getAttack()) / (defender.getDefense() + 15);
        double baseDamage = (raw * 0.85) + 5;

        double typeMultiplier = getTypeMultiplier(skill.getType(), defender.getPrimaryType(), defender.getSecondaryType());
        double randomFactor = 0.90 + (Math.random() * 0.10);

        int finalDamage = (int)(baseDamage * typeMultiplier * randomFactor);
        finalDamage = Math.max(1, finalDamage);

        // ── TUTORIAL PLOT ARMOR (THE "ALWAYS WIN" RULE) ──
        // Check if player is on the beginner map
        if (gp != null && gp.CURRENT_PATH.contains("Route131")) {
            int totalRots = gp.player.getPCSYSTEM().getPartySize() + gp.player.getPCSYSTEM().getPCCount();

            // If the player only owns their Starter BrainRot...
            if (totalRots == 1) {
                // Figure out who is attacking
                boolean isPlayerAttacking = gp.player.getPCSYSTEM().getParty().contains(attacker);

                if (isPlayerAttacking) {
                    // 1. Player hits 50% harder!
                    finalDamage = (int)(finalDamage * 1.5);
                } else {
                    // 2. Enemy hits 50% weaker!
                    finalDamage = (int)(finalDamage * 0.5);

                    // 3. Focus Sash Effect: Enemy CANNOT deal the killing blow!
                    if (finalDamage >= defender.getCurrentHp()) {
                        finalDamage = Math.max(0, defender.getCurrentHp() - 1);
                        System.out.println("Tutorial Armor saved you from fainting!");
                    }
                }
            }
        }

        return finalDamage;
    }

    private static double getTypeMultiplier(SkillType attackType, Type defPrimary, Type defSecondary) {
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