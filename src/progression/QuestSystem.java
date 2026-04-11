package progression;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.BrainRotRegistry;
import brainrots.Tier;
import engine.GamePanel;
import items.ItemRegistry;
import progression.Quest.RewardType;
import skills.Skill;
import utils.RandomUtil;

import java.util.*;

/**
 * Central manager for all 30 quests.
 *
 * Hook points:
 *   BattleManager  → onBattleWon(), onBattleLost(), onDamageDealt(), onBrainRotWin()
 *   CaptureManager → onCapture()
 *   PCSystem       → onPartySizeChanged(), onBoxSlotSet()
 *   ShopUI         → onCoinsSpent()
 *   InventoryUI    → onItemUsed(), onBattleItemThreshold()
 *   BrainRot       → onLevelUp()
 */
public class QuestSystem {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static QuestSystem instance;

    public static QuestSystem getInstance() {
        if (instance == null) instance = new QuestSystem();
        return instance;
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    private final List<Quest>        ordered    = new ArrayList<>();
    private final Map<String, Quest> registry   = new LinkedHashMap<>();
    private final Queue<Quest>       toastQueue = new LinkedList<>();

    // ── Persistent counters ───────────────────────────────────────────────────

    private int               totalCoinsSpent   = 0;
    private int               totalItemsUsed    = 0;
    private int               captureCount      = 0;
    private int               consecutiveLosses = 0;
    private final Set<String> capturedNames     = new HashSet<>();
    private final Set<String> categoriesUsed    = new HashSet<>();

    // Per-BrainRot win counters — key = BrainRot name uppercase
    private final Map<String, Integer> rotWins = new HashMap<>();

    // ── BrainRot name → quest ID map ─────────────────────────────────────────
    private static final Map<String, String> ROT_QUEST_ID = new LinkedHashMap<>();
    static {
        ROT_QUEST_ID.put("TUNG TUNG TUNG SAHUR",  "THE_ETERNAL_DRUM");
        ROT_QUEST_ID.put("TRALALERO TRALALA",      "FRESH_KICKS");
        ROT_QUEST_ID.put("BOMBARDINO CROCODILO",   "SORTIE");
        ROT_QUEST_ID.put("LIRILI LARILA",          "AGAINST_THE_CLOCK");
        ROT_QUEST_ID.put("BRR BRR PATAPIM",        "KING_OF_THE_JUNGLE");
        ROT_QUEST_ID.put("BONECA AMBALABU",        "BURNOUT");
        ROT_QUEST_ID.put("UDIN DIN DIN DIN DUN",   "FREQUENCY_DETECTED");
        ROT_QUEST_ID.put("CAPUCCINO ASSASSINO",    "LAST_DROP");
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    private QuestSystem() {
        // Initialize per-rot win counters
        for (String name : ROT_QUEST_ID.keySet()) rotWins.put(name, 0);
        registerAll();
    }

    // ── Registration ─────────────────────────────────────────────────────────

    private void reg(String id, String name, String desc,
                     int difficulty, boolean hidden, int goal,
                     RewardType rewardType, int rewardCoins, String rewardItemName) {
        String asset = "/res/Achievements/PROGRESSION_BADGES/ASCENSION.png";
        Quest q = new Quest(id, name, desc, asset, difficulty, hidden, goal,
                rewardType, rewardCoins, rewardItemName);
        registry.put(id, q);
    }

    private void registerAll() {

        // ── Battle (6) ────────────────────────────────────────────────────────
        reg("SPEED_DEMON",
                "SPEED DEMON",
                "Win a battle without the enemy ever acting.",
                1, false, 1,
                RewardType.ITEM, 0, "BLUE CAPSULE");

        reg("FLAWLESS_VICTORY",
                "FLAWLESS VICTORY",
                "Win a battle without taking any damage.",
                2, false, 1,
                RewardType.COINS, 1000, null);

        reg("NO_HEALS",
                "NO HEALS",
                "Win a battle without using any items.",
                3, false, 1,
                RewardType.ITEM, 0, "SUPER STEW");

        reg("COMEBACK_KID",
                "COMEBACK KID",
                "Win a battle after dropping below 10 percent HP.",
                4, false, 1,
                RewardType.ITEM, 0, "MODERATE STEW");

        reg("BULLY",
                "BULLY",
                "Win a battle against a higher level BrainRot.",
                5, false, 1,
                RewardType.COINS, 800, null);

        reg("OVERKILL",
                "OVERKILL",
                "Deal damage exceeding the enemy max HP in one hit.",
                6, false, 1,
                RewardType.COINS, 1500, null);

        // Collection
        reg("FIRST_CATCH",
                "FIRST CATCH",
                "Catch your first BrainRot.",
                7, false, 1,
                RewardType.COINS, 500, null);

        reg("CLEAN_CATCH",
                "CLEAN CATCH",
                "Capture a BrainRot at full HP.",
                8, false, 1,
                RewardType.ITEM, 0, "RED CAPSULE");

        reg("GROWING_COLLECTION",
                "GROWING COLLECTION",
                "Catch 5 BrainRots.",
                9, false, 5,
                RewardType.ITEM, 0, "SPEED CAPSULE");

        reg("DIAMOND_MIND",
                "DIAMOND MIND",
                "Obtain a Diamond tier BrainRot.",
                10, false, 1,
                RewardType.ITEM, 0, "HEAVY CAPSULE");

        reg("FULL_ROSTER",
                "FULL ROSTER",
                "Own all 8 unique BrainRots.",
                11, false, 8,
                RewardType.BRAINROT, 0, null);

        reg("ORGANIZED",
                "ORGANIZED",
                "Fill your party with 6 BrainRots.",
                12, false, 6,
                RewardType.ITEM, 0, "NORMAL CAPSULE");

        // Progression
        reg("LEVEL_GRINDER",
                "LEVEL GRINDER",
                "Raise any BrainRot to Level 50.",
                13, false, 1,
                RewardType.COINS, 2000, null);

        reg("SKILL_COLLECTOR",
                "SKILL COLLECTOR",
                "Have one skill of every type across your party.",
                14, false, 1,
                RewardType.ITEM, 0, "FOCUS STANCE SCROLL");

        reg("MAX_POTENTIAL",
                "MAX POTENTIAL",
                "Raise any BrainRot to Level 100.",
                15, false, 1,
                RewardType.COINS, 5000, null);

        // Economy
        reg("VARIETY_PACK",
                "VARIETY PACK",
                "Use all 4 item categories: Stew, Antidote, Scroll, and Capsule.",
                16, false, 4,
                RewardType.ITEM, 0, "SUPER STEW");

        reg("ITEM_ADDICT",
                "ITEM ADDICT",
                "Use 50 items total.",
                17, false, 50,
                RewardType.COINS, 1000, null);

        reg("BIG_SPENDER",
                "BIG SPENDER",
                "Spend 10,000 coins total.",
                18, false, 10000,
                RewardType.COINS, 3000, null);

        // BrainRot-specific
        reg("THE_ETERNAL_DRUM",
                "THE ETERNAL DRUM",
                "Win 10 battles with Tung Tung Tung Sahur.",
                19, false, 10,
                RewardType.ITEM, 0, "INFINITE SAHUR SCROLL");

        reg("FRESH_KICKS",
                "FRESH KICKS",
                "Win 10 battles with Tralalero Tralala.",
                20, false, 10,
                RewardType.ITEM, 0, "NEON RAVE SCROLL");

        reg("SORTIE",
                "SORTIE",
                "Win 10 battles with Bombardino Crocodilo.",
                21, false, 10,
                RewardType.ITEM, 0, "RISTRETTO NUKE SCROLL");

        reg("AGAINST_THE_CLOCK",
                "AGAINST THE CLOCK",
                "Win 10 battles with Lirili Larila.",
                22, false, 10,
                RewardType.ITEM, 0, "TIMELINE ROT SCROLL");

        reg("KING_OF_THE_JUNGLE",
                "KING OF THE JUNGLE",
                "Win 10 battles with Brr Brr Patapim.",
                23, false, 10,
                RewardType.ITEM, 0, "FOREST GRUNT SCROLL");

        reg("BURNOUT",
                "BURNOUT",
                "Win 10 battles with Boneca Ambalabu.",
                24, false, 10,
                RewardType.ITEM, 0, "LICKING LOOP SCROLL");

        reg("FREQUENCY_DETECTED",
                "FREQUENCY DETECTED",
                "Win 10 battles with Udin Din Din Din Dun.",
                25, false, 10,
                RewardType.ITEM, 0, "BRAIN SCRAMBLE SCROLL");

        reg("LAST_DROP",
                "LAST DROP",
                "Win 10 battles with Capuccino Assassino.",
                26, false, 10,
                RewardType.ITEM, 0, "DOUBLE SHOT SCROLL");

        // Secret
        reg("ITEM_HOARDER",
                "ITEM HOARDER",
                "Use 5 items in a single battle.",
                27, true, 1,
                RewardType.ITEM, 0, "MILD STEW");

        reg("IMPOSTER",
                "IMPOSTER",
                "Beat a trainer whose lead BrainRot shares your lead's type.",
                28, true, 1,
                RewardType.COINS, 2000, null);

        reg("WHATS_IN_THE_BOX",
                "WHAT'S IN THE BOX",
                "Place a BrainRot in Box 10, Slot 25.",
                29, true, 1,
                RewardType.ITEM, 0, "MASTER CAPSULE");

        reg("BRAIN_FULLY_ROT",
                "BRAIN FULLY ROT",
                "Lose 10 battles in a row.",
                30, true, 1,
                RewardType.COINS, 100, null);

        // Build sorted list
        ordered.addAll(registry.values());
        ordered.sort(Comparator.comparingInt(Quest::getDifficulty));
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void complete(String id) {
        Quest q = registry.get(id);
        if (q == null) { System.err.println("[QuestSystem] Unknown id: " + id); return; }
        if (q.complete()) {
            toastQueue.add(q);
            System.out.println("[Quest] Completed: " + q.getName());
        }
    }

    public void increment(String id) {
        Quest q = registry.get(id);
        if (q == null) { System.err.println("[QuestSystem] Unknown id: " + id); return; }
        if (q.increment()) {
            toastQueue.add(q);
            System.out.println("[Quest] Completed: " + q.getName());
        }
    }

    public void increment(String id, int amount) {
        Quest q = registry.get(id);
        if (q == null) { System.err.println("[QuestSystem] Unknown id: " + id); return; }
        if (q.increment(amount)) {
            toastQueue.add(q);
            System.out.println("[Quest] Completed: " + q.getName());
        }
    }

    public boolean isCompleted(String id) {
        Quest q = registry.get(id);
        return q != null && q.isCompleted();
    }

    public Quest           get(String id) { return registry.get(id); }
    public List<Quest>     getAll()       { return Collections.unmodifiableList(ordered); }
    public Quest           pollToast()    { return toastQueue.poll(); }
    public boolean         hasToast()     { return !toastQueue.isEmpty(); }

    // ── Reward claiming ───────────────────────────────────────────────────────

    /**
     * Claims the reward for a completed quest.
     * Called from QuestUI when player presses ENTER on a completed unclaimed quest.
     */
    public void claimReward(String id, GamePanel gp) {
        Quest q = registry.get(id);
        if (q == null || !q.isCompleted() || q.isRewardClaimed()) return;

        switch (q.getRewardType()) {
            case COINS -> {
                gp.player.earnRotCoins(q.getRewardCoins());
                System.out.println("[Quest] Reward claimed: +" + q.getRewardCoins() + " coins");
            }
            case ITEM -> {
                items.Item item = ItemRegistry.getItem(q.getRewardItemName());
                if (item != null) {
                    gp.player.getInventory().addItem(item);
                    System.out.println("[Quest] Reward claimed: " + q.getRewardItemName());
                } else {
                    System.err.println("[Quest] Reward item not found: " + q.getRewardItemName());
                }
            }
            case BRAINROT -> {
                String[] names = BrainRotRegistry.ALL.toArray(new String[0]);
                String name = names[RandomUtil.range(0, names.length - 1)];
                BrainRot rot = BrainRotFactory.create(name, Tier.DIAMOND);
                gp.player.getPCSYSTEM().addBrainRot(rot);
                System.out.println("[Quest] Reward claimed: " + rot.getName() + " DIAMOND");
            }
            case NONE -> {}
        }

        q.markRewardClaimed();
    }

    // ── Hook Methods ──────────────────────────────────────────────────────────

    // ── Battle hooks ──────────────────────────────────────────────────────────

    /**
     * Call at end of a won battle.
     */
    public void onBattleWon(BrainRot playerRot, BrainRot enemyRot,
                            boolean enemyActed, boolean playerTookDamage,
                            int playerMinHP, boolean noItemsUsed,
                            boolean isTrainerBattle, String trainerLeadType) {

        consecutiveLosses = 0;

        if (!enemyActed)       complete("SPEED_DEMON");
        if (!playerTookDamage) complete("FLAWLESS_VICTORY");
        if (noItemsUsed)       complete("NO_HEALS");

        int maxHp = playerRot.getMaxHp();
        if (playerMinHP > 0 && playerMinHP <= (int)(maxHp * 0.10))
            complete("COMEBACK_KID");

        if (playerRot.getLevel() < enemyRot.getLevel()) complete("BULLY");

        if (isTrainerBattle && trainerLeadType != null) {
            String playerType = playerRot.getPrimaryType().name();
            if (playerType.equalsIgnoreCase(trainerLeadType)) complete("IMPOSTER");
        }

        // BrainRot-specific win counter
        onBrainRotWin(playerRot.getName());
    }

    /**
     * Call when a single damaging hit is resolved.
     */
    public void onDamageDealt(int damage, int enemyMaxHp) {
        if (damage >= enemyMaxHp) complete("OVERKILL");
    }

    /**
     * Call when the player loses a battle.
     */
    public void onBattleLost() {
        consecutiveLosses++;
        if (consecutiveLosses >= 10) complete("BRAIN_FULLY_ROT");
    }

    /**
     * Call when the active BrainRot wins a battle.
     * Increments that BrainRot's personal win counter.
     */
    public void onBrainRotWin(String rotName) {
        String key = rotName.toUpperCase();
        if (!rotWins.containsKey(key)) return;

        int wins = rotWins.get(key) + 1;
        rotWins.put(key, wins);

        String questId = ROT_QUEST_ID.get(key);
        if (questId == null) return;

        Quest q = registry.get(questId);
        if (q != null && !q.isCompleted()) {
            q.loadState(false, wins);
            if (wins >= 10) complete(questId);
        }
    }

    // ── Capture hooks ─────────────────────────────────────────────────────────

    public void onCapture(BrainRot rot, boolean atFullHp) {
        captureCount++;
        capturedNames.add(rot.getName().toUpperCase());

        complete("FIRST_CATCH");
        increment("GROWING_COLLECTION");

        if (rot.getTier() == brainrots.Tier.DIAMOND) complete("DIAMOND_MIND");
        if (atFullHp) complete("CLEAN_CATCH");

        // Full Roster — all 8 unique names
        boolean hasAll = BrainRotRegistry.ALL.stream()
                .map(String::toUpperCase)
                .allMatch(capturedNames::contains);
        if (hasAll) complete("FULL_ROSTER");
    }

    // ── PC / Party hooks ──────────────────────────────────────────────────────

    public void onPartySizeChanged(int partySize) {
        Quest q = get("ORGANIZED");
        if (q != null && !q.isCompleted()) {
            q.loadState(false, partySize);
            if (partySize >= 6) complete("ORGANIZED");
        }
    }

    public void onBoxSlotSet(int boxIndex, int slotIndex) {
        if (boxIndex == 9 && slotIndex == 24) complete("WHATS_IN_THE_BOX");
    }

    // ── Level up hook ─────────────────────────────────────────────────────────

    public void onLevelUp(int newLevel) {
        if (newLevel >= 50)  complete("LEVEL_GRINDER");
        if (newLevel >= 100) complete("MAX_POTENTIAL");
    }

    // ── Shop hook ─────────────────────────────────────────────────────────────

    public void onCoinsSpent(int amount) {
        totalCoinsSpent += amount;
        increment("BIG_SPENDER", amount);
    }

    // ── Item hooks ────────────────────────────────────────────────────────────

    public void onItemUsed(String category) {
        totalItemsUsed++;
        increment("ITEM_ADDICT");

        categoriesUsed.add(category.toUpperCase());
        Quest vp = get("VARIETY_PACK");
        if (vp != null && !vp.isCompleted()) {
            vp.loadState(false, categoriesUsed.size());
            if (categoriesUsed.size() >= 4) complete("VARIETY_PACK");
        }
    }

    public void onBattleItemThreshold() {
        complete("ITEM_HOARDER");
    }

    // ── Skill Collector check ─────────────────────────────────────────────────

    public void checkSkillCollector(java.util.List<BrainRot> party) {
        Set<String> types = new HashSet<>();
        for (BrainRot rot : party)
            for (skills.Skill s : rot.getMoves())
                types.add(s.getType().name());
        if (types.size() >= 11) complete("SKILL_COLLECTOR");
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    public String toFileFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("[QUESTS]\n");

        // Counters line
        sb.append("counters:")
                .append(totalCoinsSpent).append(";")
                .append(totalItemsUsed).append(";")
                .append(consecutiveLosses).append(";")
                .append(captureCount).append(";")
                .append(String.join(",", capturedNames)).append(";")
                .append(String.join(",", categoriesUsed)).append("\n");

        // Per-rot wins line
        sb.append("rotwins:");
        rotWins.forEach((k, v) -> sb.append(k).append("=").append(v).append(","));
        sb.append("\n");

        // Quest states
        for (Quest q : ordered)
            sb.append(q.toFileFormat()).append("\n");

        return sb.toString();
    }

    public void loadFromLines(List<String> lines) {
        if (lines.isEmpty()) return;

        int lineIdx = 0;

        // Counters
        if (lineIdx < lines.size() && lines.get(lineIdx).startsWith("counters:")) {
            String[] c = lines.get(lineIdx).replace("counters:", "").split(";");
            if (c.length >= 6) {
                totalCoinsSpent   = parseInt(c[0]);
                totalItemsUsed    = parseInt(c[1]);
                consecutiveLosses = parseInt(c[2]);
                captureCount      = parseInt(c[3]);
                if (!c[4].isEmpty())
                    Arrays.stream(c[4].split(",")).forEach(capturedNames::add);
                if (!c[5].isEmpty())
                    Arrays.stream(c[5].split(",")).forEach(categoriesUsed::add);
            }
            lineIdx++;
        }

        // Per-rot wins
        if (lineIdx < lines.size() && lines.get(lineIdx).startsWith("rotwins:")) {
            String raw = lines.get(lineIdx).replace("rotwins:", "");
            if (!raw.isEmpty()) {
                for (String entry : raw.split(",")) {
                    String[] kv = entry.split("=");
                    if (kv.length == 2) rotWins.put(kv[0], parseInt(kv[1]));
                }
            }
            lineIdx++;
        }

        // Quest states
        for (int i = lineIdx; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(";");
            if (parts.length < 3) continue;
            String  id            = parts[0];
            boolean completed     = Boolean.parseBoolean(parts[1]);
            int     progress      = parseInt(parts[2]);
            boolean rewardClaimed = parts.length >= 4 && Boolean.parseBoolean(parts[3]);
            Quest   q             = registry.get(id);
            if (q != null) q.loadState(completed, progress, rewardClaimed);
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    public static void reset() { instance = new QuestSystem(); }
}