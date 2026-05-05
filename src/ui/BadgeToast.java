package ui;

import progression.BadgeSystem;
import progression.BadgeSystem.Badge;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static utils.Constants.*;

/**
 * Large, emphatic badge-earned toast.
 *
 * Bigger than QuestToast — occupies the centre-bottom area so it feels
 * like a proper reward moment.
 *
 * Usage in GamePanel.paintComponent() before g2.dispose():
 *   gp.BADGETOAST.update();
 *   gp.BADGETOAST.draw(g2);
 */
public class BadgeToast {

    // ── Layout ────────────────────────────────────────────────────────────────

    private static final int TOAST_W       = 440;
    private static final int TOAST_H       = 110;
    private static final int TOAST_MARGIN  = 24;   // from bottom of screen
    private static final int IMG_SIZE      = 72;
    private static final int SLIDE_FRAMES  = 18;
    private static final int HOLD_FRAMES   = 120;  // 4 s @ 30 fps
    private static final int TOTAL_FRAMES  = HOLD_FRAMES + SLIDE_FRAMES;

    // ── State ─────────────────────────────────────────────────────────────────

    private Badge         current   = null;
    private int           timer     = 0;
    private BufferedImage cachedImg = null;

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (current == null) {
            if (BadgeSystem.getInstance().hasToast()) {
                current   = BadgeSystem.getInstance().pollToast();
                timer     = 0;
                cachedImg = null;
            }
            return;
        }

        timer++;
        if (timer >= TOTAL_FRAMES) {
            current   = null;
            timer     = 0;
            cachedImg = null;
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        if (current == null) return;

        // Slide in from bottom, hold, slide out to bottom
        int slideIn  = Math.min(timer, SLIDE_FRAMES);
        int slideOut = Math.max(0, timer - HOLD_FRAMES);
        int slide    = Math.max(slideIn, slideOut);
        int offsetY  = (SLIDE_FRAMES - slide) * (TOAST_H / SLIDE_FRAMES);

        int toastX = (SCREEN_WIDTH - TOAST_W) / 2;
        int toastY = SCREEN_HEIGHT - TOAST_H - TOAST_MARGIN + offsetY;

        drawBackground(g2, toastX, toastY);
        drawBadgeImage(g2, toastX, toastY);
        drawText       (g2, toastX, toastY);
        drawShineBar   (g2, toastX, toastY);
    }

    // ── Background ────────────────────────────────────────────────────────────

    private void drawBackground(Graphics2D g2, int x, int y) {
        int arc = 16;

        // Deep dark fill
        g2.setColor(new Color(22, 20, 16, 245));
        g2.fillRoundRect(x, y, TOAST_W, TOAST_H, arc, arc);

        // Triple-stroke border: dark → gold → dark (matches game UI language)
        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(60, 55, 45));
        g2.drawRoundRect(x, y, TOAST_W, TOAST_H, arc, arc);

        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(x + 1, y + 1, TOAST_W - 2, TOAST_H - 2, arc, arc);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(60, 55, 45));
        g2.drawRoundRect(x + 4, y + 4, TOAST_W - 8, TOAST_H - 8, arc - 4, arc - 4);

        g2.setStroke(new BasicStroke(1));
    }

    // ── Badge image ───────────────────────────────────────────────────────────

    private void drawBadgeImage(Graphics2D g2, int x, int y) {
        if (cachedImg == null)
            cachedImg = AssetManager.loadImage(current.assetPath);

        int imgX = x + 18;
        int imgY = y + (TOAST_H - IMG_SIZE) / 2;

        if (cachedImg != null) {
            // Soft gold glow behind the badge image
            g2.setColor(new Color(216, 184, 88, 40));
            g2.fillOval(imgX - 6, imgY - 6, IMG_SIZE + 12, IMG_SIZE + 12);
            g2.drawImage(cachedImg, imgX, imgY, IMG_SIZE, IMG_SIZE, null);
        } else {
            // Placeholder circle
            g2.setColor(new Color(216, 184, 88, 80));
            g2.fillOval(imgX, imgY, IMG_SIZE, IMG_SIZE);
            g2.setColor(new Color(216, 184, 88));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(imgX, imgY, IMG_SIZE, IMG_SIZE);
            g2.setStroke(new BasicStroke(1));
        }
    }

    // ── Text ──────────────────────────────────────────────────────────────────

    private void drawText(Graphics2D g2, int x, int y) {
        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        int textX = x + IMG_SIZE + 32;
        int midY  = y + TOAST_H / 2;

        // "GYM BADGE EARNED!" header
        g2.setFont(base.deriveFont(Font.BOLD, 9f));
        g2.setColor(new Color(216, 184, 88));
        g2.drawString("GYM BADGE EARNED!", textX, midY - 26);

        // Defeated leader line
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(160, 155, 140));
        g2.drawString("Defeated " + current.leaderName, textX, midY - 10);

        // Badge name — large and prominent
        g2.setFont(base.deriveFont(Font.BOLD, 16f));
        g2.setColor(new Color(241, 239, 232));
        FontMetrics fm = g2.getFontMetrics();

        // Scale down if the badge name is too wide
        String badgeName = current.badgeName;
        int    maxW      = TOAST_W - IMG_SIZE - 50;
        float  sz        = 16f;
        while (fm.stringWidth(badgeName) > maxW && sz > 10f) {
            sz -= 1f;
            g2.setFont(base.deriveFont(Font.BOLD, sz));
            fm = g2.getFontMetrics();
        }
        g2.drawString(badgeName, textX, midY + 18);
    }

    // ── Animated shine bar ────────────────────────────────────────────────────

    /**
     * A thin gold bar that sweeps across the toast during the hold phase,
     * giving the impression of a sheen passing over the badge art.
     */
    private void drawShineBar(Graphics2D g2, int x, int y) {
        if (timer < SLIDE_FRAMES || timer > HOLD_FRAMES) return;

        int holdElapsed = timer - SLIDE_FRAMES;
        float progress  = (float) holdElapsed / (HOLD_FRAMES - SLIDE_FRAMES);

        int barX = x + (int)(TOAST_W * progress) - 20;

        // Clip to the toast interior
        Shape prev = g2.getClip();
        g2.setClip(x + 8, y + 8, TOAST_W - 16, TOAST_H - 16);

        // Draw a thin diagonal white streak
        g2.setColor(new Color(255, 255, 255, 30));
        int[] px = { barX - 10, barX + 10, barX + 30, barX + 10 };
        int[] py = { y, y, y + TOAST_H, y + TOAST_H };
        g2.fillPolygon(px, py, 4);

        g2.setClip(prev);
    }

    public boolean isActive() { return current != null; }
}