package entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static utils.Constants.TILE_SIZE;

public class Building {
    public int worldX, worldY;
    public int widthInTiles, heightInTiles;
    public Rectangle solidArea;
    private ArrayList<BufferedImage> tileSprites;

    public Building(int worldX, int worldY, int wTiles, int hTiles, ArrayList<BufferedImage> sprites) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.widthInTiles = wTiles;
        this.heightInTiles = hTiles;
        this.tileSprites = sprites;

        // Define the ACTUAL physical bounds (e.g., the base of the building)
        // We make it slightly smaller than the full image so the player can "walk behind" the roof
        this.solidArea = new Rectangle(0, TILE_SIZE, wTiles * TILE_SIZE, (hTiles - 1) * TILE_SIZE);
    }

    public void draw(Graphics2D g2, engine.GamePanel gp) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Logic to draw the 17 tiles in a grid pattern
        int i = 0;
        for (int row = 0; row < heightInTiles; row++) {
            for (int col = 0; col < widthInTiles; col++) {
                if (i < tileSprites.size()) {
                    g2.drawImage(tileSprites.get(i),
                            screenX + (col * TILE_SIZE),
                            screenY + (row * TILE_SIZE),
                            TILE_SIZE, TILE_SIZE, null);
                    i++;
                }
            }
        }
    }
}