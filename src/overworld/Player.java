package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
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