package progression;

import brainrots.BrainRot;
import skills.Skill;
import skills.SkillType;

import java.util.*;

/**
 * Central manager for all 30 quests.
 *
 * Usage:
 *   QuestSystem.getInstance().complete("SPEED_DEMON");
 *   QuestSystem.getInstance().increment("ITEM_ADDICT");
 *
 * Hook points:
 *   BattleManager   → battle flags
 *   CaptureManager  → capture events
 *   PCSystem        → party/box events
 *   ShopUI          → spend events
 *   InventoryUI     → item use events
 *   BrainRot        → level up events
 */
public class QuestSystem {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static QuestSystem instance;

    public static QuestSystem getInstance() {
        if (instance == null) instance = new QuestSystem();
        return instance;
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    private final List<Quest>         ordered   = new ArrayList<>();
    private final Map<String, Quest>  registry  = new LinkedHashMap<>();
    private final Queue<Quest>        toastQueue = new LinkedList<>();

    // ── Persistent counters ───────────────────────────────────────────────────

    private int            totalCoinsSpent    = 0;
    private int            totalItemsUsed     = 0;
    private int            tungTungWins       = 0;
    private int            consecutiveLosses  = 0;
    private int            totalConfusionHits = 0;
    private int            timelineRotBattles = 0;
    private int            captureCount       = 0;
    private final Set<String> capturedNames   = new HashSet<>();
    private final Set<String> categoriesUsed  = new HashSet<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    private QuestSystem() { registerAll(); }

    // ── Registration ─────────────────────────────────────────────────────────

    private void reg(String id, String name, String desc,
                     int difficulty, boolean hidden, int goal) {
        String asset = "/res/Achievements/PROGRESSION_BADGES/ASCENSION.png"; //test for now
        Quest q = new Quest(id, name, desc, asset, difficulty, hidden, goal);
        registry.put(id, q);
    }

    private void registerAll() {

        // ── Non-hidden (difficulty 1–26) ──────────────────────────────────────

        reg("FIRST_CATCH",
                "First Catch",
                "Catch your first BrainRot.",
                1, false, 1);

        reg("ORGANIZED",
                "Organized",
                "Fill your party with 6 BrainRots.",
                2, false, 6);

        reg("VARIETY_PACK",
                "Variety Pack",
                "Use all 4 item categories: Stew, Antidote, Scroll, and Capsule.",
                3, false, 4);

        reg("BULLY",
                "Bully",
                "Win a battle against a higher level BrainRot.",
                4, false, 1);

        reg("CLEAN_CATCH",
                "Clean Catch",
                "Capture a BrainRot at full HP.",
                5, false, 1);

        reg("GROWING_COLLECTION",
                "Growing Collection",
                "Catch 5 BrainRots.",
                6, false, 5);

        reg("ITEM_ADDICT",
                "Item Addict",
                "Use 50 items total.",
                7, false, 50);

        reg("SPEED_DEMON",
                "Speed Demon",
                "Win a battle without the enemy ever acting.",
                8, false, 1);

        reg("FLAWLESS_VICTORY",
                "Flawless Victory",
                "Win a battle without taking any damage.",
                9, false, 1);

        reg("CLUTCH_KING",
                "Clutch King",
                "Win a battle with exactly 1 HP remaining.",
                10, false, 1);

        reg("COMEBACK_KID",
                "Comeback Kid",
                "Win a battle after your BrainRot dropped below 10 percent HP.",
                11, false, 1);

        reg("BIG_SPENDER",
                "Big Spender",
                "Spend 10,000 coins total.",
                12, false, 10000);

        reg("DIAMOND_MIND",
                "Diamond Mind",
                "Obtain a Diamond tier BrainRot.",
                13, false, 1);

        reg("LEVEL_GRINDER",
                "Level Grinder",
                "Raise any BrainRot to Level 50.",
                14, false, 1);

        reg("SKILL_COLLECTOR",
                "Skill Collector",
                "Have at least one skill of every type across your party.",
                15, false, 1);

        reg("OVERKILL",
                "Overkill",
                "Deal damage exceeding the enemy's max HP in a single hit.",
                16, false, 1);

        reg("SNEAKER_WAVE",
                "Sneaker Wave",
                "KO an enemy using Sneaker Dash move.",
                17, false, 1);

        reg("TIRE_FIRE",
                "Tire Fire",
                "KO an enemy using Tire Burnout move.",
                18, false, 1);

        reg("DOUBLE_TAP",
                "Double Tap",
                "KO an enemy using Double Shot move.",
                19, false, 1);

        reg("PAYLOAD_DELIVERED",
                "Payload Delivered",
                "KO an enemy using Ristretto Nuke move.",
                20, false, 1);

        reg("DIN_OVERLOAD",
                "Din Overload",
                "Inflict confusion 5 times total.",
                21, false, 5);

        reg("STOMPING_GROUNDS",
                "Stomping Grounds",
                "Win a battle using only Grass or Rock type moves with Brr Brr Patapim.",
                22, false, 1);

        reg("TIME_BREAKER",
                "Time Breaker",
                "Use Timeline Rot move in 5 different battles.",
                23, false, 5);

        reg("DRUM_NEVER_STOPS",
                "Drum Never Stops",
                "Win 10 battles with Tung Tung Tung Sahur in your party.",
                24, false, 10);

        reg("FULL_ROSTER",
                "Full Roster",
                "Own all 8 unique BrainRots.",
                25, false, 8);

        reg("MAX_POTENTIAL",
                "Max Potential",
                "Raise any BrainRot to Level 100.",
                26, false, 1);

        // ── Hidden (difficulty 27–30) ─────────────────────────────────────────

        reg("POTION_HOARDER",
                "Potion Hoarder",
                "Use 5 items in a single battle.",
                27, true, 1);

        reg("IMPOSTER",
                "Imposter",
                "Beat a trainer whose lead BrainRot shares your lead's type.",
                28, true, 1);

        reg("WHATS_IN_THE_BOX",
                "What's In The Box",
                "Place a BrainRot in Box 10, Slot 25.",
                29, true, 1);

        reg("BRAIN_FULLY_ROT",
                "Brain Fully Rot",
                "Lose 10 battles in a row.",
                30, true, 1);

        // Build sorted list by difficulty
        ordered.addAll(registry.values());
        ordered.sort(Comparator.comparingInt(Quest::getDifficulty));
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Completes a quest by ID.
     * Adds to toast queue if newly completed.
     */
    public void complete(String id) {
        Quest q = registry.get(id);
        if (q == null) { System.err.println("[QuestSystem] Unknown id: " + id); return; }
        if (q.complete()) {
            toastQueue.add(q);
            System.out.println("[Quest] Completed: " + q.getName());
        }
    }

    /**
     * Increments a counter quest by 1.
     * Auto-completes and toasts when goal is reached.
     */
    public void increment(String id) {
        Quest q = registry.get(id);
        if (q == null) { System.err.println("[QuestSystem] Unknown id: " + id); return; }
        if (q.increment()) {
            toastQueue.add(q);
            System.out.println("[Quest] Completed: " + q.getName());
        }
    }

    /**
     * Increments a counter quest by a specific amount.
     */
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

    public Quest get(String id) { return registry.get(id); }

    public List<Quest> getAll() { return Collections.unmodifiableList(ordered); }

    public Quest pollToast()  { return toastQueue.poll(); }
    public boolean hasToast() { return !toastQueue.isEmpty(); }

    // ── Hook Methods ──────────────────────────────────────────────────────────

    // ── Battle hooks ──────────────────────────────────────────────────────────

    public void onBattleWon(BrainRot playerRot, BrainRot enemyRot,
                            boolean enemyActed, boolean playerTookDamage,
                            int playerMinHP, Skill killingSkill,
                            boolean usedOnlyGrassRockMoves,
                            boolean isTrainerBattle, String trainerLeadType,
                            boolean timelineRotUsed) {

        consecutiveLosses = 0;

        if (!enemyActed)       complete("SPEED_DEMON");
        if (!playerTookDamage) complete("FLAWLESS_VICTORY");
        if (playerRot.getCurrentHp() == 1) complete("CLUTCH_KING");

        int maxHp = playerRot.getMaxHp();
        if (playerMinHP > 0 && playerMinHP <= (int)(maxHp * 0.10))
            complete("COMEBACK_KID");

        if (playerRot.getLevel() < enemyRot.getLevel()) complete("BULLY");

        if (killingSkill != null) {
            switch (killingSkill.getName().toUpperCase()) {
                case "SNEAKER DASH"   -> complete("SNEAKER_WAVE");
                case "TIRE BURNOUT"   -> complete("TIRE_FIRE");
                case "DOUBLE SHOT"    -> complete("DOUBLE_TAP");
                case "RISTRETTO NUKE" -> complete("PAYLOAD_DELIVERED");
            }
        }

        if (playerRot.getName().equalsIgnoreCase("BRR BRR PATAPIM")
                && usedOnlyGrassRockMoves) {
            complete("STOMPING_GROUNDS");
        }

        if (playerRot.getName().equalsIgnoreCase("TUNG TUNG TUNG SAHUR")) {
            tungTungWins++;
            if (tungTungWins >= 10) complete("DRUM_NEVER_STOPS");
        }

        if (timelineRotUsed) {
            timelineRotBattles++;
            if (timelineRotBattles >= 5) complete("TIME_BREAKER");
        }

        if (isTrainerBattle && trainerLeadType != null) {
            String playerType = playerRot.getPrimaryType().name();
            if (playerType.equalsIgnoreCase(trainerLeadType)) complete("IMPOSTER");
        }
    }

    public void onDamageDealt(int damage, int enemyMaxHp) {
        if (damage >= enemyMaxHp) complete("OVERKILL");
    }

    public void onBattleLost() {
        consecutiveLosses++;
        if (consecutiveLosses >= 10) complete("BRAIN_FULLY_ROT");
    }

    public void onConfusionInflicted() {
        totalConfusionHits++;
        if (totalConfusionHits >= 5) complete("DIN_OVERLOAD");
    }

    // ── Capture hooks ─────────────────────────────────────────────────────────

    public void onCapture(BrainRot rot, boolean atFullHp) {
        captureCount++;
        capturedNames.add(rot.getName().toUpperCase());

        complete("FIRST_CATCH");

        get("GROWING_COLLECTION").increment();
        if (get("GROWING_COLLECTION").isCompleted()) complete("GROWING_COLLECTION");

        if (rot.getTier() == brainrots.Tier.DIAMOND) complete("DIAMOND_MIND");

        if (atFullHp) complete("CLEAN_CATCH");

        String[] all = {
                "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
                "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
                "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"
        };
        boolean hasAll = Arrays.stream(all).allMatch(capturedNames::contains);
        if (hasAll) complete("FULL_ROSTER");
    }

    // ── PC / Party hooks ──────────────────────────────────────────────────────

    public void onPartySizeChanged(int partySize) {
        Quest q = get("ORGANIZED");
        if (q != null && !q.isCompleted()) {
            // Set progress to current party size directly
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
        complete("POTION_HOARDER");
    }

    // ── Skill Collector check ─────────────────────────────────────────────────

    public void checkSkillCollector(java.util.List<BrainRot> party) {
        Set<String> types = new HashSet<>();
        for (BrainRot rot : party)
            for (Skill s : rot.getMoves())
                types.add(s.getType().name());
        if (types.size() >= 11) complete("SKILL_COLLECTOR");
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    public String toFileFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("[QUESTS]\n");
        sb.append("counters:")
                .append(totalCoinsSpent).append(";")
                .append(totalItemsUsed).append(";")
                .append(tungTungWins).append(";")
                .append(consecutiveLosses).append(";")
                .append(totalConfusionHits).append(";")
                .append(timelineRotBattles).append(";")
                .append(captureCount).append(";")
                .append(String.join(",", capturedNames)).append(";")
                .append(String.join(",", categoriesUsed)).append("\n");
        for (Quest q : ordered)
            sb.append(q.toFileFormat()).append("\n");
        return sb.toString();
    }

    public void loadFromLines(List<String> lines) {
        if (lines.isEmpty()) return;

        String[] counters = lines.get(0).replace("counters:", "").split(";");
        if (counters.length >= 9) {
            totalCoinsSpent    = parseInt(counters[0]);
            totalItemsUsed     = parseInt(counters[1]);
            tungTungWins       = parseInt(counters[2]);
            consecutiveLosses  = parseInt(counters[3]);
            totalConfusionHits = parseInt(counters[4]);
            timelineRotBattles = parseInt(counters[5]);
            captureCount       = parseInt(counters[6]);
            if (!counters[7].isEmpty())
                Arrays.stream(counters[7].split(",")).forEach(capturedNames::add);
            if (!counters[8].isEmpty())
                Arrays.stream(counters[8].split(",")).forEach(categoriesUsed::add);
        }

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(";");
            if (parts.length < 3) continue;
            String  id        = parts[0];
            boolean completed = Boolean.parseBoolean(parts[1]);
            int     progress  = parseInt(parts[2]);
            Quest   q         = registry.get(id);
            if (q != null) q.loadState(completed, progress);
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    public static void reset() { instance = new QuestSystem(); }
}