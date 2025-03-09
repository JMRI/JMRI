package jmri.time.implementation;

import java.time.LocalDateTime;

import jmri.time.DateSupport;
import jmri.time.Rate;
import jmri.time.rate.IntegerRate;

/**
 * The system date and time.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class SystemDateTime extends AbstractTimeProvider implements DateSupport {

    // System date and time always has the rate 1:1.
    private static final Rate RATE = new IntegerRate(1);


    public SystemDateTime(String systemName) {
        super(systemName);
    }

    public SystemDateTime(String systemName, String userName) {
        super(systemName, userName);
    }

    /** {@inheritDoc} */
    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.now();
    }

    /** {@inheritDoc} */
    @Override
    public Rate getRate() {
        return RATE;    // System date and time always has the rate 1:1.
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        return true;    // System date and time is always running.
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasWeekday() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDayOfMonth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasMonth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasYear() {
        return true;
    }

}
