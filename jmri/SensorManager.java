// SensorManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Interface for controlling sensors
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.6 $
 */
public interface SensorManager {

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new sensor using this as a
     * default name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Sensor getSensor(String name);

    // to free resources when no longer used
    public void dispose() throws JmriException;

    public Sensor newSensor(String systemName, String userName);

    public Sensor getByUserName(String s);
    public Sensor getBySystemName(String s);

    public List getSystemNameList();

}


/* @(#)SensorManager.java */
