package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean running;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP:
                upPressed = true;
                break;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN:
                downPressed = true;
                break;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_SHIFT:
                running = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP:
                upPressed = false;
                break;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN:
                downPressed = false;
                break;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_SHIFT:
                running = false;
                break;
        }
    }

    public boolean isMoving() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }
}
