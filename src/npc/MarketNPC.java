package npc;

import engine.GamePanel;
import java.util.ArrayList;

public class MarketNPC extends NPC {

    public MarketNPC(String name, int folderId) {
        super(name, "MarketNPC", folderId);
        setDialogue();
    }

    public void setDialogue() {
        ArrayList<String> dialogues = new ArrayList<>();
        dialogues.add("Welcome to the BrainRot Market!");
        dialogues.add("I am " + name + ", I am wandering around.");
        super.setDialogue(dialogues);
    }

    @Override
    public void interact(GamePanel gp) {
        // 1. Tell the base NPC class to handle turning towards the player
        facePlayer(gp.player);

        // 2. Open the dialogue box
        gp.DIALOGUEBOX.startDialogue(name, dialogues);

        // gp.GAMESTATE = "shop"; (Uncomment later when building the shop!)
    }
}