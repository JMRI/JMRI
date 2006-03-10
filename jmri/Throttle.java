// Thottle.java

package jmri;

/**
 * A Throttle object can be manipulated to change the speed, direction
 * and functions of a locomotive.
 * <P>
 * A Throttle implementation provides the actual control mechanism.  Clients
 * then provide GUI throttles, automated start/stop, etc.
 * <P>
 * What's the multiplicity of Throttle objects?  Entirely separate
 * locomotives, etc, need their own.  But can there be more than one
 * Throttle working with a single locomotive, e.g. for a buddy throttle,
 * or to display the status of a physical throttle on a screen.
 * <P>
 * Our working model was that there's _something_ in the control system
 * hardware that's the resource represented by a Throttle, so there's
 * a limited number of Throttle's available. A single Throttle object
 * can be used by multiple cabs via the parameters, parameter change
 * notifications, etc.  This implies that we should be able to assign
 * and deassign locos from this throttle, but that doesn't seem right.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.12 $
 */
public interface Throttle {

    /** Speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting();
    public void setSpeedSetting(float speed);

    /** direction
     * This is an bound parameter.
     */
    public boolean getIsForward();
    public void setIsForward(boolean forward);

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0();
    public void setF0(boolean f0);

    public boolean getF1();
    public void setF1(boolean f1);

    public boolean getF2();
    public void setF2(boolean f2);

    public boolean getF3();
    public void setF3(boolean f3);

    public boolean getF4();
    public void setF4(boolean f4);

    public boolean getF5();
    public void setF5(boolean f5);

    public boolean getF6();
    public void setF6(boolean f6);

    public boolean getF7();
    public void setF7(boolean f7);

    public boolean getF8();
    public void setF8(boolean f8);

    public boolean getF9();
    public void setF9(boolean f9);

    public boolean getF10();
    public void setF10(boolean f10);

    public boolean getF11();
    public void setF11(boolean f11);

    public boolean getF12();
    public void setF12(boolean f12);

    // functions momentary status - note that we use the naming for DCC, 
    // though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0Momentary();
    public void setF0Momentary(boolean f0Momentary);

    public boolean getF1Momentary();
    public void setF1Momentary(boolean f1Momentary);

    public boolean getF2Momentary();
    public void setF2Momentary(boolean f2Momentary);

    public boolean getF3Momentary();
    public void setF3Momentary(boolean f3Momentary);

    public boolean getF4Momentary();
    public void setF4Momentary(boolean f4Momentary);

    public boolean getF5Momentary();
    public void setF5Momentary(boolean f5Momentary);

    public boolean getF6Momentary();
    public void setF6Momentary(boolean f6Momentary);

    public boolean getF7Momentary();
    public void setF7Momentary(boolean f7Momentary);

    public boolean getF8Momentary();
    public void setF8Momentary(boolean f8Momentary);

    public boolean getF9Momentary();
    public void setF9Momentary(boolean f9Momentary);

    public boolean getF10Momentary();
    public void setF10Momentary(boolean f10Momentary);

    public boolean getF11Momentary();
    public void setF11Momentary(boolean f11Momentary);

    public boolean getF12Momentary();
    public void setF12Momentary(boolean f12Momentary);

    /**
     * Locomotive address.  The exact format is defined by the
     * specific implementation, as subclasses of LocoAddress will contain
     * different information.
     *
     * This is an unbound parameter.
     */
    public LocoAddress getLocoAddress();

    // register for notification if any of the properties change
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p);
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p);


    /**
     * Dispose when finished with this object.  This does not
     * free any hardware resources used; rather, it just cleans up the
     * software implmentation.
     * <P>
     * After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose();

    /**
     * Finished with this Throttle, tell the layout that the locomotive
     * is available for reuse/reallocation by somebody else.
     * <P>
     * After this, further usage of
     * this Throttle object will result in a JmriException.
     * Do not call dispose after release.
     * <P>
     * Normally, release ends with a call to dispose.
     */
    public void release();

    /**
     * Finished with this Throttle, tell the layout that the locomotive
     * is available for reuse/reallocation by somebody else. If possible,
     * tell the layout that this locomotive has been dispatched to another user.
     * Not all layouts will implement this, in which case it is synomous with
     * release();
     * <P>
     * After this, further usage of
     * this Throttle object will result in a JmriException.
     * <P>
     * Normally, dispatch ends with a call to dispose.
     */
    public void dispatch();

}


/* @(#)Thottle.java */
