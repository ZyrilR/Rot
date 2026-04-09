package ui;

import progression.Quest;
import progression.QuestSystem;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static utils.Constants.*;

/**
 * Renders a small non-blocking toast notification when a quest is completed.
 *
 * Queue-based: multiple completions are shown one at a time.
 * Each toast displays for TOAST_DURATION frames then slides out.
 *
 * Usage in GamePanel.paintComponent() before g2.dispose():
 *   gp.QUESTTOAST.update();
 *   gp.QUESTTOAST.draw(g2);
 */
public class QuestToast {

    private static final int TOAST_DURATION = 90;  // frames visible (3s @ 30fps)
    private static final int SLIDE_FRAMES   = 12;  // frames to slide in/out
    private static final int TOAST_W        = 260;
    private static final int TOAST_H        = 56;
    private static final int TOAST_MARGIN   = 12;
    private static final int IMG_SIZE       = 36;

    private Quest         current   = null;
    private int           timer     = 0;
    private BufferedImage cachedImg = null;

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (current == null) {
            if (QuestSystem.getInstance().hasToast()) {
                current   = QuestSystem.getInstance().pollToast();
                timer     = 0;
                cachedImg = null;
            }
            return;
        }

        timer++;
        if (timer >= TOAST_DURATION + SLIDE_FRAMES) {
            current   = null;
            timer     = 0;
            cachedImg = null;
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        if (current == null) return;

        int slideIn  = Math.min(timer, SLIDE_FRAMES);
        int slideOut = Math.max(0, timer - TOAST_DURATION);
        int slide    = Math.max(slideIn, slideOut);
        int offsetX  = (SLIDE_FRAMES - slide) * (TOAST_W / SLIDE_FRAMES);

        int toastX = SCREEN_WIDTH  - TOAST_W - TOAST_MARGIN + offsetX;
        int toastY = TOAST_MARGIN;

        // Background
        g2.setColor(new Color(44, 44, 42, 230));
        g2.fillRoundRect(toastX, toastY, TOAST_W, TOAST_H, 10, 10);

        // Gold border
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(toastX, toastY, TOAST_W, TOAST_H, 10, 10);
        g2.setStroke(new BasicStroke(1));

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        // Header label
        g2.setFont(base.deriveFont(Font.BOLD, 8f));
        g2.setColor(new Color(216, 184, 88));
        g2.drawString("Quest Completed!", toastX + IMG_SIZE + 16, toastY + 16);

        // Quest name
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        g2.setColor(new Color(241, 239, 232));
        FontMetrics fm = g2.getFontMetrics();
        String name = truncate(current.getName(), fm, TOAST_W - IMG_SIZE - 24);
        g2.drawString(name, toastX + IMG_SIZE + 16, toastY + 34);

        // Quest image
        if (cachedImg == null) {
            cachedImg = AssetManager.loadImage("/res/Achievements/PROGRESSION_BADGES/ASCENSION.png");
        }

        int imgX = toastX + 10;
        int imgY = toastY + (TOAST_H - IMG_SIZE) / 2;
        if (cachedImg != null) {
            g2.drawImage(cachedImg, imgX, imgY, IMG_SIZE, IMG_SIZE, null);
        } else {
            g2.setColor(new Color(216, 184, 88));
            g2.fillRoundRect(imgX, imgY, IMG_SIZE, IMG_SIZE, 6, 6);
        }
    }

    public boolean isActive() { return current != null; }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "...") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }
}