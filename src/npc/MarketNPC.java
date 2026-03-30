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
        facePlayer(gp.player);
        gp.DIALOGUEBOX.startDialogue(name, dialogues);

        // Register this NPC as the pending shop opener.
        // DialogueBox will call gp.SHOPUI.open() + switch state when dialogue finishes.
        gp.DIALOGUEBOX.setPendingShopOpen(true);

        System.out.println("[MarketNPC] Interaction triggered by player. Dialogue started.");
    }
}