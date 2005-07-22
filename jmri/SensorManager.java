// SensorManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Interface for controlling sensors.
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.11 $
 */
public interface SensorManager extends Manager {

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new sensor: If the name
     * is a valid system name, it will be used for the new
     * sensor.  Otherwise, the makeSystemName method
     * will attempt to turn it into a valid system name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Sensor provideSensor(String name);

    /**
     * Locate via user name, then system name if needed.
     * Does not create a new one if nothing found
     *
     * @param name
     * @return null if no match found
     */
    public Sensor getSensor(String name);

    // to free resources when no longer used
    public void dispose();

    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Sensor object representing a given physical turnout
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Sensor object created; a valid system name must be provided
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired sensor, and the user address
     *    is associated with it. The system name must be valid.
     *</UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects.  This is a problem, and we don't have a
     * good solution except to issue warnings.
     * This will mostly happen if you're creating Turnouts when you should
     * be looking them up.
     * @return requested Sensor object (never null)
     */
    public Sensor newSensor(String systemName, String userName);

    public Sensor getByUserName(String s);
    public Sensor getBySystemName(String s);

    public List getSystemNameList();
	
    /**
     * Requests status of all layout sensors under this Sensor Manager.
	 * This method may be invoked whenever the status of sensors needs to be updated from
	 *		the layout, for example, when an XML configuration file is read in.
	 * Note that there is a null implementation of this method in AbstractSensorManager.
	 *		This method only needs be implemented in system-specific Sensor Managers where
	 *		readout of sensor status from the layout is possible.
	 */
	public void updateAll();

}


/* @(#)SensorManager.java */
