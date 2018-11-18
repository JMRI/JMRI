package jmri;

/**
 * A Throttle object can be manipulated to change the speed, direction and
 * functions of a single locomotive.
 * <P>
 * A Throttle implementation provides the actual control mechanism. These are
 * obtained via a {@link ThrottleManager}.
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
public interface Throttle extends ReadonlyThrottle {

    /**
     * Set the speed.
     *
     * @param speed a number from 0.0 to 1.0
     */
    public void setSpeedSetting(float speed);

    /**
     * Set the speed - on systems which normally suppress the sending of a message
     * if the new speed won't (appear to JMRI to) make any difference, the two extra
     * options allow the calling method to insist the message is sent under some
     * circumstances.
     *
     * @param speed a number from 0.0 to 1.0
     * @param allowDuplicates if true, don't suppress messages that should have no effect
     * @param allowDuplicatesOnStop if true, and the new speed is idle or estop, don't suppress messages
     */
    public void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop);

    /**
     * Set the speed, and on systems which normally suppress the sending of a message make sure
     * the message gets sent.
     *
     * @param speed a number from 0.0 to 1.0
     */
    public void setSpeedSettingAgain(float speed);

    public void setIsForward(boolean forward);

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public void setF0(boolean f0);

    public void setF1(boolean f1);

    public void setF2(boolean f2);

    public void setF3(boolean f3);

    public void setF4(boolean f4);

    public void setF5(boolean f5);

    public void setF6(boolean f6);

    public void setF7(boolean f7);

    public void setF8(boolean f8);

    public void setF9(boolean f9);

    public void setF10(boolean f10);

    public void setF11(boolean f11);

    public void setF12(boolean f12);

    public void setF13(boolean f13);

    public void setF14(boolean f14);

    public void setF15(boolean f15);

    public void setF16(boolean f16);

    public void setF17(boolean f17);

    public void setF18(boolean f18);

    public void setF19(boolean f19);

    public void setF20(boolean f20);

    public void setF21(boolean f21);

    public void setF22(boolean f22);

    public void setF23(boolean f23);

    public void setF24(boolean f24);

    public void setF25(boolean f25);

    public void setF26(boolean f26);

    public void setF27(boolean f27);

    public void setF28(boolean f28);

    // functions momentary status - note that we use the naming for DCC,
    // though that's not the implication;
    // see also DccThrottle interface
    public void setF0Momentary(boolean f0Momentary);

    public void setF1Momentary(boolean f1Momentary);

    public void setF2Momentary(boolean f2Momentary);

    public void setF3Momentary(boolean f3Momentary);

    public void setF4Momentary(boolean f4Momentary);

    public void setF5Momentary(boolean f5Momentary);

    public void setF6Momentary(boolean f6Momentary);

    public void setF7Momentary(boolean f7Momentary);

    public void setF8Momentary(boolean f8Momentary);

    public void setF9Momentary(boolean f9Momentary);

    public void setF10Momentary(boolean f10Momentary);

    public void setF11Momentary(boolean f11Momentary);

    public void setF12Momentary(boolean f12Momentary);

    public void setF13Momentary(boolean f13Momentary);

    public void setF14Momentary(boolean f14Momentary);

    public void setF15Momentary(boolean f15Momentary);

    public void setF16Momentary(boolean f16Momentary);

    public void setF17Momentary(boolean f17Momentary);

    public void setF18Momentary(boolean f18Momentary);

    public void setF19Momentary(boolean f19Momentary);

    public void setF20Momentary(boolean f20Momentary);

    public void setF21Momentary(boolean f21Momentary);

    public void setF22Momentary(boolean f22Momentary);

    public void setF23Momentary(boolean f23Momentary);

    public void setF24Momentary(boolean f24Momentary);

    public void setF25Momentary(boolean f25Momentary);

    public void setF26Momentary(boolean f26Momentary);

    public void setF27Momentary(boolean f27Momentary);

    public void setF28Momentary(boolean f28Momentary);

    /**
     * Not for general use, see {@link #release()} and {@link #dispatch()}.
     * <p>
     * Dispose of object when finished it. This does not free any hardware
     * resources used; rather, it just cleans up the software implementation.
     * <P>
     * Used for handling certain internal error conditions, where the object
     * still exists but hardware is not associated with it.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     *
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager or by using {@link #dispose(ThrottleListener l)}.
     */
    @Deprecated
    public void dispose();

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException. Do not call dispose after release.
     * <P>
     * Normally, release ends with a call to dispose.
     *
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager or by using {@link #release(ThrottleListener l)}
     */
    @Deprecated
    public void release();

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else. If possible, tell the
     * layout that this locomotive has been dispatched to another user. Not all
     * layouts will implement this, in which case it is synomous with release();
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     * <P>
     * Normally, dispatch ends with a call to dispose.
     *
     * @deprecated Calls to dispose of a throttle should now be made via the
     * throttle manager, or by using {@link #dispatch(ThrottleListener l)}
     */
    @Deprecated
    public void dispatch();

    /**
     * Not for general use, see {@link #release(ThrottleListener l)} and
     * {@link #dispatch(ThrottleListener l)}.
     * <p>
     * Dispose of object when finished it. This does not free any hardware
     * resources used; rather, it just cleans up the software implementation.
     * <P>
     * Used for handling certain internal error conditions, where the object
     * still exists but hardware is not associated with it.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     *
     * @param l {@link ThrottleListener} to dispose of
     */
    public void dispose(ThrottleListener l);

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else.
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException. Do not call dispose after release.
     * <P>
     * Normally, release ends with a call to dispose.
     *
     * @param l {@link ThrottleListener} to release
     */
    public void release(ThrottleListener l);

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else. If possible, tell the
     * layout that this locomotive has been dispatched to another user. Not all
     * layouts will implement this, in which case it is synomous with release();
     * <P>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     * <P>
     * Normally, dispatch ends with a call to dispose.
     *
     * @param l {@link ThrottleListener} to dispatch
     */
    public void dispatch(ThrottleListener l);

}
