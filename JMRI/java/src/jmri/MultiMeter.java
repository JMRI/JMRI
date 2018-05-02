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
     * Set the current.
     *
     * @param c the current
     * @deprecated since 4.7.1; use {@link #setCurrent(float)} instead
     */
    @Deprecated
    public void updateCurrent(float c);

    public float getCurrent();

    /**
     * Set the voltage.
     *
     * @param v the voltage
     */
    public void setVoltage(float v);

    /**
     * Set the voltage.
     *
     * @param v the voltage
     * @deprecated since 4.7.1; use {@link #setVoltage(float)} instead
     */
    @Deprecated
    public void updateVoltage(float v);

    public float getVoltage();

    public void initializeHardwareMeter();

    public String getHardwareMeterName();

    public boolean hasCurrent();

    public boolean hasVoltage();

    /**
     * Request a call-back when the current changes.
     *
     * @param l the listener to add
     * @deprecated since 4.7.1; use
     * {@link #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
     * with {@link #CURRENT} as the first parameter
     */
    @Deprecated
    public void addDataUpdateListener(PropertyChangeListener l);

    /**
     * Remove a request for call-back when the current changes.
     *
     * @param l the listener to remove
     * @deprecated since 4.7.1; use
     * {@link #removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
     * with {@link #CURRENT} as the first parameter
     */
    @Deprecated
    public void removeDataUpdateListener(PropertyChangeListener l);

    /**
     * Get the list of minute change listeners.
     *
     * @return a list of listeners or an empty list
     * @deprecated since 4.7.1; use
     * {@link #getPropertyChangeListeners(java.lang.String)} with
     * {@link #CURRENT} as the parameter
     */
    @Deprecated
    public PropertyChangeListener[] getDataUpdateListeners();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose();

}
