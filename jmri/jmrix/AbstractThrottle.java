package jmri.jmrix;

import jmri.DccThrottle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * An abstract implementation of DccThrottle.
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author  Bob Jacobsen  Copyright (C) 2001
 * @version $Revision: 1.9 $
 */
abstract public class AbstractThrottle implements DccThrottle {
    protected float speedSetting;
    protected float speedIncrement;
    protected int address;
    protected boolean isForward;
    protected boolean f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12;

    /**
     * Is this object still usable?  Set false after dispose, this
     * variable is used to check for incorrect usage.
     */
    protected boolean active;

    public AbstractThrottle() {
		active = true;
    }

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

    public boolean getF9() {
        return f9;
    }

    public boolean getF10() {
        return f10;
    }

    public boolean getF11() {
        return f11;
    }

    public boolean getF12() {
        return f12;
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
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    /**
     * Trigger the notification of all PropertyChangeListeners
     */
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        if (oldValue.equals(newValue)) log.error("notifyPropertyChangeListener without change");
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this)
            {
                v = (Vector) listeners.clone();
            }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" listeners about property "
                                            +property);
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = (PropertyChangeListener) v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }


    // data members to hold contact with the property listeners
    final private Vector listeners = new Vector();

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        if (!active) log.error("Dispose called when not active");
        // if this object has registered any listeners, remove those.

        // and mark as unusable
        active = false;
    }

    public void dispatch() {
        if (!active) log.warn("dispatch called when not active");
        release();
    }

    public void release() {
        if (!active) log.warn("release called when not active");
        dispose();
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

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public void setF0(boolean f0) {
        this.f0 = f0;
        sendFunctionGroup1();
    }

    public void setF1(boolean f1) {
        this.f1 = f1;
        sendFunctionGroup1();
    }

    public void setF2(boolean f2) {
        this.f2 = f2;
        sendFunctionGroup1();
    }

    public void setF3(boolean f3) {
        this.f3 = f3;
        sendFunctionGroup1();
    }

    public void setF4(boolean f4) {
        this.f4 = f4;
        sendFunctionGroup1();
    }

    public void setF5(boolean f5) {
        this.f5 = f5;
        sendFunctionGroup2();
    }

    public void setF6(boolean f6) {
        this.f6 = f6;
        sendFunctionGroup2();
    }

    public void setF7(boolean f7) {
        this.f7 = f7;
        sendFunctionGroup2();
    }

    public void setF8(boolean f8) {
        this.f8 = f8;
        sendFunctionGroup2();
    }

    public void setF9(boolean f9) {
        this.f9 = f9;
        sendFunctionGroup3();
    }

    public void setF10(boolean f10) {
        this.f10 = f10;
        sendFunctionGroup3();
    }

    public void setF11(boolean f11) {
        this.f11 = f11;
        sendFunctionGroup3();
    }

    public void setF12(boolean f12) {
        this.f12 = f12;
        sendFunctionGroup3();
    }

    /**
     * Send the message to set the state of
     * functions F0, F1, F2, F3, F4.
     * <P>
     * This is used in the setFn implementations provided in this class.
     */
    abstract protected void sendFunctionGroup1();

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     * <P>
     * This is used in the setFn implementations provided in this class.
     */
    abstract protected void sendFunctionGroup2();

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12
     * <P>
     * This is used in the setFn implementations provided in this class.
     */
    abstract protected void sendFunctionGroup3();

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractThrottle.class.getName());

}