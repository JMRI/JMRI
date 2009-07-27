// SerialSensorManager.java

package jmri.jmrix.powerline;

import jmri.Sensor;

/**
 * Manage the system-specific Sensor implementation.
 * <P>
 * System names are:
 * 		Powerline - "PSann", where a is the house code and nn is the unit number without padding.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author			Ken Cameron, (C) 2009, sensors from poll replies
 * @version			$Revision: 1.10 $
 */
abstract public class SerialSensorManager extends jmri.managers.AbstractSensorManager
                            implements SerialListener {

    public SerialSensorManager() {
        super();
        _instance = this;
        SerialTrafficController.instance().addSerialListener(this);
    }

    /**
     * Return the system letter
     */
    public char systemLetter() { return 'P'; }

    // to free resources when no longer used
    public void dispose() {
        super.dispose();
    }

    /**
     * Create a new sensor if all checks are passed
     *    System name is normalized to ensure uniqueness.
     */
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
        if (sName=="") {
            // system name is not valid
            log.error("Invalid Sensor system name - "+systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s!=null) {
            log.error("Sensor with this name already exists - "+systemName);
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(sName);
        else
            s = new SerialSensor(sName, userName);

        return s;
    }
    
    /**
     * Dummy routine
     */
    public void message(SerialMessage r) {
    	// this happens during some polls from sensor messages
        //log.warn("unexpected message");
    }

    /**
     *  Process a reply to a poll of Sensors of one node
     */
    abstract public void reply(SerialReply r);
    
    /**
     * static function returning the SerialSensorManager instance to use.
     * @return The registered SerialSensorManager instance for general use,
     *         if need be creating one.
     */
    static public SerialSensorManager instance() {
        if (_instance == null) log.error("powerline.SerialSensorManager had no instance available");
        return _instance;
    }

    static SerialSensorManager _instance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
