package items;

import java.util.ArrayList;

public class Inventory {
    private ArrayList<Item> items;
    private int capacity;

    public Inventory(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>();
    }

    public boolean addItem(Item item) {
        if (items.size() >= capacity) {
            System.out.println("Inventory full!"); //TODO: Trigger inventory full UI feedback
            return false;
        }
        items.add(item);
        return true;
    }

    public boolean removeItem(Item item) {
        return items.remove(item); //TODO: Update inventory UI after removal
    }

    public void useItem(int index) {
        if (index < 0 || index >= items.size()) {
            System.out.println("Invalid item index."); //TODO: Handle invalid input in menu
            return;
        }

        Item item = items.get(index);
        item.use(); //TODO: Apply effects to the targeted BrainRot / Player
    }

    public void listItems() {
        for (int i = 0; i < items.size(); i++) {
            System.out.println(i + ": " + items.get(i).getName()); //TODO: Replace with inventory UI list display
        }
    }
}