// SerialNode.java

package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Model a serial C/MRI node
 * <P>
 * These are numbered ala the UA number, from 1 to 63.
 * Node number 1 carries sensors 0 to 127, etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 *
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class SerialNode {

    final int MAXSENSORS = 128;

    public SerialNode() {
        for (int i = 0; i<MAXSENSORS; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
        }
    }

    Sensor[] sensorArray = new Sensor[MAXSENSORS];
    int[] sensorLastSetting = new int[MAXSENSORS];

    /**
     *
     * @param bits int value of response from poll command
     */
    public void markChanges(SerialReply l) {
        try {
            for (int i=0; i<MAXSENSORS; i++) {
                int loc = i/8;
                int bit = i%8;
                int value = (l.getElement(loc+1)>>bit)&0x01;
                if (log.isDebugEnabled()) log.debug("markChanges loc="+loc+" bit="+bit+" is "+value);
                if ( value == 1) {
                    // bit set, considered ACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.ACTIVE) ) {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                    }
                } else {
                    // bit reset, considered INACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.INACTIVE) ) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                    }
                }
            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }

    /**
     * The numbers here are 0 to 127, not 1 to 128
     * @param s
     * @param i
     */
    public void registerSensor(Sensor s, int i) {
        sensorArray[i] = s;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
