package utils;

public enum Directories {
    // FORMAT: Path, Min Level, Max Level, Rots Needed to Leave, Level Needed to Leave
    ROUTE131("/res/Worlds/Routes/Route131/", 1, 10, 5, 15),
    ROUTE132("/res/Worlds/Routes/Route132/", 10, 25, 8, 30),
    ROUTE130("/res/Worlds/Routes/Route130/", 25, 35, 8, 20),
    CAVE131("/res/Worlds/Caves/Cave131/", 30, 45, 8, 20),
    MARKET("/res/Rooms/Market/", 0, 0, 0, 0),
    SAVES("src/res/Saves/", 0, 0, 0, 0); // Kept exactly the same for DataManager!

    private final String path;
    private final int minLevel;
    private final int maxLevel;
    private final int reqRots;
    private final int reqLevel;

    Directories(String path, int minLevel, int maxLevel, int reqRots, int reqLevel) {
        this.path = path;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.reqRots = reqRots;
        this.reqLevel = reqLevel;
    }

    public String getPath() { return path; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
    public int getReqRots() { return reqRots; }
    public int getReqLevel() { return reqLevel; }

    // --- RE-IMPLEMENTED GETPATH METHOD ---
    public static String getPath(String pathName) {
        try {
            return valueOf(pathName.toUpperCase()).getPath();
        } catch (IllegalArgumentException e) {
            System.err.println("[Directories] Warning: Map name '" + pathName + "' not found. Falling back to Route 131.");
            return ROUTE131.getPath();
        }
    }

    public static Directories getByPath(String pathString) {
        for (Directories dir : values()) {
            if (pathString.contains(dir.name())) return dir;
        }
        return ROUTE131;
    }
}