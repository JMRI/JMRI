package jmri;

/**
 * Interface for calculating an average meter value for a meter.
 *
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public interface AverageMeter extends Meter {

    /**
     * Get the meter.
     * @return the meter
     */
    Meter getMeter();

    /**
     * Set the meter.
     * @param meter the meter
     */
    void setMeter(Meter meter);

    /**
     * Get the time in milliseconds to average on.
     * @return the time
     */
    int getTime();

    /**
     * Set the time in milliseconds to average on.
     * @param time the time
     */
    void setTime(int time);

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     * <p>
     * The meter must be disabled before it's disposed.
     */
    @Override
    void dispose();

}
