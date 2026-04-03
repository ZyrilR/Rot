package items;

import brainrots.BrainRot;

public class Stew extends Item {
    private int healPercentage;

    public Stew(String name, String description, String assetPath, int healPercentage, int price) {
        super(name, description, assetPath, price);
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