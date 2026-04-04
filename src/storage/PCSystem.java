package storage;

import brainrots.BrainRot;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all BrainRot storage: party (max 6) and 10 boxes of 25 slots each.
 *
 * All movement logic lives here — PCUI calls these methods and reacts to
 * the returned MoveResult to display appropriate feedback.
 */
public class PCSystem {

    // ── Constants ─────────────────────────────────────────────────────────────

    public static final int PARTY_CAPACITY = 6;
    public static final int BOX_COUNT      = 10;
    public static final int BOX_CAPACITY   = 25; // 5x5 grid

    // ── Storage ───────────────────────────────────────────────────────────────

    /**
     * Party list. Size never exceeds PARTY_CAPACITY.
     * A null entry is not used here — party entries are contiguous.
     */
    private List<BrainRot> party = new ArrayList<>();

    /**
     * Boxes: boxes[b][s] where b=0..9, s=0..24.
     * null = empty slot.
     */
    private BrainRot[][] boxes = new BrainRot[BOX_COUNT][BOX_CAPACITY];

    // ── Result enum ───────────────────────────────────────────────────────────

    public enum MoveResult {
        SUCCESS,
        SWAPPED,
        PARTY_FULL,          // cannot withdraw — party already has 6
        PARTY_WOULD_BE_EMPTY,// cannot deposit — would leave 0 in party
        SOURCE_EMPTY,        // tried to move from an empty slot
        BOX_FULL,            // all boxes are completely full
        SAME_SLOT            // source == destination
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public PCSystem() {
        System.out.println("[PCSystem] Initialized with " + BOX_COUNT + " boxes x " + BOX_CAPACITY + " slots.");
    }

    // ── Party helpers ─────────────────────────────────────────────────────────

    public int getPartySize()   { return party.size(); }
    public boolean isPartyFull(){ return party.size() >= PARTY_CAPACITY; }
    public boolean isPartyEmpty(){ return party.isEmpty(); }
    public int getPCCount() {
        int count = 0;
        for (int i = 0; i < BOX_COUNT; i++) {
            for (int j = 0; j < BOX_CAPACITY; j++) {
                if (boxes[i][j] != null)
                    count++;
            }
        }
        return count;
    }

    /** Returns the BrainRot at party index, or null if out of bounds / empty slot. */
    public BrainRot getPartyMember(int index) {
        if (index < 0 || index >= party.size()) return null;
        return party.get(index);
    }

    /** Returns the entire party list (read-only view). */
    public List<BrainRot> getParty() { return party; }

    // ── Box helpers ───────────────────────────────────────────────────────────

    /** Returns the BrainRot at boxes[boxIdx][slotIdx], or null if empty. */
    public BrainRot getBoxMember(int boxIdx, int slotIdx) {
        if (boxIdx < 0 || boxIdx >= BOX_COUNT || slotIdx < 0 || slotIdx >= BOX_CAPACITY) return null;
        return boxes[boxIdx][slotIdx];
    }

    // ── Adding a new BrainRot (from capture / gift) ───────────────────────────

    /**
     * Adds a newly obtained BrainRot.
     * Party first; if full, sends to the first available box slot.
     * Returns MoveResult.BOX_FULL if both party and boxes are full.
     */
    public MoveResult addBrainRot(BrainRot rot) {
        if (!isPartyFull()) {
            party.add(rot);
            System.out.println("[PCSystem] Added " + rot.getName() + " to party.");
            return MoveResult.SUCCESS;
        }

        // Try boxes
        for (int b = 0; b < BOX_COUNT; b++) {
            for (int s = 0; s < BOX_CAPACITY; s++) {
                if (boxes[b][s] == null) {
                    boxes[b][s] = rot;
                    System.out.println("[PCSystem] Added " + rot.getName() + " to Box " + (b+1) + " slot " + s + ".");
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
            return MoveResult.SUCCESS;
        } else {
            System.out.println("[PCSystem] Party Slot Full — cannot add " + rot.getName() + ".");
            return MoveResult.PARTY_FULL;
        }
    }
    public MoveResult addBrainRot(BrainRot rot, int box, int slot) {
        if (boxes[box][slot] == null) {
            boxes[box][slot] = rot;
            System.out.println("[PCSystem] Added " + rot.getName() + " to Box " + (box + 1) + " slot " + (slot + 1) + ".");
            return MoveResult.SUCCESS;
        } else {
            System.out.println("[PCSystem] All storage full — cannot add " + rot.getName() + ".");
            return MoveResult.BOX_FULL;
        }
    }

    // ── Core move / swap ──────────────────────────────────────────────────────

    /**
     * Moves or swaps the BrainRot from {@code src} to {@code dst}.
     *
     * Handles all cases:
     *   Party → Box   (deposit)
     *   Box → Party   (withdraw)
     *   Party → Party (reorder)
     *   Box → Box     (reorganize)
     *
     * Business rules enforced:
     *   - Cannot move from an empty slot.
     *   - Cannot deposit the last party member.
     *   - Cannot withdraw to a full party.
     *   - Moving to an empty slot = move; to an occupied slot = swap.
     */
    public MoveResult move(Slot src, Slot dst) {
        if (src.toString().equals(dst.toString())) return MoveResult.SAME_SLOT;

        BrainRot srcRot = getAt(src);
        BrainRot dstRot = getAt(dst);

        if (srcRot == null) return MoveResult.SOURCE_EMPTY;

        // Rule: depositing party → box must leave at least 1 member
        if (src.isParty() && dst.isBox()) {
            if (dstRot == null && party.size() <= 1) return MoveResult.PARTY_WOULD_BE_EMPTY;
            if (dstRot != null && party.size() <= 1 && !isInParty(dstRot)) return MoveResult.PARTY_WOULD_BE_EMPTY;
        }

        // Rule: withdrawing box → party only allowed if party not full
        if (src.isBox() && dst.isParty()) {
            // dst party slot is always treated as "append to end" OR replace specific index
            // If dstRot is null and party is full (trying to place in a non-existent slot), reject
            if (dstRot == null && isPartyFull()) return MoveResult.PARTY_FULL;
        }

        // Perform the swap (null = move, non-null = swap)
        setAt(src, dstRot);
        setAt(dst, srcRot);

        boolean isSwap = dstRot != null;
        System.out.println("[PCSystem] " + (isSwap ? "Swapped" : "Moved") + " " + srcRot.getName()
                + " from " + src + " to " + dst
                + (isSwap ? " (swapped with " + dstRot.getName() + ")" : ""));

        return isSwap ? MoveResult.SWAPPED : MoveResult.SUCCESS;
    }

    // ── Internal slot accessors ───────────────────────────────────────────────

    private BrainRot getAt(Slot slot) {
        if (slot.isParty()) {
            return getPartyMember(slot.getIndex());
        } else {
            return getBoxMember(slot.getBoxIndex(), slot.getIndex());
        }
    }

    private void setAt(Slot slot, BrainRot rot) {
        if (slot.isParty()) {
            setPartyAt(slot.getIndex(), rot);
        } else {
            boxes[slot.getBoxIndex()][slot.getIndex()] = rot;
        }
    }

    /**
     * Sets a party slot. Party is a List, so we handle both
     * replacing an existing index and appending.
     */
    private void setPartyAt(int index, BrainRot rot) {
        if (rot == null) {
            // Remove from party (compact the list)
            if (index >= 0 && index < party.size()) {
                party.remove(index);
            }
        } else {
            if (index < party.size()) {
                party.set(index, rot);
            } else {
                // Append (e.g., when depositing something into a slot beyond current size)
                party.add(rot);
            }
        }
    }

    /** Returns true if the given BrainRot reference exists in the party list. */
    private boolean isInParty(BrainRot rot) {
        return party.contains(rot);
    }

    /** Returns the index of the first empty party slot, or -1 if party is full */
    public int getFirstEmptyPartySlot() {
        if (isPartyFull()) return -1;         // Party is full
        return party.size();                  // Next available index is first empty
    }

    // ── Feedback string for MoveResult ───────────────────────────────────────

    public static String resultMessage(MoveResult result, String rotName) {
        return switch (result) {
            case SUCCESS             -> (rotName != null ? rotName + " moved!" : "Moved!");
            case SWAPPED             -> "Swapped!";
            case PARTY_FULL          -> "Party is full!";
            case PARTY_WOULD_BE_EMPTY-> "Party can't be empty!";
            case SOURCE_EMPTY        -> "No BrainRot here!";
            case BOX_FULL            -> "All boxes are full!";
            case SAME_SLOT           -> "";
        };
    }

    public void reset() {
        party = new ArrayList<>();
        boxes = new BrainRot[BOX_COUNT][BOX_CAPACITY];
    }
}