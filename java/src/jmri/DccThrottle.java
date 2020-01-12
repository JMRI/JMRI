package jmri;

/**
 * Provide DCC-specific extensions to Throttle interface.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @see Throttle
 */
public interface DccThrottle extends Throttle {

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement();

    /**
     * Set the speed step value. Default should be 128 speed step mode in most
     * cases
     *
     * @param Mode the current speed step mode
     */
    public void setSpeedStepMode(SpeedStepMode Mode);

    /**
     * Get the current speed step value.
     *
     * @return the current speed step mode
     */
    public SpeedStepMode getSpeedStepMode();

    // information on consisting  (how do we set consisting?)
    // register for notification
}
