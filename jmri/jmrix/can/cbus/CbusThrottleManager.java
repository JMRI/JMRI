package jmri.jmrix.can.cbus;

import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanListener;

import javax.swing.JOptionPane;
import jmri.DccThrottle;

/**
 * CBUS implementation of a ThrottleManager.
 * <P>
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @author				Andrew Crosland  Copyright (C) 2009
 * @version 		$Revision: 1.9 $
 */
public class CbusThrottleManager extends AbstractThrottleManager implements ThrottleManager, CanListener{
    private boolean _handleExpected = false;
    private int _intAddr;
    private DccLocoAddress _dccAddr;

    /**
     * Constructor. Gets a reference to the Cbus CommandStation.
     */
    public CbusThrottleManager() {
    	super();
    }

	/**
	 * CBUS allows only one throttle per address
     */
	protected boolean singleUse() { return true; }

    /**
     * Request a new throttle object be created for the address
     **/
	synchronized public void requestThrottleSetup(LocoAddress address) {
        _dccAddr = (DccLocoAddress)address;
        _intAddr = _dccAddr.getNumber();

        // The CBUS protocol requires that we request a session from the command
        // station. Throttle object will be notified by Command Station
        log.debug("Requesting session for throttle");
                
        CanMessage msg = new CanMessage(3);
        // Request a session for this throttle
        msg.setOpCode(CbusConstants.CBUS_RLOC);
        if (((DccLocoAddress)address).isLongAddress()) {
            _intAddr = _intAddr | 0xC000;
        }
        msg.setElement(1, (_intAddr / 256));
        msg.setElement(2, _intAddr & 0xff);
        TrafficController.instance().sendCanMessage(msg, this);
        _handleExpected = true;
        startThrottleRequestTimer();
	}

    public void message(CanMessage m) {
    }

    synchronized public void reply(CanReply m) {
        int opc = m.getElement(0);
        int rcvdIntAddr;
        boolean rcvdIsLong;
        DccLocoAddress rcvdDccAddr;
        int handle;

        log.debug("Command station received reply " + m.toString());

        switch (opc) {
            case CbusConstants.CBUS_PLOC:
                rcvdIntAddr = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
                rcvdIsLong = (m.getElement(2) & 0xc0) > 0;
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                log.debug("Command station received PLOC with session handle " + m.getElement(1) + " for address " + rcvdIntAddr);
                if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                    log.debug("PLOC was expected");
                    // We're expecting an engine report and it matches our address
                    handle = m.getElement(1);
                    CbusThrottle throttle;
                    throttleRequestTimer.stop();
                    throttle = new CbusThrottle(rcvdDccAddr, handle);
                    notifyThrottleKnown(throttle, rcvdDccAddr);
                    _handleExpected = false;
                }
                break;

            case CbusConstants.CBUS_ERR:
                rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
                rcvdIsLong = (m.getElement(1) & 0xc0) > 0;
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                log.debug("Command station received ERR " + m.getElement(3) + " for address " + rcvdIntAddr);
                if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                    // We're expecting an engine report and it matches our address
                    _handleExpected = false;
                    log.debug("PLOC expected but received ERR");
                    throttleRequestTimer.stop();
                    switch (m.getElement(3)) {
                        case CbusConstants.ERR_ADDR_FULL:
                            JOptionPane.showMessageDialog(null, "Loco stack is full.");
                            break;

                        case CbusConstants.ERR_ADDR_TAKEN:
                            JOptionPane.showMessageDialog(null, "Address in use by another throttle.");
                            break;
                    }
                    failedThrottleRequest(_dccAddr);
                }
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
        failedThrottleRequest(_dccAddr);
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
    


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusThrottleManager.class.getName());
}
