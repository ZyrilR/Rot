package npc;

import engine.GamePanel;
import java.awt.*;

import static utils.Constants.*;

public abstract class NPC {

    public String name;
    public int x, y;
    protected GamePanel gp;

    public NPC(GamePanel gp, String name, int x, int y){
        this.gp = gp;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public abstract void interact();

    public void draw(Graphics2D g){
        g.setColor(Color.BLUE);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        g.setColor(Color.WHITE);
        g.drawString(name, x, y - 10);
    }
}
