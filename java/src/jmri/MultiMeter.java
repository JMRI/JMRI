package jmri;

import java.beans.PropertyChangeListener;

/**
 * Interface for displaying (and controlling where appropriate)
 * Current, Voltage, and other status data from the layout.
 *
 * @version: $Revision$
 */

public interface MultiMeter {

    public void enable();
   
    public void disable();
    
    public void updateCurrent(float c);

    public float getCurrent();

    public void updateVoltage(float v);
   
    public float getVoltage();

    public void initializeHardwareMeter();

    public String getHardwareMeterName();

    public boolean hasCurrent();

    public boolean hasVoltage();

    /**
     * Request a call-back when the bound Rate or Run property changes.
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    public void addDataUpdateListener(PropertyChangeListener l);

    /**
     * Remove a request for call-back when the minutes place of the time
     * changes.
     */
    public void removeDataUpdateListener(PropertyChangeListener l);

    /**
     * Get the list of minute change listeners.
     */
    public PropertyChangeListener[] getDataUpdateListeners();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose();


}
