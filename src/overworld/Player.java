package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import items.Inventory;
import tile.TileManager;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utils.Constants.*;

public class Player {
    GamePanel gp;
    public KeyboardHandler kh;

    public int worldX, worldY; // map position
    public final int screenX, screenY; // where we draw player on screen
    public int speed;

    private String direction;
    private boolean isMoving;
    private int moveProgress = 0;
    private boolean isWalking = false;

    //Handle Sprite Images
    ArrayList<BufferedImage> walk_up, walk_down, walk_right, walk_left;
    private int spriteCounter;

    //Collision Handling
    public Rectangle solidArea;
    public boolean collisionOn = false;

    private final Inventory inventory;
    private int rotCoins; // in-game currency

    public Player(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;

        inventory = new Inventory(99);
        rotCoins = 500; //for testing

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
        walk_down.add(AssetManager.loadImage("/assets/Sprites/player/1.png"));
        walk_down.add(AssetManager.loadImage("/assets/Sprites/player/2.png"));
        walk_down.add(AssetManager.loadImage("/assets/Sprites/player/3.png"));
        walk_up.add(AssetManager.loadImage("/assets/Sprites/player/4.png"));
        walk_up.add(AssetManager.loadImage("/assets/Sprites/player/5.png"));
        walk_up.add(AssetManager.loadImage("/assets/Sprites/player/6.png"));
        walk_right.add(AssetManager.loadImage("/assets/Sprites/player/7.png"));
        walk_right.add(AssetManager.loadImage("/assets/Sprites/player/8.png"));
        walk_right.add(AssetManager.loadImage("/assets/Sprites/player/8.png"));
        walk_right.add(AssetManager.loadImage("/assets/Sprites/player/9.png"));
        walk_right.add(AssetManager.loadImage("/assets/Sprites/player/10.png"));
        walk_left.add(AssetManager.loadImage("/assets/Sprites/player/11.png"));
        walk_left.add(AssetManager.loadImage("/assets/Sprites/player/12.png"));
        walk_left.add(AssetManager.loadImage("/assets/Sprites/player/12.png"));
        walk_left.add(AssetManager.loadImage("/assets/Sprites/player/13.png"));
        walk_left.add(AssetManager.loadImage("/assets/Sprites/player/14.png"));
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

                // 2. CHECK FOR TALL GRASS ENCOUNTERS
                checkGrass();

                // 3. CHECK FOR DOORS/WARPS
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
            gp.COLLISIONCHECKER.checkNPC(this, gp.npcs);

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
        for (npc.NPC npc : gp.npcs) {
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
    }

    private void checkGrass() {
        int gridX = worldX / TILE_SIZE;
        int gridY = worldY / TILE_SIZE;

        // Loop through decoration layers to see if we are standing in tall grass
        for (TileManager decoLayer : gp.world.getDecorationLayer()) {
            if (gridY >= 0 && gridY < MAX_WORLD_ROW && gridX >= 0 && gridX < MAX_WORLD_COL) {
                int tileNum = decoLayer.getMap()[gridY][gridX];

                // Assuming '2' is your tall grass bush tile ID in the text file
                if (tileNum == 2) {
                    // Roll a 10% chance to encounter a wild RotMon!
                    int encounterChance = (int)(Math.random() * 100); // 0 to 99
                    if (encounterChance < 10) {
                        System.out.println("A wild BrainRot appeared!");
                        // gp.GAMESTATE = "battle";
                        // -> Trigger battle system here later!
                    }
                    break;
                }
            }
        }
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
}