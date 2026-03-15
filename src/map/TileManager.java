package map;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static utils.Constants.*;

public class TileManager {

    public static ArrayList<Tile> tiles = new ArrayList<>();
    private GamePanel gp;
    private int map[][];

    public int[][] getMap() {
        return map;
    }

    public TileManager(GamePanel gp) {
        this.gp = gp;
        map = new int[MAX_WORLD_COL][MAX_WORLD_ROW];
        initializeTiles();
    }

    public void initializeTiles() {
        // Instead of recreating them, just use the modular list from AssetManager
        tiles = AssetManager.tiles;

        if (tiles.isEmpty()) {
            System.out.println("WARNING: Tiles list is empty. Make sure AssetManager.loadAll() is called first!");
        }
    }

    public void draw(Graphics2D g2) {
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

                    if (tileNum >= 0 && tileNum < tiles.size()) {
                        g2.drawImage(tiles.get(tileNum).image, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
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
    public void loadMap(int mapNo) {
        map = new int[MAX_WORLD_ROW][MAX_WORLD_COL];
        try {
            InputStream is = getClass().getResourceAsStream("/assets/Maps/world_" + mapNo + ".txt");
            if (is == null) {
                System.out.println("Map file not found: " + "/assets/Maps/world_" + mapNo + ".txt");
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
                String[] numbers = line.split(" ");

                // Fill columns for this specific row
                for (int col = 0; col < MAX_WORLD_COL; col++) {
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

}
