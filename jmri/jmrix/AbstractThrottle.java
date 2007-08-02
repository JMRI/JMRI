package jmri.jmrix;

import jmri.DccThrottle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * An abstract implementation of DccThrottle.
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 * <P>
 * Note that this implements DccThrottle, not Throttle directly, so 
 * it has some DCC-specific content.
 *
 * @author  Bob Jacobsen  Copyright (C) 2001, 2005
 * @version $Revision: 1.17 $
 */
abstract public class AbstractThrottle implements DccThrottle {
    protected float speedSetting;
    protected float speedIncrement;
    protected int speedStepMode;
    protected boolean isForward;
    protected boolean f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12;
    protected boolean f0Momentary, f1Momentary, f2Momentary, f3Momentary, 
                      f4Momentary, f5Momentary, f6Momentary, f7Momentary, 
                      f8Momentary, f9Momentary, f10Momentary, f11Momentary,
                      f12Momentary;

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

    // function momentary status  - note that we use the naming for DCC, 
    // though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0Momentary() {
        return f0Momentary;
    }

    public boolean getF1Momentary() {
        return f1Momentary;
    }

    public boolean getF2Momentary() {
        return f2Momentary;
    }

    public boolean getF3Momentary() {
        return f3Momentary;
    }

    public boolean getF4Momentary() {
        return f4Momentary;
    }

    public boolean getF5Momentary() {
        return f5Momentary;
    }

    public boolean getF6Momentary() {
        return f6Momentary;
    }

    public boolean getF7Momentary() {
        return f7Momentary;
    }

    public boolean getF8Momentary() {
        return f8Momentary;
    }

    public boolean getF9Momentary() {
        return f9Momentary;
    }

    public boolean getF10Momentary() {
        return f10Momentary;
    }

    public boolean getF11Momentary() {
        return f11Momentary;
    }

    public boolean getF12Momentary() {
        return f12Momentary;
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
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup1() {
        log.error("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup2() {
        log.error("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12
     * <P>
     * This is used in the setFn implementations provided in this class,
     * but a real implementation needs to be provided.
     */
    protected void sendFunctionGroup3() {
        log.error("sendFunctionGroup3 needs to be implemented if invoked");
    }

    // function momentary status  - note that we use the naming for DCC, 
    // though that's not the implication;
    // see also DccThrottle interface
    public void setF0Momentary(boolean f0Momentary) {
        this.f0Momentary = f0Momentary;
        sendMomentaryFunctionGroup1();
    }

    public void setF1Momentary(boolean f1Momentary) {
        this.f1Momentary = f1Momentary;
        sendMomentaryFunctionGroup1();
    }

    public void setF2Momentary(boolean f2Momentary) {
        this.f2Momentary = f2Momentary;
        sendMomentaryFunctionGroup1();
    }

    public void setF3Momentary(boolean f3Momentary) {
        this.f3Momentary = f3Momentary;
        sendMomentaryFunctionGroup1();
    }

    public void setF4Momentary(boolean f4Momentary) {
        this.f4Momentary = f4Momentary;
        sendMomentaryFunctionGroup1();
    }

    public void setF5Momentary(boolean f5Momentary) {
        this.f5Momentary = f5Momentary;
        sendMomentaryFunctionGroup2();
    }

    public void setF6Momentary(boolean f6Momentary) {
        this.f6Momentary = f6Momentary;
        sendMomentaryFunctionGroup2();
    }

    public void setF7Momentary(boolean f7Momentary) {
        this.f7Momentary = f7Momentary;
        sendMomentaryFunctionGroup2();
    }

    public void setF8Momentary(boolean f8Momentary) {
        this.f8Momentary = f8Momentary;
        sendMomentaryFunctionGroup2();
    }

    public void setF9Momentary(boolean f9Momentary) {
        this.f9Momentary = f9Momentary;
        sendMomentaryFunctionGroup3();
    }

    public void setF10Momentary(boolean f10Momentary) {
        this.f10Momentary = f10Momentary;
        sendMomentaryFunctionGroup3();
    }

    public void setF11Momentary(boolean f11Momentary) {
        this.f11Momentary = f11Momentary;
        sendMomentaryFunctionGroup3();
    }

    public void setF12Momentary(boolean f12Momentary) {
        this.f12Momentary = f12Momentary;
        sendMomentaryFunctionGroup3();
    }

    /**
     * Send the message to set the momentary state of
     * functions F0, F1, F2, F3, F4.
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, a real implementation needs to be provided if the 
     * hardware supports setting functions momentary. 
     */
    protected void sendMomentaryFunctionGroup1() {
    }

    /**
     * Send the message to set the momentary state of
     * functions F5, F6, F7, F8.
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, but a real implementation needs to be provided if the 
     * hardware supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup2() {
    }

    /**
     * Send the message to set the Momentary state of
     * functions F9, F10, F11, F12
     * <P>
     * This is used in the setFnMomentary implementations provided in this 
     * class, but a real implementation needs to be provided if the 
     * hardware supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup3() {
    }


    /*
     * setSpeedStepMode - set the speed step value.
     * <P>
     * specific implementations should override this function
     * <P>
     * @param Mode - the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
     public void setSpeedStepMode(int Mode) {
	    speedStepMode = Mode;
     }

    /*
     * getSpeedStepMode - get the current speed step value.
     * <P>
     */
     public int getSpeedStepMode() {
	    return speedStepMode;
     }


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractThrottle.class.getName());

}
