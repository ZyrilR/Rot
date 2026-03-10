package utils;

import engine.GamePanel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static utils.Constants.*;

public class TileManager {
    ArrayList<Tile> tiles;
    private final int[][] map = new int[MAX_SCREEN_ROW][MAX_SCREEN_COL];

    public TileManager() {
        tiles = new ArrayList<>();
        getTileAssets();
        displayMapValues();
    }

    public void getTileAssets() {
        for (int i = 1; i <= AssetManager.tiles; i++)
            tiles.add(new Tile(AssetManager.getImage("tiles_" + i)));
    }
    public void displayMapValues() {
        for (int i = 0; i < MAX_SCREEN_ROW; i++) {
            for (int j = 0; j < MAX_SCREEN_COL; j++)
                System.out.print(map[i][j] + " ");
            System.out.println();
        }
    }
    public void loadMap(int mapNo) {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/Maps/map_" + mapNo + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;
            // Continue until we've filled all rows
            while (row < MAX_SCREEN_ROW) {
                String line = br.readLine();

                // Safety check: if the file ends early, stop
                if (line == null) break;

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
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < MAX_SCREEN_ROW; i++) {
            for (int j = 0; j < MAX_SCREEN_COL; j++) {
                g.drawImage(tiles.get(map[i][j]).img, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }
}
