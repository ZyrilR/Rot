package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import items.Inventory;
import npc.HealerMachine;
import storage.PCSystem;
import tile.TileChecker;
import tile.TileLoot;
import tile.TileManager;
import tile.TileTeleporter;
import ui.DevConsole;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import static utils.Constants.*;

public class Player {

    //Player Attributes
    private final Inventory inventory;
    private int rotCoins; // in-game currency
    private final PCSystem PCSYSTEM = new PCSystem();

    public String name = "DIN";
    public int worldX, worldY; // map position
    public final int screenX, screenY; // where we draw player on screen
    public int speed;

    private String direction;
    private boolean isMoving;
    private int moveProgress = 0;
    private boolean isWalking = false;

    //Player Handler
    public GamePanel gp;
    public KeyboardHandler kh;

    //Handle Sprite Images
    ArrayList<BufferedImage> walk_up, walk_down, walk_right, walk_left;
    private int spriteCounter;
    private int spriteAccumulator;
    private int currentFrameIndex;

    // ── Movement / animation tuning ────────────────────────────────────────
    /** Base speed used as the reference point for animation pacing. */
    public static final int BASE_SPEED = 4;
    /** Pixels of movement required to advance one walk-cycle frame. */
    public static final int ANIM_FRAME_DISTANCE = 24;
    /** Maximum bonus added to base speed at full sprint. */
    public static final int MAX_SPRINT_BONUS = 12;
    /** Frames it takes for the sprint to fully ramp up from 0 → max bonus. */
    public static final int SPRINT_RAMP_FRAMES = 24;

    /** How many consecutive frames the sprint key has been held while moving. */
    private int sprintTicks = 0;

    //Collision Handling
    public Rectangle solidArea;
    public boolean collisionOn = false;

    // Ground level (ramps change this: going up = +1, going down = -1)
    public int groundLevel = 0;

    public Player(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;

        inventory = new Inventory(99);
        rotCoins = 2500; //for testing

        worldX = TILE_SIZE * 24;
        worldY = TILE_SIZE * 24;
        screenX = (SCREEN_WIDTH / 2) - (TILE_SIZE / 2);
        screenY = (SCREEN_HEIGHT / 2) - (TILE_SIZE / 2);
        speed = 8;

        direction = "down";
        walk_up = new ArrayList<>();
        walk_down = new ArrayList<>();
        walk_right = new ArrayList<>();
        walk_left = new ArrayList<>();
        resetSpriteCounter();
        isMoving = false;

        solidArea = new Rectangle(0, 0, TILE_SIZE, TILE_SIZE);

        loadImage();
    }

    public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }
    public String getDirection() { return direction; }
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Inventory getInventory() {
        return inventory;
    }
    public int getCurrentSpeed() { return computeCurrentSpeed(); }

    /**
     * Sprint speed formula: ramps from base speed up to (speed + MAX_SPRINT_BONUS)
     * over SPRINT_RAMP_FRAMES while the sprint key is held. Released → instant reset.
     * Tile movement still snaps to the grid (Player.update handles overshoot), so any
     * non-divisible speed is fine here.
     */
    private int computeCurrentSpeed() {
        boolean sprinting = (kh != null && (kh.running || kh.shiftPressed));
        if (!sprinting) return speed;
        int bonus = Math.min(MAX_SPRINT_BONUS,
                (sprintTicks * MAX_SPRINT_BONUS) / Math.max(1, SPRINT_RAMP_FRAMES));
        return speed + bonus;
    }

    // --- Currency methods ---
    public int getRotCoins() {
        return rotCoins;
    }

    public boolean spendRotCoins(int amount) {
        if (rotCoins >= amount) {
            rotCoins -= amount;
            return true;
        } else {
            System.out.println("Not enough RotCoins!");
            return false;
        }
    }

    public void earnRotCoins(int amount) {
        rotCoins += amount;
    }

    public void loadImage() {
        walk_down.add(AssetManager.loadImage("/res/InteractiveTiles/player/1.png"));
        walk_down.add(AssetManager.loadImage("/res/InteractiveTiles/player/2.png"));
        walk_down.add(AssetManager.loadImage("/res/InteractiveTiles/player/3.png"));
        walk_up.add(AssetManager.loadImage("/res/InteractiveTiles/player/4.png"));
        walk_up.add(AssetManager.loadImage("/res/InteractiveTiles/player/5.png"));
        walk_up.add(AssetManager.loadImage("/res/InteractiveTiles/player/6.png"));
        walk_right.add(AssetManager.loadImage("/res/InteractiveTiles/player/7.png"));
        walk_right.add(AssetManager.loadImage("/res/InteractiveTiles/player/8.png"));
        walk_right.add(AssetManager.loadImage("/res/InteractiveTiles/player/8.png"));
        walk_right.add(AssetManager.loadImage("/res/InteractiveTiles/player/9.png"));
        walk_right.add(AssetManager.loadImage("/res/InteractiveTiles/player/10.png"));
        walk_left.add(AssetManager.loadImage("/res/InteractiveTiles/player/11.png"));
        walk_left.add(AssetManager.loadImage("/res/InteractiveTiles/player/12.png"));
        walk_left.add(AssetManager.loadImage("/res/InteractiveTiles/player/12.png"));
        walk_left.add(AssetManager.loadImage("/res/InteractiveTiles/player/13.png"));
        walk_left.add(AssetManager.loadImage("/res/InteractiveTiles/player/14.png"));
    }

    public void resetSpriteCounter() {
        spriteCounter = 0;
        spriteAccumulator = 0;
        currentFrameIndex = 0;
    }

    public void draw(Graphics2D g) {
        BufferedImage img = null;

        if (gp.TELEPORTEFFECT.isActive()) {
            String dir = gp.TELEPORTEFFECT.getDirectionFrame();
            if (dir != null) {
                switch (dir) {
                    case "up"    -> img = walk_up.getFirst();
                    case "down"  -> img = walk_down.getFirst();
                    case "right" -> img = walk_right.getFirst();
                    case "left"  -> img = walk_left.getFirst();
                }
            }
        } else if (isMoving) {
            // Frame index advances proportional to current movement speed:
            //   advance one frame for every ANIM_FRAME_DISTANCE pixels travelled.
            spriteAccumulator += computeCurrentSpeed();
            while (spriteAccumulator >= ANIM_FRAME_DISTANCE) {
                spriteAccumulator -= ANIM_FRAME_DISTANCE;
                currentFrameIndex++;
            }
            switch (direction) {
                case "up"    -> img = walk_up.get(currentFrameIndex % walk_up.size());
                case "down"  -> img = walk_down.get(currentFrameIndex % walk_down.size());
                case "right" -> img = walk_right.get(currentFrameIndex % walk_right.size());
                case "left"  -> img = walk_left.get(currentFrameIndex % walk_left.size());
            }
        } else {
            switch (direction) {
                case "up" -> img = walk_up.getFirst();
                case "down" -> img = walk_down.getFirst();
                case "right" -> img = walk_right.getFirst();
                case "left" -> img = walk_left.getFirst();
            }
            resetSpriteCounter();
        }

        if (img != null) {
            int sx = worldX - gp.getCameraX();
            int sy = worldY - gp.getCameraY();
            g.drawImage(img, sx, sy, TILE_SIZE, TILE_SIZE, null);
        }
    }

    public void update() {
        // Sprint ramp-up tick: held while moving = builds, released = resets.
        boolean sprintHeld = (kh.running || kh.shiftPressed);
        if (sprintHeld && (isWalking || kh.isMoving())) {
            if (sprintTicks < SPRINT_RAMP_FRAMES) sprintTicks++;
        } else {
            sprintTicks = 0;
        }

        if (isWalking) {
            // Continue moving if already in a tile transition
            int currentSpeed = computeCurrentSpeed();

            switch (direction) {
                case "up"    -> worldY -= currentSpeed;
                case "down"  -> worldY += currentSpeed;
                case "left"  -> worldX -= currentSpeed;
                case "right" -> worldX += currentSpeed;
            }
            moveProgress += currentSpeed;

            if (moveProgress >= TILE_SIZE) {
                // Grid Snapping
                worldX = Math.round((float)worldX / TILE_SIZE) * TILE_SIZE;
                worldY = Math.round((float)worldY / TILE_SIZE) * TILE_SIZE;
                isWalking = false;
                moveProgress = 0;
                setIsMoving(false);

                // 3. CHECK FOR TALL GRASS ENCOUNTERS
                checkGrass();

                // 4. CHECK FOR DOORS/WARPS
                checkWarps();
            }
        } else if (kh.isMoving()) {
            // Start a new move
            if (kh.upPressed) direction = "up";
            else if (kh.downPressed) direction = "down";
            else if (kh.leftPressed) direction = "left";
            else if (kh.rightPressed) direction = "right";

            collisionOn = false;

            // 1. CHECK WALLS/BUILDINGS
            gp.TILECHECKER.checkTile(this);

            // 2. CHECK NPCS (This is what stops you from ghosting!)
            gp.TILECHECKER.checkNPC(this, gp.getWorldInteractiveLayer().getNPCs());

            if (!collisionOn) {
                isWalking = true;
                moveProgress = 0;
                setIsMoving(true);
            }
        }
    }

    public void checkInteraction() {
        // Find exactly which grid tile the player is currently standing on
        int currentGridX = worldX / TILE_SIZE;
        int currentGridY = worldY / TILE_SIZE;

        int targetGridX = currentGridX;
        int targetGridY = currentGridY;

        // Look exactly one tile ahead on the grid
        switch (direction) {
            case "up"    -> targetGridY--;
            case "down"  -> targetGridY++;
            case "left"  -> targetGridX--;
            case "right" -> targetGridX++;
        }

        // Loop through all NPCs
        for (npc.NPC npc : gp.getWorldInteractiveLayer().getNPCs()) {
            if (npc != null) {
                // Find which grid tile the NPC is standing on
                int npcGridX = npc.worldX / TILE_SIZE;
                int npcGridY = npc.worldY / TILE_SIZE;

                // If they are on our target tile, talk to them!
                if (npcGridX == targetGridX && npcGridY == targetGridY) {
                    System.out.println("FOUND NPC: " + npc.name); // Debug print
                    npc.interact(gp); // Triggers the Dialogue!
                    if (npc.name.equalsIgnoreCase("HealingMachine")) {
                        DevConsole dc = new DevConsole(gp);
                        dc.cmdHealAll();
                        dc.close();
                    }
                    return;
                }
            }
        }

        TileLoot tl = TileChecker.getTileLootInFront(gp);
        if (tl != null) {
            tl.interact(gp);
            gp.player.getInventory().appendInventory(tl.getInventory());

            StringBuilder names = new StringBuilder();
            if (tl.getInventory() != null) {
                for (items.Item it : tl.getInventory().getRawItems()) {
                    if (it == null) continue;
                    if (names.length() > 0) names.append(", ");
                    names.append(it.getName());
                }
            }
            String body = names.length() == 0 ? "Picked up an item." : names.toString();
            gp.NOTIFICATION.push("Item Found!", body);
            utils.AudioManager.playSFX(utils.Constants.SFX_ITEM_FOUND);

            // Persist the pickup: mark the tile as gone in the matrix + remove from loots ArrayList.
            String key = gp.CURRENT_PATH + "@" + tl.getX() + "," + tl.getY();
            gp.pickedLoots.add(key);
            int[][] m = gp.getWorldInteractiveLayer().getMap();
            if (tl.getY() >= 0 && tl.getY() < m.length
                    && tl.getX() >= 0 && tl.getX() < m[0].length) {
                m[tl.getY()][tl.getX()] = 0;
            }
            gp.getWorldInteractiveLayer().getLoots().remove(tl);
        }
    }

    private void checkGrass() {
        gp.encounterSystem.checkWildEncounter(this, gp);
    }

    private void checkWarps() {
        int gridX = worldX / TILE_SIZE;
        int gridY = worldY / TILE_SIZE;

        // Assuming doors/warps are on the interactive layer
        if (gridY >= 0 && gridY < MAX_WORLD_ROW && gridX >= 0 && gridX < MAX_WORLD_COL) {
            int tileNum = gp.getWorldInteractiveLayer().getMap()[gridY][gridX];

            // Assuming '5' is a Door tile ID
            if (tileNum == 5) {
                System.out.println("Entering the Market Room!");
                // gp.world.loadRoom(1);
                // worldX = newStartX;
                // worldY = newStartY;
            }
        }
    }
    public PCSystem getPCSYSTEM() {
        return PCSYSTEM;
    }
    public void setRotCoins(int rotCoins) {
        this.rotCoins = rotCoins;
    }
    public void teleport(int[] coordinates) {
        worldX = coordinates[0] * TILE_SIZE;
        worldY = coordinates[1] * TILE_SIZE;
    }
    public void reset() {
        name = "";
        worldX = 0;
        worldY = 0;
        setRotCoins(0);
        setDirection("Down");
        inventory.reset();
        PCSYSTEM.reset();
    }
}