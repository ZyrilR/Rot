package brainrots;

import skills.Skill;

/**
 * Returned by BrainRot.gainXp() for each level gained.
 * skillUnlocked is null if no skill is offered at this level.
 */
public class LevelUpResult {

    public final int newLevel;
    public final int hpGain;
    public final int atkGain;
    public final int defGain;
    public final int spdGain;
    public final Skill skillUnlocked; // null = no new skill this level

    public LevelUpResult(int newLevel, int hpGain, int atkGain, int defGain, int spdGain, Skill skillUnlocked) {
        this.newLevel     = newLevel;
        this.hpGain       = hpGain;
        this.atkGain      = atkGain;
        this.defGain      = defGain;
        this.spdGain      = spdGain;
        this.skillUnlocked = skillUnlocked;
    }
}
