package utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AssetManager {
    public static final ArrayList<BufferedImage> sprites = new ArrayList<>();

    public static BufferedImage loadImage(String path) {
        try {
            BufferedImage img = ImageIO.read(
                    Objects.requireNonNull(AssetManager.class.getResourceAsStream(path)) // loads image from classpath
            );
            return img;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load asset: " + path); // prints error if loading fails
            e.printStackTrace(); // useful for debugging
        }
        return null;
    }

    public static void loadAll() {


    }

    public static void clear() {
    }

}