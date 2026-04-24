package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import items.Inventory;
import storage.PCSystem;
import tile.CollisionChecker;
import tile.TileLoot;
import tile.TileManager;
import tile.TileTeleporter;
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
        speed = 32;

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
    public int getCurrentSpeed() {return kh.running ? speed + 8 : speed;}

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
    }

    public void draw(Graphics2D g) {
        BufferedImage img = null;

        if (isMoving) {
            switch (direction) {
                case "up" -> img = walk_up.get(spriteCounter % walk_up.size());
                case "down" -> img = walk_down.get(spriteCounter % walk_down.size());
                case "right" -> img = walk_right.get(spriteCounter % walk_right.size());
                case "left" -> img = walk_left.get(spriteCounter % walk_left.size());
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

        spriteCounter++;

        if (img != null)
            g.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
    }

    public void update() {
        if (isWalking) {
            // Continue moving if already in a tile transition
            int currentSpeed = kh.running ? speed + SPRINT_SPEED : speed;

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

                // 2. CHECK FOR RAMP (ground level change)
                checkRamp();

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
            gp.COLLISIONCHECKER.checkTile(this);

            // 2. CHECK NPCS (This is what stops you from ghosting!)
            gp.COLLISIONCHECKER.checkNPC(this, gp.getWorldInteractiveLayer().getNPCs());

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
                    return;
                }
            }
        }

        TileLoot tl = CollisionChecker.getTileLootInFront(gp);
        if (tl != null) {
            tl.interact(gp);
            gp.player.getInventory().appendInventory(tl.getInventory());
        }
    }

    private void checkRamp() {
        int gridX = worldX / TILE_SIZE;
        int gridY = worldY / TILE_SIZE;

        for (TileManager ramp : gp.world.getRampLayers()) {
            if (gridY >= 0 && gridY < MAX_WORLD_ROW && gridX >= 0 && gridX < MAX_WORLD_COL) {
                int tileNum = ramp.getMap()[gridY][gridX];
                if (tileNum != 0) {
                    // Player stepped on a ramp tile — determine direction
                    // Moving down (higher Y) = going downhill = -1
                    // Moving up (lower Y) = going uphill = +1
                    switch (direction) {
                        case "up"   -> groundLevel += 1;
                        case "down" -> groundLevel -= 1;
                    }
                    System.out.println("[Ramp] Ground level changed to: " + groundLevel);
                    return;
                }
            }
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
        System.out.println("TELEPORTED TO: " + coordinates[0] + "," + coordinates[1]);
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