package tile;

import engine.GamePanel;
import input.KeyboardHandler;
import npc.NPC;
import overworld.Player;
import java.util.ArrayList;

import static utils.Constants.*;
import java.awt.Rectangle;
import entity.Building;

public class CollisionChecker {
    GamePanel gp;
    KeyboardHandler kh;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Player player) {
        // 1. Get player's solid area edges
        int playerLeftWorldX = player.worldX + player.solidArea.x;
        int playerRightWorldX = player.worldX + player.solidArea.x + player.solidArea.width - 1;
        int playerTopWorldY = player.worldY + player.solidArea.y;
        int playerBottomWorldY = player.worldY + player.solidArea.y + player.solidArea.height - 1;

        // 2. Predict the next columns/rows based on direction
        int col1, col2, row1, row2;
        int speed = player.getCurrentSpeed();

        switch (player.getDirection()) {
            case "up":
                row1 = (playerTopWorldY - speed) / TILE_SIZE;
                col1 = playerLeftWorldX / TILE_SIZE;
                col2 = playerRightWorldX / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row1, col2);
                break;
            case "down":
                row1 = (playerBottomWorldY + speed) / TILE_SIZE;
                col1 = playerLeftWorldX / TILE_SIZE;
                col2 = playerRightWorldX / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row1, col2);
                break;
            case "left":
                col1 = (playerLeftWorldX - speed) / TILE_SIZE;
                row1 = playerTopWorldY / TILE_SIZE;
                row2 = playerBottomWorldY / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row2, col1);
                break;
            case "right":
                col1 = (playerRightWorldX + speed) / TILE_SIZE;
                row1 = playerTopWorldY / TILE_SIZE;
                row2 = playerBottomWorldY / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row2, col1);
                break;
        }
    }

    private void checkCollisionAt(Player player, int r1, int c1, int r2, int c2) {
        // 1. Check Background Layer
        if (isTileSolid(gp.getWorldBackgroundLayer(), r1, c1) || isTileSolid(gp.getWorldBackgroundLayer(), r2, c2)) {
            player.collisionOn = true;
            return;
        }

        // 2. Check BUILDING Layers (Crucial for your screenshot!)
        // Assuming your world has an ArrayList of Building Layers
        for (TileManager buildingLayer : gp.getWorldBuildingLayer()) {
            if (isTileSolid(buildingLayer, r1, c1) || isTileSolid(buildingLayer, r2, c2)) {
                player.collisionOn = true;
                return;
            }
        }
    }

    private boolean isTileSolid(TileManager tm, int row, int col) {
        // Bounds check
        if (row < 0 || row >= MAX_WORLD_ROW || col < 0 || col >= MAX_WORLD_COL) return true;

        int tileNum = tm.getMap()[row][col];
        if (tileNum == 0) return false; // 0 is empty/transparent

        // Adjust tileNum (since your logic uses tileNum-- later)
        int actualIndex = tileNum - 1;
        if (actualIndex >= 0 && actualIndex < tm.getTiles().size()) {
            return tm.getTiles().get(actualIndex).isCollision();
        }
        return false;
    }

    //You only need to check the tile if its collidable or not! Every Interactive Tile should be collideable
//    public void checkEntity(Player entity, java.util.ArrayList<npc.NPC> target) {
//        for (NPC npc : target) {
//            if (npc != null) {
//                // Get current hitboxes in world coordinates
//                Rectangle entityRect = new Rectangle(entity.worldX + entity.solidArea.x, entity.worldY + entity.solidArea.y, entity.solidArea.width, entity.solidArea.height);
//                Rectangle npcRect = new Rectangle(npc.worldX + npc.solidArea.x, npc.worldY + npc.solidArea.y, npc.solidArea.width, npc.solidArea.height);
//
//                // Predict the movement for the ENTIRE next tile
//                switch (entity.getDirection()) {
//                    case "up"    -> entityRect.y -= TILE_SIZE; // Check the whole tile ahead
//                    case "down"  -> entityRect.y += TILE_SIZE;
//                    case "left"  -> entityRect.x -= TILE_SIZE;
//                    case "right" -> entityRect.x += TILE_SIZE;
//                }
//
//                if (entityRect.intersects(npcRect)) {
//                    entity.collisionOn = true;
//                }
//            }
//        }
//    }

    public void checkNPC(Player player, ArrayList<npc.NPC> targetList) {

        // Predict the player's NEXT position based on their speed and direction
        int nextX = player.worldX + player.solidArea.x;
        int nextY = player.worldY + player.solidArea.y;
        int speed = player.getCurrentSpeed();

        switch (player.getDirection()) {
            case "up"    -> nextY -= speed;
            case "down"  -> nextY += speed;
            case "left"  -> nextX -= speed;
            case "right" -> nextX += speed;
        }

        // Create the predicted hitbox
        Rectangle pRect = new Rectangle(nextX, nextY, player.solidArea.width, player.solidArea.height);

        // Check against every NPC
        for (npc.NPC npc : targetList) {
            if (npc != null) {
                // Get the NPC's current hitbox
                Rectangle npcRect = new Rectangle(
                        npc.worldX + npc.solidArea.x,
                        npc.worldY + npc.solidArea.y,
                        npc.solidArea.width,
                        npc.solidArea.height
                );

                // If the boxes overlap, STOP!
                if (pRect.intersects(npcRect)) {
                    player.collisionOn = true;
                    return; // Stop checking others once we hit one
                }
            }
        }
    }

//    public void checkObject(Player player, Building building) {
//        // Get player's predicted position
//        Rectangle pRect = new Rectangle(
//                player.worldX + player.solidArea.x,
//                player.worldY + player.solidArea.y,
//                player.solidArea.width,
//                player.solidArea.height
//        );
//
//        // Predict next step
//        int speed = player.getCurrentSpeed();
//        switch(player.getDirection()) {
//            case "up" -> pRect.y -= speed;
//            case "down" -> pRect.y += speed;
//            case "left" -> pRect.x -= speed;
//            case "right" -> pRect.x += speed;
//        }
//
//        // Building's world hitbox
//        Rectangle bRect = new Rectangle(
//                building.worldX + building.solidArea.x,
//                building.worldY + building.solidArea.y,
//                building.solidArea.width,
//                building.solidArea.height
//        );
//
//        if (pRect.intersects(bRect)) {
//            player.collisionOn = true;
//        }
//    }

    // ADD THIS TO CollisionChecker.java
    public void checkTileForNPC(NPC npc) {
        int npcLeftWorldX = npc.worldX + npc.solidArea.x;
        int npcRightWorldX = npc.worldX + npc.solidArea.x + npc.solidArea.width - 1;
        int npcTopWorldY = npc.worldY + npc.solidArea.y;
        int npcBottomWorldY = npc.worldY + npc.solidArea.y + npc.solidArea.height - 1;

        int col1, col2, row1, row2;

        switch (npc.direction) {
            case "up":
                row1 = (npcTopWorldY - npc.speed) / TILE_SIZE;
                col1 = npcLeftWorldX / TILE_SIZE;
                col2 = npcRightWorldX / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row1, col2);
                break;
            case "down":
                row1 = (npcBottomWorldY + npc.speed) / TILE_SIZE;
                col1 = npcLeftWorldX / TILE_SIZE;
                col2 = npcRightWorldX / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row1, col2);
                break;
            case "left":
                col1 = (npcLeftWorldX - npc.speed) / TILE_SIZE;
                row1 = npcTopWorldY / TILE_SIZE;
                row2 = npcBottomWorldY / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row2, col1);
                break;
            case "right":
                col1 = (npcRightWorldX + npc.speed) / TILE_SIZE;
                row1 = npcTopWorldY / TILE_SIZE;
                row2 = npcBottomWorldY / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row2, col1);
                break;
        }
    }

    private void checkNPCCollisionAt(NPC npc, int r1, int c1, int r2, int c2) {
        // Check Background
        if (isTileSolid(gp.getWorldBackgroundLayer(), r1, c1) || isTileSolid(gp.getWorldBackgroundLayer(), r2, c2)) {
            npc.collisionOn = true;
            return;
        }
        // Check Buildings
        for (TileManager buildingLayer : gp.getWorldBuildingLayer()) {
            if (isTileSolid(buildingLayer, r1, c1) || isTileSolid(buildingLayer, r2, c2)) {
                npc.collisionOn = true;
                return;
            }
        }
    }
}