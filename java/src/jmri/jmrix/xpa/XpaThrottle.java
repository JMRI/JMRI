package jmri.jmrix.xpa;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An XPA+Modem implementation of the Throttle for XpressNet Systems
 *
 * @author Paul Bender Copyright (C) 2004
 */
public class XpaThrottle extends AbstractThrottle {

    private int speedvalue;
    private final int address;
    private final XpaTrafficController tc;

    /**
     * Create a throttle.
     *
     * @param address the address for the throttle
     * @param t the controller for the system connection
     */
    public XpaThrottle(LocoAddress address, XpaTrafficController t) {
        super(null);
        this.address = address.getNumber();
        this.speedStepMode = SpeedStepMode.INCREMENTAL;
        this.isForward = true;
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.speedvalue = 0;
        tc = t;
        log.debug("XpaThrottle constructor called for address {}", address);
    }

    /**
     * Set the speed and direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSetting(float speed) {
        super.setSpeedSetting(speed);
        int value = (int) ((127) * speed);
        if (value > 127) {
            value = 127;    // max possible speed
        }
        if (this.speedvalue != value) {
            XpaMessage m;
            if (value < 0) {
                value = 0;        // emergency stop
                m = XpaMessage.getEStopMsg();
                tc.sendXpaMessage(m, null);
            } else if (value == 0) {
                m = XpaMessage.getIdleMsg(address);
                tc.sendXpaMessage(m, null);
            } else if (value > this.speedvalue) {
                // Increase the speed
                int diff = value - speedvalue;
                m = XpaMessage.getIncSpeedMsg(this.address, diff);
                tc.sendXpaMessage(m, null);
            } else if (value < this.speedvalue) {
                // Decrease the speed
                int diff = speedvalue - value;
                m = XpaMessage.getDecSpeedMsg(this.address, diff);
                tc.sendXpaMessage(m, null);
            }
        }
        synchronized(this) {
            this.speedSetting = speed;
        }
        this.speedvalue = value;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setIsForward(boolean forward) {
        super.setIsForward(forward);
        XpaMessage m;
        if (forward) {
            m = XpaMessage.getDirForwardMsg(address);
        } else {
            m = XpaMessage.getDirReverseMsg(address);
        }
        tc.sendXpaMessage(m, null);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void setFunction(int func, boolean value){
        if ( func>=0 && func<13 && getFunction(func)!=value){
            updateFunction(func,value);    
            tc.sendXpaMessage(XpaMessage.getFunctionMsg(address, 0), null);
        }
        else {
            super.setFunction(func,value);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionGroup1() {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionGroup2() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionGroup3() {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, XpaThrottleManager.isLongAddress(address));
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XpaThrottle.class);

}
