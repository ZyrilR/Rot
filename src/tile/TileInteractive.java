package tile;

import java.awt.image.BufferedImage;

public class TileInteractive extends Tile {

    //
    private String role;

    public TileInteractive (BufferedImage img) {
        super(img, false, "Interactive");
    }
    public TileInteractive(BufferedImage img, Boolean collision) {
        super(img, collision, "Interactive");
    }
    public TileInteractive(BufferedImage img, Boolean collision, String role) {
        super(img, collision, "Interactive");
        this.role = role;
    }

}
