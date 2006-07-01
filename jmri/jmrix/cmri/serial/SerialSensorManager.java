// SerialSensorManager.java

package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Manage the C/MRI serial-specific Sensor implementation.
 * <P>
 * System names are "CSnnn", where nnn is the sensor number without padding.
 * <P>
 * This class is responsible for generating polling messages
 * for the TrafficController,
 * see nextAiuPoll()
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.7 $
 */
public class SerialSensorManager extends jmri.AbstractSensorManager
                            implements SerialListener {

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
        int index = (number/128);
        if (nodeArray[index] == null) {
            if (log.isDebugEnabled()) log.debug("New SerialNode index:"+index+" number:"+number);
            nodeArray[index] = new SerialNode();
        }
        nodesPresent = true;

        // register this sensor with the SerialNode
        nodeArray[index].registerSensor((SerialSensor)s, number-index*128-1);

        return s;
    }

    SerialNode[] nodeArray = new SerialNode[MAXNODE+1];  // element 0 isn't used


    private static int MINNODE =  0;
    private static int MAXNODE = 63;
    private int mNextIndex = MINNODE;
    private boolean nodesPresent = false;

    public SerialMessage nextPoll() {
        if (!nodesPresent) return null;

        // increment to next entry
        mNextIndex++;
        if (mNextIndex>MAXNODE) mNextIndex = MINNODE;

        // skip over undefined AIU entries
        while (nodeArray[mNextIndex]==null) {
            mNextIndex++;
            if (mNextIndex>MAXNODE) mNextIndex = MINNODE;
        }

        return SerialMessage.getPoll(mNextIndex);
    }

    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }
    public void reply(SerialReply r) {
        nodeArray[mNextIndex].markChanges(r);
    }

    static public SerialSensorManager instance() {
        if (_instance == null) _instance = new SerialSensorManager();
        return _instance;
    }

    static SerialSensorManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
