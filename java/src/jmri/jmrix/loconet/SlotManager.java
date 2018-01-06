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
 */
public class SlotManager extends AbstractProgrammer implements LocoNetListener, CommandStation {

    /**
     * Time to wait after programming operation complete on LocoNet
     * before reporting completion and hence starting next operation
     */
    int postProgDelay = 100;

    public SlotManager(LnTrafficController tc) {
        this.tc = tc;
        // change timeout values from AbstractProgrammer superclass
        LONG_TIMEOUT = 180000;  // Fleischman command stations take forever
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

    protected void loadSlots() {
        // initialize slot array
        for (int i = 0; i < NUM_SLOTS; i++) {
            _slots[i] = new LocoNetSlot(i);
        }
    }

    protected LnTrafficController tc;

    /**
     * Send a DCC packet to the rails. This implements the CommandStation
     * interface.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte.  The error-correction byte
     *                need not be correct as it is not propagated as part
     *                of the LocoNet message.  The length of packet
     *                determines the number of bytes to be sent in the NMRA
     *                packet.  The command station computes and
     *                fills-in the error-correction byte as the last byte of the
     *                packet.  When packet includes fewer than 6 bytes, the
     *                LocoNet message will encode the remaining bytes as 0.
     * @param repeats Number of times to repeat the transmission.  repeats
     *                must be in the range {0...7}
     */
    @Override
    public void sendPacket(byte[] packet, int repeats) {
        if (repeats > 7) {
            log.error("Too many repeats!"); // NOI18N
        }
        if (packet.length <= 1) {
            log.error("Invalid DCC packet length: " + packet.length); // NOI18N
        }
        if (packet.length > 6) {
            log.error("Only 6-byte packets accepted: " + packet.length); // NOI18N
        }

        LocoNetMessage m = new LocoNetMessage(11);
        m.setElement(0, LnConstants.OPC_IMM_PACKET);
        m.setElement(1, 0x0B);
        m.setElement(2, 0x7F);
        // the incoming packet includes a check byte that's not included in LocoNet packet
        int length = packet.length - 1;

        m.setElement(3, (repeats & 0x7) + 16 * (length & 0x7));

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
     * <P>
     * This requires access to the command station, even if the locomotive
     * address appears in the current contents of the slot array, to ensure that
     * our local image is up-to-date.
     * <P>
     * This method sends an info request. When the echo of this is returned from
     * the LocoNet, the next slot-read is recognized as the response.
     * <P>
     * The object that's looking for this information must provide a
     * SlotListener to notify when the slot ID becomes available.
     * <P>
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
     * Scan the slot array looking for slots that are in-use but have
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
            if ((slot.slotStatus() == LnConstants.LOCO_IN_USE)
                    && (slot.getLastUpdateTime() <= staleTimeout)) {
                sendReadSlot(i);
            }
        }
    }

    /**
     * Provide a mapping between locomotive addresses and the SlotListener
     * that's interested in them
     */
    Hashtable<Integer, SlotListener> mLocoAddrHash = new Hashtable<Integer, SlotListener>();

    // data members to hold contact with the slot listeners
    final private Vector<SlotListener> slotListeners = new Vector<SlotListener>();

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
        if (log.isDebugEnabled()) {
            log.debug("notify " + v.size() // NOI18N
                    + " SlotListeners about slot " // NOI18N
                    + s.getSlot());
        }
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
     * If this is a direct function command, return -1, otherwise return DCC
     * address word
     */
    int getDirectFunctionAddress(LocoNetMessage m) {
        if (m.getElement(0) != LnConstants.OPC_IMM_PACKET) {
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

    /* if this is a direct DCC packet, return as one long
     * else return -1. Packet does not include
     * address bytes.
     */
    int getDirectDccPacket(LocoNetMessage m) {
        if (m.getElement(0) != LnConstants.OPC_IMM_PACKET) {
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
     * True if the message is an external DCC packet request for F9-F28
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
     * FInd the slot number that a message references
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

            case LnConstants.OPC_MOVE_SLOTS:  // handle the follow-on message when it comes
                return i; // need to cope with that!!

            default:
                // nothing here for us
                return i;
        }
        // break gets to here
        return i;
    }

    /*
     * The following methods are for parsing LACK as response to CV programming. It is divided into numerous
     * small methods so that each bit can be overridden for special parsing for individual command station types.
     */
    protected boolean checkLackByte1(int Byte1) {
        if ((Byte1 & 0xEF) == 0x6F) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkLackTaskAccepted(int Byte2) {
        if (Byte2 == 1 // task accepted
                || Byte2 == 0x23 || Byte2 == 0x2B || Byte2 == 0x6B// added as DCS51 fix
                || Byte2 == 0x7F) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkLackProgrammerBusy(int Byte2) {
        if (Byte2 == 0) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkLackAcceptedBlind(int Byte2) {
        if (Byte2 == 0x40) {
            return true;
        } else {
            return false;
        }
    }

    protected void handleLongAck(LocoNetMessage m) {
        // handle if reply to slot. There's no slot number in the LACK, unfortunately.
        // If this is a LACK to a Slot op, and progState is command pending,
        // assume its for us...
        if (log.isDebugEnabled()) {
            log.debug("LACK in state " + progState + " message: " + m.toString()); // NOI18N
        }
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
                log.warn("unexpected LACK reply code " + m.getElement(2)); // NOI18N
                // move to not programming state
                progState = 0;
                // notify user ProgListener
                stopTimer();
                notifyProgListenerLack(jmri.ProgListener.UnknownError);
            }
        }
    }

    public void forwardMessageToSlot(LocoNetMessage m, int i) {

        // if here, i holds the slot number, and we expect to be able to parse
        // and have the slot handle the message
        if (i >= _slots.length || i < 0) {
            log.error("Received slot number " + i // NOI18N
                    + " is greater than array length " + _slots.length + " Message was " // NOI18N
                    + m.toString());
        }
        try {
            _slots[i].setSlot(m);
        } catch (LocoNetException e) {
            // must not have been interesting, or at least routed right
            log.error("slot rejected LocoNetMessage" + m); // NOI18N
            return;
        }
        // notify listeners that slot may have changed
        notify(_slots[i]);
    }

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

    protected void programmerOpMessage(LocoNetMessage m, int i) {

        // start checking for programming operations in slot 124
        if (i == 124) {
            // here its an operation on the programmer slot
            if (log.isDebugEnabled()) {
                log.debug("Prog Message " + m.getOpCodeHex() // NOI18N
                        + " for slot 124 in state " + progState); // NOI18N
            }
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
                    log.error("unexpected programming state " + progState); // NOI18N
                    break;
            }
        }
    }

    ProgrammingMode csOpSwProgrammingMode = new ProgrammingMode(
            "LOCONETCSOPSWMODE",
            Bundle.getMessage("LOCONETCSOPSWMODE"));

    // members for handling the programmer interface
    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
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
     * Service mode always checks for DecoderReply.  (The DCS240 also seems to do
     * ReadAfterWrite, but that's not fully understood yet)
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) { return WriteConfirmMode.DecoderReply; }

    /**
     * Set the command station type to one of the known types in the
     * {@link LnCommandStationType} enum.
     */
    public void setCommandStationType(LnCommandStationType value) {
        commandStationType = value;
        mCanRead = value.getCanRead();
        mProgEndSequence = value.getProgPowersOff();
    }

    LocoNetThrottledTransmitter throttledTransmitter = null;
    boolean mTurnoutNoRetry = false;

    /**
     * Provide a ThrottledTransmitter for sending immediate packets
     */
    public void setThrottledTransmitter(LocoNetThrottledTransmitter value, boolean m) {
        throttledTransmitter = value;
        mTurnoutNoRetry = m;
    }

    /**
     * Get the command station type
     */
    public LnCommandStationType getCommandStationType() {
        return commandStationType;
    }

    protected LnCommandStationType commandStationType = null;

    /**
     * Internal routine to handle a timeout
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

    public void writeCVOpsMode(int CV, int val, jmri.ProgListener p,
            int addr, boolean longAddr) throws jmri.ProgrammerException {
        lopsa = addr & 0x7f;
        hopsa = (addr / 128) & 0x7f;
        mServiceMode = false;
        doWrite(CV, val, p, 0x67);  // ops mode byte write, with feedback
    }

    @Override
    public void writeCV(String cvNum, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("writeCV(string): cvNum={}, value={}", cvNum, val);
        if (getMode().equals(csOpSwProgrammingMode)) {
            log.debug("cvOpSw mode write!");
            //handle Command Station OpSw programming here
            String[] parts = cvNum.split("\\.");
            if ((parts[0].equals("csOpSw")) && (parts.length==2)) {
                if (csOpSwAccessor == null) {
                    csOpSwAccessor = new csOpSwAccess(adaptermemo, p);
                } else {
                    csOpSwAccessor.setProgrammerListener(p);
                }
                // perform the CsOpSwMode read access
                log.debug("going to try the opsw access");
                csOpSwAccessor.writeCsOpSw(cvNum, val, p);
                return;

            } else {
                log.warn("rejecting the cs opsw access account unsupported CV name format");
                // unsupported format in "cv" name.  Signal an error.
                p.programmingOpReply(1, ProgListener.SequenceError);
                return;

            }
        } else {
            writeCV(Integer.parseInt(cvNum), val, p);
        }
    }


    @Override
    public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
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

    public void confirmCVOpsMode(String CVname, int val, jmri.ProgListener p,
            int addr, boolean longAddr) throws jmri.ProgrammerException {
        int CV = Integer.parseInt(CVname);
        lopsa = addr & 0x7f;
        hopsa = (addr / 128) & 0x7f;
        mServiceMode = false;
        doConfirm(CV, val, p, 0x2F);  // although LPE implies 0x2C, 0x2F is observed
    }

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
                    csOpSwAccessor = new csOpSwAccess(adaptermemo, p);
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
                p.programmingOpReply(1, ProgListener.SequenceError);
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

    csOpSwAccess csOpSwAccessor;
    @Override
    public void readCV(String cvNum, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("readCV(string): cvNum={}", cvNum);
        if (getMode().equals(csOpSwProgrammingMode)) {
            log.debug("cvOpSw mode!");
            //handle Command Station OpSw programming here
            String[] parts = cvNum.split("\\.");
            if ((parts[0].equals("csOpSw")) && (parts.length==2)) {
                if (csOpSwAccessor == null) {
                    csOpSwAccessor = new csOpSwAccess(adaptermemo, p);
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
                p.programmingOpReply(1, ProgListener.SequenceError);
                return;

            }
        } else {
            readCV(Integer.parseInt(cvNum), p);
        }
    }

    /**
     * Invoked by LnOpsModeProgrammer to start an ops-mode read operation.
     *
     * @param CV       Which CV to read
     * @param p        Who to notify on complete
     * @param addr     Address of the locomotive
     * @param longAddr true if a long address, false if short address
     */
    public void readCVOpsMode(int CV, jmri.ProgListener p, int addr, boolean longAddr) throws jmri.ProgrammerException {
        lopsa = addr & 0x7f;
        hopsa = (addr / 128) & 0x7f;
        mServiceMode = false;
        doRead(CV, p, 0x2F);  // although LPE implies 0x2C, 0x2F is observed
    }

    @Override
    public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
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
     * Internal method to create the LocoNetMessage for programmer task start
     */
    protected LocoNetMessage progTaskStart(int pcmd, int val, int cvnum, boolean write) {

        int addr = cvnum - 1;    // cvnum is in human readable form; addr is what's sent over loconet

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
     * internal method to notify of the final result
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
     * from nPLRead in case we need to handle something later
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
     * Internal routine to forward a programing reply. This is delayed to
     * prevent overruns of the command station.
     *
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
                p.programmingOpReply(value, status);
            }
        });
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to stop end-of-programming timer, as another programming
     * operation has happened
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
     * Internal routine to handle a programming timeout by turning power off
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
     * <P>
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
        m.setElement(2, 0);
        tc.sendLocoNetMessage(m);
    }

    protected int nextReadSlot = 0;

    synchronized protected void readNextSlot() {
        // send info request
        sendReadSlot(nextReadSlot++);

        // schedule next read if needed
        if (nextReadSlot < 127) {
            javax.swing.Timer t = new javax.swing.Timer(500, new java.awt.event.ActionListener() {
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
     * Provide a snapshot of the slots in use
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

    public void setSystemConnectionMemo(LocoNetSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    LocoNetSystemConnectionMemo adaptermemo;

    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return "LocoNet"; // NOI18N
        }
        return adaptermemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "L";
        }
        return adaptermemo.getSystemPrefix();
    }

    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        return adaptermemo;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SlotManager.class);
}
