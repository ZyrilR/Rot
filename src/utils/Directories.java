package utils;

public enum Directories {
    //Etc.
    SAVES("src/res/Saves/"),

    //World
    ROUTE130("/res/Worlds/Routes/Route130/", new int[]{1, 25}),
    ROUTE131("/res/Worlds/Routes/Route131/"),
    ROUTE132("/res/Worlds/Routes/Route132/"),

    //Caves
    CAVE131("/res/Worlds/Caves/Cave131/"),

    //Rooms
    MARKET("/res/Rooms/Market/");

    private final String path;
    private final int[] range;

    Directories(String path, int[] range) {
        this.path = path;
        this.range = range;
    }
    Directories(String path) {
        this.path = path;
        this.range = new int[2];
    }

    public String getPath() {
        return path;
    }
    public static String getPath(String path) {
        return switch(path.toUpperCase()) {
            case "SAVES" -> SAVES.getPath();
            case "ROUTE130" -> ROUTE130.getPath();
            case "ROUTE131" -> ROUTE131.getPath();
            case "ROUTE132" -> ROUTE132.getPath();
            case "CAVE131" -> CAVE131.getPath();
            case "MARKET" -> MARKET.getPath();
            default -> ROUTE131.getPath();
        };
    }
    public int[] getRange() {
        return range;
    }
    public static int[] getRange(String path) {
        return switch(path.toUpperCase()) {
            case "ROUTE130" -> ROUTE130.getRange();
            case "ROUTE131" -> ROUTE131.getRange();
            case "ROUTE132" -> ROUTE132.getRange();
            default -> ROUTE131.getRange();
        };
    }
}
