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
 * @version			$Revision: 1.3 $
 */
public class SerialSensorManager extends jmri.AbstractSensorManager
                            implements SerialListener {

    public SerialSensorManager() {
        super();
        for (int i=MINNODE; i<=MAXNODE; i++)
            nodeArray[i] = null;
    }

    // ABC implementations

    // to free resources when no longer used
    public void dispose() throws JmriException {
    }

    public Sensor newSensor(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("newSensor:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));

        // if system name is null, supply one from the number in userName
        if (systemName == null) systemName = "CS"+userName;

        // get number from name
        if (!systemName.startsWith("CS")) {
            log.error("Invalid system name for C/MRI serial sensor: "+systemName);
            return null;
        }
        int number = Integer.parseInt(systemName.substring(2));

        // return existing if there is one
        Sensor s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) return s;
        if ( (systemName!=null) && ((s = getBySystemName(systemName)) != null)) return s;

        // doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(systemName);
        else
            s = new SerialSensor(systemName, userName);

        // save in the maps
        _tsys.put(systemName, s);
        if (userName!=null) _tuser.put(userName, s);

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
