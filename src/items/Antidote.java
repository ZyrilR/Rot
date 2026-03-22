package items;

import brainrots.BrainRot;

public class Antidote extends Item {
    private String statusToCure; // Example: "Confusion", "Paralysis",

    public Antidote(String name, String description, String assetPath, String statusToCure, int price) {
        super(name, description, assetPath, price);
        this.statusToCure = statusToCure;
    }

    @Override
    public void use(BrainRot target, Object... extraArgs) {
        if (statusToCure.equalsIgnoreCase("DEBUFF")) {
            target.resetModifiers();
            System.out.println(target.getName() + "'s stat debuffs were removed!");
            return;
        }

        if (target.hasStatus(statusToCure)) {
            target.clearStatus();
            System.out.println(target.getName() + " was cured of " + statusToCure + "!");
        } else {
            System.out.println("It had no effect.");
        }
    }

    public String getStatusToCure() {
        return statusToCure;
    }
}