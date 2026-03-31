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
 *   LEFT / RIGHT — switch between category tabs (Potions, Capsules, Antidotes)
 *   UP   / DOWN  — move cursor within the current category's item list
 *   ENTER        — purchase selected item
 *   ESC          — close shop
 */
public class ShopUI {

    GamePanel gp;

    // ── Category data ──────────────────────────────────────────────────────────

    /**
     * Ordered map of category name → list of items in that category.
     * LinkedHashMap preserves insertion order so tabs render left-to-right
     * in the order we define them.
     *
     * TO ADD A CATEGORY: put a new entry in buildShopInventory() with a
     * display name key and the item names that belong to it.
     */
    private final LinkedHashMap<String, ArrayList<Item>> categories = new LinkedHashMap<>();

    // Flat list of category keys for indexed tab access
    private final ArrayList<String> categoryKeys = new ArrayList<>();

    // Which tab is active (index into categoryKeys)
    private int selectedCategory = 0;

    // Which item row is highlighted within the active category
    private int selectedIndex = 0;

    // ── Input cooldown ─────────────────────────────────────────────────────────

    private int inputCooldown = 0;
    private static final int INPUT_DELAY = 10; // frames between key repeats

    // ── Status bar ─────────────────────────────────────────────────────────────

    private String statusMessage = "";
    private int statusTimer = 0;
    private static final int STATUS_DURATION = 90; // ~3 seconds at 30 FPS

    // ── Constructor ────────────────────────────────────────────────────────────

    public ShopUI(GamePanel gp) {
        this.gp = gp;
        buildShopInventory();
    }

    // ── Inventory setup ────────────────────────────────────────────────────────

    /**
     * Defines which items belong to each category tab.
     * Edit the string arrays to change shop stock.
     * Edit the put() key strings to rename tabs.
     *
     * Scrolls are intentionally omitted — they aren't for sale.
     */
    private void buildShopInventory() {
        // --- Potions tab ---
        String[] potionNames = { "MILD STEW", "MODERATE STEW", "SUPER STEW" };
        categories.put("Stew", resolveItems(potionNames));

        // --- Capsules tab ---
        String[] capsuleNames = {
                "RED CAPSULE", "BLUE CAPSULE",
                "SPEED CAPSULE", "HEAVY CAPSULE", "MASTER CAPSULE"
        };
        categories.put("Capsules", resolveItems(capsuleNames));

        // --- Antidotes tab ---
        String[] antidoteNames = { "CONFUSION", "PARALYZE", "SLEEP", "DEBUFF" };
        categories.put("Antidotes", resolveItems(antidoteNames));

        // Build the flat key list for tab navigation
        categoryKeys.addAll(categories.keySet());

        System.out.println("[ShopUI] Categories loaded: " + categoryKeys);
        for (String key : categoryKeys) {
            System.out.println("[ShopUI]   " + key + ": " + categories.get(key).size() + " items");
        }
    }

    /**
     * Helper: looks up each name in ItemRegistry and returns the resolved list.
     * Logs a warning for any name that isn't found.
     */
    private ArrayList<Item> resolveItems(String[] names) {
        ArrayList<Item> list = new ArrayList<>();
        for (String name : names) {
            Item item = ItemRegistry.getItem(name);
            if (item != null) {
                list.add(item);
            } else {
                System.out.println("[ShopUI] Warning: item not found: " + name);
            }
        }
        return list;
    }

    // ── Convenience accessors ──────────────────────────────────────────────────

    /** Returns the item list for whichever tab is currently active. */
    private ArrayList<Item> currentItems() {
        return categories.get(categoryKeys.get(selectedCategory));
    }

    /** Returns the currently highlighted item, or null if the list is empty. */
    private Item selectedItem() {
        ArrayList<Item> items = currentItems();
        if (items.isEmpty()) return null;
        return items.get(selectedIndex);
    }

    // ── Public lifecycle ───────────────────────────────────────────────────────

    /**
     * Resets all state when the shop is opened.
     * Called by MarketNPC (via DialogueBox handshake) before GAMESTATE → "shop".
     */
    public void open() {
        selectedCategory = 0;
        selectedIndex    = 0;
        statusMessage    = "";
        statusTimer      = 0;
        // Double delay on open so ENTER from closing dialogue doesn't instantly buy
        inputCooldown    = INPUT_DELAY * 2;
        System.out.println("[ShopUI] Shop opened.");
    }

    // ── Update (called every frame in SHOP state) ──────────────────────────────

    /**
     * Handles all keyboard input for the shop.
     * Called by GamePanel.update() when GAMESTATE == "shop".
     */
    public void update() {
        if (inputCooldown > 0) inputCooldown--;
        if (statusTimer > 0) {
            statusTimer--;
            if (statusTimer == 0) statusMessage = "";
        }

        if (inputCooldown > 0) return; // block input during cooldown

        // --- TAB: next category tab ---
        else if (gp.KEYBOARDHANDLER.tabPressed) {
            selectedCategory = (selectedCategory + 1) % categoryKeys.size();
            selectedIndex    = 0;
            inputCooldown    = INPUT_DELAY;
            System.out.println("[ShopUI] Tab: " + categoryKeys.get(selectedCategory));
        }

        // --- UP: previous item in list ---
        else if (gp.KEYBOARDHANDLER.upPressed) {
            int size = currentItems().size();
            if (size > 0) {
                selectedIndex = (selectedIndex - 1 + size) % size;
                inputCooldown = INPUT_DELAY;
                System.out.println("[ShopUI] Cursor: " + selectedItem().getName());
            }
        }

        // --- DOWN: next item in list ---
        else if (gp.KEYBOARDHANDLER.downPressed) {
            int size = currentItems().size();
            if (size > 0) {
                selectedIndex = (selectedIndex + 1) % size;
                inputCooldown = INPUT_DELAY;
                System.out.println("[ShopUI] Cursor: " + selectedItem().getName());
            }
        }

        // --- ENTER: buy selected item ---
        else if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false; // consume so it doesn't bleed
            attemptPurchase();
            inputCooldown = INPUT_DELAY;
        }

        // --- ESC: close shop ---
        else if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "play";
            System.out.println("[ShopUI] Shop closed.");
        }
    }

    // ── Purchase logic ─────────────────────────────────────────────────────────

    /**
     * Attempts to buy the currently selected item.
     * Checks: coins sufficient → inventory not full → deduct → add item.
     * Sets statusMessage with the outcome either way.
     *
     * TO MODIFY: discounts, bulk buy, quantity limits — all go here.
     */
    private void attemptPurchase() {
        Item item = selectedItem();
        if (item == null) return;

        int price = item.getPrice();

        if (gp.player.getRotCoins() < price) {
            statusMessage = "Not enough coins! Need " + price;
            statusTimer   = STATUS_DURATION;
            System.out.println("[ShopUI] Purchase failed (funds): " + item.getName());
            return;
        }

        if (!gp.player.getInventory().addItem(item)) {
            statusMessage = "Inventory is full!";
            statusTimer   = STATUS_DURATION;
            System.out.println("[ShopUI] Purchase failed (inventory full): " + item.getName());
            return;
        }

        gp.player.spendRotCoins(price);
        statusMessage = "Bought " + item.getName() + " for " + price + "!";
        statusTimer   = STATUS_DURATION;
        System.out.println("[ShopUI] Purchased: " + item.getName() +
                " | Remaining coins: " + gp.player.getRotCoins());
    }

    // ── Draw ───────────────────────────────────────────────────────────────────

    /**
     * Full shop overlay render.
     * Called by GamePanel.paintComponent() when GAMESTATE == "shop".
     *
     * Structure (top to bottom):
     *   1. Dim overlay over the game world
     *   2. Main window (border + background)
     *   3. Title bar  (shop name + coin count)
     *   4. Category tabs (LEFT/RIGHT navigation)
     *   5. Left panel  — item list for active tab
     *   6. Right panel — detail card + controls hint
     *   7. Status bar  — purchase feedback
     *
     * TO ADJUST LAYOUT: change winX/winY/winW/winH and the panel split (winW/2).
     */
    public void draw(Graphics2D g2) {

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb
                : new Font("Arial", Font.PLAIN, 13);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── 1. Dim overlay ─────────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // ── 2. Main window ─────────────────────────────────────────
        int winX = TILE_SIZE;
        int winY = TILE_SIZE;
        int winW = SCREEN_WIDTH - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;
        int arc  = 16;

        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(winX, winY, winW, winH, arc, arc);

        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(winX, winY, winW, winH, arc, arc);

        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(winX + 1, winY + 1, winW - 2, winH - 2, arc, arc);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(winX + 4, winY + 4, winW - 8, winH - 8, arc - 4, arc - 4);

        g2.setStroke(new BasicStroke(1));

        // ── 3. Title bar ───────────────────────────────────────────
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("BrainRot Market", winX + 28, winY + 32);

        String coinText = "Coins: " + gp.player.getRotCoins();
        g2.setFont(base.deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(coinText, winX + winW - fm.stringWidth(coinText) - 28, winY + 32);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);

        // ── 4. Tabs ────────────────────────────────────────────────
        int tabY = winY + 48;
        int tabH = 28;
        int tabW = (winW - 16) / categoryKeys.size();

        for (int i = 0; i < categoryKeys.size(); i++) {
            int tabX = winX + 8 + i * tabW;
            boolean active = (i == selectedCategory);

            g2.setColor(active ? new Color(241, 239, 232) : new Color(58, 58, 55));
            g2.fillRect(tabX, tabY, tabW, tabH);

            if (active) {
                g2.setColor(new Color(216, 184, 88));
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(tabX + 4, tabY + tabH - 2, tabX + tabW - 4, tabY + tabH - 2);
                g2.setStroke(new BasicStroke(1));
            }

            g2.setFont(base.deriveFont(active ? Font.BOLD : Font.PLAIN, 12f));
            g2.setColor(active ? new Color(44, 44, 42) : new Color(180, 176, 165));

            FontMetrics tfm = g2.getFontMetrics();
            String label = categoryKeys.get(i);
            int labelX = tabX + (tabW - tfm.stringWidth(label)) / 2;
            g2.drawString(label, labelX, tabY + 18);
        }

        // Divider under tabs
        int bodyY = tabY + tabH + 2;
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(winX + 8, bodyY, winX + winW - 8, bodyY);

        // ── 5. Left panel (ITEMS) ─────────────────────────────────
        int listX = winX + 18;
        int listY = bodyY + 18;
        int listW = winW / 2 - 20;
        int rowH  = 26;

        // Label
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("ITEMS", listX, listY);

        listY += 26; // spacing below label

        ArrayList<Item> items = currentItems();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int rowY = listY + i * rowH;

            if (i == selectedIndex) {
                g2.setColor(new Color(178, 212, 244, 200));
                g2.fillRoundRect(listX - 4, rowY - 16, listW + 4, rowH, 5, 5);

                g2.setColor(new Color(24, 95, 165));
                g2.drawRoundRect(listX - 4, rowY - 16, listW + 4, rowH, 5, 5);

                g2.setColor(new Color(24, 95, 165));
                g2.drawString("\u25BA", listX, rowY);
            }

            g2.setFont(base.deriveFont(12f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(item.getName(), listX + 14, rowY);

            String price = item.getPrice() + " \u20B5";
            FontMetrics pfm = g2.getFontMetrics();
            g2.setColor(new Color(80, 78, 72));
            g2.drawString(price, listX + listW - pfm.stringWidth(price), rowY);
        }

        // ── 6. Right panel (DETAILS) ──────────────────────────────
        int divX = winX + winW / 2;
        int detailX = divX + 14;
        int detailY = bodyY + 18;

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, winY + winH - 36);

        // Label
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("DETAILS", detailX, detailY);

        detailY += 10;

        // Card
        int cardW = winW / 2 - 30;
        int cardH = 80;
        g2.setColor(new Color(230, 225, 215));
        g2.fillRoundRect(detailX, detailY, cardW, cardH, 8, 8);
        g2.setColor(new Color(190, 185, 172));
        g2.drawRoundRect(detailX, detailY, cardW, cardH, 8, 8);

        Item sel = selectedItem();
        if (sel != null) {
            g2.setFont(base.deriveFont(Font.BOLD, 12f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(sel.getName(), detailX + 10, detailY + 24);

            g2.setFont(base.deriveFont(11f));
            g2.setColor(new Color(88, 84, 76));
            g2.drawString(sel.getDescription(), detailX + 10, detailY + 44);

            g2.setFont(base.deriveFont(12f));
            g2.setColor(new Color(15, 110, 86));
            g2.drawString("Price: " + sel.getPrice() + " \u20B5", detailX + 10, detailY + 64);
        }

        // ── 7. Status bar: navigation guide / status ────────────────────────────────────────
        int barHeight = 36;
        int barY = winY + winH - barHeight - 8; // shift upward to avoid border overlap
        // Horizontal padding
        int padX = 12; // space from left and right edges

    // Draw the rectangle
        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(winX + 8, barY, winW - 16, barHeight, 5, 5);

    // Left status message
        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(statusMessage, winX + 8 + padX, barY + 22);
        }

    // Right navigation guide
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        String navGuide = "U/D Move  TAB Category  ENTER Buy  ESC Exit";
        fm = g2.getFontMetrics();
        int guideX = winX + winW - 8 - padX - fm.stringWidth(navGuide);
        g2.drawString(navGuide, guideX, barY + 22);
    }
}