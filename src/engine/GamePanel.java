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
import java.util.ArrayList;

import items.ItemRegistry;
import utils.RandomUtil;
import utils.Directories;

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

    // --- INTEGRATED OUR CUSTOM UI ---
    public BlackFadeEffect BLACKFADEEFFECT = new BlackFadeEffect();
    public BattleUI BATTLEUI               = new BattleUI(this, KEYBOARDHANDLER);
    public StarterUI STARTERUI             = new StarterUI(this, KEYBOARDHANDLER);

    public final ShopUI SHOPUI            = new ShopUI(this);
    public final PCUI     PCUI            = new PCUI(this, player.getPCSYSTEM());
    public final QuestUI QUESTUI          = new QuestUI(this);
    public final QuestToast QUESTTOAST    = new QuestToast();
    public final MenuUI MENUUI            = new MenuUI(this);
    public final InventoryUI INVENTORYUI  = new InventoryUI(this);
    public final MapUI MAPUI              = new MapUI(this);

    public final DarknessOverlay DARKNESSOVERLAY = new DarknessOverlay();

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
//        testQuests();
//        seedTestParty();

        // --- NEW: Force the player to the Starter Lab if they have no BrainRots! ---
        if (player.getPCSYSTEM().getPartySize() == 0) {
            GAMESTATE = "starter";
        }
    }

    // ── Test seed ─────────────────────────────────────────────────────────────

    private void seedTestParty() {
        // [YOUR ORIGINAL SEED LOGIC REMAINS UNCHANGED HERE]
        PCSystem PCSYSTEM = player.getPCSYSTEM();
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", Tier.NORMAL));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    Tier.GOLD));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", Tier.DIAMOND));

        player.getInventory().addItem(ItemRegistry.getItem("MILD STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("MODERATE STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("NORMAL CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("MASTER CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("Focus Stance Scroll"));

        for (brainrots.BrainRot rot : PCSYSTEM.getParty()) {
            java.util.List<brainrots.LevelUpResult> results = rot.gainXp(RandomUtil.range(100,10000));
        }
        System.out.println("[DEV] XP awarded.");
    }

    private void testQuests() {
        // [YOUR ORIGINAL QUEST TEST LOGIC REMAINS UNCHANGED HERE]
        progression.QuestSystem qs = progression.QuestSystem.getInstance();
        qs.complete("SPEED_DEMON");
        System.out.println("[DEV] Quests force-completed for testing.");
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

            // --- INTEGRATED OUR STARTER LAB STATE ---
            case "STARTER":
                STARTERUI.update();
                break;

            case "PLAY":
                // --- INTEGRATED OUR FADE EFFECT ---
                if (!BLACKFADEEFFECT.isFadeOutComplete()) {
                    BLACKFADEEFFECT.update();
                }

                // Update player movement
                player.update();

                // Check trainer line-of-sight every tick
                encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);

                // --- INTEGRATED OUR BATTLE FADE TRANSITION ---
                if (encounterSystem.getActiveBattle() != null && !GAMESTATE.equalsIgnoreCase("battle_fade") && !GAMESTATE.equalsIgnoreCase("battle")) {
                    GAMESTATE = "battle_fade";
                    BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 10);
                }

                //Check if theres a TileTeleporter in current position
                TileTeleporter tr = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
                if (tr != null) {

                    //If there is, then open dialogue
                    if (!tr.isInteracted) {
                        TileTeleporter tile = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
                        tr.interact(this);
                    }

                    //If dialogue is done
                    if (!DIALOGUEBOX.isPlaying) {
                        String link = tr.getLinkTo();
                        world.loadMap(Directories.getPath(link), true);
                        CURRENT_PATH = Directories.getPath(link);
                        DARKNESSOVERLAY.setActive(CURRENT_PATH.toLowerCase().contains("cave"));
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

                if (KEYBOARDHANDLER.mPressed && !CURRENT_PATH.toLowerCase().contains("cave")) {
                    KEYBOARDHANDLER.mPressed = false;
                    MAPUI.open();
                    GAMESTATE = "map";
                    System.out.println("[GamePanel] Map opened via M key.");
                }

                break;

            // --- INTEGRATED OUR BATTLE STATES ---
            case "BATTLE_FADE":
                BLACKFADEEFFECT.update();
                if (BLACKFADEEFFECT.isFullyBlack()) {
                    BATTLEUI.setBattle(encounterSystem.getActiveBattle());
                    GAMESTATE = "battle";
                    BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 10);
                }
                break;

            case "BATTLE":
                BATTLEUI.update();
                BLACKFADEEFFECT.update();
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
                // --- INTEGRATED BATTLE-ITEM RETURN LOGIC ---
                if (encounterSystem.getActiveBattle() != null && INVENTORYUI.getSelectedItemForBattle() != null) {
                    GAMESTATE = "battle";
                }
                if (KEYBOARDHANDLER.escPressed && encounterSystem.getActiveBattle() != null) {
                    GAMESTATE = "battle";
                }
                break;

            case "QUESTS":
                QUESTUI.update();
                break;

            case "MAP":
                MAPUI.update();
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

        // --- INTEGRATED OUR CUSTOM RENDER PATHS ---
        if (GAMESTATE.equalsIgnoreCase("STARTER")) {
            STARTERUI.draw(g2);
        } else if (GAMESTATE.equalsIgnoreCase("BATTLE") || (GAMESTATE.equalsIgnoreCase("INVENTORY") && encounterSystem.getActiveBattle() != null)) {
            BATTLEUI.draw(g2);
            if (GAMESTATE.equalsIgnoreCase("INVENTORY")) {
                INVENTORYUI.draw(g2);
            }
        } else {
            // World bottom layers
            world.draw(g2);

            // Player
            player.draw(g2);

            // Overlay layers (drawn on top of the player)
            world.drawOverlay(g2);

            // ── Cave darkness — after world, before UI ────────────────────────────
            DARKNESSOVERLAY.draw(
                    g2,
                    player.screenX + TILE_SIZE / 2,
                    player.screenY + TILE_SIZE / 2
            );
        }

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
                if (encounterSystem.getActiveBattle() == null) {
                    INVENTORYUI.draw(g2);
                }
                break;
            case "quests":
                QUESTUI.draw(g2);
                break;
            case "map":
                MAPUI.draw(g2);
                break;
        }

        QUESTTOAST.update();
        QUESTTOAST.draw(g2);

        // --- INTEGRATED UNIVERSAL BLACK FADE ---
        if (GAMESTATE.equalsIgnoreCase("BATTLE_FADE") || !BLACKFADEEFFECT.isFadeOutComplete()) {
            BLACKFADEEFFECT.draw(g2);
        }

        g2.dispose();
    }
}