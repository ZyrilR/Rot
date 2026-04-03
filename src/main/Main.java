package main;

import javax.swing.*;

import brainrots.BrainRotFactory;
import brainrots.BrainRotRegistry;
import engine.*;
import items.ItemRegistry;
import save.DataManager;
import tile.TileManager;
import utils.AssetManager;

public class Main {
    public static void main(String[] args) {
        AssetManager.loadAll();
        TileManager.loadTiles();

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
        DataManager.saveNewData();
    }
}
