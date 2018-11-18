package jmri;

import java.util.Vector;

/**
 * A ReadonlyThrottle object listens on changes to the speed, direction and
 * functions of a single locomotive.
 * <P>
 * A ReadonlyThrottle implementation provides the actual control mechanism.
 * These are obtained via a {@link ThrottleManager}.
 * <P>
 * With some control systems, there are only a limited number of Throttle's
 * available.
 * <p>
 * On DCC systems, Throttles are often actually {@link DccThrottle} objects,
 * which have some additional DCC-specific capabilities.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public interface ReadonlyThrottle {

    /**
     * Constants to represent the functions F0 through F28.
     */
    public static final String F0 = "F0"; // NOI18N
    public static final String F1 = "F1"; // NOI18N
    public static final String F2 = "F2"; // NOI18N
    public static final String F3 = "F3"; // NOI18N
    public static final String F4 = "F4"; // NOI18N
    public static final String F5 = "F5"; // NOI18N
    public static final String F6 = "F6"; // NOI18N
    public static final String F7 = "F7"; // NOI18N
    public static final String F8 = "F8"; // NOI18N
    public static final String F9 = "F9"; // NOI18N
    public static final String F10 = "F10"; // NOI18N
    public static final String F11 = "F11"; // NOI18N
    public static final String F12 = "F12"; // NOI18N
    public static final String F13 = "F13"; // NOI18N
    public static final String F14 = "F14"; // NOI18N
    public static final String F15 = "F15"; // NOI18N
    public static final String F16 = "F16"; // NOI18N
    public static final String F17 = "F17"; // NOI18N
    public static final String F18 = "F18"; // NOI18N
    public static final String F19 = "F19"; // NOI18N
    public static final String F20 = "F20"; // NOI18N
    public static final String F21 = "F21"; // NOI18N
    public static final String F22 = "F22"; // NOI18N
    public static final String F23 = "F23"; // NOI18N
    public static final String F24 = "F24"; // NOI18N
    public static final String F25 = "F25"; // NOI18N
    public static final String F26 = "F26"; // NOI18N
    public static final String F27 = "F27"; // NOI18N
    public static final String F28 = "F28"; // NOI18N
    /**
     * Constants to represent the functions F0 through F28.
     */
    public static final String F0Momentary = "F0Momentary"; // NOI18N
    public static final String F1Momentary = "F1Momentary"; // NOI18N
    public static final String F2Momentary = "F2Momentary"; // NOI18N
    public static final String F3Momentary = "F3Momentary"; // NOI18N
    public static final String F4Momentary = "F4Momentary"; // NOI18N
    public static final String F5Momentary = "F5Momentary"; // NOI18N
    public static final String F6Momentary = "F6Momentary"; // NOI18N
    public static final String F7Momentary = "F7Momentary"; // NOI18N
    public static final String F8Momentary = "F8Momentary"; // NOI18N
    public static final String F9Momentary = "F9Momentary"; // NOI18N
    public static final String F10Momentary = "F10Momentary"; // NOI18N
    public static final String F11Momentary = "F11Momentary"; // NOI18N
    public static final String F12Momentary = "F12Momentary"; // NOI18N
    public static final String F13Momentary = "F13Momentary"; // NOI18N
    public static final String F14Momentary = "F14Momentary"; // NOI18N
    public static final String F15Momentary = "F15Momentary"; // NOI18N
    public static final String F16Momentary = "F16Momentary"; // NOI18N
    public static final String F17Momentary = "F17Momentary"; // NOI18N
    public static final String F18Momentary = "F18Momentary"; // NOI18N
    public static final String F19Momentary = "F19Momentary"; // NOI18N
    public static final String F20Momentary = "F20Momentary"; // NOI18N
    public static final String F21Momentary = "F21Momentary"; // NOI18N
    public static final String F22Momentary = "F22Momentary"; // NOI18N
    public static final String F23Momentary = "F23Momentary"; // NOI18N
    public static final String F24Momentary = "F24Momentary"; // NOI18N
    public static final String F25Momentary = "F25Momentary"; // NOI18N
    public static final String F26Momentary = "F26Momentary"; // NOI18N
    public static final String F27Momentary = "F27Momentary"; // NOI18N
    public static final String F28Momentary = "F28Momentary"; // NOI18N

    /**
     * Speed - expressed as a value {@literal 0.0 -> 1.0.} Negative means
     * emergency stop. This is an bound property.
     *
     * @return the speed as a percentage of maximum possible speed
     */
    public float getSpeedSetting();

    /**
     * direction This is an bound property.
     *
     * @return true if forward, false if reverse or undefined
     */
    public boolean getIsForward();

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0();

    public boolean getF1();

    public boolean getF2();

    public boolean getF3();

    public boolean getF4();

    public boolean getF5();

    public boolean getF6();

    public boolean getF7();

    public boolean getF8();

    public boolean getF9();

    public boolean getF10();

    public boolean getF11();

    public boolean getF12();

    public boolean getF13();

    public boolean getF14();

    public boolean getF15();

    public boolean getF16();

    public boolean getF17();

    public boolean getF18();

    public boolean getF19();

    public boolean getF20();

    public boolean getF21();

    public boolean getF22();

    public boolean getF23();

    public boolean getF24();

    public boolean getF25();

    public boolean getF26();

    public boolean getF27();

    public boolean getF28();

    // functions momentary status - note that we use the naming for DCC,
    // though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0Momentary();

    public boolean getF1Momentary();

    public boolean getF2Momentary();

    public boolean getF3Momentary();

    public boolean getF4Momentary();

    public boolean getF5Momentary();

    public boolean getF6Momentary();

    public boolean getF7Momentary();

    public boolean getF8Momentary();

    public boolean getF9Momentary();

    public boolean getF10Momentary();

    public boolean getF11Momentary();

    public boolean getF12Momentary();

    public boolean getF13Momentary();

    public boolean getF14Momentary();

    public boolean getF15Momentary();

    public boolean getF16Momentary();

    public boolean getF17Momentary();

    public boolean getF18Momentary();

    public boolean getF19Momentary();

    public boolean getF20Momentary();

    public boolean getF21Momentary();

    public boolean getF22Momentary();

    public boolean getF23Momentary();

    public boolean getF24Momentary();

    public boolean getF25Momentary();

    public boolean getF26Momentary();

    public boolean getF27Momentary();

    public boolean getF28Momentary();

    /**
     * Locomotive address. The exact format is defined by the specific
     * implementation, as subclasses of LocoAddress will contain different
     * information.
     *
     * This is an unbound property.
     *
     * @return The locomotive address
     */
    public LocoAddress getLocoAddress();

    // register for notification if any of the properties change
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p);

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p);

    public Vector<java.beans.PropertyChangeListener> getListeners();

    public void setRosterEntry(BasicRosterEntry re);

    public BasicRosterEntry getRosterEntry();
}
