package ui;

import engine.GamePanel;
import progression.BadgeSystem;
import progression.BadgeSystem.Badge;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.*;

/**
 * Full-screen badge collection overlay.
 *
 * Layout — 2-row grid (3 badges per row, 6 total):
 *
 *   [ Drip ]   [ Airstrike ]  [ Beatdown ]
 *   [ Archive ] [ Time Warp ] [ Rot Master ]
 *
 * Controls:
 *   WASD — move cursor
 *   ESC  — close
 */
public class BadgeUI {

    private static final int COLS     = 3;
    private static final int ROWS     = 2;
    private static final int CELL_W   = 170;
    private static final int CELL_H   = 170;
    private static final int CELL_GAP = 16;
    private static final int DETAIL_H = 90;

    private final GamePanel gp;
    private int cursorIdx     = 0;
    private int inputCooldown = 0;

    private final Map<String, BufferedImage> imgCache = new HashMap<>();

    public BadgeUI(GamePanel gp) { this.gp = gp; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        cursorIdx     = 0;
        inputCooldown = INPUT_DELAY * 2;
        System.out.println("[BadgeUI] Opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        int total = BadgeSystem.getInstance().getAll().size();

        if (gp.KEYBOARDHANDLER.leftPressed && cursorIdx % COLS > 0) {
            cursorIdx--;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.rightPressed && cursorIdx % COLS < COLS - 1) {
            cursorIdx++;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.upPressed && cursorIdx >= COLS) {
            cursorIdx -= COLS;
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && cursorIdx + COLS < total) {
            cursorIdx += COLS;
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "menu";
            gp.MENUUI.open();
            System.out.println("[BadgeUI] Closed.");
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        // Dim overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE;
        int winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;

        drawWindow(g2, winX, winY, winW, winH);
        drawTitleBar(g2, base, winX, winY, winW);

        int bodyY      = winY + 140;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;
        int detailY    = statusBarY - DETAIL_H - 8;

        // Grid — centred between title bar and detail strip
        int gridTotalW = COLS * CELL_W + (COLS - 1) * CELL_GAP;
        int gridH      = ROWS * CELL_H + (ROWS - 1) * CELL_GAP;
        int gridX      = winX + (winW - gridTotalW) / 2;
        int gridY      = bodyY + (detailY - bodyY - gridH) / 2;

        List<Badge> badges = BadgeSystem.getInstance().getAll();

        for (int i = 0; i < badges.size(); i++) {
            int col   = i % COLS;
            int row   = i / COLS;
            int cellX = gridX + col * (CELL_W + CELL_GAP);
            int cellY = gridY + row * (CELL_H + CELL_GAP);
            drawCell(g2, base, cellX, cellY, badges.get(i), i == cursorIdx);
        }

//        drawDetailStrip(g2, base, winX, winW, detailY, badges.get(cursorIdx));
        drawStatusBar(g2, base, winX, winW, statusBarY);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────

    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("GYM BADGES", winX + 28, winY + 32);

        int    earned  = BadgeSystem.getInstance().badgeCount();
        String counter = earned + " / 6";
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(216, 184, 88));
        g2.drawString(counter, winX + winW - 16 - fm.stringWidth(counter), winY + 32);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── Badge cell ────────────────────────────────────────────────────────────

    private void drawCell(Graphics2D g2, Font base,
                          int x, int y, Badge badge, boolean hovered) {
        int arc = 14;

        // Background
        Color bg = hovered
                ? new Color(178, 212, 244, 220)
                : new Color(230, 226, 218);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, CELL_W, CELL_H, arc, arc);

        // Border
        if (hovered) {
            g2.setColor(new Color(24, 95, 165));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, CELL_W, CELL_H, arc, arc);
            g2.setStroke(new BasicStroke(1));
        } else {
            g2.setColor(new Color(190, 185, 172));
            g2.drawRoundRect(x, y, CELL_W, CELL_H, arc, arc);
        }

        // Badge image or placeholder
        int imgPad  = 24;
        int imgSize = CELL_W - imgPad * 2;
        int imgX    = x + imgPad;
        int imgY    = y + 12;

        if (badge.isDefeated()) {
            BufferedImage img = loadImg(badge);
            if (img != null) {
                if (hovered) {
                    g2.setColor(new Color(24, 95, 165, 30));
                    g2.fillOval(imgX - 4, imgY - 4, imgSize + 8, imgSize + 8);
                }
                g2.drawImage(img, imgX, imgY, imgSize, imgSize, null);
            } else {
                drawPlaceholder(g2, base, imgX, imgY, imgSize, true);
            }
        } else {
            drawPlaceholder(g2, base, imgX, imgY, imgSize, false);
        }

        // Badge name label at bottom of cell
        int labelY = y + CELL_H - 14;
        g2.setFont(base.deriveFont(badge.isDefeated() ? Font.BOLD : Font.PLAIN, 9f));
        g2.setColor(badge.isDefeated()
                ? new Color(44, 44, 42)
                : new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        String label = badge.isDefeated() ? badge.badgeName : "???";
        g2.drawString(label, x + (CELL_W - fm.stringWidth(label)) / 2, labelY);
    }

    private void drawPlaceholder(Graphics2D g2, Font base,
                                 int x, int y, int size, boolean earned) {
        g2.setColor(new Color(210, 206, 198));
        g2.fillOval(x, y, size, size);
        g2.setColor(new Color(175, 170, 160));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, size, size);
        g2.setStroke(new BasicStroke(1));

        if (!earned) {
            Font qFont = (AssetManager.pokemonGb != null)
                    ? AssetManager.pokemonGb.deriveFont(Font.BOLD, 28f)
                    : new Font("Monospaced", Font.BOLD, 28);
            g2.setFont(qFont);
            g2.setColor(new Color(150, 145, 138));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("?",
                    x + (size - fm.stringWidth("?")) / 2,
                    y + (size + fm.getAscent()) / 2 - 4);
        }
    }

    // ── Detail strip ──────────────────────────────────────────────────────────

    private void drawDetailStrip(Graphics2D g2, Font base,
                                 int winX, int winW,
                                 int detailY, Badge sel) {
        int stripX = winX + 12;
        int stripW = winW - 24;

        // Standard card style
        g2.setColor(new Color(230, 226, 218));
        g2.fillRoundRect(stripX, detailY, stripW, DETAIL_H, 10, 10);
        g2.setColor(new Color(190, 185, 172));
        g2.drawRoundRect(stripX, detailY, stripW, DETAIL_H, 10, 10);

        int tx   = stripX + 20;
        int midY = detailY + DETAIL_H / 2;

        if (sel.isDefeated()) {
            // Badge name
            g2.setFont(base.deriveFont(Font.BOLD, 16f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(sel.badgeName, tx, midY - 12);

            // Leader name
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(88, 84, 76));
            g2.drawString("Defeated " + sel.leaderName, tx, midY + 10);

            // OBTAINED tag — right aligned
            g2.setFont(base.deriveFont(Font.BOLD, 9f));
            g2.setColor(new Color(60, 180, 80));
            String tag = "OBTAINED";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(tag,
                    stripX + stripW - fm.stringWidth(tag) - 20,
                    midY);

        } else {
            // Not yet earned
            g2.setFont(base.deriveFont(Font.BOLD, 13f));
            g2.setColor(new Color(140, 136, 128));
            g2.drawString("???", tx, midY - 8);

            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(120, 116, 108));
            g2.drawString("Defeat the gym leader to earn this badge.",
                    tx, midY + 12);
        }
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g2, Font base,
                               int winX, int winW, int statusBarY) {
        int barX = winX + 8;
        int barW = winW - 16;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, statusBarY, barW, STATUS_BAR_H, 5, 5);

        String hint = "WASD Move   ESC Close";
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint,
                barX + barW - 12 - fm.stringWidth(hint),
                statusBarY + 25);
    }

    // ── Window chrome ─────────────────────────────────────────────────────────

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

    // ── Image cache ───────────────────────────────────────────────────────────

    private BufferedImage loadImg(Badge badge) {
        return imgCache.computeIfAbsent(badge.id,
                k -> AssetManager.loadImage(badge.assetPath));
    }
}