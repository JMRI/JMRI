// SerialSensorManager.java

package jmri.jmrix.cmri.serial;

import jmri.Sensor;

/**
 * Manage the C/MRI serial-specific Sensor implementation.
 * <P>
 * System names are "CSnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003
 * @author                      Dave Duchamp, multi node extensions, 2004
 * @version			$Revision: 1.10 $
 */
public class SerialSensorManager extends jmri.AbstractSensorManager
                            implements SerialListener {

    /**
     * Number of sensors per UA in the naming scheme.
     * <P>
     * The first UA (node address) uses sensors from 1 to SENSORSPERUA-1,
     * the second from SENSORSPERUA+1 to SENSORSPERUA+(SENSORSPERUA-1), etc.
     * <P>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     *
     */
    static final int SENSORSPERUA = 1000;

    public SerialSensorManager() {
        super();
        _instance = this;
    }

    /**
     * Return the C/MRI system letter
     */
    public char systemLetter() { return 'C'; }

    // to free resources when no longer used
    public void dispose() {
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
            log.error("Invalid C/MRI Sensor system name - "+systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s!=null) {
            log.error("Sensor with this name already exists - "+systemName);
            return null;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        s = getBySystemName(altName);
        if (s!=null) {
            log.error("Sensor with name '"+systemName+"' already exists as '"+altName+"'");
            return null;
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName);
        if ( (bit<=0) || (bit>=SENSORSPERUA) ) {
            log.error("Sensor bit number, "+Integer.toString(bit)+
                    ", is outside the supported range, 1-"+Integer.toString(SENSORSPERUA-1));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(sName);
        else
            s = new SerialSensor(sName, userName);

        // ensure that a corresponding Serial Node exists
        SerialNode node = SerialAddress.getNodeFromSystemName(sName);
        if (node==null) {
            log.warn("Sensor " + sName + " refers to an undefined Serial Node.");
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor((SerialSensor)s, bit-1);
        return s;
    }
    
    /**
     * Dummy routine
     */
    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }

    /**
     *  Process a reply to a poll of Sensors of one node
     */
    public void reply(SerialReply r) {
        // determine which node
        SerialNode node = SerialTrafficController.instance().getNodeFromAddress(r.getUA());
        if (node!=null) {
            node.markChanges(r);
        }
    }
    
    /**
     * Method to register any orphan Sensors when a new Serial Node is created
     */
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        com.sun.java.util.collections.Iterator iter =
                                    getSystemNameList().iterator();
        // Iterate through the sensors
        SerialNode tNode = null;
        while (iter.hasNext()) {
            String sName = (String)iter.next();
            if (sName==null) {
                log.error("System name null during register Sensor");
            }
            else {
                log.debug("system name is "+sName);
                if ( (sName.charAt(0) == 'C') && (sName.charAt(1) == 'S') ) {
                    // This is a C/MRI Sensor
                    tNode = SerialAddress.getNodeFromSystemName(sName);
                    if (tNode==node) {
                        // This sensor is for this new Serial Node - register it
                        node.registerSensor(getBySystemName(sName),
                                    (SerialAddress.getBitFromSystemName(sName)-1));
                    }
                }
            }
        }
    }

    /**
     * static function returning the SerialSensorManager instance to use.
     * @return The registered SerialSensorManager instance for general use,
     *         if need be creating one.
     */
    static public SerialSensorManager instance() {
        if (_instance == null) _instance = new SerialSensorManager();
        return _instance;
    }

    static SerialSensorManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
