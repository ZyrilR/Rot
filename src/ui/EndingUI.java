package ui;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

/**
 * Full-screen cinematic ending shown after defeating the Gym Master (Sir Khai).
 *
 * Displays dialogue lines one at a time over a dark background with
 * a slow fade-in per line. ENTER advances; after the last line the
 * screen fades to black and returns to the world-select screen.
 */
public class EndingUI {

    // ── Dialogue ──────────────────────────────────────────────────────────────

    private static final String[] LINES = {
            "Mamma mia... you actually defeated the Gym Master.",
            "The BrainRot energy is stabilizing...",
            "the Skibidi levels are decreasing...",
            "From a noob with one rusty capsule...",
            "to the strongest rot trainer alive.",
            "The gyms have been conquered.",
            "The sigma path has been completed.",
            "The Italian BrainRot Council acknowledges your aura.",
            "But beware...",
            "Somewhere out there, an even stronger rot",
            "is still mewing in the shadows...",
            "Until then...",
            "Stay sigma.",
            "Thank you for playing ROT."
    };

    // ── Timing ────────────────────────────────────────────────────────────────

    private static final int CHARS_PER_FRAME = 1;   // typewriter speed
    private static final int HOLD_TICKS      = 60;  // frames to hold after line is fully typed
    private static final int FADE_IN_TICKS   = 30;  // per-line fade in
    private static final int FINAL_HOLD      = 90;  // frames before exit fade begins
    private static final int EXIT_FADE_TICKS = 60;

    // ── State ─────────────────────────────────────────────────────────────────

    private final GamePanel gp;

    private int     lineIndex    = 0;
    private int     charIndex    = 0;   // characters revealed so far on current line
    private int     holdTimer    = 0;
    private int     fadeInTick   = 0;
    private boolean lineComplete = false;
    private boolean allDone      = false;
    private int     exitFadeTick = 0;
    private int     inputCooldown= 0;

    // Accumulated display lines (we keep previous lines dimmed above the current)
    private final List<String> displayed = new ArrayList<>();

    private BufferedImage bgImage = null;

    // ── Constructor ───────────────────────────────────────────────────────────

    public EndingUI(GamePanel gp) {
        this.gp = gp;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void open() {
        lineIndex     = 0;
        charIndex     = 0;
        holdTimer     = 0;
        fadeInTick    = 0;
        lineComplete  = false;
        allDone       = false;
        exitFadeTick  = 0;
        inputCooldown = INPUT_DELAY * 3;
        displayed.clear();
        bgImage = AssetManager.loadImage("/res/SplashScreen/ROT.png");
        System.out.println("[EndingUI] Opened.");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        if (inputCooldown > 0) { inputCooldown--; return; }

        // ── Exit fade ─────────────────────────────────────────────────────────
        if (allDone) {
            exitFadeTick++;
            if (exitFadeTick >= EXIT_FADE_TICKS) {
                // Reset and go to world select
                gp.GAMESTATE = "world_select";
                gp.WORLDSELECTUI.open();
                utils.AudioManager.playMusic(utils.Constants.SND_SPLASH, true);
                System.out.println("[EndingUI] Ending complete — returning to world select.");
            }
            return;
        }

        // ── Line complete: wait for ENTER or hold timer ───────────────────────
        if (lineComplete) {
            holdTimer++;
            if (gp.KEYBOARDHANDLER.enterPressed || holdTimer >= HOLD_TICKS) {
                gp.KEYBOARDHANDLER.enterPressed = false;
                advanceLine();
            }
            return;
        }

        // ── Fade in current line ──────────────────────────────────────────────
        if (fadeInTick < FADE_IN_TICKS) {
            fadeInTick++;
        }

        // ── Typewriter ────────────────────────────────────────────────────────
        String full = LINES[lineIndex];
        if (charIndex < full.length()) {
            charIndex++;
            // ENTER skips to full line immediately
            if (gp.KEYBOARDHANDLER.enterPressed) {
                gp.KEYBOARDHANDLER.enterPressed = false;
                charIndex = full.length();
                inputCooldown = INPUT_DELAY;
            }
        } else {
            // Line fully typed
            lineComplete = true;
            holdTimer    = 0;
        }
    }

    private void advanceLine() {
        // Commit current line to the "previous" list
        displayed.add(LINES[lineIndex]);
        lineIndex++;
        charIndex    = 0;
        holdTimer    = 0;
        fadeInTick   = 0;
        lineComplete = false;
        inputCooldown = INPUT_DELAY;

        if (lineIndex >= LINES.length) {
            allDone      = true;
            exitFadeTick = 0;
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Background ────────────────────────────────────────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // Dark overlay — thicker than splash so text pops
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        Font base = (AssetManager.pokemonGb != null)
                ? AssetManager.pokemonGb : new Font("Monospaced", Font.PLAIN, 10);

        // ── Dialogue card ─────────────────────────────────────────────────────
        int cardW  = SCREEN_WIDTH  - TILE_SIZE * 4;
        int cardH  = SCREEN_HEIGHT - TILE_SIZE * 4;
        int cardX  = (SCREEN_WIDTH  - cardW) / 2;
        int cardY  = (SCREEN_HEIGHT - cardH) / 2;

        drawCard(g2, cardX, cardY, cardW, cardH);

        // ── Previous lines (dimmed) ───────────────────────────────────────────
        int lineH      = 28;
        int maxVisible = 8;                       // how many old lines to show
        int textX      = cardX + 40;
        int textW      = cardW - 80;

        // Start position for the CURRENT line (anchored in lower portion)
        int currentLineY = cardY + cardH - 100;
        // Previous lines stack above it
        int startOldIdx = Math.max(0, displayed.size() - maxVisible);

        for (int i = startOldIdx; i < displayed.size(); i++) {
            int slot  = i - startOldIdx;
            int posY  = currentLineY - (displayed.size() - i) * lineH;
            float dimAlpha = Math.max(0.15f, 0.55f - (displayed.size() - 1 - i) * 0.08f);
            drawLine(g2, base, displayed.get(i), textX, posY, textW,
                    new Color(200, 190, 160, (int)(dimAlpha * 255)), 13f, false);
        }

        // ── Current line (typewriter + fade in) ───────────────────────────────
        if (!allDone && lineIndex < LINES.length) {
            String full    = LINES[lineIndex];
            String partial = full.substring(0, charIndex);

            float fadeAlpha = Math.min(1f, (float) fadeInTick / FADE_IN_TICKS);
            int   alpha     = (int)(fadeAlpha * 255);

            // Highlight for the current active line
            boolean isClosingLine = lineIndex >= LINES.length - 2; // last 2 lines
            Color   textColor     = isClosingLine
                    ? new Color(216, 184, 88, alpha)
                    : new Color(241, 239, 232, alpha);

            float fontSize = isClosingLine ? 16f : 14f;
            drawLine(g2, base, partial, textX, currentLineY, textW, textColor, fontSize, isClosingLine);

            // Blinking cursor
            if (lineComplete && (System.currentTimeMillis() / 500) % 2 == 0) {
                g2.setFont(base.deriveFont(14f));
                FontMetrics fm = g2.getFontMetrics();
                int cx = textX + fm.stringWidth(full) + 6;
                g2.setColor(new Color(216, 184, 88, 200));
                g2.fillRect(cx, currentLineY - 14, 8, 16);
            }
        }

        // ── Hint ──────────────────────────────────────────────────────────────
        if (!allDone && lineComplete) {
            g2.setFont(base.deriveFont(8f));
            g2.setColor(new Color(140, 136, 128,
                    (int)(Math.abs(Math.sin(System.currentTimeMillis() / 600.0)) * 200)));
            String hint = "ENTER Continue";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint,
                    cardX + cardW - fm.stringWidth(hint) - 20,
                    cardY + cardH - 16);
        }

        // ── Exit fade overlay ─────────────────────────────────────────────────
        if (allDone) {
            float fadeProgress = Math.min(1f, (float) exitFadeTick / EXIT_FADE_TICKS);
            g2.setColor(new Color(0, 0, 0, (int)(fadeProgress * 255)));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    // ── Draw helpers ──────────────────────────────────────────────────────────

    private void drawCard(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 16;
        // Subtle card background
        g2.setColor(new Color(20, 18, 14, 200));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        // Triple-stroke gold border
        g2.setStroke(new BasicStroke(6));
        g2.setColor(new Color(60, 55, 45));
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(216, 184, 88));
        g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(60, 55, 45));
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, arc - 4, arc - 4);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawLine(Graphics2D g2, Font base, String text,
                          int x, int y, int maxW,
                          Color color, float size, boolean bold) {
        g2.setFont(base.deriveFont(bold ? Font.BOLD : Font.PLAIN, size));
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();

        // Simple word-wrap
        StringBuilder line = new StringBuilder();
        int cy = y;
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && !line.isEmpty()) {
                g2.drawString(line.toString(), x, cy);
                cy += (int)(size + 6);
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) g2.drawString(line.toString(), x, cy);
    }
}