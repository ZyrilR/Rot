package utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AssetManager {
    private static final HashMap<String, BufferedImage> images = new HashMap<>(); // cache storing all images

    public static final int tiles = 8;
    public static final int sprites = 1;

    public static void loadImage(String key, String path) {
        try {
            BufferedImage img = ImageIO.read(
                    Objects.requireNonNull(AssetManager.class.getResourceAsStream(path)) // loads image from classpath
            );
            images.put(key, img); // store the image using the provided key
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load asset: " + path); // prints error if loading fails
            e.printStackTrace(); // useful for debugging
        }
    }

    public static void loadAll() {
        // ---------------- Sprites/player ----------------
        loadImage("player_down1", "/assets/Sprites/player/1.png");
        loadImage("player_down2", "/assets/Sprites/player/2.png");
        loadImage("player_down3", "/assets/Sprites/player/3.png");
        loadImage("player_up1", "/assets/Sprites/player/4.png");
        loadImage("player_up2", "/assets/Sprites/player/5.png");
        loadImage("player_up3", "/assets/Sprites/player/6.png");
        loadImage("player_right1", "/assets/Sprites/player/7.png");
        loadImage("player_right2", "/assets/Sprites/player/8.png");
        loadImage("player_right3", "/assets/Sprites/player/9.png");
        loadImage("player_right4", "/assets/Sprites/player/10.png");
        loadImage("player_left1", "/assets/Sprites/player/11.png");
        loadImage("player_left2", "/assets/Sprites/player/12.png");
        loadImage("player_left3", "/assets/Sprites/player/13.png");
        loadImage("player_left4", "/assets/Sprites/player/14.png");

        // ---------------- Tiles/grass ----------------
        for (int i = 1; i <= tiles; i++)
            AssetManager.loadImage("tiles_" + i, "/assets/Tiles/grass/" + i + ".png");
    }

    public static BufferedImage getImage(String key) {
        BufferedImage img = images.get(key); // retrieve image from cache

        if (img == null) {
            System.err.println("Asset not found: " + key); // warns if asset was never loaded
        }

        return img; // return the image
    }

    public static boolean hasImage(String key) {
        return images.containsKey(key); // checks if an asset exists in the cache
    }

    public static void unloadImage(String key) {
        images.remove(key); // removes a specific asset from memory
    }

    public static void clear() {
        images.clear(); // removes all assets from memory
    }

}