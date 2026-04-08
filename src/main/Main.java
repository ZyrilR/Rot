package main;

import javax.swing.*;

import engine.*;
import save.DataManager;
import tile.TileManager;
import utils.AssetManager;
import utils.Constants;

public class Main {
    public static void main(String[] args) {
        AssetManager.loadAll();
        TileManager.loadTiles();
        Constants.InitializeBrainrotDescriptions();

        JFrame windows = new JFrame();
        windows.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        windows.setResizable(false);
        windows.setTitle("Rot");

        GamePanel gamePanel = new GamePanel();
        windows.add(gamePanel);
        windows.pack();
        windows.setLocationRelativeTo(null);
        windows.setVisible(true);

        new GameLoop(gamePanel).startGameThread();
    }
}
