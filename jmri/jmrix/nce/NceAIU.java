// NceAIU.java

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Model an NCE AIU
 * <P>
 * These AIUs are numbered ala the cab bus, from 1 to 63.
 * AIU number 1 carries sensors 1 to 14; AIU 2 from 17 to 30, etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the cab bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the cab bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that AIU is polled.
 *
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.2 $
 */
public class NceAIU {

    public NceAIU() {
        for (int i = 0; i<15; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
        }
    }

    Sensor[] sensorArray = new Sensor[15];
    int[] sensorLastSetting = new int[15];

    /**
     *
     * @param bits int value of response from poll command
     */
    public void markChanges(int bits) {
        try {
            for (int i=0; i<14; i++) {
                if ( (bits&1) ==0 ) {
                    // bit reset, considered ACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.ACTIVE) ) {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                    }
                } else {
                    // bit set, considered INACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.INACTIVE) ) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                    }
                }
                bits = bits/2;
            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }

    /**
     * The numbers here are 0 to 15, not 1 to 16
     * @param s
     * @param i
     */
    public void registerSensor(Sensor s, int i) {
        sensorArray[i] = s;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceAIU.class.getName());
}

/* @(#)NceAIU.java */
