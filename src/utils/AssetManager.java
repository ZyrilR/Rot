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

    // --- ADD THIS TO ASSET MANAGER ---
    private static final java.util.Map<String, java.awt.image.BufferedImage> brainRotSprites = new java.util.HashMap<>();

    public static java.awt.image.BufferedImage getBrainRotSprite(String name, String tier, boolean isBack, int frame) {
        String sideModifier = isBack ? "_BACK" : "";
        String key = name + "_" + tier + sideModifier + "_" + frame;

        // 1. FAST CACHE RETURN
        if (brainRotSprites.containsKey(key)) {
            return brainRotSprites.get(key);
        }

        // 2. ATTEMPT TO LOAD FROM FOLDER
        String path = "/res/InteractiveTiles/Brainrots/" + toFolderName(name)
                + "/" + tier + sideModifier + "_" + frame + ".png";

        java.awt.image.BufferedImage img = loadImage(path);

        // 3. THE BUG FIX: If an animation frame (like 2 or 3) is missing, fall back to Frame 1!
        if (img == null && frame != 1) {
            img = getBrainRotSprite(name, tier, isBack, 0);
        }

        // 4. CACHE IT NO MATTER WHAT (Even if null) so the game never lags trying to read a missing file again!
        brainRotSprites.put(key, img);

        return img;
    }

    private static String toFolderName(String name) {
        return switch (name.toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR"  -> "TungTungTungSahur";
            case "TRALALERO TRALALA"      -> "TralaleroTralala";
            case "BOMBARDINO CROCODILO"   -> "BombardinoCrocodilo";
            case "LIRILI LARILA"          -> "LiriliLarila";
            case "BRR BRR PATAPIM"        -> "BrrBrrPatapim";
            case "BONECA AMBALABU"        -> "BonecaAmbalabu";
            case "UDIN DIN DIN DIN DUN"   -> "OdindindinDun";
            case "CAPUCCINO ASSASSINO"    -> "CapuccinoAssasino";
            default                       -> name.replace(" ", "");
        };
    }
}