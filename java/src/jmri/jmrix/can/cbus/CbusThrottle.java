package jmri.jmrix.can.cbus;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.DccThrottle;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific
 * to a Cbus connection.
 * <P>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in CBUS is an int
 * with values from 0 to 127.
 * <P>
 * @author  Andrew Crosland Copyright (C) 2009
 * @version $Revision$
 */
public class CbusThrottle extends AbstractThrottle {
    private CbusCommandStation cs = null;
    private int address;
    private int _handle = -1;

    /**
     * Constructor
     * @param address The address this throttle relates to.
     */
    public CbusThrottle(CanSystemConnectionMemo memo, LocoAddress address, int handle) {
        super(memo);

        log.debug("Throttle created");
        DccLocoAddress dccAddress = (DccLocoAddress)address;
        cs = (CbusCommandStation) adapterMemo.get(jmri.CommandStation.class);
        _handle = handle;

        // cache settings
        this.speedSetting = 0;
        this.f0           = false;
        this.f1           = false;
        this.f2           = false;
        this.f3           = false;
        this.f4           = false;
        this.f5           = false;
        this.f6           = false;
        this.f7           = false;
        this.f8           = false;
        this.f8           = false;
        this.f9           = false;
        this.f10          = false;
        this.f11          = false;
        this.f12          = false;

        // extended values
        this.f13          = false;
        this.f14          = false;
        this.f15          = false;
        this.f16          = false;
        this.f17          = false;
        this.f18          = false;
        this.f19          = false;
        this.f20          = false;
        this.f21          = false;
        this.f22          = false;
        this.f23          = false;
        this.f24          = false;
        this.f25          = false;
        this.f26          = false;
        this.f27          = false;
        this.f28          = false;

        this.address      = dccAddress.getNumber();
        this.isForward    = true;

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

        // start periodically sending the speed, to keep this
        // attached
        log.debug("Start Throttle refresh");
        startRefresh();

    }

    /*
     * setSpeedStepMode - set the speed step value.
     * <P>
     * Overridden to capture mode changes to be forwarded to the hardware.
     * New throttles default to 128 step
     * mode
     * <P>
     * @param Mode - the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
     public void setSpeedStepMode(int Mode) {
         int mode;
	     speedStepMode = Mode;
         super.setSpeedStepMode(speedStepMode);
         switch (speedStepMode) {
             case DccThrottle.SpeedStepMode28: mode = CbusConstants.CBUS_SS_28; break;
             case DccThrottle.SpeedStepMode14: mode = CbusConstants.CBUS_SS_14; break;
             default: mode = CbusConstants.CBUS_SS_128; break;
         }
         cs.setSpeedSteps(_handle, mode);
     }

    /**
     * Convert a CBUS speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) return 0.f;
        else if (lSpeed == 1) return -1.f;   // estop
        else return ( (lSpeed-1)/126.f);
    }

    /**
     * Convert a float speed value to a CBUS speed integer
     */
    protected int intSpeed(float fSpeed) {
      if (fSpeed == 0.f)
        return 0;
      else if (fSpeed < 0.f)
        return 1;   // estop
      // add the 0.5 to handle float to int round for positive numbers
      return (int)(fSpeed * 126.f + 0.5) + 1 ;
    }

    /**
     * Send the CBUS message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1() {
        int new_fn = ((getF0() ? CbusConstants.CBUS_F0 : 0) |
                (getF1() ? CbusConstants.CBUS_F1 : 0) |
                (getF2() ? CbusConstants.CBUS_F2 : 0) |
                (getF3() ? CbusConstants.CBUS_F3 : 0) |
                (getF4() ? CbusConstants.CBUS_F4 : 0));
        cs.setFunctions(1, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2() {
        int new_fn = ((getF5() ? CbusConstants.CBUS_F5 : 0) |
                (getF6() ? CbusConstants.CBUS_F6 : 0) |
                (getF7() ? CbusConstants.CBUS_F7 : 0) |
                (getF8() ? CbusConstants.CBUS_F8 : 0));
        cs.setFunctions(2, _handle, new_fn);
    }

    /**
     * Send the CBUS message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3() {
        int new_fn = ((getF9() ? CbusConstants.CBUS_F9 : 0) |
                (getF10() ? CbusConstants.CBUS_F10 : 0) |
                (getF11() ? CbusConstants.CBUS_F11 : 0) |
                (getF12() ? CbusConstants.CBUS_F12 : 0));
        cs.setFunctions(3, _handle, new_fn);
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
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

        // reset timeout
        mRefreshTimer.stop();
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
        if (Math.abs(oldSpeed - this.speedSetting) > 0.0001)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
    }

    /**
     * Set the direction and reset speed.
     */
    public void setIsForward(boolean forward)
    {
        boolean old = isForward; 
        isForward = forward;
        setSpeedSetting(speedSetting);
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    public String toString() {
        return getLocoAddress().toString();
    }
    
    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void throttleDispose() {
        log.debug("dispose");

        cs.releaseSession(_handle);
        _handle = -1;
        cs = null;

        // stop timeout
        mRefreshTimer.stop();

        mRefreshTimer = null;
		cs = null;

     }

    javax.swing.Timer mRefreshTimer = null;

	// CBUS command station expect DSPD every 4s
    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dspdTimeout();
            }
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void dspdTimeout() {
        log.debug("Sending throttle keep alive speed/dir speed: " + speedSetting);
        setSpeedSetting(speedSetting);
    }


    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, CbusThrottleManager.isLongAddress(address));
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusThrottle.class.getName());

}
