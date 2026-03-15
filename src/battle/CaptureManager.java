package battle;

import brainrots.BrainRot;
import items.Capsule;
import items.Inventory;
import java.util.List;
import java.util.Random;

/**
 * Handles capture attempt logic when a player throws a Capsule.
 */
public class CaptureManager {

    private static final Random rand = new Random();

    /**
     * Attempts to capture a wild BrainRot.
     *
     * Base capture rate is higher when target HP is lower.
     * Capsule type modifies the rate:
     *   - MASTER CAPSULE: 100%
     *   - SPEED CAPSULE : +20% if target speed > 30
     *   - HEAVY CAPSULE : +20% if target defense > 40
     *   - Others        : base rate only
     *
     * @param capsule     The capsule being used (by name stored in item)
     * @param target      The wild BrainRot being targeted
     * @param playerTeam  The player's current team list (max 6)
     * @param inventory   The player's inventory (capsule consumed on use)
     * @return true if capture succeeds.
     */
    public static boolean attempt(Capsule capsule, BrainRot target, List<BrainRot> playerTeam, Inventory inventory) {

        String capsuleName = capsule.getName().toUpperCase();

        // Master capsule = guaranteed
        if (capsuleName.equals("MASTER CAPSULE")) {
            return capture(target, playerTeam, inventory);
        }

        // Base rate: scales with missing HP (0–60%)
        double hpRatio     = (double) target.getCurrentHp() / target.getMaxHp();
        double baseRate    = (1.0 - hpRatio) * 60.0; // 0% at full HP → 60% at 0 HP

        // Capsule bonus
        double bonus = 0;
        if (capsuleName.equals("SPEED CAPSULE") && target.getBaseSpeed() > 30) bonus = 20;
        if (capsuleName.equals("HEAVY CAPSULE") && target.getDefense()   > 40) bonus = 20;

        double totalRate = Math.min(95, baseRate + bonus); // cap at 95%

        System.out.printf("Capture rate: %.1f%%%n", totalRate);

        if (rand.nextDouble() * 100 < totalRate) {
            return capture(target, playerTeam, inventory);
        } else {
            System.out.println(target.getName() + " broke free!");
            return false;
        }
    }

    /**
     *
     * @param target         Wild BrainRot
     * @param playerTeam     PlayerTeam is your BrainRot team
     * @param inventory      The player's inventory
     * @return true if brainrot is caught
     */
    private static boolean capture(BrainRot target, List<BrainRot> playerTeam, Inventory inventory) {
        if (playerTeam.size() > 6) {
            System.out.println("Your team is full! " + target.getName() + " could not be added.");
            return true; // still counts as caught, but not added
        }
        playerTeam.add(target);
        System.out.println(target.getName() + " was caught and added to your team!");
        return true;
    }
}