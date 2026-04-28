package save;

import brainrots.BrainRot;
import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import overworld.Player;
import progression.QuestSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static storage.PCSystem.*;
import static utils.Constants.*;
import static utils.Directories.*;

public class DataManager {

    // ── Save helpers ──────────────────────────────────────────────────────────

    public static void saveCurrentLoad(GamePanel gp) {
        saveData(gp, CURRENT_LOAD, false);
    }

    public static void saveNewData(GamePanel gp) {
        File savesRoot = new File(SAVES.getPath());
        int nextSlot;
        try {
            File cfg = new File(savesRoot, "saves_config.txt");
            if (!cfg.exists()) {
                nextSlot = 1;
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(cfg))) {
                    String line = br.readLine();
                    nextSlot = (line != null) ? Integer.parseInt(line.trim()) + 1 : 1;
                }
            }
            // Write updated config
            try (FileWriter fw = new FileWriter(cfg)) {
                fw.write(String.valueOf(nextSlot));
            }
        } catch (Exception e) {
            System.err.println("[DataManager] saveNewData config error: " + e.getMessage());
            nextSlot = 1;
        }
        saveData(gp, nextSlot, true);
    }

    private static void saveData(GamePanel gp, int folderID, boolean newFolder) {
        File folder = new File(SAVES.getPath(), String.valueOf(folderID));
        gp.GAMESTATE = "play";

        try {
            if (newFolder) folder.mkdirs();

            // Screenshot
            try {
                BufferedImage img = screenshotGamePanel(gp);
                ImageIO.write(img, "png", new File(folder, "screenshot.png"));
            } catch (Exception e) {
                System.err.println("[DataManager] Screenshot failed: " + e.getMessage());
            }

            // data.txt
            try (FileWriter fw = new FileWriter(new File(folder, "data.txt"))) {
                fw.write(getData(gp));
            }

            // quests.txt
            try (FileWriter fw = new FileWriter(new File(folder, "quests.txt"))) {
                fw.write(QuestSystem.getInstance().toFileFormat());
            }

            System.out.println("[DataManager] Saved slot " + folderID);

        } catch (Exception e) {
            System.err.println("[DataManager] Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Data serialization ────────────────────────────────────────────────────

    private static String getData(GamePanel gp) {
        Player plr = gp.player;

        String format =
                "[PLAYER]\n" +
                        plr.name + ";" +
                        plr.worldX + ";" + plr.worldY + ";" +
                        plr.getRotCoins() + ";" + plr.getDirection() + ";" + gp.CURRENT_PATH + "\n";

        // Inventory
        ArrayList<String> seen = new ArrayList<>();
        format += "[INVENTORY]\n";
        int i = 0;
        for (Item item : plr.getInventory().getRawItems()) {
            if (seen.contains(item.getName().toLowerCase())) { i++; continue; }
            seen.add(item.getName().toLowerCase());
            format += countOf(item.getName(), plr.getInventory()) + ";" + item.getName();
            if (i < plr.getInventory().getRawItems().size() - 1) format += ":";
            i++;
        }

        // PC System
        format += "\n[PCSYSTEM]\n==PARTY==\n" + plr.getPCSYSTEM().getPartySize() + "\n";
        for (BrainRot rot : plr.getPCSYSTEM().getParty())
            format += rot.toFileFormat() + "\n";

        format += "==STORED==\n" + plr.getPCSYSTEM().getPCCount() + "\n";
        for (i = 0; i < BOX_COUNT; i++) {
            for (int j = 0; j < BOX_CAPACITY; j++) {
                BrainRot rot = plr.getPCSYSTEM().getBoxMember(i, j);
                if (rot != null) format += i + ";" + j + "-" + rot.toFileFormat() + "\n";
            }
        }

        return format;
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    public static void loadLatestData(GamePanel gp) {
        loadData(gp, CURRENT_LOAD);
    }

    public static void loadData(GamePanel gp, int slotNo) {
        gp.player.reset();
        CURRENT_LOAD = slotNo;

        // ── Load quests (graceful if missing) ─────────────────────────────────
        QuestSystem.reset();
        File questFile = new File(SAVES.getPath() + "/" + slotNo, "quests.txt");
        if (questFile.exists()) {
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(questFile))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
            } catch (Exception e) {
                System.err.println("[DataManager] quests.txt read error: " + e.getMessage());
            }
            if (!lines.isEmpty()) QuestSystem.getInstance().loadFromLines(lines);
        } else {
            System.out.println("[DataManager] No quests.txt for slot " + slotNo + " — starting fresh.");
        }

        // ── Load player data ──────────────────────────────────────────────────
        File dataFile = new File(SAVES.getPath() + "/" + slotNo, "data.txt");
        if (!dataFile.exists()) {
            System.err.println("[DataManager] data.txt missing for slot " + slotNo);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line;

            // [PLAYER] header
            br.readLine();

            // Player line: name;worldX;worldY;coins;direction;path
            line = br.readLine();
            if (line == null) return;
            String[] parts = line.split(";");

            gp.player.name = parts[0];
            gp.player.setRotCoins(safeParseInt(parts[3]));
            gp.player.setDirection(parts[4]);

            String mapPath = (parts.length >= 6) ? parts[5] : utils.Directories.ROUTE131.getPath();
            gp.world.loadMap(mapPath, true);
            gp.CURRENT_PATH = mapPath;
            gp.player.teleport(new int[]{
                    safeParseInt(parts[1]) / TILE_SIZE,
                    safeParseInt(parts[2]) / TILE_SIZE
            });

            // [INVENTORY] header
            br.readLine();

            // Inventory line
            line = br.readLine();
            if (line != null && !line.isBlank() && !line.startsWith("[")) {
                String[] itemTokens = line.split(":");
                for (String token : itemTokens) {
                    String[] kv = token.split(";");
                    if (kv.length < 2)
                        continue;
                    int qty = safeParseInt(kv[0]);
                    Item item = ItemRegistry.getItem(kv[1].trim());
                    if (item != null) {
                        for (int q = 0; q < qty; q++) gp.player.getInventory().addItem(item);
                    }
                }
            }

            // [PCSYSTEM] header
            br.readLine(); // [PCSYSTEM]
            br.readLine(); // ==PARTY==
            int partySize = safeParseInt(br.readLine());
            for (int p = 0; p < partySize; p++) {
                line = br.readLine();
                if (line == null) break;
                BrainRot rot = parseRot(line.split(":"));
                if (rot != null) gp.player.getPCSYSTEM().addBrainRotToParty(rot);
            }

            br.readLine(); // ==STORED==
            int storedSize = safeParseInt(br.readLine());
            for (int s = 0; s < storedSize; s++) {
                line = br.readLine();
                if (line == null) break;
                // Format: boxIdx;slotIdx-<rotData>
                int dashIdx = line.indexOf('-');
                if (dashIdx < 0) continue;
                String location = line.substring(0, dashIdx);
                String rotData  = line.substring(dashIdx + 1);
                String[] locParts = location.split(";");
                if (locParts.length < 2) continue;
                int box  = safeParseInt(locParts[0]);
                int slot = safeParseInt(locParts[1]);
                BrainRot rot = parseRot(rotData.split(":"));
                if (rot != null) gp.player.getPCSYSTEM().addBrainRot(rot, box, slot);
            }

        } catch (Exception e) {
            System.err.println("[DataManager] loadData error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[DataManager] Loaded slot " + slotNo);
    }

    // ── BrainRot parsing ──────────────────────────────────────────────────────

    /**
     * Parses a BrainRot from the split colon-sections of toFileFormat().
     * parts[0] = semicolon-delimited attributes
     * parts[1] = pipe-delimited move names
     * parts[2] = pipe-delimited move UP values
     */
    private static BrainRot parseRot(String[] parts) {
        if (parts == null || parts.length < 1) return null;

        String[] attrs = parts[0].split(";");
        if (attrs.length < 17) {
            System.err.println("[DataManager] Malformed rot data (only " + attrs.length + " attrs): " + parts[0]);
            return null;
        }

        String[] moves  = (parts.length > 1 && !parts[1].isBlank()) ? parts[1].split("\\|") : new String[0];
        int[]    moveUPs = new int[moves.length];
        if (parts.length > 2 && !parts[2].isBlank()) {
            String[] upParts = parts[2].split("\\|");
            for (int i = 0; i < upParts.length && i < moveUPs.length; i++)
                moveUPs[i] = safeParseInt(upParts[i]);
        }

        try {
            return new BrainRot(
                    attrs[0],                           // name
                    safeParseInt(attrs[1]),             // level
                    safeParseInt(attrs[2]),             // currentXP
                    attrs[3],                           // primaryType
                    attrs[4],                           // secondaryType
                    attrs[5],                           // tier
                    safeParseInt(attrs[6]),             // MAX_HP
                    safeParseInt(attrs[7]),             // currentHP
                    safeParseInt(attrs[8]),             // BASE_ATK
                    safeParseInt(attrs[9]),             // BASE_DEF
                    safeParseInt(attrs[10]),            // BASE_SPEED
                    safeParseDouble(attrs[11]),         // attackMod
                    safeParseDouble(attrs[12]),         // defenseMod
                    safeParseDouble(attrs[13]),         // speedMod
                    attrs[14],                          // status
                    safeParseInt(attrs[15]),            // statusTurns
                    safeParseInt(attrs[16]),            // turnCount
                    moves,
                    moveUPs
            );
        } catch (Exception e) {
            System.err.println("[DataManager] parseRot failed: " + e.getMessage());
            return null;
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static int safeParseInt(String value) {
        if (value == null) return 0;
        String v = value.trim();
        if (v.isEmpty() || v.equalsIgnoreCase("NONE") || v.equalsIgnoreCase("null")) return 0;
        try { return Integer.parseInt(v); }
        catch (NumberFormatException e) { return 0; }
    }

    private static double safeParseDouble(String value) {
        if (value == null) return 1.0;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return 1.0; }
    }

    public static BufferedImage screenshotGamePanel(GamePanel gamePanel) {
        BufferedImage image = new BufferedImage(
                gamePanel.getWidth(),
                gamePanel.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        gamePanel.update();
        Graphics2D g2 = image.createGraphics();
        gamePanel.printAll(g2);
        g2.dispose();
        return image;
    }
}