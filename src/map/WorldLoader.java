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

    /*
    WORLD IS MULTILAYERED:
    Only 1 Background Layer Exists
    Flexible Amount Of Decorative Tiles
    Flexible Amount Of Building Tiles
    Only 1 Interactive Layer (Interactive Tiles should not Collide in a single Tile)
     */
    private ArrayList<TileManager> backgroundLayer = new ArrayList<>();
    private ArrayList<TileManager> decorationLayer = new ArrayList<>();
    private ArrayList<TileManager> buildingLayer = new ArrayList<>();
    private TileManager interactiveLayer;

    private GamePanel gp;

    public WorldLoader(GamePanel gp) {
        this.gp = gp;
    }
    public void draw(Graphics2D graphics2D) {

        //Load Background
        for (TileManager tm : backgroundLayer) {
            tm.draw(graphics2D, gp);
        }

        //Load Decorations
        for (TileManager tm : decorationLayer) {
            tm.draw(graphics2D, gp);
        }

        for (TileManager tm : buildingLayer) {
            tm.draw(graphics2D, gp);
        }

        if (interactiveLayer != null) {
            interactiveLayer.draw(graphics2D, gp);
        }
    }

    // Draws everything that should COVER the player
    public void drawTop(Graphics2D graphics2D) {
        for (TileManager tm : decorationLayer) {
            tm.draw(graphics2D, gp);
        }
    }

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
                        backgroundLayer.add(tm);
                        break;
                    case "DECORATION":
                        decorationLayer.add(tm);
                        break;
                    case "BUILDING":
                        buildingLayer.add(tm);
                        break;
                    case "INTERACTIVE":
                        interactiveLayer = tm;
                        break;
                    default:
                        System.out.println("NOT A LAYER!");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public TileManager getInteractiveLayer() {
        return interactiveLayer;
    }

    public ArrayList<TileManager> getBuildingLayer() {
        return buildingLayer;
    }

    public ArrayList<TileManager> getDecorationLayer() {
        return decorationLayer;
    }

    public ArrayList<TileManager> getBackgroundLayer() {
        return backgroundLayer;
    }

}
