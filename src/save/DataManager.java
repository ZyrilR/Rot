package save;

import brainrots.BrainRot;
import engine.GamePanel;
import overworld.Player;
import storage.PCSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;

import static storage.PCSystem.BOX_CAPACITY;
import static storage.PCSystem.BOX_COUNT;
import static utils.Constants.SAVES;

public class DataManager {

    /*
    Format:
    player_name;x;y;rotCoins;direction
    [INVENTORY]
    item_name;qty
    [PCSYSTEM]
    ==PARTY==
    [1] name
    [2] attributes
        [n] separated by semicolon
    [3] skills
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

    public static void saveNewData(GamePanel gp) {
        Player plr = gp.player;
        int folders = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(SAVES, "saves_config.txt")))){
            //get configuration file and check for how many folders there are
            String line = br.readLine();
            if (line != null)
                folders = Integer.parseInt(line);

            File newFolder = new File(SAVES, "" + (folders + 1));

            if (newFolder.mkdir()) {
                File data = new File(newFolder, "data.txt");
                FileWriter fileWriter = new FileWriter(SAVES + "/saves_config.txt", false);
                fileWriter.write("" + (folders + 1));
                fileWriter.close();
                fileWriter = new FileWriter(data);

                //====WRITE CONTENT====
                String format =
                        "[PLAYER]\n" +
                        plr.name + ";" +
                        plr.worldX + ";" + plr.worldY + ";" +
                        plr.getRotCoins() + ";" + plr.getDirection() + "\n" + "[INVENTORY]\n" +
                        "[PCSYSTEM]\n==PARTY==\n";

                for (BrainRot rot : plr.getPCSYSTEM().getParty()) {
                    format += rot.toFileFormat() + "\n";
                }

                format += "==STORED==\n";
                for (int i = 0; i < BOX_COUNT; i++) {
                    for (int j = 0; j < BOX_CAPACITY; j++) {
                        BrainRot rot = plr.getPCSYSTEM().getBoxMember(i, j);
                        if (rot != null) {
                            format += rot.toFileFormat() + "\n";
                        }
                    }
                }

                fileWriter.write(format);

                File img = new File(newFolder, "screenshot.png");

                ImageIO.write(screenshotGamePanel(gp), "png", img);

                fileWriter.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadData(int slotNo) {

    }

    public static BufferedImage screenshotGamePanel(GamePanel gamePanel) {
        BufferedImage image = new BufferedImage(
                gamePanel.getWidth(),
                gamePanel.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        gamePanel.save();
        Graphics2D g2 = image.createGraphics();
        gamePanel.printAll(g2); // Better than .paint() for capturing current state
        g2.dispose();
        return image;
    }
}
