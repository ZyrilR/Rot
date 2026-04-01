package ui;

import brainrots.BrainRot;
import engine.GamePanel;
import items.*;
import skills.Skill;
import skills.SkillPool;
import storage.PCSystem;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static utils.Constants.*;

/**
 * InventoryUI — full-featured backpack overlay.
 *
 * Tabs:   ITEMS (Stews, Antidotes, Scrolls)  |  CAPSULES
 * Layout: left = item image + description card  |  right = item list
 *
 * State machine:
 *   ITEM_LIST    — browsing items in the selected tab
 *   PARTY_SELECT — choosing a party member to use the item on
 *   MOVE_SWAP    — scroll: choose which of 4 moves to replace (MOVESET_FULL)
 *
 * Controls:
 *   TAB          — switch tabs (ITEM_LIST only)
 *   W/S          — move cursor
 *   ENTER        — confirm
 *   ESC          — cancel / go back one level (always returns to bag, never past it)
 */
public class InventoryUI {

    // ── Enums ─────────────────────────────────────────────────────────────────

    private enum Tab    { ITEMS, CAPSULES }
    private enum Layout { ITEM_LIST, PARTY_SELECT, MOVE_SWAP }

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final int INPUT_DELAY     = 10;
    private static final int STATUS_TICKS    = 90; // ~3 s @ 30 FPS
    private static final int LEFT_SPLIT      = 38; // % of window width for left panel

    // ── Injected refs ─────────────────────────────────────────────────────────

    private final GamePanel gp;

    // ── UI State ──────────────────────────────────────────────────────────────

    private Tab    activeTab    = Tab.ITEMS;
    private Layout layout       = Layout.ITEM_LIST;

    /** Cursor in the item list */
    private int itemCursor  = 0;
    /** Cursor in the party list */
    private int partyCursor = 0;
    /** Cursor among the 4 move slots during MOVE_SWAP */
    private int moveCursor  = 0;

    private int inputCooldown = 0;

    private String statusMessage = "";
    private int    statusTimer   = 0;

    /** The item currently being applied (set when entering PARTY_SELECT) */
    private Item   pendingItem = null;
    /** The party member chosen for a scroll (set when entering MOVE_SWAP) */
    private BrainRot pendingScrollTarget = null;

    /** Image cache: assetPath → image */
    private final Map<String, BufferedImage> imgCache = new HashMap<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public InventoryUI(GamePanel gp) {
        this.gp = gp;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Open from overworld / menu — always starts on ITEMS tab. */
    public void open() {
        activeTab     = Tab.ITEMS;
        layout        = Layout.ITEM_LIST;
        itemCursor    = 0;
        partyCursor   = 0;
        moveCursor    = 0;
        pendingItem   = null;
        pendingScrollTarget = null;
        statusMessage = "";
        statusTimer   = 0;
        inputCooldown = INPUT_DELAY * 2; // absorb the ENTER/ESC that opened us
        System.out.println("[InventoryUI] Opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }
        if (statusTimer   > 0) { statusTimer--;   if (statusTimer == 0) statusMessage = ""; }

        switch (layout) {
            case ITEM_LIST    -> updateItemList();
            case PARTY_SELECT -> updatePartySelect();
            case MOVE_SWAP    -> updateMoveSwap();
        }
    }

    // ── ITEM_LIST input ───────────────────────────────────────────────────────

    private void updateItemList() {
        // TAB — switch tabs
        if (gp.KEYBOARDHANDLER.tabPressed) {
            gp.KEYBOARDHANDLER.tabPressed = false;
            activeTab  = (activeTab == Tab.ITEMS) ? Tab.CAPSULES : Tab.ITEMS;
            itemCursor = 0;
            clearStatus();
            inputCooldown = INPUT_DELAY;
            return;
        }

        // ESC — close
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            close();
            return;
        }

        List<Item> list = currentList();

        // W / S — move cursor
        if (gp.KEYBOARDHANDLER.upPressed && itemCursor > 0) {
            itemCursor--; inputCooldown = INPUT_DELAY; return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && itemCursor < list.size() - 1) {
            itemCursor++; inputCooldown = INPUT_DELAY; return;
        }

        // ENTER — use item
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (list.isEmpty()) { setStatus("Your bag is empty!"); inputCooldown = INPUT_DELAY; return; }
            Item item = list.get(itemCursor);
            handleItemUse(item);
            inputCooldown = INPUT_DELAY;
        }
    }

    private void handleItemUse(Item item) {
        if (item instanceof Capsule) {
            // Capsules only usable in battle
            setStatus("Can't use capsules outside of battle!");
            return;
        }

        // All other items need a party target
        if (gp.PCSYSTEM.getPartySize() == 0) {
            setStatus("No BrainRots in your party!");
            return;
        }

        pendingItem = item;
        partyCursor = 0;
        layout      = Layout.PARTY_SELECT;
        clearStatus();
    }

    // ── PARTY_SELECT input ────────────────────────────────────────────────────

    private void updatePartySelect() {
        int partySize = gp.PCSYSTEM.getPartySize();

        // ESC — cancel back to item list immediately
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            cancelToItemList("Cancelled.");
            return;
        }

        // W / S — move cursor
        if (gp.KEYBOARDHANDLER.upPressed && partyCursor > 0) {
            partyCursor--; inputCooldown = INPUT_DELAY; return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && partyCursor < partySize - 1) {
            partyCursor++; inputCooldown = INPUT_DELAY; return;
        }

        // ENTER — confirm
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            BrainRot target = gp.PCSYSTEM.getPartyMember(partyCursor);
            if (target == null) { inputCooldown = INPUT_DELAY; return; }
            applyItemToTarget(target);
            inputCooldown = INPUT_DELAY;
        }
    }

    /**
     * Dispatches item use based on type, enforcing pre-conditions.
     * On success, removes the item from backpack.
     */
    private void applyItemToTarget(BrainRot target) {
        if (pendingItem instanceof Stew stew) {
            applyStew(stew, target);
        } else if (pendingItem instanceof Antidote antidote) {
            applyAntidote(antidote, target);
        } else if (pendingItem instanceof Scroll scroll) {
            applyScroll(scroll, target);
        }
    }

    // ── Stew ──────────────────────────────────────────────────────────────────

    private void applyStew(Stew stew, BrainRot target) {
        if (target.getCurrentHp() >= target.getMaxHp()) {
            setStatus(target.getName() + "'s HP is already full!");
            // Stay in PARTY_SELECT so player can pick another
            return;
        }
        int before = target.getCurrentHp();
        stew.use(target);
        int healed = target.getCurrentHp() - before;
        consumePendingItem();
        setStatus(target.getName() + " recovered " + healed + " HP!");
        returnToItemList();
    }

    // ── Antidote ──────────────────────────────────────────────────────────────

    private void applyAntidote(Antidote antidote, BrainRot target) {
        String cure = antidote.getStatusToCure().toUpperCase();

        if (cure.equals("DEBUFF")) {
            // Check if any modifier is not neutral
            boolean hasDebuff = target.getAttack()  < (int)(getBaseAtk(target))
                    || target.getDefense() < (int)(getBaseDef(target))
                    || target.getSpeed()   < (int)(getBaseSpd(target));
            // Simpler check: if all effective stats equal base we skip
            // We'll rely on resetModifiers being a no-op check via BrainRot
            // Use the item and report based on outcome
            antidote.use(target);
            consumePendingItem();
            setStatus(target.getName() + "'s stat debuffs cleared!");
            returnToItemList();
            return;
        }

        if (!target.hasStatus(cure)) {
            String readable = capitalize(cure.replace("_", " ").toLowerCase());
            setStatus(target.getName() + " isn't " + readable + "!");
            return; // Stay in PARTY_SELECT
        }

        antidote.use(target);
        consumePendingItem();
        setStatus(target.getName() + " was cured of " + capitalize(cure.toLowerCase()) + "!");
        returnToItemList();
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    private void applyScroll(Scroll scroll, BrainRot target) {
        Scroll.ScrollResult result = scroll.validate(target);

        switch (result) {
            case SUCCESS -> {
                scroll.apply(target, null);
                consumePendingItem();
                setStatus(target.getName() + " learned " + scroll.getSkillName() + "!");
                returnToItemList();
            }
            case MOVESET_FULL -> {
                // Enter MOVE_SWAP — let player pick which move to replace
                pendingScrollTarget = target;
                moveCursor = 0;
                layout     = Layout.MOVE_SWAP;
                clearStatus();
            }
            case TYPE_MISMATCH -> {
                setStatus(target.getName() + " can't learn this move!");
            }
            case SIGNATURE_MISMATCH -> {
                // Find the owner name for better feedback
                String owner = findSignatureOwner(scroll.getSkillName());
                setStatus("Only " + (owner != null ? owner : "its owner") + " can learn this!");
            }
            case ALREADY_KNOWN -> {
                setStatus(target.getName() + " already knows " + scroll.getSkillName() + "!");
            }
            case SKILL_NOT_FOUND -> {
                setStatus("Error: skill not found in registry.");
            }
            default -> setStatus("Cannot use this scroll here.");
        }
        inputCooldown = INPUT_DELAY;
    }

    // ── MOVE_SWAP input ───────────────────────────────────────────────────────

    private void updateMoveSwap() {
        if (pendingScrollTarget == null) { cancelToItemList("Cancelled."); return; }
        int moveCount = pendingScrollTarget.getMoves().size();

        // ESC — cancel scroll, return to item list immediately
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            pendingScrollTarget = null;
            cancelToItemList("Scroll cancelled.");
            return;
        }

        // W / S — navigate moves
        if (gp.KEYBOARDHANDLER.upPressed && moveCursor > 0) {
            moveCursor--; inputCooldown = INPUT_DELAY; return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && moveCursor < moveCount - 1) {
            moveCursor++; inputCooldown = INPUT_DELAY; return;
        }

        // ENTER — confirm swap
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (pendingItem instanceof Scroll scroll) {
                Scroll.ScrollResult result = scroll.apply(pendingScrollTarget, moveCursor);
                if (result == Scroll.ScrollResult.SWAPPED) {
                    consumePendingItem();
                    setStatus(pendingScrollTarget.getName() + " learned " + scroll.getSkillName() + "!");
                } else {
                    setStatus("Could not swap move (index invalid).");
                }
            }
            pendingScrollTarget = null;
            returnToItemList();
            inputCooldown = INPUT_DELAY;
        }
    }

    // ── State helpers ─────────────────────────────────────────────────────────

    /** Return to ITEM_LIST, keeping the status message visible. */
    private void returnToItemList() {
        layout      = Layout.ITEM_LIST;
        pendingItem = null;
        // Clamp cursor to remaining list
        int size = currentList().size();
        if (itemCursor >= size && size > 0) itemCursor = size - 1;
    }

    /** Cancel back to ITEM_LIST with an explicit message. */
    private void cancelToItemList(String msg) {
        pendingItem         = null;
        pendingScrollTarget = null;
        layout              = Layout.ITEM_LIST;
        if (msg != null && !msg.isEmpty()) setStatus(msg);
        inputCooldown = INPUT_DELAY;
    }

    private void close() {
        gp.GAMESTATE = "menu";  // go back to the pause menu that opened us
        gp.MENUUI.open();
        System.out.println("[InventoryUI] Closed → back to menu.");
    }

    /** Remove the pending item from the player's backpack. */
    private void consumePendingItem() {
        if (pendingItem == null) return;
        gp.player.getInventory().removeItem(pendingItem);
        pendingItem = null;
    }

    // ── Current list ──────────────────────────────────────────────────────────

    /**
     * Builds the visible item list for the active tab.
     * De-duplicates by returning one entry per unique item name
     * (quantity is computed separately for display).
     */
    private List<Item> currentList() {
        List<Item> all = gp.player.getInventory().getRawItems();
        List<Item> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Item item : all) {
            boolean isCapsule = (item instanceof Capsule);
            if (activeTab == Tab.CAPSULES && isCapsule && seen.add(item.getName())) out.add(item);
            if (activeTab == Tab.ITEMS    && !isCapsule && seen.add(item.getName())) out.add(item);
        }
        return out;
    }

    /** Count how many of a given item name are in the backpack. */
    private int countOf(String name) {
        int c = 0;
        for (Item item : gp.player.getInventory().getRawItems())
            if (item.getName().equalsIgnoreCase(name)) c++;
        return c;
    }

    // ── DRAW ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        // Dim overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Window bounds
        int winX = TILE_SIZE, winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;

        drawWindow(g2, winX, winY, winW, winH);
        drawTitleBar(g2, base, winX, winY, winW);

        int bodyY = winY + 52; // below title bar + gold divider

        // Split panels
        int leftW  = (int)(winW * LEFT_SPLIT / 100.0);
        int divX   = winX + leftW;

        // Vertical divider
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, winY + winH - STATUS_BAR_H - 10);

        // Panels
        drawLeftPanel (g2, base, winX, winY, winW, winH, bodyY, leftW);
        drawRightPanel(g2, base, winX, winY, winW, winH, bodyY, divX);

        // Overlays on top of base layout
        if (layout == Layout.PARTY_SELECT) drawPartySelectOverlay(g2, base, winX, winY, winW, winH);
        if (layout == Layout.MOVE_SWAP)    drawMoveSwapOverlay   (g2, base, winX, winY, winW, winH);

        drawStatusBar(g2, base, winX, winY, winW, winH);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────

    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("BACKPACK", winX + 28, winY + 32);

        // Tab pill buttons — right-aligned in the title bar
        String[]   labels = { "ITEMS", "CAPSULES" };
        Tab[]      tabs   = { Tab.ITEMS, Tab.CAPSULES };

        g2.setFont(base.deriveFont(Font.BOLD, 10f));
        FontMetrics fm = g2.getFontMetrics();
        int btnH = 22, gap = 4, right = winX + winW - 16;

        for (int i = labels.length - 1; i >= 0; i--) {
            int btnW = fm.stringWidth(labels[i]) + 16;
            int btnX = right - btnW, btnY = winY + 14;
            boolean active = (activeTab == tabs[i]);
            g2.setColor(active ? new Color(216, 184, 88) : new Color(80, 78, 72));
            g2.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g2.setColor(active ? new Color(44, 44, 42) : new Color(200, 196, 185));
            g2.drawString(labels[i], btnX + (btnW - fm.stringWidth(labels[i])) / 2,
                    btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent());
            right = btnX - gap;
        }

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── Left panel — image + description ──────────────────────────────────────

    private void drawLeftPanel(Graphics2D g2, Font base,
                               int winX, int winY, int winW, int winH,
                               int bodyY, int leftW) {
        List<Item> list = currentList();
        Item sel = (list.isEmpty() || itemCursor >= list.size()) ? null : list.get(itemCursor);

        int panelX = winX + OUTER_PAD;
        int panelW = leftW - OUTER_PAD - 8;

        // ── Image card ────────────────────────────────────────────────────────
        int imgCardY = bodyY + 8;
        int imgCardH = 160;
        drawCard(g2, panelX, imgCardY, panelW, imgCardH, 8);

        if (sel != null) {
            BufferedImage img = loadItemImage(sel);
            if (img != null) {
                int margin = 12;
                g2.drawImage(img, panelX + margin, imgCardY + margin,
                        panelW - margin * 2, imgCardH - margin * 2, null);
            } else {
                drawCentredText(g2, base, panelX, imgCardY, panelW, imgCardH, "[img]");
            }
        } else {
            drawCentredText(g2, base, panelX, imgCardY, panelW, imgCardH, "—");
        }

        // ── Description card ──────────────────────────────────────────────────
        int descCardY = imgCardY + imgCardH + 6;
        int descCardH = winH - (descCardY - winY) - STATUS_BAR_H - 16;
        drawCard(g2, panelX, descCardY, panelW, descCardH, 8);

        int tx = panelX + 10;
        int ty = descCardY + 18;

        if (sel != null) {
            // Item name — bold
            g2.setFont(base.deriveFont(Font.BOLD, 11f));
            g2.setColor(new Color(44, 44, 42));
            ty = drawWrappedText(g2, base.deriveFont(Font.BOLD, 11f),
                    sel.getName(), tx, ty, panelW - 20, 16, new Color(44, 44, 42));
            ty += 6;

            // Description — wrapped
            drawWrappedText(g2, base.deriveFont(9f),
                    sel.getDescription(), tx, ty, panelW - 20, 14, new Color(80, 76, 70));
        } else {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(140, 136, 128));
            g2.drawString("Select an item.", tx, ty + 10);
        }
    }

    // ── Right panel — item list ───────────────────────────────────────────────

    private void drawRightPanel(Graphics2D g2, Font base,
                                int winX, int winY, int winW, int winH,
                                int bodyY, int divX) {
        List<Item> list = currentList();

        int listX = divX + OUTER_PAD;
        int listW = winX + winW - listX - OUTER_PAD;
        int listY = bodyY + 8;

        // Section label
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        String tabLabel = (activeTab == Tab.ITEMS) ? "ITEMS" : "CAPSULES";
        g2.drawString(tabLabel, listX, listY + 12);

        if (list.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(150, 145, 138));
            g2.drawString("Nothing here.", listX, listY + 36);
            return;
        }

        int rowH       = 30;
        int firstRowY  = listY + 24;

        for (int i = 0; i < list.size(); i++) {
            Item item = list.get(i);
            int  rowY = firstRowY + i * rowH;
            boolean hovered = (i == itemCursor);

            if (hovered) {
                // Highlight background
                g2.setColor(new Color(178, 212, 244, 200));
                g2.fillRoundRect(listX - 4, rowY - 16, listW + 4, rowH, 5, 5);
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX - 4, rowY - 16, listW + 4, rowH, 5, 5);
                g2.setStroke(new BasicStroke(1));

                // Cursor triangle (same style as MenuUI)
                int ts = 7, tx2 = listX, cy = rowY - 16 + rowH / 2;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(
                        new int[]{ tx2,      tx2,      tx2 + ts },
                        new int[]{ cy - ts,  cy + ts,  cy       }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            // Item name
            g2.setFont(base.deriveFont(hovered ? Font.BOLD : Font.PLAIN, 11f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(item.getName(), listX + 14, rowY);

            // Quantity — right-aligned
            String qty     = "x " + countOf(item.getName());
            g2.setFont(base.deriveFont(10f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(80, 78, 72));
            g2.drawString(qty, listX + listW - fm.stringWidth(qty), rowY);
        }
    }

    // ── PARTY SELECT overlay ──────────────────────────────────────────────────

    private void drawPartySelectOverlay(Graphics2D g2, Font base,
                                        int winX, int winY, int winW, int winH) {
        // Semi-transparent dark overlay on the window
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(winX, winY, winW, winH, 16, 16);

        // Party panel — centred
        int panelW = 400, panelH = 320;
        int panelX = winX + (winW - panelW) / 2;
        int panelY = winY + (winH - panelH) / 2;
        drawCard(g2, panelX, panelY, panelW, panelH, 12);

        // Title
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        g2.drawString("Choose a BrainRot", panelX + 14, panelY + 22);

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(panelX + 10, panelY + 28, panelX + panelW - 10, panelY + 28);

        int rowH  = 42;
        int listY = panelY + 36;
        int partySize = gp.PCSYSTEM.getPartySize();

        for (int i = 0; i < partySize; i++) {
            BrainRot rot   = gp.PCSYSTEM.getPartyMember(i);
            boolean hovered = (i == partyCursor);
            boolean greyed  = isGreyedOut(rot);

            int rowY = listY + i * rowH;

            // Row background
            Color bg = hovered ? (greyed ? new Color(220, 210, 210) : new Color(178, 212, 244, 220))
                    : new Color(235, 231, 223);
            g2.setColor(bg);
            g2.fillRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);

            if (hovered && !greyed) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);
                g2.setStroke(new BasicStroke(1));

                // Cursor triangle
                int ts = 6, tx2 = panelX + 12, cy = rowY + (rowH - 4) / 2;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ tx2, tx2, tx2 + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            int textX = panelX + 26;

            // Name
            g2.setFont(base.deriveFont(Font.BOLD, 11f));
            g2.setColor(greyed ? new Color(160, 155, 148) : new Color(44, 44, 42));
            g2.drawString(rot.getName(), textX, rowY + 16);

            // HP  / status
            String info = "HP: " + rot.getCurrentHp() + "/" + rot.getMaxHp()
                    + (rot.getStatus().equalsIgnoreCase("NONE") ? "" : "  [" + rot.getStatus() + "]");
            g2.setFont(base.deriveFont(9f));
            g2.setColor(greyed ? new Color(170, 165, 158) : new Color(88, 84, 76));
            g2.drawString(info, textX, rowY + 30);

            // Grey reason hint on hovered greyed row
            if (hovered && greyed) {
                String hint = greyReason(rot);
                g2.setFont(base.deriveFont(9f));
                g2.setColor(new Color(180, 80, 60));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, panelX + panelW - fm.stringWidth(hint) - 12, rowY + 22);
            }
        }

        // ESC hint
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("ESC Cancel", panelX + 14, panelY + panelH - 10);
    }

    // ── MOVE SWAP overlay ─────────────────────────────────────────────────────

    private void drawMoveSwapOverlay(Graphics2D g2, Font base,
                                     int winX, int winY, int winW, int winH) {
        // Semi-transparent overlay
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(winX, winY, winW, winH, 16, 16);

        int panelW = 420, panelH = 260;
        int panelX = winX + (winW - panelW) / 2;
        int panelY = winY + (winH - panelH) / 2;
        drawCard(g2, panelX, panelY, panelW, panelH, 12);

        // Title
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(44, 44, 42));
        String title = "Replace which move?";
        if (pendingItem instanceof Scroll s)
            title = "Learning: " + s.getSkillName() + " — Replace?";
        g2.drawString(title, panelX + 14, panelY + 22);

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(panelX + 10, panelY + 28, panelX + panelW - 10, panelY + 28);

        if (pendingScrollTarget == null) return;

        List<Skill> moves = pendingScrollTarget.getMoves();
        int rowH  = 44;
        int listY = panelY + 36;
        int badgeH = 16, padX = 5;

        for (int i = 0; i < moves.size(); i++) {
            Skill mv     = moves.get(i);
            boolean hov  = (i == moveCursor);
            int rowY     = listY + i * rowH;

            Color bg = hov ? new Color(178, 212, 244, 220) : new Color(235, 231, 223);
            g2.setColor(bg);
            g2.fillRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);

            if (hov) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(panelX + 8, rowY, panelW - 16, rowH - 4, 6, 6);
                g2.setStroke(new BasicStroke(1));

                int ts = 6, tx2 = panelX + 12, cy = rowY + (rowH - 4) / 2;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ tx2, tx2, tx2 + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            // Type badge
            g2.setFont(base.deriveFont(8f));
            FontMetrics fm = g2.getFontMetrics();
            int badgeW = fm.stringWidth(mv.getType().name()) + padX * 2;
            int badgeX = panelX + 24;
            int badgeTopY = rowY + (rowH - 4 - badgeH) / 2;
            g2.setColor(typeColor(mv.getType().name()));
            g2.fillRoundRect(badgeX, badgeTopY, badgeW, badgeH, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(mv.getType().name(), badgeX + padX, badgeTopY + 11);

            // Move name
            g2.setFont(base.deriveFont(hov ? Font.BOLD : Font.PLAIN, 11f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(mv.getName(), panelX + 24 + badgeW + 8, rowY + rowH / 2 + 5);

            // SP cost
            String sp = "SP " + mv.getSpCost();
            g2.setFont(base.deriveFont(9f));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(100, 96, 90));
            g2.drawString(sp, panelX + panelW - fm.stringWidth(sp) - 16, rowY + rowH / 2 + 5);
        }

        // Hints
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("ENT Replace   ESC Cancel", panelX + 14, panelY + panelH - 10);
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g2, Font base, int winX, int winY, int winW, int winH) {
        int barH = STATUS_BAR_H, barY = winY + winH - barH - 8;
        int barX = winX + 8,    barW = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, barY, barW, barH, 5, 5);

        // Status message — left
        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(statusMessage, barX + 12, barY + (barH - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent());
        }

        // Controls — right
        String hint = switch (layout) {
            case ITEM_LIST    -> "WS Move  TAB Tab ";
            case PARTY_SELECT -> "WS Move  ENT Confirm  ESC Cancel";
            case MOVE_SWAP    -> "WS Move  ENT Replace  ESC Cancel";
        };

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, barX + barW - 12 - fm.stringWidth(hint), barY + 18);
        g2.setFont(base.deriveFont(8f));
        fm = g2.getFontMetrics();
        String hint2 = (layout == Layout.ITEM_LIST) ? "ENT Use  ESC Close" : "";
        if (!hint2.isEmpty())
            g2.drawString(hint2, barX + barW - 12 - fm.stringWidth(hint2), barY + 30);
    }

    // ── Grey-out logic ────────────────────────────────────────────────────────

    /**
     * Returns true when the pending item cannot be used on this BrainRot.
     * Greyed-out rows are still navigable but ENTER produces feedback instead of using.
     */
    private boolean isGreyedOut(BrainRot rot) {
        if (pendingItem instanceof Stew) {
            return rot.getCurrentHp() >= rot.getMaxHp();
        }
        if (pendingItem instanceof Antidote antidote) {
            String cure = antidote.getStatusToCure().toUpperCase();
            if (cure.equals("DEBUFF")) return false; // always allow; we check inside
            return !rot.hasStatus(cure);
        }
        // Scrolls: all members shown, feedback on ENTER
        return false;
    }

    private String greyReason(BrainRot rot) {
        if (pendingItem instanceof Stew) return "HP is full!";
        if (pendingItem instanceof Antidote a) {
            String cure = a.getStatusToCure();
            return "Not " + capitalize(cure.toLowerCase()) + "!";
        }
        return "";
    }

    // ── Shared draw helpers ───────────────────────────────────────────────────

    private void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 16;
        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, arc - 4, arc - 4);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawCard(Graphics2D g2, int x, int y, int w, int h, int arc) {
        g2.setColor(new Color(230, 226, 218));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setColor(new Color(190, 185, 172));
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    /**
     * Draws word-wrapped text. Returns the y of the baseline of the last line drawn.
     */
    private int drawWrappedText(Graphics2D g2, Font font, String text,
                                int x, int y, int maxW, int lineH, Color color) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        int cy = y;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && !line.isEmpty()) {
                g2.drawString(line.toString(), x, cy);
                cy += lineH;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) { g2.drawString(line.toString(), x, cy); }
        return cy;
    }

    private void drawCentredText(Graphics2D g2, Font base, int bx, int by, int bw, int bh, String t) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, bx + (bw - fm.stringWidth(t)) / 2, by + bh / 2 + 4);
    }

    // ── Image loading ─────────────────────────────────────────────────────────

    private BufferedImage loadItemImage(Item item) {
        String path = item.getAssetPath();
        if (path == null || path.isEmpty()) return null;
        return imgCache.computeIfAbsent(path, AssetManager::loadImage);
    }

    // ── Colour helpers ────────────────────────────────────────────────────────

    private Color typeColor(String typeName) {
        return switch (typeName.toUpperCase()) {
            case "FIGHTING" -> new Color(180,  80,  60);
            case "WATER"    -> new Color( 60, 130, 210);
            case "PSYCHIC"  -> new Color(200,  60, 140);
            case "FLYING"   -> new Color(120, 160, 220);
            case "SAND"     -> new Color(190, 155,  80);
            case "GRASS"    -> new Color( 80, 170,  80);
            case "ROCK"     -> new Color(140, 120,  80);
            case "FIRE"     -> new Color(220, 100,  40);
            case "DARK"     -> new Color( 80,  60, 100);
            case "POISON"   -> new Color(140,  70, 160);
            default         -> new Color(130, 126, 118);
        };
    }

    // ── String helpers ────────────────────────────────────────────────────────

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String findSignatureOwner(String skillName) {
        // Check all BrainRot names via SkillPool
        for (String rot : new String[]{
                "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
                "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
                "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"}) {
            if (SkillPool.isSignatureOf(skillName, rot)) return rot;
        }
        return null;
    }

    // ── Status helpers ────────────────────────────────────────────────────────

    private void setStatus(String msg) {
        statusMessage = msg;
        statusTimer   = STATUS_TICKS;
        System.out.println("[InventoryUI] " + msg);
    }

    private void clearStatus() {
        statusMessage = "";
        statusTimer   = 0;
    }

    // ── Placeholder stat accessors (no base-stat getter on BrainRot) ──────────
    // BrainRot only exposes effective (modified) stats, so we can't perfectly
    // detect debuffs from outside. These are best-effort helpers.

    private int getBaseAtk(BrainRot rot) { return rot.getAttack(); }   // if mods are 1.0, equals base
    private int getBaseDef(BrainRot rot) { return rot.getDefense(); }
    private int getBaseSpd(BrainRot rot) { return rot.getSpeed(); }
}