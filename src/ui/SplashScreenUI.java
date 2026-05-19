package ui;

import engine.GamePanel;
import utils.AssetManager;
import utils.AudioManager;
import utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static utils.Constants.*;

public class SplashScreenUI {

    private enum State { MAIN, CREDITS }
    private State state = State.MAIN;

    private final GamePanel gp;
    private BufferedImage splashBg;

    private final Color COLOR_MAROON = new Color(85, 14, 14); // #550E0E
    private final Color COLOR_TEXT_UNSELECTED = new Color(240, 240, 240);

    private static final String[] BUTTON_LABELS = { "PLAY", "LEADERBOARDS", "CREDITS", "QUIT" };
    private int cursor = 0;
    private int inputCooldown = 0;
    private int tick = 0;

    public SplashScreenUI(GamePanel gp) {
        this.gp = gp;
        loadBackground();
    }

    private void loadBackground() {
        try {
            // Using the path you specified
            File file = new File("src/res/SplashScreen/ROT.png");
            splashBg = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("Error: Could not load splash image at path provided.");
            e.printStackTrace();
        }
    }

    public void open() {
        state = State.MAIN;
        cursor = 0;
        inputCooldown = INPUT_DELAY * 2;

        AudioManager.playMusic(Constants.SND_SPLASH, true);
    }

    public void update() {
        tick++;
        if (inputCooldown > 0) { inputCooldown--; return; }

        if (state == State.CREDITS) {
            if (gp.KEYBOARDHANDLER.escPressed || gp.KEYBOARDHANDLER.enterPressed) {
                gp.KEYBOARDHANDLER.escPressed = false;
                gp.KEYBOARDHANDLER.enterPressed = false;
                state = State.MAIN;
                inputCooldown = INPUT_DELAY;
            }
            return;
        }

        if (gp.KEYBOARDHANDLER.upPressed && cursor > 0) {
            cursor--;
            inputCooldown = INPUT_DELAY;
        } else if (gp.KEYBOARDHANDLER.downPressed && cursor < BUTTON_LABELS.length - 1) {
            cursor++;
            inputCooldown = INPUT_DELAY;
        }

        if (gp.KEYBOARDHANDLER.enterPressed) {
            gp.KEYBOARDHANDLER.enterPressed = false;
            handleSelection();
            inputCooldown = INPUT_DELAY;
        }
    }

    private void handleSelection() {
        switch (cursor) {
            case 0 -> gp.WORLDSELECTUI.open();
            case 1 -> gp.LEADERBOARDSUI.open();
            case 2 -> state = State.CREDITS;
            case 3 -> System.exit(0);
        }
    }

    public void draw(Graphics2D g2) {

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Draw Background Image
        if (splashBg != null) {
            g2.drawImage(splashBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // Define the base font for the UI
        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        if (state == State.CREDITS) {
            // Pass the font into the new credits method
            drawCredits(g2, base);
        } else {
            drawMenu(g2);
        }
    }

    private void drawMenu(Graphics2D g2) {
        Font menuFont = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb.deriveFont(Font.BOLD, 14f)
                : new Font("SansSerif", Font.BOLD, 14);

        int btnW  = 220;
        int btnH  = 44;
        int startY = (int)(SCREEN_HEIGHT * 0.54);
        int btnX  = (SCREEN_WIDTH - btnW) / 2;

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            boolean sel  = (i == cursor);
            int     btnY = startY + i * (btnH + 12);

            // Fill
            g2.setColor(sel ? new Color(44, 44, 42) : new Color(60, 58, 54, 200));
            g2.fillRoundRect(btnX, btnY, btnW, btnH, 8, 8);

            // Border — gold on selected, dim on unselected
            if (sel) {
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(216, 184, 88));
                g2.drawRoundRect(btnX, btnY, btnW, btnH, 8, 8);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(80, 80, 80));
                g2.drawRoundRect(btnX + 2, btnY + 2, btnW - 4, btnH - 4, 6, 6);
                g2.setStroke(new BasicStroke(1));
            } else {
                g2.setStroke(new BasicStroke(1));
                g2.setColor(new Color(100, 96, 90));
                g2.drawRoundRect(btnX, btnY, btnW, btnH, 8, 8);
            }

            g2.setFont(menuFont);
            FontMetrics fm = g2.getFontMetrics();
            String label = BUTTON_LABELS[i];
            int tx = btnX + (btnW - fm.stringWidth(label)) / 2;
            int ty = btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent() + 4;

            g2.setColor(sel ? new Color(241, 239, 232) : new Color(160, 156, 148));
            g2.drawString(label, tx, ty);
        }
    }

    // ── Restored Detailed Credits screen ──────────────────────────────────────

    private void drawCredits(Graphics2D g2, Font base) {

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int panelW = 500, panelH = 480;
        int panelX = (SCREEN_WIDTH  - panelW) / 2;
        int panelY = (SCREEN_HEIGHT - panelH) / 2;

        drawWindow(g2, panelX, panelY, panelW, panelH);

        // ── Title bar ──────────────────────────────────────────────────────
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(panelX + 8, panelY + 8, panelW - 16, 36, 8, 8);
        g2.setFont(base.deriveFont(Font.BOLD, 14f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("CREDITS", panelX + 28, panelY + 32);
        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(panelX + 8, panelY + 46, panelX + panelW - 8, panelY + 46);

        int innerX  = panelX + 24;
        int innerW  = panelW - 48;
        int ty      = panelY + 66;
        int lineH   = 20;
        int sectionGap = 10;

        // ── Helper: section header ─────────────────────────────────────────
        // drawn inline below

        // ── TEAM ──────────────────────────────────────────────────────────
        // Section label
        g2.setFont(base.deriveFont(Font.BOLD, 9f));
        g2.setColor(new Color(216, 184, 88));
        String teamLabel = "TEAM BRAINROT TRAINERS";
        FontMetrics fm = g2.getFontMetrics();
        int labelW = fm.stringWidth(teamLabel);
        int lineY  = ty + fm.getAscent() / 2;
        int gap    = 8;
        g2.drawLine(innerX, lineY, innerX + (innerW - labelW) / 2 - gap, lineY);
        g2.drawString(teamLabel, innerX + (innerW - labelW) / 2, ty + fm.getAscent());
        g2.drawLine(innerX + (innerW + labelW) / 2 + gap, lineY, innerX + innerW, lineY);
        ty += fm.getHeight() + 8;

        // Members — two columns
        String[][] members = {
                { "Caipang, Chrisnel Graine",  "Developer" },
                { "Restauro, Zyril Ryle",      "Developer" },
                { "Din, Jon Vincent",          "Developer" },
                { "Dira, Luther Derrick",      "Developer" },
                { "Castro, Kyle Angelo",       "Developer" },
        };
        for (String[] m : members) {
            // Name
            g2.setFont(base.deriveFont(Font.BOLD, 10f));
            g2.setColor(new Color(44, 44, 42));
            g2.drawString(m[0], innerX + 14, ty + lineH - 4);
            // Role badge
            g2.setFont(base.deriveFont(8f));
            FontMetrics bfm = g2.getFontMetrics();
            int badgeW = bfm.stringWidth(m[1]) + 12;
            int badgeH = 14;
            int badgeX = innerX + innerW - badgeW;
            int badgeY = ty + (lineH - badgeH) / 2;
            g2.setColor(new Color(216, 184, 88, 60));
            g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 4, 4);
            g2.setColor(new Color(150, 120, 40));
            g2.setStroke(new java.awt.BasicStroke(1));
            g2.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 4, 4);
            g2.setColor(new Color(120, 96, 30));
            g2.drawString(m[1], badgeX + 6, badgeY + 11);
            ty += lineH + 2;
        }
        ty += sectionGap;

        // ── ASSETS ────────────────────────────────────────────────────────
        g2.setFont(base.deriveFont(Font.BOLD, 9f));
        g2.setColor(new Color(216, 184, 88));
        String assetsLabel = "ASSETS";
        fm = g2.getFontMetrics();
        labelW = fm.stringWidth(assetsLabel);
        lineY  = ty + fm.getAscent() / 2;
        g2.drawLine(innerX, lineY, innerX + (innerW - labelW) / 2 - gap, lineY);
        g2.drawString(assetsLabel, innerX + (innerW - labelW) / 2, ty + fm.getAscent());
        g2.drawLine(innerX + (innerW + labelW) / 2 + gap, lineY, innerX + innerW, lineY);
        ty += fm.getHeight() + 8;

        String[][] assets = {
                { "Font",  "PokemonGB(RAeo)" },
                { "Tiles", "tuxemon.org" },
        };
        for (String[] a : assets) {
            g2.setFont(base.deriveFont(9f));
            g2.setColor(new Color(120, 116, 108));
            g2.drawString(a[0], innerX + 14, ty + lineH - 4);
            g2.setFont(base.deriveFont(9f));
            FontMetrics rfm = g2.getFontMetrics();
            g2.setColor(new Color(200, 196, 185));
            g2.drawString(a[1], innerX + innerW - rfm.stringWidth(a[1]), ty + lineH - 4);
            ty += lineH + 2;
        }
        ty += sectionGap;

        // ── TECHNOLOGIES ──────────────────────────────────────────────────
        g2.setFont(base.deriveFont(Font.BOLD, 9f));
        g2.setColor(new Color(216, 184, 88));
        String techLabel = "TECHNOLOGIES";
        fm = g2.getFontMetrics();
        labelW = fm.stringWidth(techLabel);
        lineY  = ty + fm.getAscent() / 2;
        g2.drawLine(innerX, lineY, innerX + (innerW - labelW) / 2 - gap, lineY);
        g2.drawString(techLabel, innerX + (innerW - labelW) / 2, ty + fm.getAscent());
        g2.drawLine(innerX + (innerW + labelW) / 2 + gap, lineY, innerX + innerW, lineY);
        ty += fm.getHeight() + 8;

        g2.setFont(base.deriveFont(9f));
        g2.setColor(new Color(120, 116, 108));
        g2.drawString("Engine", innerX + 14, ty + lineH - 4);
        fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 196, 185));
        g2.drawString("Java / Java Swing / Java 2D", innerX + innerW - fm.stringWidth("Java / Java Swing / Java 2D"), ty + lineH - 4);
        ty += lineH + 2 + sectionGap;

        // ── SPECIAL THANKS ────────────────────────────────────────────────
        g2.setFont(base.deriveFont(Font.BOLD, 9f));
        g2.setColor(new Color(216, 184, 88));
        String thanksLabel = "SPECIAL THANKS TO";
        fm = g2.getFontMetrics();
        labelW = fm.stringWidth(thanksLabel);
        lineY  = ty + fm.getAscent() / 2;
        g2.drawLine(innerX, lineY, innerX + (innerW - labelW) / 2 - gap, lineY);
        g2.drawString(thanksLabel, innerX + (innerW - labelW) / 2, ty + fm.getAscent());
        g2.drawLine(innerX + (innerW + labelW) / 2 + gap, lineY, innerX + innerW, lineY);
        ty += fm.getHeight() + 8;

        String[] thanksRow1 = { "Sir Khai" };
        String[] thanks = { "Claude", "ChatGPT", "Gemini" };
        // Render as centered pills in one row
        g2.setFont(base.deriveFont(9f));
        fm = g2.getFontMetrics();
        int pillH = 16, pillPadX = 10, pillGap = 6;

        for (String[] row : new String[][]{ thanksRow1, thanks }) {
            int rowTotalW = 0;
            for (String t : row) rowTotalW += fm.stringWidth(t) + pillPadX * 2 + pillGap;
            rowTotalW -= pillGap;
            int pillStartX = innerX + (innerW - rowTotalW) / 2;
            for (String t : row) {
                int pw = fm.stringWidth(t) + pillPadX * 2;
                g2.setColor(new Color(230, 226, 218));
                g2.fillRoundRect(pillStartX, ty, pw, pillH, 8, 8);
                g2.setColor(new Color(190, 185, 172));
                g2.drawRoundRect(pillStartX, ty, pw, pillH, 8, 8);
                g2.setColor(new Color(80, 76, 70));
                g2.drawString(t, pillStartX + pillPadX, ty + 12);
                pillStartX += pw + pillGap;
            }
            ty += pillH + 4; // gap between rows
        }
        ty += sectionGap - 4;

        // ── INSPIRED BY ───────────────────────────────────────────────────
        g2.setFont(base.deriveFont(Font.BOLD, 9f));
        g2.setColor(new Color(216, 184, 88));
        String inspLabel = "INSPIRED BY";
        fm = g2.getFontMetrics();
        labelW = fm.stringWidth(inspLabel);
        lineY  = ty + fm.getAscent() / 2;
        g2.drawLine(innerX, lineY, innerX + (innerW - labelW) / 2 - gap, lineY);
        g2.drawString(inspLabel, innerX + (innerW - labelW) / 2, ty + fm.getAscent());
        g2.drawLine(innerX + (innerW + labelW) / 2 + gap, lineY, innerX + innerW, lineY);
        ty += fm.getHeight() + 8;

        String[] inspired = { "Pokemon", "Italian Brainrot Memes" };
        g2.setFont(base.deriveFont(9f));
        fm = g2.getFontMetrics();
        int inspTotalW = 0;
        for (String s : inspired) inspTotalW += fm.stringWidth(s) + pillPadX * 2 + pillGap;
        inspTotalW -= pillGap;
        int pillStartX = innerX + (innerW - inspTotalW) / 2;
        for (String s : inspired) {
            int pw = fm.stringWidth(s) + pillPadX * 2;
            g2.setColor(new Color(230, 226, 218));
            g2.fillRoundRect(pillStartX, ty, pw, pillH, 8, 8);
            g2.setColor(new Color(190, 185, 172));
            g2.drawRoundRect(pillStartX, ty, pw, pillH, 8, 8);
            g2.setColor(new Color(80, 76, 70));
            g2.drawString(s, pillStartX + pillPadX, ty + 12);
            pillStartX += pw + pillGap;
        }
        ty += pillH + sectionGap;

    }

    // ── Restored Window Helper ────────────────────────────────────────────────

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


}