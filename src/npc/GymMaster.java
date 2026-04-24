package npc;

import brainrots.BrainRot;
import items.Inventory;

import java.util.ArrayList;

public class GymMaster extends TrainerNPC {
    public GymMaster(String name, int folderId, int x, int y, Inventory inventory, ArrayList<BrainRot> party, int rotCoins) {
        super(name, folderId, x, y, inventory, party, rotCoins);
    }
}
