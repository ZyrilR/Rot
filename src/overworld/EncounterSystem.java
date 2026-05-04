package overworld;

import battle.BattleManager;
import brainrots.BrainRot;
import brainrots.BrainRotFactory;
import engine.GamePanel;
import npc.NPC;
import npc.TrainerNPC;
import tile.TileManager;
import utils.Directories;
import java.util.List;
import static utils.Constants.*;

public class EncounterSystem {

    // ── Terrain Enum for cleaner logic ────────────────────────────────────────
    private enum TerrainType {
        CAVE, TALL_GRASS, DARK_TALL_GRASS
    }

    // ── Encounter Rates ───────────────────────────────────────────────────────
    private static final double ENCOUNTER_RATE_TALL_GRASS = 0.06;
    private static final double ENCOUNTER_RATE_DARK_GRASS = 0.02;
    private static final double ENCOUNTER_RATE_CAVE       = 0.03;

    // ── Route pool — all types ────────────────────────────────────────────────
    private static final String[] WILD_BRAINROT_NAMES = {
            "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
            "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
            "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"
    };

    /**
     * CAVE ENEMIES
     *   TUNG TUNG TUNG SAHUR  → FIGHTING
     *   UDIN DIN DIN DIN DUN  → FIGHTING
     *   BRR BRR PATAPIM       → GRASS / ROCK  (ROCK secondary)
     *   BONECA AMBALABU       → FIRE  / ROCK  (ROCK secondary)
     *   CAPUCCINO ASSASSINO   → DARK  / POISON (DARK primary)
     */
    private static final String[] CAVE_BRAINROT_NAMES = {
            "TUNG TUNG TUNG SAHUR",
            "UDIN DIN DIN DIN DUN",
            "BRR BRR PATAPIM",
            "BONECA AMBALABU",
            "CAPUCCINO ASSASSINO"
    };

    // Tile IDs
    private static final int TALL_GRASS_TILE = 2;       // Routes — decoration layer
    private static final int CAVE_FLOOR_TILE = 84;      // Cave   — background layer
    private static final int DARK_TALL_GRASS_TILE = 16; // Routes - decoration layer

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

        // Boundary safety check
        if (gridY < 0 || gridY >= MAX_WORLD_ROW || gridX < 0 || gridX >= MAX_WORLD_COL) {
            return;
        }

        boolean inCave = gp.CURRENT_PATH.toLowerCase().contains("cave");

        // 1. Check Decoration Layer FIRST (for Normal Grass and Dark Grass)
        for (TileManager decoLayer : gp.world.getDecorationLayer()) {
            if (decoLayer == null) continue;

            int tileNum = decoLayer.getMap()[gridY][gridX];
            if (tileNum == TALL_GRASS_TILE) {
                triggerEncounter(player, gp, TerrainType.TALL_GRASS);
                return;
            } else if (tileNum == DARK_TALL_GRASS_TILE) {
                triggerEncounter(player, gp, TerrainType.DARK_TALL_GRASS);
                return;
            }
        }

        // 2. Check Background Layer SECOND (for Cave Floor) ONLY if in a cave
        if (inCave) {
            for (TileManager bgLayer : gp.world.getBackgroundLayer()) {
                if (bgLayer == null) continue;

                int tileNum = bgLayer.getMap()[gridY][gridX];
                if (tileNum == CAVE_FLOOR_TILE) {
                    triggerEncounter(player, gp, TerrainType.CAVE);
                    return;
                }
            }
        }
    }

    private void triggerEncounter(Player player, GamePanel gp, TerrainType terrain) {
        // Get the specific rate for the terrain we are standing on
        double encounterRate = switch (terrain) {
            case TALL_GRASS -> ENCOUNTER_RATE_TALL_GRASS;
            case DARK_TALL_GRASS -> ENCOUNTER_RATE_DARK_GRASS;
            case CAVE -> ENCOUNTER_RATE_CAVE;
        };

        // Use your original Math.random logic block
        if (Math.random() < encounterRate) {
            BrainRot leader = getLeadBrainRot(gp);
            if (leader == null) return;

            Directories currentMap = Directories.getByPath(gp.CURRENT_PATH);
            int mapMin = currentMap.getMinLevel();
            int mapMax = currentMap.getMaxLevel();
            int pLvl   = leader.getLevel();

            int finalMax = Math.min(pLvl, mapMax);
            finalMax     = Math.max(finalMax, mapMin);
            int finalMin = Math.max(mapMin, finalMax - 2);

            int wildLevel = utils.RandomUtil.range(finalMin, finalMax);
            BrainRot wild = spawnRandomWildBrainRot(wildLevel, terrain);
            startWildBattle(player, wild, gp);
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
        System.out.println("[EncounterSystem] Wild battle: "
                + wildRot.getName() + " Lv." + wildRot.getLevel());
    }

    public void startTrainerBattle(Player player, TrainerNPC trainer, GamePanel gp) {
        BrainRot playerRot = getLeadBrainRot(gp);
        if (playerRot == null) return;

        activeBattle = new BattleManager(
                playerRot, trainer.getLeadBrainRot(),
                gp.player.getPCSYSTEM().getParty(),
                player,
                false);
        System.out.println("[EncounterSystem] Trainer battle: " + trainer.name);
    }

    public void clearBattle() {
        activeBattle = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BrainRot spawnRandomWildBrainRot(int level, TerrainType terrain) {
        // Normal pool for normal grass, Cave pool for BOTH Cave and Dark Grass
        String[] pool = (terrain == TerrainType.TALL_GRASS) ? WILD_BRAINROT_NAMES : CAVE_BRAINROT_NAMES;
        int index = (int)(Math.random() * pool.length);

        BrainRot wild = BrainRotFactory.createEnemy(pool[index], level);

        if (level > 1) {
            for (int i = 1; i < level; i++) {
                wild.gainXp(wild.getXpToNextLevel());
            }
        }
        wild.restoreForBattle();

        // Apply buffs based on terrain
        if (terrain == TerrainType.CAVE) {
            wild.modifyAttack(0.20);
            wild.modifyDefense(0.20);
            System.out.println("[EncounterSystem] Cave buff applied to " + wild.getName());
        } else if (terrain == TerrainType.DARK_TALL_GRASS) {
            wild.modifyAttack(0.10);
            wild.modifyDefense(0.10);
            System.out.println("[EncounterSystem] Dark Grass buff applied to " + wild.getName());
        }

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
            if (isInLineOfSight(
                    npc.direction,
                    player.worldX / TILE_SIZE, player.worldY / TILE_SIZE,
                    npc.worldX   / TILE_SIZE, npc.worldY   / TILE_SIZE)) {
                if (getLeadBrainRot(gp) != null) {
                    startTrainerBattle(player, trainer, gp);
                }
                return;
            }
        }
    }

    private boolean isInLineOfSight(String dir, int px, int py, int tx, int ty) {
        return switch (dir) {
            case "up"    -> px == tx && py <  ty && py >= ty - TRAINER_SIGHT_RANGE;
            case "down"  -> px == tx && py >  ty && py <= ty + TRAINER_SIGHT_RANGE;
            case "left"  -> py == ty && px <  tx && px >= tx - TRAINER_SIGHT_RANGE;
            case "right" -> py == ty && px >  tx && px <= tx + TRAINER_SIGHT_RANGE;
            default      -> false;
        };
    }
}