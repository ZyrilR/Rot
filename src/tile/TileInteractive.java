package tile;

import engine.GamePanel;
import overworld.Player;

import java.awt.image.BufferedImage;

public class TileInteractive extends Tile {
    //NPC, Chest, etc.
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

    public TileInteractive(Boolean collision, String role) {
        super(null, collision, role);
    }
}
