package engine;
import java.awt.*;

public abstract class GameState {
    protected GameStateManager gsm;

    public GameState(GameStateManager gsm){
        this.gsm = gsm;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g);
}

