package engine;
import java.awt.*;

public abstract class GameState {
    protected String state;
    public GameState(String state){
        if (isState())
            this.state = state;
        else
            this.state = "menu";
    }
    public GameState() {
        this.state = "menu";
    }

    public boolean isState() {
        return state.equalsIgnoreCase("play") ||
                state.equalsIgnoreCase("menu") ||
                state.equalsIgnoreCase("battle");
    }
}

