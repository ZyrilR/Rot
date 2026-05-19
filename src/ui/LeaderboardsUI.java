package ui;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static utils.Constants.*;

/**
 * Leaderboards screen — ranks completed runs by total in-game time
 * to defeat Sir Khai (lower is better).
 *
 * Persisted to: src/res/Saves/leaderboards.txt
 *   Each line: "worldName|ticks"
 */
public class LeaderboardsUI {

    // ── File location ─────────────────────────────────────────────────────────

    private static final String LB_PATH = "src/res/Saves/leaderboards.txt";

    // ── Entry ─────────────────────────────────────────────────────────────────

    public static class Entry {
        public final String worldName;
        public final long   ticks;
        public Entry(String name, long ticks) { this.worldName = name; this.ticks = ticks; }
    }

    // ── Persistence API ───────────────────────────────────────────────────────

    /** Append a new completion entry. Creates the file if missing. */
    public static void recordCompletion(String worldName, long ticks) {
        if (worldName == null || worldName.isBlank()) worldName = "Unknown";
        File f = new File(LB_PATH);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileWriter fw = new FileWriter(f, true)) {
            fw.write(worldName.replace("|", "_").trim() + "|" + ticks + "\n");
            System.out.println("[Leaderboards] Recorded: " + worldName + " — " + ticks + " ticks");
        } catch (IOException e) {
            System.err.println("[Leaderboards] Failed to record completion: " + e.getMessage());
        }
    }

    public static List<Entry> readAllSorted() {
        List<Entry> entries = new ArrayList<>();
        File f = new File(LB_PATH);
        if (!f.exists()) return entries;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int sep = line.indexOf('|');
                if (sep < 0) continue;
                String name = line.substring(0, sep).trim();
                long ticks;
                try { ticks = Long.parseLong(line.substring(sep + 1).trim()); }
                catch (NumberFormatException e) { continue; }
                entries.add(new Entry(name, ticks));
            }
        } catch (IOException e) {
            System.err.println("[Leaderboards] Read failed: " + e.getMessage());
        }
        entries.sort(Comparator.comparingLong(e -> e.ticks));
        return entries;
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private static final int ROW_H        = 56;
    private static final int VISIBLE_ROWS = 7;

    private final GamePanel gp;
    private final List<Entry> entries = new ArrayList<>();

    private BufferedImage splashBg;
    private int     cursor        = 0;
    private int     scrollOffset  = 0;
    private int     inputCooldown = 0;

    public LeaderboardsUI(GamePanel gp) {
        this.gp = gp;
        this.splashBg = AssetManager.loadImage("/res/SplashScreen/ROT.png");
    }

    public void open() {
        gp.GAMESTATE  = "leaderboards";
        cursor        = 0;
        scrollOffset  = 0;
        inputCooldown = INPUT_DELAY * 2;
        entries.clear();
        entries.addAll(readAllSorted());
        System.out.println("[LeaderboardsUI] Loaded " + entries.size() + " entries.");
    }

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        if (gp.KEYBOARDHANDLER.escPressed) {
            gp.KEYBOARDHANDLER.escPressed = false;
            gp.SPLASHSCREEN.open();
            gp.GAMESTATE = "splash";
            inputCooldown = INPUT_DELAY;
            return;
        }

        if (gp.KEYBOARDHANDLER.upPressed && cursor > 0) {
            cursor--;
            clampScroll();
            inputCooldown = INPUT_DELAY;
        } else if (gp.KEYBOARDHANDLER.downPressed && cursor < entries.size() - 1) {
            cursor++;
            clampScroll();
            inputCooldown = INPUT_DELAY;
        }
    }

    private void clampScroll() {
        if (cursor < scrollOffset) scrollOffset = cursor;
        if (cursor >= scrollOffset + VISIBLE_ROWS) scrollOffset = cursor - VISIBLE_ROWS + 1;
        if (scrollOffset < 0) scrollOffset = 0;
    }

    private String formatTime(long ticks) {
        long seconds = ticks / Math.max(1, FPS);
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        if (splashBg != null) {
            g2.drawImage(splashBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else {
            g2.setColor(new Color(20, 18, 14));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int winX = TILE_SIZE,     winY = TILE_SIZE / 2;
        int winW = SCREEN_WIDTH  - TILE_SIZE * 2;
        int winH = SCREEN_HEIGHT - TILE_SIZE;
        drawWindow(g2, winX, winY, winW, winH);

        drawTitleBar(g2, base, winX, winY, winW);

        int bodyY      = winY + 52;
        int statusBarY = winY + winH - STATUS_BAR_H - 8;

        drawList(g2, base, winX, winW, bodyY, statusBarY);
        drawStatusBar(g2, base, winX, winW, statusBarY);
    }

    private void drawTitleBar(Graphics2D g2, Font base, int winX, int winY, int winW) {
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(winX + 8, winY + 8, winW - 16, 36, 8, 8);

        g2.setFont(base.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("LEADERBOARDS", winX + 28, winY + 32);

        String counter = entries.size() + " run" + (entries.size() == 1 ? "" : "s");
        g2.setFont(base.deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(216, 184, 88));
        g2.drawString(counter, winX + winW - 16 - fm.stringWidth(counter), winY + 32);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(winX + 8, winY + 46, winX + winW - 8, winY + 46);
    }

    private void drawList(Graphics2D g2, Font base,
                          int winX, int winW, int bodyY, int statusBarY) {
        int listX = winX + 14;
        int listW = winW - 28;
        int areaH = statusBarY - bodyY - 16;

        Shape prev = g2.getClip();
        g2.setClip(listX, bodyY, listW, areaH);

        if (entries.isEmpty()) {
            g2.setFont(base.deriveFont(10f));
            g2.setColor(new Color(140, 136, 128));
            String msg = "No runs recorded yet. Defeat Sir Khai to claim the top spot!";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg,
                    listX + (listW - fm.stringWidth(msg)) / 2,
                    bodyY + areaH / 2);
            g2.setClip(prev);
            return;
        }

        int endIdx = Math.min(scrollOffset + VISIBLE_ROWS, entries.size());
        for (int i = scrollOffset; i < endIdx; i++) {
            Entry e   = entries.get(i);
            int rowTop = bodyY + (i - scrollOffset) * ROW_H;
            boolean hovered = (i == cursor);

            Color rowBg = hovered ? new Color(178, 212, 244, 180) : new Color(230, 226, 218);
            g2.setColor(rowBg);
            g2.fillRoundRect(listX, rowTop + 2, listW, ROW_H - 4, 8, 8);
            if (hovered) {
                g2.setColor(new Color(24, 95, 165));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(listX, rowTop + 2, listW, ROW_H - 4, 8, 8);
                g2.setStroke(new BasicStroke(1));
            }

            // Rank medallion
            int medalSize = 36;
            int medalX = listX + 12;
            int medalY = rowTop + (ROW_H - medalSize) / 2;
            Color medalFill;
            switch (i) {
                case 0  -> medalFill = new Color(216, 184, 88);   // gold
                case 1  -> medalFill = new Color(180, 180, 188);  // silver
                case 2  -> medalFill = new Color(176, 124, 76);   // bronze
                default -> medalFill = new Color(120, 116, 108);
            }
            g2.setColor(medalFill);
            g2.fillOval(medalX, medalY, medalSize, medalSize);
            g2.setColor(new Color(60, 55, 45));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(medalX, medalY, medalSize, medalSize);
            g2.setStroke(new BasicStroke(1));

            String rank = "#" + (i + 1);
            g2.setFont(base.deriveFont(Font.BOLD, 11f));
            FontMetrics rfm = g2.getFontMetrics();
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(rank,
                    medalX + (medalSize - rfm.stringWidth(rank)) / 2,
                    medalY + (medalSize + rfm.getAscent()) / 2 - 4);

            // Name + time
            int tx = medalX + medalSize + 14;
            g2.setFont(base.deriveFont(Font.BOLD, 12f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(truncate(e.worldName, g2.getFontMetrics(), listW - (tx - listX) - 160),
                    tx, rowTop + 24);

            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(100, 96, 90));
            g2.drawString("Run time", tx, rowTop + 42);

            // Time on the right
            String time = formatTime(e.ticks);
            g2.setFont(base.deriveFont(Font.BOLD, 14f));
            FontMetrics tfm = g2.getFontMetrics();
            g2.setColor(new Color(24, 95, 165));
            g2.drawString(time,
                    listX + listW - 16 - tfm.stringWidth(time),
                    rowTop + 34);
        }
        g2.setClip(prev);

        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(140, 136, 128));
        if (scrollOffset > 0)
            g2.drawString("^ more", winX + winW / 2 - 16, bodyY - 2);
        if (scrollOffset + VISIBLE_ROWS < entries.size())
            g2.drawString("v more", winX + winW / 2 - 16, statusBarY - 4);
    }

    private void drawStatusBar(Graphics2D g2, Font base,
                               int winX, int winW, int statusBarY) {
        int barX = winX + 8, barW = winW - 16;
        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, statusBarY, barW, STATUS_BAR_H, 5, 5);

        String hint = "UP/DOWN Scroll   ESC Back";
        g2.setFont(base.deriveFont(7f));
        g2.setColor(new Color(120, 116, 108));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint,
                barX + barW - 12 - fm.stringWidth(hint),
                statusBarY + 25);
    }

    private void drawWindow(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 16;
        g2.setColor(new Color(245, 242, 235));  g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(6));  g2.setColor(new Color(80, 80, 80));   g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(4));  g2.setColor(new Color(216, 184, 88)); g2.drawRoundRect(x+1, y+1, w-2, h-2, arc, arc);
        g2.setStroke(new BasicStroke(2));  g2.setColor(new Color(80, 80, 80));   g2.drawRoundRect(x+4, y+4, w-8, h-8, arc-4, arc-4);
        g2.setStroke(new BasicStroke(1));
    }

    private String truncate(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "...") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }
}
