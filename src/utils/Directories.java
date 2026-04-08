package utils;

import static utils.Constants.WORLD_USED;

public enum Directories {
    //Etc.
    SAVES("src/res/Saves"),

    //World
    WORLD("/res/Worlds/" + WORLD_USED + "/"),

    //Rooms
    MARKET("/res/Rooms/Market/");

    private final String path;

    Directories(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
