package tile;

import java.awt.image.BufferedImage;

public class TileTeleporter extends Tile{

    //File name (World or Room)
    //Link to self
    String self;
    //Link to other shit
    String link;

    public TileTeleporter (BufferedImage img) {
        super(img, false, "Teleporter");
    }

}
