// SprogSlot.java

package jmri.jmrix.sprog;

import java.util.Vector;

/**
 * Represents information for a DCC Command Station Queue entry where each entry
 * is a DCC packet to be transmitted to the rails
 * <P>
 * A SlotListener can be registered to hear of changes in this slot.  All
 * changes in values will result in notification.
 * <P>
 * @author			Andrew Crosland Copyright (C) 2006
 * @version			$Revision: 1.1 $
 */
 public class SprogSlot {

   public SprogSlot(int num) {
     payload = new byte[SprogConstants.MAX_PACKET_LENGTH];
     length = 3;
     payload[0] = 0;
     payload[1] = 0;
     payload[2] = 0;
     repeat = -1;
     addr = 0;
     spd = 0;
     forward = true;
     status = SprogConstants.SLOT_FREE;
     slot = num;
   }

   private byte[] payload;
   private int length;
   // repeat of -1 is a persistent entry, ie a loco slot
   private int repeat;
   private int addr;
   private int spd;
   private boolean forward;
   private int status;
   private int slot;

     /**
      * Set the contents of the slot. Intended for accessory packets
      *
      * @param address int
      * @param payload byte[]
      * @param repeat int
      */
     public void set(int address, byte [] payload, int repeat) {
       addr = address;
       length = payload.length;
       for (int i=0; i<length; i++) { this.payload[i] = payload[i]; }
       setRepeat(repeat);
       status = SprogConstants.SLOT_IN_USE;
       notifySlotListeners();
     }

     /**
      * Set the contents of the slot.
      *
      * @param address int
      * @param spd int
      * @param Forward boolean
      * @param payload byte[]
      * @param repeat int number of times packet should be sent to rails -1 is
      * a persistent speed/direction packet. > 1000 is an ops mode programming
      * packet
      */
     public void set(int address, int speed, boolean forward, byte [] payload,
                     int repeat) {
       addr = address;
       spd = speed;
       this.forward = forward;
       length = payload.length;
       for (int i=0; i<length; i++) { this.payload[i] = payload[i]; }
       setRepeat(repeat);
       status = SprogConstants.SLOT_IN_USE;
       notifySlotListeners();
     }

     // Access methods
     public void clear() {
       status = SprogConstants.SLOT_FREE;
       length = 3;
       addr = 0;
       payload[0] = 0;
       payload[1] = 0;
       payload[2] = 0;
       notifySlotListeners();
     }

     public boolean isLongAddress() { return (getAddr() >= 100); }
     public boolean isFree() { return (status == SprogConstants.SLOT_FREE); }
     public int slotStatus() { return status; }
     public int getRepeat() { return repeat; }
     public void setRepeat(int r) { repeat = r; }
     public int doRepeat() {
       if (repeat > 0) {
         log.debug("Slot "+slot+"repeats");
         repeat--;
         if (repeat == 0) {
           log.debug("Clear slot "+slot+"due to repeats exhausted");
         }
       }
       return repeat;
     }
     public int speed() { return spd; }
     public int locoAddr() { return addr; }
     public int getAddr() { return addr; }
     public void setAddr(int a) { addr = a; notifySlotListeners(); }
     public boolean isForward() { return forward; }
     public byte[] getPayload() {
       byte [] p;
       p = new byte[length];
       for (int i=0; i<length; i++) {
         p[i] = payload[i];
       }
       return p;
     }

     public int getSlot() { return slot; }

    private long lastUpdateTime ; // Time of last update for detecting stale slots

    public long getLastUpdateTime() { return lastUpdateTime ; }

    // data members to hold contact with the slot listeners
    final private Vector slotListeners = new Vector();

    public synchronized void addSlotListener(SprogSlotListener l) {
        // add only if not already registered
        if (!slotListeners.contains(l)) {
            slotListeners.addElement(l);
        }
    }

    public synchronized void removeSlotListener(SprogSlotListener l) {
        if (slotListeners.contains(l)) {
            slotListeners.removeElement(l);
        }
    }

    protected void notifySlotListeners() {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this)
            {
                v = (Vector) slotListeners.clone();
            }
        if (log.isDebugEnabled()) log.debug("----->notify "+v.size()
                                            +" SlotListeners");
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            SprogSlotListener client = (SprogSlotListener) v.elementAt(i);
            client.notifyChangedSlot(this);
        }
    }

    /**
     * Get the address from the packet
     * @return int
     */
    private int addressFromPacket() {
      if (isFree()) { return -1; }
      if (payload[0] >= 0xC0) { return (payload[0]<<8 | payload[1]); }
      return payload[0];
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogSlot.class.getName());
}


/* @(#)SprogSlot.java */
