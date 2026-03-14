package tile;

import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage img;
    private boolean collision;
    private String role;

    //Role: Background, Teleporter, Spawner
    public Tile(BufferedImage img) {
        this.img = img;
        this.collision = false;
        this.role = "Background";
    }
    public Tile(BufferedImage img, boolean collision) {
        this.img = img;
        this.collision = collision;
        this.role = "Background";
    }

    public Tile(BufferedImage img, boolean collision, String role) {
        this.img = img;
        this.collision = collision;
        this.role = role;
    }
    // Inside tile.Tile
    public boolean isCollision() {return collision;}
    public void setCollision(boolean collision) {
        this.collision = collision;
    }

}
