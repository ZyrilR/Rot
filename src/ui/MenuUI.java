package ui;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;

import static utils.Constants.*;

/**
 * Compact pause menu — opens on ESC during play state.
 *
 * Controls:
 *   W / S   — move cursor
 *   ENTER   — confirm
 *   ESC     — close
 */
public class MenuUI {

    // ── Menu items ────────────────────────────────────────────────────────────

    private enum MenuItem {
        BACKPACK  ("BACKPACK"),
        BRAINROTS ("BRAINROTS"),
        EXIT      ("EXIT");

        final String label;
        MenuItem(String label) { this.label = label; }
    }

    private static final MenuItem[] ITEMS = MenuItem.values();

    // ── State ─────────────────────────────────────────────────────────────────

    private final GamePanel gp;
    private int cursorIndex   = 0;
    private int inputCooldown = 0;

    private static final int INPUT_DELAY = 10;

    // ── Layout ────────────────────────────────────────────────────────────────

    private static final int PANEL_W = 135;
    private static final int ROW_H   = 30;
    private static final int PAD_V   = 12;

    // ── Constructor ───────────────────────────────────────────────────────────

    public MenuUI(GamePanel gp) { this.gp = gp; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        cursorIndex   = 0;
        inputCooldown = INPUT_DELAY * 2;
        System.out.println("[MenuUI] Opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        if (gp.KEYBOARDHANDLER.upPressed) {
            cursorIndex = (cursorIndex - 1 + ITEMS.length) % ITEMS.length;
            inputCooldown = INPUT_DELAY;
        } else if (gp.KEYBOARDHANDLER.downPressed) {
            cursorIndex = (cursorIndex + 1) % ITEMS.length;
            inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "play";
            System.out.println("[MenuUI] Closed.");
            return;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            handleSelection();
        }
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    private void handleSelection() {
        switch (ITEMS[cursorIndex]) {
            case BACKPACK -> {
                // Open the inventory UI and switch game state
                gp.INVENTORYUI.open();
                gp.GAMESTATE = "inventory";
                System.out.println("[MenuUI] Opening Backpack.");
            }
            case BRAINROTS -> {
                gp.PCUI.open();
                gp.GAMESTATE = "pc";
                System.out.println("[MenuUI] Opening BrainRots (PC).");
            }
            case EXIT -> {
                System.out.println("[MenuUI] Exiting.");
                System.exit(0);
            }
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);
        Font labelFont = base.deriveFont(Font.BOLD, 10f);

        // ── Panel geometry ────────────────────────────────────────────────────
        int panelH = PAD_V + ITEMS.length * ROW_H + PAD_V;
        int panelX = TILE_SIZE - 16;
        int panelY = (SCREEN_HEIGHT - panelH) / 2;

        // Dim overlay
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Window
        drawWindow(g2, panelX, panelY, PANEL_W, panelH);

        // ── Menu rows ─────────────────────────────────────────────────────────
        int   itemX  = panelX + 10;
        int   labelX = itemX + 16;

        g2.setFont(labelFont);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < ITEMS.length; i++) {
            MenuItem item    = ITEMS[i];
            int      rowY    = panelY + PAD_V + i * ROW_H;
            int      cy      = rowY + ROW_H / 2 - 3;
            int      textY   = cy + fm.getAscent() / 2 - 1;
            boolean  hovered = (i == cursorIndex);

            // Cursor triangle — same style as InventoryUI
            if (hovered) {
                int ts = 9;
                int tx = itemX + 1;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(80, 76, 70));
                g2.fillPolygon(
                        new int[]{ tx,      tx,      tx + ts },
                        new int[]{ cy - ts, cy + ts, cy      },
                        3
                );
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            g2.setFont(labelFont);
            g2.setColor(new Color(80, 76, 70));
            g2.drawString(item.label, labelX, textY);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** 3px dark | 2px gold | 1px dark triple-stroke border. */
    private void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 12;
        g2.setColor(new Color(245, 242, 235));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x + 3, y + 3, w - 6, h - 6, arc - 2, arc - 2);
    }
}