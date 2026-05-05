package npc;

import brainrots.BrainRot;
import brainrots.Type;
import items.Inventory;

import java.util.ArrayList;

public class GymLeader extends TrainerNPC {
    Type type;

    public GymLeader(String name, int folderId, int x, int y, Inventory inventory, ArrayList<BrainRot> party, int rotCoins, String type) {
        super(name, folderId, x, y, inventory, party, rotCoins);
        this.type = Type.getType(type);
    }
}
