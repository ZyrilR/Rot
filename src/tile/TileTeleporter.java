package tile;

import java.awt.image.BufferedImage;

public class TileTeleporter extends Tile{

    //File name (World or Room)
    //Link to self
    //"/assets/Worlds/3"
    String self;
    //Link to other shit
    //"/assets/Rooms/1"
    String link;

    public TileTeleporter (BufferedImage img) {
        super(img, false, "Teleporter");
    }

}
