package tile;

import java.awt.image.BufferedImage;

public class TileSpawner extends Tile {

    private double spawnRate = 1;

    public TileSpawner(BufferedImage img) {
        super(img, false, "Spawner");
    }
    public TileSpawner(BufferedImage img, double spawnRate) {
        super(img, false, "Spawner");
        this.spawnRate = spawnRate;
    }
}
