package npc;

import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import overworld.Player;

import java.util.ArrayList;

public class MarketNPC extends NPC {

    public MarketNPC(String name) {
        super(name, "MarketNPC", 0, 0);
        loadSprites();
        setDialogue();
    }

    // Attempt to buy an item from unlimited stock
    public boolean attemptPurchase(Player player, String itemName) {
        Item item = ItemRegistry.getItem(itemName);
        if (item == null) {
            System.out.println("Item not found.");
            return false;
        }

        if (player.getRotCoins() < item.getPrice()) {
            System.out.println("Not enough RotCoins!");
            return false;
        }

        if (!player.getInventory().addItem(item)) {
            System.out.println("Inventory full!");
            return false;
        }

        player.spendRotCoins(item.getPrice());
        System.out.println("Purchased " + item.getName() + " for " + item.getPrice() + " coins.");
        return true;
    }

    //We don't need to load sprites its already in the InteractiveTiles array in TileManager
    @Override
    public void loadSprites() {
//        walk_down.add(AssetManager.loadImage("/assets/Sprites/1/1.png"));
    }

    public void setDialogue() {
        ArrayList<String> dialogues = new ArrayList<>();
        dialogues.add("Welcome to the BrainRot Market!");
        dialogues.add("We have the best deals in town.");
        dialogues.add("I am " + name + ", What can i do for you?");
        super.setDialogue(dialogues);
    }

    @Override
    public void interact(GamePanel gp) {
        // Face the player
        facePlayer(gp);
        // Open the dialogue box with the design you provided
        gp.DIALOGUEBOX.startDialogue(name, dialogues);

        //make sure after the dialogue box display the shop GUI
        //shop();
    }

}