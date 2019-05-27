package jmri.jmrix.can.cbus;

import jmri.CommandStation;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommandStation for CBUS communications.
 *
 * Unlike some other systems, we will hold minimal command station state 
 * in the software model. The actual command station state
 * should always be referred to.
 *
 * @author Andrew Crosland Copyright (C) 2009
 * @author Steve Young Copyright (C) 2019
 */
public class CbusCommandStation implements CommandStation {

    public CbusCommandStation(CanSystemConnectionMemo memo) {
        tc = memo.getTrafficController();
        adapterMemo = memo;
        if ( ( tc != null ) && ( tc.getClass().getName().contains("Loopback")) ) {
            jmri.util.ThreadingUtil.runOnLayout( ()->{
                new jmri.jmrix.can.cbus.simulator.CbusSimulator(adapterMemo);
            });
        }
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
    public boolean sendPacket(byte[] packet, int repeats) {

        if (repeats < 1) {
            repeats = 1;
            log.warn("Ops Mode Accessory Packet 'Send count' of < 1 is illegal and is forced to 1.");
        }
        if (repeats > 8) {
            repeats = 8;
            log.warn("Ops Mode Accessory Packet 'Send count' reduced to 8.");
        }        

        CanMessage m = new CanMessage(2 + packet.length, tc.getCanid());     // Account for opcode and repeat
        int j = 0; // counter of byte in input packet

        m.setElement(0, CbusConstants.CBUS_RDCC3 + (((packet.length - 3) & 0x3) << 5));
        m.setElement(1, repeats);   // repeat

        // add each byte of the input message
        for (j = 0; j < packet.length; j++) {
            m.setElement(j + 2, packet[j] & 0xFF);
        }

        tc.sendCanMessage(m, null);
        return true;
    }

    /**
     * Release a session freeing up the slot for reuse.
     *
     * @param handle the handle for the session to be released
     */
    protected void releaseSession(int handle) {
        // Send KLOC
        CanMessage msg = new CanMessage(2, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_KLOC);
        msg.setElement(1, handle);
        log.debug("Release session handle {}", handle);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Send keep alive (DKEEP) packet for a throttle.
     *
     */
    protected void sendKeepAlive(int handle) {
        CanMessage msg = new CanMessage(2, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_DKEEP);
        msg.setElement(1, handle);
        log.debug("keep alive handle: {}", handle);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Set loco speed and direction.
     *
     * @param handle    The handle of the session to which it applies
     * @param speed_dir Bit 7 is direction (1 = forward) 6:0 are speed
     */
    protected void setSpeedDir(int handle, int speed_dir) {
        CanMessage msg = new CanMessage(3, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_DSPD);
        msg.setElement(1, handle);
        msg.setElement(2, speed_dir);
        log.debug("setSpeedDir session handle {} speedDir {}", handle, speed_dir);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Send a CBUS message to set functions.
     *
     * @param group     The function group
     * @param handle    The handle of the session for the loco being controlled
     * @param functions Function bits
     */
    protected void setFunctions(int group, int handle, int functions) {
        log.debug("Set function group {} Fns {} for session handle {}", group, functions, handle);
        CanMessage msg = new CanMessage(4, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_DFUN);
        msg.setElement(1, handle);
        msg.setElement(2, group);
        msg.setElement(3, functions);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Send a CBUS message to change the session speed step mode.
     *
     * @param mode the speed step mode
     */
    protected void setSpeedSteps(int handle, int mode) {
        log.debug("Set speed step mode {} for session handle {}", mode, handle);
        CanMessage msg = new CanMessage(3, tc.getCanid());
        msg.setOpCode(CbusConstants.CBUS_STMOD);
        msg.setElement(1, handle);
        msg.setElement(2, mode);
        tc.sendCanMessage(msg, null);
    }

    /**
     * Get the master command station from the CBUS Node Table
     * <p>
     * Full CBUS spec is defined as to comply with CBUS Developers Guide Version 6b
     * <p>
     * eg. CANCMD FW v3 supports the main loco OPCs but not full spec, will return null.
     * eg. CANCMD FW v4 supports the full steal / share spec, will return the CbusNode.
     *
     * @return the Master Command Station, else null if not found
     */
    protected CbusNode getMasterCommandStation(){
        if ( jmri.InstanceManager.getNullableDefault(CbusNodeTableDataModel.class) != null ) {
            CbusNodeTableDataModel nodeModel =  jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
            return nodeModel.getCsByNum(0);
        }
        return null;
    }
    
    // we can't rely on the command station flags at present so instead we check the
    // node variables of master command station 0
    
    /**
     * Get if Steal is available on the Command Station
     * <p>
     * Steal availability can change, so CbusThrottleManager checks this value
     * when it struggles on initial attempt to obtain a throttle 
     */
    protected boolean isStealAvailable() {
        if ( getMasterCommandStation() != null ) {
            log.debug("found master command station");
            if ( getMasterCommandStation().getNV(2) > -1 ){
                log.debug("nv 2 has a value");
                return (( getMasterCommandStation().getNV(2) >> 1 ) & 1) != 0; // NV2 bit 1 set
            }
        }
        return false;
    }
    
    /**
     * Get if Share is available on the Command Station
     * <p>
     * Share availability can change, so CbusThrottleManager checks this value
     * when it struggles on initial attempt to obtain a throttle 
     */
    protected boolean isShareAvailable() {
        if ( getMasterCommandStation() != null ) { // command station found
            if ( getMasterCommandStation().getNV(2) > -1 ){ // NV2 is set
                return (( getMasterCommandStation().getNV(2) >> 2 ) & 1) != 0; // NV2 bit 2 set
            }
        }
        return false;
    }

    @Override
    public String getUserName() {
        return adapterMemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        return adapterMemo.getSystemPrefix();
    }
    
    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(CbusCommandStation.class);

}
