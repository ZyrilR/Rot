package items;

public class Scroll extends Item {
    private String skillName;

    public Scroll(String name, String description, String assetPath, String skillName) {
        super(name, description, assetPath);
        this.skillName = skillName;
    }

    @Override
    public void use() {
        //TODO : ambot saon ni paggamit ang scroll
    }

    public String getSkillName() { return skillName; }
}