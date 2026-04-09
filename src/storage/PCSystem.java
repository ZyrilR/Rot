package storage;

import brainrots.BrainRot;
import progression.QuestSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all BrainRot storage: party (max 6) and 10 boxes of 25 slots each.
 */
public class PCSystem {

    public static final int PARTY_CAPACITY = 6;
    public static final int BOX_COUNT      = 10;
    public static final int BOX_CAPACITY   = 25;

    private List<BrainRot> party = new ArrayList<>();
    private BrainRot[][] boxes   = new BrainRot[BOX_COUNT][BOX_CAPACITY];

    public enum MoveResult {
        SUCCESS,
        SWAPPED,
        PARTY_FULL,
        PARTY_WOULD_BE_EMPTY,
        SOURCE_EMPTY,
        BOX_FULL,
        SAME_SLOT
    }

    public PCSystem() {
        System.out.println("[PCSystem] Initialized with " + BOX_COUNT
                + " boxes x " + BOX_CAPACITY + " slots.");
    }

    // ── Party helpers ─────────────────────────────────────────────────────────

    public int     getPartySize()    { return party.size(); }
    public boolean isPartyFull()     { return party.size() >= PARTY_CAPACITY; }
    public boolean isPartyEmpty()    { return party.isEmpty(); }

    public int getPCCount() {
        int count = 0;
        for (int i = 0; i < BOX_COUNT; i++)
            for (int j = 0; j < BOX_CAPACITY; j++)
                if (boxes[i][j] != null) count++;
        return count;
    }

    public BrainRot getPartyMember(int index) {
        if (index < 0 || index >= party.size()) return null;
        return party.get(index);
    }

    public List<BrainRot> getParty() { return party; }

    // ── Box helpers ───────────────────────────────────────────────────────────

    public BrainRot getBoxMember(int boxIdx, int slotIdx) {
        if (boxIdx < 0 || boxIdx >= BOX_COUNT || slotIdx < 0 || slotIdx >= BOX_CAPACITY)
            return null;
        return boxes[boxIdx][slotIdx];
    }

    // ── Adding ────────────────────────────────────────────────────────────────

    public MoveResult addBrainRot(BrainRot rot) {
        if (!isPartyFull()) {
            party.add(rot);
            System.out.println("[PCSystem] Added " + rot.getName() + " to party.");
            QuestSystem.getInstance().onPartySizeChanged(party.size());
            return MoveResult.SUCCESS;
        }
        for (int b = 0; b < BOX_COUNT; b++) {
            for (int s = 0; s < BOX_CAPACITY; s++) {
                if (boxes[b][s] == null) {
                    boxes[b][s] = rot;
                    System.out.println("[PCSystem] Added " + rot.getName()
                            + " to Box " + (b + 1) + " slot " + s + ".");
                    QuestSystem.getInstance().onBoxSlotSet(b, s);
                    return MoveResult.SUCCESS;
                }
            }
        }
        System.out.println("[PCSystem] All storage full — cannot add " + rot.getName() + ".");
        return MoveResult.BOX_FULL;
    }

    public MoveResult addBrainRotToParty(BrainRot rot) {
        if (!isPartyFull()) {
            party.add(rot);
            System.out.println("[PCSystem] Added " + rot.getName() + " to party.");
            QuestSystem.getInstance().onPartySizeChanged(party.size());
            return MoveResult.SUCCESS;
        }
        System.out.println("[PCSystem] Party full — cannot add " + rot.getName() + ".");
        return MoveResult.PARTY_FULL;
    }

    public MoveResult addBrainRot(BrainRot rot, int box, int slot) {
        if (boxes[box][slot] == null) {
            boxes[box][slot] = rot;
            System.out.println("[PCSystem] Added " + rot.getName()
                    + " to Box " + (box + 1) + " slot " + (slot + 1) + ".");
            QuestSystem.getInstance().onBoxSlotSet(box, slot);
            return MoveResult.SUCCESS;
        }
        System.out.println("[PCSystem] Slot occupied — cannot add " + rot.getName() + ".");
        return MoveResult.BOX_FULL;
    }

    // ── Core move / swap ──────────────────────────────────────────────────────

    public MoveResult move(Slot src, Slot dst) {
        if (src.toString().equals(dst.toString())) return MoveResult.SAME_SLOT;

        BrainRot srcRot = getAt(src);
        BrainRot dstRot = getAt(dst);

        if (srcRot == null) return MoveResult.SOURCE_EMPTY;

        if (src.isParty() && dst.isBox()) {
            if (dstRot == null && party.size() <= 1) return MoveResult.PARTY_WOULD_BE_EMPTY;
            if (dstRot != null && party.size() <= 1 && !isInParty(dstRot))
                return MoveResult.PARTY_WOULD_BE_EMPTY;
        }

        if (src.isBox() && dst.isParty()) {
            if (dstRot == null && isPartyFull()) return MoveResult.PARTY_FULL;
        }

        setAt(src, dstRot);
        setAt(dst, srcRot);

        // Fire box achievement if destination is a box slot
        if (dst.isBox()) {
            QuestSystem.getInstance().onBoxSlotSet(dst.getBoxIndex(), dst.getIndex());
        }

        // Fire party size achievement if destination is party
        if (dst.isParty()) {
            QuestSystem.getInstance().onPartySizeChanged(party.size());
        }

        boolean isSwap = dstRot != null;
        System.out.println("[PCSystem] " + (isSwap ? "Swapped" : "Moved") + " "
                + srcRot.getName() + " from " + src + " to " + dst
                + (isSwap ? " (swapped with " + dstRot.getName() + ")" : ""));

        return isSwap ? MoveResult.SWAPPED : MoveResult.SUCCESS;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private BrainRot getAt(Slot slot) {
        if (slot.isParty()) return getPartyMember(slot.getIndex());
        return getBoxMember(slot.getBoxIndex(), slot.getIndex());
    }

    private void setAt(Slot slot, BrainRot rot) {
        if (slot.isParty()) setPartyAt(slot.getIndex(), rot);
        else boxes[slot.getBoxIndex()][slot.getIndex()] = rot;
    }

    private void setPartyAt(int index, BrainRot rot) {
        if (rot == null) {
            if (index >= 0 && index < party.size()) party.remove(index);
        } else {
            if (index < party.size()) party.set(index, rot);
            else party.add(rot);
        }
    }

    private boolean isInParty(BrainRot rot) { return party.contains(rot); }

    public int getFirstEmptyPartySlot() {
        if (isPartyFull()) return -1;
        return party.size();
    }

    public static String resultMessage(MoveResult result, String rotName) {
        return switch (result) {
            case SUCCESS              -> (rotName != null ? rotName + " moved!" : "Moved!");
            case SWAPPED              -> "Swapped!";
            case PARTY_FULL           -> "Party is full!";
            case PARTY_WOULD_BE_EMPTY -> "Party can't be empty!";
            case SOURCE_EMPTY         -> "No BrainRot here!";
            case BOX_FULL             -> "All boxes are full!";
            case SAME_SLOT            -> "";
        };
    }

    public void reset() {
        party = new ArrayList<>();
        boxes = new BrainRot[BOX_COUNT][BOX_CAPACITY];
    }
}