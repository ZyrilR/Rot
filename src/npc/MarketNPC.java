package npc;

import engine.GamePanel;
import utils.AssetManager;

public class MarketNPC extends NPC {

    public MarketNPC(GamePanel gp, int x, int y) {
        super(gp, "Merchant", "MarketNPC", x, y);
        loadSprites();
        setDialogue();
    }

    @Override
    public void loadSprites() {
//        walk_down.add(AssetManager.loadImage("/assets/Sprites/1/1.png"));
    }

    @Override
    public void setDialogue() {
        dialogues[0] = "Welcome to the BrainRot Market!";
        dialogues[1] = "We have the best deals in town.";
    }

    @Override
    public void interact() {
        // Face the player
        facePlayer();
        // Open the dialogue box with the design you provided
        gp.dialogueBox.startDialogue(name, dialogues);
    }

    private void facePlayer() {
        switch(gp.player.getDirection()) {
            case "up" -> direction = "down";
            case "down" -> direction = "up";
            case "left" -> direction = "right";
            case "right" -> direction = "left";
        }
    }
}