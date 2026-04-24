package tile;

import engine.GamePanel;
import items.Inventory;
import items.Item;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

public class TileLoot extends TileInteractive {
    private int x;
    private int y;

    Inventory inventory;
    ArrayList<String> dialogues;

    public TileLoot(BufferedImage img, int x, int y, Inventory inventory, String[] dialogues) {
        super(img, false, "Spawner");
        this.inventory = inventory;
        this.x = x;
        this.y = y;
        this.dialogues = new ArrayList<>();
        Collections.addAll(this.dialogues, dialogues);
        for (Item item : inventory.getRawItems())
            this.dialogues.add(item.getName());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void interact(GamePanel gp) {
        gp.DIALOGUEBOX.startDialogue("System", dialogues);

        // Register this NPC as the pending shop opener.
        // DialogueBox will call gp.SHOPUI.open() + switch state when dialogue finishes.

        System.out.println("[Backpack Loot] Interaction triggered by player. Dialogue started.");
    }
}