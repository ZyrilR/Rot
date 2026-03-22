package items;

import java.util.ArrayList;

public class ItemRegistry {
    private static java.util.HashMap<String, Item> registry = new java.util.HashMap<>();

    static { // TODO: assign appropriate assetPath
        // Potions
        registerItem(new Potion("TUBE POTION", "Restores 25% health", "assets/potion.png", 25, 500));
        registerItem(new Potion("POTION", "Restores 50% health", "assets/potion.png", 50, 800));
        registerItem(new Potion("HEAVY POTION", "Restores 100% health", "assets/potion.png", 100, 1200));

        // Antidotes
        registerItem(new Antidote("CONFUSION", "Removes confusion", "assets/antidote.png", "CONFUSE", 600));
        registerItem(new Antidote("PARALYZE", "Removes paralyze", "assets/antidote.png", "PARALYZE", 600));
        registerItem(new Antidote("SLEEP", "Removes sleep", "assets/antidote.png", "SLEEP", 600));
        registerItem(new Antidote("DEBUFF", "Removes all debuff stats", "assets/antidote.png", "DEBUFF", 600));

        // Capsules
        registerItem(new Capsule("RED CAPSULE", "Normal capture", "assets/capsule.png", 500));
        registerItem(new Capsule("BLUE CAPSULE", "Normal capture", "assets/capsule.png", 700));
        registerItem(new Capsule("SPEED CAPSULE", "Better capture if target is fast", "assets/capsule.png", 900));
        registerItem(new Capsule("HEAVY CAPSULE", "Better capture if target has high defense", "assets/capsule.png", 1200));
        registerItem(new Capsule("MASTER CAPSULE", "100% capture", "assets/capsule.png", 1500));

        // Scrolls
        registerItem(new Scroll("Scroll", "ambot", "assets/scroll.png", "Fireball", 0));
    }

    public static void registerItem(Item item) {
        registry.put(item.getName(), item); //TODO: Prevent duplicates if dynamically registering
    }

    public static Item getItem(String name) {
        Item item = registry.get(name);
        if (item == null) System.out.println("Item not found: " + name); //TODO: Handle missing items in UI
        return item;
    }

    public static ArrayList<Item> getAllItems() {
        return new ArrayList<>(registry.values());
    }
}