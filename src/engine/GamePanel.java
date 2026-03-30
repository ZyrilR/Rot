package engine;

import input.KeyboardHandler;
import map.WorldLoader;
import npc.NPC;
import npc.MarketNPC;
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
import java.awt.Rectangle;
import java.util.ArrayList;

import static utils.Constants.*;

public class GamePanel extends JPanel {

    public String GAMESTATE = "play";
    public DialogueBox DIALOGUEBOX = new DialogueBox(this);
    public ShopUI SHOPUI = new ShopUI(this);

    //GAME HANDLER
    public KeyboardHandler KEYBOARDHANDLER = new KeyboardHandler();
    public CollisionChecker COLLISIONCHECKER = new CollisionChecker(this);
    public Player player = new Player(this, KEYBOARDHANDLER);

    public final WorldLoader world = new WorldLoader(this);
    public ArrayList<NPC> npcs = new ArrayList<>();

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(KEYBOARDHANDLER);

        world.loadMap("/assets/Worlds/2/", true);

        spawnEntitiesFromMap();
        spawnCornerNPCs();
    }

    public TileManager getWorldBackgroundLayer() { return world.getBackgroundLayer(); }
    public ArrayList<TileManager> getWorldBuildingLayer() { return world.getBuildingLayer(); }
    public TileManager getWorldInteractiveLayer() { return world.getInteractiveLayer(); }

    public void spawnEntitiesFromMap() {
        int[][] interactiveMap = world.getInteractiveLayer().getMap();

        for (int row = 0; row < MAX_WORLD_ROW; row++) {
            for (int col = 0; col < MAX_WORLD_COL; col++) {
                int tileNum = interactiveMap[row][col];

                if (tileNum == 1) {
                    MarketNPC shopKeeper = new MarketNPC("Bob", 1);
                    shopKeeper.worldX = col * TILE_SIZE;
                    shopKeeper.worldY = row * TILE_SIZE;
                    shopKeeper.solidArea = new Rectangle(0, 0, TILE_SIZE, TILE_SIZE);
                    npcs.add(shopKeeper);
                    interactiveMap[row][col] = 0;
                }
            }
        }
    }

    public void update() {
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

        world.drawBottom(graphics2D);

        for (NPC npc : npcs) {
            if (npc != null) {
                npc.draw(graphics2D, this);
            }
        }

        player.draw(graphics2D);
        world.drawTop(graphics2D);

        if (GAMESTATE.equals("dialogue")) {
            DIALOGUEBOX.draw(graphics2D);
        }

        if (GAMESTATE.equals("shop")) {
            SHOPUI.draw(graphics2D);
        }

        graphics2D.dispose();
    }

    public void spawnCornerNPCs() {
        MarketNPC topLeftNpc = new MarketNPC("North-West Guard", 1);
        topLeftNpc.worldX = 10 * TILE_SIZE;
        topLeftNpc.worldY = 10 * TILE_SIZE;
        npcs.add(topLeftNpc);

        MarketNPC topRightNpc = new MarketNPC("North-East Wanderer", 2);
        topRightNpc.worldX = 40 * TILE_SIZE;
        topRightNpc.worldY = 10 * TILE_SIZE;
        npcs.add(topRightNpc);

        MarketNPC bottomLeftNpc = new MarketNPC("South-West Scout", 3);
        bottomLeftNpc.worldX = 10 * TILE_SIZE;
        bottomLeftNpc.worldY = 40 * TILE_SIZE;
        npcs.add(bottomLeftNpc);

        MarketNPC bottomRightNpc = new MarketNPC("South-East Merchant", 4);
        bottomRightNpc.worldX = 40 * TILE_SIZE;
        bottomRightNpc.worldY = 40 * TILE_SIZE;
        npcs.add(bottomRightNpc);
    }
}