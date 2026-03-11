package engine;

import input.KeyboardHandler;
import overworld.Player;
import map.TileManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import static utils.Constants.*;

public class GamePanel extends JPanel {

    //InputHandler
    public KeyboardHandler keyboardHandler = new KeyboardHandler();

    //Entities
    public Player player = new Player(this, keyboardHandler);
    private TileManager background = new TileManager(this);
    private TileManager decorations = new TileManager(this);
    private TileManager rooms = new TileManager(this);
    //decorationmanager
    //uimanager

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyboardHandler);
        System.out.println("Before loading map");  // <-- test print

        background.loadMap("/assets/Maps/world_1.txt");  // <-- must match actual classpath

        System.out.println("After loading map");  // <-- test print
    }

    public void update() {
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        graphics2D.setColor(Color.GRAY);
        graphics2D.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        //LAYER 1: Tiles
        background.draw(graphics2D);

        //LAYER 2: Decorations
//        decorations.draw(graphics2D);

        //OVERLAY: Rooms
        //Problem: Rooms should be loaded only when the player touches the teleport tile
//        rooms.draw(graphics2D);

        //player layer
        player.draw(graphics2D);

        graphics2D.dispose();
    }
}