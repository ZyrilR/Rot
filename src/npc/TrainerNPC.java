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

    /** Trainers face the direction stored in their sprite frame index (0=down, 1=right, 2=left, 3=up). */
    public String getFacingDirection() {
        // Use the last direction the trainer was set to face (defaults from spriteNum on spawn).
        if (direction != null && !direction.isEmpty()) return direction;
        return switch (spriteNum) {
            case 1 -> "right";
            case 2 -> "left";
            case 3 -> "up";
            default -> "down";
        };
    }

    /** Trainers stand still and just animate their idle frame. */
    @Override
    public void update(GamePanel gp) {
        if (direction == null || direction.isEmpty()) {
            direction = switch (spriteNum) {
                case 1 -> "right";
                case 2 -> "left";
                case 3 -> "up";
                default -> "down";
            };
        }
        // Lock the visible sprite to the directional idle frame.
        spriteNum = switch (direction) {
            case "right" -> 1;
            case "left"  -> 2;
            case "up"    -> 3;
            default      -> 0;
        };
    }

    @Override
    public void interact(GamePanel gp) {
        if (defeated) {
            gp.DIALOGUEBOX.startDialogue(name,
                    new ArrayList<>(java.util.List.of("You're strong... but I'll come back tougher!")));
            return;
        }
        facePlayer(gp.player);
        startEncounter(gp);
    }

    /** Plays the trainer's dialogue then kicks off the battle when it ends. */
    public void startEncounter(GamePanel gp) {
        ArrayList<String> lines = (dialogues == null || dialogues.isEmpty())
                ? new ArrayList<>(java.util.List.of("Let's battle!"))
                : new ArrayList<>(dialogues);
        gp.DIALOGUEBOX.startDialogue(name, lines);
        gp.DIALOGUEBOX.setOnFinish(() ->
                gp.encounterSystem.startTrainerBattle(gp.player, this, gp));
    }
}
