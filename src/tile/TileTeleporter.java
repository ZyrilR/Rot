package tile;

import engine.GamePanel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import static utils.Constants.*;

public class TileTeleporter extends Tile{

    //File name (World or Room)
    //Link to self
    //"/assets/Worlds/3"
    private String self;

    //Link to other shit
    //"/assets/Rooms/1"
    private String link;
    private String linkName;

    private int[] coordinates = new int[2];
    public ArrayList<String> dialogues = new ArrayList<>();
    public boolean isInteracted = false;

    public TileTeleporter () {
        super(null, false, "Teleporter");
    }
    public TileTeleporter(String link, int x, int y, String[] dialogues) {
        super(null, false, "Teleporter");
        this.link = switch (link.toUpperCase()) {
            case "WORLD" -> WORLD;
            case "MARKET" -> MARKET;
            default -> MARKET;
        };
        Collections.addAll(this.dialogues, dialogues);
        this.coordinates[0] = x;
        this.coordinates[1] = y;
    }

    public String getSelf() {
        return self;
    }

    public String getLink() {
        return link;
    }

    public int[] getCoordinates() {
        return coordinates;
    }
    public void interact(GamePanel gp) {
        gp.DIALOGUEBOX.startDialogue("Teleporter", dialogues);
        isInteracted = true;
    }
}
