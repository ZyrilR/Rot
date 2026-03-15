package items;

public abstract class Item {
    protected String name;
    protected String description;
    protected String assetPath; // For UI reference
    //assetpath should not be a variable
    //protected BufferredImage img = loadImage(assetPath);

    public Item(String name, String description, String assetPath) {
        this.name = name;
        this.description = description;
        this.assetPath = assetPath;
    }

    // Abstract method for using the item
    public abstract void use();

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAssetPath() {
        return assetPath;
    }
}