package engine;

import java.awt.Graphics2D;

public class GameStateManager {

    private GameState currentState;
    public GamePanel gamePanel; // Passed so states can access screen size and input

    // State Identifiers
    public static final int MENU_STATE = 0;
    public static final int OVERWORLD_STATE = 1;
    public static final int BATTLE_STATE = 2;

    public GameStateManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        // Start the game at the Menu
        setState(OVERWORLD_STATE);
    }

    public void setState(int state) {
        if (state == MENU_STATE) {
            currentState = new MenuState(this);
        } else if (state == OVERWORLD_STATE) {
            currentState = new OverworldState(this);
        } else if (state == BATTLE_STATE) {
            currentState = new BattleState(this);
        }
    }

    public void update() {
        if (currentState != null) {
            currentState.update();
        }
    }

    public void draw(Graphics2D g) {
        if (currentState != null) {
            currentState.draw(g);
        }
    }
}