package npc;

import engine.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static utils.Constants.*;

public abstract class NPC {
    public GamePanel gp;
    public String name;
    public int worldX, worldY;
    public String direction = "down";
    public Rectangle solidArea = new Rectangle(8, 16, 32, 32);

    // Every NPC manages their own sprites and dialogue
    public ArrayList<BufferedImage> walk_up, walk_down, walk_left, walk_right;
    public String[] dialogues = new String[10];

    public NPC(GamePanel gp, String name, int x, int y) {
        this.gp = gp;
        this.name = name;
        this.worldX = x;
        this.worldY = y;

        this.solidArea = new Rectangle(8, 16, 32, 32);

        walk_up = new ArrayList<>();
        walk_down = new ArrayList<>();
        walk_left = new ArrayList<>();
        walk_right = new ArrayList<>();
    }

    // MANDATORY METHODS for every child NPC
    public abstract void setDialogue();
    public abstract void loadSprites();

    //Every NPC reacts differently
    public abstract void interact();

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        //  Only draw if visible
        if (worldX + TILE_SIZE > gp.player.worldX - gp.player.screenX &&
                worldX - TILE_SIZE < gp.player.worldX + (SCREEN_WIDTH - gp.player.screenX) &&
                worldY + TILE_SIZE > gp.player.worldY - gp.player.screenY &&
                worldY - TILE_SIZE < gp.player.worldY + (SCREEN_HEIGHT - gp.player.screenY)) {

            BufferedImage img = walk_down.isEmpty() ? null : walk_down.getFirst();
            // You can add animation logic here later using spriteCounter
            if (img != null) g2.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
        }
    }
}