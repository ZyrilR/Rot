package engine;

import brainrots.BrainRotFactory;
import brainrots.Tier;
import input.KeyboardHandler;
import map.WorldLoader;
import npc.NPC;
import npc.MarketNPC;
import overworld.EncounterSystem;
import overworld.Player;
import storage.PCSystem;
import tile.CollisionChecker;
import tile.Tile;
import tile.TileManager;
import tile.TileTeleporter;
import ui.*;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import items.ItemRegistry;

import static utils.Constants.*;


public class GamePanel extends JPanel {
    // ── Core handlers ─────────────────────────────────────────────────────────
    public KeyboardHandler KEYBOARDHANDLER  = new KeyboardHandler();
    public EncounterSystem encounterSystem = new EncounterSystem();
    public CollisionChecker COLLISIONCHECKER = new CollisionChecker(this);
    public Player player = new Player(this, KEYBOARDHANDLER);

    public String GAMESTATE = "play";
    public DialogueBox DIALOGUEBOX = new DialogueBox(this);

    public final ShopUI SHOPUI = new ShopUI(this);
    public final PCUI     PCUI     = new PCUI(this, player.getPCSYSTEM());
    public final MenuUI MENUUI = new MenuUI(this);
    public final InventoryUI INVENTORYUI = new InventoryUI(this);

    public final WorldLoader world = new WorldLoader(this);

    public String path;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);this.requestFocusInWindow();
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(KEYBOARDHANDLER);

        world.loadMap(WORLD, true);

        // ── Seed the PC party with the player's starting team ────────────────
        // In a full game these would be loaded from save data.
        // For now we add test members so the PC UI has data to display.
//        seedTestParty();
    }

    // ── Test seed ─────────────────────────────────────────────────────────────

    /**
     * Populates the PC party with a small starter set.
     * Replace / remove this once save/load is implemented.
     */
    private void seedTestParty() {
        PCSystem PCSYSTEM = player.getPCSYSTEM();
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("CAPUCCINO ASSASSINO",    Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("LIRILI LARILA",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BRR BRR PATAPIM", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BONECA AMBALABU",    Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("UDIN DIN DIN DIN DUN", Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BRR BRR PATAPIM", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BONECA AMBALABU",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("UDIN DIN DIN DIN DUN", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("CAPUCCINO ASSASSINO",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("LIRILI LARILA",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BRR BRR PATAPIM", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BONECA AMBALABU",    Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("UDIN DIN DIN DIN DUN", Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("CAPUCCINO ASSASSINO",    Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("LIRILI LARILA",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BRR BRR PATAPIM", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BONECA AMBALABU",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("UDIN DIN DIN DIN DUN", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("LIRILI LARILA",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("CAPUCCINO ASSASSINO",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("LIRILI LARILA",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BRR BRR PATAPIM", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BONECA AMBALABU",    Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("LIRILI LARILA",    Tier.DIAMOND));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BRR BRR PATAPIM", Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BONECA AMBALABU",    Tier.NORMAL));
        player.getInventory().addItem(ItemRegistry.getItem("MILD STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("MILD STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("MODERATE STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("SUPER STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("SUPER STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("CONFUSION CURE"));
        player.getInventory().addItem(ItemRegistry.getItem("PARALYZE CURE"));
        player.getInventory().addItem(ItemRegistry.getItem("PARALYZE CURE"));
        player.getInventory().addItem(ItemRegistry.getItem("BURN CURE"));
        player.getInventory().addItem(ItemRegistry.getItem("DEBUFF TONIC"));
        player.getInventory().addItem(ItemRegistry.getItem("RED CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("BLUE CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("MASTER CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("Focus Stance Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Sahur Chant Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Sneaker Dash Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Heat Burst Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Evasion Up Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Power Combo Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Aqua Engine Scroll"));
        System.out.println("[GamePanel] Test party & items seeded.");
    }

    // ── Layer accessors ───────────────────────────────────────────────────────
    public ArrayList<TileManager> getWorldBackgroundLayer() {
        return world.getBackgroundLayer();
    }
    public ArrayList<TileManager> getWorldBuildingLayer() {
        return world.getBuildingLayer();
    }
    public TileManager getWorldInteractiveLayer() {
        return world.getInteractiveLayer();
    }

    // ── Entity spawning ───────────────────────────────────────────────────────
//    public void spawnEntitiesFromMap() {
//        int[][] interactiveMap = world.getInteractiveLayer().getMap();
//
//        for (int row = 0; row < MAX_WORLD_ROW; row++) {
//            for (int col = 0; col < MAX_WORLD_COL; col++) {
//                int tileNum = interactiveMap[row][col];
//
//                if (tileNum == 1) {
//                    MarketNPC shopKeeper = new MarketNPC("Bob", 1);
//                    shopKeeper.worldX = col * TILE_SIZE;
//                    shopKeeper.worldY = row * TILE_SIZE;
//                    shopKeeper.solidArea = new Rectangle(0, 0, TILE_SIZE, TILE_SIZE);
//                    npcs.add(shopKeeper);
//                    interactiveMap[row][col] = 0;
//                }
//            }
//        }
//    }

//    public void spawnCornerNPCs() {
//        MarketNPC topLeftNpc = new MarketNPC("North-West Guard", 1);
//        topLeftNpc.worldX = 10 * TILE_SIZE;
//        topLeftNpc.worldY = 10 * TILE_SIZE;
//        npcs.add(topLeftNpc);
//
//        MarketNPC topRightNpc = new MarketNPC("North-East Wanderer", 2);
//        topRightNpc.worldX = 40 * TILE_SIZE;
//        topRightNpc.worldY = 10 * TILE_SIZE;
//        npcs.add(topRightNpc);
//
//        MarketNPC bottomLeftNpc = new MarketNPC("South-West Scout", 3);
//        bottomLeftNpc.worldX = 10 * TILE_SIZE;
//        bottomLeftNpc.worldY = 40 * TILE_SIZE;
//        npcs.add(bottomLeftNpc);
//
//        MarketNPC bottomRightNpc = new MarketNPC("South-East Merchant", 4);
//        bottomRightNpc.worldX = 40 * TILE_SIZE;
//        bottomRightNpc.worldY = 40 * TILE_SIZE;
//        npcs.add(bottomRightNpc);
//    }

    // ── Game loop ─────────────────────────────────────────────────────────────
    public void update() {
        switch (GAMESTATE.toUpperCase()) {

            case "PLAY":
                // Update player movement
                player.update();

                // Check trainer line-of-sight every tick
                encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);

                TileTeleporter tr = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
                if (tr != null) {
                    if (!tr.isInteracted)
                        tr.interact(this);

                    if (!DIALOGUEBOX.isPlaying) {
                        System.out.println("YESSSS");
                        world.loadMap(tr.getLink(), true);
                        update();
                    }
                    System.out.println(DIALOGUEBOX.isPlaying);
                }

                // E key: interact with the NPC or object the player is facing
                if (KEYBOARDHANDLER.ePressed) {
                    KEYBOARDHANDLER.ePressed = false;
                    player.checkInteraction();
                }

                // Open menu with ESC key
                if (KEYBOARDHANDLER.escPressed) {
                    KEYBOARDHANDLER.escPressed = false;
                    MENUUI.open();
                    GAMESTATE = "menu";
                    System.out.println("[GamePanel] Menu opened via ESC key.");
                }
                break;

            case "DIALOGUE":
                DIALOGUEBOX.update();
                break;

            case "SHOP":
                SHOPUI.update();
                break;

            case "PC":
                PCUI.update();
                break;

            case "MENU":
                MENUUI.update();
                break;

            case "INVENTORY":
               INVENTORYUI.update();
               break;

            default:
                break;
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // World bottom layers
        world.draw(g2);

        // NPCs
        for (NPC npc : world.getInteractiveLayer().getNPCs()) {
            if (npc != null) npc.draw(g2, this);
        }

        // Player
        player.draw(g2);

        // Draw world top layers (decorations that overlap player)
//        world.drawTop(g2);

        // ── UI overlays ───────────────────────────────────────────────────────
        switch (GAMESTATE.toLowerCase()) {
            case "dialogue":
                DIALOGUEBOX.draw(g2);
                break;
            case "shop":
                SHOPUI.draw(g2);
                break;
            case "menu":
                MENUUI.draw(g2);
                break;
            case "pc":
                PCUI.draw(g2);
                break;
            case "inventory":
                INVENTORYUI.draw(g2);
                break;

        }

        g2.dispose();
    }

}