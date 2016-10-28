package jmri.jmrix.can.cbus;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific to a
 * Cbus connection.
 * <P>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in CBUS
 * is an int with values from 0 to 127.
 * <P>
 * @author Andrew Crosland Copyright (C) 2009
 */
public class CbusThrottle extends AbstractThrottle {

    private CbusCommandStation cs = null;
    private int _handle = -1;
    private DccLocoAddress dccAddress = null;

    /**
     * Constructor
     *
     * @param address The address this throttle relates to.
     */
    public CbusThrottle(CanSystemConnectionMemo memo, LocoAddress address, int handle) {
        super(memo);

        log.debug("Throttle created");
        cs = (CbusCommandStation) adapterMemo.get(jmri.CommandStation.class);
        _handle = handle;

        // cache settings
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
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;

        // extended values
        this.f13 = false;
        this.f14 = false;
        this.f15 = false;
        this.f16 = false;
        this.f17 = false;
        this.f18 = false;
        this.f19 = false;
        this.f20 = false;
        this.f21 = false;
        this.f22 = false;
        this.f23 = false;
        this.f24 = false;
        this.f25 = false;
        this.f26 = false;
        this.f27 = false;
        this.f28 = false;

        this.dccAddress = (DccLocoAddress) address;
        this.isForward = true;

//        switch(slot.decoderType())
//        {
//            case CbusConstants.DEC_MODE_128:
//            case CbusConstants.DEC_MODE_128A: this.speedIncrement = 1; break;
//            case CbusConstants.DEC_MODE_28:
//            case CbusConstants.DEC_MODE_28A:
//            case CbusConstants.DEC_MODE_28TRI: this.speedIncrement = 4; break;
//            case CbusConstants.DEC_MODE_14: this.speedIncrement = 8; break;
//        }
        // Only 128 speed step supported at the moment
        this.speedIncrement = 1;

        // start periodically sending keep alives, to keep this
        // attached
        log.debug("Start Throttle refresh");
        startRefresh();

    }

    /**
     * Set initial throttle values as taken from PLOC reply from hardware
     *
     */
    public void throttleInit(int speed, int f0f4, int f5f8, int f9f12) {

        log.debug("Setting throttle initial values");

        // cache settings
        this.speedSetting = speed & 0x7f;
        this.f0 = (f0f4 & CbusConstants.CBUS_F0) == CbusConstants.CBUS_F0;
        this.f1 = (f0f4 & CbusConstants.CBUS_F1) == CbusConstants.CBUS_F1;
        this.f2 = (f0f4 & CbusConstants.CBUS_F2) == CbusConstants.CBUS_F2;
        this.f3 = (f0f4 & CbusConstants.CBUS_F3) == CbusConstants.CBUS_F3;
        this.f4 = (f0f4 & CbusConstants.CBUS_F4) == CbusConstants.CBUS_F4;
        this.f5 = (f5f8 & CbusConstants.CBUS_F5) == CbusConstants.CBUS_F5;
        this.f6 = (f5f8 & CbusConstants.CBUS_F6) == CbusConstants.CBUS_F6;
        this.f7 = (f5f8 & CbusConstants.CBUS_F7) == CbusConstants.CBUS_F7;
        this.f8 = (f5f8 & CbusConstants.CBUS_F8) == CbusConstants.CBUS_F8;
        this.f9 = (f9f12 & CbusConstants.CBUS_F9) == CbusConstants.CBUS_F9;
        this.f10 = (f9f12 & CbusConstants.CBUS_F10) == CbusConstants.CBUS_F10;
        this.f11 = (f9f12 & CbusConstants.CBUS_F11) == CbusConstants.CBUS_F11;
        this.f12 = (f9f12 & CbusConstants.CBUS_F12) == CbusConstants.CBUS_F12;

        this.isForward = (speed & 0x80) == 0x80;
    }

    /**
     * setSpeedStepMode - set the speed step value.
     * <P>
     * Overridden to capture mode changes to be forwarded to the hardware.
     * New throttles default to 128 step
     * mode
     * <P>
     * @param Mode the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
    @Override
    public void setSpeedStepMode(int Mode) {
        int mode;
        speedStepMode = Mode;
        super.setSpeedStepMode(speedStepMode);
        switch (speedStepMode) {
            case DccThrottle.SpeedStepMode28:
                mode = CbusConstants.CBUS_SS_28;
                break;
            case DccThrottle.SpeedStepMode14:
                mode = CbusConstants.CBUS_SS_14;
                break;
            default:
                mode = CbusConstants.CBUS_SS_128;
                break;
        }
        cs.setSpeedSteps(_handle, mode);
    }

    /**
     * Convert a CBUS speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) {
            return 0.f;
        } else if (lSpeed == 1) {
            return -1.f;   // estop
        } else {
            return ((lSpeed - 1) / 126.f);
        }
    }

    /**
     * Send the CBUS message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        int new_fn = ((getF0() ? CbusConstants.CBUS_F0 : 0)
                | (getF1() ? CbusConstants.CBUS_F1 : 0)
                | (getF2() ? CbusConstants.CBUS_F2 : 0)
                | (getF3() ? CbusConstants.CBUS_F3 : 0)
                | (getF4() ? CbusConstants.CBUS_F4 : 0));
        cs.setFunctions(1, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        int new_fn = ((getF5() ? CbusConstants.CBUS_F5 : 0)
                | (getF6() ? CbusConstants.CBUS_F6 : 0)
                | (getF7() ? CbusConstants.CBUS_F7 : 0)
                | (getF8() ? CbusConstants.CBUS_F8 : 0));
        cs.setFunctions(2, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F9, F10, F11, F12
     */
    @Override
    protected void sendFunctionGroup3() {
        int new_fn = ((getF9() ? CbusConstants.CBUS_F9 : 0)
                | (getF10() ? CbusConstants.CBUS_F10 : 0)
                | (getF11() ? CbusConstants.CBUS_F11 : 0)
                | (getF12() ? CbusConstants.CBUS_F12 : 0));
        cs.setFunctions(3, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F13, F14, F15, F16,
     * F17, F18, F19, F20
     */
    @Override
    protected void sendFunctionGroup4() {
        int new_fn = ((getF13() ? CbusConstants.CBUS_F13 : 0)
                | (getF14() ? CbusConstants.CBUS_F14 : 0)
                | (getF15() ? CbusConstants.CBUS_F15 : 0)
                | (getF16() ? CbusConstants.CBUS_F16 : 0)
                | (getF17() ? CbusConstants.CBUS_F17 : 0)
                | (getF18() ? CbusConstants.CBUS_F18 : 0)
                | (getF19() ? CbusConstants.CBUS_F19 : 0)
                | (getF20() ? CbusConstants.CBUS_F20 : 0));
        cs.setFunctions(4, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of functions F21, F22, F23, F24,
     * F25, F26, F27, F28
     */
    @Override
    protected void sendFunctionGroup5() {
        int new_fn = ((getF21() ? CbusConstants.CBUS_F21 : 0)
                | (getF22() ? CbusConstants.CBUS_F22 : 0)
                | (getF23() ? CbusConstants.CBUS_F23 : 0)
                | (getF24() ? CbusConstants.CBUS_F24 : 0)
                | (getF25() ? CbusConstants.CBUS_F25 : 0)
                | (getF26() ? CbusConstants.CBUS_F26 : 0)
                | (getF27() ? CbusConstants.CBUS_F27 : 0)
                | (getF28() ? CbusConstants.CBUS_F28 : 0));
        cs.setFunctions(5, _handle, new_fn);
    }

    /**
     * Update the state of locomotive functions F0, F1, F2, F3, F4 in response
     * to a message from the hardware
     */
    protected void updateFunctionGroup1(int fns) {
        this.f0 = ((fns & CbusConstants.CBUS_F0) == CbusConstants.CBUS_F0);
        this.f1 = ((fns & CbusConstants.CBUS_F1) == CbusConstants.CBUS_F1);
        this.f2 = ((fns & CbusConstants.CBUS_F2) == CbusConstants.CBUS_F2);
        this.f3 = ((fns & CbusConstants.CBUS_F3) == CbusConstants.CBUS_F3);
        this.f4 = ((fns & CbusConstants.CBUS_F4) == CbusConstants.CBUS_F4);
    }

    /**
     * Update the state of locomotive functions F5, F6, F7, F8 in response to a
     * message from the hardware
     */
    protected void updateFunctionGroup2(int fns) {
        this.f5 = ((fns & CbusConstants.CBUS_F5) == CbusConstants.CBUS_F5);
        this.f6 = ((fns & CbusConstants.CBUS_F6) == CbusConstants.CBUS_F6);
        this.f7 = ((fns & CbusConstants.CBUS_F7) == CbusConstants.CBUS_F7);
        this.f8 = ((fns & CbusConstants.CBUS_F8) == CbusConstants.CBUS_F8);
    }

    /**
     * Update the state of locomotive functions F9, F10, F11, F12 in response to
     * a message from the hardware
     */
    protected void updateFunctionGroup3(int fns) {
        this.f9 = ((fns & CbusConstants.CBUS_F9) == CbusConstants.CBUS_F9);
        this.f10 = ((fns & CbusConstants.CBUS_F10) == CbusConstants.CBUS_F10);
        this.f11 = ((fns & CbusConstants.CBUS_F11) == CbusConstants.CBUS_F11);
        this.f12 = ((fns & CbusConstants.CBUS_F12) == CbusConstants.CBUS_F12);
    }

    /**
     * Update the state of locomotive functions F13, F14, F15, F16, F17, F18,
     * F19, F20 in response to a message from the hardware
     */
    protected void updateFunctionGroup4(int fns) {
        this.f13 = ((fns & CbusConstants.CBUS_F13) == CbusConstants.CBUS_F13);
        this.f14 = ((fns & CbusConstants.CBUS_F14) == CbusConstants.CBUS_F14);
        this.f15 = ((fns & CbusConstants.CBUS_F15) == CbusConstants.CBUS_F15);
        this.f16 = ((fns & CbusConstants.CBUS_F16) == CbusConstants.CBUS_F16);
        this.f17 = ((fns & CbusConstants.CBUS_F17) == CbusConstants.CBUS_F17);
        this.f18 = ((fns & CbusConstants.CBUS_F18) == CbusConstants.CBUS_F18);
        this.f19 = ((fns & CbusConstants.CBUS_F19) == CbusConstants.CBUS_F19);
        this.f20 = ((fns & CbusConstants.CBUS_F20) == CbusConstants.CBUS_F20);
    }

    /**
     * Update the state of locomotive functions F21, F22, F23, F24, F25, F26,
     * F27, F28 in response to a message from the hardware
     */
    protected void updateFunctionGroup5(int fns) {
        this.f21 = ((fns & CbusConstants.CBUS_F21) == CbusConstants.CBUS_F21);
        this.f22 = ((fns & CbusConstants.CBUS_F22) == CbusConstants.CBUS_F22);
        this.f23 = ((fns & CbusConstants.CBUS_F23) == CbusConstants.CBUS_F23);
        this.f24 = ((fns & CbusConstants.CBUS_F24) == CbusConstants.CBUS_F24);
        this.f25 = ((fns & CbusConstants.CBUS_F25) == CbusConstants.CBUS_F25);
        this.f26 = ((fns & CbusConstants.CBUS_F26) == CbusConstants.CBUS_F26);
        this.f27 = ((fns & CbusConstants.CBUS_F27) == CbusConstants.CBUS_F27);
        this.f28 = ((fns & CbusConstants.CBUS_F28) == CbusConstants.CBUS_F28);
    }

    /**
     * Update the state of a single function in response to a message fromn the
     * hardware
     */
    protected void updateFunction(int fn, boolean state) {
        switch (fn) {
            case 0:
                this.f0 = state;
                break;
            case 1:
                this.f1 = state;
                break;
            case 2:
                this.f2 = state;
                break;
            case 3:
                this.f3 = state;
                break;
            case 4:
                this.f4 = state;
                break;
            case 5:
                this.f5 = state;
                break;
            case 6:
                this.f6 = state;
                break;
            case 7:
                this.f7 = state;
                break;
            case 8:
                this.f8 = state;
                break;
            case 9:
                this.f9 = state;
                break;
            case 10:
                this.f10 = state;
                break;
            case 11:
                this.f11 = state;
                break;
            case 12:
                this.f12 = state;
                break;
            case 13:
                this.f13 = state;
                break;
            case 14:
                this.f14 = state;
                break;
            case 15:
                this.f15 = state;
                break;
            case 16:
                this.f16 = state;
                break;
            case 17:
                this.f17 = state;
                break;
            case 18:
                this.f18 = state;
                break;
            case 19:
                this.f19 = state;
                break;
            case 20:
                this.f20 = state;
                break;
            case 21:
                this.f21 = state;
                break;
            case 22:
                this.f22 = state;
                break;
            case 23:
                this.f23 = state;
                break;
            case 24:
                this.f24 = state;
                break;
            case 25:
                this.f25 = state;
                break;
            case 26:
                this.f26 = state;
                break;
            case 27:
                this.f27 = state;
                break;
            case 28:
                this.f28 = state;
                break;
        }
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed < 0) {
            this.speedSetting = -1.f;
        }

        int new_spd = intSpeed(speed);
        if (this.isForward) {
            new_spd = new_spd | 0x80;
        }
        log.debug("Sending speed/dir for speed: " + new_spd);
        cs.setSpeedDir(_handle, new_spd);

        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    /**
     * Update the throttles speed setting without sending to hardware. Used to
     * support CBUS sharing by taking speed received <b>from</b> the hardware in
     * an OPC_DSPD message.
     *
     * @param speed integer speed value
     */
    public void updateSpeedSetting(int speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = floatSpeed(speed);
        if (speed < 0) {
            this.speedSetting = -1.f;
        }

        int new_spd = speed;
        if (this.isForward) {
            new_spd = new_spd | 0x80;
        }
        log.debug("Updated speed/dir for speed: " + new_spd);

        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting);
        }
    }

    /**
     * Set the direction and reset speed.
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);
        if (old != isForward) {
            notifyPropertyChangeListener("IsForward", old, isForward);
        }
    }

    /**
     * Update the throttles direction without sending to hardware. Used to
     * support CBUS sharing by taking direction received <b>from</b> the
     * hardware in an OPC_DSPD message.
     *
     */
    public void updateIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        updateSpeedSetting(intSpeed(speedSetting));
        if (old != isForward) {
            notifyPropertyChangeListener("IsForward", old, isForward);
        }
    }

    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Return the handle for this throttle
     *
     * @return integer session handle
     */
    public int getHandle() {
        return _handle;
    }

    /**
     * Received a session not present error form command station saying the
     * session has timed out. This code is the same as throttleDispose() without
     * releasing the session that would trigger a KLOC message to the command
     * station.
     */
    public void throttleTimedOut() {
        _handle = -1;
        cs = null;

        // stop timeout
        mRefreshTimer.stop();

        mRefreshTimer = null;
        cs = null;

    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     */
    @Override
    public void throttleDispose() {
        log.debug("dispose");

        cs.releaseSession(_handle);
        _handle = -1;
        cs = null;

        // stop timeout
        mRefreshTimer.stop();

        mRefreshTimer = null;
        cs = null;
        finishRecord();
    }

    javax.swing.Timer mRefreshTimer = null;

    // CBUS command station expect DSPD every 4s
    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                keepAlive();
            }
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void keepAlive() {
        cs.sendKeepAlive(_handle);

        // reset timeout
        mRefreshTimer.stop();
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();

    }

    @Override
    public LocoAddress getLocoAddress() {
        return dccAddress;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CbusThrottle.class.getName());

}
