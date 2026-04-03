package skills;

public class Skill {
    private String name;
    private SkillType type;
    private int power;       // Base damage power
    private int spCost;      // Skill Point cost
    private String effect;   // Description of effect (Example: "BURN", "CONFUSE", "PARALYZE", "NONE")
    private String description;

    public Skill(String name, SkillType type, int power, int spCost, String effect, String description) {
        this.name   = name;
        this.type   = type;
        this.power  = power;
        this.spCost = spCost;
        this.effect = effect;
        this.description = description;
    }

    // Getters
    public String getName()        { return name; }
    public SkillType getType()     { return type; }
    public int getPower()          { return power; }
    public int getSpCost()         { return spCost; }
    public String getEffect()      { return effect; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name + " [" + type + "] PWR:" + power + " SP:" + spCost;
    }
}