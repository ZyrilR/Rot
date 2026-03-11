package map;

import java.awt.image.BufferedImage;

public class Tile {
    private BufferedImage img;
    private boolean collision;
    private String role;

    //Role: Background, Teleporter, Spawner
    public Tile(BufferedImage img) {
        this.img = img;
        this.collision = false;
        this.role = "Background";
    }
    public Tile(BufferedImage img, String role) {
        this.img = img;
        this.collision = false;
        this.role = role;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }
    public BufferedImage getImg() {
        return img;
    }
}
