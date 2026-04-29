package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean running;
    public boolean ePressed;
    public boolean enterPressed, escPressed;
    public boolean bPressed;
    public boolean tabPressed;
    public boolean shiftPressed;
    public boolean mPressed;
    public boolean consolePressed;

    // ── Console / typing mode ─────────────────────────────────────────────────
    /**
     * When true, keyTyped characters are routed to the typing buffer
     * instead of being processed as game keybinds.
     */
    public boolean consoleTypingMode = false;

    // Typed character state — used by both WorldSelectUI (consumeTyped)
    // and the dev console (lastTypedChar / backspaceHit / enterHit).
    public char    lastTypedChar = 0;
    public boolean backspaceHit  = false;
    public boolean enterHit      = false;
    private boolean hasTyped     = false;

    /** Poll the latest typed character. Returns 0 if nothing new since last call. */
    public char consumeTyped() {
        if (!hasTyped) return 0;
        hasTyped     = false;
        char c       = lastTypedChar;
        lastTypedChar = 0;
        return c;
    }

    // ── KeyListener ───────────────────────────────────────────────────────────

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();

        if (consoleTypingMode) {
            if (c == KeyEvent.VK_BACK_SPACE) { backspaceHit = true; return; }
            if (c == '\n' || c == '\r')      { enterHit     = true; return; }
            if (c == KeyEvent.CHAR_UNDEFINED || c < 32 || c > 126) return;
        }

        lastTypedChar = c;
        hasTyped      = true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (consoleTypingMode) {
            if (code == KeyEvent.VK_ESCAPE)     escPressed     = true;
            if (code == KeyEvent.VK_BACK_SLASH) consolePressed = true;
            return;
        }

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
            case KeyEvent.VK_BACK_SLASH           -> consolePressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_BACK_SLASH) { consolePressed = false; return; }

        if (consoleTypingMode) return;

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