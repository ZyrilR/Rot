package brainrots;

import skills.Skill;
import skills.SkillRegistry;

import java.util.ArrayList;
import java.util.List;

import static brainrots.Type.getType;
import static brainrots.Tier.getTier;
import static utils.Constants.MAX_SPEED;

/**
 * Represents a BrainRot creature with stats, type, moves, and status.
 */
public class BrainRot {

    //Sound Effects: (Taking Damage, Attack, Buff, Encounter)

    // Identity
    private String name;
    private Type primaryType;
    private Type secondaryType; // nullable
    private Tier tier;

    // Base stats (set at creation by Factory)
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int speed;
    private int currentSp;

    // Stat modifiers (as multipliers applied on base, capped at ±40%)
    private double attackMod  = 1.0;
    private double defenseMod = 1.0;
    private double speedMod   = 1.0;

    // Level and experience
    private int level = 1;
    private int currentXp = 0;

    // Battle state
    private String status = "NONE"; // NONE, BURN, PARALYZE, CONFUSE, FLINCH
    private int statusTurns = 0;
    private int turnCount = 0;

    // Moves (max 4)
    private static final int MAX_MOVES = 4;
    private List<Skill> moves = new ArrayList<>();

    public BrainRot(String name, Type primaryType, Type secondaryType, Tier tier,
                    int maxHp, int attack, int defense, int speed) {
        this.name = name;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
        this.tier = tier;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.currentSp = MAX_SPEED;
    }

    // ── HP / Damage / Healing ────────────────────────────────────────────────

    public void takeDamage(int dmg) {
        currentHp = Math.max(0, currentHp - dmg);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public boolean isFainted() {
        return currentHp <= 0;
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

        // Fixed stat growth per level
        int hpGain  = 3;
        int atkGain = 1;
        int defGain = 1;
        int spdGain = 1;

        maxHp  += hpGain;
        currentHp += hpGain; // heal by the amount gained so HP doesn't drop on level-up
        attack += atkGain;
        defense += defGain;
        speed  += spdGain;

        Skill unlocked = LevelUpLearnset.getSkillAt(name, level);
        return new LevelUpResult(level, hpGain, atkGain, defGain, spdGain, unlocked);
    }

    public int getLevel()      { return level; }
    public int getCurrentXp()  { return currentXp; }
    public int getXpToNextLevel() { return ExperienceSystem.xpToNextLevel(level); }

    // ── SP ───────────────────────────────────────────────────────────────────

    public boolean useSkill(Skill skill) {
        if (currentSp < skill.getSpCost()) {
            System.out.println(name + " doesn't have enough SP!");
            return false;
        }
        currentSp -= skill.getSpCost();
        return true;
    }

    // ── Status ───────────────────────────────────────────────────────────────

    public void setStatus(String status) {
        this.status = status;
        this.statusTurns = switch (status.toUpperCase()) {
            case "BURN"     -> 3;
            case "PARALYZE" -> 2;
            case "CONFUSE"  -> 2;
            default         -> 0;
        };
    }

    public boolean hasStatus(String status) {
        return this.status.equalsIgnoreCase(status);
    }

    public void clearStatus() {
        this.status = "NONE";
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
    }

    public void modifyDefense(double delta) {
        defenseMod = Math.min(1.4, Math.max(0.6, defenseMod + delta));
    }

    public void modifySpeed(double delta) {
        speedMod = Math.min(1.4, Math.max(0.6, speedMod + delta));
    }

    public void resetModifiers() {
        attackMod = 1.0;
        defenseMod = 1.0;
        speedMod = 1.0;
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

    public String getName()         { return name; }
    public Type getPrimaryType()    { return primaryType; }
    public Type getSecondaryType()  { return secondaryType; }
    public Tier getTier()           { return tier; }
    public int getMaxHp()           { return maxHp; }
    public int getCurrentHp()       { return currentHp; }
    public int getAttack()          { return (int)(attack * attackMod); }
    public int getDefense()         { return (int)(defense * defenseMod); }
    public int getSpeed()           { return (int)(speed * speedMod); }
    public int getBaseSpeed()       { return speed; }
    public int getCurrentSp()       { return currentSp; }
    public String getStatus()       { return status; }
    public List<Skill> getMoves()   { return moves; }

    public void restoreForBattle() {
        currentHp = maxHp;
        resetModifiers();
    }

    @Override
    public String toString() {
        return name + " [" + primaryType + (secondaryType != null ? "/" + secondaryType : "") + "] "
                + tier + " HP:" + currentHp + "/" + maxHp;
    }

    public BrainRot(String name, String primaryType, String secondaryType, String tier, int maxHp, int currentHp,
    int attack, int defense, int speed, int currentSp, double attackMod, double defenseMod, double speedMod, String status,
    int statusTurns, int turnCount, String[] moves) {
        this.name = name;
        this.primaryType = getType(primaryType);
        this.secondaryType = getType(secondaryType);
        this.tier = Tier.getTier(tier);
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.currentSp = currentSp;
        this.attackMod = attackMod;
        this.defenseMod = defenseMod;
        this.speedMod = speedMod;
        this.status = status;
        this.statusTurns = statusTurns;
        this.turnCount = turnCount;
        for (String move : moves) {
            addMove(SkillRegistry.get(move));
        }
    }

    public String toFileFormat() {
        String format = name + ";" +
                primaryType + ";" +
                secondaryType + ";" +
                tier + ";" +
                maxHp + ";" +
                currentHp + ";" +
                attack + ";" +
                defense + ";" +
                speed + ";" +
                currentSp + ";" +
                attackMod + ";" +
                defenseMod + ";" +
                speedMod + ";" +
                status + ";" +
                statusTurns + ";" +
                turnCount + ":";
        int i = 0;
        for (Skill move : moves) {
            format += move.getName();
            if (i < moves.size() - 1)
                format += "|";
            i++;
        }

        return format;
    }
}