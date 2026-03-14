package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.util.ArrayList;

import static utils.AssetManager.getImage;
import static utils.Constants.*;

public class Player {
    GamePanel gp;
    KeyboardHandler kh;

    public int worldX, worldY; // map position
    public final int screenX, screenY; // where we draw player on screen (always on center)
    public int speed;

    private String direction;
    private boolean isMoving;

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
        speed = 6;

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
        walk_down.add(getImage("player_down1"));
        walk_down.add(getImage("player_down2"));
        walk_down.add(getImage("player_down3"));
        walk_up.add(getImage("player_up1"));
        walk_up.add(getImage("player_up2"));
        walk_up.add(getImage("player_up3"));
        walk_right.add(getImage("player_right1"));
        walk_right.add(getImage("player_right2"));
        walk_right.add(getImage("player_right2"));
        walk_right.add(getImage("player_right3"));
        walk_right.add(getImage("player_right4"));
        walk_left.add(getImage("player_left1"));
        walk_left.add(getImage("player_left2"));
        walk_left.add(getImage("player_left2"));
        walk_left.add(getImage("player_left3"));
        walk_left.add(getImage("player_left4"));
    }
    public void resetSpriteCounter() {
        spriteCounter = 0;
    }

    public void draw(Graphics2D g) {
        BufferedImage img = null;

        //if player is moving make sure to load sprites
        if (isMoving) {
            //load all sprites depending on the images
            switch (direction) {
                case "up" ->
                    img = walk_up.get(spriteCounter % walk_up.size());
                case "down" ->
                    img = walk_down.get(spriteCounter % walk_down.size());
                case "right" ->
                    img = walk_right.get(spriteCounter % walk_right.size());
                case "left" ->
                    img = walk_left.get(spriteCounter % walk_left.size());
            }
        //else if player is not moving just set it to be the idle state
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

        movePlayer();
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
        if (kh.isMoving()) {
            // Change direction immediately on click
            if (kh.upPressed) direction = "up";
            else if (kh.downPressed) direction = "down";
            else if (kh.leftPressed) direction = "left";
            else if (kh.rightPressed) direction = "right";

            // Reset collision flag and check
            collisionOn = false;
            gp.collisionChecker.checkTile(this);

            // Only update position if collision is NOT detected
            if (!collisionOn) {
                switch (direction) {
                    case "up" -> worldY -= speed;
                    case "down" -> worldY += speed;
                    case "left" -> worldX -= speed;
                    case "right" -> worldX += speed;
                }
            }
            setIsMoving(true);
        } else {
            setIsMoving(false);
        }
    }
}