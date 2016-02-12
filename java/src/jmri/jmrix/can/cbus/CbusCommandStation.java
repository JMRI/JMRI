// CbusCommandStation.java
package jmri.jmrix.can.cbus;

import jmri.CommandStation;
import jmri.jmrix.DccCommandStation;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement CommandStation for CBUS communications.
 *
 * The intention is that, unlike some other systems, we will hold no or minimal
 * command station state in the software model. The actual command station state
 * should always be referred to.
 *
 * @author Andrew Crosland Copyright (C) 2009
 * @version $Revision$
 */
public class CbusCommandStation implements CommandStation, DccCommandStation, CanListener {

    public CbusCommandStation(CanSystemConnectionMemo memo) {
        tc = memo.getTrafficController();
        adapterMemo = memo;
    }

    TrafficController tc;
    CanSystemConnectionMemo adapterMemo;

    /**
     * Send a specific packet to the rails.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats Number of times to repeat the transmission, but is ignored
     *                in the current implementation
     */
    @Override
    public void sendPacket(byte[] packet, int repeats) {

        if (repeats != 1) {
            log.warn("Only single transmissions currently available");
        }

        CanMessage m = new CanMessage(2 + packet.length, tc.getCanid());     // Account for opcode and repeat
        int j = 0; // counter of byte in input packet

        m.setElement(0, CbusConstants.CBUS_RDCC3 + (((packet.length - 3) & 0x3) << 5));
        m.setElement(1, 1);   // repeat

        // add each byte of the input message
        for (j = 0; j < packet.length; j++) {
            m.setElement(j + 2, packet[j] & 0xFF);
        }

        tc.sendCanMessage(m, null);
    }

    /**
     * Release a session freeing up the slot for reuse
     *
     * @param handle the handle for the session to be released
     */
    public void releaseSession(int handle) {
        // Send KLOC
        CanMessage msg = new CanMessage(2, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_KLOC);
        msg.setElement(1, handle);
        log.debug("Release session handle " + handle);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Send keep alive (DKEEP) packet for a throttle
     *
     * @param handle
     */
    public void sendKeepAlive(int handle) {
        CanMessage msg = new CanMessage(2, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_DKEEP);
        msg.setElement(1, handle);
        log.debug("keep alive handle: " + handle);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Set loco speed and direction
     *
     * @param handle    The handle of the session to which it applies
     * @param speed_dir Bit 7 is direction (1 = forward) 6:0 are speed
     */
    public void setSpeedDir(int handle, int speed_dir) {
        CanMessage msg = new CanMessage(3, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_DSPD);
        msg.setElement(1, handle);
        msg.setElement(2, speed_dir);
        log.debug("setSpeedDir session handle " + handle + " speedDir " + speed_dir);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Send a CBUS message to set functions
     *
     * @param group     The function group
     * @param handle    The handle of the session for the loco being controlled
     * @param functions Function bits
     */
    protected void setFunctions(int group, int handle, int functions) {
        log.debug("Set function group " + group + " Fns " + functions + " for session handle" + handle);
        CanMessage msg = new CanMessage(4, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_DFUN);
        msg.setElement(1, handle);
        msg.setElement(2, group);
        msg.setElement(3, functions);
        tc.sendCanMessage(msg, this);
    }

    /**
     * Send a CBUS message to change the session speed step mode
     *
     * @param mode the speed step mode
     */
    protected void setSpeedSteps(int handle, int mode) {
        log.debug("Set speed step mode " + mode + " for session handle" + handle);
        CanMessage msg = new CanMessage(3, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_STMOD);
        msg.setElement(1, handle);
        msg.setElement(2, mode);
        tc.sendCanMessage(msg, this);
    }

    @Override
    public void message(CanMessage m) {
    }

    @Override
    synchronized public void reply(CanReply m) {
    }

    /**
     * Does this command station have a "service mode", where it stops normal
     * train operation while programming?
     */
    @Override
    public boolean getHasServiceMode() {
        return true;
    }

    /**
     * If this command station has a service mode, is the command station
     * currently in that mode?
     */
    @Override
    public boolean getInServiceMode() {

        // *** ???
        return true;
    }

    /**
     * Provides an-implementation specific version string from the command
     * station. In general, this should be as close as possible to what the
     * command station replied when asked; it should not be reformatted
     *
     */
    @Override
    public String getVersionString() {
        return "0.0";
    }

    @Override
    public String getUserName() {
        return adapterMemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        return adapterMemo.getSystemPrefix();
    }

    private final static Logger log = LoggerFactory.getLogger(CbusCommandStation.class.getName());
}

/* @(#)CbusCommandStation.java */
