package engine;

import input.KeyboardHandler;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel {

    // Screen Settings
    public final int ORIGINAL_TILE_SIZE = 32;
    public final int SCALE = 3;

    public final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;
    public final int MAX_SCREEN_COL = 32;
    public final int MAX_SCREEN_ROW = 28;
    public final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    public final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

    public final int FPS = 60;

    public KeyboardHandler input = new KeyboardHandler();
    public GameStateManager gsm;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(input);
        this.setFocusable(true);

        // Initialize the State Manager instead of the Player directly
        gsm = new GameStateManager(this);
    }

    public void update() {
        gsm.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        // Let the state manager draw whatever state is currently active
        gsm.draw(graphics2D);

        graphics2D.dispose();
    }
}