package storage;

/**
 * Represents a reference to a single slot in either the party or a PC box.
 *
 * Used by PCSystem to generalize move / swap operations without caring
 * whether the source or destination is a party slot or a box slot.
 */
public class Slot {

    public enum SlotType { PARTY, BOX }

    private final SlotType type;
    private final int index;    // 0-5 for party, 0-24 for box
    private final int boxIndex; // which box (0-9); ignored for PARTY slots

    // ── Party slot ───────────────────────────────────────────────────────────

    public Slot(int partyIndex) {
        this.type     = SlotType.PARTY;
        this.index    = partyIndex;
        this.boxIndex = -1;
    }

    // ── Box slot ─────────────────────────────────────────────────────────────

    public Slot(int boxIndex, int slotIndex) {
        this.type     = SlotType.BOX;
        this.index    = slotIndex;
        this.boxIndex = boxIndex;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public SlotType getType()     { return type; }
    public int      getIndex()    { return index; }
    public int      getBoxIndex() { return boxIndex; }
    public boolean  isParty()     { return type == SlotType.PARTY; }
    public boolean  isBox()       { return type == SlotType.BOX; }

    @Override
    public String toString() {
        if (isParty()) return "Party[" + index + "]";
        return "Box[" + boxIndex + "][" + index + "]";
    }
}