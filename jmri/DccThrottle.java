// DccThottle.java

package jmri;

/**
 * Provide DCC-specific extensions to Throttle interface.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.8 $
 * @see Throttle
 */
public interface DccThrottle extends Throttle {

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement();

    // Handle Speed Step Information

    // There are 4 valid speed step modes
    public static final int SpeedStepMode128 = 1;
    public static final int SpeedStepMode28 = 2;
    public static final int SpeedStepMode27 = 4;
    public static final int SpeedStepMode14 = 8;

    /*
     * setSpeedStepMode - set the speed step value.
     * <P>
     * @parm Mode - the current speed step mode - default should be 128 
     *              speed step mode in most cases
     */
     public void setSpeedStepMode(int Mode);

    /*
     * getSpeedStepMode - get the current speed step value.
     * <P>
     */
     public int getSpeedStepMode();

    // information on consisting  (how do we set consisting?)

    // register for notification


}


/* @(#)DccThottle.java */
