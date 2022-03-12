package jmri;

/**
 * Interface for displaying (and controlling where appropriate) Current,
 * Voltage, and other status data from the layout.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface Meter extends AnalogIO {
    
    public enum Unit {
        
        /**
         * The meter measures in percent: 0.0 - 100.0
         */
        Percent,
        
        /**
         * The meter measures in kilo units (kilo volts, kilo amperes, ...)
         */
        Kilo,
        
        /**
         * The meter measures in full units (volts, amperes, ...)
         */
        NoPrefix,
        
        /**
         * The meter measures in milli units (milli volts, milli amperes, ...)
         */
        Milli,
        
        /**
         * The meter measures in micro units (micro volts, micro amperes, ...)
         */
        Micro;
    }
    
    public Unit getUnit();

    /**
     * Enable this meter
     */
    public void enable();

    /**
     * Disable this meter
     */
    public void disable();
    
    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     * <p>
     * The meter must be disabled before it's disposed.
     */
    @Override
    public void dispose();

}
