package battle;

import brainrots.BrainRot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Determines the order in which BrainRots act each turn.
 * Order is based on Speed stat; ties are broken randomly (50/50).
 */
public class TurnManager {

    private static final Random rand = new Random();

    /**
     * Returns the list of combatants sorted by descending Speed.
     * Tied speeds are resolved randomly.
     */
    public static List<BrainRot> getOrder(List<BrainRot> combatants) {
        List<BrainRot> order = new ArrayList<>(combatants);

        order.sort((a, b) -> {
            int speedDiff = b.getSpeed() - a.getSpeed();
            if (speedDiff != 0) return speedDiff;
            return rand.nextBoolean() ? 1 : -1; // 50/50 on tie
        });

        return order;
    }

    /**
     * Simple two-combatant version: returns [first, second].
     */
    public static BrainRot[] getOrder(BrainRot rot1, BrainRot rot2) {
        if (rot1.getSpeed() > rot2.getSpeed()) return new BrainRot[]{rot1, rot2};
        if (rot2.getSpeed() > rot1.getSpeed()) return new BrainRot[]{rot2, rot1};

        return rand.nextBoolean() ? new BrainRot[]{rot1, rot2} : new BrainRot[]{rot2, rot1};
    }
}