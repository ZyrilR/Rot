package overworld;

import battle.BattleManager;
import brainrots.BrainRot;
import engine.GamePanel;
import npc.NPC;
import npc.TrainerNPC;
import tile.TileManager;
import utils.Directories;
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

    public List<BrainRot> getPlayerTeam(GamePanel gp) {
        return gp.player.getPCSYSTEM().getParty();
    }

    public BattleManager getActiveBattle() {
        return activeBattle;
    }

    // ── Wild Encounters ───────────────────────────────────────────────────────

    public void checkWildEncounter(Player player, GamePanel gp) {
        if (activeBattle != null) return;

        int gridX = player.worldX / TILE_SIZE;
        int gridY = player.worldY / TILE_SIZE;

        for (TileManager decoLayer : gp.world.getDecorationLayer()) {
            if (gridY >= 0 && gridY < MAX_WORLD_ROW && gridX >= 0 && gridX < MAX_WORLD_COL) {
                int tileNum = decoLayer.getMap()[gridY][gridX];

                if (tileNum == 2) { // Tall Grass
                    if (Math.random() < 0.12) { // 12% chance to encounter
                        BrainRot leader = getLeadBrainRot(gp);
                        if (leader == null) return;

                        // 1. Get Map Settings via Enum
                        Directories currentMap = Directories.getByPath(gp.CURRENT_PATH);

                        // 2. Use the new Min and Max Range!
                        int minLvl = currentMap.getMinLevel();
                        int maxLvl = currentMap.getMaxLevel();

                        // 3. Roll random level between min and max
                        int wildLevel = utils.RandomUtil.range(minLvl, maxLvl);

                        startWildBattle(player, spawnRandomWildBrainRot(wildLevel), gp);
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
        System.out.println("Wild battle: " + wildRot.getName() + " Lv." + wildRot.getLevel());
    }

    public void startTrainerBattle(Player player, TrainerNPC trainer, GamePanel gp) {
        BrainRot playerRot = getLeadBrainRot(gp);
        if (playerRot == null) return;

        activeBattle = new BattleManager(
                playerRot, trainer.getLeadBrainRot(),
                gp.player.getPCSYSTEM().getParty(),
                player,
                false);
        System.out.println("Trainer battle: " + trainer.name);
    }

    public void clearBattle() {
        activeBattle = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BrainRot spawnRandomWildBrainRot(int level) {
        int index = (int)(Math.random() * WILD_BRAINROT_NAMES.length);
        BrainRot wild = brainrots.BrainRotFactory.create(WILD_BRAINROT_NAMES[index], brainrots.Tier.NORMAL);

        // Simulated XP gain to reach the dynamic level
        if (level > 1) {
            for (int i = 1; i < level; i++) {
                wild.gainXp(wild.getXpToNextLevel());
            }
        }
        wild.restoreForBattle();
        return wild;
    }

    private BrainRot getLeadBrainRot(GamePanel gp) {
        for (BrainRot rot : gp.player.getPCSYSTEM().getParty()) {
            if (rot != null && !rot.isFainted()) return rot;
        }
        return null;
    }

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