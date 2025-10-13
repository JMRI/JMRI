package jmri.clock;

import jmri.TimebaseRateException;

/**
 * The rate of a clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockRate {

    /**
     * Property Change sent when the rate value changes.
     */
    String PROPERTY_CHANGE_RATE = "rate";

    /**
     * Set fast clock rate.
     *
     * @param factor the fast clock rate
     * @throws jmri.TimebaseRateException if the implementation can not use the
     *                                        requested rate
     */
    void setRate(double factor) throws TimebaseRateException;

    /**
     * Get the true fast clock rate even if the master timebase rate has been
     * modified by a hardware clock. External changes in fast clock rate occur
     * because of the peculiar way some hardware clocks attempt to synchronize
     * with the JMRI fast clock.
     *
     * @return the rate, e.g. 1.0 runs at the same rate as real clocks,
     *         2.0 at twice the speed.
     */
    double getRate();

    /**
     * Set the start clock speed rate.
     * @param factor start clock speed factor.
     */
    void setStartRate(double factor);

    /**
     * Get the startup clock speed rate.
     * @return startup clock speed rate factor.
     */
    double getStartRate();

    /**
     * Set Set Rate at Start option.
     * @param set If true, the rate at startup will be set to the value of getStartRate().
     */
    void setSetRateAtStart(boolean set);

    /**
     * Get if to Set Rate at Start option checked.
     * @return If true, the rate at startup should be set to the value of getStartRate()
     */
    boolean getSetRateAtStart();

}
