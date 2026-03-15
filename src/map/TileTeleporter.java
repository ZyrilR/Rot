package map;

import java.awt.image.BufferedImage;

public class TileTeleporter extends Tile {
    TileTeleporter link = this;

    public TileTeleporter(BufferedImage img) {
        super(img, false, "Teleporter");
    }

    public void setLink(TileTeleporter link) {
        this.link = link;
    }
}