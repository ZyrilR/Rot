package npc;

import engine.GamePanel;
import ui.DevConsole;

public class HealerMachine extends NPC {

    public HealerMachine(int x, int y) {
        super("HealerMachine", 0, x, y);
    }

    @Override
    public void interact(GamePanel gp) {
        super.interact(gp);
        System.out.println("HEADLING EVERYTHING");
        DevConsole dc = new DevConsole(gp);
        dc.cmdHealAll();
        dc.close();
    }

}
