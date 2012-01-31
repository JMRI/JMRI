package jmri.jmrix.can.cbus;

import java.util.HashMap;
import java.util.Iterator;
import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;

import javax.swing.JOptionPane;
import jmri.DccThrottle;

/**
 * CBUS implementation of a ThrottleManager.
 * <P>
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @author				Andrew Crosland  Copyright (C) 2009
 * @version 		$Revision$
 */
public class CbusThrottleManager extends AbstractThrottleManager implements ThrottleManager, CanListener{
    private boolean _handleExpected = false;
    private int _intAddr;
    private DccLocoAddress _dccAddr;

    private HashMap<Integer, CbusThrottle> softThrottles = new HashMap<Integer, CbusThrottle>(CbusConstants.CBUS_MAX_SLOTS);
    
    public CbusThrottleManager(CanSystemConnectionMemo memo) {
    	super(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }
    
    TrafficController tc;


	/**
	 * CBUS allows only one throttle per address
     */
	protected boolean singleUse() { return true; }

    /**
     * Request a new throttle object be created for the address
     **/
	synchronized public void requestThrottleSetup(LocoAddress address, boolean control) {
        _dccAddr = (DccLocoAddress)address;
        _intAddr = _dccAddr.getNumber();

        // The CBUS protocol requires that we request a session from the command
        // station. Throttle object will be notified by Command Station
        log.debug("Requesting session for throttle");
                
        CanMessage msg = new CanMessage(3, tc.getCanid());
        // Request a session for this throttle
        msg.setOpCode(CbusConstants.CBUS_RLOC);
        if (((DccLocoAddress)address).isLongAddress()) {
            _intAddr = _intAddr | 0xC000;
        }
        msg.setElement(1, (_intAddr / 256));
        msg.setElement(2, _intAddr & 0xff);
        tc.sendCanMessage(msg, this);
        _handleExpected = true;
        startThrottleRequestTimer();
	}

    /**
     * stopAll()
     * 
     * <P>
     * Called when track stopped message received. Sets all JMRI managed
     * throttles to speed zero
     */
    void stopAll() {
        // Get set of handles for JMRI managed throttles and
        // iterate over them setting the speed of each throttle to 0
        log.debug("stopAll() setting all speeds to emergency stop");
        Iterator<Integer> itr = softThrottles.keySet().iterator();
        while (itr.hasNext()) {
            CbusThrottle throttle = softThrottles.get(itr.next());
            throttle.setSpeedSetting(0.0F);
        }
    }

    public void message(CanMessage m) {
        int opc = m.getElement(0);
        int handle;

        switch (opc) {
            case CbusConstants.CBUS_ESTOP:
            case CbusConstants.CBUS_RESTP:
                stopAll();
                break;

            case CbusConstants.CBUS_KLOC:
                // Kill loco
                log.debug("Kill loco message");
                handle = m.getElement(1);
                softThrottles.remove(handle);
                break;

            default:
                break;
        }
    }

    synchronized public void reply(CanReply m) {
        int opc = m.getElement(0);
        int rcvdIntAddr;
        boolean rcvdIsLong;
        DccLocoAddress rcvdDccAddr;
        int handle;

        switch (opc) {
            case CbusConstants.CBUS_PLOC:
                rcvdIntAddr = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
                rcvdIsLong = (m.getElement(2) & 0xc0) > 0;
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                log.debug("Throttle manager received PLOC with session handle " + m.getElement(1) + " for address " + rcvdIntAddr);
                if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                    log.debug("PLOC was expected");
                    // We're expecting an engine report and it matches our address
                    handle = m.getElement(1);
                    CbusThrottle throttle;
                    throttleRequestTimer.stop();
                    throttle = new CbusThrottle((CanSystemConnectionMemo)adapterMemo, rcvdDccAddr, handle);
                    notifyThrottleKnown(throttle, rcvdDccAddr);
                    softThrottles.put(handle, throttle);
                    _handleExpected = false;
                }
                break;

            case CbusConstants.CBUS_ERR:
                rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
                rcvdIsLong = (m.getElement(1) & 0xc0) > 0;
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                log.debug("Throttle manager received ERR " + m.getElement(3) + " for address " + rcvdIntAddr);
                if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                    // We're expecting an engine report and it matches our address
                    _handleExpected = false;
                    log.debug("PLOC expected but received ERR");
                    throttleRequestTimer.stop();
                    String message = "";
                    switch (m.getElement(3)) {
                        case CbusConstants.ERR_ADDR_FULL:
                            message = "Loco stack is full.";
                            JOptionPane.showMessageDialog(null, message);
                            break;

                        case CbusConstants.ERR_ADDR_TAKEN:
                            message = "Address in use by another throttle.";
                            JOptionPane.showMessageDialog(null, message);
                            break;
                        default:
                            break;
                    }
                    failedThrottleRequest(_dccAddr, message);
                }
                break;

            case CbusConstants.CBUS_ESTOP:
            case CbusConstants.CBUS_RESTP:
                stopAll();
                break;

            default:
                break;
        }
    }

    /**
     * CBUS does not have a Dispatch function
     **/
    public boolean hasDispatchFunction(){ return false; }

    /**
     * Address 128 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }
    
    /**
     * Address 127 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=128);
    }

    javax.swing.Timer throttleRequestTimer = null;

	/**
     * Start timer to wait for command station to respond to RLOC
     */
    protected void startThrottleRequestTimer() {
        throttleRequestTimer = new javax.swing.Timer(5000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                timeout();
            }
        });
        throttleRequestTimer.setRepeats(false);
        throttleRequestTimer.start();
    }

    /**
     * Internal routine to notify failed throttle request a timeout
     */
    synchronized protected void timeout() {
        log.debug("Throttle request (RLOC) timed out");
        failedThrottleRequest(_dccAddr, "Throttle request (RLOC) timed out");
        throttleRequestTimer.stop();
    }

    /**
     * What speed modes are supported by this system?                       
     * value should be xor of possible modes specifed by the 
     * DccThrottle interface
     */
    public int supportedSpeedModes() {
        return(DccThrottle.SpeedStepMode128
                | DccThrottle.SpeedStepMode28
                | DccThrottle.SpeedStepMode14);
    }
    
    public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l){
        log.debug("disposeThrottle called for " + t);
        if ( super.disposeThrottle(t, l)){
            CbusThrottle lnt = (CbusThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusThrottleManager.class.getName());
}
