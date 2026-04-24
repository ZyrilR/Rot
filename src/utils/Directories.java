package utils;

public enum Directories {
    // FORMAT: Path, Base Difficulty Cap, Rots Needed to Leave, Level Needed to Leave
    ROUTE131("/res/Worlds/Routes/Route131/", 10, 5, 15),
    ROUTE132("/res/Worlds/Routes/Route132/", 25, 8, 30),
    ROUTE130("/res/Worlds/Routes/Route130/", 35, 8, 20),
    CAVE131("/res/Worlds/Caves/Cave131/", 35, 8, 20),
    MARKET("/res/Rooms/Market/", 0, 0, 0),
    SAVES("src/res/Saves/", 0, 0, 0);

    private final String path;
    private final int baseLevel;
    private final int reqRots;
    private final int reqLevel;

    Directories(String path, int baseLevel, int reqRots, int reqLevel) {
        this.path = path;
        this.baseLevel = baseLevel;
        this.reqRots = reqRots;
        this.reqLevel = reqLevel;
    }

    public String getPath() { return path; }
    public int getBaseLevel() { return baseLevel; }
    public int getReqRots() { return reqRots; }
    public int getReqLevel() { return reqLevel; }

    // --- RE-IMPLEMENTED GETPATH METHOD ---
    /** * Keeps your existing system working.
     * Converts a string like "ROUTE132" into the actual folder path.
     */
    public static String getPath(String pathName) {
        try {
            // This replaces the entire switch statement!
            // It looks for an Enum constant with the same name as the string.
            return valueOf(pathName.toUpperCase()).getPath();
        } catch (IllegalArgumentException e) {
            // Fallback if the name isn't found
            System.err.println("[Directories] Warning: Map name '" + pathName + "' not found. Falling back to Route 131.");
            return ROUTE131.getPath();
        }
    }

    /** Helper to get the full Enum object by searching the path string */
    public static Directories getByPath(String pathString) {
        for (Directories dir : values()) {
            if (pathString.contains(dir.name())) return dir;
        }
        return ROUTE131;
    }
}