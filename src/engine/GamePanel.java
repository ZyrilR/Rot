package engine;

import input.KeyboardHandler;
import overworld.Player;
import map.CollisionChecker;
import map.TileManager;

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
        background.loadMap(1);  // <-- must match actual classpath
        System.out.println("After loading map");  // <-- test print
        npcs.add(new npc.MarketNPC(this, TILE_SIZE * 24, TILE_SIZE * 26));
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

    public TileManager getTileManager() {
        return background;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        // 1. Draw Background Tiles
        background.draw(graphics2D);

        // 2. Draw NPCs (Drawn before player for correct layering)
        for (npc.NPC npc : npcs) {
            npc.draw(graphics2D);
        }

        // 3. Draw Player
        player.draw(graphics2D);

        // 4. Draw UI LAST (So it sits on top of everything)
        if (gameState.equals("dialogue")) {
            dialogueBox.draw(graphics2D);
        }

        graphics2D.dispose();
    }
}