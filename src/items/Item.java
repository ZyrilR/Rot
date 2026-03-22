package items;

import brainrots.BrainRot;

public abstract class Item {
    protected final String name;
    protected final String description;
    protected final String assetPath; // for UI reference
    protected final int price;        // price in gold or currency

    public Item(String name, String description, String assetPath, int price) {
        this.name = name;
        this.description = description;
        this.assetPath = assetPath;
        this.price = price;
    }

    /** Use the item on a target BrainRot, optionally receiving extra arguments */
    public abstract void use(BrainRot target, Object... extraArgs);

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAssetPath() { return assetPath; }

    public int getPrice() {
        return price;
    }
}
