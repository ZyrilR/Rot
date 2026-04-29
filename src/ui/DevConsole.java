package ui;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.Tier;
import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import utils.AssetManager;
import utils.Directories;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

/**
 * Developer cheat console -- toggle with \ (backslash).
 * All commands are prefixed with /
 *
 * COMMAND REFERENCE
 *
 *  /addrot "<name>" <level> [tier]
 *      Adds a BrainRot to the party (or PC box if full).
 *      tier: NORMAL | GOLD | DIAMOND  (optional, derived from level if omitted)
 *      Example: /addrot "TUNG TUNG TUNG SAHUR" 30 GOLD
 *
 *  /additem "<name>" [qty]
 *      Adds any item (stew, antidote, capsule, scroll, UP bottle) to inventory.
 *      Example: /additem "Sahur Chant Scroll" 3
 *
 *  /addcoins <amount>
 *      Adds RotCoins to the player wallet.
 *      Example: /addcoins 9999
 *
 *  /heal <slot> [amount]
 *      Heals party BrainRot in slot 1-6. Omit amount for full restore.
 *      Example: /heal 1        (full heal)
 *      Example: /heal 3 50     (restore 50 HP)
 *
 *  /healall
 *      Fully restores HP, UP, and stat modifiers for the entire party.
 *
 *  /levelup <slot|all> [amount]
 *      Level up a party slot (1-6) or all party members by [amount] levels.
 *      Example: /levelup 1 10
 *      Example: /levelup all 5
 *
 *  /speed <multiplier>
 *      Sets player movement speed to base * multiplier. Use 1 to reset.
 *      Example: /speed 2
 *
 *  /teleport <mapname> <x> <y>
 *      Teleports to a tile on the named map.
 *      Maps: ROUTE131 | ROUTE132 | ROUTE130 | CAVE131 | MARKET
 *      Example: /teleport ROUTE131 20 30
 *
 *  /help
 *      Lists all commands. Use UP/DOWN arrows to scroll.
 */
public class DevConsole {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int BAR_H         = 36;
    private static final int BAR_PAD_X     = 12;
    private static final int HISTORY_ROWS  = 7;
    private static final int HISTORY_ROW_H = 17;
    private static final int HISTORY_H     = HISTORY_ROWS * HISTORY_ROW_H + 10;
    private static final int CURSOR_BLINK  = 30;

    // Must match Player.java initial speed value
    private static final int BASE_SPEED = 32;

    // ── State ─────────────────────────────────────────────────────────────────
    private final GamePanel gp;
    private boolean open        = false;
    private int     blinkTick   = 0;
    private int     scrollOffset = 0;   // 0 = newest at bottom

    public final StringBuilder inputBuffer = new StringBuilder();

    private final List<LogEntry> log = new ArrayList<>();
    private static final int MAX_LOG = 128;

    // ── Constructor ───────────────────────────────────────────────────────────
    public DevConsole(GamePanel gp) {
        this.gp = gp;
        pushLog("DEV CONSOLE ready.  Type /help for commands.", LogType.INFO);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public boolean isOpen() { return open; }

    public void toggle() {
        open = !open;
        if (open) {
            inputBuffer.setLength(0);
            scrollOffset = 0;
            System.out.println("[DevConsole] Opened.");
        } else {
            System.out.println("[DevConsole] Closed.");
        }
    }

    public void close() {
        open = false;
        inputBuffer.setLength(0);
        scrollOffset = 0;
    }

    public void submit() {
        String raw = inputBuffer.toString().trim();
        inputBuffer.setLength(0);
        scrollOffset = 0;
        if (raw.isEmpty()) return;
        pushLog(raw, LogType.INPUT);
        executeCommand(raw);
    }

    public void backspace() {
        if (inputBuffer.length() > 0)
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
    }

    public void typeChar(char c) {
        if (inputBuffer.length() < 120) inputBuffer.append(c);
    }

    public void scrollUp() {
        int maxScroll = Math.max(0, log.size() - HISTORY_ROWS);
        if (scrollOffset < maxScroll) scrollOffset++;
    }

    public void scrollDown() {
        if (scrollOffset > 0) scrollOffset--;
    }

    // ── Update / Draw ─────────────────────────────────────────────────────────

    public void update() {
        if (!open) return;
        blinkTick = (blinkTick + 1) % (CURSOR_BLINK * 2);
    }

    public void draw(Graphics2D g2) {
        if (!open) return;

        // Use a standard Java monospaced font so every ASCII character (/, -, |, <, >, etc.)
        // renders correctly. pokemonGb is a bitmap pixel font with a very limited glyph set
        // and many symbols either render as boxes or are missing entirely.
        Font base  = new Font("Monospaced", Font.PLAIN, 13);
        Font bold  = new Font("Monospaced", Font.BOLD,  13);
        Font small = new Font("Monospaced", Font.BOLD,  11);

        int barY  = SCREEN_HEIGHT - BAR_H;
        int histY = barY - HISTORY_H;

        // History background
        g2.setColor(new Color(8, 8, 8, 215));
        g2.fillRect(0, histY, SCREEN_WIDTH, HISTORY_H);

        // Gold separator line
        g2.setColor(new Color(216, 184, 88));
        g2.fillRect(0, barY - 1, SCREEN_WIDTH, 1);

        g2.setFont(base);
        FontMetrics fm = g2.getFontMetrics();
        int maxTextW = SCREEN_WIDTH - BAR_PAD_X * 2;

        // Draw history rows (bottom-aligned window from scrollOffset)
        int lastIdx = Math.min(log.size(), scrollOffset + HISTORY_ROWS);
        for (int i = scrollOffset; i < lastIdx; i++) {
            LogEntry entry = log.get(i);
            int slot = i - scrollOffset;          // 0 = bottom-most visible row
            int rowY = barY - 6 - slot * HISTORY_ROW_H;
            g2.setColor(entry.type.color);
            g2.drawString(truncate(entry.text, fm, maxTextW), BAR_PAD_X, rowY);
        }

        // Scroll arrows
        g2.setFont(small);
        FontMetrics sfm = g2.getFontMetrics();
        if (scrollOffset > 0) {
            g2.setColor(new Color(216, 184, 88, 200));
            g2.drawString("v more", SCREEN_WIDTH - sfm.stringWidth("v more") - BAR_PAD_X, barY - 3);
        }
        if (scrollOffset + HISTORY_ROWS < log.size()) {
            g2.setColor(new Color(216, 184, 88, 200));
            g2.drawString("^ more", SCREEN_WIDTH - sfm.stringWidth("^ more") - BAR_PAD_X, histY + 13);
        }

        // Input bar
        g2.setColor(new Color(15, 15, 15, 235));
        g2.fillRect(0, barY, SCREEN_WIDTH, BAR_H);
        g2.setColor(new Color(216, 184, 88));
        g2.fillRect(0, barY, SCREEN_WIDTH, 2);     // gold top border

        g2.setFont(bold);
        g2.setColor(new Color(216, 184, 88));
        String prompt = "[DEV] > ";
        g2.drawString(prompt, BAR_PAD_X, barY + 24);
        int promptW = g2.getFontMetrics().stringWidth(prompt);

        g2.setFont(base);
        g2.setColor(new Color(225, 225, 225));
        String input = inputBuffer.toString();
        g2.drawString(input, BAR_PAD_X + promptW, barY + 24);

        // Blinking block cursor
        if (blinkTick < CURSOR_BLINK) {
            int cursorX = BAR_PAD_X + promptW + fm.stringWidth(input);
            g2.setColor(new Color(216, 184, 88, 200));
            g2.fillRect(cursorX + 1, barY + 8, 8, 16);
        }
    }

    // ── Command Dispatcher ────────────────────────────────────────────────────

    private void executeCommand(String raw) {
        if (!raw.startsWith("/")) {
            err("Commands must start with /   Try /help");
            return;
        }
        List<String> tokens = tokenise(raw.substring(1));
        if (tokens.isEmpty()) return;

        String cmd = tokens.get(0).toLowerCase();
        switch (cmd) {
            case "addrot"   -> cmdAddRot(tokens);
            case "additem"  -> cmdAddItem(tokens);
            case "addcoins" -> cmdAddCoins(tokens);
            case "heal"     -> cmdHeal(tokens);
            case "healall"  -> cmdHealAll();
            case "levelup"  -> cmdLevelUp(tokens);
            case "speed"    -> cmdSpeed(tokens);
            case "teleport" -> cmdTeleport(tokens);
            case "help"     -> cmdHelp();
            default         -> err("Unknown command: /" + cmd + "   Try /help");
        }
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    private void cmdAddRot(List<String> t) {
        if (t.size() < 3) {
            err("Usage: /addrot \"<name>\" <level>");
            info("  Ex: /addrot \"TUNG TUNG TUNG SAHUR\" 30");
            return;
        }

        String name  = t.get(1).toUpperCase();
        int    level = parseInt(t.get(2), -1);

        if (level < 1 || level > 100) { err("Level must be 1-100.  Got: " + t.get(2)); return; }

        BrainRot rot;
        try {
            rot = BrainRotFactory.create(name, level);
        } catch (IllegalArgumentException e) {
            err("Unknown BrainRot: '" + name + "'");
            info("  Valid: TUNG TUNG TUNG SAHUR   TRALALERO TRALALA");
            info("         BOMBARDINO CROCODILO   LIRILI LARILA");
            info("         BRR BRR PATAPIM        BONECA AMBALABU");
            info("         UDIN DIN DIN DIN DUN   CAPUCCINO ASSASSINO");
            return;
        }

        var result = gp.player.getPCSYSTEM().addBrainRot(rot);
        ok("Added " + rot.getName() + " [" + rot.getTier() + " Lv." + rot.getLevel() + "]  " + result.name());
    }

    private void cmdAddItem(List<String> t) {
        if (t.size() < 2) {
            err("Usage: /additem \"<item name>\" [qty]");
            info("  Ex: /additem \"Sahur Chant Scroll\" 3");
            info("  Ex: /additem \"MASTER CAPSULE\"");
            return;
        }

        String itemName = t.get(1).toUpperCase();
        int    qty      = (t.size() >= 3) ? parseInt(t.get(2), 1) : 1;

        if (qty < 1 || qty > 99) { err("Quantity must be 1-99.  Got: " + t.get(2)); return; }

        Item item = ItemRegistry.getItem(itemName);
        if (item == null) {
            err("Item not found: '" + itemName + "'");
            info("  Ex: MILD STEW, NORMAL CAPSULE, Sahur Chant Scroll, UP BOTTLE");
            return;
        }

        int added = 0;
        for (int i = 0; i < qty; i++) {
            if (gp.player.getInventory().addItem(item)) added++;
            else { err("Inventory full after adding " + added + "."); break; }
        }
        if (added > 0) ok("Added " + added + "x " + item.getName());
    }

    private void cmdAddCoins(List<String> t) {
        if (t.size() < 2) {
            err("Usage: /addcoins <amount>");
            info("  Ex: /addcoins 9999");
            return;
        }
        int amount = parseInt(t.get(1), -1);
        if (amount < 1) { err("Amount must be a positive integer."); return; }
        gp.player.earnRotCoins(amount);
        ok("+" + amount + " RotCoins.  Total: " + gp.player.getRotCoins());
    }

    private void cmdHeal(List<String> t) {
        if (t.size() < 2) {
            err("Usage: /heal <slot 1-6> [amount]");
            info("  Ex: /heal 1       (full restore)");
            info("  Ex: /heal 2 50    (restore 50 HP)");
            return;
        }

        int slot = parseInt(t.get(1), -1);
        if (slot < 1 || slot > 6) { err("Slot must be 1-6.  Got: " + t.get(1)); return; }

        BrainRot rot = gp.player.getPCSYSTEM().getPartyMember(slot - 1);
        if (rot == null) { err("No BrainRot in slot " + slot); return; }

        if (t.size() >= 3) {
            int amount = parseInt(t.get(2), -1);
            if (amount < 1) { err("Amount must be a positive integer."); return; }
            int before = rot.getCurrentHp();
            rot.heal(amount);
            ok(rot.getName() + " [slot " + slot + "] +"
                    + (rot.getCurrentHp() - before) + " HP  ("
                    + rot.getCurrentHp() + "/" + rot.getMaxHp() + ")");
        } else {
            rot.restoreForBattle();
            ok(rot.getName() + " [slot " + slot + "] fully restored  ("
                    + rot.getCurrentHp() + "/" + rot.getMaxHp() + ")");
        }
    }

    private void cmdHealAll() {
        int size = gp.player.getPCSYSTEM().getPartySize();
        if (size == 0) { err("Party is empty."); return; }
        for (int i = 0; i < size; i++) {
            BrainRot rot = gp.player.getPCSYSTEM().getPartyMember(i);
            if (rot != null) rot.restoreForBattle();
        }
        ok("All " + size + " party BrainRot(s) fully restored.");
    }

    private void cmdLevelUp(List<String> t) {
        if (t.size() < 2) {
            err("Usage: /levelup <slot 1-6 | all> [amount]");
            info("  Ex: /levelup 1 10");
            info("  Ex: /levelup all 5");
            return;
        }

        String target = t.get(1).toLowerCase();
        int    amount = (t.size() >= 3) ? parseInt(t.get(2), 1) : 1;

        if (amount < 1 || amount > 99) { err("Amount must be 1-99.  Got: " + t.get(2)); return; }

        if (target.equals("all")) {
            int size = gp.player.getPCSYSTEM().getPartySize();
            if (size == 0) { err("Party is empty."); return; }
            for (int i = 0; i < size; i++)
                levelUpRot(gp.player.getPCSYSTEM().getPartyMember(i), i + 1, amount);
        } else {
            int slot = parseInt(target, -1);
            if (slot < 1 || slot > 6) { err("Slot must be 1-6 or 'all'.  Got: " + t.get(1)); return; }
            BrainRot rot = gp.player.getPCSYSTEM().getPartyMember(slot - 1);
            if (rot == null) { err("No BrainRot in slot " + slot); return; }
            levelUpRot(rot, slot, amount);
        }
    }

    /** Shared level-up loop. Drives real gainXp() so all hooks fire normally. */
    private void levelUpRot(BrainRot rot, int slot, int amount) {
        if (rot == null) return;
        int start  = rot.getLevel();
        int target = Math.min(100, start + amount);
        int gained = 0;
        while (rot.getLevel() < target) {
            int xpNeeded = rot.getXpToNextLevel() - rot.getCurrentXp() + 1;
            rot.gainXp(Math.max(xpNeeded, 1));
            gained++;
            if (rot.getLevel() >= 100) break;
        }
        ok("[slot " + slot + "] " + rot.getName()
                + "  Lv." + start + " -> Lv." + rot.getLevel() + "  (+" + gained + ")");
    }

    private void cmdSpeed(List<String> t) {
        if (t.size() < 2) {
            err("Usage: /speed <multiplier>");
            info("  Ex: /speed 2     (double speed)");
            info("  Ex: /speed 1     (reset to normal)");
            info("  Ex: /speed 0.5   (half speed)");
            return;
        }

        double mult;
        try {
            mult = Double.parseDouble(t.get(1).trim());
        } catch (NumberFormatException e) {
            err("Multiplier must be a number.  Got: " + t.get(1));
            return;
        }

        if (mult < 0.1 || mult > 10.0) {
            err("Multiplier must be 0.1-10.  Got: " + mult);
            return;
        }

        int newSpeed = (int) Math.round(BASE_SPEED * mult);
        gp.player.speed = newSpeed;
        ok("Player speed set to " + newSpeed + "  (x" + mult + "  base=" + BASE_SPEED + ")");
    }

    private void cmdTeleport(List<String> t) {
        if (t.size() < 4) {
            err("Usage: /teleport <mapname> <x> <y>");
            info("  Maps: ROUTE131, ROUTE132, ROUTE130, CAVE131, MARKET");
            info("  Ex: /teleport ROUTE131 20 30");
            return;
        }

        String mapName = t.get(1).toUpperCase();
        int x = parseInt(t.get(2), -1);
        int y = parseInt(t.get(3), -1);

        if (x < 0 || y < 0) { err("x and y must be non-negative integers."); return; }

        String path;
        try {
            path = Directories.getPath(mapName);
        } catch (Exception e) {
            err("Unknown map: '" + mapName + "'");
            info("  Valid: ROUTE131, ROUTE132, ROUTE130, CAVE131, MARKET");
            return;
        }

        if (!path.equalsIgnoreCase(gp.CURRENT_PATH)) {
            gp.world.loadMap(path, true);
            gp.CURRENT_PATH = path;
            gp.DARKNESSOVERLAY.setActive(path.toLowerCase().contains("cave"));
        }

        gp.player.teleport(new int[]{x, y});
        ok("Teleported to " + mapName + " (" + x + ", " + y + ")");
    }

    private void cmdHelp() {
        // Push lines in reverse order so the header reads top-to-bottom in the log
        // (log index 0 = newest = bottom-most visible row)
        info("  Press \\ to close.  UP/DOWN arrows scroll this log.");
        info("---------------------------------------------------------");
        info("  /help                            Show this list");
        info("  /teleport <map> <x> <y>          Teleport player");
        info("     Maps: ROUTE131 ROUTE132 ROUTE130 CAVE131 MARKET");
        info("  /speed <multiplier>              Set speed (1=normal)");
        info("  /levelup <slot|all> [amount]     Level up slot or party");
        info("  /healall                         Restore entire party");
        info("  /heal <slot> [amount]            Heal party slot 1-6");
        info("  /addcoins <amount>               Add RotCoins");
        info("  /additem \"<name>\" [qty]          Add item to inventory");
        info("  /addrot \"<name>\" <level> [tier]  Add BrainRot to party");
        info("---------------------------------------------------------");
        info("  BrainRot Dev Console -- all commands start with /");
    }

    // ── Tokeniser ─────────────────────────────────────────────────────────────

    private List<String> tokenise(String raw) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char inQuote = 0;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (inQuote != 0) {
                if (c == inQuote) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    inQuote = 0;
                } else {
                    current.append(c);
                }
            } else if (c == '"' || c == '\'') {
                if (current.length() > 0) { tokens.add(current.toString()); current.setLength(0); }
                inQuote = c;
            } else if (c == ' ' || c == '\t') {
                if (current.length() > 0) { tokens.add(current.toString()); current.setLength(0); }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) tokens.add(current.toString());
        return tokens;
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    private enum LogType {
        INPUT(new Color(180, 180, 180)),
        OK   (new Color(80,  210, 100)),
        ERR  (new Color(230, 80,  70)),
        INFO (new Color(140, 200, 255));

        final Color color;
        LogType(Color c) { this.color = c; }
    }

    private record LogEntry(String text, LogType type) {}

    private void pushLog(String text, LogType type) {
        log.add(0, new LogEntry(text, type));
        if (log.size() > MAX_LOG) log.remove(log.size() - 1);
        System.out.println("[DevConsole][" + type + "] " + text);
    }

    private void ok  (String msg) { pushLog(msg, LogType.OK);   }
    private void err (String msg) { pushLog(msg, LogType.ERR);  }
    private void info(String msg) { pushLog(msg, LogType.INFO); }

    // ── Util ──────────────────────────────────────────────────────────────────

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "...") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }
}