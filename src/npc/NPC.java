package npc;

import engine.GamePanel;
import items.Inventory;
import overworld.Player;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import static utils.Constants.*;

public class NPC {
    public String name;
    public int worldX, worldY;
    public int speed = 2;
    public String direction = "down";
    public Rectangle solidArea = new Rectangle(0, 0, TILE_SIZE, TILE_SIZE);
    public boolean collisionOn = false;

    public ArrayList<ArrayList<NPC>> npcs = new ArrayList<ArrayList<NPC>>();

    public int actionLockCounter = 0;
    public int spriteCounter = 0;
    public int spriteNum = 0;
    public ArrayList<BufferedImage> sprites = new ArrayList<>();
    public ArrayList<String> dialogues = new ArrayList<>();
    public Inventory inventory;

    public NPC(String name, int folderId, int x, int y) {
        this.name = name;
        worldX = x * TILE_SIZE;
        worldY = y * TILE_SIZE;
        loadSprites(folderId + 1);
    }
    public NPC(String name, int folderId, int x, int y, Inventory inventory) {
        this.name = name;
        worldX = x * TILE_SIZE;
        worldY = y * TILE_SIZE;
        loadSprites(folderId + 1);
        this.inventory = inventory;
    }

    public void loadSprites(int folderId) {
        for (int i = 1; i <= 5; i++) {
            sprites.add(AssetManager.loadImage("/res/InteractiveTiles/" + folderId + "/" + i + ".png"));
        }
    }

    public void update(GamePanel gp) {
        actionLockCounter++;
        if (actionLockCounter == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;

            if (i <= 25) direction = "up";
            else if (i <= 50) direction = "down";
            else if (i <= 75) direction = "left";
            else direction = "right";

            actionLockCounter = 0;
        }

        collisionOn = false;
        gp.COLLISIONCHECKER.checkTileForNPC(this);

        if (!collisionOn) {
            switch (direction) {
                case "up" -> worldY -= speed;
                case "down" -> worldY += speed;
                case "left" -> worldX -= speed;
                case "right" -> worldX += speed;
            }
        }

        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum++;
            if (spriteNum >= sprites.size()) {
                spriteNum = 0;
            }
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if (worldX + TILE_SIZE > gp.player.worldX - gp.player.screenX &&
                worldX - TILE_SIZE < gp.player.worldX + (SCREEN_WIDTH - gp.player.screenX) &&
                worldY + TILE_SIZE > gp.player.worldY - gp.player.screenY &&
                worldY - TILE_SIZE < gp.player.worldY + (SCREEN_HEIGHT - gp.player.screenY)) {

            BufferedImage img = null;
            if (!sprites.isEmpty()) {
                img = sprites.get(spriteNum);
            }

            if (img != null) {
                g2.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    // UNIVERSAL METHOD: Every NPC can now face the player!
    public void facePlayer(Player player) {
        switch(player.getDirection()) {
            case "up" -> direction = "down";
            case "down" -> direction = "up";
            case "left" -> direction = "right";
            case "right" -> direction = "left";
        }
        spriteNum = 0; // Reset animation frame
    }

    public void setDialogue(ArrayList<String> dialogues) { this.dialogues = dialogues; }
    public void setDialogue(String[] dialogues) {
        this.dialogues.addAll(Arrays.asList(dialogues));
    }

    // Abstract-like method for children to override
    public void interact(GamePanel gp) {
        gp.DIALOGUEBOX.startDialogue(name, dialogues);
    }
}