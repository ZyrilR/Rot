package map;

import tile.Tile;

import java.awt.image.BufferedImage;

public class TileInteractive extends Tile {

    public TileInteractive (BufferedImage img) {
        super(img, false, "Interactive");
    }
    public TileInteractive(BufferedImage img, Boolean collision) {
        super(img, collision, "Interactive");
    }

}
