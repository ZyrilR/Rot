package utils;

public class Constants {

    // Screen Settings
    public static final int ORIGINAL_TILE_SIZE = 16; // 16x16 tile
    public static final int SCALE = 3;

    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // 48x48 tile
    public static final int MAX_SCREEN_COL = 16;
    public static final int MAX_SCREEN_ROW = 12;
    public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL; // 768 pixels
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW; // 576 pixels

    // World Settings
    public static int MAX_WORLD_ROW;
    public static int MAX_WORLD_COL;
    public static final int WORLD_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    public static final int WORLD_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

    public static final int FPS = 30;

    //Player Settings
    public static final int SPRINT_SPEED = 32;

    //Dialogue Settings
    public static final int TEXT_SPEED = 1;

    // PC & Shop UI Constants
    public static final int    GRID_COLS    = 5;
    public static final int    GRID_ROWS    = 5;
    public static final double PANEL_SPLIT  = 0.60;
    public static final int    INPUT_DELAY  = 10;
    public static final int    STATUS_TICKS = 60; // 2 s @ 30 FPS
    public static final int OUTER_PAD   = 18;
    public static final int STATUS_BAR_H = 44;

    // Menu UI Constants
    public static final int PANEL_W = 135;
    public static final int ROW_H   = 30;
    public static final int PAD_V   = 12;
}
