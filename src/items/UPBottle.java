package items;

import brainrots.BrainRot;
import skills.Skill;

public class UPBottle extends Item {

    public enum RestoreMode { FIXED, FULL }

    private final RestoreMode mode;
    private final int         restoreAmount; // ignored when mode == FULL

    public UPBottle(String name, String description, String assetPath,
                    RestoreMode mode, int restoreAmount, int price) {
        super(name, description, assetPath, price);
        this.mode          = mode;
        this.restoreAmount = restoreAmount;
    }

    @Override
    public void use(BrainRot target, Object... extraArgs) {
        // extraArgs[0] = Integer skillIndex
        if (extraArgs.length < 1 || !(extraArgs[0] instanceof Integer idx)) {
            System.out.println("UPBottle requires a skill index!");
            return;
        }
        if (idx < 0 || idx >= target.getMoves().size()) {
            System.out.println("Invalid skill index: " + idx);
            return;
        }
        Skill skill = target.getMoves().get(idx);
        if (mode == RestoreMode.FULL) {
            skill.restoreUP();
        } else {
            int restored = Math.min(restoreAmount, skill.getMaxUP() - skill.getCurrentUP());
            skill.setCurrentUP(skill.getCurrentUP() + restored);
        }
        System.out.println(target.getName() + "'s " + skill.getName()
                + " UP restored! (" + skill.getCurrentUP() + "/" + skill.getMaxUP() + ")");
    }

    public RestoreMode getMode()          { return mode; }
    public int         getRestoreAmount() { return restoreAmount; }

    /** Returns true if the given skill already has full UP. */
    public boolean isSkillFull(Skill skill) {
        return skill.getCurrentUP() >= skill.getMaxUP();
    }
}