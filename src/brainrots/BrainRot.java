package brainrots;

import skills.Skill;
import skills.SkillRegistry;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

import static brainrots.Type.getType;
import static brainrots.Tier.getTier;

/**
 * Represents a BrainRot creature with stats, type, moves, and status.
 *
 * Stat structure:
 *   currentHP  / MAX_HP    — hit points
 *   attack     / BASE_ATK  — current (modified) attack   / permanent base
 *   defense    / BASE_DEF  — current (modified) defense  / permanent base
 *   speed      / BASE_SPEED— current (modified) speed    / permanent base
 *   currentUP  / BASE_UP   — usable skill points (current / max)
 *
 * attack, defense, and speed reflect live battle values and are updated
 * whenever modifyAttack / modifyDefense / modifySpeed is called.
 * BASE_* values are never changed outside of level-ups.
 */
public class BrainRot {

    //Sound Effects: (Taking Damage, Attack, Buff, Encounter)

    // Identity
    private String name;
    private Type primaryType;
    private Type secondaryType; // nullable
    private Tier tier;

    // HP
    private int MAX_HP;
    private int currentHP;

    // Attack — BASE_ATK is permanent; attack = BASE_ATK * attackMod (live value)
    private int BASE_ATK;
    private int attack;

    // Defense — BASE_DEF is permanent; defense = BASE_DEF * defenseMod (live value)
    private int BASE_DEF;
    private int defense;

    // Speed — BASE_SPEED is permanent; speed = BASE_SPEED * speedMod (live value)
    private int BASE_SPEED;
    private int speed;

    // UP (Use Points)
    private int currentUP;
    private int BASE_UP;

    // Internal modifier multipliers — used only for cap arithmetic (±40%)
    private double attackMod  = 1.0;
    private double defenseMod = 1.0;
    private double speedMod   = 1.0;

    // Level and experience
    private int level    = 1;
    private int currentXp = 0;

    // Battle state
    private String status     = "NONE"; // NONE, BURN, PARALYZE, CONFUSE, FLINCH, SLEEP
    private int    statusTurns = 0;
    private int    turnCount   = 0;

    // Moves (max 4)
    private static final int MAX_MOVES = 4;
    private List<Skill> moves = new ArrayList<>();

    // ── Primary constructor ───────────────────────────────────────────────────

    public BrainRot(String name, Type primaryType, Type secondaryType, Tier tier,
                    int maxHp, int attack, int defense, int speed) {
        this.name          = name;
        this.primaryType   = primaryType;
        this.secondaryType = secondaryType;
        this.tier          = tier;

        this.MAX_HP    = maxHp;
        this.currentHP = maxHp;

        this.BASE_ATK = attack;
        this.attack   = attack;

        this.BASE_DEF = defense;
        this.defense  = defense;

        this.BASE_SPEED = speed;
        this.speed      = speed;

        this.BASE_UP    = 100;
        this.currentUP  = BASE_UP;
    }

    // ── HP / Damage / Healing ────────────────────────────────────────────────

    public void takeDamage(int dmg) {
        currentHP = Math.max(0, currentHP - dmg);
    }

    public void heal(int amount) {
        currentHP = Math.min(MAX_HP, currentHP + amount);
    }

    public boolean isFainted() {
        return currentHP <= 0;
    }

    // ── Level and XP ─────────────────────────────────────────────────────────

    /**
     * Awards XP and processes any resulting level-ups.
     * Returns one LevelUpResult per level gained (empty list = no level-up).
     * Each result includes stat gains and the skill unlocked at that level (may be null).
     */
    public List<LevelUpResult> gainXp(int amount) {
        List<LevelUpResult> results = new ArrayList<>();
        if (level >= ExperienceSystem.MAX_LEVEL) return results;

        currentXp += amount;
        while (level < ExperienceSystem.MAX_LEVEL && currentXp >= ExperienceSystem.xpToNextLevel(level)) {
            currentXp -= ExperienceSystem.xpToNextLevel(level);
            results.add(levelUp());
        }
        return results;
    }

    private LevelUpResult levelUp() {
        level++;

        int hpGain  = 3;
        int atkGain = 1;
        int defGain = 1;
        int spdGain = 1;

        // Grow permanent bases
        MAX_HP     += hpGain;
        BASE_ATK   += atkGain;
        BASE_DEF   += defGain;
        BASE_SPEED += spdGain;

        // Keep current HP healed by the gained amount
        currentHP += hpGain;

        // Recalculate live values from new bases (preserves any active modifiers)
        attack  = (int)(BASE_ATK   * attackMod);
        defense = (int)(BASE_DEF   * defenseMod);
        speed   = (int)(BASE_SPEED * speedMod);

        Skill unlocked = LevelUpLearnset.getSkillAt(name, level);
        return new LevelUpResult(level, hpGain, atkGain, defGain, spdGain, unlocked);
    }

    public int getLevel()         { return level; }
    public int getCurrentXp()     { return currentXp; }
    public int getXpToNextLevel() { return ExperienceSystem.xpToNextLevel(level); }

    // ── UP (Use Points) ───────────────────────────────────────────────────────

    /**
     * Deducts the skill's UP cost. Returns false if there are not enough UP.
     */
    public boolean useSkill(Skill skill) {
        if (currentUP < skill.getSpCost()) {
            System.out.println(name + " doesn't have enough UP!");
            return false;
        }
        currentUP -= skill.getSpCost();
        return true;
    }

    // ── Status ───────────────────────────────────────────────────────────────

    public void setStatus(String status) {
        this.status = status;
        this.statusTurns = switch (status.toUpperCase()) {
            case "BURN"     -> 3;
            case "PARALYZE" -> 2;
            case "CONFUSE"  -> 2;
            case "SLEEP"    -> RandomUtil.range(1, 3);
            default         -> 0;
        };
    }

    public boolean hasStatus(String status) {
        return this.status.equalsIgnoreCase(status);
    }

    public void clearStatus() {
        this.status      = "NONE";
        this.statusTurns = 0;
    }

    public void decrementStatusTurns() {
        if (statusTurns > 0) {
            statusTurns--;
            if (statusTurns == 0) {
                System.out.println(name + "'s " + status + " wore off!");
                clearStatus();
            }
        }
    }

    /**
     * Returns true if any stat modifier is currently below neutral (1.0),
     * meaning the BrainRot has at least one active negative debuff.
     * Used by InventoryUI to prevent wasting a Debuff Tonic on a healthy BrainRot.
     */
    public boolean hasActiveDebuffs() {
        return attackMod < 1.0 || defenseMod < 1.0 || speedMod < 1.0;
    }

    // ── Stat Modifiers (capped at ±40%) ──────────────────────────────────────

    public void modifyAttack(double delta) {
        attackMod = Math.min(1.4, Math.max(0.6, attackMod + delta));
        attack    = (int)(BASE_ATK * attackMod);
    }

    public void modifyDefense(double delta) {
        defenseMod = Math.min(1.4, Math.max(0.6, defenseMod + delta));
        defense    = (int)(BASE_DEF * defenseMod);
    }

    public void modifySpeed(double delta) {
        speedMod = Math.min(1.4, Math.max(0.6, speedMod + delta));
        speed    = (int)(BASE_SPEED * speedMod);
    }

    public void resetModifiers() {
        attackMod  = 1.0;
        defenseMod = 1.0;
        speedMod   = 1.0;
        attack     = BASE_ATK;
        defense    = BASE_DEF;
        speed      = BASE_SPEED;
    }

    // ── Moves ─────────────────────────────────────────────────────────────────

    public boolean addMove(Skill skill) {
        if (moves.size() >= MAX_MOVES) {
            System.out.println(name + " already knows " + MAX_MOVES + " moves!");
            return false;
        }
        moves.add(skill);
        return true;
    }

    public boolean replaceMove(int index, Skill newSkill) {
        if (index < 0 || index >= moves.size()) return false;
        moves.set(index, newSkill);
        return true;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getName()        { return name; }
    public Type getPrimaryType()   { return primaryType; }
    public Type getSecondaryType() { return secondaryType; }
    public Tier getTier()          { return tier; }

    // HP
    public int getMaxHp()          { return MAX_HP; }
    public int getCurrentHp()      { return currentHP; }

    // Attack
    public int getAttack()         { return attack; }       // current (modified)
    public int getBaseAtk()        { return BASE_ATK; }     // permanent base

    // Defense
    public int getDefense()        { return defense; }      // current (modified)
    public int getBaseDef()        { return BASE_DEF; }     // permanent base

    // Speed
    public int getSpeed()          { return speed; }        // current (modified)
    public int getBaseSpeed()      { return BASE_SPEED; }   // permanent base

    // UP
    public int getCurrentUP()      { return currentUP; }
    public int getBaseUP()         { return BASE_UP; }

    public String getStatus()      { return status; }
    public List<Skill> getMoves()  { return moves; }

    public void restoreForBattle() {
        currentHP = MAX_HP;
        currentUP = BASE_UP;
        resetModifiers();
    }

    @Override
    public String toString() {
        return name + " [" + primaryType + (secondaryType != null ? "/" + secondaryType : "") + "] "
                + tier + " HP:" + currentHP + "/" + MAX_HP;
    }

    // ── Save / Load constructor ───────────────────────────────────────────────

    /**
     * Deserialization constructor — mirrors the field order in toFileFormat().
     */
    public BrainRot(String name, String primaryType, String secondaryType, String tier,
                    int maxHp, int currentHp,
                    int baseAtk, int baseDef, int baseSpeed, int currentUP,
                    double attackMod, double defenseMod, double speedMod,
                    String status, int statusTurns, int turnCount,
                    String[] moves) {
        this.name          = name;
        this.primaryType   = getType(primaryType);
        this.secondaryType = getType(secondaryType);
        this.tier          = Tier.getTier(tier);

        this.MAX_HP    = maxHp;
        this.currentHP = currentHp;

        this.BASE_ATK = baseAtk;
        this.BASE_DEF = baseDef;
        this.BASE_SPEED = baseSpeed;

        this.attackMod  = attackMod;
        this.defenseMod = defenseMod;
        this.speedMod   = speedMod;

        // Restore live values from saved bases and modifiers
        this.attack  = (int)(BASE_ATK   * attackMod);
        this.defense = (int)(BASE_DEF   * defenseMod);
        this.speed   = (int)(BASE_SPEED * speedMod);

        this.BASE_UP   = 100;
        this.currentUP = currentUP;

        this.status      = status;
        this.statusTurns = statusTurns;
        this.turnCount   = turnCount;

        for (String move : moves) {
            addMove(SkillRegistry.get(move));
        }
    }

    // ── File serialization ────────────────────────────────────────────────────

    public String toFileFormat() {
        String format =
                name            + ";" +
                primaryType     + ";" +
                secondaryType   + ";" +
                tier            + ";" +
                MAX_HP          + ";" +
                currentHP       + ";" +
                BASE_ATK        + ";" +
                BASE_DEF        + ";" +
                BASE_SPEED      + ";" +
                currentUP       + ";" +
                attackMod       + ";" +
                defenseMod      + ";" +
                speedMod        + ";" +
                status          + ";" +
                statusTurns     + ";" +
                turnCount       + ";";

        int i = 0;
        for(Skill move : moves) {
            format += move.getName();
            if(i < moves.size())
                format += "|";
            i++;
        }

        return format;
    }
}