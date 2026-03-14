package tile;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import static utils.Constants.*;

public class TileManager {

    private GamePanel gp;
    private ArrayList<Tile> tiles;
    private int map[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new ArrayList<>();
        map = new int[MAX_WORLD_COL][MAX_WORLD_ROW];
        getTileAssets();
        setupCollision();
    }

    public void draw(Graphics2D g2){
        int worldCol = 0;
        int worldRow = 0;

        while(worldCol < MAX_WORLD_COL && worldRow < MAX_WORLD_ROW){
            int tileNum = map[worldCol][worldRow];

            int worldX = worldCol * TILE_SIZE;
            int worldY = worldRow * TILE_SIZE;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            int tileRight = worldX + TILE_SIZE;
            int tileBottom = worldY + TILE_SIZE;

            if(tileRight > gp.player.worldX - gp.player.screenX &&
                    worldX < gp.player.worldX + (SCREEN_WIDTH - gp.player.screenX) &&
                    tileBottom > gp.player.worldY - gp.player.screenY &&
                    worldY < gp.player.worldY + (SCREEN_HEIGHT - gp.player.screenY)) {
                g2.drawImage(tiles.get(tileNum).img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
            }
            worldCol++;
            if(worldCol == MAX_WORLD_COL){
                worldCol = 0;
                worldRow++;
            }
        }
    }

    public void getTileAssets() {
        for (int i = 1; i <= AssetManager.tiles; i++) {
            tiles.add(new Tile(AssetManager.getImage("tile_" + i)));
            System.out.println("Added (Size|" + tiles.size() + "): " + ("tiles_" + i));
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

//    public void draw(Graphics2D g) {
//        for (int i = 0; i < map.length; i++) {
//            for (int j = 0; j < map[0].length; j++) {
//                g.drawImage(tiles.get(map[i][j]).getImg(), i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
//            }
//        }
//    }

    private void setupCollision() {
        // Loop through ALL loaded tiles and make them solid (collision = true)
        for (Tile t : tiles) {
            t.setCollision(true);
        }

        // 2. Make the Grass tile walkable (collision = false)
        // reads Folder '1' first, our Grass tile is loaded at index 0.
        if (!tiles.isEmpty()) {
            tiles.get(0).setCollision(false);
        }

        // add new walkable floors (like dirt or paths), just add them here
        // tiles.get(1).setCollision(false);
    }

    public int[][] getMap() {
        return map;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

}
