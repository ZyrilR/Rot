package utils;

public enum Directories {
    // FORMAT: Path, Min Level, Max Level, Rots Needed to Leave, Level Needed to Leave

    //ROUTES

    //Route130
    ROUTE130("/res/Worlds/Routes/Route130/", 25, 35, 8, 20),
    ROUTE130Market("/res/Worlds/Rooms/Route130Market/", 0, 0, 0, 0),
    GYMFIGHTING("/res/Worlds/Rooms/GymFighting/", 0, 0, 0, 0),
    GYMFIGHTINGROOM1("/res/Worlds/Rooms/GymFighting/Room1/", 0, 0, 0, 0),

    //Route131
    ROUTE131("/res/Worlds/Routes/Route131/", 1, 10, 5, 15),

    //Route132
    ROUTE132("/res/Worlds/Routes/Route132/", 10, 25, 8, 30),

    //Route140
    ROUTE140("/res/Worlds/Routes/Route140/", 35, 50, 8, 35),
    GYMMASTER("/res/Worlds/Rooms/GymMaster/", 35, 50, 8, 35),
    GYMMASTERROOM1("/res/Worlds/Rooms/GymMaster/Room1/", 35, 50, 8, 35),
    GYMMASTERROOM2("/res/Worlds/Rooms/GymMaster/Room2/", 35, 50, 8, 35),
    GYMMASTERROOM3("/res/Worlds/Rooms/GymMaster/Room3/", 35, 50, 8, 35),
    GYMMASTERCORRIDOR("/res/Worlds/Rooms/GymMaster/Corridor/", 35, 50, 8, 35),
    GYMMASTERSTAIRS("/res/Worlds/Rooms/GymMaster/Stairs/", 35, 50, 8, 35),
    GYMMASTERFINALFLOOR("/res/Worlds/Rooms/GymMaster/FinalFloor/", 35, 50, 8, 35),

    //CAVES
    CAVE131("/res/Worlds/Caves/Cave131/", 30, 45, 8, 20),
    GYMSTONE("/res/Worlds/Caves/Cave131/GymStone/", 30, 45, 8, 20),

    //ROOMS
    DEWDROPPIER("/res/Worlds/Rooms/DewdropPier/", 0, 0, 0, 0),
    WATERGYMFLOOR1("/res/Worlds/Rooms/WaterGymFloor1/", 0, 0, 0, 0),
    WATERGYMFLOOR2("/res/Worlds/Rooms/WaterGymFloor2/", 0, 0, 0, 0),
    MARKET("/res/Worlds/Rooms/Route130Market/", 0, 0, 0, 0),

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