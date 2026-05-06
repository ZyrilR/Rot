package npc;

import brainrots.BrainRot;
import engine.GamePanel;
import items.Inventory;

import java.util.ArrayList;
import java.util.List;

public class TrainerNPC extends NPC {

    /** 3 minutes of game time at 60 fps. */
    public static final long COOLDOWN_TICKS = 60L * 60L * 3L;

    private ArrayList<BrainRot> party = new ArrayList<>();
    private boolean defeated = false;
    private long defeatedAtGameTime = -1;
    private int rotCoins;

    public TrainerNPC(String name, int folderId, int x, int y) {
        super(name, folderId, x, y);
    }
    public TrainerNPC(String name, int folderId, int x, int y, Inventory inventory, ArrayList<BrainRot> party, int rotCoins) {
        super(name, folderId, x, y, inventory);
        this.party = party;
        this.rotCoins = rotCoins;
    }

    /** Returns the first non-fainted BrainRot in this trainer's party. */
    public BrainRot getLeadBrainRot() {
        for (BrainRot rot : party) {
            if (!rot.isFainted()) return rot;
        }
        return null;
    }

    /** Returns the first non-fainted BrainRot that isn't the one currently sent out. */
    public BrainRot getNextBrainRot(BrainRot current) {
        for (BrainRot rot : party) {
            if (rot != null && rot != current && !rot.isFainted()) return rot;
        }
        return null;
    }

    public boolean hasMoreFightableThan(BrainRot current) {
        return getNextBrainRot(current) != null;
    }

    public void addToParty(BrainRot rot) { party.add(rot); }
    public List<BrainRot> getParty()     { return party; }
    public boolean hasBeenDefeated()     { return defeated; }
    public void setDefeated(boolean b)   { defeated = b; if (!b) defeatedAtGameTime = -1; }

    public void markDefeated(long gameTime) {
        this.defeated = true;
        this.defeatedAtGameTime = gameTime;
    }
    public long getDefeatedAtGameTime() { return defeatedAtGameTime; }
    public void restoreDefeatedState(long defeatedAtGameTime) {
        this.defeated = true;
        this.defeatedAtGameTime = defeatedAtGameTime;
    }
    public boolean isCooldownExpired(long currentGameTime) {
        return defeatedAtGameTime >= 0 && currentGameTime - defeatedAtGameTime >= COOLDOWN_TICKS;
    }

    /**
     * Direction derived from the trainer's current sprite frame.
     * Image mapping: 1=down, 2=right, 3=left, 4=up, 5=down (spriteNum is 0-indexed).
     */
    public String getFacingDirection() {
        return switch (spriteNum) {
            case 0 -> "down";
            case 1 -> "right";
            case 2 -> "left";
            case 3 -> "up";
            case 4 -> "down";
            default -> "down";
        };
    }

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
