// AbstractSensorManager.java

package jmri;

import java.util.*;
import java.util.Hashtable;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * Abstract base implementation of the SensorManager interface
 * @author			Bob Jacobsen Copyright (C) 2001, 2003
 * @version			$Revision: 1.7 $
 */
public abstract class AbstractSensorManager extends AbstractManager implements SensorManager {

    // abstract methods to be provided by subclasses
    public abstract Sensor newSensor(String systemName, String userName);

    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        _tsys.clear();
        _tuser.clear();
    }

    public char typeLetter() { return 'S'; }

    // implemented methods
    protected Hashtable _tsys = new Hashtable();   // stores known Sensor instances by system name
    protected Hashtable _tuser = new Hashtable();   // stores known Sensor instances by user name

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new sensor using this as a
     * default name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Sensor getSensor(String name) {
        Sensor t = getByUserName(name);
        if (t!=null) return t;

        t = getBySystemName(name);
        if (t!=null) return t;

        // did not exist under either name; create via a default
        // of either a valid system name, or a number that can create one
        return newSensor(null, name);
    }

    public Sensor getBySystemName(String key) {
        return (Sensor)_tsys.get(key);
    }

    public Sensor getByUserName(String key) {
        return (Sensor)_tuser.get(key);
    }

    public List getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List out = new ArrayList();
        Enumeration en = _tsys.elements();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = ((Sensor)en.nextElement()).getSystemName();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }

}

/* @(#)AbstractTurnoutManager.java */
