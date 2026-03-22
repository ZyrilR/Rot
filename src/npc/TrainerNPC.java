package npc;

import engine.GamePanel;

public class TrainerNPC extends NPC {

    public TrainerNPC(String name) {
        super(name, "TrainerNPC", 0, 0);
    }
    public TrainerNPC(String name, int x, int y) {
        super(name, "TrainerNPC", x, y);
    }

    @Override
    public void interact(GamePanel gp) {
        //set gp's gamestate to battle
        //battle();
    }

    //BATTLE
}
