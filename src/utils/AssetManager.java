package utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AssetManager {
    public static final ArrayList<BufferedImage> tileAssets = new ArrayList<>();
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

        int count = 1;

        //Grass
        for (int i = 1; i <= 30; i++, count++)
            tileAssets.add(loadImage("/assets/Tiles/1/" + count + ".png"));

        //Pathway Mud
        for (int i = 1; i <= 4; i++, count++)
            tileAssets.add(loadImage("/assets/Tiles/2/" + i + ".png"));

        //
        for (int i = 1; i <= 16; i++, count++)
            tileAssets.add(loadImage("/assets/Tiles/3/" + i + ".png"));

        for (int i = 1; i <= 8; i++, count++)
            tileAssets.add(loadImage("/assets/Tiles/4/" + i + ".png"));

    }

    public static void clear() {
        tileAssets.clear(); // removes all assets from memory
    }

}