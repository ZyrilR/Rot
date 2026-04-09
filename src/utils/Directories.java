package utils;

public enum Directories {
    //Etc.
    SAVES("src/res/Saves/"),

    //World
    ROUTE130("/res/Worlds/Routes/Route130/"),
    ROUTE131("/res/Worlds/Routes/Route131/"),
    ROUTE132("/res/Worlds/Routes/Route132/"),

    //Rooms
    MARKET("/res/Rooms/Market/");

    private final String path;

    Directories(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    public static String getPath(String path) {
        return switch(path.toUpperCase()) {
            case "SAVES" -> SAVES.getPath();
            case "ROUTE131" -> ROUTE131.getPath();
            case "ROUTE132" -> ROUTE132.getPath();
            case "ROUTE130" -> ROUTE130.getPath();
            case "MARKET" -> MARKET.getPath();
            default -> ROUTE131.getPath();
        };
    }
}
