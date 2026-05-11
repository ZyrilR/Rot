package engine;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.Tier;
import input.KeyboardHandler;
import map.WorldLoader;
import overworld.EncounterSystem;
import overworld.Player;
import progression.QuestSystem;
import storage.PCSystem;
import npc.NPC;
import npc.TrainerNPC;
import tile.*;
import ui.*;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import items.ItemRegistry;
import utils.RandomUtil;
import utils.Directories;

import static utils.Constants.*;
import static utils.Directories.*;

public class GamePanel extends JPanel {
    // ── Core handlers ─────────────────────────────────────────────────────────
    public KeyboardHandler  KEYBOARDHANDLER  = new KeyboardHandler();
    public EncounterSystem  encounterSystem  = new EncounterSystem();
    public TileChecker      TILECHECKER      = new TileChecker(this);
    public Player           player           = new Player(this, KEYBOARDHANDLER);

    public String     GAMESTATE   = "splash";   // start on splash screen
    public DialogueBox DIALOGUEBOX = new DialogueBox(this);

    // ── UI systems ────────────────────────────────────────────────────────────
    public final SplashScreenUI   SPLASHSCREEN   = new SplashScreenUI(this);
    public final WorldSelectUI  WORLDSELECTUI  = new WorldSelectUI(this);

    public BlackFadeEffect BLACKFADEEFFECT = new BlackFadeEffect();
    public BattleUI        BATTLEUI        = new BattleUI(this, KEYBOARDHANDLER);
    public StarterUI       STARTERUI       = new StarterUI(this, KEYBOARDHANDLER);

    public final ShopUI      SHOPUI      = new ShopUI(this);
    public final PCUI        PCUI        = new PCUI(this, player.getPCSYSTEM());
    public final QuestUI     QUESTUI     = new QuestUI(this);
    public final QuestToast  QUESTTOAST  = new QuestToast();
    public final NotificationToast NOTIFICATION = new NotificationToast();
    public final TeleportEffect TELEPORTEFFECT = new TeleportEffect();
    public final MenuUI      MENUUI      = new MenuUI(this);
    public final InventoryUI INVENTORYUI = new InventoryUI(this);
    public final MapUI       MAPUI       = new MapUI(this);
    public final DevConsole DEVCONSOLE    = new DevConsole(this);
    public final BadgeUI    BADGEUI    = new BadgeUI(this);
    public final BadgeToast BADGETOAST = new BadgeToast();
    public final EndingUI ENDINGUI = new EndingUI(this);

    public final DarknessOverlay DARKNESSOVERLAY = new DarknessOverlay();

    public final WorldLoader world = new WorldLoader(this);
    public String CURRENT_PATH;
    private String  pendingTeleportPath;
    private String  pendingTeleportName;
    private TileTeleporter pendingTeleportTile;

    // ── Persistent runtime state (saved/loaded across slots) ──────────────────
    /** Game time in ticks (frames). Advances only while updatePlayState is running. */
    public long gameTime = 0;
    /** Keys ("mapPath@col,row") for loots already picked up. */
    public final Set<String> pickedLoots = new HashSet<>();
    /** Keys ("mapPath@col,row") for trainers defeated, mapped to gameTime when defeated. */
    public final Map<String, Long> defeatedTrainers = new HashMap<>();

    public long getGameTime() { return gameTime; }

    /** Camera X clamped to map bounds. Uses player's centered screenX as the desired offset. */
    public int getCameraX() {
        int worldW = MAX_WORLD_COL * TILE_SIZE;
        int desired = player.worldX - player.screenX;
        int max = Math.max(0, worldW - SCREEN_WIDTH);
        return Math.max(0, Math.min(desired, max));
    }

    /** Camera Y clamped to map bounds. */
    public int getCameraY() {
        int worldH = MAX_WORLD_ROW * TILE_SIZE;
        int desired = player.worldY - player.screenY;
        int max = Math.max(0, worldH - SCREEN_HEIGHT);
        return Math.max(0, Math.min(desired, max));
    }

    /** Player's actual on-screen position (accounts for clamped camera at map edges). */
    public int getPlayerScreenX() { return player.worldX - getCameraX(); }
    public int getPlayerScreenY() { return player.worldY - getCameraY(); }

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(KEYBOARDHANDLER);

        // Load the default map so the world is ready when needed
        System.out.println("[GamePanel] Loading Default Map");
        world.loadMap(ROUTE131.getPath(), true);
        CURRENT_PATH = ROUTE131.getPath();

        // Open splash on startup
        SPLASHSCREEN.open();
    }

    // ── Dev helpers (commented out for production) ────────────────────────────

    private void seedTestParty() {
        PCSystem PCSYSTEM = player.getPCSYSTEM();
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TUNG TUNG TUNG SAHUR", 15));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("TRALALERO TRALALA",    25));
        PCSYSTEM.addBrainRot(BrainRotFactory.create("BOMBARDINO CROCODILO", 30));

        player.getInventory().addItem(ItemRegistry.getItem("MILD STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("MODERATE STEW"));
        player.getInventory().addItem(ItemRegistry.getItem("NORMAL CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("MASTER CAPSULE"));
        player.getInventory().addItem(ItemRegistry.getItem("Focus Stance Scroll"));

        for (BrainRot rot : PCSYSTEM.getParty()) {
            rot.gainXp(RandomUtil.range(100, 10000));
        }
        System.out.println("[DEV] XP awarded.");
    }

    private void testQuests() {
        QuestSystem qs = QuestSystem.getInstance();
        qs.complete("SPEED_DEMON");
        System.out.println("[DEV] Quests force-completed for testing.");
    }

    // ── Layer accessors ───────────────────────────────────────────────────────

    public ArrayList<TileManager> getWorldBackgroundLayer() { return world.getBackgroundLayer(); }
    public ArrayList<TileManager> getWorldBuildingLayer()   { return world.getBuildingLayer(); }
    public TileManager            getWorldInteractiveLayer() { return world.getInteractiveLayer(); }

    /**
     * Re-applies persistent state to the freshly-loaded interactive layer:
     *  - removes already-picked loots from the map matrix and the loot ArrayList
     *  - restores defeated/cooldown state on trainer NPCs
     * Safe to call after every world.loadMap().
     */
    public void applyPersistentMapState() {
        TileManager interactive = world.getInteractiveLayer();
        if (interactive == null || CURRENT_PATH == null) return;

        // Prune picked-up loots
        Iterator<TileLoot> it = interactive.getLoots().iterator();
        while (it.hasNext()) {
            TileLoot tl = it.next();
            String key = CURRENT_PATH + "@" + tl.getX() + "," + tl.getY();
            if (pickedLoots.contains(key)) {
                int[][] map = interactive.getMap();
                if (tl.getY() >= 0 && tl.getY() < map.length
                        && tl.getX() >= 0 && tl.getX() < map[0].length) {
                    map[tl.getY()][tl.getX()] = 0;
                }
                it.remove();
            }
        }

        // Restore defeated/cooldown state for trainers on this map
        for (NPC npc : interactive.getNPCs()) {
            if (npc instanceof TrainerNPC trainer) {
                String key = CURRENT_PATH + "@" + (trainer.worldX / TILE_SIZE) + "," + (trainer.worldY / TILE_SIZE);
                Long defAt = defeatedTrainers.get(key);
                if (defAt != null) {
                    boolean permanent = trainer instanceof npc.GymLeader
                            || trainer instanceof npc.GymMaster;
                    if (!permanent && gameTime - defAt >= TrainerNPC.COOLDOWN_TICKS) {
                        defeatedTrainers.remove(key);
                    } else {
                        trainer.restoreDefeatedState(defAt);
                    }
                }
            }
        }
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    public void update() {
        BLACKFADEEFFECT.update();

        switch (GAMESTATE.toLowerCase()) {
            case "splash"       -> SPLASHSCREEN.update();
            case "world_select" -> WORLDSELECTUI.update();
            case "starter"      -> STARTERUI.update();
            case "play"         -> updatePlayState();
            case "battle_fade"  -> {
                if (BLACKFADEEFFECT.isFullyBlack()) {
                    BATTLEUI.setBattle(encounterSystem.getActiveBattle());
                    GAMESTATE = "battle";
                    BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_OUT_TO_PLAY, 10);
                }
            }
            case "battle" -> {
                BATTLEUI.update();
                if (encounterSystem.getActiveBattle() == null) GAMESTATE = "play";
            }
            case "teleport"  -> updateTeleportState();
            case "dialogue"  -> DIALOGUEBOX.update();
            case "shop"      -> SHOPUI.update();
            case "pc"        -> PCUI.update();
            case "menu"      -> MENUUI.update();
            case "badges" -> BADGEUI.update();
            case "quests"    -> QUESTUI.update();
            case "map"       -> MAPUI.update();
            case "ending"    -> ENDINGUI.update();
            case "inventory" -> {
                INVENTORYUI.update();
                if (encounterSystem.getActiveBattle() != null
                        && (INVENTORYUI.getSelectedItemForBattle() != null
                        || KEYBOARDHANDLER.escPressed)) {
                    GAMESTATE = "battle";
                }
            }
        }
    }

    private void updatePlayState() {
        if (KEYBOARDHANDLER.consolePressed) {
            KEYBOARDHANDLER.consolePressed = false;
            DEVCONSOLE.toggle();
            KEYBOARDHANDLER.consoleTypingMode = DEVCONSOLE.isOpen();
        }

        // Route typed characters to the console while it is open
        if (DEVCONSOLE.isOpen()) {
            if (KEYBOARDHANDLER.lastTypedChar != 0) {
                DEVCONSOLE.typeChar(KEYBOARDHANDLER.lastTypedChar);
                KEYBOARDHANDLER.lastTypedChar = 0;
            }
            if (KEYBOARDHANDLER.backspaceHit) {
                DEVCONSOLE.backspace();
                KEYBOARDHANDLER.backspaceHit = false;
            }
            if (KEYBOARDHANDLER.enterHit) {
                DEVCONSOLE.submit();
                KEYBOARDHANDLER.enterHit = false;
            }
            if (KEYBOARDHANDLER.escPressed) {
                KEYBOARDHANDLER.escPressed = false;
                DEVCONSOLE.close();
                KEYBOARDHANDLER.consoleTypingMode = false;
            }
            // UP/DOWN → scroll history; LEFT/RIGHT → move text cursor
            if (KEYBOARDHANDLER.upPressed) {
                KEYBOARDHANDLER.upPressed = false;
                DEVCONSOLE.scrollUp();
            }
            if (KEYBOARDHANDLER.downPressed) {
                KEYBOARDHANDLER.downPressed = false;
                DEVCONSOLE.scrollDown();
            }
            if (KEYBOARDHANDLER.leftPressed) {
                KEYBOARDHANDLER.leftPressed = false;
                DEVCONSOLE.moveCursorLeft();
            }
            if (KEYBOARDHANDLER.rightPressed) {
                KEYBOARDHANDLER.rightPressed = false;
                DEVCONSOLE.moveCursorRight();
            }
            DEVCONSOLE.update();
            return;
        }

        gameTime++;
        // Keep the cave darkness overlay in sync with the current map every tick.
        // Catches edge cases (save loads, dev-console teleport, exit-via-warp) where
        // setActive(false) might otherwise be missed when leaving a cave.
        DARKNESSOVERLAY.setActive(CURRENT_PATH != null && CURRENT_PATH.toLowerCase().contains("cave"));
        player.update();
        encounterSystem.checkTrainerLook(player, world.getInteractiveLayer().getNPCs(), this);

        if (encounterSystem.getActiveBattle() != null) {
            GAMESTATE = "battle_fade";
            BLACKFADEEFFECT.start(BlackFadeEffect.FadeMode.FADE_IN_TO_BLACK, 10);
            return;
        }

        TileTeleporter tr = TileChecker.getTeleporterTileInCurrentPosition(this, player);
        if (tr != null) handleTeleport(tr);

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

        progression.NarrativeManager.checkTriggers(this);
    }
    private void updateTeleportState() {
        boolean phaseDone = TELEPORTEFFECT.update();
        if (!phaseDone) return;

        switch (TELEPORTEFFECT.getPhase()) {
            case SPIN_IN -> {
                // 1. CAPTURE THE OLD PATH
                String oldPath = CURRENT_PATH;

                // Swap maps, reposition player, then spin out
                TELEPORTEFFECT.markReadyToSwap();
                CURRENT_PATH = pendingTeleportPath;
                world.loadMap(CURRENT_PATH, true);
                DARKNESSOVERLAY.setActive(CURRENT_PATH.toLowerCase().contains("cave"));

                // 2. CHECK IF WE NEED TO CHANGE MUSIC
                if (oldPath != null && !oldPath.equalsIgnoreCase(CURRENT_PATH)) {
                    updateMusic();
                }

                int[] coords = new int[2];
                for (TileTeleporter tile : getWorldInteractiveLayer().getTeleporters()) {
                    if (tile != null && tile.getName().equalsIgnoreCase(pendingTeleportName)) {
                        coords = tile.getCoordinates().clone();
                        switch (tile.getDirection().toUpperCase()) {
                            case "LEFT"  -> coords[0] -= 1;
                            case "RIGHT" -> coords[0] += 1;
                            case "DOWN"  -> coords[1] += 1;
                            case "UP"    -> coords[1] -= 1;
                        }
                    }
                }
                player.teleport(coords);
                if (pendingTeleportTile != null) pendingTeleportTile.isInteracted = false;
                TELEPORTEFFECT.startSpinOut();
            }
            case SPIN_OUT -> {
                TELEPORTEFFECT.stop();
                pendingTeleportPath = null;
                pendingTeleportName = null;
                pendingTeleportTile = null;
                GAMESTATE = "play";
            }
            default -> {}
        }
    }

    private void handleTeleport(TileTeleporter tr) {
        Directories currentMapData = getByPath(CURRENT_PATH);
        String targetPath = getPath(tr.getLinkTo());

        if (!targetPath.equalsIgnoreCase(CURRENT_PATH)) {
            int totalRots = player.getPCSYSTEM().getPartySize() + player.getPCSYSTEM().getPCCount();
            BrainRot lead = player.getPCSYSTEM().getPartyMember(0);

            if (totalRots < currentMapData.getReqRots()
                    || (lead != null && lead.getLevel() < currentMapData.getReqLevel())) {
                ArrayList<String> warning = new ArrayList<>();
                warning.add("The path ahead is blocked!");
                warning.add("Mastery of " + currentMapData.name() + " required:");
                warning.add("- Own " + currentMapData.getReqRots() + " BrainRots (You: " + totalRots + ")");
                warning.add("- Partner Lv. " + currentMapData.getReqLevel()
                        + " (You: " + (lead != null ? lead.getLevel() : 0) + ")");
                DIALOGUEBOX.startDialogue("System", warning);

                player.worldX -= (player.getDirection().equals("right") ? TILE_SIZE : 0);
                player.worldX += (player.getDirection().equals("left")  ? TILE_SIZE : 0);
                player.worldY -= (player.getDirection().equals("down")  ? TILE_SIZE : 0);
                player.worldY += (player.getDirection().equals("up")    ? TILE_SIZE : 0);
                return;
            }
        }

        if (!tr.isInteracted) tr.interact(this);

        if (!DIALOGUEBOX.isPlaying && !TELEPORTEFFECT.isActive()) {
            pendingTeleportPath = targetPath;
            pendingTeleportName = tr.getLinkToTeleporterName();
            pendingTeleportTile = tr;
            utils.AudioManager.playSFX(utils.Constants.SFX_TELEPORT);
            TELEPORTEFFECT.startSpinIn();
            GAMESTATE = "teleport";
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        switch (GAMESTATE.toLowerCase()) {

            case "splash" -> SPLASHSCREEN.draw(g2);

            case "world_select" -> WORLDSELECTUI.draw(g2);

            case "starter" -> STARTERUI.draw(g2);

            case "battle" -> {
                if (encounterSystem.getActiveBattle() != null
                        && encounterSystem.getActiveBattle().getEnemyRot() != null) {
                    BATTLEUI.draw(g2);
                }
            }
            case "inventory" -> {
                if (encounterSystem.getActiveBattle() != null) {
                    // Battle inventory: draw battle UI behind it
                    BATTLEUI.draw(g2);
                } else {
                    // Overworld inventory: draw world behind it
                    world.draw(g2);
                    player.draw(g2);
                    DARKNESSOVERLAY.draw(g2,
                            getPlayerScreenX() + TILE_SIZE / 2,
                            getPlayerScreenY() + TILE_SIZE / 2);
                }
                INVENTORYUI.draw(g2);
            }

            default -> {
                // Overworld + fade (covers play, battle_fade, dialogue, shop, etc.)
                world.draw(g2);
                player.draw(g2);
                DARKNESSOVERLAY.draw(g2,
                        getPlayerScreenX() + TILE_SIZE / 2,
                        getPlayerScreenY() + TILE_SIZE / 2);

                // UI overlays on top of the world
                switch (GAMESTATE.toLowerCase()) {
                    case "dialogue"  -> DIALOGUEBOX.draw(g2);
                    case "shop"      -> SHOPUI.draw(g2);
                    case "menu"      -> MENUUI.draw(g2);
                    case "pc"        -> PCUI.draw(g2);
                    case "inventory" -> INVENTORYUI.draw(g2);
                    case "badges"    -> BADGEUI.draw(g2);
                    case "quests"    -> QUESTUI.draw(g2);
                    case "map"       -> MAPUI.draw(g2);
                    case "ending"    -> ENDINGUI.draw(g2);
                }
            }
        }

        // Quest toast renders over everything except the fade
        QUESTTOAST.update();
        QUESTTOAST.draw(g2);

        BADGETOAST.update();
        BADGETOAST.draw(g2);

        NOTIFICATION.update();
        NOTIFICATION.draw(g2);

        // Fade is the absolute top layer
        if (!BLACKFADEEFFECT.isFadeOutComplete()) {
            BLACKFADEEFFECT.draw(g2);
        }

        DEVCONSOLE.draw(g2);
        g2.dispose();
    }

    /**
     * Checks the current map path and plays the correct background music.
     */
    /**
     * Checks the current map path and plays the correct background music.
     */
    public void updateMusic() {
        if (CURRENT_PATH == null) return;

        String pathLower = CURRENT_PATH.toLowerCase();

        if (pathLower.contains("cave")) {
            utils.AudioManager.playMusic(utils.Constants.BGM_CAVE, true);
        }
        // ── NEW: WATER MAPS MUSIC ──
        else if (pathLower.contains("dewdroppier") ||
                pathLower.contains("watergymfloor1") ||
                pathLower.contains("watergymfloor2")) {
            utils.AudioManager.playMusic(utils.Constants.BGM_WATER, true);
        }
        // ── DEFAULT OVERWORLD MUSIC ──
        else {
            utils.AudioManager.playMusic(utils.Constants.BGM_OVERWORLD, true);
        }
    }
}