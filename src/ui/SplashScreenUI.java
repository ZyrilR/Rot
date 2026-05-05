package ui;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static utils.Constants.*;

/**
 * Splash / title screen shown at game startup.
 *
 * States:
 *   MAIN   — title + three buttons (PLAY, CREDITS, QUIT)
 *   CREDITS — simple overlay; ESC or BACK closes it
 *
 * Controls (MAIN):
 *   W / S / UP / DOWN — move cursor
 *   ENTER             — confirm
 *
 * Controls (CREDITS):
 *   ESC / ENTER       — back
 */
public class SplashScreenUI {

    // ── State ─────────────────────────────────────────────────────────────────

    private enum State { MAIN, CREDITS }
    private State state = State.MAIN;

    private final GamePanel gp;

    // ── Button layout ─────────────────────────────────────────────────────────

    private static final String[] BUTTON_LABELS = { "PLAY", "CREDITS", "QUIT" };
    private int cursor        = 0;
    private int inputCooldown = 0;

    // ── Animation ─────────────────────────────────────────────────────────────

    private int tick = 0; // used for subtle pulse on cursor

    public SplashScreenUI(GamePanel gp) {
        this.gp = gp;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        state         = State.MAIN;
        cursor        = 0;
        inputCooldown = INPUT_DELAY * 2;
        tick          = 0;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        tick++;
        if (inputCooldown > 0) { inputCooldown--; return; }

        if (state == State.CREDITS) {
            if (gp.KEYBOARDHANDLER.escPressed || gp.KEYBOARDHANDLER.enterPressed) {
                gp.KEYBOARDHANDLER.escPressed   = false;
                gp.KEYBOARDHANDLER.enterPressed = false;
                state         = State.MAIN;
                inputCooldown = INPUT_DELAY;
            }
            return;
        }

        // ── MAIN state ────────────────────────────────────────────────────────
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
            case 0 -> {   // PLAY
                gp.WORLDSELECTUI.open();
            }
            case 1 -> {   // CREDITS
                state = State.CREDITS;
            }
            case 2 -> {   // QUIT
                System.exit(0);
            }
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

        // ── Background ───────────────────────────────────────────────────────
        g2.setColor(new Color(20, 18, 14));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (state == State.CREDITS) {
            drawCredits(g2, base);
            return;
        }

        drawMain(g2, base);
    }

    // ── Main screen ───────────────────────────────────────────────────────────

    private void drawMain(Graphics2D g2, Font base) {

        // ── Title ─────────────────────────────────────────────────────────────
        int titleY = SCREEN_HEIGHT / 3;

        g2.setFont(base.deriveFont(Font.BOLD, 64f));
        FontMetrics titleFm = g2.getFontMetrics();
        String title = "ROT";
        int titleX = (SCREEN_WIDTH - titleFm.stringWidth(title)) / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(title, titleX + 4, titleY + 4);

        // Gold fill
        g2.setColor(new Color(216, 184, 88));
        g2.drawString(title, titleX, titleY);

        // Subtitle
        g2.setFont(base.deriveFont(12f));
        FontMetrics subFm = g2.getFontMetrics();
        String sub = "A BrainRot Adventure";
        g2.setColor(new Color(160, 155, 140));
        g2.drawString(sub, (SCREEN_WIDTH - subFm.stringWidth(sub)) / 2, titleY + 28);

        // Thin gold separator
        int sepY = titleY + 50;
        g2.setColor(new Color(216, 184, 88, 120));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(SCREEN_WIDTH / 2 - 80, sepY, SCREEN_WIDTH / 2 + 80, sepY);

        // ── Buttons ───────────────────────────────────────────────────────────
        int btnW   = 200;
        int btnH   = 36;
        int btnGap = 14;
        int totalH = BUTTON_LABELS.length * btnH + (BUTTON_LABELS.length - 1) * btnGap;
        int startY = SCREEN_HEIGHT / 2 + 20;
        int btnX   = (SCREEN_WIDTH - btnW) / 2;

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            int btnY   = startY + i * (btnH + btnGap);
            boolean sel = (i == cursor);

            // Button background
            Color bgColor = sel ? new Color(216, 184, 88) : new Color(44, 42, 38);
            g2.setColor(bgColor);
            g2.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);

            // Border
            g2.setStroke(new BasicStroke(sel ? 2 : 1));
            g2.setColor(sel ? new Color(255, 230, 120) : new Color(80, 76, 68));
            g2.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);
            g2.setStroke(new BasicStroke(1));

            // Label
            g2.setFont(base.deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            String label = BUTTON_LABELS[i];
            int lx = btnX + (btnW - fm.stringWidth(label)) / 2;
            int ly = btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(sel ? new Color(44, 44, 42) : new Color(200, 196, 185));
            g2.drawString(label, lx, ly + 4);

            // Cursor triangle (left of selected button)
            if (sel) {
                int pulse   = (int)(Math.sin(tick * 0.12) * 2);
                int ts      = 7;
                int cx      = btnX - 16 + pulse;
                int cy      = btnY + btnH / 2;
                g2.setColor(new Color(216, 184, 88));
                g2.fillPolygon(
                        new int[]{ cx, cx, cx + ts },
                        new int[]{ cy - ts, cy + ts, cy }, 3);
            }
        }

        // ── Footer hint ───────────────────────────────────────────────────────
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(80, 76, 68));
        String hint = "W/S Move   ENTER Confirm";
        FontMetrics hfm = g2.getFontMetrics();
        g2.drawString(hint, (SCREEN_WIDTH - hfm.stringWidth(hint)) / 2,
                SCREEN_HEIGHT - 20);
    }

    // ── Credits screen ────────────────────────────────────────────────────────

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
                { "Caipang, Chrisnel Graine",  "Programmer" },
                { "Restauro, Zyril Ryle",      "Programmer" },
                { "Din, Jon Vincent",          "Programmer" },
                { "Dira, Luther Derrick",      "Programmer" },
                { "Castro, Kyle Angelo",       "Programmer" },
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
                { "Font",  "PokemonGB (RAeo)" },
                { "Tiles", "Placeholder assets" },
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

        String[] thanks = { "Sir Khai", "Claude", "ChatGPT", "Gemini" };
        // Render as centered pills in one row
        g2.setFont(base.deriveFont(9f));
        fm = g2.getFontMetrics();
        int pillH = 16, pillPadX = 10, pillGap = 6;
        int totalPillW = 0;
        for (String t : thanks) totalPillW += fm.stringWidth(t) + pillPadX * 2 + pillGap;
        totalPillW -= pillGap;
        int pillStartX = innerX + (innerW - totalPillW) / 2;
        int pillY = ty;
        for (String t : thanks) {
            int pw = fm.stringWidth(t) + pillPadX * 2;
            g2.setColor(new Color(230, 226, 218));
            g2.fillRoundRect(pillStartX, pillY, pw, pillH, 8, 8);
            g2.setColor(new Color(190, 185, 172));
            g2.drawRoundRect(pillStartX, pillY, pw, pillH, 8, 8);
            g2.setColor(new Color(80, 76, 70));
            g2.drawString(t, pillStartX + pillPadX, pillY + 12);
            pillStartX += pw + pillGap;
        }
        ty += pillH + 2 + sectionGap;

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
        pillStartX = innerX + (innerW - inspTotalW) / 2;
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

    // ── Helper ────────────────────────────────────────────────────────────────

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