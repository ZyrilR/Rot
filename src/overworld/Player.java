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

    KeyboardHandler kh;

    public int x, y;
    public int speed;

    private String direction;
    private boolean isMoving;

    public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }

    //Handle Sprite Images
    ArrayList<BufferedImage> walk_up, walk_down, walk_right, walk_left;

    private int spriteCounter;

    public Player() {
        this.kh = new KeyboardHandler();
        x = 100;
        y = 100;
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
    public Player(KeyboardHandler kh) {
        this.kh = kh;
        x = 100;
        y = 100;
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
            g.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);

        movePlayer();
    }

    public void movePlayer() {
        boolean moving = false;

        if (kh.upPressed) {
            setDirection("up");
            y -= speed;
            moving = true;
        } else if (kh.downPressed) {
            setDirection("down");
            y += speed;
            moving = true;
        } else if (kh.leftPressed) {
            setDirection("left");
            x -= speed;
            moving = true;
        } else if (kh.rightPressed) {
            setDirection("right");
            x += speed;
            moving = true;
        }
        setIsMoving(moving);
    }
}