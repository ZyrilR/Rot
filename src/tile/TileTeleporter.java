package tile;

import engine.GamePanel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import static utils.Constants.*;
import static utils.Directories.*;

public class TileTeleporter extends Tile{
    //FORMAT PATH: from~to
    private String link;
    public boolean isExit = false;

    private int[] coordinates = new int[2];
    public ArrayList<String> dialogues = new ArrayList<>();
    public boolean isInteracted = false;

    public TileTeleporter () {
        super(null, false, "Teleporter");
    }
    public TileTeleporter(String link, String role, int x, int y, String[] dialogues) {
        super(null, false, role);
        if (role.equalsIgnoreCase("Exit"))
            isExit = true;
        this.link = link;
        Collections.addAll(this.dialogues, dialogues);
        this.coordinates[0] = x;
        this.coordinates[1] = y;
    }

    public String getLink() {
        return link;
    }
    public String getLinkFrom() {
        return switch(link.split("~")[0]) {
            case "WORLD" -> WORLD.getPath();
            case "MARKET" -> MARKET.getPath();
            default -> WORLD.getPath();
        };
    }
    public String getLinkTo() {
        return switch(link.split("~")[1]) {
            case "WORLD" -> WORLD.getPath();
            case "MARKET" -> MARKET.getPath();
            default -> WORLD.getPath();
        };
    }

    public int[] getCoordinates() {
        return coordinates;
    }
    public void interact(GamePanel gp) {
        gp.DIALOGUEBOX.startDialogue("Narrator", dialogues);
        isInteracted = true;
    }
}
