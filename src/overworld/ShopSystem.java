package overworld;

import items.Item;
import items.ItemRegistry;
import overworld.Player;
import progression.QuestSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Singleton manager for the global shop inventory and purchase logic.
 * No rendering — pure data and business rules.
 */
public class ShopSystem {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static ShopSystem instance;

    public static ShopSystem getInstance() {
        if (instance == null) instance = new ShopSystem();
        return instance;
    }

    // ── Purchase result ───────────────────────────────────────────────────────

    public enum PurchaseResult {
        SUCCESS,
        NOT_ENOUGH_COINS,
        INVENTORY_FULL
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final List<Item> shopItems = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    private ShopSystem() {
        buildInventory();
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    private void buildInventory() {
        String[] names = {
                // Stews
                "MILD STEW", "MODERATE STEW", "SUPER STEW",
                // Antidotes
                "CONFUSION CURE", "PARALYZE CURE", "BURN CURE", "SLEEP CURE", "DEBUFF TONIC",
                // Capsules
                "NORMAL CAPSULE", "RED CAPSULE", "BLUE CAPSULE", "SPEED CAPSULE", "HEAVY CAPSULE",
                // UP Bottles
                "UP BOTTLE", "UP MEDIUM BOTTLE", "UP HIGH BOTTLE", "UP FULL BOTTLE"
        };

        for (String name : names) {
            Item item = ItemRegistry.getItem(name);
            if (item != null) shopItems.add(item);
            else System.out.println("[ShopSystem] Warning: item not found: " + name);
        }

        shopItems.sort(Comparator.comparingInt(Item::getPrice));
        System.out.println("[ShopSystem] Inventory built: " + shopItems.size() + " items.");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Item> getShopItems() {
        return Collections.unmodifiableList(shopItems);
    }

    public Item getItem(int index) {
        if (index < 0 || index >= shopItems.size()) return null;
        return shopItems.get(index);
    }

    public int getItemCount() {
        return shopItems.size();
    }

    /**
     * Attempts to purchase the item at the given index for the given player.
     * Fires quest hooks on success.
     * Returns a PurchaseResult — ShopUI decides what message to show.
     */
    public PurchaseResult purchase(int index, Player player) {
        Item item = getItem(index);
        if (item == null) return PurchaseResult.NOT_ENOUGH_COINS;

        if (player.getRotCoins() < item.getPrice())
            return PurchaseResult.NOT_ENOUGH_COINS;

        if (!player.getInventory().addItem(item))
            return PurchaseResult.INVENTORY_FULL;

        player.spendRotCoins(item.getPrice());
        QuestSystem.getInstance().onCoinsSpent(item.getPrice());
        QuestSystem.getInstance().onShopPurchase();

        System.out.println("[ShopSystem] Purchased: " + item.getName()
                + " | Remaining coins: " + player.getRotCoins());

        return PurchaseResult.SUCCESS;
    }
}