package ui;

import engine.GamePanel;
import progression.QuestSystem;
import save.DataManager;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

/**
 * Minecraft-style world / save-slot selection screen.
 *
 * Controls:
 *   UP / DOWN    — scroll slot list
 *   LEFT / RIGHT — cycle action button (Play | New | Rename | Delete)
 *   ENTER        — confirm action
 *   ESC          — back / cancel
 *
 * Sub-states:
 *   BROWSING        — normal navigation
 *   NAMING_NEW      — typing a name for the NEW world (before it is created)
 *   RENAMING        — typing a new name for an EXISTING world
 *   CONFIRM_DELETE  — yes / no prompt, LEFT / RIGHT toggles
 */
public class WorldSelectUI {

    // ── Save slot data ────────────────────────────────────────────────────────

    public static class SaveSlot {
        public final int         slotId;
        public       String      worldName;
        public       String      mapPath;
        public       BufferedImage screenshot;

        public SaveSlot(int slotId, String worldName, String mapPath, BufferedImage screenshot) {
            this.slotId     = slotId;
            this.worldName  = worldName;
            this.mapPath    = mapPath;
            this.screenshot = screenshot;
        }
    }

    // ── Sub-state ─────────────────────────────────────────────────────────────

    public enum SubState { BROWSING, NAMING_NEW, RENAMING, CONFIRM_DELETE }

    // ── Action buttons ────────────────────────────────────────────────────────

    private static final String[] ACTION_LABELS = { "PLAY", "NEW WORLD", "RENAME", "DELETE" };
    // All four buttons share the same gray tone; selected state brightens it
    private static final Color    ACTION_BASE   = new Color(90, 88, 84);
    private static final Color    ACTION_SEL    = new Color(130, 126, 118);

    // ── Fields ────────────────────────────────────────────────────────────────

    private final GamePanel      gp;
    private final List<SaveSlot> slots = new ArrayList<>();

    private int      slotCursor    = 0;
    private int      actionCursor  = 0;   // 0=Play 1=New 2=Rename 3=Delete
    private int      scrollOffset  = 0;
    private int      inputCooldown = 0;
    private SubState subState      = SubState.BROWSING;
    private String   statusMessage = "";

    // Text buffer — shared between NAMING_NEW and RENAMING
    private StringBuilder textBuffer = new StringBuilder();

    // Delete confirm: 0=YES 1=NO
    private int deleteChoice = 1;

    // ── Layout ────────────────────────────────────────────────────────────────

    private static final int ROW_H        = 80;
    private static final int THUMB_W      = 110;
    private static final int THUMB_H      = 66;
    private static final int VISIBLE_ROWS = 4;

    // ── Constructor ───────────────────────────────────────────────────────────

    public WorldSelectUI(GamePanel gp) { this.gp = gp; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        subState      = SubState.BROWSING;
        statusMessage = "";
        actionCursor  = 0;
        inputCooldown = INPUT_DELAY * 2;
        refreshSlots();
        slotCursor   = 0;
        scrollOffset = 0;
    }

    // ── Slot discovery ────────────────────────────────────────────────────────

    public void refreshSlots() {
        slots.clear();
        File savesRoot = new File("src/res/Saves");
        if (!savesRoot.exists()) {
            System.out.println("[WorldSelectUI] Saves root missing: " + savesRoot.getAbsolutePath());
            return;
        }
        File[] children = savesRoot.listFiles(File::isDirectory);
        if (children == null) return;

        java.util.Arrays.sort(children, (a, b) -> {
            try { return Integer.compare(Integer.parseInt(a.getName()), Integer.parseInt(b.getName())); }
            catch (NumberFormatException e) { return a.getName().compareTo(b.getName()); }
        });

        for (File dir : children) {
            if (!new File(dir, "data.txt").exists()) continue;
            int slotId;
            try { slotId = Integer.parseInt(dir.getName()); }
            catch (NumberFormatException e) { continue; }

            slots.add(new SaveSlot(
                    slotId,
                    readWorldName(dir, slotId),
                    readMapPath(new File(dir, "data.txt")),
                    readScreenshot(dir)
            ));
        }
        System.out.println("[WorldSelectUI] Found " + slots.size() + " slot(s).");
    }

    // ── File helpers ──────────────────────────────────────────────────────────

    private String readWorldName(File dir, int slotId) {
        File f = new File(dir, "world_name.txt");
        if (!f.exists()) return "Save " + slotId;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            return (line != null && !line.isBlank()) ? line.trim() : "Save " + slotId;
        } catch (IOException e) { return "Save " + slotId; }
    }

    private String readMapPath(File dataFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            br.readLine(); // [PLAYER]
            String line = br.readLine();
            if (line == null) return "Unknown";
            String[] parts = line.split(";");
            if (parts.length >= 6) return prettifyPath(parts[5]);
            return "Unknown";
        } catch (IOException e) { return "Unknown"; }
    }

    private String prettifyPath(String path) {
        if (path == null || path.isEmpty()) return "Unknown";
        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int last = p.lastIndexOf('/');
        return last >= 0 ? p.substring(last + 1) : p;
    }

    private BufferedImage readScreenshot(File dir) {
        File ss = new File(dir, "screenshot.png");
        if (!ss.exists()) return null;
        try { return javax.imageio.ImageIO.read(ss); }
        catch (IOException e) { return null; }
    }

    public void writeWorldName(int slotId, String name) {
        File dir = new File("src/res/Saves/" + slotId);
        if (!dir.exists()) return;
        try (FileWriter fw = new FileWriter(new File(dir, "world_name.txt"))) {
            fw.write(name.trim());
        } catch (IOException e) {
            System.err.println("[WorldSelectUI] Could not write world name: " + e.getMessage());
        }
    }

    private void deleteSlot(int slotId) {
        deleteRecursively(new File("src/res/Saves/" + slotId));
        System.out.println("[WorldSelectUI] Deleted slot " + slotId);
    }

    private void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] ch = f.listFiles();
            if (ch != null) for (File c : ch) deleteRecursively(c);
        }
        f.delete();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        // Forward typed characters every frame when text input is active
        if (subState == SubState.NAMING_NEW || subState == SubState.RENAMING) {
            char c = gp.KEYBOARDHANDLER.consumeTyped();
            if (c != 0) handleTyped(c);
        }

        switch (subState) {
            case BROWSING       -> updateBrowsing();
            case NAMING_NEW     -> updateNamingNew();
            case RENAMING       -> updateRenaming();
            case CONFIRM_DELETE -> updateConfirmDelete();
        }
    }

    // ── BROWSING ──────────────────────────────────────────────────────────────

    private void updateBrowsing() {
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.SPLASHSCREEN.open();
            gp.GAMESTATE = "splash";
            inputCooldown = INPUT_DELAY;
            return;
        }

        // UP / DOWN — scroll slots
        if (gp.KEYBOARDHANDLER.upPressed && slotCursor > 0) {
            slotCursor--;
            clampScroll();
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && slotCursor < slots.size() - 1) {
            slotCursor++;
            clampScroll();
            inputCooldown = INPUT_DELAY;
            return;
        }

        // LEFT / RIGHT — cycle action button
        if (gp.KEYBOARDHANDLER.leftPressed) {
            actionCursor  = (actionCursor - 1 + ACTION_LABELS.length) % ACTION_LABELS.length;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.rightPressed) {
            actionCursor  = (actionCursor + 1) % ACTION_LABELS.length;
            inputCooldown = INPUT_DELAY;
            return;
        }

        // ENTER — confirm selected action
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            switch (actionCursor) {
                case 0 -> playSelected();
                case 1 -> startNamingNew();   // ask for name BEFORE creating
                case 2 -> startRename();
                case 3 -> startDelete();
            }
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── NAMING_NEW ────────────────────────────────────────────────────────────

    private void updateNamingNew() {
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            confirmNewWorld();
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            subState      = SubState.BROWSING;
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── RENAMING ──────────────────────────────────────────────────────────────

    private void updateRenaming() {
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            confirmRename();
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            subState      = SubState.BROWSING;
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── CONFIRM_DELETE ────────────────────────────────────────────────────────

    private void updateConfirmDelete() {
        if (gp.KEYBOARDHANDLER.leftPressed || gp.KEYBOARDHANDLER.rightPressed) {
            gp.KEYBOARDHANDLER.leftPressed  = false;
            gp.KEYBOARDHANDLER.rightPressed = false;
            deleteChoice  = (deleteChoice == 0) ? 1 : 0;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            subState      = SubState.BROWSING;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (deleteChoice == 0 && !slots.isEmpty()) {
                deleteSlot(slots.get(slotCursor).slotId);
                refreshSlots();
                slotCursor = Math.min(slotCursor, Math.max(0, slots.size() - 1));
                clampScroll();
                statusMessage = "[ World deleted ]";
            }
            subState      = SubState.BROWSING;
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── Typed char handler ────────────────────────────────────────────────────

    private void handleTyped(char c) {
        if (c == '\b') {
            if (textBuffer.length() > 0)
                textBuffer.deleteCharAt(textBuffer.length() - 1);
        } else if (c >= 32 && c < 127 && textBuffer.length() < 28) {
            textBuffer.append(c);
        }
    }

    // ── Action handlers ───────────────────────────────────────────────────────

    private void playSelected() {
        if (slots.isEmpty()) { statusMessage = "[ No worlds found — create one first ]"; return; }
        SaveSlot sel = slots.get(slotCursor);
        utils.Constants.CURRENT_LOAD = sel.slotId;
        gp.player.reset();
        DataManager.loadData(gp, sel.slotId);
        gp.GAMESTATE = "play";
        System.out.println("[WorldSelectUI] Loaded slot " + sel.slotId);
    }

    /** Step 1: ask the player to type a world name. */
    private void startNamingNew() {
        textBuffer    = new StringBuilder("New World");
        subState      = SubState.NAMING_NEW;
        inputCooldown = INPUT_DELAY;
    }

    /**
     * Step 2: called when ENTER is pressed in NAMING_NEW.
     * Creates the world, writes the name, then transitions to the starter screen.
     * Spawn fix: teleport to (13, 68) AFTER DataManager.saveNewData() so the
     * screenshot inside saveNewData does not reset the coordinates afterward.
     */
    private void confirmNewWorld() {
        String name = textBuffer.toString().trim();
        if (name.isEmpty()) name = "New World";

        // Reset state for a fresh save
        gp.player.reset();
        QuestSystem.reset();

        // Set the correct spawn before saving so the screenshot captures Route 131
        gp.world.loadMap(utils.Directories.ROUTE131.getPath(), true);
        gp.CURRENT_PATH = utils.Directories.ROUTE131.getPath();
        gp.player.teleport(new int[]{ 13, 68 });

        // Now save — the screenshot will capture the player at (13, 68)
        DataManager.saveNewData(gp);

        // Refresh list and point cursor at the newest slot
        refreshSlots();
        if (!slots.isEmpty()) {
            slotCursor = slots.size() - 1;
            clampScroll();
            SaveSlot newest = slots.get(slotCursor);
            writeWorldName(newest.slotId, name);
            newest.worldName = name;
        }

        subState      = SubState.BROWSING;
        statusMessage = "[ \"" + name + "\" created ]";

        // Go to the starter screen
        gp.GAMESTATE = "starter";
        System.out.println("[WorldSelectUI] New world \"" + name + "\" — spawned at (13, 68).");
    }

    private void startRename() {
        if (slots.isEmpty()) { statusMessage = "[ No world to rename ]"; return; }
        textBuffer    = new StringBuilder(slots.get(slotCursor).worldName);
        subState      = SubState.RENAMING;
        inputCooldown = INPUT_DELAY;
    }

    private void confirmRename() {
        if (!slots.isEmpty()) {
            SaveSlot sel   = slots.get(slotCursor);
            String newName = textBuffer.toString().trim();
            if (newName.isEmpty()) newName = "Save " + sel.slotId;
            sel.worldName  = newName;
            writeWorldName(sel.slotId, newName);
            statusMessage  = "[ Renamed to: " + newName + " ]";
        }
        subState = SubState.BROWSING;
    }

    private void startDelete() {
        if (slots.isEmpty()) { statusMessage = "[ No world to delete ]"; return; }
        deleteChoice  = 1; // default NO
        subState      = SubState.CONFIRM_DELETE;
        inputCooldown = INPUT_DELAY;
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    private void clampScroll() {
        if (slotCursor < scrollOffset) scrollOffset = slotCursor;
        if (slotCursor >= scrollOffset + VISIBLE_ROWS)
            scrollOffset = slotCursor - VISIBLE_ROWS + 1;
        if (scrollOffset < 0) scrollOffset = 0;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DRAW
    // ══════════════════════════════════════════════════════════════════════════

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        g2.setColor(new Color(20, 18, 14));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE,     winY = TILE_SIZE / 2;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE;
        drawWindow(g2, winX, winY, winW, winH);

        drawTitleBar     (g2, base, winX, winY, winW);

        int bodyY      = winY + 52;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;
        int btnBarY    = statusBarY - 54;

        drawSlotList     (g2, base, winX, winW, bodyY, btnBarY);
        drawActionButtons(g2, base, winX, winW, btnBarY);
        drawStatusBar    (g2, base, winX, winY, winW, statusBarY);

        // Overlays
        if (subState == SubState.NAMING_NEW)      drawTextInputOverlay(g2, base, "NAME YOUR WORLD",
                "Enter a name for the new world:");
        if (subState == SubState.RENAMING)        drawTextInputOverlay(g2, base, "RENAME WORLD",
                "Enter a new name:");
        if (subState == SubState.CONFIRM_DELETE)  drawDeleteOverlay   (g2, base);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────

    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("SELECT WORLD", winX + 28, winY + 32);

        String counter = slots.size() + " world" + (slots.size() == 1 ? "" : "s");
        g2.setFont(base.deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(216, 184, 88));
        g2.drawString(counter, winX + winW - 16 - fm.stringWidth(counter), winY + 32);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── Slot list ─────────────────────────────────────────────────────────────

    private void drawSlotList(Graphics2D g2, Font base,
                              int winX, int winW, int bodyY, int btnBarY) {
        int listX = winX + 14;
        int listW = winW - 28;
        int areaH = btnBarY - bodyY - 8;

        Shape prev = g2.getClip();
        g2.setClip(listX, bodyY, listW, areaH);

        if (slots.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(140, 136, 128));
            String msg = "No worlds yet. Select 'NEW WORLD' to get started!";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg,
                    listX + (listW - fm.stringWidth(msg)) / 2,
                    bodyY + areaH / 2);
            g2.setClip(prev);
            return;
        }

        int endIdx = Math.min(scrollOffset + VISIBLE_ROWS, slots.size());
        for (int i = scrollOffset; i < endIdx; i++) {
            SaveSlot slot    = slots.get(i);
            int      rowTop  = bodyY + (i - scrollOffset) * ROW_H;
            boolean  hovered = (i == slotCursor);

            Color rowBg = hovered ? new Color(178, 212, 244, 180) : new Color(230, 226, 218);
            g2.setColor(rowBg);
            g2.fillRoundRect(listX, rowTop + 2, listW, ROW_H - 4, 8, 8);
            if (hovered) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX, rowTop + 2, listW, ROW_H - 4, 8, 8);
                g2.setStroke(new BasicStroke(1));
            }

            // Thumbnail
            int thumbX = listX + 8;
            int thumbY = rowTop + (ROW_H - THUMB_H) / 2;
            g2.setColor(new Color(60, 58, 54));
            g2.fillRoundRect(thumbX, thumbY, THUMB_W, THUMB_H, 4, 4);
            if (slot.screenshot != null)
                g2.drawImage(slot.screenshot, thumbX + 1, thumbY + 1, THUMB_W - 2, THUMB_H - 2, null);
            else {
                g2.setFont(base.deriveFont(7f));
                g2.setColor(new Color(100, 96, 90));
                g2.drawString("No Preview", thumbX + 12, thumbY + THUMB_H / 2 + 3);
            }

            // Text
            int tx = thumbX + THUMB_W + 14;
            int ty = rowTop + 26;
            g2.setFont(base.deriveFont(Font.BOLD, 12f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(truncate(slot.worldName, g2.getFontMetrics(), listW - (tx - listX) - 20), tx, ty);

            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(100, 96, 90));
            g2.drawString("Location: " + slot.mapPath, tx, ty + 18);

            // Slot badge
            g2.setFont(base.deriveFont(8f));
            FontMetrics bf = g2.getFontMetrics();
            String badge   = "Slot " + slot.slotId;
            int bw         = bf.stringWidth(badge) + 10;
            int bx         = listX + listW - bw - 10;
            int by         = rowTop + ROW_H - 22;
            g2.setColor(new Color(180, 175, 165));
            g2.fillRoundRect(bx, by, bw, 14, 4, 4);
            g2.setColor(new Color(80, 76, 70));
            g2.drawString(badge, bx + 5, by + 11);
        }
        g2.setClip(prev);

        // Scroll hints
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(140, 136, 128));
        if (scrollOffset > 0)
            g2.drawString("^ more", winX + winW / 2 - 16, bodyY - 2);
        if (scrollOffset + VISIBLE_ROWS < slots.size())
            g2.drawString("v more", winX + winW / 2 - 16, btnBarY - 4);
    }

    // ── Action buttons ─────────────────────────────────────────────────────────

    private void drawActionButtons(Graphics2D g2, Font base, int winX, int winW, int btnBarY) {
        int totalW = winW - 28;
        int gap    = 8;
        int btnW   = (totalW - gap * (ACTION_LABELS.length - 1)) / ACTION_LABELS.length;
        int btnH   = 36;
        int startX = winX + 14;

        for (int i = 0; i < ACTION_LABELS.length; i++) {
            int bx      = startX + i * (btnW + gap);
            int by      = btnBarY + 6;
            boolean sel = (i == actionCursor);
            boolean dim = (i >= 2 && slots.isEmpty()); // dim Rename/Delete if no slots

            // Single gray palette for all buttons
            Color bg = dim ? new Color(55, 53, 50)
                    : sel  ? ACTION_SEL
                      :        ACTION_BASE;
            g2.setColor(bg);
            g2.fillRoundRect(bx, by, btnW, btnH, 8, 8);

            // White border on selected
            if (sel && !dim) {
                g2.setColor(new Color(210, 206, 198));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(bx, by, btnW, btnH, 8, 8);
                g2.setStroke(new BasicStroke(1));
            }

            g2.setFont(base.deriveFont(Font.BOLD, 10f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(dim ? new Color(80, 76, 70) : sel ? new Color(241, 239, 232) : new Color(190, 186, 178));
            String lbl = ACTION_LABELS[i];
            g2.drawString(lbl,
                    bx + (btnW - fm.stringWidth(lbl)) / 2,
                    by + (btnH - fm.getHeight()) / 2 + fm.getAscent());
        }
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g2, Font base,
                               int winX, int winY, int winW, int statusBarY) {
        int barX = winX + 8, barW = winW - 16;
        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, statusBarY, barW, STATUS_BAR_H, 5, 5);

        String hint = "UP/DOWN Scroll   LEFT/RIGHT Action   ENTER Confirm   ESC Back";
        g2.setFont(base.deriveFont(7f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, barX + barW - 12 - fm.stringWidth(hint), statusBarY + 25);

        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(statusMessage, barX + 14, statusBarY + 25);
        }
    }

    // ── Shared text-input overlay (used for both NAMING_NEW and RENAMING) ─────

    private void drawTextInputOverlay(Graphics2D g2, Font base, String title, String prompt) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int panelW = 440, panelH = 160;
        int panelX = (SCREEN_WIDTH  - panelW) / 2;
        int panelY = (SCREEN_HEIGHT - panelH) / 2;
        drawWindow(g2, panelX, panelY, panelW, panelH);

        // Title bar
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(panelX + 8, panelY + 8, panelW - 16, 30, 8, 8);
        g2.setFont(base.deriveFont(Font.BOLD, 12f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString(title, panelX + 20, panelY + 29);

        // Prompt label
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(100, 96, 90));
        g2.drawString(prompt, panelX + 20, panelY + 54);

        // Input box
        int boxX = panelX + 18, boxY = panelY + 62, boxW = panelW - 36, boxH = 34;
        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g2.setColor(new Color(24, 95, 165));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(base.deriveFont(13f));
        g2.setColor(new Color(44, 44, 42));
        boolean caret = (System.currentTimeMillis() / 500) % 2 == 0;
        g2.drawString(textBuffer.toString() + (caret ? "|" : " "), boxX + 10, boxY + 23);

        // Hint
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("Type name   ENTER confirm   ESC cancel", panelX + 20, panelY + 144);
    }

    // ── Delete confirm overlay ────────────────────────────────────────────────

    private void drawDeleteOverlay(Graphics2D g2, Font base) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int panelW = 400, panelH = 170;
        int panelX = (SCREEN_WIDTH  - panelW) / 2;
        int panelY = (SCREEN_HEIGHT - panelH) / 2;
        drawWindow(g2, panelX, panelY, panelW, panelH);

        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(panelX + 8, panelY + 8, panelW - 16, 30, 8, 8);
        g2.setFont(base.deriveFont(Font.BOLD, 12f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("DELETE WORLD", panelX + 20, panelY + 29);

        String slotName = slots.isEmpty() ? "?" : slots.get(slotCursor).worldName;
        g2.setFont(base.deriveFont(11f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Delete \"" + truncate(slotName, g2.getFontMetrics(), panelW - 80) + "\"?",
                panelX + 20, panelY + 62);
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(160, 60, 60));
        g2.drawString("This cannot be undone.", panelX + 20, panelY + 80);

        int btnW = 130, btnH = 34;
        int yesX = panelX + panelW / 2 - btnW - 8;
        int noX  = panelX + panelW / 2 + 8;
        int btnY = panelY + 96;

        // YES
        g2.setColor(deleteChoice == 0 ? new Color(190, 60, 60) : ACTION_BASE);
        g2.fillRoundRect(yesX, btnY, btnW, btnH, 6, 6);
        if (deleteChoice == 0) {
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(yesX, btnY, btnW, btnH, 6, 6);
            g2.setStroke(new BasicStroke(1));
        }
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString("YES", yesX + (btnW - fm.stringWidth("YES")) / 2,
                btnY + (btnH + fm.getAscent()) / 2 - 2);

        // NO
        g2.setColor(deleteChoice == 1 ? ACTION_SEL : ACTION_BASE);
        g2.fillRoundRect(noX, btnY, btnW, btnH, 6, 6);
        if (deleteChoice == 1) {
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(noX, btnY, btnW, btnH, 6, 6);
            g2.setStroke(new BasicStroke(1));
        }
        g2.setColor(Color.WHITE);
        g2.drawString("NO", noX + (btnW - fm.stringWidth("NO")) / 2,
                btnY + (btnH + fm.getAscent()) / 2 - 2);

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("LEFT/RIGHT switch   ENTER confirm   ESC cancel",
                panelX + 20, panelY + 154);
    }

    // ── Window chrome ─────────────────────────────────────────────────────────

    private void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 16;
        g2.setColor(new Color(245, 242, 235));  g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(6));  g2.setColor(new Color(80, 80, 80));   g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(4));  g2.setColor(new Color(216, 184, 88)); g2.drawRoundRect(x+1, y+1, w-2, h-2, arc, arc);
        g2.setStroke(new BasicStroke(2));  g2.setColor(new Color(80, 80, 80));   g2.drawRoundRect(x+4, y+4, w-8, h-8, arc-4, arc-4);
        g2.setStroke(new BasicStroke(1));
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "...") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }

    public SubState getSubState() { return subState; }
    public boolean  isRenaming()  { return subState == SubState.RENAMING || subState == SubState.NAMING_NEW; }
}