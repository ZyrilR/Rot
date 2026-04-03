package overworld;

import battle.BattleManager;
import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import brainrots.Tier;
import engine.GamePanel;
import items.Inventory;
import npc.NPC;
import npc.TrainerNPC;
import tile.TileManager;

import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

/**
 * Bridges overworld exploration and the turn-based battle system.
 *
 * Responsibilities:
 *   - Wild grass encounters (10% chance on tall-grass tile ID 2)
 *   - Trainer line-of-sight detection (5 tiles in the trainer's facing direction)
 *   - Constructing and activating a BattleManager for both encounter types
 *   - Managing the player's BrainRot party
 */
public class EncounterSystem {

    private static final String[] WILD_BRAINROT_NAMES = {
        "TUNG TUNG TUNG SAHUR",
        "TRALALERO TRALALA",
        "BOMBARDINO CROCODILO",
        "LIRILI LARILA",
        "BRR BRR PATAPIM",
        "BONECA AMBALABU",
        "UDIN DIN DIN DIN DUN",
        "CAPUCCINO ASSASSINO"
    };

    private static final int TRAINER_SIGHT_RANGE = 5;

    /** The player's party of BrainRots (persists across battles). */
    private final List<BrainRot> playerTeam = new ArrayList<>();

    /** The currently active battle, or null when no battle is in progress. */
    private BattleManager activeBattle;

    // ── Team Management ───────────────────────────────────────────────────────

    public void addToPlayerTeam(BrainRot rot) {
        playerTeam.add(rot);
    }

    public List<BrainRot> getPlayerTeam() {
        return playerTeam;
    }

    public BattleManager getActiveBattle() {
        return activeBattle;
    }

    // ── Wild Encounters ───────────────────────────────────────────────────────

    /**
     * Checks whether the player is standing on tall grass (decoration tile ID 2)
     * and rolls a 10% chance for a wild BrainRot encounter.
     *
     * Call this once per completed tile-step in the PLAY state.
     * Mirrors the logic previously stubbed in Player.checkGrass().
     */
    public void checkWildEncounter(Player player, GamePanel gp) {
        int gridX = player.worldX / TILE_SIZE;
        int gridY = player.worldY / TILE_SIZE;

        for (TileManager decoLayer : gp.world.getDecorationLayer()) {
            if (gridY >= 0 && gridY < MAX_WORLD_ROW && gridX >= 0 && gridX < MAX_WORLD_COL) {
                int tileNum = decoLayer.getMap()[gridY][gridX];

                if (tileNum == 2) { // Tile ID 2 = Tall Grass
                    if (Math.random() < 0.10) {
                        BrainRot wildRot = spawnRandomWildBrainRot();
                        System.out.println("A wild " + wildRot.getName() + " appeared!");
                        startWildBattle(player, wildRot, gp);
                    }
                    break;
                }
            }
        }
    }

    // ── Trainer Line of Sight ─────────────────────────────────────────────────

    /**
     * Iterates all NPCs and checks whether the player has stepped into a
     * TrainerNPC's 5-tile directional line of sight. Triggers a trainer battle
     * on the first match.
     *
     * Call this every update tick in the PLAY state (after player.update()).
     *
     * @param player the player entity
     * @param npcs   the full list of active NPCs in the current world
     * @param gp     the GamePanel (used to change game state)
     */
    public void checkTrainerLook(Player player, List<NPC> npcs, GamePanel gp) {
        int playerGridX = player.worldX / TILE_SIZE;
        int playerGridY = player.worldY / TILE_SIZE;

        for (NPC npc : npcs) {
            // Polymorphism: only TrainerNPCs have a line-of-sight mechanic
            if (!(npc instanceof TrainerNPC trainer)) continue;
            if (trainer.hasBeenDefeated()) continue;

            int trainerGridX = npc.worldX / TILE_SIZE;
            int trainerGridY = npc.worldY / TILE_SIZE;

            if (isInLineOfSight(npc.direction, playerGridX, playerGridY,
                                trainerGridX, trainerGridY)) {
                System.out.println(npc.name + " spotted you!");
                startTrainerBattle(player, trainer, gp);
                return; // one battle at a time
            }
        }
    }

    /**
     * Returns true if the player's grid position falls within the trainer's
     * TRAINER_SIGHT_RANGE-tile cone in the direction the trainer is facing.
     */
    private boolean isInLineOfSight(String direction,
                                    int playerGridX, int playerGridY,
                                    int trainerGridX, int trainerGridY) {
        return switch (direction) {
            case "up"    -> playerGridX == trainerGridX
                         && playerGridY <  trainerGridY
                         && playerGridY >= trainerGridY - TRAINER_SIGHT_RANGE;
            case "down"  -> playerGridX == trainerGridX
                         && playerGridY >  trainerGridY
                         && playerGridY <= trainerGridY + TRAINER_SIGHT_RANGE;
            case "left"  -> playerGridY == trainerGridY
                         && playerGridX <  trainerGridX
                         && playerGridX >= trainerGridX - TRAINER_SIGHT_RANGE;
            case "right" -> playerGridY == trainerGridY
                         && playerGridX >  trainerGridX
                         && playerGridX <= trainerGridX + TRAINER_SIGHT_RANGE;
            default      -> false;
        };
    }

    // ── Battle Triggers ───────────────────────────────────────────────────────

    /**
     * Starts a wild BrainRot battle (capture is allowed).
     * Sets gp.GAMESTATE to "battle" and initializes the BattleManager.
     */
    public void startWildBattle(Player player, BrainRot wildRot, GamePanel gp) {
        BrainRot playerRot = getLeadBrainRot();
        Inventory inventory  = player.getInventory();

        activeBattle = new BattleManager(playerRot, wildRot, playerTeam, inventory, true);
        gp.GAMESTATE = "battle";
        System.out.println("Wild battle started against " + wildRot.getName() + "!");
    }

    /**
     * Starts a trainer battle (capture is NOT allowed).
     * Called both from line-of-sight detection and from TrainerNPC.interact().
     */
    public void startTrainerBattle(Player player, TrainerNPC trainer, GamePanel gp) {
        BrainRot trainerRot = trainer.getLeadBrainRot();
        BrainRot playerRot  = getLeadBrainRot();
        Inventory inventory   = player.getInventory();

        activeBattle = new BattleManager(playerRot, trainerRot, playerTeam, inventory, false);
        gp.GAMESTATE = "battle";
        System.out.println("Trainer battle started against " + trainer.name + "!");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Picks a random wild BrainRot at NORMAL tier. */
    private BrainRot spawnRandomWildBrainRot() {
        int index = (int)(Math.random() * WILD_BRAINROT_NAMES.length);
        return BrainRotFactory.create(WILD_BRAINROT_NAMES[index], Tier.NORMAL);
    }

    /**
     * Returns the player's first non-fainted BrainRot.
     * If the party is empty or everyone has fainted, creates a default starter
     * and adds it to the team so the battle can still initialize.
     */
    private BrainRot getLeadBrainRot() {
        for (BrainRot rot : playerTeam) {
            if (!rot.isFainted()) return rot;
        }
        BrainRot starter = BrainRotFactory.create("TUNG TUNG TUNG SAHUR", Tier.NORMAL);
        playerTeam.add(starter);
        System.out.println("[EncounterSystem] No healthy BrainRot found – assigned default starter.");
        return starter;
    }
}
