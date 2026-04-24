package engine;

import brainrots.BrainRot;
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
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(KEYBOARDHANDLER);

        world.loadMap(ROUTE131.getPath(), true);
        CURRENT_PATH = ROUTE131.getPath();

        // ── Seed the PC party with the player's starting team ────────────────
//        testQuests();
//        seedTestParty();

        // --- NEW: Force the player to the Starter Lab if they have no BrainRots! ---
//        if (player.getPCSYSTEM().getPartySize() == 0) {
//            GAMESTATE = "starter";
//        } else {
//            GAMESTATE = "play";
//        }
    }


    // ── Test seed ─────────────────────────────────────────────────────────────

    private void seedTestParty() {
        // [YOUR ORIGINAL SEED LOGIC REMAINS UNCHANGED HERE]
        PCSystem PCSYSTEM = player.getPCSYSTEM();
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", 15));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    25));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", 30));

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
        BLACKFADEEFFECT.update();

        switch (GAMESTATE.toUpperCase()) {
            case "STARTER" -> STARTERUI.update();
            case "PLAY"    -> updatePlayState();
            case "BATTLE_FADE" -> {
                if (BLACKFADEEFFECT.isFullyBlack()) {
                    BATTLEUI.setBattle(encounterSystem.getActiveBattle());
                    GAMESTATE = "battle";
                    BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 10);
                }
            }
            case "BATTLE" -> {
                BATTLEUI.update();
                if (encounterSystem.getActiveBattle() == null) GAMESTATE = "play";
            }
            case "DIALOGUE"  -> DIALOGUEBOX.update();
            case "SHOP"      -> SHOPUI.update();
            case "PC"        -> PCUI.update();
            case "MENU"      -> MENUUI.update();
            case "QUESTS"    -> QUESTUI.update();
            case "MAP"       -> MAPUI.update();
            case "INVENTORY" -> {
                INVENTORYUI.update();
                if (encounterSystem.getActiveBattle() != null && (INVENTORYUI.getSelectedItemForBattle() != null || KEYBOARDHANDLER.escPressed)) {
                    GAMESTATE = "battle";
                }
            }
        }
    }

    private void updatePlayState() {
        player.update();
        encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);

        // BUG FIX: Removed checkWildEncounter from here!
        // It belongs in your Player.java class when the player moves to a new tile.

        if (encounterSystem.getActiveBattle() != null) {
            GAMESTATE = "battle_fade";
            BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 10);
            return;
        }

        TileTeleporter tr = CollisionChecker.getTeleporterTileInCurrentPosition(this, player);
        if (tr != null) {
            handleTeleport(tr);
        }

        if (KEYBOARDHANDLER.ePressed) {
            KEYBOARDHANDLER.ePressed = false;
            player.checkInteraction();
        }

        if (KEYBOARDHANDLER.escPressed) {
            KEYBOARDHANDLER.escPressed = false;
            MENUUI.open();
            GAMESTATE = "MENU";
        }

        if (KEYBOARDHANDLER.mPressed && !CURRENT_PATH.toLowerCase().contains("cave")) {
            KEYBOARDHANDLER.mPressed = false;
            MAPUI.open();
            GAMESTATE = "map";
        }
    }

    private void handleTeleport(TileTeleporter tr) {
        // 1. Identification
        Directories currentMapData = Directories.getByPath(CURRENT_PATH);
        String targetPath = Directories.getPath(tr.getLinkTo());

        // 2. Progression Check (Strictly enforcing your requirements)
        if (!targetPath.equalsIgnoreCase(CURRENT_PATH)) {
            int totalRots = player.getPCSYSTEM().getPartySize() + player.getPCSYSTEM().getPCCount();
            BrainRot lead = player.getPCSYSTEM().getPartyMember(0);

            if (totalRots < currentMapData.getReqRots() || (lead != null && lead.getLevel() < currentMapData.getReqLevel())) {
                ArrayList<String> warning = new ArrayList<>();
                warning.add("The path ahead is blocked!");
                warning.add("Mastery of " + currentMapData.name() + " required:");
                warning.add("- Own " + currentMapData.getReqRots() + " BrainRots (You: " + totalRots + ")");
                warning.add("- Partner Lv. " + currentMapData.getReqLevel() + " (You: " + (lead != null ? lead.getLevel() : 0) + ")");
                DIALOGUEBOX.startDialogue("System", warning);

                // Push back
                player.worldX -= (player.getDirection().equals("right") ? TILE_SIZE : 0);
                player.worldX += (player.getDirection().equals("left") ? TILE_SIZE : 0);
                player.worldY -= (player.getDirection().equals("down") ? TILE_SIZE : 0);
                player.worldY += (player.getDirection().equals("up") ? TILE_SIZE : 0);
                return;
            }
        }

        if (!tr.isInteracted) tr.interact(this);

        if (!DIALOGUEBOX.isPlaying) {
            CURRENT_PATH = targetPath;
            world.loadMap(CURRENT_PATH, true);
            DARKNESSOVERLAY.setActive(CURRENT_PATH.toLowerCase().contains("cave"));

            int[] coords = new int[2];
            for (TileTeleporter tile : getWorldInteractiveLayer().getTeleporters()) {
                if (tile != null && tile.getName().equalsIgnoreCase(tr.getLinkToTeleporterName())) {
                    coords = tile.getCoordinates().clone();
                    switch(tile.getDirection().toUpperCase()) {
                        case "LEFT"  -> coords[0] -= 1;
                        case "RIGHT" -> coords[0] += 1;
                        case "DOWN"  -> coords[1] += 1;
                        case "UP"    -> coords[1] -= 1;
                    }
                }
            }
            player.teleport(coords);
            tr.isInteracted = false;
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (GAMESTATE.equalsIgnoreCase("STARTER")) {
            STARTERUI.draw(g2);
        }
        // FIX: Removed "BATTLE_FADE" from here!
        // We only draw the BattleUI when we are actually IN the battle state.
        else if (GAMESTATE.equalsIgnoreCase("BATTLE") ||
                (GAMESTATE.equalsIgnoreCase("INVENTORY") && encounterSystem.getActiveBattle() != null)) {

            // Only draw if the battle manager is actually created
            if (encounterSystem.getActiveBattle() != null) {
                BATTLEUI.draw(g2);
                if (GAMESTATE.equalsIgnoreCase("INVENTORY")) INVENTORYUI.draw(g2);
            }
        } else {
            // FIX: During "BATTLE_FADE", it now falls into this block.
            // This keeps the Overworld visible WHILE the screen smoothly fades to black!
            world.draw(g2);
            player.draw(g2);
            DARKNESSOVERLAY.draw(g2, player.screenX + TILE_SIZE / 2, player.screenY + TILE_SIZE / 2);
        }

        // Draw UI overlays depending on the state
        switch (GAMESTATE.toLowerCase()) {
            case "dialogue"  -> DIALOGUEBOX.draw(g2);
            case "shop"      -> SHOPUI.draw(g2);
            case "menu"      -> MENUUI.draw(g2);
            case "pc"        -> PCUI.draw(g2);
            case "inventory" -> { if (encounterSystem.getActiveBattle() == null) INVENTORYUI.draw(g2); }
            case "quests"    -> QUESTUI.draw(g2);
            case "map"       -> MAPUI.draw(g2);
        }

        QUESTTOAST.update();
        QUESTTOAST.draw(g2);

        // Top-most layer: The Fade Effect smoothly draws over EVERYTHING
        if (!BLACKFADEEFFECT.isFadeOutComplete()) {
            BLACKFADEEFFECT.draw(g2);
        }

        g2.dispose();
    }
}