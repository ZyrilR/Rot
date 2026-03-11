package map;

import engine.GamePanel;
import utils.AssetManager;

import java.awt.*;
import java.io.*;
import java.util.*;

import static utils.Constants.*;

public class TileManager {

    GamePanel gp;
    Tile[] tile;
    int mapTileNum[][];

    //ArrayList<Tile> tiles;
    //private int[][] map;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tile = new Tile[2];
        mapTileNum = new int[MAX_WORLD_COL][MAX_WORLD_ROW];
        getTileAssets();
    }

    public void getTileAssets(){
        tile[0] = new Tile(AssetManager.getImage("tile_1"));
        tile[1] = new Tile(AssetManager.getImage("tile_2"));
    }

    public void loadMap(String filePath){
        try{
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("Map file not found: " + filePath);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while(col < MAX_WORLD_COL && row < MAX_WORLD_ROW){
                String line = br.readLine();

                while(col < MAX_WORLD_COL) {
                    String nums[] = line.split(" ");
                    int num = Integer.parseInt(nums[col]);

                    mapTileNum[col][row] = num;
                    col++;
                }
                if(col == MAX_WORLD_COL){
                    col = 0;
                    row++;
                }
            }
            br.close();
            System.out.println("Map loaded");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2){
        int worldCol = 0;
        int worldRow = 0;

        while(worldCol < MAX_WORLD_COL && worldRow < MAX_WORLD_ROW){
            int tileNum = mapTileNum[worldCol][worldRow];

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
                g2.drawImage(tile[tileNum].img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
            }
            worldCol++;
            if(worldCol == MAX_WORLD_COL){
                worldCol = 0;
                worldRow++;
            }
        }
    }
    /*
    public void getTileAssets() {
        for (int i = 1; i <= AssetManager.tiles; i++) {
            tiles.add(new Tile(AssetManager.getImage("tiles_" + i)));
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
        map = new int[MAX_SCREEN_ROW][MAX_SCREEN_COL];
        try {
            InputStream is = getClass().getResourceAsStream("/assets/Maps/map_" + mapNo + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;
            // Continue until we've filled all rows
            while (row < MAX_SCREEN_ROW) {
                String line = br.readLine();

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

    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                g.drawImage(tiles.get(map[i][j]).getImg(), i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }
    */
}
