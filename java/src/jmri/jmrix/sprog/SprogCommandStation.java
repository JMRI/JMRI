package jmri.jmrix.sprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import javax.swing.JOptionPane;
import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.sprog.sprogslotmon.SprogSlotMonDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control a collection of slots, acting as a soft command station for SPROG
 * <p>
 * A SlotListener can register to hear changes. By registering here, the
 * SlotListener is saying that it wants to be notified of a change in any slot.
 * Alternately, the SlotListener can register with some specific slot, done via
 * the SprogSlot object itself.
 * <p>
 * This Programmer implementation is single-user only. It's not clear whether
 * the command stations can have multiple programming requests outstanding (e.g.
 * service mode and ops mode, or two ops mode) at the same time, but this code
 * definitely can't.
 * <p>
 * Updated by Andrew Berridge, January 2010 - state management code now safer,
 * uses enum, etc. Amalgamated with Sprog Slot Manager into a single class -
 * reduces code duplication.
 * <p>
 * Updated by Andrew Crosland February 2012 to allow slots to hold 28 step speed
 * packets
 * <p>
 * Re-written by Andrew Crosland to send the next packet as soon as a reply is 
 * notified. This removes a race between the old state machine running before 
 * the traffic controller despatches a reply, missing the opportunity to send a 
 * new packet to the layout until the next JVM time slot, which can be 15ms on 
 * Windows platforms.
 * <p>
 * May-17 Moved status reply handling to the slot monitor. Monitor messages from
 * other sources and suppress messages from here to prevent queueing messages in
 * the traffic controller.
 * <p>
 * Jan-18 Re-written again due to threading issues. Previous changes removed
 * activity from the slot thread, which could result in loading the swing thread
 * to the extent that the gui becomes very slow to respond.
 * Moved status message generation to the slot monitor.
 * Interact with power control as a way to allow the user to recover after a
 * timeout error due to loss of communication with the hardware.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Andrew Crosland (C) 2006 ported to SPROG, 2012, 2016, 2018
 */
public class SprogCommandStation implements CommandStation, SprogListener, Runnable,
        java.beans.PropertyChangeListener {

    protected int currentSlot = 0;
    protected int currentSprogAddress = -1;

    protected LinkedList<SprogSlot> slots;
    protected int numSlots = SprogConstants.MIN_SLOTS;
    protected Queue<SprogSlot> sendNow;

    private SprogTrafficController tc = null;

    final Object lock = new Object();
    
    // it's not at all clear what the following object does. It's only
    // set, with a newly created copy of a reply, in notifyReply(SprogReply m);
    // it's never referenced.
    @SuppressWarnings("unused") // added april 2018; should be removed?
    private SprogReply reply;  
    
    private boolean waitingForReply = false;
    private boolean replyAvailable = false;
    private boolean sendSprogAddress = false;
    private long time, timeNow, packetDelay;
    private int lastId;
    
    PowerManager powerMgr = null;
    int powerState = PowerManager.OFF;
    boolean powerChanged = false;
    
    public SprogCommandStation(SprogTrafficController controller) {
        sendNow = new LinkedList<>();
        /**
         * Create a default length queue
         */
        slots = new LinkedList<>();
        numSlots = SprogSlotMonDataModel.getSlotCount();
        for (int i = 0; i < numSlots; i++) {
            slots.add(new SprogSlot(i));
        }
        tc = controller;
        tc.addSprogListener(this);
    }

    /**
     * Send a specific packet as a SprogMessage.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats number of times to repeat the packet
     */
    @Override
    public boolean sendPacket(byte[] packet, int repeats) {
        if (packet.length <= 1) {
            log.error("Invalid DCC packet length: {}", packet.length);
        }
        if (packet.length >= 7) {
            log.error("Maximum 6-byte packets accepted: {}", packet.length);
        }
        final SprogMessage m = new SprogMessage(packet);
        sendMessage(m);
        return true;
    }

    /**
     * Send the SprogMessage to the hardware.
     * <p>
     * sendSprogMessage will block until the message can be sent. When it returns
     * we set the reply status for the message just sent.
     * 
     * @param m       The message to be sent
     */
    protected void sendMessage(SprogMessage m) {
        log.debug("Sending message [{}] id {}", m.toString(tc.isSIIBootMode()), m.getId());
        lastId = m.getId();
        tc.sendSprogMessage(m, this);
    }
    
    /**
     * Return contents of Queue slot i.
     *
     * @param i int of slot requested
     * @return SprogSlot slot i
     */
    public SprogSlot slot(int i) {
        return slots.get(i);
    }

    /**
     * Clear all slots.
     */
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private void clearAllSlots() {
        slots.stream().forEach((s) -> {
            s.clear();
        });
    }

    /**
     * Find a free slot entry.
     *
     * @return SprogSlot the next free Slot or null if all slots are full
     */
    protected SprogSlot findFree() {
        for (SprogSlot s : slots) {
            if (s.isFree()) {
                if (log.isDebugEnabled()) {
                    log.debug("Found free slot {}", s.getSlotNumber());
                }
                return s;
            }
        }
        return (null);
    }

    /**
     * Find a queue entry matching the address.
     *
     * @param address The address to locate
     * @return The slot or null if the address is not in the queue
     */
    private SprogSlot findAddress(DccLocoAddress address) {
        for (SprogSlot s : slots) {
            if ( s.isActiveAddressMatch(address) ) {
                return s;
            }
        }
        return (null);
    }

    private SprogSlot findAddressSpeedPacket(DccLocoAddress address) {
        // SPROG doesn't use IDLE packets but sends speed commands to last address selected by "A" command.
        // We may need to move these pseudo-idle packets to an unused long address so locos will not receive conflicting speed commands.
        // Some short-address-only decoders may also respond to same-numbered long address so we avoid any number match irrespective of type
        // We need to find a suitable free long address, save (currentSprogAddress) and use it for pseudo-idle packets
        int lastSprogAddress = currentSprogAddress;
        while ( (currentSprogAddress <= 0) || // initialisation || avoid address 0 for reason above
                    ( (address.getNumber() == currentSprogAddress ) ) || // avoid this address (slot may not exist but we will be creating one)
                    ( findAddress(new DccLocoAddress(currentSprogAddress,true)) != null) || ( findAddress(new DccLocoAddress(currentSprogAddress,false)) != null) // avoid in-use (both long or short versions of) address
                    ) {
                    currentSprogAddress++;
                    currentSprogAddress = currentSprogAddress % 10240;
            }
        if (currentSprogAddress != lastSprogAddress) {
            log.info("Changing currentSprogAddress (for pseudo-idle packets) to {}(L)", currentSprogAddress);
            // We want to ignore the reply to this message so it does not trigger an extra packet
            // Set a flag to send this from the slot thread and avoid swing thread waiting
            //sendMessage(new SprogMessage("A " + currentSprogAddress + " 0"));
            sendSprogAddress = true;
        }
        for (SprogSlot s : slots) {
            if (s.isActiveAddressMatch(address) && s.isSpeedPacket()) {
                return s;
            }
        }
        if (getInUseCount() < numSlots) {
            return findFree();
        }
        return (null);
    }

    private SprogSlot findF0to4Packet(DccLocoAddress address) {
        for (SprogSlot s : slots) {
            if (s.isActiveAddressMatch(address) && s.isF0to4Packet()) {
                return s;
            }
        }
        if (getInUseCount() < numSlots) {
            return findFree();
        }
        return (null);
    }

    private SprogSlot findF5to8Packet(DccLocoAddress address) {
        for (SprogSlot s : slots) {
            if (s.isActiveAddressMatch(address) && s.isF5to8Packet()) {
                return s;
            }
        }
        if (getInUseCount() < numSlots) {
            return findFree();
        }
        return (null);
    }

    private SprogSlot findF9to12Packet(DccLocoAddress address) {
        for (SprogSlot s : slots) {
            if (s.isActiveAddressMatch(address) && s.isF9to12Packet()) {
                return s;
            }
        }
        if (getInUseCount() < numSlots) {
            return findFree();
        }
        return (null);
    }

    public void forwardCommandChangeToLayout(int address, boolean closed) {

        SprogSlot s = this.findFree();
        if (s != null) {
            s.setAccessoryPacket(address, closed, SprogConstants.S_REPEATS);
            notifySlotListeners(s);
        }
    }

    public void function0Through4Packet(DccLocoAddress address,
            boolean f0, boolean f0Momentary,
            boolean f1, boolean f1Momentary,
            boolean f2, boolean f2Momentary,
            boolean f3, boolean f3Momentary,
            boolean f4, boolean f4Momentary) {
        SprogSlot s = this.findF0to4Packet(address);
        s.f0to4packet(address.getNumber(), address.isLongAddress(), f0, f0Momentary,
                f1, f1Momentary,
                f2, f2Momentary,
                f3, f3Momentary,
                f4, f4Momentary);
        notifySlotListeners(s);
    }

    public void function5Through8Packet(DccLocoAddress address,
            boolean f5, boolean f5Momentary,
            boolean f6, boolean f6Momentary,
            boolean f7, boolean f7Momentary,
            boolean f8, boolean f8Momentary) {
        SprogSlot s = this.findF5to8Packet(address);
        s.f5to8packet(address.getNumber(), address.isLongAddress(), f5, f5Momentary, f6, f6Momentary, f7, f7Momentary, f8, f8Momentary);
        notifySlotListeners(s);
    }

    public void function9Through12Packet(DccLocoAddress address,
            boolean f9, boolean f9Momentary,
            boolean f10, boolean f10Momentary,
            boolean f11, boolean f11Momentary,
            boolean f12, boolean f12Momentary) {
        SprogSlot s = this.findF9to12Packet(address);
        s.f9to12packet(address.getNumber(), address.isLongAddress(), f9, f9Momentary, f10, f10Momentary, f11, f11Momentary, f12, f12Momentary);
        notifySlotListeners(s);
    }

    /**
     * Handle speed changes from throttle. As well as updating an existing slot,
     * or creating a new on where necessary, the speed command is added to the
     * queue of packets to be sent immediately. This ensures minimum latency
     * between the user adjusting the throttle and a loco responding, rather
     * than possibly waiting for a complete traversal of all slots before the
     * new speed is actually sent to the hardware.
     *
     */
    public void setSpeed(jmri.SpeedStepMode mode, DccLocoAddress address, int spd, boolean isForward) {
        SprogSlot s = this.findAddressSpeedPacket(address);
        if (s != null) { // May need an error here - if all slots are full!
            s.setSpeed(mode, address.getNumber(), address.isLongAddress(), spd, isForward);
            notifySlotListeners(s);
            log.debug("Registering new speed");
            sendNow.add(s);
        }
    }

    public SprogSlot opsModepacket(int address, boolean longAddr, int cv, int val) {
        SprogSlot s = findFree();
        if (s != null) {
            s.setOps(address, longAddr, cv, val);
            if (log.isDebugEnabled()) {
                log.debug("opsModePacket() Notify ops mode packet for address {}", address);
            }
            notifySlotListeners(s);
            return (s);
        } else {
             return (null);
        }
    }

    public void release(DccLocoAddress address) {
        SprogSlot s;
        while ((s = findAddress(address)) != null) {
            s.clear();
            notifySlotListeners(s);
        }
    }

    /**
     * Send emergency stop to all slots.
     */
    public void estopAll() {
        slots.stream().filter((s) -> ((s.getRepeat() == -1)
                && s.slotStatus() != SprogConstants.SLOT_FREE
                && s.speed() != 1)).forEach((s) -> {
                    eStopSlot(s);
                });
    }

    /**
     * Send emergency stop to a slot.
     *
     * @param s SprogSlot to eStop
     */
    protected void eStopSlot(SprogSlot s) {
        log.debug("Estop slot: {} for address: {}", s.getSlotNumber(), s.getAddr());
        s.eStop();
        notifySlotListeners(s);
    }

    // data members to hold contact with the slot listeners
    final private Vector<SprogSlotListener> slotListeners = new Vector<>();

    public synchronized void addSlotListener(SprogSlotListener l) {
        // add only if not already registered
        slotListeners.addElement(l);
    }

    public synchronized void removeSlotListener(SprogSlotListener l) {
        slotListeners.removeElement(l);
    }

    /**
     * Trigger the notification of all SlotListeners.
     *
     * @param s The changed slot to notify.
     */
    private synchronized void notifySlotListeners(SprogSlot s) {
        log.debug("notifySlotListeners() notify {} SlotListeners about slot for address {}",
                    slotListeners.size(), s.getAddr());

        // forward to all listeners
        slotListeners.stream().forEach((client) -> {
            client.notifyChangedSlot(s);
        });
    }

    @Override
    /**
     * The run() method will only be called (from SprogSystemConnectionMemo
     * ConfigureCommandStation()) if the connected SPROG is in Command Station mode.
     * 
     */
    public void run() {
        log.debug("Command station slot thread starts");
        while(true) {
            try {
                synchronized(lock) {
                    lock.wait(SprogConstants.CS_REPLY_TIMEOUT);
                }
            } catch (InterruptedException e) {
               log.debug("Slot thread interrupted");
               // We'll loop around if there's no reply available yet
               // Save the interrupted status for anyone who may be interested
               Thread.currentThread().interrupt();
               // and exit
               return;
            }
            log.debug("Slot thread wakes");
            
            if (powerMgr == null) {
                // Wait until power manager is available
                powerMgr = InstanceManager.getNullableDefault(jmri.PowerManager.class);
                if (powerMgr == null) {
                    log.info("No power manager instance found");
                } else {
                    log.info("Registering with power manager");
                    powerMgr.addPropertyChangeListener(this);
                }
            } else {
                if (sendSprogAddress) {
                    // If we need to change the SPROGs default address, do that immediately,
                    // regardless of the power state.
                    sendMessage(new SprogMessage("A " + currentSprogAddress + " 0"));
                    replyAvailable = false;
                    sendSprogAddress = false;
                } else if (powerChanged && (powerState == PowerManager.ON) && !waitingForReply) {
                    // Power has been turned on so send an idle packet to start the
                    // message/reply handshake
                    sendPacket(jmri.NmraPacket.idlePacket(), SprogConstants.S_REPEATS);
                    powerChanged = false;
                    time = System.currentTimeMillis();
                } else if (replyAvailable && (powerState == PowerManager.ON)) {
                    // Received a reply whilst power is on, so send another packet
                    // Get next packet to send if track power is on
                    byte[] p;
                    SprogSlot s = sendNow.poll();
                    if (s != null) {
                        // New throttle action to be sent immediately
                        p = s.getPayload();
                        log.debug("Packet from immediate send queue");
                    } else {
                        // Or take the next one from the stack
                        p = getNextPacket();
                        if (p != null) {
                            log.debug("Packet from stack");
                        }
                    }
                    replyAvailable = false;
                    if (p != null) {
                        // Send the packet
                        sendPacket(p, SprogConstants.S_REPEATS);
                        log.debug("Packet sent");
                    } else {
                        // Send a decoder idle packet to prompt a reply from hardware and keep things running
                        sendPacket(jmri.NmraPacket.idlePacket(), SprogConstants.S_REPEATS);
                    }
                    timeNow = System.currentTimeMillis();
                    packetDelay = timeNow - time;
                    time = timeNow;
                    // Useful for debug if packets are being delayed
                    if (packetDelay > SprogConstants.PACKET_DELAY_WARN_THRESHOLD) {
                        log.warn("Packet delay was {} ms", packetDelay);
                    }
                } else {
                    if (powerState == PowerManager.ON) {

                        // Should never get here. Something is wrong so turn power off
                        // Kill reply wait so send doesn't block
                        log.warn("Slot thread timeout - removing power");
                        waitingForReply = false;
                        try {
                            powerMgr.setPower(PowerManager.OFF);
                        } catch (JmriException ex) {
                            log.error("Exception turning power off {}", ex);
                        }
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("CSErrorFrameDialogString"),
                            Bundle.getMessage("SprogCSTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Get the next packet to be transmitted.
     *
     * @return byte[] null if no packet
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "API defined by Sprog docs")
    private byte[] getNextPacket() {
        SprogSlot s;

        if (!isBusy()) {
            return null;
        }
        while (slots.get(currentSlot).isFree()) {
            currentSlot++;
            currentSlot = currentSlot % numSlots;
        }
        s = slots.get(currentSlot);
        byte[] ret = s.getPayload();
        // Resend ops packets until repeat count is exhausted so that
        // decoder receives contiguous identical packets, otherwsie find
        // next packet to send
        if (!s.isOpsPkt() || (s.getRepeat() == 0)) {
            currentSlot++;
            currentSlot = currentSlot % numSlots;
        }

        if (s.isFinished()) {
            notifySlotListeners(s);
            //return null;
        }

        return ret;
    }

    /*
     *
     * @param m the sprog message received
     */
    @Override
    public void notifyMessage(SprogMessage m) {
    }

    /**
     * Handle replies.
     * <p>
     * Handle replies from the hardware, ignoring those that were not sent from
     * the command station.
     *
     * @param m The SprogReply to be handled
     */
    @Override
    public void notifyReply(SprogReply m) {
        if (m.getId() != lastId) {
            // Not my id, so not interested, message send still blocked
            log.debug("Ignore reply with mismatched id {} looking for {}", m.getId(), lastId);
            return;
        } else {
            // it's not at all clear what the following line does. The "reply"
            // variable is only set here, and never referenced.
            reply = new SprogReply(m);
            
            log.debug("Reply received [{}]", m.toString());
            // Log the reply and wake the slot thread
            synchronized (lock) {
                replyAvailable = true;
                lock.notifyAll();
            }
        }
    }

    /**
     * implement a property change listener for power
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        log.debug("propertyChange " + evt.getPropertyName() + " = " + evt.getNewValue());
        if (evt.getPropertyName().equals("Power")) {
            try {
                powerState = powerMgr.getPower();
            } catch (JmriException ex) {
                log.error("Exception getting power state {}", ex);
            }
            powerChanged = true;
        }
    }

    /**
     * Provide a count of the slots in use.
     * 
     * @return the number of slots in use
     */
    public int getInUseCount() {
        int result = 0;
        for (SprogSlot s : slots) {
            if (!s.isFree()) {
                result++;
            }
        }
        return result;
    }

    /**
     *
     * @return a boolean if the command station is busy - i.e. it has at least
     *         one occupied slot
     */
    public boolean isBusy() {
        return slots.stream().anyMatch((s) -> (!s.isFree()));
    }

    public void setSystemConnectionMemo(SprogSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    SprogSystemConnectionMemo adaptermemo;

    /**
     * Get user name.
     * 
     * @return the user name
     */
    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return "Sprog";
        }
        return adaptermemo.getUserName();
    }

    /**
     * Get system prefix.
     * 
     * @return the system prefix
     */
    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "S";
        }
        return adaptermemo.getSystemPrefix();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SprogCommandStation.class);

}
