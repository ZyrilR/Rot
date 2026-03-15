package overworld;

import brainrots.BrainRot;
import engine.GamePanel;
import input.KeyboardHandler;
import items.Inventory;
import items.Item;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utils.AssetManager.*;
import static utils.Constants.*;

public class Player {
    GamePanel gp;
    KeyboardHandler kh;
    private final Inventory inventory;
    private final MovementSystem move;

    public int worldX, worldY;
    public final int screenX, screenY;
    public int speed;

    private String direction;
    private boolean isMoving;

    private int spriteCounter;

    public Rectangle solidArea;
    public boolean collisionOn = false;

    public Player(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;
        this.inventory = new Inventory(20);

        worldX = TILE_SIZE * 24;
        worldY = TILE_SIZE * 24;
        screenX = (SCREEN_WIDTH / 2) - (TILE_SIZE / 2);
        screenY = (SCREEN_HEIGHT / 2) - (TILE_SIZE / 2);
        speed = 8;

        direction = "down";
        spriteCounter = 0;
        isMoving = false;

        solidArea = new Rectangle(8, 16, 32, 32);

        // Initialize MovementSystem
        move = new MovementSystem(gp, kh, this);
    }

    public void setIsMoving(boolean isMoving) { this.isMoving = isMoving; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public void update() {
        move.update();  // Movement is now handled externally
        spriteCounter++;          // Keep animation updating
    }

    public void draw(Graphics2D g) {
        BufferedImage img = null;

        if (isMoving) {
            switch (direction) {
                case "up" -> img = playerWalkUp.get(spriteCounter % playerWalkUp.size());
                case "down" -> img = playerWalkDown.get(spriteCounter % playerWalkDown.size());
                case "right" -> img = playerWalkRight.get(spriteCounter % playerWalkRight.size());
                case "left" -> img = playerWalkLeft.get(spriteCounter % playerWalkLeft.size());
            }
        } else {
            switch (direction) {
                case "up" -> img = playerWalkUp.getFirst();
                case "down" -> img = playerWalkDown.getFirst();
                case "right" -> img = playerWalkRight.getFirst();
                case "left" -> img = playerWalkLeft.getFirst();
            }
            spriteCounter = 0;
        }

        if (img != null) g.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
    }

    // Only expose the inventory directly
    public Inventory getInventory() { return inventory; }

}
