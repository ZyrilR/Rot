package battle;

import brainrots.BrainRot;
import skills.SkillEffect;
import utils.RandomUtil;

/**
 * Processes active status effects on a BrainRot each turn.
 * Also checks whether a BrainRot can act based on its status.
 */
public class StatusEffectManager {

    /**
     * Called at the START of a BrainRot's turn.
     * Returns false if the BrainRot cannot act (paralyzed, flinched, etc.).
     */
    public static boolean canAct(BrainRot rot) {
        switch (rot.getStatus().toUpperCase()) {
            case "PARALYZE":
                // 25% chance to fail move using RandomUtil
                if (RandomUtil.chance(25.0)) {
                    System.out.println(rot.getName() + " is paralyzed and can't move!");
                    return false;
                }
                break;

            case "CONFUSE":
                // 40% chance to hurt itself using RandomUtil
                if (RandomUtil.chance(40.0)) {
                    int selfDmg = (int)(rot.getMaxHp() * 0.10);
                    rot.takeDamage(selfDmg);
                    System.out.println(rot.getName() + " hurt itself in confusion! (-" + selfDmg + " HP)");
                    return false;
                }
                break;

            case "FLINCH":
                System.out.println(rot.getName() + " flinched and couldn't move!");
                rot.clearStatus(); // flinch only lasts one turn
                return false;

            case "SLEEP":
                System.out.println(rot.getName() + " is fast asleep!");
                return false;
        }
        return true;
    }

    /**
     * Called at the END of a BrainRot's turn.
     * Applies burn damage and decrements status counters.
     */
    public static void processTurnEnd(BrainRot rot) {
        SkillEffect.processTurnEffects(rot); // handles burn damage
        rot.decrementStatusTurns();          // ticks down status duration
    }
}
