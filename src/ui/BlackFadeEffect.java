package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import static utils.Constants.SCREEN_WIDTH;
import static utils.Constants.SCREEN_HEIGHT;

public class BlackFadeEffect {
    private int alpha = 0;
    private int fadeSpeed = 8;
    private boolean fullyBlack = false;

    public enum FadeMode { FADE_IN_TO_BLACK, FADE_OUT_TO_PLAY }

    // FIX: Start in FADE_OUT_TO_PLAY mode with 0 alpha so the screen is clear by default!
    private FadeMode mode = FadeMode.FADE_OUT_TO_PLAY;

    public void start(FadeMode mode, int speed) {
        this.mode = mode;
        this.fadeSpeed = speed;
        this.fullyBlack = false;
        alpha = (mode == FadeMode.FADE_IN_TO_BLACK) ? 0 : 255;
    }

    public void update() {
        if (mode == FadeMode.FADE_IN_TO_BLACK) {
            alpha += fadeSpeed;
            if (alpha >= 255) { alpha = 255; fullyBlack = true; }
        } else {
            alpha -= fadeSpeed;
            if (alpha <= 0) { alpha = 0; }
        }
    }

    public void draw(Graphics2D g2) {
        if (alpha > 0) {
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    public boolean isFullyBlack() { return fullyBlack; }
    // FIX: Safely check if alpha is 0 or less
    public boolean isFadeOutComplete() { return mode == FadeMode.FADE_OUT_TO_PLAY && alpha <= 0; }
}