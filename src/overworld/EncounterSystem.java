package overworld;

import battle.BattleManager;
import brainrots.BrainRot;
import engine.GamePanel;
import items.Inventory;
import npc.NPC;
import npc.TrainerNPC;
import tile.TileManager;
import java.util.List;
import static utils.Constants.*;

public class EncounterSystem {

    private static final String[] WILD_BRAINROT_NAMES = {
            "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
            "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
            "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"
    };

    private static final int TRAINER_SIGHT_RANGE = 5;
    private BattleManager activeBattle;

    // ── Team Management ───────────────────────────────────────────────────────

    // FIX: Get team directly from the Player's PC System
    public List<BrainRot> getPlayerTeam(GamePanel gp) {
        return gp.player.getPCSYSTEM().getParty();
    }

    public BattleManager getActiveBattle() {
        return activeBattle;
    }

    // ── Wild Encounters ───────────────────────────────────────────────────────

    public void checkWildEncounter(Player player, GamePanel gp) {
        // SAFETY: Don't trigger if already in a battle
        if (activeBattle != null) return;

        int gridX = player.worldX / TILE_SIZE;
        int gridY = player.worldY / TILE_SIZE;

        for (TileManager decoLayer : gp.world.getDecorationLayer()) {
            if (gridY >= 0 && gridY < MAX_WORLD_ROW && gridX >= 0 && gridX < MAX_WORLD_COL) {
                int tileNum = decoLayer.getMap()[gridY][gridX];

                if (tileNum == 2) { // Tall Grass
                    if (Math.random() < 0.10) {
                        // Check for healthy team BEFORE starting
                        BrainRot leader = getLeadBrainRot(gp);
                        if (leader == null) {
                            System.out.println("No healthy BrainRots! Stay safe!");
                            return;
                        }

                        BrainRot wildRot = spawnRandomWildBrainRot();
                        startWildBattle(player, wildRot, gp);
                    }
                    break;
                }
            }
        }
    }

    // ── Battle Triggers ───────────────────────────────────────────────────────

    public void startWildBattle(Player player, BrainRot wildRot, GamePanel gp) {
        BrainRot playerRot = getLeadBrainRot(gp);
        if (playerRot == null) return;
        activeBattle = new BattleManager(
                playerRot, wildRot,
                gp.player.getPCSYSTEM().getParty(),
                player,
                true);
        System.out.println("Wild battle started against " + wildRot.getName() + "!");
    }

    public void startTrainerBattle(Player player, TrainerNPC trainer, GamePanel gp) {
        BrainRot playerRot = getLeadBrainRot(gp);
        if (playerRot == null) return;
        BrainRot trainerRot = trainer.getLeadBrainRot();
        activeBattle = new BattleManager(
                playerRot, trainerRot,
                gp.player.getPCSYSTEM().getParty(),
                player,
                false);
        System.out.println("Trainer battle started against " + trainer.name + "!");
    }

    public void clearBattle() {
        activeBattle = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BrainRot spawnRandomWildBrainRot() {
        int index = (int)(Math.random() * WILD_BRAINROT_NAMES.length);
        return brainrots.BrainRotFactory.create(WILD_BRAINROT_NAMES[index], brainrots.Tier.NORMAL);
    }

    // FIX: Check the Player's PCSystem party
    private BrainRot getLeadBrainRot(GamePanel gp) {
        List<BrainRot> party = gp.player.getPCSYSTEM().getParty();
        for (BrainRot rot : party) {
            if (rot != null && !rot.isFainted()) return rot;
        }
        return null;
    }

    // Trainer Look logic remains the same, just ensure it uses getLeadBrainRot(gp)
    public void checkTrainerLook(Player player, List<NPC> npcs, GamePanel gp) {
        for (NPC npc : npcs) {
            if (!(npc instanceof TrainerNPC trainer) || trainer.hasBeenDefeated()) continue;
            if (isInLineOfSight(npc.direction, player.worldX/TILE_SIZE, player.worldY/TILE_SIZE, npc.worldX/TILE_SIZE, npc.worldY/TILE_SIZE)) {
                if (getLeadBrainRot(gp) != null) {
                    startTrainerBattle(player, trainer, gp);
                }
                return;
            }
        }
    }

    private boolean isInLineOfSight(String dir, int px, int py, int tx, int ty) {
        return switch (dir) {
            case "up"    -> px == tx && py < ty && py >= ty - TRAINER_SIGHT_RANGE;
            case "down"  -> px == tx && py > ty && py <= ty + TRAINER_SIGHT_RANGE;
            case "left"  -> py == ty && px < tx && px >= tx - TRAINER_SIGHT_RANGE;
            case "right" -> py == ty && px > tx && px <= tx + TRAINER_SIGHT_RANGE;
            default      -> false;
        };
    }
}