package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utils.Constants.*;

public class Player {
    GamePanel gp;
    KeyboardHandler kh;

    public int worldX, worldY; // map position
    public final int screenX, screenY; // where we draw player on screen (always on center)
    public int speed;

    private String direction;
    private boolean isMoving;
    private int moveProgress = 0;   // how many pixels we've moved in current tile
    private boolean isWalking = false; // true while moving to next tile

    //Handle Sprite Images
    ArrayList<BufferedImage> walk_up, walk_down, walk_right, walk_left;
    private int spriteCounter;

    public Rectangle solidArea;
    public boolean collisionOn = false;

    private int moveDelay = 0;
    private final int MOVE_DELAY_THRESHOLD = 5; // How many frames to hold before walking

    public Player(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;

        worldX = TILE_SIZE * 24; // starting position
        worldY = TILE_SIZE * 24; // starting position
        screenX = (SCREEN_WIDTH / 2) - (TILE_SIZE / 2); // center of the screen
        screenY = (SCREEN_HEIGHT / 2) - (TILE_SIZE / 2);
        speed = 8;

        direction = "down";
        walk_up = new ArrayList<>();
        walk_down = new ArrayList<>();
        walk_right = new ArrayList<>();
        walk_left = new ArrayList<>();
        resetSpriteCounter();
        isMoving = false;

        solidArea = new Rectangle(8, 16, 32, 32);

        loadImage();
    }

    public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }
    public String getDirection() { return direction; }
    public void setDirection(String direction) {
        this.direction = direction;
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

    public void movePlayer() {
        if (kh.isMoving()) {

            //Figures out which way the player WANTS to go
            String intendedDirection = "";
            if (kh.upPressed) intendedDirection = "up";
            else if (kh.downPressed) intendedDirection = "down";
            else if (kh.leftPressed) intendedDirection = "left";
            else if (kh.rightPressed) intendedDirection = "right";

            // when turned
            if (!direction.equals(intendedDirection)) {
                // Just change direction, don't walk yet!
                direction = intendedDirection;
                moveDelay = 0; // Reset the hold timer
                setIsMoving(false); // Keep the idle sprite
            } else {
                //counting the hold time.
                moveDelay++;

                // 4. Have they held the button long enough to actually walk?
                if (moveDelay > MOVE_DELAY_THRESHOLD) {

                    // Check for walls and camera bounds
                    collisionOn = false;
                    gp.collisionChecker.checkTile(this);

                    // If no wall, start walking
                    if (!collisionOn) {
                        switch (direction) {
                            case "up" -> worldY -= speed;
                            case "down" -> worldY += speed;
                            case "left" -> worldX -= speed;
                            case "right" -> worldX += speed;
                        }
                        setIsMoving(true); // Trigger walking animation
                    } else {
                        setIsMoving(false); // Stop animation if hitting a wall
                    }
                } else {
                    // Still waiting for the hold threshold
                    setIsMoving(false);
                }
            }
        } else {
            // Player let go of the keys. Reset everything.
            moveDelay = 0;
            setIsMoving(false);
        }
    }

    // Inside overworld.Player.update()
    public void update() {
        int currentSpeed = speed;

        // Only determine speed at the START of a step to prevent mid-tile jitters
        if (!isWalking) {
            currentSpeed = kh.running ? speed + 8 : speed;
        }

        // STEP 1: Input handling
        if (!isWalking && kh.isMoving()) {
            if (kh.upPressed) direction = "up";
            else if (kh.downPressed) direction = "down";
            else if (kh.leftPressed) direction = "left";
            else if (kh.rightPressed) direction = "right";

            collisionOn = false;
            gp.collisionChecker.checkTile(this);

            if (!collisionOn) {
                isWalking = true;
                moveProgress = 0;
                setIsMoving(true);
            }
        }

        // STEP 2: Movement
        if (isWalking) {
            switch (direction) {
                case "up"    -> worldY -= currentSpeed;
                case "down"  -> worldY += currentSpeed;
                case "left"  -> worldX -= currentSpeed;
                case "right" -> worldX += currentSpeed;
            }
            moveProgress += currentSpeed;

            // STEP 3: Grid Snapping
            if (moveProgress >= TILE_SIZE) {
                int overshoot = moveProgress - TILE_SIZE;

                // Correct the position
                switch (direction) {
                    case "up"    -> worldY += overshoot;
                    case "down"  -> worldY -= overshoot;
                    case "left"  -> worldX += overshoot;
                    case "right" -> worldX -= overshoot;
                }

                isWalking = false;
                moveProgress = 0;
                setIsMoving(false);
            }
        }
    }
    public void checkInteraction() {
        int targetX = worldX;
        int targetY = worldY;

        // Look one tile ahead based on current direction
        switch (direction) {
            case "up" -> targetY -= TILE_SIZE;
            case "down" -> targetY += TILE_SIZE;
            case "left" -> targetX -= TILE_SIZE;
            case "right" -> targetX += TILE_SIZE;
        }

        for (npc.NPC npc : gp.npcs) {
            if (npc.worldX == targetX && npc.worldY == targetY) {
                npc.interact(); // NPC decides what happens!
                return;
            }
        }
    }
}
