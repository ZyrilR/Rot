package items;

import brainrots.BrainRot;
import battle.CaptureManager;

import java.util.List;

public class Capsule extends Item {

    public Capsule(String name, String description, String assetPath, int price) {
        super(name, description, assetPath, price);
    }

    /** Basic use method for compatibility */
    @Override
    public void use(BrainRot target, Object... extraArgs) {
        if (extraArgs.length == 0 || !(extraArgs[0] instanceof List<?> playerTeam)) {
            System.out.println("Capsule requires player team as argument!");
            return;
        }

        @SuppressWarnings("unchecked")
        List<BrainRot> team = (List<BrainRot>) playerTeam;
        CaptureManager.attempt(this, target, team); // perform capture
    }
}
