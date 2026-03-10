package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.nio.Buffer;

public class Tile {
    public BufferedImage img;
    public boolean collision = false;

    public Tile(BufferedImage img) {
        this.img = img;
        this.collision = false;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }
}
