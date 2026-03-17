package tile;

import engine.GamePanel;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import static utils.AssetManager.loadImage;
import static utils.Constants.*;

public class TileManager {

    //layerType : {BACKGROUND, DECORATION, BUILDING, INTERACTIVE}
    private String layerType;
    private int map[][];

    //TileSets
    public static ArrayList<Tile> BACKGROUND_TILES = new ArrayList<>();
    public static ArrayList<Tile> DECORATION_TILES = new ArrayList<>();
    public static ArrayList<Tile> BUILDING_TILES = new ArrayList<>();
    public static ArrayList<Tile> INTERACTIVE_TILES = new ArrayList<>();

    //TileSet Used
    public ArrayList<Tile> tiles = new ArrayList<>();

    public int[][] getMap() {
        return map;
    }
    public ArrayList<Tile> getTiles() {
        return tiles;
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
        for (int worldRow = 0; worldRow < MAX_WORLD_ROW; worldRow++) {
            for (int worldCol = 0; worldCol < MAX_WORLD_COL; worldCol++) {

                // Access the 2D array: [Row][Col]
                int tileNum = map[worldRow][worldCol];
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

//                    if (layerType.equalsIgnoreCase("Building")) {
//                        if (tileNum >= 0) {
//                            System.out.println("TILE NUM: " + tileNum);
//                            System.out.println("TILES SIZE: " + tiles.size());
//                            if (tileNum < tiles.size()) {
////                                if (tileNum == 0)
////                                    g2.drawImage(tiles.get(0).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
////                                else
////                                    g2.drawImage(tiles.get(tileNum).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
//                            g2.drawImage(tiles.get(0).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
//                            }
//                        }
//                    } else

                    if (tileNum >= 0) {

                        if (tileNum < tiles.size()) {
                            g2.drawImage(tiles.get(tileNum).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
//                            g2.drawImage(tiles.get(3).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
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
    public void loadTiles(String path, int tile_row, int tile_col) {
        map = new int[tile_row][tile_col];
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.out.println("Map file not found: " + path);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;

            // Continue until we've filled all rows
            while (row < MAX_WORLD_ROW) {
                String line = br.readLine();

                // Safety check: if the file ends early, stop
                if (line == null)
                    break;

                // Split the line by spaces
                String[] numbers = line.split(",");

                // Fill columns for this specific row
                for (int col = 0; col < MAX_WORLD_COL; col++) {
                    // Ensure we don't exceed the number of elements in the text line
                    if (col < numbers.length) {
                        map[row][col] = Integer.parseInt(numbers[col]);
                    }
                }
                row++;
            }
            displayMapValues();
            br.close();

        } catch (Exception e) {
            // This will tell you exactly what went wrong (e.g., File Not Found or NullPointer)
            e.printStackTrace();
        }
//        displayMapValues();
        System.out.println();
    }

    public void loadRoom(int roomNo) {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/Rooms/room_" + roomNo + ".txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            //Read first line of the file which contains the row and col values
            String line = br.readLine();
            if (line != null) {
                String[] numbers = line.split(" ");

                map = new int[Integer.parseInt(numbers[0])][Integer.parseInt(numbers[1])];
            } else {
                throw new RuntimeException();
            }

            int row = 0;
            // Continue until we've filled all rows
            while (row < MAX_SCREEN_ROW) {
                line = br.readLine();

                // Safety check: if the file ends early, stop
                if (line == null)
                    break;

                // Split the line by spaces
                String[] numbers = line.split(" ");

                // Fill columns for this specific row
                for (int col = 0; col < MAX_SCREEN_COL; col++) {
                    // Ensure we don't exceed the number of elements in the text line
                    if (col < numbers.length) {
                        map[row][col] = Integer.parseInt(numbers[col]);
                    }
                }
                row++;
            }
            br.close();

        } catch (Exception e) {
            // This will tell you exactly what went wrong (e.g., File Not Found or NullPointer)
            e.printStackTrace();
        }
        displayMapValues();
        System.out.println();
    }

    public static void loadTiles() {
        int count = 1;

        //Grass
        for (int i = 1; i <= 30; i++, count++) {
            BACKGROUND_TILES.add(new Tile(loadImage("/assets/Tiles/NonCollidable/1/" + count + ".png")));
            System.out.println("ADDED: Grass Tile " + BACKGROUND_TILES.size());
        }

        //Pathway Mud
        for (int i = 1; i <= 4; i++, count++) {
            BACKGROUND_TILES.add(new Tile(loadImage("/assets/Tiles/NonCollidable/2/" + i + ".png")));
            System.out.println("ADDED: Pathway Mud " + i + BACKGROUND_TILES.size());
        }

        //Walls
        for (int i = 1; i <= 16; i++, count++) {
            BACKGROUND_TILES.add(new Tile(loadImage("/assets/Tiles/Collidable/1/" + i + ".png"), true));
            System.out.println("ADDED: Walls " + i + BACKGROUND_TILES.size());
        }

        //Water
        for (int i = 1; i <= 8; i++, count++) {
            BACKGROUND_TILES.add(new Tile(loadImage("/assets/Tiles/Collidable/2/" + i + ".png"), true));
            System.out.println("ADDED: Water " + i + BACKGROUND_TILES.size());
        }

        //Decorations
        for (int i = 1; i <= 32; i++) {
            Tile tile;
            //if first img = transparent : background : false
            if (i == 1 || i == 3)
                tile = new Tile(loadImage("/assets/Decorations/" + i + ".png"));
            //if second img = bush : interactive : false
            else if (i == 2)
                tile = new TileInteractive(loadImage("/assets/Decorations/" + i + ".png"));
            else
                tile = new Tile(loadImage("/assets/Decorations/" + i + ".png"), true, "Background");

            DECORATION_TILES.add(tile);
            System.out.println("ADDED: Decorations " + i + DECORATION_TILES.size());
        }

        for (int i = 1; i <= 17; i++) {
            BUILDING_TILES.add(new Tile(loadImage("/assets/Buildings/1/" + i + ".png"), true));
            System.out.println("ADDED: Building 1 " + i + BACKGROUND_TILES.size());
        }


    }


}
