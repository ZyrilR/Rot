package brainrots;

import skills.Skill;
import skills.SkillRegistry;

import java.util.ArrayList;
import java.util.List;

import static brainrots.Type.getType;
import static brainrots.Tier.getTier;

public class BrainRot {

    private String name;
    private Type primaryType;
    private Type secondaryType;
    private Tier tier;

    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int speed;
    private int maxSp;
    private int currentSp;

    // --- INTEGRATED LEVELING SYSTEM variables ---
    private int level = 1;
    private int currentXp = 0;

    private double attackMod  = 1.0;
    private double defenseMod = 1.0;
    private double speedMod   = 1.0;

    private String status = "NONE";
    private int statusTurns = 0;
    private boolean ultimateUsed = false;
    private int turnCount = 0;

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
        this.maxSp = 50; // Default SP for all
        this.currentSp = maxSp;
    }

    public void takeDamage(int dmg) {
        currentHp = Math.max(0, currentHp - dmg);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

    public boolean useSkill(Skill skill) {
        if (currentSp < skill.getSpCost()) {
            System.out.println(name + " doesn't have enough SP!");
            return false;
        }
        currentSp -= skill.getSpCost();
        return true;
    }

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

    public boolean hasActiveDebuffs() {
        return attackMod < 1.0 || defenseMod < 1.0 || speedMod < 1.0;
    }

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

    // --- INTEGRATED LEVELING AND XP LOGIC ---
    public List<LevelUpResult> gainXp(int xpGained) {
        List<LevelUpResult> results = new ArrayList<>();
        currentXp += xpGained;

        while (level < ExperienceSystem.MAX_LEVEL && currentXp >= getXpToNextLevel()) {
            currentXp -= getXpToNextLevel();
            level++;

            int hpGain = 2; int atkGain = 1; int defGain = 1; int spdGain = 1;
            maxHp += hpGain;
            currentHp += hpGain;
            attack += atkGain;
            defense += defGain;
            speed += spdGain;

            Skill newSkill = LevelUpLearnset.getSkillAt(name, level);
            results.add(new LevelUpResult(level, hpGain, atkGain, defGain, spdGain, newSkill));

            progression.QuestSystem.getInstance().onLevelUp(level);
        }
        return results;
    }

    public int getXpToNextLevel() {
        return ExperienceSystem.xpToNextLevel(level);
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

    // --- INTEGRATED REQUIRED UI GETTERS ---
    public int getMaxSp()           { return maxSp; }
    public int getCurrentSp()       { return currentSp; }
    public int getLevel()           { return level; }
    public int getCurrentXp()       { return currentXp; }
    public int getBaseAtk()         { return attack; }
    public int getBaseDef()         { return defense; }

    public String getStatus()       { return status; }
    public List<Skill> getMoves()   { return moves; }

    public void restoreForBattle() {
        currentHp = maxHp;
        currentSp = maxSp;
        clearStatus();
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