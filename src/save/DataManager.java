package save;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

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

    public static void saveNewData() {

        try (BufferedReader br = new BufferedReader(new FileReader(new File(SAVES, "saves-config.txt")))){
            //get configuration file and check for how many folders there are

            int folders = Integer.parseInt(br.readLine());

            File newFolder = new File(SAVES + "/" + (folders + 1));

            if (newFolder.mkdir()) {
                System.out.println("SUCCESSFUL FOLDER CREATION");
                File data = new File(newFolder, "data.txt");
                FileWriter fileWriter = new FileWriter(SAVES + "/saves-config.txt");
                fileWriter.write(folders + 1);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadData(int slotNo) {

    }
}
