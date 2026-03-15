package items;

import brainrots.BrainRot;

public abstract class Item {
    protected final String name;
    protected final String description;
    protected final String assetPath; // for UI reference

    public Item(String name, String description, String assetPath) {
        this.name = name;
        this.description = description;
        this.assetPath = assetPath;
    }

    /** Use the item on a target BrainRot, optionally receiving extra arguments */
    public abstract void use(BrainRot target, Object... extraArgs);

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAssetPath() { return assetPath; }
}
