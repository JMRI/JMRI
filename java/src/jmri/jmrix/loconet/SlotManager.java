package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import jmri.CommandStation;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls a collection of slots, acting as the counter-part of a LocoNet
 * command station.
 * <p>
 * A SlotListener can register to hear changes. By registering here, the
 * SlotListener is saying that it wants to be notified of a change in any slot.
 * Alternately, the SlotListener can register with some specific slot, done via
 * the LocoNetSlot object itself.
 * <p>
 * Strictly speaking, functions 9 through 28 are not in the actual slot, but
 * it's convenient to imagine there's an "extended slot" and keep track of them
 * here. This is a partial implementation, though, because setting is still done
 * directly in {@link LocoNetThrottle}. In particular, if this slot has not been
 * read from the command station, the first message directly setting F9 through
 * F28 will not have a place to store information. Instead, it will trigger a
 * slot read, so the following messages will be properly handled.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <p>
 * This Programmer implementation is single-user only. It's not clear whether
 * the command stations can have multiple programming requests outstanding (e.g.
 * service mode and ops mode, or two ops mode) at the same time, but this code
 * definitely can't.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author B. Milhaupt, Copyright (C) 2018
 */
public class SlotManager extends AbstractProgrammer implements LocoNetListener, CommandStation {

    /**
     * Time to wait after programming operation complete on LocoNet
     * before reporting completion and hence starting next operation
     */
    static public int postProgDelay = 100; // this is public to allow changes via script

    /**
     * Constructor for a SlotManager on a given TrafficController.
     *
     * @param tc Traffic Controller to be used by SlotManager for communication
     *          with LocoNet
     */
    public SlotManager(LnTrafficController tc) {
        this.tc = tc;
        // change timeout values from AbstractProgrammer superclass
        LONG_TIMEOUT = 180000;  // Fleischmann command stations take forever
        SHORT_TIMEOUT = 8000;   // DCS240 reads

        loadSlots();

        // listen to the LocoNet
        tc.addLocoNetListener(~0, this);

        // We will scan the slot table every 10 s for in-use slots that are stale
        final int slotScanDelay = 10000; // 10 seconds; must be less than 90, see checkStaleSlots()
        staleSlotCheckTimer = new javax.swing.Timer(slotScanDelay, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkStaleSlots();
            }
        }
        );

        staleSlotCheckTimer.setRepeats(true);
        staleSlotCheckTimer.setInitialDelay(3*slotScanDelay);  // wait a bit more at startup
        staleSlotCheckTimer.start();
    }

    /**
     * Initialize the slots array.
     */
    protected void loadSlots() {
        // initialize slot array
        for (int i = 0; i < NUM_SLOTS; i++) {
            _slots[i] = new LocoNetSlot(i);
        }
    }

    protected LnTrafficController tc;

    /**
     * Send a DCC packet to the rails. This implements the CommandStation
     * interface.  This mechanism can pass any valid NMRA packet of up to
     * 6 data bytes (including the error-check byte).
     *
     * When available, these messages are forwarded to LocoNet using a
     * "throttledTransmitter".  This decreases the speed with which these
     * messages are sent, resulting in lower throughput, but fewer
     * rejections by the command station on account of "buffer-overflow".
     *
     * @param packet  the data bytes of the raw NMRA packet to be sent.  The
     *          "error check" byte must be included, even though the LocoNet
     *          message will not include that byte; the command station
     *          will re-create the error byte from the bytes encoded in
     *          the LocoNet message.  LocoNet is unable to propagate packets
     *          longer than 6 bytes (including the error-check byte).
     *
     * @param sendCount  the total number of times the packet is to be
     *          sent on the DCC track signal (not LocoNet!).  Valid range is
     *          between 1 and 8.  sendCount will be forced to this range if it
     *          is outside of this range.
     */
    @Override
    public boolean sendPacket(byte[] packet, int sendCount) {
        if (sendCount > 8) {
            log.warn("Ops Mode Accessory Packet 'Send count' reduced from {} to 8.", sendCount); // NOI18N
            sendCount = 8;
        }
        if (sendCount < 1) {
            log.warn("Ops Mode Accessory Packet 'Send count' of {} is illegal and is forced to 1.", sendCount); // NOI18N
            sendCount = 1;
        }
        if (packet.length <= 1) {
            log.error("Invalid DCC packet length: {}", packet.length); // NOI18N
        }
        if (packet.length > 6) {
            log.error("DCC packet length is too great: {} bytes were passed; ignoring the request. ", packet.length); // NOI18N
        }

        LocoNetMessage m = new LocoNetMessage(11);
        m.setElement(0, LnConstants.OPC_IMM_PACKET);
        m.setElement(1, 0x0B);
        m.setElement(2, 0x7F);
        // the incoming packet includes a check byte that's not included in LocoNet packet
        int length = packet.length - 1;

        m.setElement(3, ((sendCount - 1) & 0x7) + 16 * (length & 0x7));

        int highBits = 0;
        if (length >= 1 && ((packet[0] & 0x80) != 0)) {
            highBits |= 0x01;
        }
        if (length >= 2 && ((packet[1] & 0x80) != 0)) {
            highBits |= 0x02;
        }
        if (length >= 3 && ((packet[2] & 0x80) != 0)) {
            highBits |= 0x04;
        }
        if (length >= 4 && ((packet[3] & 0x80) != 0)) {
            highBits |= 0x08;
        }
        if (length >= 5 && ((packet[4] & 0x80) != 0)) {
            highBits |= 0x10;
        }
        m.setElement(4, highBits);

        m.setElement(5, 0);
        m.setElement(6, 0);
        m.setElement(7, 0);
        m.setElement(8, 0);
        m.setElement(9, 0);
        for (int i = 0; i < packet.length - 1; i++) {
            m.setElement(5 + i, packet[i] & 0x7F);
        }

        if (throttledTransmitter != null) {
            throttledTransmitter.sendLocoNetMessage(m);
        } else {
            tc.sendLocoNetMessage(m);
        }
        return true;
    }

    final static protected int NUM_SLOTS = 128;
    /**
     * Information on slot state is stored in an array of LocoNetSlot objects.
     * This is declared final because we never need to modify the array itself,
     * just its contents.
     */
    final protected LocoNetSlot _slots[] = new LocoNetSlot[NUM_SLOTS];

    /**
     * Access the information in a specific slot. Note that this is a mutable
     * access, so that the information in the LocoNetSlot object can be changed.
     *
     * @param i Specific slot, counted starting from zero.
     * @return The Slot object
     */
    public LocoNetSlot slot(int i) {
        return _slots[i];
    }

    /**
     * Obtain a slot for a particular loco address.
     * <p>
     * This requires access to the command station, even if the locomotive
     * address appears in the current contents of the slot array, to ensure that
     * our local image is up-to-date.
     * <p>
     * This method sends an info request. When the echo of this is returned from
     * the LocoNet, the next slot-read is recognized as the response.
     * <p>
     * The object that's looking for this information must provide a
     * SlotListener to notify when the slot ID becomes available.
     * <p>
     * The SlotListener is not subscribed for slot notifications; it can do that
     * later if it wants. We don't currently think that's a race condition.
     *
     * @param i Specific slot, counted starting from zero.
     * @param l The SlotListener to notify of the answer.
     */
    public void slotFromLocoAddress (int i, SlotListener l) {
        // store connection between this address and listener for later
        mLocoAddrHash.put(Integer.valueOf(i), l);

        // send info request
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(LnConstants.OPC_LOCO_ADR);  // OPC_LOCO_ADR
        m.setElement(1, (i / 128) & 0x7F);
        m.setElement(2, i & 0x7F);
        tc.sendLocoNetMessage(m);
    }

    javax.swing.Timer staleSlotCheckTimer = null;

    /**
     * Scan the slot array looking for slots that are in-use or common but have
     * not had any updates in over 90s and issue a read slot request to update
     * their state as the command station may have purged or stopped updating
     * the slot without telling us via a LocoNet message.
     * <p>
     * This is intended to be called from the staleSlotCheckTimer
     */
    private void checkStaleSlots() {
        long staleTimeout = System.currentTimeMillis() - 90000;  // 90 seconds ago
        LocoNetSlot slot;

        // We will just check the normal loco slots 1 to 120
        for (int i = 1; i <= 120; i++) {
            slot = _slots[i];
            if ((slot.slotStatus() == LnConstants.LOCO_IN_USE || slot.slotStatus() == LnConstants.LOCO_COMMON)
                    && (slot.getLastUpdateTime() <= staleTimeout)) {
                sendReadSlot(i);
            }
        }
    }

    /**
     * Provide a mapping between locomotive addresses and the SlotListener
     * that's interested in them.
     */
    Hashtable<Integer, SlotListener> mLocoAddrHash = new Hashtable<>();

    // data members to hold contact with the slot listeners
    final private Vector<SlotListener> slotListeners = new Vector<>();

    /**
     * Add a slot listener, if it is not already registered
     * <p>
     * The slot listener will be invoked every time a slot changes state.
     *
     * @param l Slot Listener to be added
     */
    public synchronized void addSlotListener(SlotListener l) {
        // add only if not already registered
        if (!slotListeners.contains(l)) {
            slotListeners.addElement(l);
        }
    }

    /**
     * Add a slot listener, if it is registered.
     * <p>
     * The slot listener will be removed from the list of listeners which are
     * invoked whenever a slot changes state.
     *
     * @param l Slot Listener to be removed
     */
    public synchronized void removeSlotListener(SlotListener l) {
        if (slotListeners.contains(l)) {
            slotListeners.removeElement(l);
        }
    }

    /**
     * Trigger the notification of all SlotListeners.
     *
     * @param s The changed slot to notify.
     */
    @SuppressWarnings("unchecked")
    protected void notify(LocoNetSlot s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<SlotListener> v;
        synchronized (this) {
            v = (Vector<SlotListener>) slotListeners.clone();
        }
        log.debug("notify {} SlotListeners about slot {}", // NOI18N
                v.size(), s.getSlot());
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            SlotListener client = v.elementAt(i);
            client.notifyChangedSlot(s);
        }
    }

    LocoNetMessage immedPacket;

    /**
     * Listen to the LocoNet. This is just a steering routine, which invokes
     * others for the various processing steps.
     *
     * @param m incoming message
     */
    @Override
    public void message(LocoNetMessage m) {
        if (m.getOpCode() == LnConstants.OPC_RE_LOCORESET_BUTTON) {
            if (commandStationType.getSupportsLocoReset()) {
                // Command station LocoReset button was triggered.
                //
                // Note that sending a LocoNet message using this OpCode to the command
                // station does _not_ seem to trigger the equivalent effect; only
                // pressing the button seems to do so.
                // If the OpCode is received by JMRI, regardless of its source,
                // JMRI will simply trigger a re-read of all slots.  This will
                // allow the JMRI slots to stay consistent with command station
                // slot information, regardless of whether the command station
                // just modified the slot information.
                javax.swing.Timer t = new javax.swing.Timer(500, (java.awt.event.ActionEvent e) -> {
                    log.debug("Updating slots account received opcode 0x8a message");   // NOI18N
                    update();
                });
                t.stop();
                t.setInitialDelay(500);
                t.setRepeats(false);
                t.start();
            }
            return;
        }

        // LACK processing for resend of immediate command
        if (!mTurnoutNoRetry && immedPacket != null &&
                m.getOpCode() == LnConstants.OPC_LONG_ACK &&
                m.getElement(1) == 0x6D && m.getElement(2) == 0x00) {
            // LACK reject, resend immediately
            tc.sendLocoNetMessage(immedPacket);
            immedPacket = null;
        }
        if (m.getOpCode() == LnConstants.OPC_IMM_PACKET &&
                m.getElement(1) == 0x0B && m.getElement(2) == 0x7F) {
            immedPacket = m;
        } else {
            immedPacket = null;
        }

        // slot specific message?
        int i = findSlotFromMessage(m);
        if (i != -1) {
            forwardMessageToSlot(m, i);
            respondToAddrRequest(m, i);
            programmerOpMessage(m, i);
        }

        // LONG_ACK response?
        if (m.getOpCode() == LnConstants.OPC_LONG_ACK) {
            handleLongAck(m);
        }

        // see if extended function message
        if (isExtFunctionMessage(m)) {
            // yes, get address
            int addr = getDirectFunctionAddress(m);
            // find slot(s) containing this address
            // and route message to them
            boolean found = false;
            for (int j = 0; j < 120; j++) {
                LocoNetSlot slot = slot(j);
                if (slot == null) {
                    continue;
                }
                if ((slot.locoAddr() != addr)
                        || (slot.slotStatus() == LnConstants.LOCO_FREE)) {
                    continue;
                }
                // found!
                slot.functionMessage(getDirectDccPacket(m));
                found = true;
            }
            if (!found) {
                // rats! Slot not loaded since program start.  Request it be
                // reloaded for later, but that'll be too late
                // for this one.
                LocoNetMessage mo = new LocoNetMessage(4);
                mo.setOpCode(LnConstants.OPC_LOCO_ADR);  // OPC_LOCO_ADR
                mo.setElement(1, (addr / 128) & 0x7F);
                mo.setElement(2, addr & 0x7F);
                tc.sendLocoNetMessage(mo);
            }
        }
    }

    /**
     * Checks a LocoNet message to see if it encodes a DCC "direct function" packet.
     *
     * @param m  a LocoNet Message
     * @return the loco address if the LocoNet message encodes a "direct function" packet,
     * else returns -1
     */
    int getDirectFunctionAddress(LocoNetMessage m) {
        if (m.getOpCode() != LnConstants.OPC_IMM_PACKET) {
            return -1;
        }
        if (m.getElement(1) != 0x0B) {
            return -1;
        }
        if (m.getElement(2) != 0x7F) {
            return -1;
        }
        // Direct packet, check length
        if ((m.getElement(3) & 0x70) < 0x20) {
            return -1;
        }
        int addr = -1;
        // check long address
        if ((m.getElement(5) & 0x40) != 0) {
            addr = (m.getElement(5) & 0x3F) * 256 + (m.getElement(6) & 0xFF);
            if ((m.getElement(4) & 0x02) != 0) {
                addr += 128;  // and high bit
            }
        } else {
            addr = (m.getElement(5) & 0xFF);
            if ((m.getElement(4) & 0x01) != 0) {
                addr += 128;  // and high bit
            }
        }
        return addr;
    }

    /**
     * Extracts a DCC "direct packet" from a LocoNet message, if possible.
     * <p>
     * if this is a direct DCC packet, return as one long
     * else return -1. Packet does not include address bytes.
     *
     * @param m a LocoNet message to be inspected
     * @return an integer containing the bytes of the DCC packet, except the address bytes.
     */
    int getDirectDccPacket(LocoNetMessage m) {
        if (m.getOpCode() != LnConstants.OPC_IMM_PACKET) {
            return -1;
        }
        if (m.getElement(1) != 0x0B) {
            return -1;
        }
        if (m.getElement(2) != 0x7F) {
            return -1;
        }
        // Direct packet, check length
        if ((m.getElement(3) & 0x70) < 0x20) {
            return -1;
        }
        int result = 0;
        int n = (m.getElement(3) & 0xF0) / 16;
        int start;
        int high = m.getElement(4);
        // check long or short address
        if ((m.getElement(5) & 0x40) != 0) {
            start = 7;
            high = high >> 2;
            n = n - 2;
        } else {
            start = 6;
            high = high >> 1;
            n = n - 1;
        }
        // get result
        for (int i = 0; i < n; i++) {
            result = result * 256 + (m.getElement(start + i) & 0x7F);
            if ((high & 0x01) != 0) {
                result += 128;
            }
            high = high >> 1;
        }
        return result;
    }

    /**
     * Determines if a LocoNet message encodes a direct request to control
     * DCC functions F9 thru F28
     *
     * @param m the LocoNet message to be evaluated
     * @return true if the message is an external DCC packet request for F9-F28,
     *      else false.
     */
    boolean isExtFunctionMessage(LocoNetMessage m) {
        int pkt = getDirectDccPacket(m);
        if (pkt < 0) {
            return false;
        }
        // check F9-12
        if ((pkt & 0xFFFFFF0) == 0xA0) {
            return true;
        }
        // check F13-28
        if ((pkt & 0xFFFFFE00) == 0xDE00) {
            return true;
        }
        return false;
    }

    /**
     * Extracts the LocoNet slot number from a LocoNet message, if possible.
     * <p>
     * Find the slot number that a message references
     * <p>
     * This routine only looks for explicit slot references; it does not, for example,
     * identify a loco address in the message and then work thru the slots to find a
     * slot which references that loco address.
     *
     * @param m LocoNet Message to be inspected
     * @return an integer representing the slot number encoded in the LocoNet
     *          message, or -1 if the message does not contain a slot reference
     */
    public int findSlotFromMessage(LocoNetMessage m) {

        int i = -1;  // find the slot index in the message and store here

        // decode the specific message type and hence slot number
        switch (m.getOpCode()) {
            case LnConstants.OPC_WR_SL_DATA:
            case LnConstants.OPC_SL_RD_DATA:
                i = m.getElement(2);
                break;

            case LnConstants.OPC_LOCO_DIRF:
            case LnConstants.OPC_LOCO_SND:
            case LnConstants.OPC_LOCO_SPD:
            case LnConstants.OPC_SLOT_STAT1:
                i = m.getElement(1);
                break;

            case LnConstants.OPC_MOVE_SLOTS:  // No follow on for some moves
                if (m.getElement(1) != 0) {
                    i = m.getElement(1);
                    return i;
                }
                break;
            default:
                // nothing here for us
                return i;
        }
        // break gets to here
        return i;
    }

    /**
     * Check CV programming LONG_ACK message byte 1
     * <p>
     * The following methods are for parsing LACK as response to CV programming.
     * It is divided into numerous small methods so that each bit can be
     * overridden for special parsing for individual command station types.
     *
     * @param Byte1 from the LocoNet message
     * @return true if Byte1 encodes a response to a OPC_SL_WRITE or an
     *          Expanded Slot Write
     */
    protected boolean checkLackByte1(int Byte1) {
        if ((Byte1 & 0xEF) == 0x6F) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the status byte of an OPC_LONG_ACK when performing CV programming
     * operations.
     *
     * @param Byte2 status byte
     * @return True if status byte indicates acceptance of the command, else false.
     */
    protected boolean checkLackTaskAccepted(int Byte2) {
        if (Byte2 == 1 // task accepted
                || Byte2 == 0x23 || Byte2 == 0x2B || Byte2 == 0x6B// added as DCS51 fix
                || Byte2 == 0x7F) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the OPC_LONG_ACK status byte response to a programming
     * operation.
     *
     * @param Byte2 from the OPC_LONG_ACK message
     * @return true if the programmer returned "busy" else false
     */
    protected boolean checkLackProgrammerBusy(int Byte2) {
        if (Byte2 == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the OPC_LONG_ACK status byte response to a programming
     * operation to see if the programmer accepted the operation "blindly".
     *
     * @param Byte2 from the OPC_LONG_ACK message
     * @return true if the programmer indicated a "blind operation", else false
     */
    protected boolean checkLackAcceptedBlind(int Byte2) {
        if (Byte2 == 0x40) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handles OPC_LONG_ACK replies to programming slot operations.
     *
     * @param m LocoNet message being analyzed
     */
    protected void handleLongAck(LocoNetMessage m) {
        // handle if reply to slot. There's no slot number in the LACK, unfortunately.
        // If this is a LACK to a Slot op, and progState is command pending,
        // assume its for us...
        log.debug("LACK in state {} message: {}", progState, m.toString()); // NOI18N
        if (checkLackByte1(m.getElement(1)) && progState == 1) {
            // in programming state
            // check status byte
            if (checkLackTaskAccepted(m.getElement(2))) { // task accepted
                // 'not implemented' (op on main)
                // but BDL16 and other devices can eventually reply, so
                // move to commandExecuting state
                log.debug("LACK accepted, next state 2"); // NOI18N
                if ((_progRead || _progConfirm) && mServiceMode) {
                    startLongTimer();
                } else {
                    startShortTimer();
                }
                progState = 2;
            } else if (checkLackProgrammerBusy(m.getElement(2))) { // task aborted as busy
                // move to not programming state
                progState = 0;
                // notify user ProgListener
                stopTimer();
                notifyProgListenerLack(jmri.ProgListener.ProgrammerBusy);
            } else if (checkLackAcceptedBlind(m.getElement(2))) { // task accepted blind
                if ((_progRead || _progConfirm) && !mServiceMode) { // incorrect Reserved OpSw setting can cause this response to OpsMode Read
                    // just treat it as a normal OpsMode Read response
                    // move to commandExecuting state
                    log.debug("LACK accepted (ignoring incorrect OpSw), next state 2"); // NOI18N
                    startShortTimer();
                    progState = 2;
                } else {
                    // move to not programming state
                    progState = 0;
                    // notify user ProgListener
                    stopTimer();
                    // have to send this in a little while to
                    // allow command station time to execute
                    javax.swing.Timer timer = new javax.swing.Timer(postProgDelay, new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            notifyProgListenerEnd(-1, 0); // no value (e.g. -1), no error status (e.g.0)
                        }
                    });
                    timer.stop();
                    timer.setInitialDelay(postProgDelay);
                    timer.setRepeats(false);
                    timer.start();
                }
            } else { // not sure how to cope, so complain
                log.warn("unexpected LACK reply code {}", m.getElement(2)); // NOI18N
                // move to not programming state
                progState = 0;
                // notify user ProgListener
                stopTimer();
                notifyProgListenerLack(jmri.ProgListener.UnknownError);
            }
        }
    }

    /**
     * Forward Slot-related LocoNet message to the slot.
     *
     * @param m a LocoNet message targeted at a slot
     * @param i the slot number to which the LocoNet message is targeted.
     */
    public void forwardMessageToSlot(LocoNetMessage m, int i) {

        // if here, i holds the slot number, and we expect to be able to parse
        // and have the slot handle the message
        if (i >= _slots.length || i < 0) {
            log.error("Received slot number {} is greater than array length {} Message was {}", // NOI18N
                    i, _slots.length, m.toString()); // NOI18N
            return; // prevents array index out-of-bounds when referencing _slots[i]
        }
        try {
            _slots[i].setSlot(m);
        } catch (LocoNetException e) {
            // must not have been interesting, or at least routed right
            log.error("slot rejected LocoNetMessage {}", m); // NOI18N
            return;
        }
        // notify listeners that slot may have changed
        notify(_slots[i]);
    }

    /**
     * A sort of slot listener which handles loco address requests
     *
     * @param m a LocoNet message
     * @param i the slot to which it is directed
     */
    protected void respondToAddrRequest(LocoNetMessage m, int i) {
        // is called any time a LocoNet message is received.  Note that we do _NOT_ know why a given message happens!

        // if this is OPC_SL_RD_DATA
        if (m.getOpCode() == LnConstants.OPC_SL_RD_DATA) {
            // yes, see if request exists
            // note that the appropriate _slots[] entry has already been updated
            // to reflect the content of the LocoNet message, so _slots[i]
            // has the locomotive address of this request
            int addr = _slots[i].locoAddr();
            log.debug("LOCO_ADR resp is slot {} for addr {}", i, addr); // NOI18N
            SlotListener l = mLocoAddrHash.get(Integer.valueOf(addr));
            if (l != null) {
                // only notify once per request
                mLocoAddrHash.remove(Integer.valueOf(addr));
                // and send the notification
                log.debug("notify listener"); // NOI18N
                l.notifyChangedSlot(_slots[i]);
            } else {
                log.debug("no request for addr {}", addr); // NOI18N
            }
        }
    }

    /**
     * Handle LocoNet messages related to CV programming operations
     *
     * @param m a LocoNet message
     * @param i the slot toward which the message is destined
     */
    protected void programmerOpMessage(LocoNetMessage m, int i) {

        // start checking for programming operations in slot 124
        if (i == 124) {
            // here its an operation on the programmer slot
            log.debug("Prog Message {} for slot 124 in state {}", // NOI18N
                    m.getOpCodeHex(), progState); // NOI18N
            switch (progState) {
                case 0:   // notProgramming
                    break;
                case 1:   // commandPending
                    // we just sit here waiting for a LACK, handled above
                    break;
                case 2:   // commandExecuting
                    // waiting for slot read, is it present?
                    if (m.getOpCode() == LnConstants.OPC_SL_RD_DATA) {
                        log.debug("  was OPC_SL_RD_DATA"); // NOI18N
                        // yes, this is the end
                        // move to not programming state
                        stopTimer();
                        progState = 0;

                        // parse out value returned
                        int value = -1;
                        int status = 0;
                        if (_progConfirm) {
                            // read command, get value; check if OK
                            value = _slots[i].cvval();
                            if (value != _confirmVal) {
                                status = status | jmri.ProgListener.ConfirmFailed;
                            }
                        }
                        if (_progRead) {
                            // read command, get value
                            value = _slots[i].cvval();
                        }
                        // parse out status
                        if ((_slots[i].pcmd() & LnConstants.PSTAT_NO_DECODER) != 0) {
                            status = (status | jmri.ProgListener.NoLocoDetected);
                        }
                        if ((_slots[i].pcmd() & LnConstants.PSTAT_WRITE_FAIL) != 0) {
                            status = (status | jmri.ProgListener.NoAck);
                        }
                        if ((_slots[i].pcmd() & LnConstants.PSTAT_READ_FAIL) != 0) {
                            status = (status | jmri.ProgListener.NoAck);
                        }
                        if ((_slots[i].pcmd() & LnConstants.PSTAT_USER_ABORTED) != 0) {
                            status = (status | jmri.ProgListener.UserAborted);
                        }

                        // and send the notification
                        notifyProgListenerEnd(value, status);
                    }
                    break;
                default:  // error!
                    log.error("unexpected programming state {}", progState); // NOI18N
                    break;
            }
        }
    }

    ProgrammingMode csOpSwProgrammingMode = new ProgrammingMode(
            "LOCONETCSOPSWMODE",
            Bundle.getMessage("LOCONETCSOPSWMODE"));

    // members for handling the programmer interface

    /**
     * Return a list of ProgrammingModes supported by this interface
     * Types implemented here.
     *
     * @return a List of ProgrammingMode objects containing the supported
     *          programming modes.
     */

    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.REGISTERMODE);
        ret.add(ProgrammingMode.ADDRESSMODE);
        ret.add(csOpSwProgrammingMode);

        return ret;
    }

    /**
     * Remember whether the attached command station needs a sequence sent after
     * programming. The default operation is implemented in doEndOfProgramming
     * and turns power back on by sending a GPON message.
     */
    private boolean mProgEndSequence = false;

    /**
     * Remember whether the attached command station can read from Decoders.
     */
    private boolean mCanRead = true;

    /**
     * Determine whether this Programmer implementation is capable of reading
     * decoder contents. This is entirely determined by the attached command
     * station, not the code here, so it refers to the mCanRead member variable
     * which is recording the known state of that.
     *
     * @return True if reads are possible
     */
    @Override
    public boolean getCanRead() {
        return mCanRead;
    }

    /**
     * Return the write confirm mode implemented by the command station.
     * <p>
     * Service mode always checks for DecoderReply. (The DCS240 also seems to do
     * ReadAfterWrite, but that's not fully understood yet)
     *
     * @param addr This implementation ignores this parameter
     * @return the supported WriteConfirmMode
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) { return WriteConfirmMode.DecoderReply; }

    /**
     * Set the command station type to one of the known types in the
     * {@link LnCommandStationType} enum.
     *
     * @param value contains the command station type
     */
    public void setCommandStationType(LnCommandStationType value) {
        commandStationType = value;
        mCanRead = value.getCanRead();
        mProgEndSequence = value.getProgPowersOff();
    }

    LocoNetThrottledTransmitter throttledTransmitter = null;
    boolean mTurnoutNoRetry = false;

    /**
     * Provide a ThrottledTransmitter for sending immediate packets.
     *
     * @param value contains a LocoNetThrottledTransmitter object
     * @param m contains a boolean value indicating mTurnoutNoRetry
     */
    public void setThrottledTransmitter(LocoNetThrottledTransmitter value, boolean m) {
        throttledTransmitter = value;
        mTurnoutNoRetry = m;
    }

    /**
     * Get the command station type.
     *
     * @return an LnCommandStationType object
     */
    public LnCommandStationType getCommandStationType() {
        return commandStationType;
    }

    protected LnCommandStationType commandStationType = null;

    /**
     * Internal routine to handle a timeout.
     */
    @Override
    synchronized protected void timeout() {
        log.debug("timeout fires in state {}", progState); // NOI18N

        if (progState != 0) {
            // we're programming, time to stop
            log.debug("timeout while programming"); // NOI18N

            // perhaps no communications present? Fail back to end of programming
            progState = 0;
            // and send the notification; error code depends on state
            if (progState == 2 && !mServiceMode) { // ops mode command executing,
                // so did talk to command station at first
                notifyProgListenerEnd(_slots[124].cvval(), jmri.ProgListener.NoAck);
            } else {
                // all others
                notifyProgListenerEnd(_slots[124].cvval(), jmri.ProgListener.FailedTimeout);
                // might be leaving power off, but that's currently up to user to fix
            }
        }
    }

    int progState = 0;
    // 1 is commandPending
    // 2 is commandExecuting
    // 0 is notProgramming
    boolean _progRead = false;
    boolean _progConfirm = false;
    int _confirmVal;
    boolean mServiceMode = true;

    /**
     * Write a CV via Ops Mode programming.
     *
     * @param CVname CV number
     * @param val value to write to the CV
     * @param p programmer
     * @param addr address of decoder
     * @param longAddr true if the address is a long address
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    public void writeCVOpsMode(String CVname, int val, jmri.ProgListener p,
            int addr, boolean longAddr) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        lopsa = addr & 0x7f;
        hopsa = (addr / 128) & 0x7f;
        mServiceMode = false;
        doWrite(CV, val, p, 0x67);  // ops mode byte write, with feedback
    }

    /**
     * Write a CV via the Service Mode programmer.
     *
     * @param cvNum CV id as String
     * @param val value to write to the CV
     * @param p programmer
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    @Override
    public void writeCV(String cvNum, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("writeCV(string): cvNum={}, value={}", cvNum, val);
        if (getMode().equals(csOpSwProgrammingMode)) {
            log.debug("cvOpSw mode write!");
            // handle Command Station OpSw programming here
            String[] parts = cvNum.split("\\.");
            if ((parts[0].equals("csOpSw")) && (parts.length==2)) {
                if (csOpSwAccessor == null) {
                    csOpSwAccessor = new CsOpSwAccess(adaptermemo, p);
                } else {
                    csOpSwAccessor.setProgrammerListener(p);
                }
                // perform the CsOpSwMode read access
                log.debug("going to try the opsw access");
                csOpSwAccessor.writeCsOpSw(cvNum, val, p);
                return;

            } else {
                log.warn("rejecting the cs opsw access account unsupported CV name format");
                // unsupported format in "cv" name. Signal an error
                notifyProgListenerEnd(p, 1, ProgListener.SequenceError);
                return;

            }
        } else {
            // regular CV case
            int CV = Integer.parseInt(cvNum);

            lopsa = 0;
            hopsa = 0;
            mServiceMode = true;
            // parse the programming command
            int pcmd = 0x43;       // LPE imples 0x40, but 0x43 is observed
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                pcmd = pcmd | 0x20;
            } else if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                pcmd = pcmd | 0x28;
            } else if (getMode().equals(ProgrammingMode.REGISTERMODE)
                    || getMode().equals(ProgrammingMode.ADDRESSMODE)) {
                pcmd = pcmd | 0x10;
            } else {
                throw new jmri.ProgrammerException("mode not supported"); // NOI18N
            }

            doWrite(CV, val, p, pcmd);
        }
    }

    /**
     * Perform a write a CV via the Service Mode programmer.
     *
     * @param CV CV number
     * @param val value to write to the CV
     * @param p programmer
     * @param pcmd programming command
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    public void doWrite(int CV, int val, jmri.ProgListener p, int pcmd) throws jmri.ProgrammerException {
        log.debug("writeCV: {}", CV); // NOI18N

        stopEndOfProgrammingTimer();  // still programming, so no longer waiting for power off

        useProgrammer(p);
        _progRead = false;
        _progConfirm = false;
        // set commandPending state
        progState = 1;

        // format and send message
        startShortTimer();
        tc.sendLocoNetMessage(progTaskStart(pcmd, val, CV, true));
    }

    /**
     * Confirm a CV via the OpsMode programmer.
     *
     * @param CVname a String containing the CV name
     * @param val expected value
     * @param p programmer
     * @param addr address of loco to write to
     * @param longAddr true if addr is a long address
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    public void confirmCVOpsMode(String CVname, int val, jmri.ProgListener p,
            int addr, boolean longAddr) throws jmri.ProgrammerException {
        int CV = Integer.parseInt(CVname);
        lopsa = addr & 0x7f;
        hopsa = (addr / 128) & 0x7f;
        mServiceMode = false;
        doConfirm(CV, val, p, 0x2F);  // although LPE implies 0x2C, 0x2F is observed
    }

    /**
     * Confirm a CV via the Service Mode programmer.
     *
     * @param CVname a String containing the CV name
     * @param val expected value
     * @param p programmer
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    @Override
    public void confirmCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        int CV = Integer.parseInt(CVname);
        lopsa = 0;
        hopsa = 0;
        mServiceMode = true;
        if (getMode().equals(csOpSwProgrammingMode)) {
            log.debug("cvOpSw mode!");
            //handle Command Station OpSw programming here
            String[] parts = CVname.split("\\.");
            if ((parts[0].equals("csOpSw")) && (parts.length==2)) {
                if (csOpSwAccessor == null) {
                    csOpSwAccessor = new CsOpSwAccess(adaptermemo, p);
                } else {
                    csOpSwAccessor.setProgrammerListener(p);
                }
                // perform the CsOpSwMode read access
                log.debug("going to try the opsw access");
                csOpSwAccessor.readCsOpSw(CVname, p);
                return;
            } else {
                log.warn("rejecting the cs opsw access account unsupported CV name format");
                // unsupported format in "cv" name.  Signal an error.
                notifyProgListenerEnd(p, 1, ProgListener.SequenceError);
                return;
            }
        }

        // parse the programming command
        int pcmd = 0x03;       // LPE imples 0x00, but 0x03 is observed
        if (getMode().equals(ProgrammingMode.PAGEMODE)) {
            pcmd = pcmd | 0x20;
        } else if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            pcmd = pcmd | 0x28;
        } else if (getMode().equals(ProgrammingMode.REGISTERMODE)
                || getMode().equals(ProgrammingMode.ADDRESSMODE)) {
            pcmd = pcmd | 0x10;
        } else {
            throw new jmri.ProgrammerException("mode not supported"); // NOI18N
        }

        doConfirm(CV, val, p, pcmd);
    }

    /**
     * Perform a confirm operation of a CV via the Service Mode programmer.
     *
     * @param CV the CV number
     * @param val expected value
     * @param p programmer
     * @param pcmd programming command
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    public void doConfirm(int CV, int val, ProgListener p,
            int pcmd) throws jmri.ProgrammerException {

        log.debug("confirmCV: {}", CV); // NOI18N

        stopEndOfProgrammingTimer();  // still programming, so no longer waiting for power off

        useProgrammer(p);
        _progRead = false;
        _progConfirm = true;
        _confirmVal = val;

        // set commandPending state
        progState = 1;

        // format and send message
        startShortTimer();
        tc.sendLocoNetMessage(progTaskStart(pcmd, -1, CV, false));
    }

    int hopsa; // high address for CV read/write
    int lopsa; // low address for CV read/write

    CsOpSwAccess csOpSwAccessor;

    /**
     * Read a CV via the OpsMode programmer.
     *
     * @param cvNum a String containing the CV number
     * @param p programmer
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    @Override
    public void readCV(String cvNum, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("readCV(string): cvNum={} mode={}", cvNum, getMode());
        if (getMode().equals(csOpSwProgrammingMode)) {
            log.debug("cvOpSw mode!");
            //handle Command Station OpSw programming here
            String[] parts = cvNum.split("\\.");
            if ((parts[0].equals("csOpSw")) && (parts.length==2)) {
                if (csOpSwAccessor == null) {
                    csOpSwAccessor = new CsOpSwAccess(adaptermemo, p);
                } else {
                    csOpSwAccessor.setProgrammerListener(p);
                }
                // perform the CsOpSwMode read access
                log.debug("going to try the opsw access");
                csOpSwAccessor.readCsOpSw(cvNum, p);
                return;

            } else {
                log.warn("rejecting the cs opsw access account unsupported CV name format");
                // unsupported format in "cv" name.  Signal an error.
                notifyProgListenerEnd(p, 1, ProgListener.SequenceError);
                return;

            }
        } else {
            // regular integer address for DCC form
            int CV = Integer.parseInt(cvNum);

            lopsa = 0;
            hopsa = 0;
            mServiceMode = true;
            // parse the programming command
            int pcmd = 0x03;       // LPE imples 0x00, but 0x03 is observed
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                pcmd = pcmd | 0x20;
            } else if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                pcmd = pcmd | 0x28;
            } else if (getMode().equals(ProgrammingMode.REGISTERMODE)
                    || getMode().equals(ProgrammingMode.ADDRESSMODE)) {
                pcmd = pcmd | 0x10;
            } else {
                throw new jmri.ProgrammerException("mode not supported"); // NOI18N
            }

            doRead(CV, p, pcmd);

        }
    }

    /**
     * Invoked by LnOpsModeProgrammer to start an ops-mode read operation.
     *
     * @param CVname       Which CV to read
     * @param p        Who to notify on complete
     * @param addr     Address of the locomotive
     * @param longAddr true if a long address, false if short address
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    public void readCVOpsMode(String CVname, jmri.ProgListener p, int addr, boolean longAddr) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        lopsa = addr & 0x7f;
        hopsa = (addr / 128) & 0x7f;
        mServiceMode = false;
        doRead(CV, p, 0x2F);  // although LPE implies 0x2C, 0x2F is observed
    }

    /**
     * Perform a CV Read.
     *
     * @param CV the CV number
     * @param p programmer
     * @param progByte programming command
     * @throws jmri.ProgrammerException if an unsupported programming mode is exercised
     */
    void doRead(int CV, jmri.ProgListener p, int progByte) throws jmri.ProgrammerException {

        log.debug("readCV: {}", CV); // NOI18N

        stopEndOfProgrammingTimer();  // still programming, so no longer waiting for power off

        useProgrammer(p);
        _progRead = true;
        _progConfirm = false;
        // set commandPending state
        progState = 1;

        // format and send message
        startShortTimer();
        tc.sendLocoNetMessage(progTaskStart(progByte, 0, CV, false));
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {

            log.info("programmer already in use by {}", _usingProgrammer); // NOI18N

            throw new jmri.ProgrammerException("programmer in use"); // NOI18N
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    /**
     * Internal method to create the LocoNetMessage for programmer task start.
     *
     * @param pcmd programmer command
     * @param val value to be used
     * @param cvnum CV number
     * @param write true if write, else false
     * @return a LocoNet message containing a programming task start operation
     */
    protected LocoNetMessage progTaskStart(int pcmd, int val, int cvnum, boolean write) {

        int addr = cvnum - 1;    // cvnum is in human readable form; addr is what's sent over LocoNet

        LocoNetMessage m = new LocoNetMessage(14);

        m.setOpCode(LnConstants.OPC_WR_SL_DATA);
        m.setElement(1, 0x0E);
        m.setElement(2, LnConstants.PRG_SLOT);

        m.setElement(3, pcmd);

        // set zero, then HOPSA, LOPSA, TRK
        m.setElement(4, 0);
        m.setElement(5, hopsa);
        m.setElement(6, lopsa);
        m.setElement(7, 0);  // TRK was 0, then 7 for PR2, now back to zero

        // store address in CVH, CVL. Note CVH format is truely wierd...
        m.setElement(8, ((addr & 0x300)>>4) | ((addr & 0x80) >> 7) | ((val & 0x80) >> 6));
        m.setElement(9, addr & 0x7F);

        // store low bits of CV value
        m.setElement(10, val & 0x7F);

        // throttle ID
        m.setElement(11, 0x7F);
        m.setElement(12, 0x7F);
        return m;
    }

    /**
     * Internal method to notify of the final result.
     *
     * @param value  The cv value to be returned
     * @param status The error code, if any
     */
    protected void notifyProgListenerEnd(int value, int status) {
        log.debug("  notifyProgListenerEnd with {}, {} and _usingProgrammer = {}", value, status, _usingProgrammer); // NOI18N
        // (re)start power timer
        restartEndOfProgrammingTimer();
        // and send the reply
        ProgListener p = _usingProgrammer;
        _usingProgrammer = null;
        if (p != null) {
            sendProgrammingReply(p, value, status);
        }
    }

    /**
     * Internal method to notify of the LACK result. This is a separate routine
     * from nPLRead in case we need to handle something later.
     *
     * @param status The error code, if any
     */
    protected void notifyProgListenerLack(int status) {
        // (re)start power timer
        restartEndOfProgrammingTimer();
        // and send the reply
        sendProgrammingReply(_usingProgrammer, -1, status);
        _usingProgrammer = null;
    }

    /**
     * Internal routine to forward a programming reply. This is delayed to
     * prevent overruns of the command station.
     *
     * @param p a ProgListener object
     * @param value  the value to return
     * @param status The error code, if any
     */
    protected void sendProgrammingReply(ProgListener p, int value, int status) {
        int delay = 20;  // value in service mode
        if (!mServiceMode) {
            delay = 100;  // value in ops mode
        }

        // delay and run on GUI thread
        javax.swing.Timer timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                notifyProgListenerEnd(p, value, status);
            }
        });
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to stop end-of-programming timer, as another programming
     * operation has happened.
     */
    protected void stopEndOfProgrammingTimer() {
        if (mPowerTimer != null) {
            mPowerTimer.stop();
        }
    }

    /**
     * Internal routine to handle timer restart if needed to restore power. This
     * is only needed in service mode.
     */
    protected void restartEndOfProgrammingTimer() {
        final int delay = 10000;
        if (mProgEndSequence) {
            if (mPowerTimer == null) {
                mPowerTimer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        doEndOfProgramming();
                    }
                });
            }
            mPowerTimer.stop();
            mPowerTimer.setInitialDelay(delay);
            mPowerTimer.setRepeats(false);
            mPowerTimer.start();
        }
    }

    /**
     * Internal routine to handle a programming timeout by turning power off.
     */
    synchronized protected void doEndOfProgramming() {
        if (progState == 0) {
             if ( mServiceMode ) {
                // finished service-track programming, time to power on
                log.debug("end service-mode programming: turn power on"); // NOI18N
                try {
                    jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.ON);
                } catch (jmri.JmriException e) {
                    log.error("exception during power on at end of programming: {}", e); // NOI18N
                }
            } else {
                log.debug("end ops-mode programming: no power change"); // NOI18N
            }
        }
    }

    javax.swing.Timer mPowerTimer = null;

    /**
     * Start the process of checking each slot for contents.
     * <p>
     * This is not invoked by this class, but can be invoked from elsewhere to
     * start the process of scanning all slots to update their contents.
     */
    synchronized public void update() {
        nextReadSlot = 0;
        readNextSlot();
    }

    /**
     * Send a message requesting the data from a particular slot.
     *
     * @param slot Slot number
     */
    public void sendReadSlot(int slot) {
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(LnConstants.OPC_RQ_SL_DATA);
        m.setElement(1, slot & 0x7F);
        if (slot > 127) {
            m.setElement(2, (slot / 128 ) & 0b00000111 );
            // and se t expanded format wanted
            m.setElement(2, m.getElement(2) | 0x40) ;
        } else {
            m.setElement(2, 0);
        }
        tc.sendLocoNetMessage(m);
    }

    protected int nextReadSlot = 0;

    /**
     * Continue the sequence of reading all slots.
     */
    synchronized protected void readNextSlot() {
        // send info request
        sendReadSlot(nextReadSlot++);

        // schedule next read if needed
        if (nextReadSlot < 127) {
            javax.swing.Timer t = new javax.swing.Timer(50, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    readNextSlot();
                }
            });
            t.setRepeats(false);
            t.start();
        }
    }

    /**
     * Provide a snapshot of the slots in use.
     * <p>
     * Note that the count of "in-use" slots may be somewhat misleading,
     * as slots in the "common" state can be controlled and are occupying
     * a slot in a meaningful way.
     *
     * @return the count of in-use LocoNet slots
     */
    public int getInUseCount() {
        int result = 0;
        for (int i = 0; i <= 120; i++) {
            if (slot(i).slotStatus() == LnConstants.LOCO_IN_USE) {
                result++;
            }
        }
        return result;
    }

    /**
     * Set the system connection memo.
     *
     * @param memo a LocoNetSystemConnectionMemo
     */
    public void setSystemConnectionMemo(LocoNetSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    LocoNetSystemConnectionMemo adaptermemo;

    /**
     * Get the "user name" for the slot manager connection, from the memo.
     *
     * @return the connection's user name or "LocoNet" if the memo
     * does not exist
     */
    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return "LocoNet"; // NOI18N
        }
        return adaptermemo.getUserName();
    }

    /**
     * Return the memo "system prefix".
     *
     * @return the system prefix or "L" if the memo
     * does not exist
     */
    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "L";
        }
        return adaptermemo.getSystemPrefix();
    }

    boolean transpondingAvailable = false;
    public void setTranspondingAvailable(boolean val) { transpondingAvailable = val; }
    public boolean getTranspondingAvailable() { return transpondingAvailable; }

    /**
     * Get the memo.
     *
     * @return the memo
     */
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        return adaptermemo;
    }

    /**
     * Dispose of this by stopped it's ongoing actions
     */
    public void dispose() {
        if (staleSlotCheckTimer != null) staleSlotCheckTimer.stop();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SlotManager.class);

}
