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

        // Block movement at map borders
        switch (player.getDirection()) {
            case "up":
                if (playerTopWorldY - speed < 0) { player.collisionOn = true; return; }
                row1 = (playerTopWorldY - speed) / TILE_SIZE;
                col1 = playerLeftWorldX / TILE_SIZE;
                col2 = playerRightWorldX / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row1, col2);
                break;
            case "down":
                if (playerBottomWorldY + speed >= MAX_WORLD_ROW * TILE_SIZE) { player.collisionOn = true; return; }
                row1 = (playerBottomWorldY + speed) / TILE_SIZE;
                col1 = playerLeftWorldX / TILE_SIZE;
                col2 = playerRightWorldX / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row1, col2);
                break;
            case "left":
                if (playerLeftWorldX - speed < 0) { player.collisionOn = true; return; }
                col1 = (playerLeftWorldX - speed) / TILE_SIZE;
                row1 = playerTopWorldY / TILE_SIZE;
                row2 = playerBottomWorldY / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row2, col1);
                break;
            case "right":
                if (playerRightWorldX + speed >= MAX_WORLD_COL * TILE_SIZE) { player.collisionOn = true; return; }
                col1 = (playerRightWorldX + speed) / TILE_SIZE;
                row1 = playerTopWorldY / TILE_SIZE;
                row2 = playerBottomWorldY / TILE_SIZE;
                checkCollisionAt(player, row1, col1, row2, col1);
                break;
        }
    }

    private void checkCollisionAt(Player player, int r1, int c1, int r2, int c2) {
        // 1. Check Background Layer
        for (TileManager tm : gp.getWorldBackgroundLayer()) {
            if (isTileSolid(tm, r1, c1) || isTileSolid(tm, r2, c2)) {
                player.collisionOn = true;
                return;
            }
        }

        // 2. Check BUILDING Layers (Crucial for your screenshot!)
        // Assuming your world has an ArrayList of Building Layers
        for (TileManager buildingLayer : gp.getWorldBuildingLayer()) {
            if (isTileSolid(buildingLayer, r1, c1) || isTileSolid(buildingLayer, r2, c2)) {
                player.collisionOn = true;
                return;
            }
        }

        //3. Check DECORATION Layers
        for (TileManager tm : gp.world.getDecorationLayer()) {
            if (isTileSolid(tm, r1, c1) || isTileSolid(tm, r2, c2)) {
                player.collisionOn = true;
                return;
            }
        }

        if (isTileSolid(gp.getWorldInteractiveLayer(), r1, c1))
            player.collisionOn = true;
    }

    private boolean isTileSolid(TileManager tm, int row, int col) {
        // Bounds check
        if (row < 0 || row >= tm.getMap().length || col < 0 || col >= tm.getMap()[0].length) {
            return true; // Treat "Out of Bounds" as solid
        }
        int tileNum = tm.getMap()[row][col];
        if (tileNum == 0) return false; // 0 is empty/transparent

        // Check per-position collision map
        boolean[][] collisionMap = tm.getCollisionMap();
        if (collisionMap != null && collisionMap[row][col]) {
            return true;
        }

        // Fall back to tile-level collision (set during tile asset loading)
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

    // ADD THIS TO CollisionChecker.java
    public void checkTileForNPC(NPC npc) {
        int npcLeftWorldX = npc.worldX + npc.solidArea.x;
        int npcRightWorldX = npc.worldX + npc.solidArea.x + npc.solidArea.width - 1;
        int npcTopWorldY = npc.worldY + npc.solidArea.y;
        int npcBottomWorldY = npc.worldY + npc.solidArea.y + npc.solidArea.height - 1;

        int col1, col2, row1, row2;

        switch (npc.direction) {
            case "up":
                if (npcTopWorldY - npc.speed < 0) { npc.collisionOn = true; return; }
                row1 = (npcTopWorldY - npc.speed) / TILE_SIZE;
                col1 = npcLeftWorldX / TILE_SIZE;
                col2 = npcRightWorldX / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row1, col2);
                break;
            case "down":
                if (npcBottomWorldY + npc.speed >= MAX_WORLD_ROW * TILE_SIZE) { npc.collisionOn = true; return; }
                row1 = (npcBottomWorldY + npc.speed) / TILE_SIZE;
                col1 = npcLeftWorldX / TILE_SIZE;
                col2 = npcRightWorldX / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row1, col2);
                break;
            case "left":
                if (npcLeftWorldX - npc.speed < 0) { npc.collisionOn = true; return; }
                col1 = (npcLeftWorldX - npc.speed) / TILE_SIZE;
                row1 = npcTopWorldY / TILE_SIZE;
                row2 = npcBottomWorldY / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row2, col1);
                break;
            case "right":
                if (npcRightWorldX + npc.speed >= MAX_WORLD_COL * TILE_SIZE) { npc.collisionOn = true; return; }
                col1 = (npcRightWorldX + npc.speed) / TILE_SIZE;
                row1 = npcTopWorldY / TILE_SIZE;
                row2 = npcBottomWorldY / TILE_SIZE;
                checkNPCCollisionAt(npc, row1, col1, row2, col1);
                break;
        }
    }

    private void checkNPCCollisionAt(NPC npc, int r1, int c1, int r2, int c2) {
        // Check Background
        for (TileManager tm : gp.getWorldBackgroundLayer()) {
            if (isTileSolid(tm, r1, c1) || isTileSolid(tm, r2, c2)) {
                npc.collisionOn = true;
                return;
            }
        }
        // Check Buildings
        for (TileManager buildingLayer : gp.getWorldBuildingLayer()) {
            if (isTileSolid(buildingLayer, r1, c1) || isTileSolid(buildingLayer, r2, c2)) {
                npc.collisionOn = true;
                return;
            }
        }
    }
    public static Tile getTileInFront(GamePanel gp, Player player) {
        int playerLeftWorldX = player.worldX + player.solidArea.x;
        int playerRightWorldX = player.worldX + player.solidArea.x + player.solidArea.width - 1;
        int playerTopWorldY = player.worldY + player.solidArea.y;
        int playerBottomWorldY = player.worldY + player.solidArea.y + player.solidArea.height - 1;

        int centerX = (playerLeftWorldX + playerRightWorldX) / 2;
        int centerY = (playerTopWorldY + playerBottomWorldY) / 2;

        int speed = player.getCurrentSpeed();
        int row, col;

        switch (player.getDirection()) {
            case "up"    -> { row = (playerTopWorldY - speed) / TILE_SIZE; col = centerX / TILE_SIZE; }
            case "down"  -> { row = (playerBottomWorldY + speed) / TILE_SIZE; col = centerX / TILE_SIZE; }
            case "left"  -> { row = centerY / TILE_SIZE; col = (playerLeftWorldX - speed) / TILE_SIZE; }
            case "right" -> { row = centerY / TILE_SIZE; col = (playerRightWorldX + speed) / TILE_SIZE; }
            default      -> { return null; }
        }

        // Search all layers, top priority first
        for (TileManager tm : gp.world.getDecorationLayer()) {
            Tile t = getTileAt(tm, row, col);
            if (t != null) return t;
        }
        for (TileManager tm : gp.getWorldBuildingLayer()) {
            Tile t = getTileAt(tm, row, col);
            if (t != null) return t;
        }
        for (TileManager tm : gp.getWorldBackgroundLayer()) {
            Tile t = getTileAt(tm, row, col);
            if (t != null) return t;
        }

        return null;
    }

    public static TileLoot getTileLootInFront(GamePanel gp) {
        Player player = gp.player;
        int centerX = player.worldX + player.solidArea.x + player.solidArea.width / 2;
        int centerY = player.worldY + player.solidArea.y + player.solidArea.height / 2;

        int row = centerY / TILE_SIZE;
        int col = centerX / TILE_SIZE;

        switch (player.getDirection()) {
            case "up"    -> row -= 1;
            case "down"  -> row += 1;
            case "left"  -> col -= 1;
            case "right" -> col += 1;
        }

        for (TileLoot tl : gp.getWorldInteractiveLayer().getLoots()) {
            if (tl.getX() == col && tl.getY() == row)
                return tl;
        }
        return null;
    }

    public static TileTeleporter getTeleporterTileInCurrentPosition(GamePanel gp, Player player) {
        int centerX = player.worldX + player.solidArea.x + player.solidArea.width / 2;
        int centerY = player.worldY + player.solidArea.y + player.solidArea.height / 2;

        int row = centerY / TILE_SIZE;
        int col = centerX / TILE_SIZE;

        for (TileTeleporter tt : gp.getWorldInteractiveLayer().getTeleporters()) {
            if (tt.getCoordinates()[1] == row && tt.getCoordinates()[0] == col)
                return tt;
        }
        return null;
    }

    public static Tile getTileInCurrentPosition(GamePanel gp, Player player) {
        int centerX = player.worldX + player.solidArea.x + player.solidArea.width / 2;
        int centerY = player.worldY + player.solidArea.y + player.solidArea.height / 2;

        int row = centerY / TILE_SIZE;
        int col = centerX / TILE_SIZE;

        // Search all layers, top priority first
        for (TileManager tm : gp.world.getDecorationLayer()) {
            Tile t = getTileAt(tm, row, col);
            if (t != null) return t;
        }
        for (TileManager tm : gp.getWorldBuildingLayer()) {
            Tile t = getTileAt(tm, row, col);
            if (t != null) return t;
        }
        for (TileManager tm : gp.getWorldBackgroundLayer()) {
            Tile t = getTileAt(tm, row, col);
            if (t != null) return t;
        }

        return null;
    }

    // Private helper — reuse across both methods
    private static Tile getTileAt(TileManager tm, int row, int col) {
        if (row < 0 || row >= MAX_WORLD_ROW || col < 0 || col >= MAX_WORLD_COL) return null;

        int tileNum = tm.getMap()[row][col];
        if (tileNum == 0) return null;

        int actualIndex = tileNum - 1;
        if (actualIndex >= 0 && actualIndex < tm.getTiles().size()) {
            return tm.getTiles().get(actualIndex);
        }
        return null;
    }
    public static int[] getPreviousTileCoordinates(Player player) {
        // 1. Get the player's current center position in grid coordinates
        int centerX = player.worldX + player.solidArea.x + player.solidArea.width / 2;
        int centerY = player.worldY + player.solidArea.y + player.solidArea.height / 2;

        int currentRow = centerY / TILE_SIZE;
        int currentCol = centerX / TILE_SIZE;

        int prevRow = currentRow;
        int prevCol = currentCol;

        // 2. Determine the tile "behind" the player based on direction
        // If facing DOWN, the previous tile is UP (-1 row)
        // If facing UP, the previous tile is DOWN (+1 row)
        switch (player.getDirection()) {
            case "up"    -> prevRow = currentRow + 1;
            case "down"  -> prevRow = currentRow - 1;
            case "left"  -> prevCol = currentCol + 1;
            case "right" -> prevCol = currentCol - 1;
        }

        // Return as [col, row] to match your teleport coordinate system
        return new int[]{prevCol, prevRow};
    }
}