package skills;

public class Skill {
    private String name;
    private SkillType type;
    private int power;       // Base damage power
    private int spCost;      // Skill Point cost
    private String effect;   // Description of effect (Example: "BURN", "CONFUSE", "PARALYZE", "NONE")

    public Skill(String name, SkillType type, int power, int spCost, String effect) {
        this.name   = name;
        this.type   = type;
        this.power  = power;
        this.spCost = spCost;
        this.effect = effect;
    }

    // Getters
    public String getName()       { return name; }
    public SkillType getType()    { return type; }
    public int getPower()         { return power; }
    public int getSpCost()        { return spCost; }
    public String getEffect()     { return effect; }

    @Override
    public String toString() {
        return name + " [" + type + "] PWR:" + power + " SP:" + spCost;
    }
}