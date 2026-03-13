package map;

import java.awt.image.BufferedImage;
import tile.Tile;

public class TileTeleporter extends Tile {
    TileTeleporter link = null;

    public TileTeleporter(BufferedImage img) {
        super(img);
    }

    public void setLink(TileTeleporter link) {
        this.link = link;
    }
}