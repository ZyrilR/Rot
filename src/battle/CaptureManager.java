package battle;

import brainrots.BrainRot;
import items.Capsule;
import utils.RandomUtil;

import java.util.List;

/**
 * Handles capture attempt logic when a player throws a Capsule.
 */
public class CaptureManager {
    /**
     * Attempts to capture a wild BrainRot.
     *
     * Base capture rate is higher when target HP is lower.
     * Capsule type modifies the rate:
     *   - MASTER CAPSULE: 100%
     *   - SPEED CAPSULE : +20% if player speed > target speed
     *   - HEAVY CAPSULE : +20% if player defense > target defense
     *   - Type Capsules : +20% if target type matches capsule type
     *   - Others        : base rate only
     */
    public static boolean attempt(Capsule capsule, BrainRot target, BrainRot playerRot, List<BrainRot> playerTeam) {

        String capsuleName = capsule.getName().toUpperCase();

        // Master capsule = guaranteed
        if (capsuleName.equals("MASTER CAPSULE")) {
            return capture(target, playerTeam);
        }

        // Base capture rate scales with missing HP (0–60%)
        double hpRatio  = (double) target.getCurrentHp() / target.getMaxHp();
        double baseRate = (1.0 - hpRatio) * 60.0;

        // Capsule bonus
        double bonus = 0;
        if (capsuleName.equals("SPEED CAPSULE") && playerRot.getSpeed() > target.getSpeed()) bonus = 20;
        if (capsuleName.equals("HEAVY CAPSULE") && playerRot.getDefense() > target.getDefense()) bonus = 20;
        if (hasTypeBonus(capsuleName, target)) bonus = 20;

        double totalRate = Math.min(95, baseRate + bonus);

        System.out.printf("Capture rate: %.1f%%%n", totalRate);

        if (RandomUtil.chance(totalRate)) {
            return capture(target, playerTeam);
        } else {
            System.out.println(target.getName() + " broke free!");
            return false;
        }
    }

    /** Returns true if the capsule's type prefix matches the target's primary or secondary type. */
    private static boolean hasTypeBonus(String capsuleName, BrainRot target) {
        if (!capsuleName.endsWith(" CAPSULE")) return false;

        String typePrefix = capsuleName.replace(" CAPSULE", "").trim();

        // Exclude non-type capsules
        switch (typePrefix) {
            case "NORMAL": case "RED":    case "BLUE":
            case "SPEED":  case "HEAVY":  case "MASTER":
                return false;
        }

        if (target.getPrimaryType().name().equalsIgnoreCase(typePrefix)) return true;
        return target.getSecondaryType() != null &&
                target.getSecondaryType().name().equalsIgnoreCase(typePrefix);
    }

    /** Adds captured BrainRot to player team if space allows. */
    private static boolean capture(BrainRot target, List<BrainRot> playerTeam) {
        if (playerTeam.size() >= 6) {
            System.out.println("Your team is full! " + target.getName() + " could not be added.");
            return true;
        }
        playerTeam.add(target);
        System.out.println(target.getName() + " was caught and added to your team!");
        return true;
    }
}