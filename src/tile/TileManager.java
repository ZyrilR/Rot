package tile;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import engine.GamePanel;
import items.Inventory;
import items.ItemRegistry;
import npc.*;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static utils.AssetManager.loadImage;
import static utils.Constants.*;

public class TileManager {

    //layerType : {BACKGROUND, DECORATION, BUILDING, INTERACTIVE}
    private String layerType;
    private String layerName;

    private int map[][];
    private boolean[][] collisionMap;

    private ArrayList<TileTeleporter> teleporters = new ArrayList<>();
    private ArrayList<TileLoot> loots = new ArrayList<>();
    private ArrayList<NPC> NPCs = new ArrayList<>();

    //TileSets
    public static ArrayList<Tile> BACKGROUND_TILES = new ArrayList<>();
    public static ArrayList<Tile> DECORATION_TILES = new ArrayList<>();
    public static ArrayList<Tile> BUILDING_TILES = new ArrayList<>();
    public static ArrayList<Tile> INTERACTIVE_TILES = new ArrayList<>();

    //TileSet Used
    public ArrayList<Tile> tiles;

    public int[][] getMap() {
        return map;
    }
    public boolean[][] getCollisionMap() {
        return collisionMap;
    }
    public ArrayList<Tile> getTiles() {
        return tiles;
    }
    public ArrayList<NPC> getNPCs() {
        return NPCs;
    }

    public TileManager(String layerType) {
        this.layerType = layerType;
        tiles = switch (layerType) {
            case "BACKGROUND" -> BACKGROUND_TILES;
            case "DECORATION" -> DECORATION_TILES;
            case "BUILDING" -> BUILDING_TILES;
            case "INTERACTIVE" -> INTERACTIVE_TILES;
            default -> null;
        };
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        synchronized (this) {
            for (int worldRow = 0; worldRow < map.length; worldRow++) {
                for (int worldCol = 0; worldCol < map[0].length; worldCol++) {

                    // Access the 2D array: [Row][Col]
                    int tileNum = map[worldRow][worldCol];

                    if (tileNum == 0 && (
                            layerType.equalsIgnoreCase("Interactive") ||
                                    layerType.equalsIgnoreCase("Building") ||
                                    layerType.equalsIgnoreCase("Background")
                    ))
                        continue;

                    if (tileNum != 0)
                        tileNum--;

                    // Map indices to coordinates
                    int worldX = worldCol * TILE_SIZE; // Columns move along X
                    int worldY = worldRow * TILE_SIZE; // Rows move along Y

                    // Screen position calculation
                    int screenX = worldX - gp.player.worldX + gp.player.screenX;
                    int screenY = worldY - gp.player.worldY + gp.player.screenY;

                    // Culling and Drawing...
                    if (worldX + TILE_SIZE > gp.player.worldX - gp.player.screenX &&
                            worldX - TILE_SIZE < gp.player.worldX + (SCREEN_WIDTH - gp.player.screenX) &&
                            worldY + TILE_SIZE > gp.player.worldY - gp.player.screenY &&
                            worldY - TILE_SIZE < gp.player.worldY + (SCREEN_HEIGHT  - gp.player.screenY)) {

//                    =========USED FOR DEBUGGING=========
//                    if (layerType.equalsIgnoreCase("Background")) {
//                        if (tileNum >= 0) {
//                            System.out.println("TILE NUM: " + tileNum);
//                            System.out.println("TILES SIZE: " + tiles.size());
//                            if (tileNum < tiles.size()) {
////                                if (tileNum == 0)
////                                    g2.drawImage(tiles.get(0).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
////                                else
////                                    g2.drawImage(tiles.get(tileNum).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
//                            g2.drawImage(tiles.get(11).image, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
//                            }
//                        }
//                    } else

                        if (tileNum >= 0) {
                            int currentSize = tiles.size();
                            if (tileNum < currentSize) {
                                g2.drawImage(tiles.get(tileNum).image, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                            }
                        }
                    }
                }
            }
        }
    }
    public void displayMapValues() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++)
                System.out.print(map[i][j] + " ");
            System.out.println();
        }
    }

    //path : "assets/Worlds/1/decoration.txt"
    public void loadTiles(String path, int tile_row, int tile_col, boolean isCollidable, String layerType) {
        map = new int[tile_row][tile_col];
        collisionMap = new boolean[tile_row][tile_col];
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.out.println("Map file not found: " + path);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;

            // Continue until we've filled all rows
            while (row < tile_row) {
                String line = br.readLine();

                // Safety check: if the file ends early, stop
                if (line == null)
                    break;

                // Split the line by spaces
                String[] numbers = line.split(",");

                // Fill columns for this specific row
                for (int col = 0; col < tile_col; col++) {
                    // Ensure we don't exceed the number of elements in the text line
                    if (col < numbers.length) {
                        int number = Integer.parseInt(numbers[col]);
                        map[row][col] = number;
                        if (isCollidable && number != 0) {
                            collisionMap[row][col] = true;
                        }
                    }
                }
                row++;
            }
//            displayMapValues();

            if (layerType.equalsIgnoreCase("interactive")) {
                br.readLine();
                String line;
                String[] items;
                while((line = br.readLine()) != null) {
//                    System.out.println(line);

                    String[] parts = line.split("\\|");

                    NPC npc1 = null;

                    switch (parts[1].toUpperCase()) {
                        case "TRAINERNPC", "GYMLEADER", "GYMMASTER":

                            ArrayList<BrainRot> party = new ArrayList<>();
                            String rots = parts[5];
                            String itemString = parts[6];
                            if (!parts[1].equalsIgnoreCase("TRAINERNPC")) {
                                rots = parts[6];
                                itemString = parts[7];
                            }

                            System.out.println(rots);

                            String[] brainrots = rots.split(";");
                            System.out.println(line);

                            for (String brainrot : brainrots) {
                                String[] rot = brainrot.split(":");
                                party.add(BrainRotFactory.create(rot[0], Integer.parseInt(rot[1])));
                            }

                            items = itemString.split(";");
                            Inventory inventory = new Inventory(items.length);
                            for (String item : items)
                                if (!item.isEmpty())
                                    inventory.addItem(ItemRegistry.getItem(item));

                            npc1 = switch(parts[1].toUpperCase()) {
                                case "TRAINERNPC" -> new TrainerNPC(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), inventory, party, Integer.parseInt(parts[7]));
                                case "GYMLEADER" -> new GymLeader(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), inventory, party, Integer.parseInt(parts[8]), parts[5]);
                                case "GYMMASTER" -> new GymMaster(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), inventory, party, Integer.parseInt(parts[8]));
                                default -> null;
                            };

                            break;
                        case "MARKETNPC":
                            npc1 = new MarketNPC(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                            break;
                        case "NPC":
                            npc1 = new NPC(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                            break;
                        case "TELEPORTER":
                            teleporters.add(new TileTeleporter(parts[0], parts[5], parts[1], Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[6].split(";")));
                            continue;
                        case "BACKPACK":
                            items = parts[5].split(";");
                            Inventory inv = new Inventory(items.length);
                            for (String item: items)
                                if (!item.isEmpty())
                                    inv.addItem(ItemRegistry.getItem(item));
                            loots.add(new TileLoot(null, Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), inv, parts[parts.length - 1].split(";")));
                            break;
                        default:
                            break;
                    };
                    String[] dialogues = parts[parts.length - 1].split(";");

                    if (npc1 != null)
                        npc1.setDialogue(dialogues);

                    NPCs.add(npc1);
                }
            }
            br.close();

        } catch (Exception e) {
            // This will tell you exactly what went wrong (e.g., File Not Found or NullPointer)
            e.printStackTrace();
        }
        System.out.println("[TileManager] Loaded Layer " + layerName);
    }

    public static void loadTiles() {
        System.out.println("[TileManager] Loading Tiles...");
        //Tiles
        int size = 2128;
        for (int i = 1; i <= size; i++)
            BACKGROUND_TILES.add(new Tile(loadImage("/res/Tiles/" + i + ".png")));

        System.out.println("[TileManager] Loaded " + size + " Tiles");

        size = 424;
        System.out.println("[TileManager] Loading Decorations...");
        //Decorations
        for (int i = 1; i <= size; i++) {
            Tile tile = null;
            //if first img = transparent : background : false
            if (i == 1 || i == 3 || (i >= 33 && i <= 38))
                tile = new Tile(loadImage("/res/Decorations/" + i + ".png"));
            //if second img = bush : interactive : false
            else if (contains(BUSH_INDEXES, i)) {
                switch (i) {
                    case 2:
                        tile = new TileSpawner(loadImage("/res/Decorations/" + i + ".png"), 1);
                        break;
                    case 16:
                        tile = new TileSpawner(loadImage("/res/Decorations/" + i + ".png"), 2);
                        break;
                }
            }
            else
                tile = new Tile(loadImage("/res/Decorations/" + i + ".png"), false, "Background");

            DECORATION_TILES.add(tile);
        }
        System.out.println("[TileManager] Loaded " + size + " Decoration Tiles");

        size = 127;
        int[] NON_COLLIDABLE = new int[]{92, 94, 2, 107, 64, 45};
        System.out.println("[TileManager] Loading Decorations...");

        for (int i = 1; i <= size; i++) {
            if (contains(NON_COLLIDABLE, i))
                BUILDING_TILES.add(new Tile(loadImage("/res/Buildings/" + i + ".png"), false));
            else
                BUILDING_TILES.add(new Tile(loadImage("/res/Buildings/" + i + ".png"), true));
        }
        System.out.println("[TileManager] Loaded " + size + " Building Tiles");

        System.out.println("[TileManager] Loading Sprites...");
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 5; j++) {
                INTERACTIVE_TILES.add(new Tile(loadImage("/res/InteractiveTiles/" + i + "/" + j + ".png"), true));
            }
        }
        System.out.println("[TileManager] Loaded " + 5 + " Sprites");

        size = 34;
        System.out.println("[TileManager] Loading Interactive Tiles...");
        for (int i = 26; i <= size; i++) {
            if (i >= 31 && i <= 34)
                INTERACTIVE_TILES.add(new Tile(loadImage("/res/InteractiveTiles/Interactives/" + i + ".png"), true));
            else
                INTERACTIVE_TILES.add(new Tile(loadImage("/res/InteractiveTiles/Interactives/" + i + ".png"), false));
        }
    }

    public ArrayList<TileTeleporter> getTeleporters() {
        return teleporters;
    }

    public String getLayerName() { return layerName; }
    public void setLayerName(String name) { this.layerName = name; }
    public ArrayList<TileLoot> getLoots() {
        return loots;
    }
}
