package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean running;
    public boolean ePressed;
    public boolean enterPressed, escPressed;
    public boolean bPressed;       // opens PC storage
    public boolean tabPressed;     // switches box in PCUI
    public boolean shiftPressed;   // toggles Party / Box view in PCUI
    public boolean mPressed;       // opens world map

    // ── Typed character (for rename text input in WorldSelectUI) ──────────────
    // keyTyped fires for every printable character including backspace.
    // consumeTyped() returns and clears it each frame — no extra key booleans needed.

    private char    lastTypedChar = 0;
    private boolean hasTyped      = false;

    /** Poll the latest typed character. Returns 0 if nothing was typed since last call. */
    public char consumeTyped() {
        if (!hasTyped) return 0;
        hasTyped      = false;
        char c        = lastTypedChar;
        lastTypedChar = 0;
        return c;
    }

    // ── KeyListener ───────────────────────────────────────────────────────────

    @Override
    public void keyTyped(KeyEvent e) {
        lastTypedChar = e.getKeyChar();
        hasTyped      = true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed    = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed  = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed  = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_E                    -> ePressed     = true;
            case KeyEvent.VK_ENTER                -> enterPressed = true;
            case KeyEvent.VK_ESCAPE               -> escPressed   = true;
            case KeyEvent.VK_B                    -> bPressed     = true;
            case KeyEvent.VK_TAB                  -> tabPressed   = true;
            case KeyEvent.VK_SHIFT                -> shiftPressed = true;
            case KeyEvent.VK_M                    -> mPressed     = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        switch (code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed    = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed  = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed  = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_E                    -> ePressed     = false;
            case KeyEvent.VK_ENTER                -> enterPressed = false;
            case KeyEvent.VK_ESCAPE               -> escPressed   = false;
            case KeyEvent.VK_B                    -> bPressed     = false;
            case KeyEvent.VK_TAB                  -> tabPressed   = false;
            case KeyEvent.VK_SHIFT                -> shiftPressed = false;
            case KeyEvent.VK_M                    -> mPressed     = false;
        }
    }

    public boolean isMoving() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }
}