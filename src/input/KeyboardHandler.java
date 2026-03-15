package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed;
    public boolean running;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_ENTER, KeyEvent.VK_E -> enterPressed = true;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_E) {
            enterPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> downPressed = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_ENTER, KeyEvent.VK_E -> enterPressed = false;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_E) {
            enterPressed = false;
        }
    }

    public boolean isMoving() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }
}
