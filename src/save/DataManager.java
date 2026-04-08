package save;

import brainrots.BrainRot;
import engine.GamePanel;
import items.Item;
import items.ItemRegistry;
import overworld.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static storage.PCSystem.*;
import static utils.Constants.*;
import static utils.Directories.*;

public class DataManager {

    /*
    Format:
    player_name;x;y;rotCoins;direction
    [INVENTORY]
    item_name;qty
    [PCSYSTEM]
    ==PARTY==
    [1] attributes
        [n] separated by semicolon
    [2] skills
        [n] separated by comma
            [m] separated by semicolon
    ==STORED==
    [1] name
    [2] attributes
        [n] separated by semicolon
    [3] skills
        [n] separated by comma
            [m] separated by semicolon
    */

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
            File img = new File(currentFolder, "screenshot.png");

            ImageIO.write(screenshotGamePanel(gp), "png", img);

            FileWriter fileWriter = new FileWriter(data);
            fileWriter.write(getData(gp));

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
                        plr.getRotCoins() + ";" + plr.getDirection() + "\n" + "[INVENTORY]\n";

        ArrayList<String> names = new ArrayList<>();

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
        try (BufferedReader br = new BufferedReader(new FileReader(new File(SAVES + "/" + slotNo, "data.txt")))) {

            //READ FIRST PART
            line = br.readLine();
//            System.out.println("LOADING " + line);

            //LOAD PLAYER ATTRIBUTES
            line = br.readLine();
            parts = line.split(";");

            gp.player.name = parts[0];
            gp.player.worldX = Integer.parseInt(parts[1]);
            gp.player.worldY = Integer.parseInt(parts[2]);
            gp.player.setRotCoins(Integer.parseInt(parts[3]));
            gp.player.setDirection(parts[4]);
            System.out.println(line);
            gp.world.loadMap(parts[5], true);
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
//                    System.out.println("LOADED ITEM " + item[1]);
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
        String[] attributes = parts[0].split(";");

                /*
                0 = Name
                1 = TYPE1
                2 = TYPE2 (nullable)
                3 = Tier
                4 = MaxHP
                5 = CurrentHP
                6 = Attack
                7 = Defense
                8 = Speed
                9 = CurrentSpeed
                10 = AttackMod
                11 = DefenseMod
                12 = SpeedMod
                13 = Status
                14 = StatusTurns
                15 = TurnCount
                 */

        String[] skills = parts[1].split("\\|");

        BrainRot rot = new BrainRot(
                attributes[0],
                attributes[1],
                attributes[2],
                attributes[3],
                Integer.parseInt(attributes[4]),
                Integer.parseInt(attributes[5]),
                Integer.parseInt(attributes[6]),
                Integer.parseInt(attributes[7]),
                Integer.parseInt(attributes[8]),
                Integer.parseInt(attributes[9]),
                Double.parseDouble(attributes[10]),
                Double.parseDouble(attributes[11]),
                Double.parseDouble(attributes[12]),
                attributes[13],
                Integer.parseInt(attributes[14]),
                Integer.parseInt(attributes[15]),
                skills
                );
        return rot;
    }

    public static BufferedImage screenshotGamePanel(GamePanel gamePanel) {
        BufferedImage image = new BufferedImage(
                gamePanel.getWidth(),
                gamePanel.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        gamePanel.update();
        Graphics2D g2 = image.createGraphics();
        gamePanel.printAll(g2); // Better than .paint() for capturing current state
        g2.dispose();
        return image;
    }
}
