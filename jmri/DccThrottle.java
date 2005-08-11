// DccThottle.java

package jmri;

/**
 * Provide DCC-specific extensions to Throttle interface.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.6 $
 * @see Throttle
 */
public interface DccThrottle extends Throttle {

    // extends Throttle to allow asking about DCC items

    public int getDccAddress();

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement();

    // information on consisting  (how do we set consisting?)

    // register for notification


}


/* @(#)DccThottle.java */
