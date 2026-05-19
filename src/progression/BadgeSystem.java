package progression;

import java.util.*;

/**
 * Tracks gym leader / gym master defeat state and badge ownership.
 *
 * Six badges in order:
 *   WATER    — Drip Badge      — Graine
 *   FLYING   — Airstrike Badge — Vincent
 *   FIGHTING — Beatdown Badge  — Ryle
 *   ROCK     — Archive Badge   — Derrick
 *   SAND     — Time Warp Badge — Angelo
 *   GYM_MASTER — Rot Master Badge — Sir Khai
 */
public class BadgeSystem {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static BadgeSystem instance;

    public static BadgeSystem getInstance() {
        if (instance == null) instance = new BadgeSystem();
        return instance;
    }

    public static void reset() { instance = new BadgeSystem(); }

    // ── Badge definition ──────────────────────────────────────────────────────

    public static class Badge {
        public final String id;
        public final String badgeName;
        public final String leaderName;
        public final String assetPath;
        private boolean defeated = false;

        public Badge(String id, String badgeName, String leaderName) {
            this(id, badgeName, leaderName, id);
        }

        public Badge(String id, String badgeName, String leaderName, String assetName) {
            this.id         = id;
            this.badgeName  = badgeName;
            this.leaderName = leaderName;
            this.assetPath  = "/res/Achievements/GYM_BADGES/" + assetName + ".png";
        }

        public boolean isDefeated()          { return defeated; }
        public void    setDefeated(boolean b) { defeated = b; }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final List<Badge>        ordered  = new ArrayList<>();
    private final Map<String, Badge> registry = new LinkedHashMap<>();
    private final Queue<Badge>       toastQueue = new LinkedList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    private BadgeSystem() {
        register(new Badge("WATER",      "DRIP BADGE",       "Graine"));
        register(new Badge("FLYING",     "AIRSTRIKE BADGE",  "Vincent"));
        register(new Badge("FIGHTING",   "BEATDOWN BADGE",   "Ryle"));
        register(new Badge("ROCK",       "STONE BADGE",      "Derrick", "STONE"));
        register(new Badge("SAND",       "TIME WARP BADGE",  "Angelo"));
        register(new Badge("GYM_MASTER", "ROT MASTER BADGE", "Sir Khai"));
    }

    private void register(Badge b) {
        registry.put(b.id, b);
        ordered.add(b);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Call when the player defeats a gym leader / master.
     * @param id  Badge ID, e.g. "WATER", "GYM_MASTER"
     */
    public void onLeaderDefeated(String id) {
        Badge b = registry.get(id.toUpperCase());
        if (b == null) { System.err.println("[BadgeSystem] Unknown badge id: " + id); return; }
        if (b.isDefeated()) return;
        b.setDefeated(true);
        toastQueue.add(b);
        System.out.println("[BadgeSystem] Badge earned: " + b.badgeName
                + " (defeated " + b.leaderName + ")");
    }

    public boolean isDefeated(String id) {
        Badge b = registry.get(id.toUpperCase());
        return b != null && b.isDefeated();
    }

    public List<Badge>  getAll()     { return Collections.unmodifiableList(ordered); }
    public Badge        pollToast()  { return toastQueue.poll(); }
    public boolean      hasToast()   { return !toastQueue.isEmpty(); }

    /** True iff all five gym leader badges (excluding GYM_MASTER) are obtained. */
    public boolean hasAllLeaderBadges() {
        for (Badge b : ordered) {
            if (b.id.equalsIgnoreCase("GYM_MASTER")) continue;
            if (!b.isDefeated()) return false;
        }
        return true;
    }

    public int badgeCount() {
        int c = 0;
        for (Badge b : ordered) if (b.isDefeated()) c++;
        return c;
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    /** Returns a single line: "WATER:true,FLYING:false,..." */
    public String toFileFormat() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            Badge b = ordered.get(i);
            sb.append(b.id).append(":").append(b.defeated);
            if (i < ordered.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    public void loadFromLine(String line) {
        if (line == null || line.isBlank()) return;
        for (String entry : line.split(",")) {
            String[] kv = entry.split(":");
            if (kv.length < 2) continue;
            Badge b = registry.get(kv[0].trim().toUpperCase());
            if (b != null) b.setDefeated(Boolean.parseBoolean(kv[1].trim()));
        }
    }
}