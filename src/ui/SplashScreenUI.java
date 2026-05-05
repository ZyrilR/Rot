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

    private static final String[] BUTTON_LABELS = { "PLAY", "CREDITS", "QUIT" };
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
            case 0 -> {
                gp.WORLDSELECTUI.open();
            }
            case 1 -> state = State.CREDITS;
            case 2 -> System.exit(0);
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
        int btnW = 220;
        int btnH = 44;

        int startY = (int)(SCREEN_HEIGHT * 0.54);
        int btnX = (SCREEN_WIDTH - btnW) / 2;

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            boolean sel = (i == cursor);
            int btnY = startY + i * (btnH + 12);

            if (sel) {
                g2.setColor(COLOR_MAROON);
                int pulse = (int)(Math.sin(tick * 0.1) * 3);
                g2.fillRoundRect(btnX - pulse, btnY, btnW + (pulse * 2), btnH, 5, 5);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(btnX - pulse, btnY, btnW + (pulse * 2), btnH, 5, 5);
            } else {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(btnX, btnY, btnW, btnH, 5, 5);
            }

            Font menuFont = (AssetManager.pokemonGb != null) ? AssetManager.pokemonGb.deriveFont(Font.BOLD, 18)
                    : new Font("SansSerif", Font.BOLD, 18);

            g2.setFont(menuFont);
            FontMetrics fm = g2.getFontMetrics();
            String label = BUTTON_LABELS[i];
            int tx = btnX + (btnW - fm.stringWidth(label)) / 2;
            int ty = btnY + (btnH + fm.getAscent() - 2) / 2;

            g2.setColor(sel ? Color.WHITE : COLOR_TEXT_UNSELECTED);
            g2.drawString(label, tx, ty);
        }
    }

    // ── Restored Detailed Credits screen ──────────────────────────────────────

    private void drawCredits(Graphics2D g2, Font base) {

        // Dim overlay
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        int panelW = 460, panelH = 340;
        int panelX = (SCREEN_WIDTH - panelW) / 2;
        int panelY = (SCREEN_HEIGHT - panelH) / 2;

        drawWindow(g2, panelX, panelY, panelW, panelH);

        // Title bar
        g2.setColor(new Color(44, 44, 42));
        g2.fillRoundRect(panelX + 8, panelY + 8, panelW - 16, 36, 8, 8);
        g2.setFont(base.deriveFont(Font.BOLD, 14f));
        g2.setColor(new Color(241, 239, 232));
        g2.drawString("CREDITS", panelX + 28, panelY + 32);

        g2.setColor(new Color(216, 184, 88));
        g2.drawLine(panelX + 8, panelY + 46, panelX + panelW - 8, panelY + 46);

        // Credit lines
        String[][] credits = {
                { "Game Design & Programming", "" },
                { "Zyril", "" },
                { "", "" },
                { "Engine", "Java Swing / Java 2D" },
                { "Font", "PokemonGB (RAeo)" },
                { "", "" },
                { "Inspired by", "Pokemon (Game Freak)" },
                { "& Italian Brainrot memes", "" },
        };

        int ty = panelY + 70;
        for (String[] row : credits) {
            if (row[0].isEmpty() && row[1].isEmpty()) { ty += 10; continue; }

            boolean isHeader = row[1].isEmpty();

            if (isHeader) {
                g2.setFont(base.deriveFont(Font.BOLD, 11f));
                g2.setColor(new Color(216, 184, 88));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(row[0], panelX + (panelW - fm.stringWidth(row[0])) / 2, ty);
            } else {
                g2.setFont(base.deriveFont(9f));
                g2.setColor(new Color(120, 116, 108));
                g2.drawString(row[0], panelX + 28, ty);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(200, 196, 185));
                g2.drawString(row[1], panelX + panelW - fm.stringWidth(row[1]) - 28, ty);
            }
            ty += 22;
        }

        // Back hint
        g2.setFont(base.deriveFont(8f));
        g2.setColor(new Color(120, 116, 108));
        String hint = "ESC / ENTER to go back";
        FontMetrics hfm = g2.getFontMetrics();
        g2.drawString(hint,
                panelX + (panelW - hfm.stringWidth(hint)) / 2,
                panelY + panelH - 16);
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