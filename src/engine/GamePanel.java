package engine;

import input.KeyboardHandler;
import map.WorldLoader;
import overworld.Player;
import tile.CollisionChecker;
import tile.TileManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import static utils.Constants.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    //dialogueOpen
    public String gameState = "play";
    public ui.DialogueBox dialogueBox = new ui.DialogueBox(this);
    //InputHandler
    public KeyboardHandler keyboardHandler = new KeyboardHandler();

    //Entities
    public Player player = new Player(this, keyboardHandler);
    public ArrayList<npc.NPC> npcs = new ArrayList<>();
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

        world.loadMap("/assets/Worlds/2/", true);

        System.out.println("After loading map");  // <-- test print
    }

    public void update() {
        if (gameState.equals("play")) {
            player.update();

            // Check for 'E' or 'Enter' click
            if (keyboardHandler.enterPressed) {
                keyboardHandler.enterPressed = false; // consume input
                player.checkInteraction();
            }
        } else if (gameState.equals("dialogue")) {
            dialogueBox.update();
        }
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

        // 3. Draw Player
        player.draw(graphics2D);

        // 4. Draw UI LAST (So it sits on top of everything)
        if (gameState.equals("dialogue")) {
            dialogueBox.draw(graphics2D);
        }

        graphics2D.dispose();
    }
}