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
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
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

    // functions 
    @Override
    public void setF0(boolean f0) {
        if (this.f0 != f0) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 0);
            tc.sendXpaMessage(m, null);
        }
        this.f0 = f0;
    }

    @Override
    public void setF1(boolean f1) {
        if (this.f1 != f1) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 1);
            tc.sendXpaMessage(m, null);
        }
        this.f1 = f1;
    }

    @Override
    public void setF2(boolean f2) {
        if (this.f2 != f2) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 2);
            tc.sendXpaMessage(m, null);
        }
        this.f2 = f2;
    }

    @Override
    public void setF3(boolean f3) {
        if (this.f3 != f3) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 3);
            tc.sendXpaMessage(m, null);
        }
        this.f3 = f3;
    }

    @Override
    public void setF4(boolean f4) {
        if (this.f4 != f4) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 4);
            tc.sendXpaMessage(m, null);
        }
        this.f4 = f4;
    }

    @Override
    public void setF5(boolean f5) {
        if (this.f5 != f5) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 5);
            tc.sendXpaMessage(m, null);
        }
        this.f5 = f5;
    }

    @Override
    public void setF6(boolean f6) {
        if (this.f6 != f6) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 6);
            tc.sendXpaMessage(m, null);
        }
        this.f6 = f6;
    }

    @Override
    public void setF7(boolean f7) {
        if (this.f7 != f7) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 7);
            tc.sendXpaMessage(m, null);
        }
        this.f7 = f7;
    }

    @Override
    public void setF8(boolean f8) {
        if (this.f8 != f8) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 8);
            tc.sendXpaMessage(m, null);
        }
        this.f8 = f8;
    }

    @Override
    public void setF9(boolean f9) {
        if (this.f9 != f9) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 9);
            tc.sendXpaMessage(m, null);
        }
        this.f9 = f9;
    }

    @Override
    public void setF10(boolean f10) {
        if (this.f10 != f10) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 10);
            tc.sendXpaMessage(m, null);
        }
        this.f10 = f10;
    }

    @Override
    public void setF11(boolean f11) {
        if (this.f11 != f11) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 11);
            tc.sendXpaMessage(m, null);
        }
        this.f11 = f11;
    }

    @Override
    public void setF12(boolean f12) {
        if (this.f12 != f12) {
            XpaMessage m = XpaMessage.getFunctionMsg(address, 12);
            tc.sendXpaMessage(m, null);
        }
        this.f12 = f12;
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
