package ui;

import utils.AssetManager;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

import static utils.Constants.*;

/**
 * Generic top-right toast for save/item/capture events.
 * Multiple notifications are queued and shown one at a time.
 */
public class NotificationToast {

    private static final int TOAST_DURATION = 110;
    private static final int SLIDE_FRAMES   = 12;
    private static final int TOAST_W        = 280;
    private static final int TOAST_H        = 56;
    private static final int TOAST_MARGIN   = 12;

    public static class Notice {
        final String header;
        final String body;
        final Color  accent;
        public Notice(String header, String body, Color accent) {
            this.header = header; this.body = body; this.accent = accent;
        }
    }

    private final Deque<Notice> queue = new ArrayDeque<>();
    private Notice current = null;
    private int    timer   = 0;

    public void push(String header, String body) {
        push(header, body, new Color(216, 184, 88));
    }
    public void push(String header, String body, Color accent) {
        queue.add(new Notice(header, body, accent));
    }

    public void update() {
        if (current == null) {
            if (!queue.isEmpty()) {
                current = queue.poll();
                timer   = 0;
            }
            return;
        }
        timer++;
        if (timer >= TOAST_DURATION + SLIDE_FRAMES) {
            current = null;
            timer   = 0;
        }
    }

    public void draw(Graphics2D g2) {
        if (current == null) return;

        int slideIn  = Math.min(timer, SLIDE_FRAMES);
        int slideOut = Math.max(0, timer - TOAST_DURATION);
        int slide    = Math.max(slideIn, slideOut);
        int offsetX  = (SLIDE_FRAMES - slide) * (TOAST_W / SLIDE_FRAMES);

        int toastX = SCREEN_WIDTH  - TOAST_W - TOAST_MARGIN + offsetX;
        int toastY = TOAST_MARGIN + 70; // below quest toast

        g2.setColor(new Color(44, 44, 42, 230));
        g2.fillRoundRect(toastX, toastY, TOAST_W, TOAST_H, 10, 10);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(current.accent);
        g2.drawRoundRect(toastX, toastY, TOAST_W, TOAST_H, 10, 10);
        g2.setStroke(new BasicStroke(1));

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        g2.setFont(base.deriveFont(Font.BOLD, 8f));
        g2.setColor(current.accent);
        g2.drawString(current.header, toastX + 18, toastY + 22);

        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        g2.setColor(new Color(241, 239, 232));
        FontMetrics fm = g2.getFontMetrics();
        String body = truncate(current.body == null ? "" : current.body, fm, TOAST_W - 28);
        g2.drawString(body, toastX + 18, toastY + 40);
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "...") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }
}
