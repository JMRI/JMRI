// Thottle.java

package jmri;
import java.util.Vector;
/**
 * A Throttle object can be manipulated to change the speed, direction
 * and functions of a single locomotive.
 * <P>
 * A Throttle implementation provides the actual control mechanism.  
 * These are obtained via a {@link ThrottleManager}.
 * <P>
 * With some control systems, there are only
 * a limited number of Throttle's available. 
 * <p>
 * On DCC systems, Throttles are often actually
 * {@link DccThrottle} objects, which have some additional
 * DCC-specific capabilities.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public interface Throttle {

    /** Speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound property.
     */
    public float getSpeedSetting();
    public void setSpeedSetting(float speed);

    /** direction
     * This is an bound property.
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
    
    public boolean getF13();
    public void setF13(boolean f13);
    
    public boolean getF14();
    public void setF14(boolean f14);
    
    public boolean getF15();
    public void setF15(boolean f15);
    
    public boolean getF16();
    public void setF16(boolean f16);
    
    public boolean getF17();
    public void setF17(boolean f17);
    
    public boolean getF18();
    public void setF18(boolean f18);
    
    public boolean getF19();
    public void setF19(boolean f19);
    
    public boolean getF20();
    public void setF20(boolean f20);
    
    public boolean getF21();
    public void setF21(boolean f21);
    
    public boolean getF22();
    public void setF22(boolean f22);
    
    public boolean getF23();
    public void setF23(boolean f23);
    
    public boolean getF24();
    public void setF24(boolean f24);
    
    public boolean getF25();
    public void setF25(boolean f25);
    
    public boolean getF26();
    public void setF26(boolean f26);
    
    public boolean getF27();
    public void setF27(boolean f27);
    
    public boolean getF28();
    public void setF28(boolean f28);

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

    public boolean getF13Momentary();
    public void setF13Momentary(boolean f13Momentary);

    public boolean getF14Momentary();
    public void setF14Momentary(boolean f14Momentary);

    public boolean getF15Momentary();
    public void setF15Momentary(boolean f15Momentary);

    public boolean getF16Momentary();
    public void setF16Momentary(boolean f16Momentary);

    public boolean getF17Momentary();
    public void setF17Momentary(boolean f17Momentary);

    public boolean getF18Momentary();
    public void setF18Momentary(boolean f18Momentary);

    public boolean getF19Momentary();
    public void setF19Momentary(boolean f19Momentary);

    public boolean getF20Momentary();
    public void setF20Momentary(boolean f20Momentary);

    public boolean getF21Momentary();
    public void setF21Momentary(boolean f21Momentary);

    public boolean getF22Momentary();
    public void setF22Momentary(boolean f22Momentary);

    public boolean getF23Momentary();
    public void setF23Momentary(boolean f23Momentary);

    public boolean getF24Momentary();
    public void setF24Momentary(boolean f24Momentary);

    public boolean getF25Momentary();
    public void setF25Momentary(boolean f25Momentary);

    public boolean getF26Momentary();
    public void setF26Momentary(boolean f26Momentary);

    public boolean getF27Momentary();
    public void setF27Momentary(boolean f27Momentary);

    public boolean getF28Momentary();
    public void setF28Momentary(boolean f28Momentary);

    /**
     * Locomotive address.  The exact format is defined by the
     * specific implementation, as subclasses of LocoAddress will contain
     * different information.
     *
     * This is an unbound property.
     */
    public LocoAddress getLocoAddress();

    // register for notification if any of the properties change
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p);
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p);
    public Vector<java.beans.PropertyChangeListener> getListeners();


    /**
     * Not for general use, see {@link #release()} and {@link #dispatch()}.
     * <p> 
     * Dispose of object when finished it.  This does not
     * free any hardware resources used; rather, it just cleans up the
     * software implementation.
     * <P>
     * Used for handling certain internal error conditions, where
     * the object still exists but hardware is not associated with it.
     * <P>
     * After this, further usage of
     * this Throttle object will result in a JmriException.
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager or by using {@link #dispose(ThrottleListener l)}.
     */
    @Deprecated
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
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager or by using {@link #release(ThrottleListener l)}
     */
    @Deprecated
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
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager, or by using {@link #dispatch(ThrottleListener l)}
     */
    @Deprecated
    public void dispatch();
    
    
        /**
     * Not for general use, see {@link #release(ThrottleListener l)} and {@link #dispatch(ThrottleListener l)}.
     * <p> 
     * Dispose of object when finished it.  This does not
     * free any hardware resources used; rather, it just cleans up the
     * software implementation.
     * <P>
     * Used for handling certain internal error conditions, where
     * the object still exists but hardware is not associated with it.
     * <P>
     * After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose(ThrottleListener l);

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
    public void release(ThrottleListener l);

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
    public void dispatch(ThrottleListener l);
    
    public void setRosterEntry(BasicRosterEntry re);
    public BasicRosterEntry getRosterEntry();
}


/* @(#)Thottle.java */
