package items;

public class Capsule extends Item {

    public Capsule(String name, String description, String assetPath) {
        super(name, description, assetPath);
    }

    @Override
    public void use() {
        // empty, well be utilizing isCaught()
    }

    public boolean isCaught() { // needs a Rot parameter
        //TODO: Implement logic to attempt capturing a BrainRot
        // utilize RandomUtil.chance(int)
        // needs Rot getters
        return false;
    }

}