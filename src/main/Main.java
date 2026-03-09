package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import engine.*;
import overworld.MovementSystem;
import overworld.Player;
import utils.AssetManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        AssetManager.loadAll();

        JFrame windows = new JFrame();
        windows.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        windows.setResizable(false);
        windows.setTitle("Rot");

        GamePanel gamePanel = new GamePanel();

        windows.add(gamePanel);

        windows.pack();

        windows.setLocationRelativeTo(null);
        windows.setVisible(true);

        new GameLoop(gamePanel).startGameThread(gamePanel);
    }
}
