// LocoNetSlot.java

package jmri.jmrix.loconet;

import java.util.*;

/**
 * Represents the contents of a single slot in the LocoNet command station
 * <P>
 * A SlotListener can be registered to hear of changes in this slot.  All
 * changes in values will result in notification.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.7 $
 */
public class LocoNetSlot {

    // accessors to specific information
    public int getSlot() { return slot;}  // cannot modify the slot number once created

    // status accessors
    // decoder mode
    // possible values are  DEC_MODE_128A, DEC_MODE_28A, DEC_MODE_128,
    //						DEC_MODE_14, DEC_MODE_28TRI, DEC_MODE_28
    public int decoderType() 	{ return stat&LnConstants.DEC_MODE_MASK;}

    // slot status
    // possible values are LOCO_IN_USE, LOCO_IDLE, LOCO_COMMON, LOCO_FREE
    public int slotStatus() 	{ return stat&LnConstants.LOCOSTAT_MASK; }

    // consist status
    // possible values are CONSIST_MID, CONSIST_TOP, CONSIST_SUB, CONSIST_NO
    public int consistStatus() 	{ return stat&LnConstants.CONSIST_MASK; }

    // direction and functions
    public boolean isForward()	{ return 0==(dirf&LnConstants.DIRF_DIR); }
    public boolean isF0()	{ return 0!=(dirf&LnConstants.DIRF_F0); }
    public boolean isF1()	{ return 0!=(dirf&LnConstants.DIRF_F1); }
    public boolean isF2()	{ return 0!=(dirf&LnConstants.DIRF_F2); }
    public boolean isF3()	{ return 0!=(dirf&LnConstants.DIRF_F3); }
    public boolean isF4()	{ return 0!=(dirf&LnConstants.DIRF_F4); }
    public boolean isF5()	{ return 0!=(snd&LnConstants.SND_F5); }
    public boolean isF6()	{ return 0!=(snd&LnConstants.SND_F6); }
    public boolean isF7()	{ return 0!=(snd&LnConstants.SND_F7); }
    public boolean isF8()	{ return 0!=(snd&LnConstants.SND_F8); }

    // loco address, speed
    public int locoAddr()   { return addr; }
    public int speed()      { return spd; }

    public int id()			{ return id; }

    // programmer track special case accessors
    public int pcmd()          	{ return _pcmd; }
    public int cvval()          { return snd+(ss2&2)*64; }

    // global track status should be reference through SlotManager

    // create a specific slot
    public LocoNetSlot(int slotNum)  { slot = slotNum;}
    public LocoNetSlot(LocoNetMessage l) throws LocoNetException {
        slot = l.getElement(1);
        setSlot(l);
    }

    // methods to interact with LocoNet
    public void setSlot(LocoNetMessage l) throws LocoNetException { // exception if message can't be parsed
        // sort out valid messages, handle
        switch (l.getOpCode()) {
        case LnConstants.OPC_WR_SL_DATA:
        case LnConstants.OPC_SL_RD_DATA: {
            if ( l.getElement(1) != 0x0E ) return;  // not an appropriate reply
            // valid, so fill contents
            if (slot != l.getElement(2)) log.error("Asked to handle message not for this slot ("
                                                    +slot+") "+l);
            stat = l.getElement(3);
            _pcmd = l.getElement(4);
            addr = l.getElement(4)+128*l.getElement(9);
            spd =  l.getElement(5);
            dirf = l.getElement(6);
            trk =  l.getElement(7);
            ss2 =  l.getElement(8);
            // item 9 is add2
            snd =  l.getElement(10);
            id =   l.getElement(11)+128*l.getElement(12);
            notifySlotListeners();
            return;
        }
        case LnConstants.OPC_SLOT_STAT1:
            if (slot != l.getElement(1)) log.error("Asked to handle message not for this slot "+l);
            stat = l.getElement(2);
            notifySlotListeners();
            return;
        case LnConstants.OPC_LOCO_SND: {
            // set sound functions in slot - first, clear bits
            snd &= ~(LnConstants.SND_F5 | LnConstants.SND_F6
                     | LnConstants.SND_F7 | LnConstants.SND_F8);
            // and set them as masked
            snd |= ((LnConstants.SND_F5 | LnConstants.SND_F6
                     | LnConstants.SND_F7 | LnConstants.SND_F8) & l.getElement(2));
            notifySlotListeners();
            return;
        }
        case  LnConstants.OPC_LOCO_DIRF: {
            // set direction, functions in slot - first, clear bits
            dirf &= ~(LnConstants.DIRF_DIR | LnConstants.DIRF_F0
                      | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                      | LnConstants.DIRF_F3 | LnConstants.DIRF_F4);
            // and set them as masked
            dirf += ((LnConstants.DIRF_DIR | LnConstants.DIRF_F0
                      | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                      | LnConstants.DIRF_F3 | LnConstants.DIRF_F4) & l.getElement(2));
            notifySlotListeners();
            return;
        }
        case LnConstants.OPC_MOVE_SLOTS: {
            // change in slot status will be reported by the reply,
            // so don't need to do anything here (but could)
            return;
        }
        case LnConstants.OPC_LOCO_SPD: {
            // set speed
            spd  = l.getElement(2);
            notifySlotListeners();
            return;
        }
        }
    }

    public LocoNetMessage writeSlot() {
        LocoNetMessage l = new LocoNetMessage(13);
        l.setOpCode( LnConstants.OPC_WR_SL_DATA );
        l.setElement(1, 0x0E);
        l.setElement(2, slot);
        l.setElement(3, stat);
        l.setElement(4, addr & 0x7F); l.setElement(9, (addr/128)&0x7F);
        l.setElement(5, spd);
        l.setElement(6, dirf);
        l.setElement(7, trk);
        l.setElement(8, ss2);
        // item 9 is add2
        l.setElement(10, snd);
        l.setElement(11, id&0x7F); l.setElement(12, (id/128)&0x7F );
        return l;
    }

    // data values to echo slot contents
    final private int slot;   // <SLOT#> is the number of the slot that was read.
    private int stat;	// <STAT> is the status of the slot
    private int addr;	// full address of the loco, made from
    //    <ADDR> is the low 7 (0-6) bits of the Loco address
    //    <ADD2> is the high 7 bits (7-13) of the 14-bit loco address
    private int spd;	// <SPD> is the current speed (0-127)
    private int dirf;	// <DIRF> is the current Direction and the setting for functions F0-F4
    private int trk;	// <TRK> is the global track status
    private int ss2;	// <SS2> is the an additional slot status
    private int snd; 	// <SND> is the settings for functions F5-F8
    private int id;		// throttle id, made from
    //     <ID1> and <ID2> normally identify the throttle controlling the loco

    private int _pcmd;  // hold pcmd and pstat for programmer

    // data members to hold contact with the slot listeners
    final private Vector slotListeners = new Vector();

    public synchronized void addSlotListener(SlotListener l) {
        // add only if not already registered
        if (!slotListeners.contains(l)) {
            slotListeners.addElement(l);
        }
    }

    public synchronized void removeSlotListener(SlotListener l) {
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
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" SlotListeners");
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            SlotListener client = (SlotListener) v.elementAt(i);
            client.notifyChangedSlot(this);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetSlot.class.getName());
}


/* @(#)LocoNetSlot.java */
