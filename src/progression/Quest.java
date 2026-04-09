package progression;

/**
 * Represents a single quest.
 *
 * Counter-based quests use progress + goal.
 * Boolean quests use goal = 1, progress flips to 1 on unlock.
 * Hidden quests display as "???" until completed.
 */
public class Quest {

    private final String  id;
    private final String  name;
    private final String  description;
    private final String  assetPath;
    private final int     difficulty;
    private final boolean hidden;
    private final int     goal;

    private boolean completed = false;
    private int     progress  = 0;

    public Quest(String id, String name, String description,
                 String assetPath, int difficulty, boolean hidden, int goal) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.assetPath   = assetPath;
        this.difficulty  = difficulty;
        this.hidden      = hidden;
        this.goal        = goal;
    }

    // ── Completion logic ──────────────────────────────────────────────────────

    /**
     * Marks the quest as completed.
     * Returns true if this call actually completed it (wasn't already done).
     */
    public boolean complete() {
        if (completed) return false;
        completed = true;
        progress  = goal;
        return true;
    }

    /**
     * Increments progress by 1.
     * Auto-completes when progress reaches goal.
     * Returns true if this increment caused completion.
     */
    public boolean increment() {
        if (completed) return false;
        progress++;
        if (progress >= goal) return complete();
        return false;
    }

    /**
     * Increments progress by a specific amount.
     * Returns true if this caused completion.
     */
    public boolean increment(int amount) {
        if (completed) return false;
        progress += amount;
        if (progress >= goal) return complete();
        return false;
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

    public String  getId()          { return id; }
    public String  getName()        { return name; }
    public String  getDescription() { return description; }
    public String  getAssetPath()   { return assetPath; }
    public int     getDifficulty()  { return difficulty; }
    public boolean isHidden()       { return hidden; }
    public boolean isCompleted()    { return completed; }
    public int     getProgress()    { return progress; }
    public int     getGoal()        { return goal; }

    // ── Save / Load ───────────────────────────────────────────────────────────

    /** Serializes to: id;completed;progress */
    public String toFileFormat() {
        return id + ";" + completed + ";" + progress;
    }

    /** Restores completed + progress state from saved values. */
    public void loadState(boolean completed, int progress) {
        this.completed = completed;
        this.progress  = progress;
    }
}