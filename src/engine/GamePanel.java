package engine;

import input.KeyboardHandler;
import map.WorldLoader;
import overworld.Player;
import tile.TileManager;

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
    public CollisionChecker collisionChecker = new CollisionChecker(this);
    private final WorldLoader world = new WorldLoader(this);
    private WorldLoader room = new WorldLoader(this);

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyboardHandler);
        System.out.println("Before loading map");  // <-- test print

//        background.loadMap(1);  // <-- must match actual classpath
        world.loadMap("/assets/Worlds/2/", true);

        System.out.println("After loading map");  // <-- test print
    }

    public void update() {
        player.update();
    }

    public TileManager getWorldBackgroundLayer() {
        return world.getBackgroundLayer();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        //LAYER 1: Tiles
        world.draw(graphics2D);

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