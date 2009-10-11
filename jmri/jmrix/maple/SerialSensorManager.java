// SerialSensorManager.java

package jmri.jmrix.maple;

import jmri.Sensor;
import jmri.jmrix.AbstractNode;

/**
 * Manage the specific Sensor implementation.
 * <P>
 * System names are "KSnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
 * This is a SerialListener to handle the replies to poll messages.
 * Those are forwarded to the specific SerialNode object corresponding
 * to their origin for processing of the data.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003, 2007, 2008
 * @author                      Dave Duchamp, multi node extensions, 2004
 * @version			$Revision: 1.6 $
 */
public class SerialSensorManager extends jmri.managers.AbstractSensorManager
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
     * Return the system letter
     */
    public char systemLetter() { return 'K'; }

    /**
     * Create a new sensor if all checks are passed
     *    System name is normalized to ensure uniqueness.
     */
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid sensor system name - "+systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s!=null) {
            log.error("Sensor with this name already exists - "+systemName);
            return null;
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName);
        if ( (bit<=0) || (bit>1000) ) {
            log.error("Sensor bit number, "+Integer.toString(bit)+
                    ", is outside the supported range, 1-1000");
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(sName);
        else
            s = new SerialSensor(sName, userName);
		if (s!=null) {
			// check configured
            if (!SerialAddress.validSystemNameConfig(sName,'S')) {
                log.warn("Sensor system Name '"+sName+"' does not address configured hardware.");
				javax.swing.JOptionPane.showMessageDialog(null,"WARNING - The Sensor just added, "+
					sName+", refers to an unconfigured input bit.","Configuration Warning",
						javax.swing.JOptionPane.INFORMATION_MESSAGE,null);
			}
			// register this sensor 
			InputBits.instance().registerSensor(s, bit-1);
		}
		return s;
    }
    
    /**
     * Dummy routine
     */
    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }

    /**
     *  Process a reply to a poll of Sensors of one panel node
     */
    public void reply(SerialReply r) {
		InputBits.instance().markChanges(r);
    }
    
    /**
     * Method to register any orphan Sensors when a new Serial Node is created
     */
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        java.util.Iterator<String> iter =
                                    getSystemNameList().iterator();
        // Iterate through the sensors
        AbstractNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName==null) {
                log.error("System name null during register Sensor");
            }
            else {
                log.debug("system name is "+sName);
                if ( (sName.charAt(0) == 'K') && (sName.charAt(1) == 'S') ) {
                    // This is a valid Sensor - make sure it is registered
                   InputBits.instance().registerSensor(getBySystemName(sName),
                                    (SerialAddress.getBitFromSystemName(sName)-1));
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
