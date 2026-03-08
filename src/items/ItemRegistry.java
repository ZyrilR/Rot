package items;

public class ItemRegistry {
    private static java.util.HashMap<String, Item> registry = new java.util.HashMap<>();

    static { // TODO: assign appropriate assetPath
        // Potions
        registerItem(new Potion("TUBE POTION", "Restores 25% health", "assets/potion.png", 25));
        registerItem(new Potion("POTION", "Restores 50% health", "assets/potion.png", 50));
        registerItem(new Potion("HEAVY POTION", "Restores 100% health", "assets/potion.png", 100));

        // Antidotes
        registerItem(new Antidote("CONFUSION", "Removes confusion", "assets/antidote.png", "CONFUSE"));
        registerItem(new Antidote("PARALYZE", "Removes paralyze", "assets/antidote.png", "PARALYZE"));
        registerItem(new Antidote("SLEEP", "Removes sleep", "assets/antidote.png", "SLEEP"));
        registerItem(new Antidote("DEBUFF", "Removes all debuff stats", "assets/antidote.png", "DEBUFF"));

        // Capsules
        registerItem(new Capsule("RED CAPSULE", "Normal capture", "assets/capsule.png"));
        registerItem(new Capsule("BLUE CAPSULE", "Normal capture", "assets/capsule.png"));
        registerItem(new Capsule("SPEED CAPSULE", "Better capture if target is fast", "assets/capsule.png"));
        registerItem(new Capsule("HEAVY CAPSULE", "Better capture if target has high defense", "assets/capsule.png"));
        registerItem(new Capsule("MASTER CAPSULE", "100% capture", "assets/capsule.png"));

        // Scrolls
        registerItem(new Scroll("Scroll", "ambot", "assets/scroll.png", "Fireball"));
    }

    public static void registerItem(Item item) {
        registry.put(item.getName(), item); //TODO: Prevent duplicates if dynamically registering
    }

    public static Item getItem(String name) {
        Item item = registry.get(name);
        if (item == null) System.out.println("Item not found: " + name); //TODO: Handle missing items in UI
        return item;
    }
}