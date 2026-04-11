package map;

import engine.GamePanel;
import npc.NPC;
import tile.Tile;
import tile.TileManager;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Constants.*;

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
    private ArrayList<TileManager> rampLayers = new ArrayList<>();
    private TileManager interactiveLayer;

    private void resetLayers() {
        backgroundLayer.clear();
        decorationLayer.clear();
        buildingLayer.clear();
        rampLayers.clear();
        interactiveLayer = null;
    }

    private GamePanel gp;

    public WorldLoader(GamePanel gp) {
        this.gp = gp;
    }
    public void draw(Graphics2D graphics2D) {

        ArrayList<TileManager> tileManagers = new ArrayList<>(backgroundLayer);
        //Load Background
        for (TileManager tm : tileManagers) {
            if (tm != null)
                tm.draw(graphics2D, gp);
        }

        tileManagers = new ArrayList<>(decorationLayer);
        //Load Decorations
        for (TileManager tm : tileManagers) {
            if (tm != null)
                tm.draw(graphics2D, gp);
        }

        tileManagers = new ArrayList<>(buildingLayer);
        //Load Buildings
        for (TileManager tm : tileManagers) {
            if (tm != null)
                tm.draw(graphics2D, gp);
        }

        //Load Interactive
        if (interactiveLayer != null)
            interactiveLayer.draw(graphics2D, gp);

    }

    public void loadMap(String folderPath, boolean initWorldSettings) {

        // Get Layers (Naming Convention: background_#)
        try {
            InputStream is = getClass().getResourceAsStream(folderPath + "project_config.txt");
            if (is == null) {
                System.out.println("Project Configuration: " + folderPath + "project_config.txt' missing!" );
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
                resetLayers();
            }

            //initialize spawn point
            SPAWN_POINT[0] = Integer.parseInt(parts[3]);
            SPAWN_POINT[1] = Integer.parseInt(parts[4]);

            int numOfLayers = Integer.parseInt(parts[0]);

            Pattern overlayCollisionPattern = Pattern.compile("(?i)overlay.*collision_(\\d+)");

            for (int i = 0; i < numOfLayers; i++) {
                line = br.readLine();

                String[] layer = line.split(":");
                TileManager tm = new TileManager(layer[1]);
                tm.setLayerName(layer[0]);
                boolean isCollidable = layer[0].toLowerCase().contains("collision");
                tm.loadTiles(folderPath + layer[0] + ".txt", tile_row, tile_col, isCollidable, layer[1]);

                // Detect ramp layers
                if (layer[0].toLowerCase().contains("ramp")) {
                    tm.setRampLayer(true);
                    rampLayers.add(tm);
                }

                // Detect overlay-collision layers (e.g. overlay-collision_1)
                Matcher matcher = overlayCollisionPattern.matcher(layer[0]);
                if (matcher.find()) {
                    tm.setOverlayCollisionLevel(Integer.parseInt(matcher.group(1)));
                }

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

        gp.player.teleport(SPAWN_POINT);
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

    public ArrayList<TileManager> getRampLayers() {
        return rampLayers;
    }

}
