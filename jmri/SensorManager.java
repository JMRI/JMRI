// SensorManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Interface for controlling sensors
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.5 $
 */
public interface SensorManager {

    // to free resources when no longer used
    public void dispose() throws JmriException;

    public Sensor newSensor(String systemName, String userName);

    public Sensor getByUserName(String s);
    public Sensor getBySystemName(String s);

    public List getSystemNameList();

}


/* @(#)SensorManager.java */
