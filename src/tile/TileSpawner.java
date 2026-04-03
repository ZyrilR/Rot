package tile;

import java.awt.image.BufferedImage;

public class TileSpawner extends Tile {

    private double spawnLevel = 1;

    public TileSpawner(BufferedImage img) {
        super(img, false, "Spawner");
    }
    public TileSpawner(BufferedImage img, double spawnLevel) {
        super(img, false, "Spawner");
        this.spawnLevel = spawnLevel;
    }
}
