package ui;

import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

import static utils.Constants.*;

/**
 * Renders and manages the in-game shop UI overlay.
 *
 * Layout:
 *   Title bar  — "BrainRot Market"  |  coin icon + balance
 *   Left panel — item image card + detail card (name, description, price)
 *   Right panel— scrollable item list sorted by price
 *   Status bar — feedback left, nav hints right
 *
 * Controls:
 *   W / S   — move cursor
 *   ENTER   — purchase
 *   ESC     — close
 */
public class ShopUI {

    // ── Injected refs ─────────────────────────────────────────────────────────
    private final GamePanel gp;

    // ── Shop inventory ────────────────────────────────────────────────────────
    private final ArrayList<Item> shopItems = new ArrayList<>();

    // ── State ─────────────────────────────────────────────────────────────────
    private int selectedIndex = 0;
    private int scrollOffset  = 0;

    // ── Input / status ────────────────────────────────────────────────────────
    private int    inputCooldown = 0;
    private String statusMessage = "";
    private int    statusTimer   = 0;

    // ── Image caches ──────────────────────────────────────────────────────────
    private BufferedImage coinIcon   = null;
    private boolean       iconLoaded = false;
    private final java.util.Map<String, BufferedImage> imgCache = new java.util.HashMap<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    public ShopUI(GamePanel gp) {
        this.gp = gp;
        buildShopInventory();
    }

    // ── Inventory setup ───────────────────────────────────────────────────────
    private void buildShopInventory() {
        String[] names = {
                // Stews
                "MILD STEW", "MODERATE STEW", "SUPER STEW",
                // Antidotes
                "CONFUSION CURE", "PARALYZE CURE", "BURN CURE", "SLEEP CURE", "DEBUFF TONIC",
                // Capsules
                "NORMAL CAPSULE", "RED CAPSULE", "BLUE CAPSULE", "SPEED CAPSULE", "HEAVY CAPSULE"
        };

        for (String name : names) {
            Item item = ItemRegistry.getItem(name);
            if (item != null) shopItems.add(item);
            else System.out.println("[ShopUI] Warning: item not found: " + name);
        }

        // Sort by price ascending
        shopItems.sort(Comparator.comparingInt(Item::getPrice));

        System.out.println("[ShopUI] Shop loaded: " + shopItems.size() + " items.");
    }

    // ── Coin icon (lazy load) ─────────────────────────────────────────────────
    private BufferedImage coinIcon() {
        if (!iconLoaded) {
            coinIcon   = AssetManager.loadImage("/res/Templates/Items/7.png");
            iconLoaded = true;
        }
        return coinIcon;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    private Item selectedItem() {
        return shopItems.isEmpty() ? null : shopItems.get(selectedIndex);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    public void open() {
        selectedIndex = 0;
        scrollOffset  = 0;
        statusMessage = "";
        statusTimer   = 0;
        inputCooldown = INPUT_DELAY * 2;
        System.out.println("[ShopUI] Shop opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }
        if (statusTimer   > 0) { statusTimer--; if (statusTimer == 0) statusMessage = ""; }

        if (gp.KEYBOARDHANDLER.upPressed && selectedIndex > 0) {
            selectedIndex--;
            clampScroll(computeVisibleCount());
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.downPressed && selectedIndex < shopItems.size() - 1) {
            selectedIndex++;
            clampScroll(computeVisibleCount());
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            attemptPurchase();
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "play";
            System.out.println("[ShopUI] Shop closed.");
        }
    }

    // ── Purchase ──────────────────────────────────────────────────────────────
    private void attemptPurchase() {
        Item item = selectedItem();
        if (item == null) return;

        int price = item.getPrice();

        if (gp.player.getRotCoins() < price) {
            setStatus("Not enough coins! Need " + price);
            statusTimer   = STATUS_TICKS;
            return;
        }

        if (!gp.player.getInventory().addItem(item)) {
            setStatus("Inventory is full!");
            statusTimer   = STATUS_TICKS;
            return;
        }

        gp.player.spendRotCoins(price);
        progression.QuestSystem.getInstance().onCoinsSpent(price);
        setStatus("Bought " + item.getName() + " for " + price + "!");
        statusTimer   = STATUS_TICKS;
        System.out.println("[ShopUI] Purchased: " + item.getName()
                + " | Remaining: " + gp.player.getRotCoins());
    }

    // ── Scroll helpers ────────────────────────────────────────────────────────
    private void clampScroll(int visCount) {
        if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;
        if (selectedIndex >= scrollOffset + visCount) scrollOffset = selectedIndex - visCount + 1;
        if (scrollOffset < 0) scrollOffset = 0;
    }

    private int computeVisibleCount() {
        int winY       = TILE_SIZE;
        int winH       = SCREEN_HEIGHT - TILE_SIZE * 2;
        int bodyY      = winY + 52;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;
        int labelY     = bodyY + 16;
        int firstRowY  = labelY + 8;
        int listAreaH  = (statusBarY - 8) - firstRowY;
        return Math.max(1, listAreaH / ROW_H);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DRAW
    // ══════════════════════════════════════════════════════════════════════════
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Arial", Font.PLAIN, 13);

        // Dim overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE, winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;

        drawWindow(g2, winX, winY, winW, winH);
        drawTitleBar(g2, base, winX, winY, winW);

        int bodyY      = winY + 52;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;

        int leftW = (int)(winW * LEFT_SPLIT / 100.0);
        int divX  = winX + leftW;

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, statusBarY - 4);

        drawLeftPanel (g2, base, winX, bodyY, leftW, statusBarY);
        drawRightPanel(g2, base, winX, winW, bodyY, divX, statusBarY);
        drawStatusBar (g2, base, winX, winY, winW, statusBarY, STATUS_BAR_H);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────
    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("BrainRot Market", winX + 28, winY + 32);

        // Coin balance — right side
        String coinText = String.valueOf(gp.player.getRotCoins());
        g2.setFont(base.deriveFont(Font.BOLD, 12f));
        FontMetrics numFm = g2.getFontMetrics();
        int numW     = numFm.stringWidth(coinText);
        int iconSize = 24, iconGap = 4;
        int blockW   = iconSize + iconGap + numW;
        int blockX   = winX + winW - 18 - blockW;
        int blockCY  = winY + 28;

        BufferedImage icon = coinIcon();
        if (icon != null) {
            g2.drawImage(icon, blockX, blockCY - iconSize / 2 - 2, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(216, 184, 88));
            g2.fillOval(blockX, blockCY - iconSize / 2, iconSize, iconSize);
        }

        g2.setColor(new Color(241, 239, 232));
        g2.drawString(coinText, blockX + iconSize + iconGap,
                blockCY + numFm.getAscent() / 2 - 1);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── Left panel — image + detail card ─────────────────────────────────────
    private void drawLeftPanel(Graphics2D g2, Font base,
                               int winX, int bodyY, int leftW, int statusBarY) {
        Item sel = selectedItem();

        int panelX      = winX + OUTER_PAD;
        int panelW      = leftW - OUTER_PAD - 10;
        int panelBottom = statusBarY - 8;

        // Image card
        int imgCardY = bodyY + 8;
        int imgCardH = 170;
        if (imgCardY + imgCardH > panelBottom) imgCardH = panelBottom - imgCardY - 4;
        drawCard(g2, panelX, imgCardY, panelW, imgCardH, 8);

        if (sel != null) {
            BufferedImage img = loadItemImage(sel);
            int m = 10;
            if (img != null) g2.drawImage(img, panelX + m, imgCardY + m, panelW - m * 2, imgCardH - m * 2, null);
            else drawCentredText(g2, base, panelX, imgCardY, panelW, imgCardH, "[img]");
        } else {
            drawCentredText(g2, base, panelX, imgCardY, panelW, imgCardH, "—");
        }

        // Detail card
        int descCardY = imgCardY + imgCardH + 6;
        int descCardH = panelBottom - descCardY;
        if (descCardH < 24) return;
        drawCard(g2, panelX, descCardY, panelW, descCardH, 8);

        if (sel == null) {
            g2.setFont(base.deriveFont(11f));
            g2.setColor(new Color(140, 136, 128));
            g2.drawString("Select an item.", panelX + 10, descCardY + 20);
            return;
        }

        Shape prevClip = g2.getClip();
        g2.setClip(panelX + 4, descCardY + 4, panelW - 8, descCardH - 8);

        int tx    = panelX + 10;
        int textW = panelW - 20;
        int ty    = descCardY + 22;

        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        ty = drawWordWrapped(g2, g2.getFontMetrics(), sel.getName(), tx, ty, textW,
                NAME_LINE_H, new Color(44, 44, 42));
        ty += 6;

        g2.setFont(base.deriveFont(11f));
        ty = drawWordWrapped(g2, g2.getFontMetrics(), sel.getDescription(),
                tx, ty, textW, DESC_LINE_H, new Color(88, 84, 76));
        ty += 12;

        // Price row
        int iconSize = 24;
        String priceStr = String.valueOf(sel.getPrice());
        g2.setFont(base.deriveFont(Font.BOLD, 12f));
        FontMetrics priceFm = g2.getFontMetrics();

        if (ty + iconSize <= descCardY + descCardH - 4) {
            BufferedImage icon = coinIcon();
            int iconY = ty - iconSize + 8;
            if (icon != null) g2.drawImage(icon, tx - 4, iconY, iconSize, iconSize, null);
            else { g2.setColor(new Color(216, 184, 88)); g2.fillOval(tx, iconY, iconSize, iconSize); }
            g2.setColor(new Color(15, 110, 86));
            g2.drawString(priceStr, tx + iconSize + 1, ty);
        }

        g2.setClip(prevClip);
    }

    // ── Right panel — scrollable item list ────────────────────────────────────
    private void drawRightPanel(Graphics2D g2, Font base,
                                int winX, int winW, int bodyY,
                                int divX, int statusBarY) {
        int totalCount = shopItems.size();

        int listX       = divX + OUTER_PAD;
        int listW       = winX + winW - listX - OUTER_PAD;
        int labelY      = bodyY + 16;
        int firstRowY   = labelY + 8;
        int listAreaBot = statusBarY - 8;
        int listAreaH   = listAreaBot - firstRowY;
        int visCount    = Math.max(1, listAreaH / ROW_H);

        // Label + counter
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("ITEMS", listX, labelY);

        if (totalCount > 0) {
            String counter = (selectedIndex + 1) + " / " + totalCount;
            FontMetrics cfm = g2.getFontMetrics();
            g2.drawString(counter, listX + listW - cfm.stringWidth(counter), labelY);
        }

        if (totalCount == 0) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(150, 145, 138));
            g2.drawString("Nothing here.", listX, firstRowY + ROW_H / 2);
            return;
        }

        Shape prevClip = g2.getClip();
        g2.setClip(listX - 6, firstRowY, listW + 12, listAreaH);

        int endIdx = Math.min(scrollOffset + visCount, totalCount);

        for (int i = scrollOffset; i < endIdx; i++) {
            Item    item    = shopItems.get(i);
            int     rowTop  = firstRowY + (i - scrollOffset) * ROW_H;
            int     textY   = rowTop + ROW_H - 10;
            boolean hovered = (i == selectedIndex);

            if (hovered) {
                g2.setColor(new Color(178, 212, 244, 200));
                g2.fillRoundRect(listX - 4, rowTop, listW + 4, ROW_H - 2, 5, 5);
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX - 4, rowTop, listW + 4, ROW_H - 2, 5, 5);
                g2.setStroke(new BasicStroke(1));

                int ts = 7, cx = listX + 2, cy = rowTop + ROW_H / 2 - 1;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ cx, cx, cx + ts }, new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            // Item name
            g2.setFont(base.deriveFont(hovered ? Font.BOLD : Font.PLAIN, 11f));
            g2.setColor(new Color(44, 44, 42));
            String priceStr = String.valueOf(item.getPrice());
            int iconSize  = 20, iconGap = 4;
            g2.setFont(base.deriveFont(9f));
            int priceW = g2.getFontMetrics().stringWidth(priceStr) + iconSize + iconGap + 8;
            g2.setFont(base.deriveFont(hovered ? Font.BOLD : Font.PLAIN, 11f));
            int nameMaxW = listW - priceW - 20;
            g2.drawString(truncate(item.getName(), g2.getFontMetrics(), nameMaxW), listX + 16, textY);

            // Price — coin icon + number, right-aligned
            g2.setFont(base.deriveFont(11f));
            FontMetrics pFm  = g2.getFontMetrics();
            int numW         = pFm.stringWidth(priceStr);
            int blockW       = iconSize + iconGap + numW;
            int blockX       = listX + listW - blockW - 6;
            int iconY        = textY - iconSize + 4;

            BufferedImage icon = coinIcon();
            if (icon != null) g2.drawImage(icon, blockX, iconY, iconSize, iconSize, null);
            else { g2.setColor(new Color(216, 184, 88)); g2.fillOval(blockX, iconY, iconSize, iconSize); }
            g2.setColor(new Color(80, 78, 72));
            g2.drawString(priceStr, blockX + iconSize + iconGap, textY - 2);

            if (i < endIdx - 1) {
                g2.setColor(new Color(205, 200, 190));
                g2.drawLine(listX, rowTop + ROW_H - 1, listX + listW, rowTop + ROW_H - 1);
            }
        }

        g2.setClip(prevClip);

        // Scroll hints
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(140, 136, 128));
        int arrowX = listX + listW / 2;
        if (scrollOffset > 0)
            g2.drawString("^ more", arrowX - 16, firstRowY - 2);
        if (scrollOffset + visCount < totalCount) {
            int hintY = firstRowY + visCount * ROW_H + 10;
            if (hintY < listAreaBot + 14)
                g2.drawString("v more", arrowX - 16, hintY);
        }
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private void drawStatusBar(Graphics2D g2, Font base,
                               int winX, int winY, int winW,
                               int statusBarY, int statusBarH) {
        int barX = winX + 8, barW = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, statusBarY, barW, statusBarH, 5, 5);

        String hint = "WS Move  ENTER Buy  ESC Close";

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics hfm = g2.getFontMetrics();
        int rx = barX + barW - 12;
        g2.drawString(hint, rx - hfm.stringWidth(hint), statusBarY + 25);

        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            FontMetrics mfm = g2.getFontMetrics();
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(truncate(statusMessage, mfm, barW),
                    barX + 14,
                    statusBarY + (statusBarH - mfm.getHeight()) / 2 + mfm.getAscent());
        }
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

    private void drawCentredText(Graphics2D g2, Font base,
                                 int bx, int by, int bw, int bh, String t) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, bx + (bw - fm.stringWidth(t)) / 2, by + bh / 2 + 4);
    }

    private int drawWordWrapped(Graphics2D g2, FontMetrics fm, String text,
                                int x, int y, int maxW, int lineH, Color color) {
        g2.setColor(color);
        int cy = y;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && !line.isEmpty()) {
                g2.drawString(line.toString(), x, cy);
                cy += lineH + 4;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) { g2.drawString(line.toString(), x, cy); cy += lineH; }
        return cy;
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "…") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    private BufferedImage loadItemImage(Item item) {
        String path = item.getAssetPath();
        if (path == null || path.isEmpty()) return null;
        String key = path.startsWith("/") ? path : "/" + path;
        return imgCache.computeIfAbsent(key, AssetManager::loadImage);
    }

    private void setStatus(String msg) {
        statusMessage = "[ " + msg + " ]";
        statusTimer   = STATUS_TICKS;
        System.out.println("[InventoryUI] " + msg);
    }
}