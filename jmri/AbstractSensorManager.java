// AbstractSensorManager.java

package jmri;


/**
 * Abstract base implementation of the SensorManager interface
 * @author			Bob Jacobsen Copyright (C) 2001, 2003
 * @version			$Revision: 1.10 $
 */
public abstract class AbstractSensorManager extends AbstractManager implements SensorManager {

    public char typeLetter() { return 'S'; }

    public Sensor provideSensor(String name) {
        Sensor t = getSensor(name);
        if (t!=null) return t;
        if (name.startsWith(""+systemLetter()+typeLetter()))
            return newSensor(name, null);
        else
            return newSensor(makeSystemName(name), null);
    }

    public Sensor getSensor(String name) {
        Sensor t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public Sensor getBySystemName(String key) {
        return (Sensor)_tsys.get(key);
    }

    public Sensor getByUserName(String key) {
        return (Sensor)_tuser.get(key);
    }

    public Sensor newSensor(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("newSensor:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));
        if (systemName == null) log.error("SystemName cannot be null. UserName was "
                                        +( (userName==null) ? "null" : userName));
        // is system name in correct format?
        if (!systemName.startsWith(""+systemLetter()+typeLetter())) {
            log.error("Invalid system name for sensor: "+systemName
                            +" needed "+systemLetter()+typeLetter());
            return null;
        }

        // return existing if there is one
        Sensor s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
            if (userName != null) log.warn("Found turnout via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return s;
        }

        // doesn't exist, make a new one
        s = createNewSensor(systemName, userName);

        // save in the maps
        register(s);

        return s;
    }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return new null
     */
    abstract protected Sensor createNewSensor(String systemName, String userName);

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractSensorManager.class.getName());
}

/* @(#)AbstractTurnoutManager.java */
