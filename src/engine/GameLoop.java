package engine;

import java.awt.*;

public class GameLoop implements Runnable {

    Thread gameThread;
    GamePanel gamePanel;

    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void startGameThread(GamePanel gamePanel) {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0/gamePanel.FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime = 0;
        long timer = 0;
        int drawCount = 0;

        while(gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) /drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                gamePanel.repaint();
                delta--;
                drawCount++;
            }

            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }
    public void update() {
        gamePanel.player.update();
    }

}