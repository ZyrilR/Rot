package utils;

import map.Tile;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
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
    public static final ArrayList<Tile> tiles = new ArrayList<>();

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

    public static Font pocketMonk, pokemonGb, pokemonSolid;

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

        try {
            // Load PocketMonk
            InputStream is1 = AssetManager.class.getResourceAsStream("/assets/Fonts/PocketMonk-15ze.ttf");
            pocketMonk = Font.createFont(Font.TRUETYPE_FONT, is1).deriveFont(22f);

            // Load PokemonGB (The classic dialogue font)
            InputStream is2 = AssetManager.class.getResourceAsStream("/assets/Fonts/PokemonGb-RAeo.ttf");
            pokemonGb = Font.createFont(Font.TRUETYPE_FONT, is2).deriveFont(18f);

            // Load Pokemon Solid (The logo font)
            InputStream is3 = AssetManager.class.getResourceAsStream("/assets/Fonts/Pokemon Solid.ttf");
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

        // --- Tiles (single list for all, preserve indexing) ---
        int count;

        // Grass
        for (count = 1; count <= 30; count++) {
            BufferedImage img = loadImage("/assets/Tiles/NonCollidable/1/" + count + ".png");
            if (img != null) {
                // Role: "Background", Collision: false
                tiles.add(new Tile(img, false, "Background"));
            }
        }

        // Mud
        for (count = 1; count <= 4; count++) {
            BufferedImage img = loadImage("/assets/Tiles/NonCollidable/2/" + count + ".png");
            if (img != null) {
                tiles.add(new Tile(img, false, "Background"));
            }
        }
        // Walls
        for (count = 1; count <= 16; count++) {
            BufferedImage img = loadImage("/assets/Tiles/Collidable/1/" + count + ".png");
            if (img != null) {
                // Role: "Wall", Collision: true
                tiles.add(new Tile(img, true, "Wall"));
            }
        }

        // Water
        for (count = 1; count <= 8; count++) {
            BufferedImage img = loadImage("/assets/Tiles/Collidable/2/" + count + ".png");
            if (img != null) {
                // Role: "Water", Collision: true
                tiles.add(new Tile(img, true, "Water"));
            }
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
