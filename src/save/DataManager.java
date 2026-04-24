package save;

import brainrots.BrainRot;
import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import overworld.Player;
import progression.Quest;
import progression.QuestSystem;
import ui.QuestToast;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

import static storage.PCSystem.*;
import static utils.Constants.*;
import static utils.Directories.*;

public class DataManager {

    public static void saveCurrentLoad(GamePanel gp) {
        saveData(gp, CURRENT_LOAD, false);
    }
    public static void saveNewData(GamePanel gp) {
        File currentFolder = new File(SAVES.getPath());

        try (BufferedReader br = new BufferedReader(new FileReader(new File(currentFolder, "saves_config.txt")))) {
            int folders = Integer.parseInt(br.readLine());
            saveData(gp, folders + 1, true);
            FileWriter fw = new FileWriter(new File(currentFolder, "saves_config.txt"));
            fw.write("" + (folders + 1));
            fw.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private static void saveData(GamePanel gp, int folderID, boolean newFolder) {
        File currentFolder = new File(SAVES.getPath(), "/" + folderID);
        gp.GAMESTATE = "play";

        try {
            if (newFolder) {
                currentFolder.mkdir();
            }
            File data = new File(currentFolder, "data.txt");
            File quests = new File(currentFolder, "quests.txt");
            File img = new File(currentFolder, "screenshot.png");

            ImageIO.write(screenshotGamePanel(gp), "png", img);

            FileWriter fileWriter = new FileWriter(data);
            fileWriter.write(getData(gp));
            fileWriter.close();
            fileWriter = new FileWriter(quests);
            fileWriter.write(QuestSystem.getInstance().toFileFormat());

            fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getData(GamePanel gp) {
        Player plr = gp.player;
        //====WRITE CONTENT====
        String format =
                "[PLAYER]\n" +
                        plr.name + ";" +
                        plr.worldX + ";" + plr.worldY + ";" +
                        plr.getRotCoins() + ";" + plr.getDirection() + ";" + gp.CURRENT_PATH + "\n";

        ArrayList<String> names = new ArrayList<>();

        format += "[INVENTORY]\n";
        int i = 0;
        for (Item item : plr.getInventory().getRawItems()) {
            if (names.contains(item.getName().toLowerCase())) {
                i++;
                continue;
            }
            names.add(item.getName().toLowerCase());
            format += countOf(item.getName(), plr.getInventory()) + ";" + item.getName();
            if (i < plr.getInventory().getRawItems().size() - 1)
                format += ":";
            i++;
        }

        format += "\n[PCSYSTEM]\n==PARTY==\n" + plr.getPCSYSTEM().getPartySize() + "\n";

        for (BrainRot rot : plr.getPCSYSTEM().getParty())
            format += rot.toFileFormat() + "\n";

        format += "==STORED==\n" + plr.getPCSYSTEM().getPCCount() + "\n";
        for (i = 0; i < BOX_COUNT; i++) {
            for (int j = 0; j < BOX_CAPACITY; j++) {
                BrainRot rot = plr.getPCSYSTEM().getBoxMember(i, j);
                if (rot != null) {
                    format += i + ";" + j + "-" + rot.toFileFormat() + "\n";
                }
            }
        }
        return format;
    }

    public static void loadLatestData(GamePanel gp) {
        loadData(gp, CURRENT_LOAD);
    }

    public static void loadData(GamePanel gp, int slotNo) {
        gp.player.reset();
        String line;
        String[] parts;
        System.out.println("CURRENT LOAD: " + CURRENT_LOAD);

        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader((new FileReader(new File(SAVES.getPath() + "/" + slotNo, "quests.txt"))))) {

            while ((line = br.readLine()) != null) {
                System.out.println("LOADING LINE: " + line);
                lines.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        QuestSystem.reset();
        QuestSystem.getInstance().loadFromLines(lines);

        try (BufferedReader br = new BufferedReader(new FileReader(new File(SAVES.getPath() + "/" + slotNo, "data.txt")))) {

            //READ FIRST PART
            line = br.readLine();
//            System.out.println("LOADING " + line);

            //LOAD PLAYER ATTRIBUTES
            line = br.readLine();
            parts = line.split(";");

            gp.player.name = parts[0];
            gp.player.setRotCoins(Integer.parseInt(parts[3]));
            gp.player.setDirection(parts[4]);
            gp.world.loadMap(parts[5], true);
            gp.player.teleport(new int[]{Integer.parseInt(parts[1])/TILE_SIZE, Integer.parseInt(parts[2])/TILE_SIZE});
            System.out.println("LOADED WORLD 4: " + parts[5]);

            //READ SECOND PART
            line = br.readLine();
//            System.out.println("LOADING " + line);

            line = br.readLine();
            parts = line.split(":");
            for (String part : parts) {
                String[] item = part.split(";");
                for (int i = 0; i < Integer.parseInt(item[0]); i++) {
                    gp.player.getInventory().addItem(ItemRegistry.getItem(item[1]));
                }
            }

            //READ THIRD PART
            line = br.readLine();
//            System.out.println("LOADING " + line);

            //READ THIRD PART FIRST SECTION
            line = br.readLine();
//            System.out.println("LOADING " + line);

            //GET THIRD PART FIRST SECTION's SIZE
            line = br.readLine();
//            System.out.println("PARTY SIZE: " + line);

            //LOOP THROUGH THIRD PART FIRST SECTION
            for (int i = 0; i < Integer.parseInt(line); i++) {
                parts = br.readLine().split(":");
                BrainRot rot = getRot(parts);
                gp.player.getPCSYSTEM().addBrainRotToParty(rot);
//                System.out.println("ADDED TO PARTY: " + rot.getName());
            }

            //READ THIRD PART SECOND SECTION
            line = br.readLine();
//            System.out.println("LOADING " + line);

            //GET THIRD PART FIRST SECTION's SIZE
            line = br.readLine();
//            System.out.println("PARTY SIZE: " + line);

            //LOOP THROUGH THIRD PART FIRST SECTION
            for (int i = 0; i < Integer.parseInt(line); i++) {
                parts = br.readLine().split("-");
                BrainRot rot = getRot(parts[1].split(":"));
                parts = parts[0].split(";");
                gp.player.getPCSYSTEM().addBrainRot(rot, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
//                System.out.println("ADDED TO BOX [" + Integer.parseInt(parts[0]) + "," + Integer.parseInt(parts[1]) + "]: " + rot.getName());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BrainRot getRot(String[] parts) {

        System.out.println(parts[0]);

        String[] attributes = parts[0].split(";");

                /*
                0 = Name
                0.5 = Level
                0.75 = XP
                1 = TYPE1
                2 = TYPE2 (nullable)
                3 = Tier
                4 = MaxHP
                5 = CurrentHP
                6 = Attack
                7 = Defense
                8 = Speed
                9 = AttackMod
                10 = DefenseMod
                11 = SpeedMod
                12 = Status
                13 = StatusTurns
                14 = TurnCount
                parts[1] = moves (pipe-separated)
                parts[2] = per-move UP values (pipe-separated)
                 */

        String[] skills = parts[1].split("\\|");

        // Parse per-move UP values
        int[] moveUPs = new int[skills.length];
        if (parts.length > 2 && !parts[2].isEmpty()) {
            String[] upParts = parts[2].split("\\|");
            for (int i = 0; i < upParts.length && i < moveUPs.length; i++) {
                moveUPs[i] = Integer.parseInt(upParts[i]);
            }
        }

        System.out.println("HEALTH: " + safeParseInt(attributes[7]));
        System.out.println("MAXHEALTH: " + safeParseInt(attributes[6]));

        BrainRot rot = new BrainRot(
                attributes[0],                               // Name
                safeParseInt(attributes[1]),                 // Level
                safeParseInt(attributes[2]),                 // currentXP
                attributes[3],                               // primaryType
                attributes[4],                               // secondaryType
                attributes[5],                               // tier

                safeParseInt(attributes[6]),                 // MAX_HP
                safeParseInt(attributes[7]),                 // currentHP
                safeParseInt(attributes[8]),                 // BASE_ATK
                safeParseInt(attributes[9]),                 // BASE_DEF
                safeParseInt(attributes[10]),                 // BASE_SPEED
                Double.parseDouble(attributes[11]),           // attackMod
                Double.parseDouble(attributes[12]),          // defenseMod
                Double.parseDouble(attributes[13]),          // speedMod
                attributes[14],                              // status
                safeParseInt(attributes[15]),                // statusTurns
                safeParseInt(attributes[16]),                // turnCount
                skills,
                moveUPs
        );
        return rot;
    }

    private static int safeParseInt(String value) {
        if (value == null || value.trim().isEmpty() ||
                value.equalsIgnoreCase("NONE") || value.equalsIgnoreCase("null")) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static BufferedImage screenshotGamePanel(GamePanel gamePanel) {
        BufferedImage image = new BufferedImage(
                gamePanel.getWidth(),
                gamePanel.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        gamePanel.update();
        Graphics2D g2 = image.createGraphics();
        gamePanel.printAll(g2);
        g2.dispose();
        return image;
    }
}
