package tile;

import engine.GamePanel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import static utils.Constants.*;
import static utils.Directories.*;

public class TileTeleporter extends Tile{
    //FORMAT PATH: from~name~to~name
    private String name;
    private String link;

    private int[] coordinates = new int[2];
    public ArrayList<String> dialogues = new ArrayList<>();
    public boolean isInteracted = false;

    //Name|Role|TileNumber|x|y|from~name~direction|Dialogues
    //ToRoute132|Teleporter|28|49|13|ROUTE131~TOROUTE132~LEFT|You are about to enter;Route 132

    public TileTeleporter () {
        super(null, false, "Teleporter");
    }
    public TileTeleporter(String name, String link, String role, int x, int y, String[] dialogues) {
        super(null, false, role);
        this.name = name;
        this.link = link;
        Collections.addAll(this.dialogues, dialogues);
        this.coordinates[0] = x;
        this.coordinates[1] = y;
    }

    public String getLink() {
        return link;
    }
    public String getLinkTo() {
        return link.split("~")[0];
    }
    public String getLinkToTeleporterName() {
        return link.split("~")[1];
    }
    public String getDirection() {
        return link.split("~")[2];
    }

    public String getName() {
        return name;
    }
    public int[] getCoordinates() {
        return coordinates;
    }
    public void interact(GamePanel gp) {
        gp.DIALOGUEBOX.startDialogue("Narrator", dialogues);
        isInteracted = true;
    }
}
