package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class AssetManager {
    private static final HashMap<String, BufferedImage> images = new HashMap<>(); // cache storing all images

    public static int tiles;
    public static int sprites = 1;

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

        // 1. Correct the base path (Relative to your project root)
        Path tilesDir = Paths.get("src/assets/Tiles");

        try (Stream<Path> subFolders = Files.list(tilesDir)) {
            // We get all folders inside "Tiles" (e.g., "grass")
            subFolders.forEach(subFolder -> {
                String folderName = subFolder.getFileName().toString();

                try (Stream<Path> fileStream = Files.list(subFolder)) {
                    // Now we iterate through every image in that folder
                    fileStream.forEach(filePath -> {
                        String fileName = filePath.getFileName().toString();

                        // We need to format the path for getResourceAsStream
                        // It should look like: "/assets/Tiles/grass/1.png"
                        String resourcePath = "/assets/Tiles/" + folderName + "/" + fileName;

                        // Generate a unique key, e.g., "tiles_1"
                        String key = "tiles_" + (images.size() + 1);

                        loadImage(key, resourcePath);
                        System.out.println("Loaded: " + key + " from " + resourcePath);
                    });
                } catch (IOException e) {
                    System.err.println("Error reading subfolder: " + folderName);
                }
            });
        } catch (IOException e) {
            System.err.println("Could not find Tiles directory at: " + tilesDir.toAbsolutePath());
        }

        tiles = images.size();

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