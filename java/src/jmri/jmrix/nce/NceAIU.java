package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model an NCE AIU
 * <p>
 * These AIUs are numbered ala the cab bus, from 1 to 63. AIU number 1 carries
 * sensors 1 to 14; AIU 2 from 17 to 30, etc.
 * <p>
 * The array of sensor states is used to update sensor known state only when
 * there's a change on the cab bus. This allows for the sensor state to be
 * updated within the program, keeping this updated state until the next change
 * on the cab bus. E.g. you can manually change a state via an icon, and not
 * have it change back the next time that AIU is polled.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 */
public class NceAIU {

    public NceAIU() {
        for (int i = 0; i < 15; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
        }
    }

    Sensor[] sensorArray = new Sensor[15];
    int[] sensorLastSetting = new int[15];
    int lastAIUValue = 0xc000;      // can't ever take this value

    /**
     *
     * @param bits int value of response from poll command
     */
    public void markChanges(int bits) {
        if (bits != lastAIUValue) {
            if (log.isDebugEnabled()) {
                log.debug("sensor array change from " + Integer.toHexString(lastAIUValue)
                        + " to " + Integer.toHexString(bits));
            }
            lastAIUValue = bits;
            for (int i = 0; i < 14; i++) {
                if (sensorArray[i] != null) {
                    boolean value = ((bits & 1) == 0) ^ sensorArray[i].getInverted();
                    if (value) {
                        sensorChange(i, Sensor.ACTIVE);
                    } else {
                        sensorChange(i, Sensor.INACTIVE);
                    }
                }
                bits = bits / 2;
            }
        }
    }

    /**
     * set state of a single sensor based on AIU input
     *
     * @param offset   sensor number within the current array
     * @param newState new state (Sensor.ACTIVE / .INACTIVE)
     */
    public void sensorChange(int offset, int newState) {
        if (sensorArray[offset] != null && sensorLastSetting[offset] != newState) {
            sensorLastSetting[offset] = newState;
            if (log.isDebugEnabled()) {
                String newStateStr = "Active";
                if (newState == Sensor.INACTIVE) {
                    newStateStr = "Inactive";
                }
                log.debug("setting sensor " + sensorArray[offset].getSystemName() + ": " + newStateStr);
            }
            try {
                sensorArray[offset].setKnownState(newState);
            } catch (JmriException e) {
                log.error("exception in sensorChange: " + e);
            }
        }
    }

    /**
     * The numbers here are 0 to 15, not 1 to 16
     * @param s bit within the AIU card
     * @param i index for AIU card
     *
     */
    public void registerSensor(Sensor s, int i) {
        sensorArray[i] = s;
    }

    /**
     * Return the sensor object for the specified AIU
     *
     * @param index AIU index (0..15)
     * @return sensor object
     */
    public Sensor getSensor(int index) {
        return sensorArray[index];
    }

    private final static Logger log = LoggerFactory.getLogger(NceAIU.class);
}


