package jmri.jmrix.loconet;

/**
 * slotMapEntry - a from to pair of slot numbers defining a valid range of loco/system slots
 * TODO add slottype, eg systemslot, std slot, expanded slot etc
 * @author sg
 *
 */
public class SlotMapEntry {

    public SlotMapEntry(int from, int to, SlotType type) {
        fromSlot = from;
        toSlot = to;
        slotType = type;
    }

    public enum SlotType {
        UNKNOWN,
        SYSTEM,
        LOCO
    }

    int fromSlot;
    int toSlot;
    SlotType slotType;

    protected int getFrom() {
        return fromSlot;
    }
    protected int getTo() {
        return toSlot;
    }
    protected SlotType getSlotType() {
        return slotType;
    }
}
