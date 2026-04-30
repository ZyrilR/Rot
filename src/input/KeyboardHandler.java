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

    public boolean consoleTypingMode = false;

    public char    lastTypedChar = 0;
    public boolean backspaceHit  = false;
    public boolean enterHit      = false;
    private boolean hasTyped     = false;

    // NEW: set true for one frame when console is toggled, so keyTyped's \ is swallowed
    private boolean justToggledConsole = false;

    public char consumeTyped() {
        if (!hasTyped) return 0;
        hasTyped      = false;
        char c        = lastTypedChar;
        lastTypedChar = 0;
        return c;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();

        // Always swallow the backslash — it is only a toggle key, never a typeable char
        if (c == '\\') return;

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
            // Arrow keys still need to reach GamePanel while console is open
            if (code == KeyEvent.VK_UP)    upPressed    = true;
            if (code == KeyEvent.VK_DOWN)  downPressed  = true;
            if (code == KeyEvent.VK_LEFT)  leftPressed  = true;
            if (code == KeyEvent.VK_RIGHT) rightPressed = true;
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

        if (consoleTypingMode) {
            if (code == KeyEvent.VK_UP)    upPressed    = false;
            if (code == KeyEvent.VK_DOWN)  downPressed  = false;
            if (code == KeyEvent.VK_LEFT)  leftPressed  = false;
            if (code == KeyEvent.VK_RIGHT) rightPressed = false;
            return;
        }

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