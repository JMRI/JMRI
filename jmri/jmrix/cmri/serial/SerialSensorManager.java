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
 * @version			$Revision: 1.9 $
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
        for (int i=MINNODE; i<=MAXNODE; i++)
            nodeArray[i] = null;
        _instance = this;
    }

    public char systemLetter() { return 'C'; }

    // to free resources when no longer used
    public void dispose() {
    }

    public Sensor createNewSensor(String systemName, String userName) {

        int number = Integer.parseInt(systemName.substring(2));
        Sensor s;
        // doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(systemName);
        else
            s = new SerialSensor(systemName, userName);

        // ensure the serial node exists
        int index = (number/SENSORSPERUA);
        if (nodeArray[index] == null) {
            if (log.isDebugEnabled()) log.debug("New SerialNode address:"+index+" number:"+number);
            nodeArray[index] = SerialTrafficController.instance().getNodeFromAddress(index);
            if (nodeArray[index] == null) {
                // Sensor refers to undefined node
                log.error("Sensor " + systemName + " refers to an undefined Serial Node.");
            }
        }
        nodesPresent = true;

        // register this sensor with the SerialNode
        if (nodeArray[index] != null) {
            nodeArray[index].registerSensor((SerialSensor)s, number-index*SENSORSPERUA-1);
        }
        return s;
    }

    SerialNode[] nodeArray = new SerialNode[MAXNODE+1]; // indexed by node address, which
                                                        //    ranges from 0 thru MAXNODE
    private static int MINNODE =  0;
    private static int MAXNODE = 127;
    private int mNextIndex = MINNODE;
    private boolean nodesPresent = false;

    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }
    /**
     *  Process a reply to a poll of Sensors of one node
     */
    public void reply(SerialReply r) {
        // determine which node
        nodeArray[r.getUA()].markChanges(r);
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
