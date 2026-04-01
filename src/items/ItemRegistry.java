package items;

import java.util.ArrayList;

public class ItemRegistry {
    private static java.util.HashMap<String, Item> registry = new java.util.HashMap<>();

    static { // TODO: assign appropriate assetPath
        // Potions
        registerItem(new Stew("MILD STEW", "Restores 25% health", "assets/potion.png", 25, 500));
        registerItem(new Stew("MODERATE STEW", "Restores 50% health", "assets/potion.png", 50, 800));
        registerItem(new Stew("SUPER STEW", "Restores 100% health", "assets/potion.png", 100, 1200));

        // Antidotes
        registerItem(new Antidote("CONFUSION", "Removes confusion", "assets/antidote.png", "CONFUSE", 600));
        registerItem(new Antidote("PARALYZE", "Removes paralyze", "assets/antidote.png", "PARALYZE", 600));
        registerItem(new Antidote("SLEEP", "Removes sleep", "assets/antidote.png", "SLEEP", 600));
        registerItem(new Antidote("DEBUFF", "Removes all debuff stats", "assets/antidote.png", "DEBUFF", 600));

        // Capsules
        registerItem(new Capsule("RED CAPSULE", "Basic capture", "assets/capsule.png", 500));
        registerItem(new Capsule("BLUE CAPSULE", "Improved capture", "assets/capsule.png", 700));
        registerItem(new Capsule("SPEED CAPSULE", "Best vs fast targets", "assets/capsule.png", 900));
        registerItem(new Capsule("HEAVY CAPSULE", "Best vs high defense", "assets/capsule.png", 1200));
        registerItem(new Capsule("MASTER CAPSULE", "Guaranteed capture", "assets/capsule.png", 1500));

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