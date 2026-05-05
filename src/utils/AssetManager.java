package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AssetManager {
    // --- FONTS ---
    public static Font pocketMonk, pokemonGb, pokemonSolid;

    // --- SPRITE CACHE ---
    private static final Map<String, BufferedImage> brainRotSprites = new HashMap<>();

    public static BufferedImage loadImage(String path) {
        try {
            InputStream is = AssetManager.class.getResourceAsStream(path);

            // DEFENSIVE PROGRAMMING: Check if the file exists before reading
            if (is == null) {
                System.out.println("[AssetManager] Missing asset, using fallback for: " + path);
                return null;
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
            // Load existing fonts
            InputStream is1 = AssetManager.class.getResourceAsStream("/res/Fonts/PocketMonk-15ze.ttf");
            if (is1 != null) pocketMonk = Font.createFont(Font.TRUETYPE_FONT, is1).deriveFont(22f);

            InputStream is2 = AssetManager.class.getResourceAsStream("/res/Fonts/PokemonGb-RAeo.ttf");
            if (is2 != null) pokemonGb = Font.createFont(Font.TRUETYPE_FONT, is2).deriveFont(18f);

            InputStream is3 = AssetManager.class.getResourceAsStream("/res/Fonts/Pokemon Solid.ttf");
            if (is3 != null) pokemonSolid = Font.createFont(Font.TRUETYPE_FONT, is3).deriveFont(24f);

            System.out.println("[AssetManager] All Fonts loaded successfully");
        } catch (Exception e){
            System.err.println("[AssetManager] Error loading custom fonts! Using fallbacks.");
            e.printStackTrace();

            // Fallbacks so the game doesn't crash
            pocketMonk = new Font("Arial", Font.BOLD, 22);
            pokemonGb = new Font("Monospaced", Font.PLAIN, 18);
            pokemonSolid = new Font("Arial", Font.BOLD, 24);
        }
    }

    public static BufferedImage getBrainRotSprite(String name, String tier, boolean isBack, int frame) {
        String sideModifier = isBack ? "_BACK" : "";
        String key = name + "_" + tier + sideModifier + "_" + frame;

        // 1. FAST CACHE RETURN
        if (brainRotSprites.containsKey(key)) {
            return brainRotSprites.get(key);
        }

        // 2. ATTEMPT TO LOAD FROM FOLDER
        String path = "/res/InteractiveTiles/Brainrots/" + toFolderName(name)
                + "/" + tier + sideModifier + "_" + frame + ".png";

        BufferedImage img = loadImage(path);

        // 3. THE BUG FIX: If an animation frame is missing, fall back to Frame 0
        if (img == null && frame != 0) {
            img = getBrainRotSprite(name, tier, isBack, 0);
        }

        // 4. CACHE IT NO MATTER WHAT
        brainRotSprites.put(key, img);

        return img;
    }

    private static String toFolderName(String name) {
        return switch (name.toUpperCase()) {
            case "TUNG TUNG TUNG SAHUR"   -> "TungTungTungSahur";
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