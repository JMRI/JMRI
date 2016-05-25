package jmri;

/**
 * Provide DCC-specific extensions to Throttle interface.
 *
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
 * @author	Bob Jacobsen Copyright (C) 2001
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
    public static final int SpeedStepMode28Mot = 16;

    /**
     * Set the speed step value. Default should be 128 speed step mode in most
     * cases
     *
     * @param Mode the current speed step mode
     */
    public void setSpeedStepMode(int Mode);

    /**
     * Get the current speed step value.
     *
     * @return the current speed step mode
     */
    public int getSpeedStepMode();

    // information on consisting  (how do we set consisting?)
    // register for notification
}
