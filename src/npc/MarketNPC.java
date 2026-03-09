package npc;

import engine.GamePanel;

public class MarketNPC extends NPC {

    public MarketNPC(GamePanel gp, int x, int y) {
        super(gp, "DINDINDINDININ", x, y);
    }

    @Override
    public void interact() {
        System.out.println(name + ": DIN OTIN'");
    }
}