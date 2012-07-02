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
    @Override
    protected boolean singleUse() { return true; }

    /**
     * Request a new throttle object be created for the address
     **/
    @Override
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

    @Override
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

    @Override
    synchronized public void reply(CanReply m) {
        int opc = m.getElement(0);
        int rcvdIntAddr = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
        boolean rcvdIsLong = (m.getElement(2) & 0xc0) > 0;
        int handle = m.getElement(1);
        int errCode = m.getElement(3);
        DccLocoAddress rcvdDccAddr;
        String errStr = "";
        Iterator<Integer> itr;

        switch (opc) {
            case CbusConstants.CBUS_PLOC:
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                log.debug("Throttle manager received PLOC with session handle " + m.getElement(1) + " for address " + rcvdIntAddr);
                if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                    log.debug("PLOC was expected");
                    // We're expecting an engine report and it matches our address
                    handle = m.getElement(1);
                    CbusThrottle throttle;
                    throttleRequestTimer.stop();
                    throttle = new CbusThrottle((CanSystemConnectionMemo) adapterMemo, rcvdDccAddr, handle);
                    // Initialise throttle from PLOC data to allow taking over moving trains
                    throttle.CbusThrottleInit(m.getElement(4), m.getElement(5), m.getElement(6), m.getElement(7));
                    notifyThrottleKnown(throttle, rcvdDccAddr);
                    softThrottles.put(handle, throttle);
                    _handleExpected = false;
                }
                break;

            case CbusConstants.CBUS_ERR:
                // TODO: should be a better way to do this with constants or properties
                switch (errCode) {
                    case CbusConstants.ERR_LOCO_STACK_FULL:
                        errStr = "loco stack full for address " + rcvdIntAddr;
                        break;
                    case CbusConstants.ERR_LOCO_ADDRESS_TAKEN:
                        errStr = "loco address taken for address " + rcvdIntAddr;
                        break;
                    case CbusConstants.ERR_INVALID_REQUEST:
                        errStr = "invalid request for address " + rcvdIntAddr;
                        break;
                    case CbusConstants.ERR_SESSION_NOT_PRESENT:
                        errStr = "session not present for session " + handle;
                        break;
                    case CbusConstants.ERR_CONSIST_EMPTY:
                        errStr = "consist empty for consist " + handle;
                        break;
                    case CbusConstants.ERR_LOCO_NOT_FOUND:
                        errStr = "loco not found for session " + handle;
                        break;
                    case CbusConstants.ERR_CAN_BUS_ERROR:
                        errStr = "CAN bus error";
                        break;
                    case CbusConstants.ERR_SESSION_CANCELLED:
                        errStr = "Throttle session cancelled for loco ";
                        break;
                }
                
                log.debug("Throttle manager received ERR " + errStr);
                rcvdDccAddr = new DccLocoAddress(rcvdIntAddr, rcvdIsLong);
                switch (errCode) {
                    case CbusConstants.ERR_LOCO_STACK_FULL:
                    case CbusConstants.ERR_LOCO_ADDRESS_TAKEN:
                        log.debug("PLOC expected but received ERR address" + rcvdDccAddr.toString());
                        if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                            // We were expecting an engine report and it matches our address
                            log.debug("Failed throttle request due to ERR");
                            _handleExpected = false;
                            throttleRequestTimer.stop();
                            JOptionPane.showMessageDialog(null, "CBUS ERR:" + errStr);
                            failedThrottleRequest(_dccAddr, "CBUS ERR:" + errStr);
                        } else {
                            log.debug("ERR address not matched");
                        }
                        break;
                    
                    case CbusConstants.ERR_SESSION_NOT_PRESENT:
                        if ((_handleExpected) && rcvdDccAddr.equals(_dccAddr)) {
                            // We were expecting an engine report and it matches our address
                            _handleExpected = false;
                        }
                        JOptionPane.showMessageDialog(null, "CBUS ERR:" + errStr);
                        break;
                        
                    case CbusConstants.ERR_CONSIST_EMPTY:
                    case CbusConstants.ERR_LOCO_NOT_FOUND:
                        // Ignore for now CAN_CMD only supports advanced consisting
                        // and will never issue these errors
                        break;

                    case CbusConstants.ERR_CAN_BUS_ERROR:
                    case CbusConstants.ERR_INVALID_REQUEST:
                        JOptionPane.showMessageDialog(null, "CBUS ERR:" + errStr);
                        break;

                    case CbusConstants.ERR_SESSION_CANCELLED:
                        // There will be a session cancelled error for the other throttle(s)
                        // when you are stealing, but as you don't yet have a session id, it
                        // won't match so you will ignore it, then a PLOC will come with that
                        // session id and your requested loco number which is giving it to you.
 
                        // Inform the throttle associated with this session handle, if any
                        itr = softThrottles.keySet().iterator();
                        while (itr.hasNext()) {
                            CbusThrottle throttle = softThrottles.get(itr.next());
                            if (throttle.getHandle() == handle) {
                                JOptionPane.showMessageDialog(null, errStr + throttle.getLocoAddress().toString());
                                throttle.throttleTimedOut();
                                // Attempt to dispode of the throttle
                                super.disposeThrottle(throttle, null);
                                break;
                            }
                        }

                        break;

                    default:
                        break;
                }

            case CbusConstants.CBUS_DSPD:
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        // Set the throttle session to match the DSPD packet received
                        throttle.updateSpeedSetting(m.getElement(2) & 0x7f);
                        throttle.updateIsForward((m.getElement(2) & 0x80) == 0x80);
                    }
                }
                break;

            case CbusConstants.CBUS_DFUN:
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        // Set the throttle session to match the DFUN packet received
                        log.debug("DFUN group: "+m.getElement(2)+" Fns: "+m.getElement(3)+" for session: "+m.getElement(1));
                        switch (m.getElement(2)) {
                            case 1:
                                throttle.updateFunctionGroup1(m.getElement(3));
                                break;
                            case 2:
                                throttle.updateFunctionGroup2(m.getElement(3));
                                break;
                            case 3:
                                throttle.updateFunctionGroup3(m.getElement(3));
                                break;
                            case 4:
                                throttle.updateFunctionGroup4(m.getElement(3));
                                break;
                            case 5:
                                throttle.updateFunctionGroup5(m.getElement(3));
                                break;
                            default:
                                log.error("Unrecognised function group");
                                break;
                        }
                    }
                }
            break;
				
            case CbusConstants.CBUS_DFNON:
            case CbusConstants.CBUS_DFNOF:
                // Find a throttle corresponding to the handle
                itr = softThrottles.keySet().iterator();
                while (itr.hasNext()) {
                    CbusThrottle throttle = softThrottles.get(itr.next());
                    if (throttle.getHandle() == handle) {
                        throttle.updateFunction(m.getElement(2), (opc == CbusConstants.CBUS_DFNON) ? true : false);
                    }
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
    @Override
    public boolean hasDispatchFunction(){ return false; }

    /**
     * Any address is potentially a long address
     **/
    @Override
    public boolean canBeLongAddress(int address) {
        return true;
    }
    
    /**
     * Address 127 and below is a short address
     **/
    @Override
    public boolean canBeShortAddress(int address) {
        if (address < 128) { return true; }
        return false;
    }

    /**
     * Short and long address spaces overlap and are not unique
     */
    @Override
    public boolean addressTypeUnique() { return false; }

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
            @Override
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
    @Override
    public int supportedSpeedModes() {
        return(DccThrottle.SpeedStepMode128
                | DccThrottle.SpeedStepMode28
                | DccThrottle.SpeedStepMode14);
    }
    
    @Override
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
