package map;

import engine.GamePanel;
import overworld.Player;

import static utils.Constants.*;
import java.awt.Rectangle;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    // Inside CollisionChecker.java
    public void checkTile(Player entity) {
        // 1. Get the world coordinates of the entity's hitbox (solidArea)
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        // 2. Convert those world coordinates into Tile column and row indices
        int entityLeftCol = entityLeftWorldX / TILE_SIZE;
        int entityRightCol = entityRightWorldX / TILE_SIZE;
        int entityTopRow = entityTopWorldY / TILE_SIZE;
        int entityBottomRow = entityBottomWorldY / TILE_SIZE;

        int tileNum1, tileNum2;

        // 3. Predict collision based on current direction
        switch (entity.getDirection()) {
            case "up" -> {
                // Predict the top row after moving 'speed' pixels
                entityTopRow = (entityTopWorldY - entity.speed) / TILE_SIZE;
                if (entityTopRow >= 0) {
                    tileNum1 = gp.getTileManager().getMap()[entityTopRow][entityLeftCol];
                    tileNum2 = gp.getTileManager().getMap()[entityTopRow][entityRightCol];
                    if (gp.getTileManager().tiles.get(tileNum1).isCollision() ||
                            gp.getTileManager().tiles.get(tileNum2).isCollision()) {
                        entity.collisionOn = true;
                    }
                } else { entity.collisionOn = true; }
            }
            case "down" -> {
                // Predict the bottom row after moving 'speed' pixels
                entityBottomRow = (entityBottomWorldY + entity.speed) / TILE_SIZE;
                if (entityBottomRow < gp.getTileManager().getMap().length) {
                    tileNum1 = gp.getTileManager().getMap()[entityBottomRow][entityLeftCol];
                    tileNum2 = gp.getTileManager().getMap()[entityBottomRow][entityRightCol];
                    if (gp.getTileManager().tiles.get(tileNum1).isCollision() ||
                            gp.getTileManager().tiles.get(tileNum2).isCollision()) {
                        entity.collisionOn = true;
                    }
                } else { entity.collisionOn = true; }
            }
            case "left" -> {
                // Predict the left column after moving 'speed' pixels
                entityLeftCol = (entityLeftWorldX - entity.speed) / TILE_SIZE;
                if (entityLeftCol >= 0) {
                    tileNum1 = gp.getTileManager().getMap()[entityTopRow][entityLeftCol];
                    tileNum2 = gp.getTileManager().getMap()[entityBottomRow][entityLeftCol];
                    if (gp.getTileManager().tiles.get(tileNum1).isCollision() ||
                            gp.getTileManager().tiles.get(tileNum2).isCollision()) {
                        entity.collisionOn = true;
                    }
                } else { entity.collisionOn = true; }
            }
            case "right" -> {
                // Predict the right column after moving 'speed' pixels
                entityRightCol = (entityRightWorldX + entity.speed) / TILE_SIZE;
                if (entityRightCol < gp.getTileManager().getMap()[0].length) {
                    tileNum1 = gp.getTileManager().getMap()[entityTopRow][entityRightCol];
                    tileNum2 = gp.getTileManager().getMap()[entityBottomRow][entityRightCol];
                    if (gp.getTileManager().tiles.get(tileNum1).isCollision() ||
                            gp.getTileManager().tiles.get(tileNum2).isCollision()) {
                        entity.collisionOn = true;
                    }
                } else { entity.collisionOn = true; }
            }
        }
    }

    public void checkEntity(Player entity, java.util.ArrayList<npc.NPC> target) {
        for (npc.NPC npc : target) {
            if (npc != null) {
                // Get current hitboxes in world coordinates
                Rectangle entityRect = new Rectangle(entity.worldX + entity.solidArea.x, entity.worldY + entity.solidArea.y, entity.solidArea.width, entity.solidArea.height);
                Rectangle npcRect = new Rectangle(npc.worldX + npc.solidArea.x, npc.worldY + npc.solidArea.y, npc.solidArea.width, npc.solidArea.height);

                // Predict the movement for the ENTIRE next tile
                switch (entity.getDirection()) {
                    case "up"    -> entityRect.y -= TILE_SIZE; // Check the whole tile ahead
                    case "down"  -> entityRect.y += TILE_SIZE;
                    case "left"  -> entityRect.x -= TILE_SIZE;
                    case "right" -> entityRect.x += TILE_SIZE;
                }

                if (entityRect.intersects(npcRect)) {
                    entity.collisionOn = true;
                }
            }
        }
    }
}