package tile;

import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage image; // Renamed from img to image for consistency
    private boolean collision;
    private String role;

    // Default: Background, No Collision
    public Tile(BufferedImage image) {
        this.image = image;
        this.collision = false;
        this.role = "Background";
    }

    // Custom Collision
    public Tile(BufferedImage image, boolean collision) {
        this.image = image;
        this.collision = collision;
        this.role = "Background";
    }

    // Fully Custom (For Spawners, Teleporters, etc.)
    public Tile(BufferedImage image, boolean collision, String role) {
        this.image = image;
        this.collision = collision;
        this.role = role;
    }

    // Getters
    public boolean isCollision() { return collision; }
    public String getRole() { return role; }

    // Setters (If you want to change map properties at runtime)
    public void setCollision(boolean collision) { this.collision = collision; }
    public void setRole(String role) { this.role = role; }
}