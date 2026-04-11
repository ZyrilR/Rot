package progression;

/**
 * Represents a single quest with completion tracking and a claimable reward.
 *
 * Counter-based quests use progress + goal.
 * Boolean quests use goal = 1.
 * Hidden quests display as "???" until completed.
 */
public class Quest {

    // ── Reward type ───────────────────────────────────────────────────────────
    public enum RewardType { COINS, ITEM, BRAINROT, NONE }

    // ── Core fields ───────────────────────────────────────────────────────────
    private final String     id;
    private final String     name;
    private final String     description;
    private final String     assetPath;
    private final int        difficulty;
    private final boolean    hidden;
    private final int        goal;

    // ── Reward fields ─────────────────────────────────────────────────────────
    private final RewardType rewardType;
    private final int        rewardCoins;
    private final String     rewardItemName;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean completed     = false;
    private boolean rewardClaimed = false;
    private int     progress      = 0;

    // ── Constructor ───────────────────────────────────────────────────────────

    public Quest(String id, String name, String description, String assetPath,
                 int difficulty, boolean hidden, int goal,
                 RewardType rewardType, int rewardCoins, String rewardItemName) {
        this.id             = id;
        this.name           = name;
        this.description    = description;
        this.assetPath      = assetPath;
        this.difficulty     = difficulty;
        this.hidden         = hidden;
        this.goal           = goal;
        this.rewardType     = rewardType;
        this.rewardCoins    = rewardCoins;
        this.rewardItemName = rewardItemName;
    }

    // ── Completion logic ──────────────────────────────────────────────────────

    public boolean complete() {
        if (completed) return false;
        completed = true;
        progress  = goal;
        return true;
    }

    public boolean increment() {
        if (completed) return false;
        progress++;
        if (progress >= goal) return complete();
        return false;
    }

    public boolean increment(int amount) {
        if (completed) return false;
        progress += amount;
        if (progress >= goal) return complete();
        return false;
    }

    // ── Reward ────────────────────────────────────────────────────────────────

    public void markRewardClaimed() { rewardClaimed = true; }

    public String getRewardText() {
        return switch (rewardType) {
            case COINS    -> rewardCoins + " COINS";
            case ITEM     -> "1x " + rewardItemName;
            case BRAINROT -> "1x DIAMOND BRAINROT";
            case NONE     -> "No reward";
        };
    }

    // ── Display helpers ───────────────────────────────────────────────────────

    public String getDisplayName() {
        return (hidden && !completed) ? "???" : name;
    }

    public String getDisplayDescription() {
        return (hidden && !completed) ? "This quest is a secret." : description;
    }

    public double getProgressFraction() {
        if (goal <= 0) return completed ? 1.0 : 0.0;
        return Math.min(1.0, (double) progress / goal);
    }

    public boolean isCounterBased() { return goal > 1; }

    public String getProgressText() {
        if (!isCounterBased()) return completed ? "Complete" : "Incomplete";
        return progress + " / " + goal;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String     getId()             { return id; }
    public String     getName()           { return name; }
    public String     getDescription()    { return description; }
    public String     getAssetPath()      { return assetPath; }
    public int        getDifficulty()     { return difficulty; }
    public boolean    isHidden()          { return hidden; }
    public boolean    isCompleted()       { return completed; }
    public boolean    isRewardClaimed()   { return rewardClaimed; }
    public int        getProgress()       { return progress; }
    public int        getGoal()           { return goal; }
    public RewardType getRewardType()     { return rewardType; }
    public int        getRewardCoins()    { return rewardCoins; }
    public String     getRewardItemName() { return rewardItemName; }

    // ── Save / Load ───────────────────────────────────────────────────────────

    public String toFileFormat() {
        return id + ";" + completed + ";" + progress + ";" + rewardClaimed;
    }

    public void loadState(boolean completed, int progress, boolean rewardClaimed) {
        this.completed     = completed;
        this.progress      = progress;
        this.rewardClaimed = rewardClaimed;
    }

    public void loadState(boolean completed, int progress) {
        loadState(completed, progress, false);
    }
}