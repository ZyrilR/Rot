package npc;

import brainrots.BrainRot;
import engine.GamePanel;
import items.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainerNPC extends NPC {

    /** 3 minutes of game time at 60 fps. */
    public static final long COOLDOWN_TICKS = 60L * 60L * 3L;

    private ArrayList<BrainRot> party = new ArrayList<>();
    private boolean defeated = false;
    private long defeatedAtGameTime = -1;
    private int rotCoins;

    // --- NEW DIALOGUE VARIABLES ---
    private ArrayList<String> preBattleDialogues = new ArrayList<>();
    private ArrayList<String> postBattleDialogues = new ArrayList<>();
    private boolean dialoguesProcessed = false;
    public boolean wantsToTalkAfterBattle = false;
    public int postBattleTalkDelay = 0;

    public TrainerNPC(String name, int folderId, int x, int y) {
        super(name, folderId, x, y);
        initDefaultDialogues();
    }

    public TrainerNPC(String name, int folderId, int x, int y, Inventory inventory, ArrayList<BrainRot> party, int rotCoins) {
        super(name, folderId, x, y, inventory);
        this.party = party;
        this.rotCoins = rotCoins;
        initDefaultDialogues();
    }

    private void initDefaultDialogues() {
        preBattleDialogues.add("Let's battle!");
        postBattleDialogues.add("You're strong... but I'll come back tougher!");
    }

    /** * This checks the inherited 'dialogues' array from NPC.java.
     * If it finds the [DEFEAT] tag, it automatically splits the text
     * into pre-battle and post-battle arrays!
     */
    private void processDialoguesIfNeeded() {
        if (dialoguesProcessed) return;
        dialoguesProcessed = true;

        if (this.dialogues != null && !this.dialogues.isEmpty()) {
            // Re-join the text in case the map loader already split it by ';'
            String fullText = String.join(";", this.dialogues);

            preBattleDialogues.clear();
            postBattleDialogues.clear();

            if (fullText.contains("[DEFEAT]")) {
                // Slice the string exactly at the tag
                String[] parts = fullText.split("\\[DEFEAT\\]");

                // Add the first half to Pre-Battle
                preBattleDialogues.addAll(Arrays.asList(parts[0].split(";")));

                // Add the second half to Post-Battle
                if (parts.length > 1) {
                    postBattleDialogues.addAll(Arrays.asList(parts[1].split(";")));
                } else {
                    postBattleDialogues.add("...");
                }
            } else {
                // If no [DEFEAT] tag exists, treat it normally
                preBattleDialogues.addAll(Arrays.asList(fullText.split(";")));
                postBattleDialogues.add("You're strong... but I'll come back tougher!");
            }
        }
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
    public int  getRotCoins()            { return rotCoins; }
    public void clearRotCoins()          { rotCoins = 0; }
    public Inventory getInventory()      { return inventory; }
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

        if (wantsToTalkAfterBattle) {
            // ONLY start counting if the engine has officially returned to "play" mode!
            // This guarantees the dialogue won't get crushed by the fade effect.
            if (gp.GAMESTATE.equalsIgnoreCase("play")) {
                postBattleTalkDelay--;
                if (postBattleTalkDelay <= 0) {
                    wantsToTalkAfterBattle = false;
                    gp.GAMESTATE = "dialogue"; // Hijack the state
                    playDefeatDialogue(gp);    // Pop the text!
                }
            }
        }
    }

    @Override
    public void interact(GamePanel gp) {
        processDialoguesIfNeeded(); // Ensure dialogues are split before speaking!

        if (defeated) {
            // Show the crying/post-battle dialogue if we talk to them later!
            gp.DIALOGUEBOX.startDialogue(name, postBattleDialogues);
            return;
        }
        facePlayer(gp.player);
        startEncounter(gp);
    }

    /** Plays the trainer's dialogue then kicks off the battle when it ends. */
    public void startEncounter(GamePanel gp) {
        processDialoguesIfNeeded(); // Ensure dialogues are split before speaking!

        // Show pre-battle dialogue and trigger the fight
        gp.DIALOGUEBOX.startDialogue(name, preBattleDialogues);
        gp.DIALOGUEBOX.setOnFinish(() ->
                gp.encounterSystem.startTrainerBattle(gp.player, this, gp));
    }

    /** Forces the post-battle crying dialogue to trigger automatically. */
    public void playDefeatDialogue(GamePanel gp) {
        processDialoguesIfNeeded();

        // CRITICAL: We pass an empty action so the game DOES NOT try to
        // start the battle all over again when the dialogue closes!
        gp.DIALOGUEBOX.setOnFinish(() -> {});

        gp.DIALOGUEBOX.startDialogue(name, postBattleDialogues);
    }
}