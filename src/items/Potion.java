package items;

import brainrots.BrainRot;

public class Potion extends Item {
    private int healPercentage;

    public Potion(String name, String description, String assetPath, int healPercentage) {
        super(name, description, assetPath);
        this.healPercentage = healPercentage;
    }

    @Override
    public void use(BrainRot target, Object... extraArgs) {
        int healAmount = (target.getMaxHp() * healPercentage) / 100;
        target.heal(healAmount);

        System.out.println(target.getName() + " healed for " + healAmount + " HP.");
    }

    public int getHealPercentage() { return healPercentage; }
}