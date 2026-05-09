package ui;

import engine.GamePanel;
import save.DataManager;
import utils.AssetManager;

import java.awt.*;

import static java.awt.FileDialog.SAVE;
import static utils.Constants.*;

/**
 * Compact pause menu — opens on ESC during play state.
 *
 * Controls:
 *   W / S   — move cursor
 *   A / D   — lower / raise volume
 *   ENTER   — confirm
 *   ESC     — close
 */
public class MenuUI {

    // ── Menu items ────────────────────────────────────────────────────────────

    private enum MenuItem {
        BACKPACK  ("BACKPACK"),
        BRAINROTS ("BRAINROTS"),
        QUESTS    ("QUESTS"),
        SAVE      ("SAVE"),
        VOLUME    ("VOLUME"), // <-- NEW VOLUME OPTION
        RETURN    ("RETURN");

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

    private static final int PANEL_W = 165; // WIDENED slightly to fit the slider
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

        // --- UP / DOWN NAVIGATION ---
        if (gp.KEYBOARDHANDLER.upPressed) {
            cursorIndex = (cursorIndex - 1 + ITEMS.length) % ITEMS.length;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        } else if (gp.KEYBOARDHANDLER.downPressed) {
            cursorIndex = (cursorIndex + 1) % ITEMS.length;
            utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
            inputCooldown = INPUT_DELAY;
        }

        // --- LEFT / RIGHT VOLUME CONTROL ---
        if (gp.KEYBOARDHANDLER.leftPressed) {
            gp.KEYBOARDHANDLER.leftPressed = false;
            if (ITEMS[cursorIndex] == MenuItem.VOLUME) {
                if (utils.AudioManager.volume > 0) {
                    utils.AudioManager.volume--;
                    utils.AudioManager.applyVolume();
                    utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
                    inputCooldown = INPUT_DELAY;
                }
            }
        }
        if (gp.KEYBOARDHANDLER.rightPressed) {
            gp.KEYBOARDHANDLER.rightPressed = false;
            if (ITEMS[cursorIndex] == MenuItem.VOLUME) {
                if (utils.AudioManager.volume < 10) {
                    utils.AudioManager.volume++;
                    utils.AudioManager.applyVolume();
                    utils.AudioManager.playSFX(utils.Constants.SFX_SELECT);
                    inputCooldown = INPUT_DELAY;
                }
            }
        }

        // --- CANCEL ---
        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "play";
            System.out.println("[MenuUI] Closed.");
            return;
        }

        // --- CONFIRM ---
        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;

            // Prevent pressing enter on the volume slider from doing anything
            if (ITEMS[cursorIndex] == MenuItem.VOLUME) {
                return;
            }

            utils.AudioManager.playSFX(utils.Constants.SFX_ENTER);
            handleSelection();
        }
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    private void handleSelection() {
        switch (ITEMS[cursorIndex]) {
            case BACKPACK -> {
                gp.INVENTORYUI.open();
                gp.GAMESTATE = "inventory";
                System.out.println("[MenuUI] Opening Backpack.");
            }
            case BRAINROTS -> {
                gp.GAMESTATE = "pc";
                gp.PCUI.open();
                System.out.println("[MenuUI] Opening BrainRots (PC).");
            }
            case QUESTS -> {
                gp.QUESTUI.open();
                gp.GAMESTATE = "quests";
                System.out.println("[MenuUI] Opening Quests.");
            }
            case SAVE -> {
                DataManager.saveCurrentLoad(gp);
                gp.NOTIFICATION.push("Saved", "Game progress saved.", new java.awt.Color(120, 200, 120));
                utils.AudioManager.playSFX(utils.Constants.SFX_SAVE);
                gp.GAMESTATE = "play";
                System.out.println("[MenuUI] Saving Current Slot.");
            }
            case RETURN -> {
                System.out.println("[MenuUI] Returning to Splash Screen.");
                utils.AudioManager.playMusic(utils.Constants.SND_SPLASH, true);
                gp.GAMESTATE = "splash";
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
            int      cy      = rowY + ROW_H / 2;
            int      textY   = cy + fm.getAscent() / 2;
            boolean  hovered = (i == cursorIndex);

            // Cursor triangle
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

            // --- CRAZY SHII: DRAW THE VOLUME SLIDER ---
            if (item == MenuItem.VOLUME) {
                g2.drawString("VOL", labelX, textY);

                int barX = labelX + fm.stringWidth("VOL") + 10;
                int barY = cy - 4;

                g2.drawString("<", barX, textY);

                // Draw 10 little volume notches
                for (int v = 0; v < 10; v++) {
                    int rectX = barX + 12 + (v * 7);
                    if (v < utils.AudioManager.volume) {
                        g2.fillRect(rectX, barY, 5, 8); // Solid box
                    } else {
                        g2.drawRect(rectX, barY, 4, 7); // Empty box
                    }
                }

                g2.drawString(">", barX + 86, textY);

            } else {
                g2.drawString(item.label, labelX, textY);
            }
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