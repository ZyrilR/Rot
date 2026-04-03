package items;

import brainrots.BrainRot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {
    private final ArrayList<Item> items; // stores player's items
    private final int capacity;          // maximum number of items allowed

    public Inventory(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>();
    }

    /** Adds an item to the inventory */
    public boolean addItem(Item item) {
        if (items.size() >= capacity) {
            System.out.println("Inventory full!");
            return false;
        }
        items.add(item);
        return true;
    }

    /** Removes the first occurrence of the specific item instance */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    /**
     * Uses an item at a given index on a BrainRot target.
     * Passes extra arguments to handle special items (e.g., Capsules).
     * Consumes the item after use.
     */
    public void useItem(int index, BrainRot target, Object... extraArgs) {
        if (index < 0 || index >= items.size()) {
            System.out.println("Invalid item index.");
            return;
        }

        Item item = items.get(index);
        item.use(target, extraArgs); // pass extra arguments
        items.remove(index);         // automatically consume
    }

    /** Check if inventory contains an item by name */
    public boolean hasItem(String name) {
        return items.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name));
    }

    /** Return a list of item names */
    public ArrayList<String> listItemNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Item item : items) names.add(item.getName());
        return names;
    }

    /**
     * Returns the raw backing list as an unmodifiable view.
     * Used by InventoryUI to read items for display / quantity counting
     * without exposing mutability.
     */
    public List<Item> getRawItems() {
        return Collections.unmodifiableList(items);
    }

    /** Get current inventory size */
    public int getSize() {
        return items.size();
    }

    /** Get inventory capacity */
    public int getCapacity() {
        return capacity;
    }
}