package ui;

import engine.GamePanel;
import progression.Quest;
import progression.QuestSystem;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.*;

/**
 * Full-screen quest browser overlay.
 *
 * Layout:
 *   Left panel  — selected quest image + name + description + progress + reward
 *   Right panel — scrollable list of all 30 quests sorted by difficulty
 *   Status bar  — nav hints
 *
 * Controls:
 *   W / S   — move cursor
 *   ENTER   — claim reward (if completed and unclaimed)
 *   ESC     — close
 */
public class QuestUI {

    private final GamePanel gp;
    private int  cursor        = 0;
    private int  scrollOffset  = 0;
    private int  inputCooldown = 0;

    private String statusMessage = "";

    private final Map<String, BufferedImage> imgCache = new HashMap<>();

    private static final int ROW_H      = 34;
    private static final int OUTER_PAD  = 18;
    private static final int LEFT_SPLIT = 38;

    public QuestUI(GamePanel gp) { this.gp = gp; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        cursor        = 0;
        scrollOffset  = 0;
        statusMessage = "";
        inputCooldown = INPUT_DELAY * 2;
        System.out.println("[QuestUI] Opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        List<Quest> all  = QuestSystem.getInstance().getAll();
        int         size = all.size();

        if (gp.KEYBOARDHANDLER.upPressed && cursor > 0) {
            cursor--;
            clampScroll(computeVisibleCount());
            statusMessage = "";
            inputCooldown = INPUT_DELAY;
            return;
        }
        if (gp.KEYBOARDHANDLER.downPressed && cursor < size - 1) {
            cursor++;
            clampScroll(computeVisibleCount());
            statusMessage = "";
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            if (!all.isEmpty()) {
                Quest sel = all.get(cursor);
                if (sel.isCompleted() && !sel.isRewardClaimed()) {
                    QuestSystem.getInstance().claimReward(sel.getId(), gp);
                    statusMessage = "[ Reward claimed! ]";
                } else if (sel.isCompleted() && sel.isRewardClaimed()) {
                    statusMessage = "[ Already claimed ]";
                } else {
                    statusMessage = "[ Quest not completed yet ]";
                }
            }
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.GAMESTATE = "menu";
            gp.MENUUI.open();
            System.out.println("[QuestUI] Closed.");
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE, winY = TILE_SIZE;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE * 2;

        drawWindow(g2, winX, winY, winW, winH);
        drawTitleBar(g2, base, winX, winY, winW);

        int bodyY      = winY + 52;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;
        int leftW      = (int)(winW * LEFT_SPLIT / 100.0);
        int divX       = winX + leftW;

        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(divX, bodyY, divX, statusBarY - 4);

        drawLeftPanel (g2, base, winX, bodyY, leftW, statusBarY);
        drawRightPanel(g2, base, winX, winW, bodyY, divX, statusBarY);
        drawStatusBar (g2, base, winX, winY, winW, statusBarY);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────

    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("QUESTS", winX + 28, winY + 32);

        long completed = QuestSystem.getInstance().getAll().stream()
                .filter(Quest::isCompleted).count();
        String counter = completed + " / 30";
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(216, 184, 88));
        g2.drawString(counter, winX + winW - 16 - fm.stringWidth(counter), winY + 32);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    // ── Left panel ────────────────────────────────────────────────────────────

    private void drawLeftPanel(Graphics2D g2, Font base,
                               int winX, int bodyY, int leftW, int statusBarY) {
        List<Quest> all = QuestSystem.getInstance().getAll();
        Quest sel = all.isEmpty() ? null : all.get(cursor);

        int panelX      = winX + OUTER_PAD;
        int panelW      = leftW - OUTER_PAD - 10;
        int panelBottom = statusBarY - 8;

        // Image card
        int imgCardY = bodyY + 8;
        int imgCardH = 150;
        drawCard(g2, panelX, imgCardY, panelW, imgCardH, 8);

        if (sel != null) {
            BufferedImage img = loadImg(sel);
            int m = 10;
            if (img != null)
                g2.drawImage(img, panelX + m, imgCardY + m,
                        panelW - m * 2, imgCardH - m * 2, null);
            else
                drawCentred(g2, base, panelX, imgCardY, panelW, imgCardH, "[img]");
        }

        // Detail card — fills remaining space
        int detCardY = imgCardY + imgCardH + 6;
        int detCardH = panelBottom - detCardY;
        if (detCardH < 24) return;
        drawCard(g2, panelX, detCardY, panelW, detCardH, 8);

        if (sel == null) return;

        Shape prev = g2.getClip();
        g2.setClip(panelX + 4, detCardY + 4, panelW - 8, detCardH - 8);

        int tx = panelX + 14;
        int tw = panelW - 20;
        int ty = detCardY + 24;

        // Name
        g2.setFont(base.deriveFont(Font.BOLD, 13f));
        ty = drawWordWrapped(g2, g2.getFontMetrics(),
                sel.getDisplayName(), tx, ty, tw, 15, new Color(44, 44, 42));
        ty += 6;

        // Description
        g2.setFont(base.deriveFont(10f));
        ty = drawWordWrapped(g2, g2.getFontMetrics(),
                sel.getDisplayDescription(), tx, ty, tw, 13, new Color(88, 84, 76));
        ty += 2;

        // Progress
        if (sel.isCounterBased() && !sel.isCompleted()) {
            int barW = tw - 8, barH = 8;
            g2.setColor(new Color(200, 196, 186));
            g2.fillRoundRect(tx, ty, barW, barH, 3, 3);
            int fillW = (int)(barW * sel.getProgressFraction());
            if (fillW > 0) {
                g2.setColor(new Color(0, 160, 220));
                g2.fillRoundRect(tx, ty, fillW, barH, 3, 3);
            }
            g2.setColor(new Color(160, 155, 145));
            g2.drawRoundRect(tx, ty, barW, barH, 3, 3);
            ty += barH + 14;
            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(100, 96, 90));
            g2.drawString(sel.getProgressText(), tx, ty);
            ty += 14;
        }

        // Divider before reward
        g2.setColor(new Color(200, 195, 180));
        g2.drawLine(tx, ty, tx + tw, ty);
        ty += 18;

        // Reward label
        g2.setFont(base.deriveFont(Font.BOLD, 10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("REWARD", tx, ty);
        ty += 16;

        // Reward value
        g2.setFont(base.deriveFont(Font.BOLD, 11f));
        ty = drawWordWrapped(g2, g2.getFontMetrics(),
                sel.getRewardText(), tx, ty, tw, 14, new Color(216, 184, 88));
        ty += 2;

        // Claim hint
        if (sel.isCompleted() && !sel.isRewardClaimed()) {
            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(60, 180, 80));
            g2.drawString("Press ENTER to claim", tx, ty);
        }

        g2.setClip(prev);
    }

    // ── Right panel ───────────────────────────────────────────────────────────

    private void drawRightPanel(Graphics2D g2, Font base,
                                int winX, int winW, int bodyY,
                                int divX, int statusBarY) {
        List<Quest> all   = QuestSystem.getInstance().getAll();
        int         total = all.size();

        int listX       = divX + OUTER_PAD;
        int listW       = winX + winW - listX - OUTER_PAD;
        int labelY      = bodyY + 16;
        int firstRowY   = labelY + 8;
        int listAreaBot = statusBarY - 8;
        int listAreaH   = listAreaBot - firstRowY;
        int visCount    = Math.max(1, listAreaH / ROW_H);

        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("ALL QUESTS", listX, labelY);

        String ctr = (cursor + 1) + " / " + total;
        FontMetrics cfm = g2.getFontMetrics();
        g2.drawString(ctr, listX + listW - cfm.stringWidth(ctr), labelY);

        clampScroll(visCount);

        Shape prev = g2.getClip();
        g2.setClip(listX - 6, firstRowY, listW + 12, listAreaH);

        int endIdx = Math.min(scrollOffset + visCount, total);

        for (int i = scrollOffset; i < endIdx; i++) {
            Quest   q       = all.get(i);
            int     rowTop  = firstRowY + (i - scrollOffset) * ROW_H;
            int     textY   = rowTop + ROW_H - 10;
            boolean hovered = (i == cursor);

            if (hovered) {
                g2.setColor(new Color(178, 212, 244, 200));
                g2.fillRoundRect(listX - 4, rowTop, listW + 4, ROW_H - 2, 5, 5);
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX - 4, rowTop, listW + 4, ROW_H - 2, 5, 5);
                g2.setStroke(new BasicStroke(1));

                int ts = 7, cx = listX + 2, cy = rowTop + ROW_H / 2 - 1;
                g2.setColor(new Color(80, 76, 70));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillPolygon(new int[]{ cx, cx, cx + ts },
                        new int[]{ cy - ts, cy + ts, cy }, 3);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            // Status dot
            int dotX = listX + 14;
            int dotY = rowTop + ROW_H / 2 - 4;
            if (q.isCompleted() && q.isRewardClaimed()) {
                // Green — fully done
                g2.setColor(new Color(60, 180, 80));
                g2.fillOval(dotX, dotY, 8, 8);
            } else if (q.isCompleted()) {
                // Gold — completed, reward not claimed
                g2.setColor(new Color(216, 184, 88));
                g2.fillOval(dotX, dotY, 8, 8);
            } else {
                // Empty — not done
                g2.setColor(new Color(180, 175, 165));
                g2.drawOval(dotX, dotY, 8, 8);
            }

            // Quest name
            g2.setFont(base.deriveFont(hovered ? Font.BOLD : Font.PLAIN, 11f));
            g2.setColor(q.isCompleted()
                    ? new Color(44, 44, 42)
                    : new Color(140, 136, 128));
            int nameMaxW = listW - 80;
            g2.drawString(truncate(q.getDisplayName(), g2.getFontMetrics(), nameMaxW),
                    listX + 28, textY);

            // Right-side label
            g2.setFont(base.deriveFont(Font.BOLD, 9f));
            FontMetrics fm = g2.getFontMetrics();
            if (q.isCompleted() && !q.isRewardClaimed()) {
                // Gold CLAIM
                g2.setColor(new Color(216, 184, 88));
                g2.drawString("CLAIM", listX + listW - fm.stringWidth("CLAIM") - 6, textY);
            } else if (q.isCompleted()) {
                // Green Done
                g2.setColor(new Color(60, 180, 80));
                g2.drawString("DONE", listX + listW - fm.stringWidth("DONE") - 6, textY);
            } else if (q.isCounterBased()) {
                // Progress counter
                g2.setFont(base.deriveFont(9f));
                fm = g2.getFontMetrics();
                String prog = q.getProgressText();
                g2.setColor(new Color(100, 96, 90));
                g2.drawString(prog, listX + listW - fm.stringWidth(prog) - 6, textY);
            }

            if (i < endIdx - 1) {
                g2.setColor(new Color(205, 200, 190));
                g2.drawLine(listX, rowTop + ROW_H - 1,
                        listX + listW, rowTop + ROW_H - 1);
            }
        }

        g2.setClip(prev);

        // Scroll hints
        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(140, 136, 128));
        int arrowX = listX + listW / 2;
        if (scrollOffset > 0)
            g2.drawString("^ more", arrowX - 16, firstRowY - 2);
        if (scrollOffset + visCount < total) {
            int hintY = firstRowY + visCount * ROW_H + 10;
            if (hintY < listAreaBot + 14)
                g2.drawString("v more", arrowX - 16, hintY);
        }
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g2, Font base,
                               int winX, int winY, int winW, int statusBarY) {
        int barX = winX + 8, barW = winW - 16;
        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, statusBarY, barW, STATUS_BAR_H, 5, 5);

        // Nav hints right
        String hint = "WS Move  ENT Claim  ESC Close";
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, barX + barW - 12 - fm.stringWidth(hint), statusBarY + 25);

        // Status message left
        if (!statusMessage.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(statusMessage, barX + 14, statusBarY + 25);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void clampScroll(int visCount) {
        if (cursor < scrollOffset) scrollOffset = cursor;
        if (cursor >= scrollOffset + visCount) scrollOffset = cursor - visCount + 1;
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

    private void drawCentred(Graphics2D g2, Font base,
                             int bx, int by, int bw, int bh, String t) {
        g2.setFont(base.deriveFont(10f));
        g2.setColor(new Color(150, 145, 138));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, bx + (bw - fm.stringWidth(t)) / 2, by + bh / 2 + 4);
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "...") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }

    private BufferedImage loadImg(Quest q) {
        return imgCache.computeIfAbsent(q.getId(),
                k -> AssetManager.loadImage(q.getAssetPath()));
    }
}