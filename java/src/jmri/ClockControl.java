package jmri;

import java.util.Date;

/**
 * ClockControl defines an interface for control of hardware Fast Clocks
 * <p>
 * Each hardware system that has a hardware Fast Clock implementation must
 * supply a module that implements this interface. Each ClockControl module must
 * register itself with the Instance Manager at start up.
 * <p>
 * Parameters for fast clocks are set up generically in the Fast Clock Setup,
 * accessed via the JMRI Tools menu. These parameters are saved in the
 * configuration file generically, so no special configxml module is needed for
 * storing parameters.
 * <p>
 * Hardware ClockControl modules should extend DefaultClockControl, which
 * supplies default implementations of methods required by this interface that
 * specific hardware implementations may not need.
 * <p>
 * All Clock Control modules communicate with the internal clock and the master
 * JMRI timebase, using methods of the Timebase interface.
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
 * @author Dave Duchamp Copyright (C) 2007
 */
public interface ClockControl {

    /**
     * Get status of the fast clock
     *
     * @return the status
     */
    public int getStatus();

    /**
     * Get name of hardware clock Note: If there is no hardware clock,
     * DefaultClockControl returns null, so all hardware clocks must override
     * this method.
     *
     * @return the name
     */
    public String getHardwareClockName();

    /**
     * Returns true if hardware clock accuracy can be corrected using the
     * computer clock.
     *
     * @return true if correctable; false otherwise
     */
    public boolean canCorrectHardwareClock();

    /**
     * Returns 'true' if hardware clock can be set to 12 or 24 hour display from
     * JMRI software.
     *
     * @return true if settable; false otherwise
     */
    public boolean canSet12Or24HourClock();

    /**
     * Returns true if hardware clock requires an integer rate
     *
     * @return true if integer rates only; false otherwise
     */
    public boolean requiresIntegerRate();

    /**
     * Get and set the rate of the fast clock Note: The rate is a number that
     * multiplies the wall clock time For example, a rate of 4 specifies that
     * the fast clock runs 4 times faster than the wall clock.
     *
     * @param newRate the new rate
     */
    public void setRate(double newRate);

    public double getRate();

    /**
     * Set and get the fast clock time
     *
     * @param now the new time
     */
    public void setTime(Date now);

    public Date getTime();

    /**
     * Start and stop hardware fast clock Some hardware fast clocks continue to
     * run indefinitely. This is provided for the case where the hardware clock
     * can be stopped and started.
     *
     * @param now the starting time
     */
    public void startHardwareClock(Date now);

    public void stopHardwareClock();

    /**
     * Initialize the hardware fast clock Note: When the hardware clock control
     * receives this, it should initialize those clock settings that are
     * available on the hardware clock. This method is used when the fast clock
     * is started, and when time source, synchronize, or correct options are
     * changed. If rate is 0.0, the hardware clock should be initialized
     * "stopped", and the current rate saved for when the clock is restarted. If
     * getTime is "true" the time from the hardware clock should be used in
     * place of the supplied time if possible.
     *
     * @param rate    the rate
     * @param now     the time
     * @param getTime true if hardware clock should be used; false otherwise
     */
    public void initializeHardwareClock(double rate, Date now, boolean getTime);

}
