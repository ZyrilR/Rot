package map;

import java.awt.image.BufferedImage;

public class Tile {
    private final BufferedImage image;
    private final boolean walkable;
    private final String type;

    public Tile(BufferedImage image, boolean walkable, String type) {
        this.image = image;
        this.walkable = walkable;
        this.type = type;
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public String getType() {
        return type;
    }
}