package map;

import utils.AssetManager;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class TileManager {
    private Tile[][] tiles;
    private final int tileSize;

    public TileManager(int tileSize) {
        this.tileSize = tileSize;
    }

    // Initialize the 2D tile array from MapLoader
    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    // Draw all tiles
    public void render(Graphics g) {
        if (tiles == null) return;

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[y].length; x++) {
                Tile tile = tiles[y][x];
                if (tile != null) {
                    g.drawImage(tile.getImage(), x * tileSize, y * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }

    // Collision check
    public boolean isWalkable(int x, int y) {
        if (tiles == null || y < 0 || y >= tiles.length || x < 0 || x >= tiles[0].length) {
            return false;
        }
        return tiles[y][x].isWalkable();
    }
}