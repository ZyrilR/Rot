package map;

import engine.GamePanel;
import tile.Tile;
import tile.TileManager;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import static utils.Constants.MAX_WORLD_COL;
import static utils.Constants.MAX_WORLD_ROW;

public class WorldLoader {
    private TileManager backgroundLayer;
    private ArrayList<TileManager> decorationLayer = new ArrayList<>();
    private ArrayList<TileManager> buildingLayer = new ArrayList<>();
    private ArrayList<TileManager> interactiveLayer = new ArrayList<>();

    private GamePanel gp;

    public WorldLoader(GamePanel gp) {
        this.gp = gp;
    }
    public void draw(Graphics2D graphics2D) {
        backgroundLayer.draw(graphics2D, gp);

        //Load Decorations
        for (int i = 0; i < decorationLayer.size(); i++) {
            TileManager dec = decorationLayer.get(i);

            dec.draw(graphics2D, gp);
        }

        //Load Buildings
        for (int i = 0; i < buildingLayer.size(); i++) {
            TileManager build = buildingLayer.get(i);

            build.draw(graphics2D, gp);
        }

    }

    /*
    LoadMap:
    - Load all the layers

     */
    public void loadMap(String folderPath, boolean initWorldSettings) {

        // Get Layers (Naming Convention: background_#)
        try {
            InputStream is = getClass().getResourceAsStream(folderPath + "project_config.txt");
            if (is == null) {
                System.out.println("Project Configuration: '" + folderPath + "project_config.txt' missing!" );
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();

            String[] parts = line.split(",");

            //initialize world
            int tile_row = Integer.parseInt(parts[1]);
            int tile_col = Integer.parseInt(parts[2]);

            if (initWorldSettings) {
                MAX_WORLD_ROW = tile_row;
                MAX_WORLD_COL = tile_col;
            }

            int numOfLayers = Integer.parseInt(parts[0]);

            for (int i = 0; i < numOfLayers; i++) {
                line = br.readLine();

                String[] layer = line.split(":");
                TileManager tm = new TileManager(layer[1]);
                tm.loadTiles(folderPath + layer[0] + ".txt", tile_row, tile_col);

                //check what kind of layer
                switch (layer[1].toUpperCase()) {
                    case "BACKGROUND":
                        backgroundLayer = tm;
                        break;
                    case "DECORATION":
                        decorationLayer.add(tm);
                        break;
                    case "BUILDING":
                        System.out.println("PATH: " + folderPath + layer[0] + ".txt");
                        System.out.println("ROW: " + tile_row);
                        System.out.println("COL: " + tile_col);
                        buildingLayer.add(tm);
                        System.out.println("ADDED BUILDING LAYER");
                        break;
                    case "INTERACTABLE":
                        interactiveLayer.add(tm);
                        break;
                    default:
                        System.out.println("NOT A LAYER!");
                        break;
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

//        map = new int[MAX_WORLD_ROW][MAX_WORLD_COL];
//        try {
//            InputStream is = getClass().getResourceAsStream("/assets/Worlds/world_" + mapNo + ".txt");
//            if (is == null) {
//                System.out.println("Map file not found: " + "/assets/Worlds/world_" + mapNo + ".txt");
//                return;
//            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//
//            int row = 0;
//
//            // Continue until we've filled all rows
//            while (row < MAX_WORLD_ROW) {
//                String line = br.readLine();
//
//                // Safety check: if the file ends early, stop
//                if (line == null)
//                    break;
//
//                // Split the line by spaces
//                String[] numbers = line.split(" ");
//
//                // Fill columns for this specific row
//                for (int col = 0; col < MAX_WORLD_COL; col++) {
//                    // Ensure we don't exceed the number of elements in the text line
//                    if (col < numbers.length) {
//                        map[row][col] = Integer.parseInt(numbers[col]);
//                    }
//                }
//                row++;
//            }
//            br.close();
//
//        } catch (Exception e) {
//            // This will tell you exactly what went wrong (e.g., File Not Found or NullPointer)
//            e.printStackTrace();
//        }
//        System.out.println();

    }

    public ArrayList<TileManager> getInteractiveLayer() {
        return interactiveLayer;
    }

    public ArrayList<TileManager> getBuildingLayer() {
        return buildingLayer;
    }

    public ArrayList<TileManager> getDecorationLayer() {
        return decorationLayer;
    }

    public TileManager getBackgroundLayer() {
        return backgroundLayer;
    }

}
