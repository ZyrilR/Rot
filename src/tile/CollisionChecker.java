package tile;

import engine.GamePanel;
import overworld.Player;

import static utils.Constants.*;
import java.awt.Rectangle;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Player player) {
        int playerLeftWorldX = player.worldX + player.solidArea.x;
        int playerRightWorldX = player.worldX + player.solidArea.x + player.solidArea.width;
        int playerTopWorldY = player.worldY + player.solidArea.y;
        int playerBottomWorldY = player.worldY + player.solidArea.y + player.solidArea.height;

        int playerLeftCol = playerLeftWorldX / TILE_SIZE;
        int playerRightCol = playerRightWorldX / TILE_SIZE;
        int playerTopRow = playerTopWorldY / TILE_SIZE;
        int playerBottomRow = playerBottomWorldY / TILE_SIZE;

        int tileNum1, tileNum2;

        switch (player.getDirection()) {
            case "up":
                playerTopRow = (playerTopWorldY - player.speed) / TILE_SIZE;
                // MAP BORDER CHECK (Invisible Wall at top)
                if (playerTopRow < 0) {
                    player.collisionOn = true;
                } else {
                    tileNum1 = gp.getWorldBackgroundLayer().getMap()[playerTopRow][playerLeftCol];
                    tileNum2 = gp.getWorldBackgroundLayer().getMap()[playerTopRow][playerRightCol];
                    if (gp.getWorldBackgroundLayer().getTiles().get(tileNum1).isCollision() ||
                            gp.getWorldBackgroundLayer().getTiles().get(tileNum2).isCollision()) {
                        player.collisionOn = true;
                    }
                }
                break;
            case "down":
                playerBottomRow = (playerBottomWorldY + player.speed) / TILE_SIZE;
                // MAP BORDER CHECK (Invisible Wall at bottom)
                if (playerBottomRow >= MAX_WORLD_ROW) {
                    player.collisionOn = true;
                } else {
                    tileNum1 = gp.getWorldBackgroundLayer().getMap()[playerBottomRow][playerLeftCol];
                    tileNum2 = gp.getWorldBackgroundLayer().getMap()[playerBottomRow][playerRightCol];
                    if (gp.getWorldBackgroundLayer().getTiles().get(tileNum1).isCollision() ||
                            gp.getWorldBackgroundLayer().getTiles().get(tileNum2).isCollision()) {
                        player.collisionOn = true;
                    }
                }
                break;
            case "left":
                playerLeftCol = (playerLeftWorldX - player.speed) / TILE_SIZE;
                // MAP BORDER CHECK (Invisible Wall on left)
                if (playerLeftCol < 0) {
                    player.collisionOn = true;
                } else {
                    tileNum1 = gp.getWorldBackgroundLayer().getMap()[playerTopRow][playerLeftCol];
                    tileNum2 = gp.getWorldBackgroundLayer().getMap()[playerBottomRow][playerLeftCol];
                    if (gp.getWorldBackgroundLayer().getTiles().get(tileNum1).isCollision() ||
                            gp.getWorldBackgroundLayer().getTiles().get(tileNum2).isCollision()) {
                        player.collisionOn = true;
                    }
                }
                break;
            case "right":
                playerRightCol = (playerRightWorldX + player.speed) / TILE_SIZE;
                // MAP BORDER CHECK (Invisible Wall on right)
                if (playerRightCol >= MAX_WORLD_COL) {
                    player.collisionOn = true;
                } else {
                    tileNum1 = gp.getWorldBackgroundLayer().getMap()[playerTopRow][playerRightCol];
                    tileNum2 = gp.getWorldBackgroundLayer().getMap()[playerBottomRow][playerRightCol];

                    if (gp.getWorldBackgroundLayer().getTiles().get(tileNum1).isCollision() ||
                            gp.getWorldBackgroundLayer().getTiles().get(tileNum2).isCollision()) {
//                        System.out.println("TILE NUM1 COLLISION: " + TileManager.tiles.get(tileNum1).isCollision());
//                        System.out.println("TILE NUM1 POSITION: " + playerTopRow + " | " + playerRightCol);
//                        System.out.println("TILE NUM2 COLLISION: " + TileManager.tiles.get(tileNum2).isCollision());
                        player.collisionOn = true;
                    }
                }
                break;
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