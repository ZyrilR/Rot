package skills;

public class Skill {
    private String name;
    private SkillType type;
    private int power;       // Base damage power
    private int maxUP;       // Maximum Use Points for this move
    private int currentUP;   // Current Use Points remaining
    private String effect;   // Description of effect (Example: "BURN", "CONFUSE", "PARALYZE", "SLEEP", "NONE")
    private String description;

    public Skill(String name, SkillType type, int power, int maxUP, String effect, String description) {
        this.name   = name;
        this.type   = type;
        this.power  = power;
        this.maxUP  = maxUP;
        this.currentUP = maxUP;
        this.effect = effect;
        this.description = description;
    }

    /** Returns a fresh copy with full UP. Each BrainRot should own its own Skill instances. */
    public Skill copy() {
        return new Skill(name, type, power, maxUP, effect, description);
    }

    /** Decrements UP by 1. Returns false if no UP left. */
    public boolean useUP() {
        if (currentUP < 1) return false;
        currentUP--;
        return true;
    }

    /** Restores UP to max. */
    public void restoreUP() {
        currentUP = maxUP;
    }

    // Getters
    public String getName()        { return name; }
    public SkillType getType()     { return type; }
    public int getPower()          { return power; }
    public int getMaxUP()          { return maxUP; }
    public int getCurrentUP()      { return currentUP; }
    public void setCurrentUP(int up) { this.currentUP = Math.min(up, maxUP); }
    public String getEffect()      { return effect; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name + " [" + type + "] PWR:" + power + " UP:" + currentUP + "/" + maxUP;
    }
}
