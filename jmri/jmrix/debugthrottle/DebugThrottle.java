package jmri.jmrix.debugthrottle;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle for debugging use.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class DebugThrottle extends AbstractThrottle
{
    /**
     * Constructor
     */
    public DebugThrottle(int address)
    {
        super();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
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
        this.f9           = false;
        this.f10           = false;
        this.f11           = false;
        this.f12           = false;
        this.address      = address;
        this.isForward    = true;

    }


    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1() {
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2() {

    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3() {
    }

    /**
     * Set the speed & direction
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    public void setSpeedSetting(float speed) {
        this.speedSetting = speed;
        int value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>127) value = 127;    // max possible speed
        if (value<0) value = 1;        // emergency stop

    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    /**
     * Finished with this throttle.  Right now, this does nothing,
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
        if (!active) log.warn("release called when not active");
        dispose();
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        log.debug("dispose");
        super.dispose();

        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebugThrottle.class.getName());

}