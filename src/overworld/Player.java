package overworld;

import engine.GamePanel;
import input.KeyboardHandler;
import java.awt.*;

public class Player {

    GamePanel gp;
    KeyboardHandler kh;

    public int x, y;
    public int speed;

    public Player(GamePanel gp, KeyboardHandler kh) {
        this.gp = gp;
        this.kh = kh;
        setDefaultValues();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
    }

    public void update() {

    }

    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, gp.TILE_SIZE, gp.TILE_SIZE);
    }
}