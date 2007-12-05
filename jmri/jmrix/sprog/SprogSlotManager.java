/* SprogSlotManager.java */

package jmri.jmrix.sprog;

import jmri.CommandStation;
import jmri.NmraPacket;

import java.util.Vector;
import jmri.jmrix.sprog.*;
import jmri.jmrix.sprog.sprogslotmon.*;

/**
 * Controls a collection of slots, acting as a soft command station for SPROG
 * <P>
 * A SlotListener can register to hear changes. By registering here, the SlotListener
 * is saying that it wants to be notified of a change in any slot.  Alternately,
 * the SlotListener can register with some specific slot, done via the SprogSlot
 * object itself.
 * <P>
 * This Programmer implementation is single-user only. It's not clear whether
 * the command stations can have multiple programming requests outstanding
 * (e.g. service mode and ops mode, or two ops mode) at the same time, but this
 * code definitely can't.
 * <P>
 * @author	Bob Jacobsen  Copyright (C) 2001, 2003
 *              Andrew Crosland         (C) 2006 ported to SPROG
 * @version     $Revision: 1.1 $
 */
public class SprogSlotManager extends SprogCommandStation implements SprogListener, CommandStation, Runnable {

    public SprogSlotManager() {
        // error if more than one constructed?
        if (self != null) log.debug("Creating too many SlotManager objects");
        SprogTrafficController.instance().addSprogListener(this);
    }

    /**
     * The SPROG implementation has one queue for both loco refresh packets and
     * temporary accessory and function packets.
     *
     * Information on slot state is stored in a SprogQueue object.
     * This is declared final because we never need to modify the queue
     * itself, just its contents.
     */
    final private SprogQueue _Queue = new SprogQueue();

    /**
     * Send a DCC packet to the rails. This implements the CommandStation interface.
     *
     * @param packet
     */
    public void sendPacket(byte[] packet) {
        if (packet.length<=1) log.error("Invalid DCC packet length: "+packet.length);
        if (packet.length>=7) log.error("Only 6-byte packets accepted: "+packet.length);
        log.debug("Send packet length "+packet.length);

        SprogMessage m = new SprogMessage(1+(packet.length*3));
        int i = 0; // counter of byte in output message
        int j = 0; // counter of byte in input packet

        m.setElement(i++, 'O');  // "O " starts output packet

        // add each byte of the input message
        for (j=0; j<packet.length; j++) {
            m.setElement(i++,' ');
            String s = Integer.toHexString((int)packet[j]&0xFF).toUpperCase();
            if (s.length() == 1) {
                m.setElement(i++, '0');
                m.setElement(i++, s.charAt(0));
            } else {
                m.setElement(i++, s.charAt(0));
                m.setElement(i++, s.charAt(1));
            }
        }

        SprogTrafficController.instance().sendSprogMessage(m, this);
    }

    /**
     * Access the information in a specific slot.
     * @param i  Specific slot, counted starting from zero.
     * @return   The SprogSlot object
     */
    public SprogSlot slot(int i) {return _Queue.slot(i);}

    public void function0Through4Packet(int address,
                                        boolean f0, boolean f1, boolean f2,
                                        boolean f3, boolean f4) {
      byte[] payload = jmri.NmraPacket.function0Through4Packet(address,
          (address >= SprogConstants.LONG_START), f0, f1, f2, f3, f4);
      int s = _Queue.add(address, payload, SprogConstants.S_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void function5Through8Packet(int address,
                                        boolean f5, boolean f6,
                                        boolean f7, boolean f8) {
      byte[] payload = jmri.NmraPacket.function5Through8Packet(address,
          (address >= SprogConstants.LONG_START), f5, f6, f7, f8);
      int s = _Queue.add(address, payload, SprogConstants.S_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void function9Through12Packet(int address,
                                         boolean f9, boolean f10,
                                         boolean f11, boolean f12) {
      byte[] payload = jmri.NmraPacket.function9Through12Packet(address,
          (address >= SprogConstants.LONG_START), f9, f10, f11, f12);
      int s = _Queue.add(address, payload, SprogConstants.S_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void speedStep128Packet(int address, int spd, boolean isForward) {
      byte[] payload = jmri.NmraPacket.speedStep128Packet(address,
          (address >= SprogConstants.LONG_START), spd, isForward);
      int s = _Queue.findReplace(address, spd, isForward, payload, -1);
      if (s>=0) { notify(slot(s)); }
    }

    public void opsModepacket(int mAddress, boolean mLongAddr, int cv, int val) {
      byte[] payload = NmraPacket.opsCvWriteByte(mAddress, mLongAddr, cv, val );
      int s = _Queue.add(mAddress, payload, 1000 + SprogConstants.OPS_REPEATS);
      if (s>=0) { notify(slot(s)); }
    }

    public void release(int address) {
      int s = _Queue.release(address);
      if (s>=0) {
        notify(slot(s));
      } else {
        log.error("Release for address not in queue"+address);
      }
    }

    /**
     * Send emergency stop to all slots
     */
    public void estopAll() {
    for (int slotNum=0; slotNum<SprogConstants.MAX_SLOTS; slotNum++) {
        SprogSlot s = slot(slotNum);
        if ((s.getRepeat() == -1)
            && s.slotStatus() != SprogConstants.SLOT_FREE
            && s.speed() != 1) {
          estopSlot(slotNum);
        }
      }
    }

    /**
     * Send emergency stop to a slot
     *
     * @param slotNum int
     */
    public void estopSlot(int slotNum) {
      SprogSlot s = slot(slotNum);
      log.debug("Estop slot: "+slotNum+" for address: "+s.locoAddr());
      // Generate a new packet with speed step 1
      byte[] payload = jmri.NmraPacket.speedStep128Packet(s.locoAddr(),
          (s.locoAddr() >= SprogConstants.LONG_START), 1, s.isForward());
      // Replace existing slot
      _Queue.findReplace(s.locoAddr(), 1, s.isForward(), payload, -1);
      notify(s);
    }

    /**
     * method to find the existing SlotManager object, if need be creating one
     */
    static public final SprogSlotManager instance() {
        if (self == null) {
          log.debug("creating a new SprogSlotManager object");
          self = new SprogSlotManager();
        }
        return self;
    }
    static private SprogSlotManager self = null;

    // data members to hold contact with the slot listeners
    final private Vector slotListeners = new Vector();

    public synchronized void addSlotListener(SprogSlotListener l) {
        // add only if not already registered
        slotListeners.addElement(l);
    }

    public synchronized void removeSlotListener(SprogSlotListener l) {
        slotListeners.removeElement(l);
    }

    /**
     * Trigger the notification of all SlotListeners.
     * @param s The changed slot to notify.
     */
    protected void notify(SprogSlot s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this)
            {
                v = (Vector) slotListeners.clone();
            }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" SlotListeners about slot for address"
                                            +s.getAddr());
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            SprogSlotListener client = (SprogSlotListener) v.elementAt(i);
            client.notifyChangedSlot(s);
        }
    }

    /**
     * Loop here sending packets to the rails
     */
    private volatile boolean replyReceived;
    private volatile boolean awaitingReply;
    private int statusDue = 0;
    public void run() {
      log.debug("Slot thread starts");
      byte [] p;
      int [] statusA = new int [4];
      int statusIdx = 0;
      int state = 0;
      SprogMessage m = SprogMessage.getStatus();
      while (true) {
        // loop permanently but sleep
        try {
          Thread.sleep(3);
        } catch (InterruptedException i) {
          log.error("Sprog slot thread interrupted\n"+i);
        }
        switch(state) {
          case 0: {
            log.debug("Slot thread state 0");
            // Get next packet to send
            p = getNextPacket();
            if (p != null) {
              sendPacket(p);
              synchronized(this) {
                  replyReceived = false;
                  awaitingReply = true;
              }
              state = 1;
            }
            break;
          }
          case 1: {
            log.debug("Slot thread state 1");
            // Wait for reply
            if (replyReceived) {
              if (++statusDue > 20) {
                state = 2;
              } else {
                state = 0;
              }
            }
            break;
          }
          case 2: {
            log.debug("Slot thread state 2");
            // Send status request
            SprogTrafficController.instance().sendSprogMessage(m, this);
            log.debug("Send status request");
            synchronized(this) {
              replyReceived = false;
              awaitingReply = true;
            }
            statusDue = 0;
            state = 3;
            break;
          }
          case 3: {
            log.debug("Slot thread state 3");
            // Waiting for status reply
            if (replyReceived) {
              if (SprogSlotMonFrame.instance() != null) {
                String s = replyForMe.toString();
                log.debug("Reply received whilst waiting for status");
                int i = s.indexOf('h');
//                float volts = Integer.decode("0x"+s.substring(i+1, i+5)).intValue();
                int milliAmps = ((Integer.decode("0x"+s.substring(i+7, i+11)).intValue())*488)/47;
                statusA[statusIdx] = milliAmps;
                statusIdx = (statusIdx+1)%4;
                String voltString, ampString;
                ampString = Float.toString((float)((statusA[0] + statusA[1] + statusA[2] + statusA[3])/4)/1000);
                SprogSlotMonFrame.instance().updateStatus(ampString);
              }
              state = 0;
              break;
            }
          }
        }
      }
    }

    private int currentQ = 0;
    private int oldQ = 0;

    /**
     * Get the next packet to be transmitted. returns length 1 if no packet
     *
     * @return byte[]
     */
    private byte [] getNextPacket() {
      boolean foundQ = true;
      SprogSlot s;
      byte [] p;
      int rep;
      oldQ = currentQ;
      while (_Queue.slot(currentQ).slotStatus() == SprogConstants.SLOT_FREE) {
        currentQ++;
        currentQ = currentQ%SprogConstants.MAX_SLOTS;
        if (currentQ == oldQ) {
          return null;
        }
      }
      s = _Queue.slot(currentQ);
      p = s.getPayload();
      rep = s.getRepeat();
      if (rep < 1000) {
        // If it's not an ops mode packet step to next slot, otherwise we
        // repeat ops mode packets until exhausted
        currentQ++;
        currentQ = currentQ % SprogConstants.MAX_SLOTS;
      }
      if (s.getRepeat() != -1) {
        // Repeating accessory slot
        if ((s.doRepeat()%1000) == 0) {
          // exhausted
          s.clear();
          notify(s);
        }
      }
      return p;
    }

    /*
     * Needs to listen to replies
     * Need to implement asynch replies for overload & notify power manager
     *
     * How does POM work??? how does programmer send packets??
     */







    public void message(SprogMessage m) {
        log.error("message received unexpectedly: "+m.toString());
    }

    private SprogReply replyForMe;

    public void reply(SprogReply m) {
      replyForMe = m;
      log.debug("reply received: "+m.toString());
      if (m.isUnsolicited() && m.isOverload()){
        log.error("Overload");

        // *** turn power off

      }
      if (awaitingReply) {
        synchronized(this) {
          replyReceived = true;
          awaitingReply = false;
        }
      }
    }

    /**
     * Provide a snapshot of the slots in use
     */
    public int getInUseCount() {
        int result = 0;
        for (int i = 0; i<=SprogConstants.MAX_SLOTS; i++) {
// ???            if (slotQ(i).slotStatus() == SprogConstants.LOCO_IN_USE ) result++;
        }
        return result;
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogSlotManager.class.getName());
}


/* @(#)SprogSlotManager.java */
