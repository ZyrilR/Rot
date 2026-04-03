package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AssetManager {
    public static Font pocketMonk, pokemonGb, pokemonSolid;

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
        try {
            // Load PocketMonk
            InputStream is1 = AssetManager.class.getResourceAsStream("/res/Fonts/PocketMonk-15ze.ttf");
            pocketMonk = Font.createFont(Font.TRUETYPE_FONT, is1).deriveFont(22f);

            // Load PokemonGB (The classic dialogue font)
            InputStream is2 = AssetManager.class.getResourceAsStream("/res/Fonts/PokemonGb-RAeo.ttf");
            pokemonGb = Font.createFont(Font.TRUETYPE_FONT, is2).deriveFont(18f);

            // Load Pokemon Solid (The logo font)
            InputStream is3 = AssetManager.class.getResourceAsStream("/res/Fonts/Pokemon Solid.ttf");
            pokemonSolid = Font.createFont(Font.TRUETYPE_FONT, is3).deriveFont(24f);
            System.out.println("Loaded successfully");
        } catch (Exception e){
            System.err.println("Error loading custom fonts! Using fallbacks.");
            e.printStackTrace();
            // Fallbacks so the game doesn't crash
            pocketMonk = new Font("Arial", Font.BOLD, 22);
            pokemonGb = new Font("Monospaced", Font.PLAIN, 18);
            pokemonSolid = new Font("Arial", Font.BOLD, 24);
        }
    }

}

