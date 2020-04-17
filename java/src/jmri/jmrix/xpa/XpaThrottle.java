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
        this.speedSetting = 0;
        // Functions default to false
        this.speedvalue = 0;
        tc = t;
        if (log.isDebugEnabled()) {
            log.debug("XpaThrottle constructor called for address " + address);
        }
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
                // Increase the speed
                int diff = speedvalue - value;
                m = XpaMessage.getDecSpeedMsg(this.address, diff);
                tc.sendXpaMessage(m, null);
            }
        }
        this.speedSetting = speed;
        this.speedvalue = value;
    }

    // Direction
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
    
    private void setF(int func, boolean value){
        if (getFunction(func)!=value){
            updateFunction(func,value);    
            tc.sendXpaMessage(XpaMessage.getFunctionMsg(address, 0), null);
        }
    }

    // functions 
    @Override
    public void setF0(boolean f0) {
        setF(0,f0);
    }

    @Override
    public void setF1(boolean f1) {
        setF(1,f1);
    }

    @Override
    public void setF2(boolean f2) {
        setF(2,f2);
    }

    @Override
    public void setF3(boolean f3) {
        setF(3,f3);
    }

    @Override
    public void setF4(boolean f4) {
        setF(4,f4);
    }

    @Override
    public void setF5(boolean f5) {
        setF(5,f5);
    }

    @Override
    public void setF6(boolean f6) {
        setF(6,f6);
    }

    @Override
    public void setF7(boolean f7) {
        setF(7,f7);
    }

    @Override
    public void setF8(boolean f8) {
        setF(8,f8);
    }

    @Override
    public void setF9(boolean f9) {
        setF(9,f9);
    }

    @Override
    public void setF10(boolean f10) {
        setF(10,f10);
    }

    @Override
    public void setF11(boolean f11) {
        setF(11,f11);
    }

    @Override
    public void setF12(boolean f12) {
        setF(12,f12);
    }

    @Override
    public void sendFunctionGroup1() {
    }

    @Override
    public void sendFunctionGroup2() {
    }

    @Override
    public void sendFunctionGroup3() {
    }

    @Override
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, XpaThrottleManager.isLongAddress(address));
    }

    @Override
    protected void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XpaThrottle.class);

}
