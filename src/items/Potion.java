package items;

public class Potion extends Item {
    private int healPercentage;

    public Potion(String name, String description, String assetPath, int healPercentage) {
        super(name, description, assetPath);
        this.healPercentage = healPercentage;
    }

    @Override
    public void use() {
        //TODO: Implement logic to heal a BrainRot in battle or overworld
        System.out.println("Using " + name + " to heal " + healPercentage + " HP.");
    }

    public int getHealPercentage() { return healPercentage; }
}