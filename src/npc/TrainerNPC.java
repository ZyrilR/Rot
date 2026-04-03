package npc;

import engine.GamePanel;

public class TrainerNPC extends NPC {

    // Updated constructor to match the new NPC blueprint!
    public TrainerNPC(String name, int folderId, int x, int y) {
        super(name, folderId, x, y);
    }

    @Override
    public void interact(GamePanel gp) {
        // Here you will eventually trigger the battle state!
        System.out.println(name + " locked eyes with you! Time to battle!");

        // gp.GAMESTATE = "battle";
        // startBattle();
    }
}