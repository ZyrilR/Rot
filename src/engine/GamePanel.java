package engine;

import input.KeyboardHandler;
import overworld.Player;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    //Screen Settings
    public final int ORIGINAL_TILE_SIZE = 32;
    final int SCALE = 3;

    final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;
    final int MAX_SCREEN_COL = 32;
    final int MAX_SCREEN_ROW = 28;
    final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

    final int FPS = 60;

    public KeyboardHandler input = new KeyboardHandler();
    Player player = new Player(this, input);

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(input);
        this.setFocusable(true);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        player.draw(graphics2D);
        graphics2D.dispose();
    }

}
