package jmri.jmrix.sprog;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import jmri.CommandStation;
import jmri.DccLocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls a collection of slots, acting as a soft command station for SPROG
 * <P>
 * A SlotListener can register to hear changes. By registering here, the
 * SlotListener is saying that it wants to be notified of a change in any slot.
 * Alternately, the SlotListener can register with some specific slot, done via
 * the SprogSlot object itself.
 * <P>
 * This Programmer implementation is single-user only. It's not clear whether
 * the command stations can have multiple programming requests outstanding (e.g.
 * service mode and ops mode, or two ops mode) at the same time, but this code
 * definitely can't.
 * <P>
 * Updated by Andrew Berridge, January 2010 - state management code now safer,
 * uses enum, etc. Amalgamated with Sprog Slot Manager into a single class -
 * reduces code duplication </P>
 * <P>
 * Updated by Andrew Crosland February 2012 to allow slots to hold 28 step speed
 * packets</P>
 * 
 * Re-written by Andrew Crosland to send the next packet as soon as a reply is 
 * notified. This removes a race between the old state machine running before 
 * the traffic controller despatches a reply, missing the opportunity to send a 
 * new packet to the layout until the next JVM time slot, which can be 15ms on 
 * Windows platforms.
 * 
 * May-17 Moved status reply handling to the slot monitor. Monitor messages from
 * other sources and suppress messages from here to prevent queueing messages in
 * the traffic controller.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Andrew Crosland (C) 2006 ported to SPROG, 2012, 2016
 */
public class SprogCommandStation implements CommandStation, SprogListener, Runnable {

    private boolean running = false;
    
    protected int currentSlot = 0;
    protected int currentSprogAddress = -1;

    protected LinkedList<SprogSlot> slots;
    protected Queue<SprogSlot> sendNow;

    javax.swing.Timer timer = null;

    private SprogTrafficController tc = null;

    public SprogCommandStation(SprogTrafficController controller) {
        sendNow = new LinkedList<>();
        /**
         * Create a default length queue
         */
        slots = new LinkedList<>();
        for (int i = 0; i < SprogConstants.MAX_SLOTS; i++) {
            slots.add(new SprogSlot(i));
        }
        tc = controller;
        tc.addSprogListener(this);
    }

    /**
     * Send a specific packet to the rails.
     *
     * Call to sendSprogMessage seems to get delayed if this thread sleeps, so
     * create a new runnable object to despatch the message to the traffic
     * controller.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats number of times to repeat the packet
     */
    @Override
    public void sendPacket(byte[] packet, int repeats) {
        if (packet.length <= 1) {
            log.error("Invalid DCC packet length: " + packet.length);
        }
        if (packet.length >= 7) {
            log.error("Maximum 6-byte packets accepted: " + packet.length);
        }
        final SprogMessage m = new SprogMessage(packet);
        if (log.isDebugEnabled()) {
            log.debug("Sending packet " + m.toString(tc.isSIIBootMode()));
        }
        tc.sendSprogMessage(m, this);
    }

    /**
     * Return contents of Queue slot i
     *
     * @param i int
     * @return SprogSlot
     */
    public SprogSlot slot(int i) {
        return slots.get(i);
    }

    /**
     * Clear all slots
     */
    @SuppressWarnings("unused")
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
                    log.debug("Found free slot " + s.getSlotNumber());
                }
                return s;
            }
        }
        return (null);
    }

    /**
     * Find a queue entry matching the address
     *
     * @param a int
     * @return the slot or null if the address is not in the queue
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
            log.info("Changing currentSprogAddress (for pseudo-idle packets) to "+currentSprogAddress+"(L)");
        }   
        tc.sendSprogMessage(new SprogMessage("A " + currentSprogAddress + " 0"), this);
        for (SprogSlot s : slots) {
            if (s.isActiveAddressMatch(address) && s.isSpeedPacket()) {
                return s;
            }
        }
        if (getInUseCount() < SprogConstants.MAX_SLOTS) {
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
        if (getInUseCount() < SprogConstants.MAX_SLOTS) {
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
        if (getInUseCount() < SprogConstants.MAX_SLOTS) {
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
        if (getInUseCount() < SprogConstants.MAX_SLOTS) {
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
    public void setSpeed(int mode, DccLocoAddress address, int spd, boolean isForward) {
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
                log.debug("opsModePacket() Notify ops mode packet for address " + address);
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
     * Send emergency stop to all slots
     */
    public void estopAll() {
        slots.stream().filter((s) -> ((s.getRepeat() == -1)
                && s.slotStatus() != SprogConstants.SLOT_FREE
                && s.speed() != 1)).forEach((s) -> {
                    eStopSlot(s);
                });
    }

    /**
     * Send emergency stop to a slot
     *
     * @param s SprogSlot to eStop
     */
    protected void eStopSlot(SprogSlot s) {
        log.debug("Estop slot: " + s.getSlotNumber() + " for address: " + s.getAddr());
        s.eStop();
        notifySlotListeners(s);
    }

    /**
     * method to find the existing SlotManager object, if need be creating one
     *
     * @return the SlotManager object
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    public final SprogCommandStation instance() {
        return null;
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
        log.debug("notifySlotListeners() notify " + slotListeners.size()
                    + " SlotListeners about slot for address "
                    + s.getAddr());

        // forward to all listeners
        slotListeners.stream().forEach((client) -> {
            client.notifyChangedSlot(s);
        });
    }

    private int statusDue = 0;
    @Override
    /**
     * The run() method will only be called (from SprogSystemconnecionMemo 
     * ConfigureCommandStation()) if the connected SPROG is in OPS mode.
     * 
     */
    public void run() {
        log.debug("Slot thread starts");
        running = true;
        // Send a decoder idle packet to prompt a reply from hardware and set things running
        sendPacket(jmri.NmraPacket.idlePacket(), SprogConstants.S_REPEATS);
    }

    /**
     * Get the next packet to be transmitted. returns null if no packet
     *
     * @return byte[]
     */
    private byte[] getNextPacket() {
        SprogSlot s;

        if (!isBusy()) {
            return null;
        }
        while (slots.get(currentSlot).isFree()) {
            currentSlot++;
            currentSlot = currentSlot % SprogConstants.MAX_SLOTS;
        }
        s = slots.get(currentSlot);
        byte[] ret = s.getPayload();
        // Resend ops packets until repeat count is exhausted so that
        // decoder receives contiguous identical packets, otherwsie find
        // next packet to send
        if (!s.isOpsPkt() || (s.getRepeat() == 0)) {
            currentSlot++;
            currentSlot = currentSlot % SprogConstants.MAX_SLOTS;
        }

        if (s.isFinished()) {
            notifySlotListeners(s);
            //return null;
        }

        return ret;
    }

    private int ignoreReply = 0;
    
    /*
     * Needs to listen to replies
     * Need to implement asynch replies for overload & notify power manager
     *
     * How does POM work??? how does programmer send packets??
     *
     * Only messages from other sources are forwarded by the traffic controller.
     * We want to ignore the replies to such messages.
     *
     * @param m the sprog message received
     */
    @Override
    public void notifyMessage(SprogMessage m) {
        log.debug("notifyMessage "+"'"+m.toString()+"'");
        ignoreReply++;
    }

    /**
     * The thread will only run when the connected SPROG is in OPS mode.
     * 
     * @param m The SprogReply to be handled
     */
    @Override
    public void notifyReply(SprogReply m) {
        log.debug("Reply received: "+"StatusDue = "+statusDue+" ignoreReply = "+ignoreReply+" "+m.toString());
        if (ignoreReply > 0) {
            // Ignore one reply
            log.debug("Ignoring reply");
            ignoreReply--;
        } else if (running == true) {
            byte[] p;
            if (m.isUnsolicited() && m.isOverload()) {
                log.error("Overload");

                // *** turn power off
            }
            
            // Is it time to send a status request?
            if (statusDue == 40) {
                // Ask for status periodically
                log.debug("Sending status request");
                tc.sendSprogMessage(SprogMessage.getStatus(), this);
                statusDue = 0;
            } else {
                // Get next packet to send
                log.debug("Get next packet to send");
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
                if (p != null) {
                    // Send the packet
                    sendPacket(p, SprogConstants.S_REPEATS);
                    log.debug("Packet sent");
                } else {
                    // Send a decoder idle packet to prompt a reply from hardware and keep things running
                    sendPacket(jmri.NmraPacket.idlePacket(), SprogConstants.S_REPEATS);
                    log.debug("Idle packet sent");
                }
                statusDue++;
            }
        }
    }

    /**
     * Provide a count of the slots in use
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
     * Get user name
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
     * Get system prefix
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

    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        Runnable r = () -> {
            log.debug("Send idle packet due to timeout");
            // Send a decoder idle packet to prompt a reply from hardware and keep things running
            sendPacket(jmri.NmraPacket.idlePacket(), SprogConstants.S_REPEATS);
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Internal routine to handle timer starts {@literal &} restarts
     * 
     * @param delay timer delay
     */
    protected void restartTimer(int delay) {
        log.debug("Restart timer");
        if (timer == null) {
            timer = new javax.swing.Timer(delay, (java.awt.event.ActionEvent e) -> {
                timeout();
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to handle timer stop
     */
    protected void stopTimer() {
        log.debug("Stop timer");
        if (timer != null) {
            timer.stop();
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SprogCommandStation.class);
}
