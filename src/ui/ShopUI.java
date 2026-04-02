package ui;

import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import utils.AssetManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static utils.Constants.*;

/**
 * Renders and manages the in-game shop UI overlay with category tabs.
 *
 * Navigation:
 *   TAB          — cycle category tabs (Stew, Capsules, Antidotes)
 *   UP   / DOWN  — move cursor within the current category's item list
 *   ENTER        — purchase selected item
 *   ESC          — close shop
 */
public class ShopUI {

    GamePanel gp;

    // ── Category data ──────────────────────────────────────────────────────────

    private final LinkedHashMap<String, ArrayList<Item>> categories = new LinkedHashMap<>();
    private final ArrayList<String> categoryKeys = new ArrayList<>();

    private int selectedCategory = 0;
    private int selectedIndex    = 0;

    // ── Input cooldown ─────────────────────────────────────────────────────────

    private int inputCooldown = 0;
    private static final int INPUT_DELAY     = 10;
    private static final int STATUS_DURATION = 90; // ~3 seconds at 30 FPS

    // ── Status bar ─────────────────────────────────────────────────────────────

    private String statusMessage = "";
    private int    statusTimer   = 0;

    // ── Constructor ────────────────────────────────────────────────────────────

    public ShopUI(GamePanel gp) {
        this.gp = gp;
        buildShopInventory();
    }

    // ── Inventory setup ────────────────────────────────────────────────────────

    private void buildShopInventory() {
        String[] stewNames     = { "MILD STEW", "MODERATE STEW", "SUPER STEW" };
        String[] capsuleNames  = { "RED CAPSULE", "BLUE CAPSULE", "SPEED CAPSULE", "HEAVY CAPSULE", "MASTER CAPSULE" };
        String[] antidoteNames = { "CONFUSION", "PARALYZE", "SLEEP", "DEBUFF" };

        categories.put("Stew",      resolveItems(stewNames));
        categories.put("Capsules",  resolveItems(capsuleNames));
        categories.put("Antidotes", resolveItems(antidoteNames));

        categoryKeys.addAll(categories.keySet());

        System.out.println("[ShopUI] Categories loaded: " + categoryKeys);
        for (String key : categoryKeys)
            System.out.println("[ShopUI]   " + key + ": " + categories.get(key).size() + " items");
    }

    private ArrayList<Item> resolveItems(String[] names) {
        ArrayList<Item> list = new ArrayList<>();
        for (String name : names) {
            Item item = ItemRegistry.getItem(name);
            if (item != null) list.add(item);
            else System.out.println("[ShopUI] Warning: item not found: " + name);
        }
        return list;
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    private ArrayList<Item> currentItems() {
        return categories.get(categoryKeys.get(selectedCategory));
    }

    private Item selectedItem() {
        ArrayList<Item> items = currentItems();
        return items.isEmpty() ? null : items.get(selectedIndex);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    public void open() {
        selectedCategory = 0;
        selectedIndex    = 0;
        statusMessage    = "";
        statusTimer      = 0;
        inputCooldown    = INPUT_DELAY * 2; // prevent ENTER bleed from dialogue
        System.out.println("[ShopUI] Shop opened.");
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) inputCooldown--;
        if (statusTimer > 0) { statusTimer--; if (statusTimer == 0) statusMessage = ""; }
        if (inputCooldown > 0) return;

        else if (gp.KEYBOARDHANDLER.tabPressed) {
            selectedCategory = (selectedCategory + 1) % categoryKeys.size();
            selectedIndex    = 0;
            inputCooldown    = INPUT_DELAY;
            System.out.println("[ShopUI] Tab: " + categoryKeys.get(selectedCategory));
        }

        else if (gp.KEYBOARDHANDLER.upPressed) {
            int size = currentItems().size();
            if (size > 0) { selectedIndex = (selectedIndex - 1 + size) % size; inputCooldown = INPUT_DELAY; }
        }

        else if (gp.KEYBOARDHANDLER.downPressed) {
            int size = currentItems().size();
            if (size > 0) { selectedIndex = (selectedIndex + 1) % size; inputCooldown = INPUT_DELAY; }
        }

        else if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            attemptPurchase();
            inputCooldown = INPUT_DELAY;
        }

        else if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "play";
            System.out.println("[ShopUI] Shop closed.");
        }
    }

    // ── Purchase ───────────────────────────────────────────────────────────────

    private void attemptPurchase() {
        Item item = selectedItem();
        if (item == null) return;

        int price = item.getPrice();

        if (gp.player.getRotCoins() < price) {
            statusMessage = "Not enough coins! Need " + price;
            statusTimer   = STATUS_DURATION;
            return;
        }

        if (!gp.player.getInventory().addItem(item)) {
            statusMessage = "Inventory is full!";
            statusTimer   = STATUS_DURATION;
            return;
        }

        gp.player.spendRotCoins(price);
        statusMessage = "Bought " + item.getName() + " for " + price + "!";
        statusTimer   = STATUS_DURATION;
        System.out.println("[ShopUI] Purchased: " + item.getName()
                + " | Remaining coins: " + gp.player.getRotCoins());
    }

    // ── Draw ───────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Arial", Font.PLAIN, 13);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Dim overlay ────────────────────────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // ── Window ────────────────────────────────────────────────────────────
        int winX  = TILE_SIZE;
        int winY  = TILE_SIZE;
        int winW  = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH  = SCREEN_HEIGHT - TILE_SIZE * 2;
        int winArc = 16;

        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(winX, winY, winW, winH, winArc, winArc);

        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(winX, winY, winW, winH, winArc, winArc);

        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(winX + 1, winY + 1, winW - 2, winH - 2, winArc, winArc);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(winX + 4, winY + 4, winW - 8, winH - 8, winArc - 4, winArc - 4);

        g2.setStroke(new BasicStroke(1));

        // ── Title bar ─────────────────────────────────────────────────────────
        int titleBarX = winX + 8;
        int titleBarY = winY + 8;
        int titleBarW = winW - 16;
        int titleBarH = 36;

        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(titleBarX, titleBarY, titleBarW, titleBarH, 8, 8);

        int titleTextY = winY + 32; // baseline for text inside title bar

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("BrainRot Market", titleBarX + 20, titleTextY);

        String coinText     = "Coins: " + gp.player.getRotCoins();
        int    coinRightPad = 20; // distance from right edge of title bar
        g2.setFont(base.deriveFont(12f));
        FontMetrics coinFm = g2.getFontMetrics();
        g2.drawString(coinText, titleBarX + titleBarW - coinFm.stringWidth(coinText) - coinRightPad, titleTextY);

        // Gold divider under title bar
        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);

        // ── Tabs ──────────────────────────────────────────────────────────────
        int tabRowY = winY + 48; // top of tab row
        int tabRowH = 28;        // height of tab row
        int tabW    = (winW - 16) / categoryKeys.size(); // width of each tab

        for (int i = 0; i < categoryKeys.size(); i++) {
            int     tabX   = winX + 8 + i * tabW;
            boolean active = (i == selectedCategory);

            int tabArc = 5;

            // Fill
            g2.setColor(active ? new Color(241, 239, 232) : new Color(58, 58, 55));
            g2.fillRoundRect(tabX, tabRowY, tabW, tabRowH, tabArc, tabArc);

            // Thin gold outline (inner)
            g2.setColor(new Color(216, 184, 88));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(tabX, tabRowY, tabW, tabRowH, tabArc, tabArc);

            // Reset stroke
            g2.setStroke(new BasicStroke(1));

            if (active) {
                g2.setColor(new Color(216, 184, 88));
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(tabX + 4, tabRowY + tabRowH - 2, tabX + tabW - 4, tabRowY + tabRowH - 2);
                g2.setStroke(new BasicStroke(1));
            }

            String      tabLabel   = categoryKeys.get(i);
            FontMetrics tabLabelFm = g2.getFontMetrics();
            int         tabLabelX  = tabX + (tabW - tabLabelFm.stringWidth(tabLabel)) / 2;

            g2.setFont(base.deriveFont(active ? Font.BOLD : Font.PLAIN, 12f));
            g2.setColor(active ? new Color(44, 44, 42) : new Color(180, 176, 165));
            g2.drawString(tabLabel, tabLabelX, tabRowY + 18);
        }

        // Divider under tab row — marks top of body area
        int bodyY = tabRowY + tabRowH + 2;
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(winX + 8, bodyY, winX + winW - 8, bodyY);

        // ── Left panel — item list ─────────────────────────────────────────────
        int listMarginX  = 18;          // horizontal margin from window edge to list
        int listX        = winX + listMarginX;
        int listTopY     = bodyY + 18;  // top of the list area (below the body divider)
        int listW        = winW / 2 - 20;
        int itemRowH     = 26;          // height of each item row

        // "ITEMS" section label
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("ITEMS", listX, listTopY);

        int itemListY = listTopY + 26; // first item row starts below the label

        ArrayList<Item> items = currentItems();
        for (int i = 0; i < items.size(); i++) {
            Item item     = items.get(i);
            int  itemRowY = itemListY + i * itemRowH; // top-left of this row's text baseline

            if (i == selectedIndex) {
                // Highlight background
                int highlightPadLeft = 4;
                g2.setColor(new Color(178, 212, 244, 200));
                g2.fillRoundRect(listX - highlightPadLeft, itemRowY - 16, listW + highlightPadLeft, itemRowH, 5, 5);
                g2.setColor(new Color(24, 95, 165));
                g2.drawRoundRect(listX - highlightPadLeft, itemRowY - 16, listW + highlightPadLeft, itemRowH, 5, 5);

                // Cursor arrow
                g2.drawString("\u25BA", listX, itemRowY);
            }

            // Item name
            int itemNameOffsetX = 14; // space for cursor arrow
            g2.setFont(base.deriveFont(12f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(item.getName(), listX + itemNameOffsetX, itemRowY);

            // Price — right-aligned to list width
            String      priceText  = item.getPrice() + " \u20B5";
            FontMetrics priceFm    = g2.getFontMetrics();
            g2.setColor(new Color(80, 78, 72));
            g2.drawString(priceText, listX + listW - priceFm.stringWidth(priceText), itemRowY);
        }

        // ── Right panel — detail card ──────────────────────────────────────────
        int panelDividerX = winX + winW / 2; // x of the vertical divider between panels
        int detailPadX    = 14;              // left padding inside the detail panel
        int detailX       = panelDividerX + detailPadX;
        int detailTopY    = bodyY + 18;

        // Vertical divider
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(panelDividerX, bodyY, panelDividerX, winY + winH - 36);

        // "DETAILS" section label
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("DETAILS", detailX, detailTopY);

        // Detail card
        int cardTopY  = detailTopY + 10;
        int cardW     = winW / 2 - 30;
        int cardH     = 80;
        int cardPadX  = 10; // horizontal inner padding inside the card

        g2.setColor(new Color(230, 225, 215));
        g2.fillRoundRect(detailX, cardTopY, cardW, cardH, 8, 8);
        g2.setColor(new Color(190, 185, 172));
        g2.drawRoundRect(detailX, cardTopY, cardW, cardH, 8, 8);

        Item sel = selectedItem();
        if (sel != null) {
            int cardLineH = 20; // baseline-to-baseline inside the card

            g2.setFont(base.deriveFont(Font.BOLD, 12f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(sel.getName(), detailX + cardPadX, cardTopY + cardLineH + 4);

            g2.setFont(base.deriveFont(11f));
            g2.setColor(new Color(88, 84, 76));
            g2.drawString(sel.getDescription(), detailX + cardPadX, cardTopY + cardLineH * 2 + 4);

            g2.setFont(base.deriveFont(12f));
            g2.setColor(new Color(15, 110, 86));
            g2.drawString("Price: " + sel.getPrice() + " \u20B5", detailX + cardPadX, cardTopY + cardLineH * 3 + 4);
        }

        // ── Status bar ────────────────────────────────────────────────────────
        int statusBarH      = 36;
        int statusBarPadX   = 12; // horizontal inner padding inside the status bar
        int statusBarY      = winY + winH - statusBarH - 8; // sits above the window border
        int statusBarX      = winX + 8;
        int statusBarW      = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(statusBarX, statusBarY, statusBarW, statusBarH, 5, 5);

        int statusTextY = statusBarY + 22; // vertical baseline for text inside the bar

        // Left — purchase feedback message
        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(statusMessage, statusBarX + statusBarPadX, statusTextY);
        }

        // Right — navigation hint
        String navGuide = "WS Move  TAB Category  ENTER Buy  ESC Close";
        g2.setFont(base.deriveFont(7f));
        FontMetrics navGuideFm = g2.getFontMetrics();
        int navGuideX = statusBarX + statusBarW - statusBarPadX - navGuideFm.stringWidth(navGuide);

        g2.setColor(new Color(120, 116, 108));
        g2.drawString(navGuide, navGuideX, statusTextY);
    }
}