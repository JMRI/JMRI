package jmri.jmrix;

import jmri.DccThrottle;

/**
 * An abstract implementation of DccThrottle.
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.2 $
 */
abstract public class AbstractThrottle implements DccThrottle {
    protected float speedSetting;
    protected float speedIncrement;
    protected int address;
    protected boolean isForward;
    protected boolean f0, f1, f2, f3, f4, f5, f6, f7, f8;


    /** speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting() {
        return speedSetting;
    }

    /** direction
     * This is an bound parameter.
     */
    public boolean getIsForward() {
        return isForward;
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0() {
        return f0;
    }

    public boolean getF1() {
        return f1;
    }

    public boolean getF2() {
        return f2;
    }

    public boolean getF3() {
        return f3;
    }

    public boolean getF4() {
        return f4;
    }

    public boolean getF5() {
        return f5;
    }

    public boolean getF6() {
        return f6;
    }

    public boolean getF7() {
        return f7;
    }

    public boolean getF8() {
        return f8;
    }

    /**
     * Locomotive identification.  The exact format is defined by the
     * specific implementation, but its intended that this is a user-specified
     * name like "UP 777", or whatever convention the user wants to employ.
     *
     * This is an unbound parameter.
     */
    public String getLocoIdentification() {
        return "";
    }


    /**
     * Locomotive address.  The exact format is defined by the
     * specific implementation, but for DCC systems it is intended that this
     * will be the DCC address in the form "nnnn" (extended) vs "nnn" or "nn" (short).
     * Non-DCC systems may use a different form.
     *
     * This is an unbound parameter.
     */
    public String getLocoAddress() {
        return "";
    }


    // register for notification if any of the properties change
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) {
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) {
    }


    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when
     * it's the last user.
     */
    public void dispose() {
        // if this object has registered any listeners, remove those.

    }


    public int getDccAddress() {
        return address;
    }

    /**
     * to handle quantized speed. Note this can change! Valued returned is
     * always positive.
     */
    public float getSpeedIncrement() {
        return speedIncrement;
    }

}