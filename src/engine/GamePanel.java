package engine;

import brainrots.BrainRotFactory;
import brainrots.Tier;
import input.KeyboardHandler;
import map.WorldLoader;
import overworld.EncounterSystem;
import overworld.Player;
import storage.PCSystem;
import tile.CollisionChecker;
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
import java.util.Random;

import items.ItemRegistry;
import utils.RandomUtil;
import utils.Directories;
import utils.RandomUtil;

import static utils.Constants.*;
import static utils.Directories.*;


public class GamePanel extends JPanel {
    // ── Core handlers ─────────────────────────────────────────────────────────
    public KeyboardHandler KEYBOARDHANDLER      = new KeyboardHandler();
    public EncounterSystem encounterSystem      = new EncounterSystem();
    public CollisionChecker COLLISIONCHECKER    = new CollisionChecker(this);
    public Player player                        = new Player(this, KEYBOARDHANDLER);

    public String GAMESTATE               = "play";
    public DialogueBox DIALOGUEBOX        = new DialogueBox(this);

    public final ShopUI SHOPUI            = new ShopUI(this);
    public final PCUI     PCUI            = new PCUI(this, player.getPCSYSTEM());
    public final QuestUI QUESTUI          = new QuestUI(this);
    public final QuestToast QUESTTOAST    = new QuestToast();
    public final MenuUI MENUUI            = new MenuUI(this);
    public final InventoryUI INVENTORYUI  = new InventoryUI(this);

    public final WorldLoader world = new WorldLoader(this);
    public String CURRENT_PATH;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);this.requestFocusInWindow();
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(KEYBOARDHANDLER);

        world.loadMap(ROUTE131.getPath(), true);
        CURRENT_PATH = ROUTE131.getPath();

        // ── Seed the PC party with the player's starting team ────────────────
        // In a full game these would be loaded from save data.
        // For now we add test members so the PC UI has data to display.
//        seedTestParty();
        testQuests();
        seedTestParty();
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
        player.getInventory().addItem(ItemRegistry.getItem("NORMAL CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("RED CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("BLUE CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("SPEED CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("HEAVY CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("FIGHTING CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("WATER CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("PSYCHIC CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("FLYING CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("SAND CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("GRASS CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("ROCK CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("FIRE CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("DARK CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("POISON CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("MASTER CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("Focus Stance Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Sahur Chant Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Sneaker Dash Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Heat Burst Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Evasion Up Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Power Combo Scroll"));
        player.getInventory().addItem(ItemRegistry.getItem("Aqua Engine Scroll"));
        System.out.println("[GamePanel] Test party & items seeded.");

        for (brainrots.BrainRot rot : PCSYSTEM.getParty()) {
            java.util.List<brainrots.LevelUpResult> results = rot.gainXp(RandomUtil.range(100,10000));
            for (brainrots.LevelUpResult r : results) {
                System.out.println("[DEV] " + rot.getName()
                        + " → Lv." + r.newLevel
                        + " | +" + r.hpGain + "HP"
                        + " +" + r.atkGain + "ATK"
                        + " +" + r.defGain + "DEF"
                        + " +" + r.spdGain + "SPD"
                        + (r.skillUnlocked != null ? " | Learned: " + r.skillUnlocked.getName() : ""));
            }
        }
        System.out.println("[DEV] XP awarded.");

    }

    private void testQuests() {
        progression.QuestSystem qs = progression.QuestSystem.getInstance();

        // Boolean completions (valid)
        qs.complete("SPEED_DEMON");
        qs.complete("FLAWLESS_VICTORY");
        qs.complete("NO_HEALS");
        qs.complete("OVERKILL");

        qs.complete("FIRST_CATCH");
        qs.complete("DIAMOND_MIND");
        qs.complete("FULL_ROSTER");
        qs.complete("ORGANIZED");

        qs.complete("SKILL_COLLECTOR");

        qs.complete("VARIETY_PACK");

        // Counter-based (valid)
        for (int i = 0; i < 10; i++) qs.increment("ITEM_ADDICT");
        qs.increment("BIG_SPENDER", 9000);

        for (int i = 0; i < 4; i++) qs.increment("GROWING_COLLECTION");

        for (int i = 0; i < 10; i++) {
            qs.increment("THE_ETERNAL_DRUM");
            qs.increment("FRESH_KICKS");
            qs.increment("KING_OF_THE_JUNGLE");
            qs.increment("BURNOUT");
            qs.increment("LAST_DROP");
        }
        for (int i = 0; i < 5; i++) {
            qs.increment("AGAINST_THE_CLOCK");
            qs.increment("KING_OF_THE_JUNGLE");
            qs.increment("BURNOUT");
            qs.increment("FREQUENCY_DETECTED");
            qs.increment("LAST_DROP");
        }
        for (int i = 0; i < 4; i++) {
            qs.increment("THE_ETERNAL_DRUM");
            qs.increment("AGAINST_THE_CLOCK");
            qs.increment("KING_OF_THE_JUNGLE");
            qs.increment("BURNOUT");
        }

        // Hidden (valid)
        qs.complete("ITEM_HOARDER");
        qs.complete("BRAIN_FULLY_ROT");

        System.out.println("[DEV] Quests force-completed for testing.");

        for (brainrots.BrainRot rot : player.getPCSYSTEM().getParty()) {
            java.util.List<brainrots.LevelUpResult> results = rot.gainXp(RandomUtil.range(100,10000));
            for (brainrots.LevelUpResult r : results) {
                System.out.println("[DEV] " + rot.getName()
                        + " → Lv." + r.newLevel
                        + " | +" + r.hpGain + "HP"
                        + " +" + r.atkGain + "ATK"
                        + " +" + r.defGain + "DEF"
                        + " +" + r.spdGain + "SPD"
                        + (r.skillUnlocked != null ? " | Learned: " + r.skillUnlocked.getName() : ""));
            }
        }
        System.out.println("[DEV] XP awarded.");

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

    // ── Game loop ─────────────────────────────────────────────────────────────
    public void update() {
        switch (GAMESTATE.toUpperCase()) {

            case "PLAY":
                // Update player movement
                player.update();

                // Check trainer line-of-sight every tick
                encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);

                //Check if theres a TileTeleporter in current position
                TileTeleporter tr = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
                if (tr != null) {

                    //If there is, then open dialogue
                    if (!tr.isInteracted) {
                        TileTeleporter tile = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
                        System.out.println("TILE RIGHT NOW: " + tile.getCoordinates()[0] + "," + tile.getCoordinates()[1]);
                        System.out.println("PLAYER's POSITION: " + (player.worldX/TILE_SIZE) + "," + (player.worldY/TILE_SIZE));
                        tr.interact(this);
                    }

                    //If dialogue is done
                    if (!DIALOGUEBOX.isPlaying) {
                        String link = tr.getLinkTo();
                        world.loadMap(Directories.getPath(link), true);
                        CURRENT_PATH = Directories.getPath(link);
                        int[] coordinates = new int[2];
                        for (TileTeleporter tile : getWorldInteractiveLayer().getTeleporters()) {
                            if (tile != null) {
                                if (tile.getName().equalsIgnoreCase(tr.getLinkToTeleporterName())) {
                                    coordinates = tile.getCoordinates().clone();
                                    switch(tile.getDirection().toUpperCase()) {
                                        case "LEFT" -> coordinates[0] -= 1;
                                        case "RIGHT" -> coordinates[0] += 1;
                                        case "DOWN" -> coordinates[1] += 1;
                                        case "UP" -> coordinates[1] -= 1;
                                    }
                                }
                            }
                        }
                        player.teleport(coordinates);
                    }

                }

                // E key: interact with the NPC or object the player is facing
                if (KEYBOARDHANDLER.ePressed) {
                    KEYBOARDHANDLER.ePressed = false;
                    player.checkInteraction();
                    SHOPUI.open();
                    GAMESTATE = "SHOP";
                }

                // Open menu with ESC key
                if (KEYBOARDHANDLER.escPressed) {
                    KEYBOARDHANDLER.escPressed = false;
                    MENUUI.open();
                    GAMESTATE = "MENU";
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

            case "QUESTS":
                QUESTUI.update();
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
//        TileManager tm = world.getInteractiveLayer();
//        if (tm != null) {
//            for (NPC npc : tm.getNPCs()) {
//                if (npc != null) npc.draw(g2, this);
//            }
//        }

        // Player
        player.draw(g2);

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
            case "quests":
                QUESTUI.draw(g2);
                break;
        }

        QUESTTOAST.update();
        QUESTTOAST.draw(g2);
        g2.dispose();
    }

}