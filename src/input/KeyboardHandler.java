package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean running;
    public boolean ePressed;
    public boolean enterPressed, escPressed;
    public boolean bPressed;   // opens PC storage
    public boolean tabPressed; // switches box in PCUI
    public boolean pPressed;   // toggles Party / Box view in PCUI

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed    = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed  = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed  = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_E                    -> ePressed     = true;
            case KeyEvent.VK_ENTER                -> enterPressed = true;
            case KeyEvent.VK_ESCAPE               -> escPressed   = true;
            case KeyEvent.VK_B                    -> bPressed     = true;
            case KeyEvent.VK_TAB                  -> tabPressed   = true;
            case KeyEvent.VK_P                    -> pPressed     = true;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed    = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed  = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed  = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_E                    -> ePressed     = false;
            case KeyEvent.VK_ENTER                -> enterPressed = false;
            case KeyEvent.VK_ESCAPE               -> escPressed   = false;
            case KeyEvent.VK_B                    -> bPressed     = false;
            case KeyEvent.VK_TAB                  -> tabPressed   = false;
            case KeyEvent.VK_P                    -> pPressed     = false;
        }

    }

    public boolean isMoving() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }
}