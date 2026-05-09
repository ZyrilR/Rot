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

import static utils.AudioManager.playMusic;
import static utils.Constants.*;

public class EncounterSystem {

    private enum TerrainType { CAVE, TALL_GRASS, DARK_TALL_GRASS }

    private static final double ENCOUNTER_RATE_TALL_GRASS = 0.06;
    private static final double ENCOUNTER_RATE_DARK_GRASS = 0.02;
    private static final double ENCOUNTER_RATE_CAVE       = 0.03;

    private static final String[] WILD_BRAINROT_NAMES = {
            "TUNG TUNG TUNG SAHUR", "TRALALERO TRALALA", "BOMBARDINO CROCODILO",
            "LIRILI LARILA", "BRR BRR PATAPIM", "BONECA AMBALABU",
            "UDIN DIN DIN DIN DUN", "CAPUCCINO ASSASSINO"
    };

    private static final String[] CAVE_BRAINROT_NAMES = {
            "TUNG TUNG TUNG SAHUR",
            "UDIN DIN DIN DIN DUN",
            "BRR BRR PATAPIM",
            "BONECA AMBALABU",
            "CAPUCCINO ASSASSINO"
    };

    private static final int TALL_GRASS_TILE     = 2;
    private static final int CAVE_FLOOR_TILE      = 84;
    private static final int DARK_TALL_GRASS_TILE = 16;
    private static final int TRAINER_SIGHT_RANGE  = 5;

    private BattleManager activeBattle;

    public List<BrainRot> getPlayerTeam(GamePanel gp) { return gp.player.getPCSYSTEM().getParty(); }
    public BattleManager  getActiveBattle()            { return activeBattle; }

    // ── Wild Encounters ───────────────────────────────────────────────────────

    public void checkWildEncounter(Player player, GamePanel gp) {
        if (activeBattle != null) return;

        int gridX = player.worldX / TILE_SIZE;
        int gridY = player.worldY / TILE_SIZE;
        if (gridY < 0 || gridY >= MAX_WORLD_ROW || gridX < 0 || gridX >= MAX_WORLD_COL) return;

        boolean inCave = gp.CURRENT_PATH.toLowerCase().contains("cave");

        for (TileManager decoLayer : gp.world.getDecorationLayer()) {
            if (decoLayer == null) continue;
            int tileNum = decoLayer.getMap()[gridY][gridX];
            if (tileNum == TALL_GRASS_TILE) { triggerEncounter(player, gp, TerrainType.TALL_GRASS); return; }
            else if (tileNum == DARK_TALL_GRASS_TILE) { triggerEncounter(player, gp, TerrainType.DARK_TALL_GRASS); return; }
        }

        if (inCave) {
            for (TileManager bgLayer : gp.world.getBackgroundLayer()) {
                if (bgLayer == null) continue;
                int tileNum = bgLayer.getMap()[gridY][gridX];
                if (tileNum == CAVE_FLOOR_TILE) { triggerEncounter(player, gp, TerrainType.CAVE); return; }
            }
        }
    }

    private void triggerEncounter(Player player, GamePanel gp, TerrainType terrain) {
        double rate = switch (terrain) {
            case TALL_GRASS      -> ENCOUNTER_RATE_TALL_GRASS;
            case DARK_TALL_GRASS -> ENCOUNTER_RATE_DARK_GRASS;
            case CAVE            -> ENCOUNTER_RATE_CAVE;
        };
        if (Math.random() >= rate) return;

        BrainRot leader = getLeadBrainRot(gp);
        if (leader == null) return;

        Directories currentMap = Directories.getByPath(gp.CURRENT_PATH);
        int finalMax = Math.min(leader.getLevel(), currentMap.getMaxLevel());
        finalMax     = Math.max(finalMax, currentMap.getMinLevel());
        int finalMin = Math.max(currentMap.getMinLevel(), finalMax - 2);

        int wildLevel = utils.RandomUtil.range(finalMin, finalMax);
        BrainRot wild = spawnRandomWildBrainRot(wildLevel, terrain);
        startWildBattle(player, wild, gp);
    }

    // ── Battle Triggers ───────────────────────────────────────────────────────

    public void startWildBattle(Player player, BrainRot wildRot, GamePanel gp) {
        BrainRot playerRot = getLeadBrainRot(gp);
        if (playerRot == null) return;
        if (wildRot == null || wildRot.getMoves() == null || wildRot.getMoves().isEmpty()) {
            System.err.println("[EncounterSystem] Wild BrainRot invalid — skipping battle.");
            return;
        }
        activeBattle = new BattleManager(playerRot, wildRot, gp.player.getPCSYSTEM().getParty(), player, true);
        System.out.println("[EncounterSystem] Wild battle: " + wildRot.getName() + " Lv." + wildRot.getLevel());
        playMusic(utils.Constants.BGM_WILD_BATTLE, true);

    }

    public void startTrainerBattle(Player player, TrainerNPC trainer, GamePanel gp) {
        // ── FIX: guard player ──────────────────────────────────────────────────
        BrainRot playerRot = getLeadBrainRot(gp);
        if (playerRot == null) {
            System.err.println("[EncounterSystem] Trainer battle skipped — player has no healthy BrainRot.");
            return;
        }

        // ── FIX: guard trainer lead — ROOT CAUSE OF THE BATTLE LOOP ───────────
        // If trainer.getLeadBrainRot() is null (empty/all fainted party),
        // BattleManager gets a null enemyRot → BattleUI.draw() NPEs every frame.
        BrainRot trainerLead = trainer.getLeadBrainRot();
        if (trainerLead == null) {
            System.err.println("[EncounterSystem] Trainer " + trainer.name
                    + " has no healthy BrainRot — marking defeated to stop re-trigger.");
            trainer.setDefeated(true);
            return;
        }
        if (trainerLead.getMoves() == null || trainerLead.getMoves().isEmpty()) {
            System.err.println("[EncounterSystem] Trainer " + trainer.name
                    + "'s lead has no moves — marking defeated.");
            trainer.setDefeated(true);
            return;
        }

        playMusic(BGM_TRAINER_BATTLE, true);
        activeBattle = new BattleManager(playerRot, trainerLead, gp.player.getPCSYSTEM().getParty(), player, false);
        activeBattle.setTrainer(trainer);
        System.out.println("[EncounterSystem] Trainer battle: " + trainer.name);
    }

    public void clearBattle() { activeBattle = null; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BrainRot spawnRandomWildBrainRot(int level, TerrainType terrain) {
        String[] pool = (terrain == TerrainType.TALL_GRASS) ? WILD_BRAINROT_NAMES : CAVE_BRAINROT_NAMES;
        BrainRot wild = BrainRotFactory.createEnemy(pool[(int)(Math.random() * pool.length)], level);

        if (level > 1) {
            for (int i = 1; i < level; i++) wild.gainXp(wild.getXpToNextLevel());
        }
        wild.restoreForBattle();

        if (terrain == TerrainType.CAVE)            { wild.modifyAttack(0.20); wild.modifyDefense(0.20); }
        else if (terrain == TerrainType.DARK_TALL_GRASS) { wild.modifyAttack(0.10); wild.modifyDefense(0.10); }

        return wild;
    }

    private BrainRot getLeadBrainRot(GamePanel gp) {
        for (BrainRot rot : gp.player.getPCSYSTEM().getParty())
            if (rot != null && !rot.isFainted()) return rot;
        return null;
    }

    public void checkTrainerLook(Player player, List<NPC> npcs, GamePanel gp) {
        if (activeBattle != null) return; // already in battle — don't retrigger
        long now = gp.getGameTime();
        for (NPC npc : npcs) {
            if (npc == null) continue;
            if (!(npc instanceof TrainerNPC trainer)) continue;
            if (trainer.hasBeenDefeated()) {
                if (trainer.isCooldownExpired(now)) {
                    trainer.setDefeated(false);
                    String key = gp.CURRENT_PATH + "@" + (trainer.worldX / TILE_SIZE) + "," + (trainer.worldY / TILE_SIZE);
                    gp.defeatedTrainers.remove(key);
                } else {
                    continue;
                }
            }
            String dir = trainer.getFacingDirection();
            if (isInLineOfSight(dir,
                    player.worldX / TILE_SIZE, player.worldY / TILE_SIZE,
                    trainer.worldX   / TILE_SIZE, trainer.worldY   / TILE_SIZE)) {
                if (getLeadBrainRot(gp) != null && !gp.DIALOGUEBOX.isPlaying) {
                    trainer.facePlayer(player);
                    trainer.startEncounter(gp);
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