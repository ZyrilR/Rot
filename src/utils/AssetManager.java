package utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AssetManager {

    // --- Player sprites ---
    public static final ArrayList<BufferedImage> playerWalkUp = new ArrayList<>();
    public static final ArrayList<BufferedImage> playerWalkDown = new ArrayList<>();
    public static final ArrayList<BufferedImage> playerWalkLeft = new ArrayList<>();
    public static final ArrayList<BufferedImage> playerWalkRight = new ArrayList<>();

    // --- Tiles (single list for all tiles, preserves indexing) ---
    public static final ArrayList<BufferedImage> tiles = new ArrayList<>();

    // Generic image loader
    public static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(
                    Objects.requireNonNull(AssetManager.class.getResourceAsStream(path))
            );
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load asset: " + path);
            e.printStackTrace();
        }
        return null;
    }

    // Load all player sprites and tiles
    public static void loadAll() {
        // --- Player sprites ---
        playerWalkDown.add(loadImage("/assets/Sprites/player/1.png"));
        playerWalkDown.add(loadImage("/assets/Sprites/player/2.png"));
        playerWalkDown.add(loadImage("/assets/Sprites/player/3.png"));

        playerWalkUp.add(loadImage("/assets/Sprites/player/4.png"));
        playerWalkUp.add(loadImage("/assets/Sprites/player/5.png"));
        playerWalkUp.add(loadImage("/assets/Sprites/player/6.png"));

        playerWalkRight.add(loadImage("/assets/Sprites/player/7.png"));
        playerWalkRight.add(loadImage("/assets/Sprites/player/8.png"));
        playerWalkRight.add(loadImage("/assets/Sprites/player/9.png"));
        playerWalkRight.add(loadImage("/assets/Sprites/player/10.png"));

        playerWalkLeft.add(loadImage("/assets/Sprites/player/11.png"));
        playerWalkLeft.add(loadImage("/assets/Sprites/player/12.png"));
        playerWalkLeft.add(loadImage("/assets/Sprites/player/13.png"));
        playerWalkLeft.add(loadImage("/assets/Sprites/player/14.png"));

        // --- Tiles (single list for all, preserve indexing) ---
        int count;

        // Grass
        for (count = 1; count <= 30; count++) {
            BufferedImage img = loadImage("/assets/Tiles/Collidable/1/" + count + ".png");
            if (img != null) tiles.add(img);
        }

        // Mud
        for (count = 1; count <= 4; count++) {
            BufferedImage img = loadImage("/assets/Tiles/Collidable/2/" + count + ".png");
            if (img != null) tiles.add(img);
        }

        // Walls
        for (count = 1; count <= 16; count++) {
            BufferedImage img = loadImage("/assets/Tiles/NonCollidable/1/" + count + ".png");
            if (img != null) tiles.add(img);
        }

        // Water
        for (count = 1; count <= 8; count++) {
            BufferedImage img = loadImage("/assets/Tiles/NonCollidable/2/" + count + ".png");
            if (img != null) tiles.add(img);
        }
    }

    // Clear all loaded images
    public static void clear() {
        playerWalkUp.clear();
        playerWalkDown.clear();
        playerWalkLeft.clear();
        playerWalkRight.clear();
        tiles.clear();
    }
}
