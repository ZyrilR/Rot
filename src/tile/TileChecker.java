package tile;

import engine.GamePanel;
import npc.NPC;
import overworld.Player;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

/**
 * Tile and entity collision queries for overworld movement.
 * All predictive checks use the entity's current direction + speed to test the tile
 * one step ahead; lookup helpers (getTileInFront / ...InCurrentPosition) walk the
 * decoration → building → background layer stack in priority order.
 */
public class TileChecker {

    private final GamePanel gp;

    public TileChecker(GamePanel gp) {
        this.gp = gp;
    }

    // ── Player movement ───────────────────────────────────────────────────────

    public void checkTile(Player player) {
        int[] box = solidBox(player.worldX, player.worldY, player.solidArea);
        int speed = player.getCurrentSpeed();
        int[] rc  = nextTiles(box, player.getDirection(), speed);
        if (rc == null) { player.collisionOn = true; return; }
        if (isBlockedAtAnyLayer(rc[0], rc[1], rc[2], rc[3])) player.collisionOn = true;
    }

    public void checkNPC(Player player, ArrayList<NPC> targetList) {
        int nextX = player.worldX + player.solidArea.x;
        int nextY = player.worldY + player.solidArea.y;
        int speed = player.getCurrentSpeed();

        switch (player.getDirection()) {
            case "up"    -> nextY -= speed;
            case "down"  -> nextY += speed;
            case "left"  -> nextX -= speed;
            case "right" -> nextX += speed;
        }

        Rectangle pRect = new Rectangle(nextX, nextY, player.solidArea.width, player.solidArea.height);

        for (NPC npc : targetList) {
            if (npc == null) continue;
            Rectangle npcRect = new Rectangle(
                    npc.worldX + npc.solidArea.x,
                    npc.worldY + npc.solidArea.y,
                    npc.solidArea.width,
                    npc.solidArea.height);
            if (pRect.intersects(npcRect)) {
                player.collisionOn = true;
                return;
            }
        }
    }

    // ── NPC movement ──────────────────────────────────────────────────────────

    public void checkTileForNPC(NPC npc) {
        int[] box = solidBox(npc.worldX, npc.worldY, npc.solidArea);
        int[] rc  = nextTiles(box, npc.direction, npc.speed);
        if (rc == null) { npc.collisionOn = true; return; }
        if (isBlockedAtBgOrBuilding(rc[0], rc[1], rc[2], rc[3])) npc.collisionOn = true;
    }

    // ── Tile lookups ──────────────────────────────────────────────────────────

    public static Tile getTileInFront(GamePanel gp, Player player) {
        int[] box = solidBox(player.worldX, player.worldY, player.solidArea);
        int centerX = (box[0] + box[2]) / 2;
        int centerY = (box[1] + box[3]) / 2;
        int speed = player.getCurrentSpeed();

        int row, col;
        switch (player.getDirection()) {
            case "up"    -> { row = (box[1] - speed) / TILE_SIZE; col = centerX / TILE_SIZE; }
            case "down"  -> { row = (box[3] + speed) / TILE_SIZE; col = centerX / TILE_SIZE; }
            case "left"  -> { row = centerY / TILE_SIZE; col = (box[0] - speed) / TILE_SIZE; }
            case "right" -> { row = centerY / TILE_SIZE; col = (box[2] + speed) / TILE_SIZE; }
            default      -> { return null; }
        }
        return firstTileAcrossLayers(gp, row, col);
    }

    public static Tile getTileInCurrentPosition(GamePanel gp, Player player) {
        int[] rc = centerTile(player);
        return firstTileAcrossLayers(gp, rc[0], rc[1]);
    }

    public static TileLoot getTileLootInFront(GamePanel gp) {
        int[] rc = centerTile(gp.player);
        int row = rc[0], col = rc[1];
        switch (gp.player.getDirection()) {
            case "up"    -> row -= 1;
            case "down"  -> row += 1;
            case "left"  -> col -= 1;
            case "right" -> col += 1;
        }
        for (TileLoot tl : gp.getWorldInteractiveLayer().getLoots()) {
            if (tl.getX() == col && tl.getY() == row) return tl;
        }
        return null;
    }

    public static TileTeleporter getTeleporterTileInCurrentPosition(GamePanel gp, Player player) {
        int[] rc = centerTile(player);
        for (TileTeleporter tt : gp.getWorldInteractiveLayer().getTeleporters()) {
            if (tt.getCoordinates()[1] == rc[0] && tt.getCoordinates()[0] == rc[1]) return tt;
        }
        return null;
    }

    public static int[] getPreviousTileCoordinates(Player player) {
        int[] rc = centerTile(player);
        int prevRow = rc[0], prevCol = rc[1];
        switch (player.getDirection()) {
            case "up"    -> prevRow = rc[0] + 1;
            case "down"  -> prevRow = rc[0] - 1;
            case "left"  -> prevCol = rc[1] + 1;
            case "right" -> prevCol = rc[1] - 1;
        }
        return new int[]{prevCol, prevRow};
    }

    // ── New helpers ───────────────────────────────────────────────────────────

    /** True if the tile the player is about to step into is blocked. Useful for one-shot checks. */
    public boolean isBlockedInFront(Player player) {
        int[] box = solidBox(player.worldX, player.worldY, player.solidArea);
        int[] rc  = nextTiles(box, player.getDirection(), player.getCurrentSpeed());
        return rc == null || isBlockedAtAnyLayer(rc[0], rc[1], rc[2], rc[3]);
    }

    /** Cross-layer solidity check for a single tile coordinate. */
    public boolean isTileSolidAt(int row, int col) {
        for (TileManager tm : gp.getWorldBackgroundLayer()) if (isTileSolid(tm, row, col)) return true;
        for (TileManager tm : gp.getWorldBuildingLayer())   if (isTileSolid(tm, row, col)) return true;
        for (TileManager tm : gp.world.getDecorationLayer()) if (isTileSolid(tm, row, col)) return true;
        return isTileSolid(gp.getWorldInteractiveLayer(), row, col);
    }

    /** Tile-grid coordinates of an entity's solid-box center. Returns [row, col]. */
    public static int[] centerTile(Player player) {
        int cx = player.worldX + player.solidArea.x + player.solidArea.width  / 2;
        int cy = player.worldY + player.solidArea.y + player.solidArea.height / 2;
        return new int[]{ cy / TILE_SIZE, cx / TILE_SIZE };
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /** Returns [left, top, right, bottom] in world pixels. */
    private static int[] solidBox(int worldX, int worldY, Rectangle solidArea) {
        return new int[] {
                worldX + solidArea.x,
                worldY + solidArea.y,
                worldX + solidArea.x + solidArea.width  - 1,
                worldY + solidArea.y + solidArea.height - 1
        };
    }

    /**
     * Given a solid box and a direction/speed, returns the two tiles that must be checked as
     * [row1, col1, row2, col2], or null if the predicted step falls outside the world bounds.
     */
    private static int[] nextTiles(int[] box, String direction, int speed) {
        int left = box[0], top = box[1], right = box[2], bottom = box[3];
        return switch (direction) {
            case "up" -> {
                if (top - speed < 0) yield null;
                int r = (top - speed) / TILE_SIZE;
                yield new int[]{ r, left / TILE_SIZE, r, right / TILE_SIZE };
            }
            case "down" -> {
                if (bottom + speed >= MAX_WORLD_ROW * TILE_SIZE) yield null;
                int r = (bottom + speed) / TILE_SIZE;
                yield new int[]{ r, left / TILE_SIZE, r, right / TILE_SIZE };
            }
            case "left" -> {
                if (left - speed < 0) yield null;
                int c = (left - speed) / TILE_SIZE;
                yield new int[]{ top / TILE_SIZE, c, bottom / TILE_SIZE, c };
            }
            case "right" -> {
                if (right + speed >= MAX_WORLD_COL * TILE_SIZE) yield null;
                int c = (right + speed) / TILE_SIZE;
                yield new int[]{ top / TILE_SIZE, c, bottom / TILE_SIZE, c };
            }
            default -> null;
        };
    }

    private boolean isBlockedAtAnyLayer(int r1, int c1, int r2, int c2) {
        if (anySolid(gp.getWorldBackgroundLayer(), r1, c1, r2, c2)) return true;
        if (anySolid(gp.getWorldBuildingLayer(),   r1, c1, r2, c2)) return true;
        if (anySolid(gp.world.getDecorationLayer(), r1, c1, r2, c2)) return true;
        return isTileSolid(gp.getWorldInteractiveLayer(), r1, c1);
    }

    private boolean isBlockedAtBgOrBuilding(int r1, int c1, int r2, int c2) {
        return anySolid(gp.getWorldBackgroundLayer(), r1, c1, r2, c2)
                || anySolid(gp.getWorldBuildingLayer(), r1, c1, r2, c2);
    }

    private boolean anySolid(List<TileManager> layers, int r1, int c1, int r2, int c2) {
        for (TileManager tm : layers) {
            if (isTileSolid(tm, r1, c1) || isTileSolid(tm, r2, c2)) return true;
        }
        return false;
    }

    private boolean isTileSolid(TileManager tm, int row, int col) {
        if (row < 0 || row >= tm.getMap().length || col < 0 || col >= tm.getMap()[0].length) return true;
        int tileNum = tm.getMap()[row][col];
        if (tileNum == 0) return false;

        boolean[][] collisionMap = tm.getCollisionMap();
        if (collisionMap != null && collisionMap[row][col]) return true;

        int idx = tileNum - 1;
        if (idx >= 0 && idx < tm.getTiles().size()) return tm.getTiles().get(idx).isCollision();
        return false;
    }

    private static Tile firstTileAcrossLayers(GamePanel gp, int row, int col) {
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

    private static Tile getTileAt(TileManager tm, int row, int col) {
        if (row < 0 || row >= MAX_WORLD_ROW || col < 0 || col >= MAX_WORLD_COL) return null;
        int tileNum = tm.getMap()[row][col];
        if (tileNum == 0) return null;
        int idx = tileNum - 1;
        if (idx >= 0 && idx < tm.getTiles().size()) return tm.getTiles().get(idx);
        return null;
    }
}
