package items;

public class Antidote extends Item {
    private String statusToCure; // Example: "Confusion", "Paralysis",

    public Antidote(String name, String description, String assetPath, String statusToCure) {
        super(name, description, assetPath);
        this.statusToCure = statusToCure;
    }

    @Override
    public void use() {
        //TODO: Implement logic to remove the specific status effect from a BrainRot
        // Example: if(brainRot.hasStatus(statusToCure)) { brainRot.removeStatus(statusToCure); }
        System.out.println("Using " + name + " to cure " + statusToCure + ".");
    }

    public String getStatusToCure() {
        return statusToCure;
    }
}