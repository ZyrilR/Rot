package npc;

import engine.GamePanel;
import map.WorldLoader;
import overworld.Player;
import tile.TileInteractive;
import tile.TileManager;
import utils.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static utils.Constants.*;

public class NPC extends TileInteractive {
    public String name;
    public int worldX, worldY;
    public String direction = "down";
    public Rectangle solidArea;

    // Every NPC manages their own dialogue
    public ArrayList<String> dialogues;

    public NPC(String name, String role, int x, int y) {
        super(true, role);
        this.name = name;
        this.worldX = x;
        this.worldY = y;

        //???
        this.solidArea = new Rectangle(8, 16, 32, 32);
    }

    // MANDATORY METHODS for every child NPC
    public void setDialogue(ArrayList<String> dialogues) {
        this.dialogues = dialogues;
    }
    public void loadSprites() {

    }

    //Every NPC reacts differently
    @Override
    public void interact(GamePanel gp) {

    }

    protected void facePlayer(GamePanel gp) {
        switch(gp.player.getDirection()) {
            case "up" -> direction = "down";
            case "down" -> direction = "up";
            case "left" -> direction = "right";
            case "right" -> direction = "left";
        }
    }
}