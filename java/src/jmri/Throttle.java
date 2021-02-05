package jmri;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.beans.PropertyChangeProvider;

/**
 * A Throttle object can be manipulated to change the speed, direction and
 * functions of a single locomotive.
 * <p>
 * A Throttle implementation provides the actual control mechanism. These are
 * obtained via a {@link ThrottleManager}.
 * <p>
 * With some control systems, there are only a limited number of Throttle's
 * available.
 * <p>
 * On DCC systems, Throttles are often actually {@link DccThrottle} objects,
 * which have some additional DCC-specific capabilities.
 * <p>
 * {@link java.beans.PropertyChangeEvent}s that can be listened to include
 * <ul>
 * <li>SpeedSetting, SpeedSteps, isForward
 * <li>F0, F1, F2 .. F27, F28, F29, F30 ..
 * <li>F0Momentary, F1Momentary, F2Momentary .. F28Momentary .. F29Momentary ..
 * <li>ThrottleAssigned, throttleRemoved, throttleConnected,
 * throttleNotFoundInRemoval
 * <li>DispatchEnabled, ReleaseEnabled
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public interface Throttle extends PropertyChangeProvider {

    /**
     * Constant used in getThrottleInfo.
     */
    public static final String SPEEDSTEPMODE = "SpeedStepsMode"; // speed steps NOI18N
    
    /*
     * Properties strings sent to property change listeners
     */
    
    /**
     * Constant sent by Throttle on Property Change.
     */
    public static final String SPEEDSTEPS = "SpeedSteps"; // speed steps NOI18N
    
    public static final String SPEEDSETTING = "SpeedSetting"; // speed setting NOI18N
    public static final String ISFORWARD = "IsForward"; // direction setting NOI18N
    public static final String SPEEDINCREMENT = "SpeedIncrement"; // direction setting NOI18N

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
     * Constants to represent Function Groups.
     * <p>
     * The are the same groupings for both normal Functions and Momentary.
     */
    public static final int[] FUNCTION_GROUPS = new int[]{ 1, 1, 1, 1, 1, /** 0-4 */
        2, 2, 2, 2, /** 5-8 */   3, 3, 3, 3, /** 9-12 */
        4, 4, 4, 4, 4, 4, 4, 4, /** 13-20 */ 5, 5, 5, 5, 5, 5, 5, 5 /** 21-28 */ 
        
    };
    
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
     * Get the Function String for a particular Function number.
     * Commonly used string in Throttle property change listeners.
     * @param functionNum Function Number, minimum 0.
     * @return function string, e.g. "F0" or "F7".
     */
    public static String getFunctionString(int functionNum){
        StringBuilder sb = new StringBuilder(3);
        sb.append("F"); // NOI18N
        sb.append(functionNum);
        return sb.toString();
    }

    /**
     * Get the Momentary Function String for a particular Function number.
     * Commonly used string in Throttle property change listeners.
     * @param momentFunctionNum Momentary Function Number, minimum 0.
     * @return momentary function string, e.g. "F0Momentary" or "F7Momentary".
     */
    public static String getFunctionMomentaryString(int momentFunctionNum){
        StringBuilder sb = new StringBuilder(12);
        sb.append("F"); // NOI18N
        sb.append(momentFunctionNum);
        sb.append("Momentary"); // NOI18N
        return sb.toString();
    }
    
    /**
     * Get copy of function array.
     * Typically returns array length of 29, i.e. 0-28.
     * @return function array, length dependant by hardware type.
     */
    @Nonnull
    public abstract boolean[] getFunctions();
    
    /**
     * Get copy of function momentary status array.
     * Typically returns array length of 29, i.e. 0-28.
     * @return momentary function array, length dependant by hardware type.
     */
    @Nonnull
    public abstract boolean[] getFunctionsMomentary();
    
    /**
     * Get the current speed setting, expressed as a value {@literal 0.0 -> 1.0.} 
     * This property is bound to the {@link #SPEEDSETTING} name.
     *
     * @return the speed as a {@literal 0.0 -> 1.0.}  fraction of maximum possible speed or -1 for emergency stop.
     */
    public float getSpeedSetting();

    /**
     * Set the desired speed setting, expressed as a value {@literal 0.0 -> 1.0.} Negative means
     * emergency stop. 
     * This property is bound to the {@link #SPEEDSETTING} name.
     *
     * @param speed the speed as a {@literal 0.0 -> 1.0.} fraction of maximum possible speed or -1 for emergency stop.
     */
    public void setSpeedSetting(float speed);

    /**
     * Set the desired speed, expressed as a value {@literal 0.0 -> 1.0.}, 
     * with extra control over the messages to the layout. Negative means
     * emergency stop. 
     * On systems which normally suppress the sending of a
     * message if the new speed won't (appear to JMRI to) make any difference,
     * the two extra options allow the calling method to insist the message is
     * sent under some circumstances.
     *
     * @param speed the speed as a {@literal 0.0 -> 1.0.} fraction of maximum possible speed or -1 for emergency stop.
     * @param allowDuplicates       if true, don't suppress messages that should
     *                              have no effect
     * @param allowDuplicatesOnStop if true, and the new speed is idle or estop,
     *                              don't suppress messages
     */
    public void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop);

    /**
     * Set the speed, and on systems which normally suppress the sending of a
     * message make sure the message gets sent.
     *
     * @param speed the speed as a {@literal 0.0 -> 1.0.} fraction of maximum possible speed or -1 for emergency stop.
     */
    public void setSpeedSettingAgain(float speed);

    /**
     * direction This is an bound property.
     *
     * @return true if forward, false if reverse or undefined
     */
    public boolean getIsForward();

    /**
     * Set direction.
     *
     * @param forward true if forward, false if reverse or undefined
     */
    public void setIsForward(boolean forward);

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    
    /**
     * Set Loco Function and send to Layout.
     * @param functionNum Function Number, 0-28
     * @param newState New Function State. True on, false off.
     */
    public abstract void setFunction(int functionNum, boolean newState);
    
    /**
     * Get Loco Function status.
     * @param functionNum Function Number, 0-28
     * @return Function State. True on, false off.
     */
    public boolean getFunction(int functionNum);
    
    /**
     * Set Momentary Loco Function and send to Layout.
     * @param momFuncNum Momentary Function Number, 0-28
     * @param state New Function State. True on, false off.
     */
    public abstract void setFunctionMomentary(int momFuncNum, boolean state);
    
    /**
     * Get the Momentary Function Value.
     * @param fN Momentary function number
     * @return true if momentary function is on, else false.
     */
    public abstract boolean getFunctionMomentary(int fN);
            
    /**
     * Get Function 0 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF0() {
        return getFunction(0);
    }

    /**
     * Get Function 1 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF1() {
        return getFunction(1);
    }

    /**
     * Get Function 2 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF2() {
        return getFunction(2);
    }

    /**
     * Get Function 3 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF3() {
        return getFunction(3);
    }

    /**
     * Get Function 4 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF4() {
        return getFunction(4);
    }

    /**
     * Get Function 5 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF5() {
        return getFunction(5);
    }

    /**
     * Get Function 6 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF6() {
        return getFunction(6);
    }

    /**
     * Get Function 7 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF7() {
        return getFunction(7);
    }

    /**
     * Get Function 8 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF8() {
        return getFunction(8);
    }

    /**
     * Get Function 9 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF9() {
        return getFunction(9);
    }
    
    /**
     * Get Function 10 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF10() {
        return getFunction(10);
    }
    
    /**
     * Get Function 11 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF11() {
        return getFunction(11);
    }
        
    /**
     * Get Function 12 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF12() {
        return getFunction(12);
    }
    
    /**
     * Get Function 13 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF13() {
        return getFunction(13);
    }
        
    /**
     * Get Function 14 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF14() {
        return getFunction(14);
    }
    
    /**
     * Get Function 15 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF15() {
        return getFunction(15);
    }
    
    /**
     * Get Function 16 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF16() {
        return getFunction(16);
    }
    
    /**
     * Get Function 17 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF17() {
        return getFunction(17);
    }
    
    /**
     * Get Function 18 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF18() {
        return getFunction(18);
    }
    
    /**
     * Get Function 19 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF19() {
        return getFunction(19);
    }
    
    /**
     * Get Function 20 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF20() {
        return getFunction(20);
    }
    
    /**
     * Get Function 21 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF21() {
        return getFunction(21);
    }
    
    /**
     * Get Function 22 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF22() {
        return getFunction(22);
    }
    
    /**
     * Get Function 23 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF23() {
        return getFunction(23);
    }
    
    /**
     * Get Function 24 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF24() {
        return getFunction(24);
    }
    
    /**
     * Get Function 25 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF25() {
        return getFunction(25);
    }
    
    /**
     * Get Function 26 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF26() {
        return getFunction(26);
    }
    
    /**
     * Get Function 27 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF27() {
        return getFunction(27);
    }
    
    /**
     * Get Function 28 Status.
     * @return true for Function On, false for Function Off.
     */
    public default boolean getF28() {
        return getFunction(28);
    }

    /**
     * Set Function 0 Status.
     * @param f0 true for Function On, false for Function Off.
     */
    public default void setF0(boolean f0) {
        setFunction(0,f0);
    }

    /**
     * Set Function 1 Status.
     * @param f1 true for Function On, false for Function Off.
     */
    public default void setF1(boolean f1) {
        setFunction(1,f1);
    }

    /**
     * Set Function 2 Status.
     * @param f2 true for Function On, false for Function Off.
     */
    public default void setF2(boolean f2) {
        setFunction(2,f2);
    }

    /**
     * Set Function 3 Status.
     * @param f3 true for Function On, false for Function Off.
     */
    public default void setF3(boolean f3) {
        setFunction(3,f3);
    }

    /**
     * Set Function 4 Status.
     * @param f4 true for Function On, false for Function Off.
     */
    public default void setF4(boolean f4) {
        setFunction(4,f4);
    }

    /**
     * Set Function 5 Status.
     * @param f5 true for Function On, false for Function Off.
     */
    public default void setF5(boolean f5) {
        setFunction(5,f5);
    }

    /**
     * Set Function 6 Status.
     * @param f6 true for Function On, false for Function Off.
     */
    public default void setF6(boolean f6) {
        setFunction(6,f6);
    }

    /**
     * Set Function 7 Status.
     * @param f7 true for Function On, false for Function Off.
     */
    public default void setF7(boolean f7) {
        setFunction(7,f7);
    }

    /**
     * Set Function 8 Status.
     * @param f8 true for Function On, false for Function Off.
     */
    public default void setF8(boolean f8) {
        setFunction(8,f8);
    }

    /**
     * Set Function 9 Status.
     * @param f9 true for Function On, false for Function Off.
     */
    public default void setF9(boolean f9) {
        setFunction(9,f9);
    }

    /**
     * Set Function 10 Status.
     * @param f10 true for Function On, false for Function Off.
     */
    public default void setF10(boolean f10) {
        setFunction(10,f10);
    }

    /**
     * Set Function 11 Status.
     * @param f11 true for Function On, false for Function Off.
     */
    public default void setF11(boolean f11) {
        setFunction(11,f11);
    }

    /**
     * Set Function 12 Status.
     * @param f12 true for Function On, false for Function Off.
     */
    public default void setF12(boolean f12) {
        setFunction(12,f12);
    }

    /**
     * Set Function 13 Status.
     * @param f13 true for Function On, false for Function Off.
     */
    public default void setF13(boolean f13) {
        setFunction(13,f13);
    }

    /**
     * Set Function 14 Status.
     * @param f14 true for Function On, false for Function Off.
     */
    public default void setF14(boolean f14) {
        setFunction(14,f14);
    }

    /**
     * Set Function 15 Status.
     * @param f15 true for Function On, false for Function Off.
     */
    public default void setF15(boolean f15) {
        setFunction(15,f15);
    }

    /**
     * Set Function 16 Status.
     * @param f16 true for Function On, false for Function Off.
     */
    public default void setF16(boolean f16) {
        setFunction(16,f16);
    }

    /**
     * Set Function 17 Status.
     * @param f17 true for Function On, false for Function Off.
     */
    public default void setF17(boolean f17) {
        setFunction(17,f17);
    }

    /**
     * Set Function 18 Status.
     * @param f18 true for Function On, false for Function Off.
     */
    public default void setF18(boolean f18) {
        setFunction(18,f18);
    }

    /**
     * Set Function 19 Status.
     * @param f19 true for Function On, false for Function Off.
     */
    public default void setF19(boolean f19) {
        setFunction(19,f19);
    }

    /**
     * Set Function 20 Status.
     * @param f20 true for Function On, false for Function Off.
     */
    public default void setF20(boolean f20) {
        setFunction(20,f20);
    }

    /**
     * Set Function 21 Status.
     * @param f21 true for Function On, false for Function Off.
     */
    public default void setF21(boolean f21) {
        setFunction(21,f21);
    }

    /**
     * Set Function 22 Status.
     * @param f22 true for Function On, false for Function Off.
     */
    public default void setF22(boolean f22) {
        setFunction(22,f22);
    }

    /**
     * Set Function 23 Status.
     * @param f23 true for Function On, false for Function Off.
     */
    public default void setF23(boolean f23) {
        setFunction(23,f23);
    }

    /**
     * Set Function 24 Status.
     * @param f24 true for Function On, false for Function Off.
     */
    public default void setF24(boolean f24) {
        setFunction(24,f24);
    }

    /**
     * Set Function 25 Status.
     * @param f25 true for Function On, false for Function Off.
     */
    public default void setF25(boolean f25) {
        setFunction(25,f25);
    }

    /**
     * Set Function 26 Status.
     * @param f26 true for Function On, false for Function Off.
     */
    public default void setF26(boolean f26) {
        setFunction(26,f26);
    }

    /**
     * Set Function 27 Status.
     * @param f27 true for Function On, false for Function Off.
     */
    public default void setF27(boolean f27) {
        setFunction(27,f27);
    }

    /**
     * Set Function 28 Status.
     * @param f28 true for Function On, false for Function Off.
     */
    public default void setF28(boolean f28) {
        setFunction(28,f28);
    }
    
    // functions momentary status - note that we use the naming for DCC,
    // though that's not the implication;
    // see also DccThrottle interface
    
    /**
     * Get Momentary Function 0 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF0Momentary() {
        return getFunctionMomentary(0);
    }
    
    /**
     * Get Momentary Function 1 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF1Momentary() {
        return getFunctionMomentary(1);
    }
    
    /**
     * Get Momentary Function 2 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF2Momentary() {
        return getFunctionMomentary(2);
    }
    
    /**
     * Get Momentary Function 3 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF3Momentary() {
        return getFunctionMomentary(3);
    }
    
    /**
     * Get Momentary Function 4 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF4Momentary() {
        return getFunctionMomentary(4);
    }
    
    /**
     * Get Momentary Function 5 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF5Momentary() {
        return getFunctionMomentary(5);
    }
    
    /**
     * Get Momentary Function 6 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF6Momentary() {
        return getFunctionMomentary(6);
    }
    
    /**
     * Get Momentary Function 7 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF7Momentary() {
        return getFunctionMomentary(7);
    }
    
    /**
     * Get Momentary Function 8 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF8Momentary() {
        return getFunctionMomentary(8);
    }
    
    /**
     * Get Momentary Function 9 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF9Momentary() {
        return getFunctionMomentary(9);
    }
    
    /**
     * Get Momentary Function 10 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF10Momentary() {
        return getFunctionMomentary(10);
    }
    
    /**
     * Get Momentary Function 11 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF11Momentary() {
        return getFunctionMomentary(11);
    }
    
    /**
     * Get Momentary Function 12 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF12Momentary() {
        return getFunctionMomentary(12);
    }
    
    /**
     * Get Momentary Function 13 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF13Momentary() {
        return getFunctionMomentary(13);
    }
    
    /**
     * Get Momentary Function 14 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF14Momentary() {
        return getFunctionMomentary(14);
    }
    
    /**
     * Get Momentary Function 15 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF15Momentary() {
        return getFunctionMomentary(15);
    }
    
    /**
     * Get Momentary Function 16 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF16Momentary() {
        return getFunctionMomentary(16);
    }
    
    /**
     * Get Momentary Function 17 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF17Momentary() {
        return getFunctionMomentary(17);
    }
    
    /**
     * Get Momentary Function 18 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF18Momentary() {
        return getFunctionMomentary(18);
    }
    
    /**
     * Get Momentary Function 19 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF19Momentary() {
        return getFunctionMomentary(19);
    }
    
    /**
     * Get Momentary Function 20 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF20Momentary() {
        return getFunctionMomentary(20);
    }
    
    /**
     * Get Momentary Function 21 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF21Momentary() {
        return getFunctionMomentary(21);
    }
    
    /**
     * Get Momentary Function 22 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF22Momentary() {
        return getFunctionMomentary(22);
    }
    
    /**
     * Get Momentary Function 23 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF23Momentary() {
        return getFunctionMomentary(23);
    }
    
    /**
     * Get Momentary Function 24 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF24Momentary() {
        return getFunctionMomentary(24);
    }
    
    /**
     * Get Momentary Function 25 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF25Momentary() {
        return getFunctionMomentary(25);
    }
    
    /**
     * Get Momentary Function 26 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF26Momentary() {
        return getFunctionMomentary(26);
    }
    
    /**
     * Get Momentary Function 27 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF27Momentary() {
        return getFunctionMomentary(27);
    }
    
    /**
     * Get Momentary Function 28 Status.
     * @return true for Momentary Function On, else false.
     */
    public default boolean getF28Momentary() {
        return getFunctionMomentary(28);
    }
    
    /**
     * Set Momentary Function 0 Status.
     * @param f0Momentary true for Momentary Function On, else false.
     */
    public default void setF0Momentary(boolean f0Momentary) {
        setFunctionMomentary(0,f0Momentary);
    }
    
    /**
     * Set Momentary Function 1 Status.
     * @param f1Momentary true for Momentary Function On, else false.
     */
    public default void setF1Momentary(boolean f1Momentary) {
        setFunctionMomentary(1,f1Momentary);
    }
    
    /**
     * Set Momentary Function 2 Status.
     * @param f2Momentary true for Momentary Function On, else false.
     */
    public default void setF2Momentary(boolean f2Momentary) {
        setFunctionMomentary(2,f2Momentary);
    }
    
    /**
     * Set Momentary Function 3 Status.
     * @param f3Momentary true for Momentary Function On, else false.
     */
    public default void setF3Momentary(boolean f3Momentary) {
        setFunctionMomentary(3,f3Momentary);
    }
    
    /**
     * Set Momentary Function 4 Status.
     * @param f4Momentary true for Momentary Function On, else false.
     */
    public default void setF4Momentary(boolean f4Momentary) {
        setFunctionMomentary(4,f4Momentary);
    }
    
    /**
     * Set Momentary Function 5 Status.
     * @param f5Momentary true for Momentary Function On, else false.
     */
    public default void setF5Momentary(boolean f5Momentary) {
        setFunctionMomentary(5,f5Momentary);
    }
    
    /**
     * Set Momentary Function 6 Status.
     * @param f6Momentary true for Momentary Function On, else false.
     */
    public default void setF6Momentary(boolean f6Momentary) {
        setFunctionMomentary(6,f6Momentary);
    }
    
    /**
     * Set Momentary Function 7 Status.
     * @param f7Momentary true for Momentary Function On, else false.
     */
    public default void setF7Momentary(boolean f7Momentary) {
        setFunctionMomentary(7,f7Momentary);
    }
    
    /**
     * Set Momentary Function 8 Status.
     * @param f8Momentary true for Momentary Function On, else false.
     */
    public default void setF8Momentary(boolean f8Momentary) {
        setFunctionMomentary(8,f8Momentary);
    }
    
    /**
     * Set Momentary Function 9 Status.
     * @param f9Momentary true for Momentary Function On, else false.
     */
    public default void setF9Momentary(boolean f9Momentary) {
        setFunctionMomentary(9,f9Momentary);
    }
    
    /**
     * Set Momentary Function 10 Status.
     * @param f10Momentary true for Momentary Function On, else false.
     */
    public default void setF10Momentary(boolean f10Momentary) {
        setFunctionMomentary(10,f10Momentary);
    }
    
    /**
     * Set Momentary Function 11 Status.
     * @param f11Momentary true for Momentary Function On, else false.
     */
    public default void setF11Momentary(boolean f11Momentary) {
        setFunctionMomentary(11,f11Momentary);
    }
    
    /**
     * Set Momentary Function 12 Status.
     * @param f12Momentary true for Momentary Function On, else false.
     */
    public default void setF12Momentary(boolean f12Momentary) {
        setFunctionMomentary(12,f12Momentary);
    }
    
    /**
     * Set Momentary Function 13 Status.
     * @param f13Momentary true for Momentary Function On, else false.
     */
    public default void setF13Momentary(boolean f13Momentary) {
        setFunctionMomentary(13,f13Momentary);
    }
    
    /**
     * Set Momentary Function 14 Status.
     * @param f14Momentary true for Momentary Function On, else false.
     */
    public default void setF14Momentary(boolean f14Momentary) {
        setFunctionMomentary(14,f14Momentary);
    }
    
    /**
     * Set Momentary Function 15 Status.
     * @param f15Momentary true for Momentary Function On, else false.
     */
    public default void setF15Momentary(boolean f15Momentary) {
        setFunctionMomentary(15,f15Momentary);
    }
    
    /**
     * Set Momentary Function 16 Status.
     * @param f16Momentary true for Momentary Function On, else false.
     */
    public default void setF16Momentary(boolean f16Momentary) {
        setFunctionMomentary(16,f16Momentary);
    }
    
    /**
     * Set Momentary Function 17 Status.
     * @param f17Momentary true for Momentary Function On, else false.
     */
    public default void setF17Momentary(boolean f17Momentary) {
        setFunctionMomentary(17,f17Momentary);
    }
    
    /**
     * Set Momentary Function 18 Status.
     * @param f18Momentary true for Momentary Function On, else false.
     */
    public default void setF18Momentary(boolean f18Momentary) {
        setFunctionMomentary(18,f18Momentary);
    }
    
    /**
     * Set Momentary Function 19 Status.
     * @param f19Momentary true for Momentary Function On, else false.
     */
    public default void setF19Momentary(boolean f19Momentary) {
        setFunctionMomentary(19,f19Momentary);
    }
    
    /**
     * Set Momentary Function 20 Status.
     * @param f20Momentary true for Momentary Function On, else false.
     */
    public default void setF20Momentary(boolean f20Momentary) {
        setFunctionMomentary(20,f20Momentary);
    }

    /**
     * Set Momentary Function 21 Status.
     * @param f21Momentary true for Momentary Function On, else false.
     */
    public default void setF21Momentary(boolean f21Momentary) {
        setFunctionMomentary(21,f21Momentary);
    }
    
    /**
     * Set Momentary Function 22 Status.
     * @param f22Momentary true for Momentary Function On, else false.
     */
    public default void setF22Momentary(boolean f22Momentary) {
        setFunctionMomentary(22,f22Momentary);
    }
    
    /**
     * Set Momentary Function 23 Status.
     * @param f23Momentary true for Momentary Function On, else false.
     */
    public default void setF23Momentary(boolean f23Momentary) {
        setFunctionMomentary(23,f23Momentary);
    }
    
    /**
     * Set Momentary Function 24 Status.
     * @param f24Momentary true for Momentary Function On, else false.
     */
    public default void setF24Momentary(boolean f24Momentary) {
        setFunctionMomentary(24,f24Momentary);
    }
    
    /**
     * Set Momentary Function 25 Status.
     * @param f25Momentary true for Momentary Function On, else false.
     */
    public default void setF25Momentary(boolean f25Momentary) {
        setFunctionMomentary(25,f25Momentary);
    }
    
    /**
     * Set Momentary Function 26 Status.
     * @param f26Momentary true for Momentary Function On, else false.
     */
    public default void setF26Momentary(boolean f26Momentary) {
        setFunctionMomentary(26,f26Momentary);
    }
    
    /**
     * Set Momentary Function 27 Status.
     * @param f27Momentary true for Momentary Function On, else false.
     */
    public default void setF27Momentary(boolean f27Momentary) {
        setFunctionMomentary(27,f27Momentary);
    }
    
    /**
     * Set Momentary Function 28 Status.
     * @param f28Momentary true for Momentary Function On, else false.
     */
    public default void setF28Momentary(boolean f28Momentary) {
        setFunctionMomentary(28,f28Momentary);
    }

    /**
     * Locomotive address. The exact format is defined by the specific
     * implementation, as subclasses of LocoAddress will contain different
     * information.
     * <p>
     * This is an unbound property.
     *
     * @return The locomotive address
     */
    public LocoAddress getLocoAddress();

    /**
     * Get a list of property change listeners.
     * 
     * @return a list of listeners
     * @deprecated since 4.19.5; use {@link #getPropertyChangeListeners()} or
     * {@link #getPropertyChangeListeners(java.lang.String)} instead
     */
    @Deprecated
    public List<PropertyChangeListener> getListeners();

    /**
     * Not for general use, see {@link #release(ThrottleListener l)} and
     * {@link #dispatch(ThrottleListener l)}.
     * <p>
     * Dispose of object when finished it. This does not free any hardware
     * resources used; rather, it just cleans up the software implementation.
     * <p>
     * Used for handling certain internal error conditions, where the object
     * still exists but hardware is not associated with it.
     * <p>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     *
     * @param l {@link ThrottleListener} to dispose of
     */
    public void dispose(ThrottleListener l);

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else.
     * <p>
     * After this, further usage of this Throttle object will result in a
     * JmriException. Do not call dispose after release.
     * <p>
     * Normally, release ends with a call to dispose.
     *
     * @param l {@link ThrottleListener} to release. May be null if no
     *          {@link ThrottleListener} is currently held.
     */
    public void release(ThrottleListener l);

    /**
     * Finished with this Throttle, tell the layout that the locomotive is
     * available for reuse/reallocation by somebody else. If possible, tell the
     * layout that this locomotive has been dispatched to another user. Not all
     * layouts will implement this, in which case it is synonymous with
     * {@link #release(jmri.ThrottleListener)}.
     * <p>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     * <p>
     * Normally, dispatch ends with a call to dispose.
     *
     * @param l {@link ThrottleListener} to dispatch
     */
    public void dispatch(ThrottleListener l);

    public void setRosterEntry(BasicRosterEntry re);

    public BasicRosterEntry getRosterEntry();

    /**
     * Notify listeners that a Throttle has Release enabled or disabled.
     * <p>
     * For systems where release availability is variable.
     *
     * @param newVal true if Release enabled, else false
     */
    public void notifyThrottleReleaseEnabled(boolean newVal);

    /**
     * Notify listeners that a Throttle has Dispatch enabled or disabled.
     * <p>
     * For systems where dispatch availability is variable.
     *
     * @param newVal true if Dispatch enabled, else false
     */
    public void notifyThrottleDispatchEnabled(boolean newVal);
}
