package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class BattleState extends GameState {

    public BattleState(GameStateManager gsm) {
        super(gsm);
        //TODO: Initialize BattleManager, TurnManager, etc.
    }

    @Override
    public void update() {
        //TODO: Battle Turn Logic dirii
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.drawString("BATTLE STATE - Brain Rots fight here!", 100, 100);
    }
}