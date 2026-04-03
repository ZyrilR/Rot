package npc;

import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.Tier;
import engine.GamePanel;

import java.util.ArrayList;
import java.util.List;

public class TrainerNPC extends NPC {

    private final List<BrainRot> party = new ArrayList<>();
    private boolean defeated = false;

    public TrainerNPC(String name, int folderId, int x, int y) {
        super(name, folderId, x, y);
        // Give each trainer a default lead BrainRot at NORMAL tier.
        // Replace with a configured team when setting up specific trainers.
        party.add(BrainRotFactory.create("LIRILI LARILA", Tier.NORMAL));
    }

    /** Returns the first non-fainted BrainRot in this trainer's party. */
    public BrainRot getLeadBrainRot() {
        for (BrainRot rot : party) {
            if (!rot.isFainted()) return rot;
        }
        // Fallback: give a fresh BrainRot so the battle can still start
        BrainRot fallback = BrainRotFactory.create("LIRILI LARILA", Tier.NORMAL);
        party.add(fallback);
        return fallback;
    }

    public void addToParty(BrainRot rot) { party.add(rot); }
    public List<BrainRot> getParty()     { return party; }
    public boolean hasBeenDefeated()     { return defeated; }
    public void setDefeated(boolean b)   { defeated = b; }

    /**
     * Called when the player presses the action key while facing this trainer.
     * Delegates immediately to the EncounterSystem so the battle starts right away.
     */
    @Override
    public void interact(GamePanel gp) {
        if (defeated) {
            System.out.println(name + ": You're strong… but I'll come back tougher!");
            return;
        }
        facePlayer(gp.player);
        System.out.println(name + " locked eyes with you! Time to battle!");
        gp.encounterSystem.startTrainerBattle(gp.player, this, gp);
    }
}