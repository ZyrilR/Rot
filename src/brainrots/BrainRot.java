package brainrots;

import skills.Skill;
import skills.SkillRegistry;
import utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

import static brainrots.Type.getType;
import static brainrots.Tier.getTier;
import static utils.Constants.MAX_LEVEL;

/**
 * Represents a BrainRot creature with stats, type, moves, and status.
 *
 * Stat structure:
 *   currentHP  / MAX_HP    — hit points
 *   attack     / BASE_ATK  — current (modified) attack   / permanent base
 *   defense    / BASE_DEF  — current (modified) defense  / permanent base
 *   speed      / BASE_SPEED— current (modified) speed    / permanent base
 *   Each Skill tracks its own UP (Use Points) that decrements by 1 per use
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

    // Internal modifier multipliers — used only for cap arithmetic (±40%)
    private double attackMod  = 1.0;
    private double defenseMod = 1.0;
    private double speedMod   = 1.0;

    // Level and experience
    private int level    = 5;
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
                    int level,
                    int maxHp, int attack, int defense, int speed) {
        this.name          = name;
        this.primaryType   = primaryType;
        this.secondaryType = secondaryType;
        this.tier          = tier;
        this.level         = level;

        this.MAX_HP    = maxHp;
        this.currentHP = maxHp;

        this.BASE_ATK = attack;
        this.attack   = attack;

        this.BASE_DEF = defense;
        this.defense  = defense;

        this.BASE_SPEED = speed;
        this.speed      = speed;

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
        if (level >= MAX_LEVEL) return results;

        currentXp += amount;
        while (level < MAX_LEVEL && currentXp >= xpToNextLevel(level)) {
            currentXp -= xpToNextLevel(level);
            results.add(levelUp());
        }
        return results;
    }

    private int xpToNextLevel(int level) {
        if (level >= MAX_LEVEL) return Integer.MAX_VALUE;
        return level * level * 5;
    }

    private LevelUpResult levelUp() {
        level++;

        // Type-aware growth rates
        int hpGain  = StatGrowth.hpGrowth(primaryType);
        int atkGain = StatGrowth.atkGrowth();
        int defGain = StatGrowth.defGrowth(level);
        int spdGain = StatGrowth.spdGrowth(level);

        // Apply gains, clamping to hard caps
        int hpCap  = StatGrowth.hpCap(primaryType);
        int atkCap = StatGrowth.atkCap(primaryType, BASE_SPEED + spdGain);
        int defCap = StatGrowth.defCap();
        int spdCap = StatGrowth.spdCap();

        hpGain  = Math.min(hpGain,  hpCap  - MAX_HP);
        atkGain = Math.min(atkGain, atkCap  - BASE_ATK);
        defGain = Math.min(defGain, defCap  - BASE_DEF);
        spdGain = Math.min(spdGain, spdCap  - BASE_SPEED);

        // Ensure no negative gains (already at or past cap)
        hpGain  = Math.max(0, hpGain);
        atkGain = Math.max(0, atkGain);
        defGain = Math.max(0, defGain);
        spdGain = Math.max(0, spdGain);

        // Grow permanent bases
        MAX_HP     += hpGain;
        BASE_ATK   += atkGain;
        BASE_DEF   += defGain;
        BASE_SPEED += spdGain;

        // Re-check ATK cap in case speed crossed the 40 threshold
        if (BASE_SPEED > 40 && BASE_ATK > 115) {
            BASE_ATK = 115;
        }

        // Keep current HP healed by the gained amount
        currentHP += hpGain;

        // Recalculate live values from new bases (preserves any active modifiers)
        attack  = (int)(BASE_ATK   * attackMod);
        defense = (int)(BASE_DEF   * defenseMod);
        speed   = (int)(BASE_SPEED * speedMod);

        // Enforce buffed speed ceiling
        speed = Math.min(speed, StatGrowth.spdBuffCap());

        Skill unlocked = LevelUpLearnset.getSkillAt(name, level);

        progression.QuestSystem.getInstance().onLevelUp(level);
        if (level == 100 && tier == Tier.DIAMOND)
            progression.QuestSystem.getInstance().onDiamondLevelUp();

        return new LevelUpResult(level, hpGain, atkGain, defGain, spdGain, unlocked);
    }

    public int getLevel()         { return level; }
    public int getCurrentXp()     { return currentXp; }
    public int getXpToNextLevel() { return xpToNextLevel(level); }

    // ── UP (Use Points) — tracked per Skill ────────────────────────────────────

    /**
     * Deducts 1 UP from the move at the given index. Returns false if that move has 0 UP left.
     */
    public boolean useSkill(int moveIndex) {
        if (moveIndex < 0 || moveIndex >= moves.size()) return false;
        Skill skill = moves.get(moveIndex);
        if (!skill.useUP()) {
            System.out.println(name + " doesn't have enough UP for " + skill.getName() + "!");
            return false;
        }
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
        speed    = Math.min((int)(BASE_SPEED * speedMod), StatGrowth.spdBuffCap());
    }

    public void resetModifiers() {
        attackMod  = 1.0;
        defenseMod = 1.0;
        speedMod   = 1.0;
        attack     = BASE_ATK;
        defense    = BASE_DEF;
        speed      = Math.min(BASE_SPEED, StatGrowth.spdBuffCap());
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

    public String getStatus()      { return status; }
    public List<Skill> getMoves()  { return moves; }

    public void restoreForBattle() {
        currentHP = MAX_HP;
        for (Skill move : moves) move.restoreUP();
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
    public BrainRot(String name, int level, int currentXp, String primaryType, String secondaryType, String tier,
                    int maxHp, int currentHp,
                    int baseAtk, int baseDef, int baseSpeed,
                    double attackMod, double defenseMod, double speedMod,
                    String status, int statusTurns, int turnCount,
                    String[] moves, int[] moveUPs) {
        this.name          = name;
        this.level         = level;
        this.currentXp = currentXp;
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

        this.status      = status;
        this.statusTurns = statusTurns;
        this.turnCount   = turnCount;

        for (int i = 0; i < moves.length; i++) {
            Skill skill = SkillRegistry.get(moves[i]);
            if (skill != null && i < moveUPs.length) skill.setCurrentUP(moveUPs[i]);
            this.moves.add(skill);
        }
    }

    // ── File serialization ────────────────────────────────────────────────────

    public String toFileFormat() {
        String format =
                name            + ";" +
                        level           + ";" +
                        currentXp       + ";" +
                        primaryType     + ";" +
                        secondaryType   + ";" +
                        tier            + ";" +
                        MAX_HP          + ";" +
                        currentHP       + ";" +
                        BASE_ATK        + ";" +
                        BASE_DEF        + ";" +
                        BASE_SPEED      + ";" +
                        attackMod       + ";" +
                        defenseMod      + ";" +
                        speedMod        + ";" +
                        status          + ";" +
                        statusTurns     + ";" +
                        turnCount       + ":";

        // Moves (pipe-separated)
        for (int i = 0; i < moves.size(); i++) {
            format += moves.get(i).getName();
            if (i < moves.size() - 1) format += "|";
        }

        // Per-move UP values (pipe-separated, colon-separated section)
        format += ":";
        for (int j = 0; j < moves.size(); j++) {
            format += moves.get(j).getCurrentUP();
            if (j < moves.size() - 1) format += "|";
        }

        return format;
    }
}