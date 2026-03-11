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

        loadImage();
    }

    public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }
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
        boolean moving = false;

        if (kh.upPressed) {
            setDirection("up");
            worldY -= speed;
            moving = true;
        } else if (kh.downPressed) {
            setDirection("down");
            worldY += speed;
            moving = true;
        } else if (kh.leftPressed) {
            setDirection("left");
            worldX -= speed;
            moving = true;
        } else if (kh.rightPressed) {
            setDirection("right");
            worldX += speed;
            moving = true;
        }
        setIsMoving(moving);
    }
}