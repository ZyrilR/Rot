package utils;

import items.Inventory;
import items.Item;

import java.util.HashMap;

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

    // PC UI Constants
    public static final int    GRID_COLS    = 5;
    public static final int    GRID_ROWS    = 5;
    public static final double PANEL_SPLIT  = 0.60;
    public static final int    INPUT_DELAY  = 6;
    public static final int    STATUS_TICKS = 60; // 2 s @ 30 FPS
    public static final int OUTER_PAD   = 18;
    public static final int STATUS_BAR_H = 44;

    // Menu UI Constants
    public static final int PANEL_W = 135;
    public static final int ROW_H   = 30;
    public static final int PAD_V   = 12;

    // Shop & Inventory UI constants
    public static final int LEFT_SPLIT   = 38;  // % of window width for left panel
    public static final int NAME_LINE_H  = 15;  // line height for wrapped name in desc card
    public static final int DESC_LINE_H  = 13;  // line height for description body text

    //Directories
    public static final String SAVES = "src/res/Saves";
    public static final String WORLD = "/res/Worlds/4/";
    public static final String MARKET = "/res/Rooms/Market/";

    //Asset Constants
    public static int[] BUSH_INDEXES = new int[]{2, 16};

    //Load Handler
    public static int CURRENT_LOAD = 2;

    //Brainrot Descriptions
    public static HashMap<String, String> BRAINROT_DESCRIPTIONS = new HashMap<>();

    public static final void InitializeBrainrotDescriptions() {
        BRAINROT_DESCRIPTIONS.put("TUNG TUNG TUNG SAHUR", "A cursed wooden log that wakes up at 3AM and just starts going. Nobody asked it to. Nobody can stop it. It has been drumming since before you were born and will still be drumming long after.");
        BRAINROT_DESCRIPTIONS.put("TRALALERO TRALALA", "A shark wearing Nike shoes who absolutely does not care about your opinion. It walks on the ocean floor, it vibes, it confuses marine biologists, and it has never once explained itself. The shoes are real. They are fresh.");
        BRAINROT_DESCRIPTIONS.put("BOMBARDINO CROCODILO", "An Italian military crocodile that runs entirely on espresso and unearned confidence. It flies a bomber jet it did not train for, drops things it probably shouldn't, and lands every time like it meant to do that.");
        BRAINROT_DESCRIPTIONS.put("LIRILI LARILA", "A very old elephant wearing sandals who controls time and simply does not rush. It has seen civilizations collapse. It is currently in no hurry. The sandals are comfortable. The clock around its neck has never shown the correct time.");
        BRAINROT_DESCRIPTIONS.put("BRR BRR PATAPIM", "A large hairy thing from the forest that goes brr brr and then patapim. Scientists have not studied it. Scientists are afraid. It emerges from dense jungle, stomps twice, snorts once, and then just stands there looking at you.");
        BRAINROT_DESCRIPTIONS.put("BONECA AMBALABU", "A frog doll riding car tires who is absolutely losing it at all times. It licks. It burns rubber. It cackles. It rolls into your life uninvited and leaves skid marks on everything you hold dear.");
        BRAINROT_DESCRIPTIONS.put("UDIN DIN DIN DIN DUN", "A being made entirely of a sound that should not exist. It says its own name over and over and somehow this is a psychic attack. The more you listen, the less you understand. The less you understand, the more it wins.");
        BRAINROT_DESCRIPTIONS.put("CAPUCCINO ASSASSINO", "A small assassin who smells like coffee and has already decided. It appears from steam. It hits you with a milk frother. It fires two espresso shots point blank. It was never here. The cup is empty. You are on the floor.");
    }

    public static final boolean contains(int[] arr, int val) {
        for (int x : arr) {
            if (x == val)
                return true;
        }
        return false;
    }

    public static final int countOf(String name, Inventory inventory) {
        int c = 0;
        for (Item item : inventory.getRawItems())
            if (item.getName().equalsIgnoreCase(name)) c++;
        return c;
    }

    public static final String getDescription(String key) {
        return BRAINROT_DESCRIPTIONS.get(key);
    }

}
