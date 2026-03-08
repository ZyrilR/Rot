package engine;

import java.awt.Color;
import java.awt.Graphics2D;

import overworld.Player;

public class OverworldState extends GameState {

    //TODO: declare your Player object here
    // private Player player;
    private Player player;

    public OverworldState(GameStateManager gsm) {
        super(gsm);
        //TODO: Initialize player here
         player = new Player(gsm.gamePanel, gsm.gamePanel.input);
    }

    @Override
    public void update() {
        player.update();
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0 , 0, gsm.gamePanel.SCREEN_WIDTH, gsm.gamePanel.SCREEN_HEIGHT);

        player.draw(g);
    }
}