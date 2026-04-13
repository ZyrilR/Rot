package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class AssetManager {
    public static Font pocketMonk, pokemonGb, pokemonSolid;

    public static BufferedImage loadImage(String path) {
        try {
            InputStream is = AssetManager.class.getResourceAsStream(path);

            // DEFENSIVE PROGRAMMING: Check if the file exists before reading
            if (is == null) {
                System.out.println("[AssetManager] Missing asset, using fallback for: " + path);
                return null; // Return null safely instead of crashing
            }

            return ImageIO.read(is);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[AssetManager] Failed to load asset: " + path);
            e.printStackTrace();
        }
        return null;
    }

    public static void loadAll() {
        try {
            // Load PocketMonk
            InputStream is1 = AssetManager.class.getResourceAsStream("/res/Fonts/PocketMonk-15ze.ttf");
            if (is1 != null) pocketMonk = Font.createFont(Font.TRUETYPE_FONT, is1).deriveFont(22f);

            // Load PokemonGB (The classic dialogue font)
            InputStream is2 = AssetManager.class.getResourceAsStream("/res/Fonts/PokemonGb-RAeo.ttf");
            if (is2 != null) pokemonGb = Font.createFont(Font.TRUETYPE_FONT, is2).deriveFont(18f);

            // Load Pokemon Solid (The logo font)
            InputStream is3 = AssetManager.class.getResourceAsStream("/res/Fonts/Pokemon Solid.ttf");
            if (is3 != null) pokemonSolid = Font.createFont(Font.TRUETYPE_FONT, is3).deriveFont(24f);

            System.out.println("[AssetManager] Fonts loaded successfully");
        } catch (Exception e){
            System.err.println("[AssetManager] Error loading custom fonts! Using fallbacks.");
            e.printStackTrace();
            // Fallbacks so the game doesn't crash
            pocketMonk = new Font("Arial", Font.BOLD, 22);
            pokemonGb = new Font("Monospaced", Font.PLAIN, 18)  ;
            pokemonSolid = new Font("Arial", Font.BOLD, 24);
        }
    }
}