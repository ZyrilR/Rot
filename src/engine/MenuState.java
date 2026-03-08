package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class MenuState extends GameState {

    public MenuState(GameStateManager gsm) {
        super(gsm);
    }

    @Override
    public void update() {
        /*TODO: example: if the user presses ENTER, switch to the Overworld
             if (gsm.gamePanel.input.enterPressed) {
            gsm.setState(GameStateManager.OVERWORLD_STATE);
         } */
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("MAIN MENU - Press a key to start (Logic pending)", 100, 100);
    }
}