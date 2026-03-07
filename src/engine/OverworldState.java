package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class OverworldState extends GameState {

    // declare your Player object here
    // private Player player;

    public OverworldState(GameStateManager gsm) {
        super(gsm);
        // Initialize player here
        // player = new Player(gsm.gamePanel, gsm.gamePanel.input);
    }

    @Override
    public void update() {
        // player.update();
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.drawString("OVERWORLD STATE - Map and Player go here", 100, 100);
        // player.draw(g);
    }
}