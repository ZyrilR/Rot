package engine;

import input.KeyboardHandler;
import map.WorldLoader;
import overworld.Player;
import tile.CollisionChecker;
import tile.TileManager;
import ui.DialogueBox;
import ui.ShopUI;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import static utils.Constants.*;

public class GamePanel extends JPanel {

    //GAMESTATE MANAGER
    public String GAMESTATE = "play";
    public DialogueBox DIALOGUEBOX = new DialogueBox(this);
    public ShopUI SHOPUI = new ShopUI(this);

    //GAME HANDLER
    public KeyboardHandler KEYBOARDHANDLER = new KeyboardHandler();
    public CollisionChecker COLLISIONCHECKER = new CollisionChecker(this);

    //GAME PANEL ENTITIES
    public Player player = new Player(this, KEYBOARDHANDLER);
    private final WorldLoader world = new WorldLoader(this);
    private WorldLoader room = new WorldLoader(this);

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(KEYBOARDHANDLER);
        System.out.println("Before loading map");  // <-- test print

        world.loadMap("/assets/Worlds/2/", true);

        System.out.println("After loading map");  // <-- test print
    }

    public void update() {
        // Only update player movement when actually playing
        if (GAMESTATE.equalsIgnoreCase("play")) {
            player.update();
        }

        switch (GAMESTATE.toUpperCase()) {
            case "PLAY":
                // Check for 'E' or 'Enter' click
//                if (KEYBOARDHANDLER.enterPressed) {
//                    KEYBOARDHANDLER.enterPressed = false; // consume input
//                    player.checkInteraction();
//                }
                // TEMP TEST: open shop directly with ENTER (remove once NPC interaction works)
                if (KEYBOARDHANDLER.ePressed) {
                    KEYBOARDHANDLER.ePressed = false;
                    SHOPUI.open();
                    GAMESTATE = "shop";
                    System.out.println("[GamePanel] TEST: Shop opened via ENTER key.");
                }
                break;

            case "DIALOGUE":
                DIALOGUEBOX.update();
                break;

            case "SHOP":
                SHOPUI.update();
                break;

            default:
                break;
        }

    }

    public TileManager getWorldBackgroundLayer() {
        //temporary
        return world.getBackgroundLayer().getFirst();
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
        if (GAMESTATE.equals("dialogue")) {
            DIALOGUEBOX.draw(graphics2D);
        }

        if (GAMESTATE.equals("shop")) {
            SHOPUI.draw(graphics2D);
        }

        graphics2D.dispose();
    }
}