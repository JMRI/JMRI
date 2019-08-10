package jmri;

import java.beans.PropertyChangeListener;
import jmri.beans.PropertyChangeProvider;

/**
 * Interface for displaying (and controlling where appropriate) Current,
 * Voltage, and other status data from the layout.
 *
 */
public interface MultiMeter extends PropertyChangeProvider {

    public static final String CURRENT = "MultiMeterCurrent";
    public static final String VOLTAGE = "MultiMeterVoltage";

    public void enable();

    public void disable();

    /**
     * Set the current.
     *
     * @param c the current
     */
    public void setCurrent(float c);

    /**
     * get the current
     * @return the current in units specified by getCurrentUnits
     */
    public float getCurrent();

    /**
     * The units returned by getCurrent.
     * CURRENT_UNITS_PERCENTAGE - 100.0 = 100%
     * CURRENT_UNITS_AMPS - 1 = 1AMP.
     * CURRENT_UNITS_MILLIAMPS - 1000 = 1AMP.
     */
    public static enum CurrentUnits {
        CURRENT_UNITS_PERCENTAGE,
        CURRENT_UNITS_AMPS,
        CURRENT_UNITS_MILLIAMPS
    }

    /**
     * Gets the unit used for current
     * @return the units used for current either percentage (100.0 = 100%) or Amps or milliamps.
     */
    public CurrentUnits getCurrentUnits();

    /**
     * Set the voltage.
     *
     * @param v the voltage in volts.
     */
    public void setVoltage(float v);

    /**
     * get the voltage.
     *
     * @return v the voltage in volts.
     */
    public float getVoltage();

    public void initializeHardwareMeter();

    public String getHardwareMeterName();

    public boolean hasCurrent();

    public boolean hasVoltage();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose();

}
