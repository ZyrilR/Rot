package engine;

import overworld.Player;
import overworld.MovementSystem;
import java.awt.Color;
import java.awt.Graphics2D;
import npc.MarketNPC;

public class OverworldState extends GameState {

    private Player player;
    private MovementSystem movementSystem;

    public OverworldState(GameStateManager gsm) {
        super(gsm);

        player = new Player(gsm.gamePanel, gsm.gamePanel.input);
        movementSystem = new MovementSystem(gsm.gamePanel);
    }

    @Override
    public void update() {
        movementSystem.movePlayer(player, gsm.gamePanel.input);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, gsm.gamePanel.SCREEN_WIDTH, gsm.gamePanel.SCREEN_HEIGHT);

        player.draw(g);
    }
}