package items;

import brainrots.BrainRot;
import skills.Skill;
import skills.SkillRegistry;

public class Scroll extends Item {
    private String skillName;

    public Scroll(String name, String description, String assetPath, String skillName) {
        super(name, description, assetPath);
        this.skillName = skillName;
    }

    @Override
    public void use(BrainRot target, Object... extraArgs) { // ambot kung sakto ni HAHAHAHAH
        Skill skill = SkillRegistry.get(skillName);

        if (skill == null) {
            System.out.println("Skill does not exist.");
            return;
        }

        if (target.addMove(skill)) {
            System.out.println(target.getName() + " learned " + skill.getName() + "!");
        }
    }

    public String getSkillName() { return skillName; }
}