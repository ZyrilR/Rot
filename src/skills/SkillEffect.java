package skills;

import brainrots.BrainRot;
import progression.QuestSystem;
import utils.RandomUtil;

/**
 * Applies secondary effects (buffs, debuffs, status) from a Skill onto a target BrainRot.
 */
public class SkillEffect {

    /**
     * Applies the skill's effect to the target.
     * @param skill  The skill being used.
     * @param user   The BrainRot using the skill.
     * @param target The BrainRot receiving the effect.
     */
    public static void apply(Skill skill, BrainRot user, BrainRot target) {
        String effect = skill.getEffect().toUpperCase();

        switch (effect) {
            case "BURN":
                if (!target.hasStatus("BURN")) {
                    target.setStatus("BURN");
                    System.out.println(target.getName() + " was Burned!");
                }
                break;

            case "PARALYZE":
                if (!target.hasStatus("PARALYZE")) {
                    target.setStatus("PARALYZE");
                    target.modifySpeed(-0.25); // -25% speed
                    System.out.println(target.getName() + " was Paralyzed!");
                }
                break;

            case "CONFUSE":
                if (!target.hasStatus("CONFUSE")) {
                    target.setStatus("CONFUSE");
                    System.out.println(target.getName() + " became Confused!");
                    // Din Overload achievement
                    QuestSystem.getInstance().onConfusionInflicted();
                }
                break;

            case "SLEEP":
                if (!target.hasStatus("SLEEP")) {
                    target.setStatus("SLEEP");
                    System.out.println(target.getName() + " is Asleep!");
                }
                break;

            case "LOWER_DEF":
                target.modifyDefense(-0.20);
                System.out.println(target.getName() + "'s Defense fell!");
                break;

            case "LOWER_ATK":
                target.modifyAttack(-0.20);
                System.out.println(target.getName() + "'s Attack fell!");
                break;

            case "LOWER_SPD":
                target.modifySpeed(-0.20);
                System.out.println(target.getName() + "'s Speed fell!");
                break;

            case "RAISE_ATK":
                user.modifyAttack(0.40);
                System.out.println(user.getName() + "'s Attack rose!");
                break;

            case "RAISE_DEF":
                user.modifyDefense(0.40);
                System.out.println(user.getName() + "'s Defense rose!");
                break;

            case "RAISE_SPD":
                user.modifySpeed(0.40);
                System.out.println(user.getName() + "'s Speed rose!");
                break;

            case "HEAL":
                int healAmount = (int)(user.getMaxHp() * 0.30);
                user.heal(healAmount);
                System.out.println(user.getName() + " restored " + healAmount + " HP!");
                break;

            case "FLINCH":
                // 30% chance to flinch using RandomUtil
                if (RandomUtil.chance(30.0)) {
                    target.setStatus("FLINCH");
                    System.out.println(target.getName() + " flinched!");
                }
                break;

            case "NONE":
            default:
                break;
        }
    }

    /**
     * Processes end-of-turn status effects (burn damage, etc.)
     */
    public static void processTurnEffects(BrainRot rot) {
        if (rot.hasStatus("BURN")) {
            int burnDmg = (int)(rot.getMaxHp() * 0.05);
            rot.takeDamage(burnDmg);
            System.out.println(rot.getName() + " took " + burnDmg + " burn damage!");
        }
    }
}
