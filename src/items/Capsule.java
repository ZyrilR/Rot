package items;

import brainrots.BrainRot;
import battle.CaptureManager;

import java.util.List;

public class Capsule extends Item {

    public Capsule(String name, String description, String assetPath, int price) {
        super(name, description, assetPath, price);
    }

    @Override
    public void use(BrainRot target, Object... extraArgs) {
        if (extraArgs.length < 2
                || !(extraArgs[0] instanceof List<?>)
                || !(extraArgs[1] instanceof BrainRot playerRot)) {
            System.out.println("Capsule requires player team and active BrainRot as arguments!");
            return;
        }

        @SuppressWarnings("unchecked")
        List<BrainRot> team = (List<BrainRot>) extraArgs[0];
        CaptureManager.attempt(this, target, playerRot, team);
    }
}