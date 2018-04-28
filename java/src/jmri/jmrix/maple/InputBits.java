package jmri.jmrix.maple;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting input from Maple HMI's
 * <P>
 * All of the Maple HMI panels are configured with the same input bits. As each
 * HMI is polled, the results are ORed together in an input array that is
 * initialized to all 0 when a polling cycle is initiated. That way, if a bit is
 * 1 in any of the HMI's, it will be 1 in the input array at the end of the
 * polling cycle. At the end of each polling cycle, each input bit is compared
 * to the input bit from the last polling cycle. If a bit has changed, or if
 * this is the first polling cycle, the correspnding Sensor state is updated. No
 * updating occurs if all panels timed out. Serial systems with unique input
 * bits for each node keep their input array in each node. That code has been
 * moved to this utility class for Maple Systems because all nodes share the
 * same set of input bits. Coil bits within Maple Systems HMI's are divided into
 * input (1-1000) and output (1001-9000), so input bits are read starting from
 * HMI address 1, and output bits are written starting at HMI address 1001.
 *
 * @author Dave Duchamp, Copyright (C) 2009
 */
public class InputBits {

    private SerialTrafficController tc = null;

    public InputBits(SerialTrafficController _tc) {
        // clear the Sensor arrays
        for (int i = 0; i < MAXSENSORS + 1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
            sensorORedSetting[i] = false;
        }
        tc = _tc;
    }

    // class constants
    static final int MAXSENSORS = 1000;

    // operational variables
    private static int mNumInputBits = 48;     // number of Sensors that are configured
    private static int mTimeoutTime = 2000;    // timeout time when polling nodes (milliseconds)
    private int lastUsedSensor = -1; // number of Sensors that are in use - 1 (less than above)
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS + 1];
    protected int[] sensorLastSetting = new int[MAXSENSORS + 1];
    protected int[] sensorTempSetting = new int[MAXSENSORS + 1];
    protected boolean[] sensorORedSetting = new boolean[MAXSENSORS + 1];

    // access routines
    public static void setNumInputBits(int n) {
        mNumInputBits = n;
    }

    public static int getNumInputBits() {
        return mNumInputBits;
    }

    public static void setTimeoutTime(int n) {
        mTimeoutTime = n;
    }

    public static int getTimeoutTime() {
        return mTimeoutTime;
    }

    public int getLastSensor() {
        return lastUsedSensor;
    }

    public int getMaxSensors() {
        return MAXSENSORS;
    }

    public void forceSensorsUnknown() {
        // force sensors to UNKNOWN, including callbacks; might take some time
        for (int i = 0; i <= lastUsedSensor; i++) {
            if (sensorArray[i] != null) {
                sensorLastSetting[i] = Sensor.UNKNOWN;
                sensorTempSetting[i] = Sensor.UNKNOWN;
                sensorORedSetting[i] = false;
                try {
                    sensorArray[i].setKnownState(Sensor.UNKNOWN);
                } catch (jmri.JmriException ex) {
                    log.error("unexpected exception setting sensor i={}, ex:{}", i, ex);
                }
            }
        }
    }

    /**
     * Use the contents of the poll reply to mark changes Bits from all the
     * polled panels are ORed together. So if any panel has the bit on (after
     * any inversion is provided for), the resulting bit will be on when the
     * polling cycle is complete.
     *
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        int begAddress = tc.getSavedPollAddress();
        int count = l.getNumDataElements() - 8;
        for (int i = 0; i < count; i++) {
            if (sensorArray[i + begAddress - 1] == null) {
                continue; // skip ones that don't exist
            }
            boolean value = ((l.getElement(5 + i) & 0x01) != 0) ^ sensorArray[i + begAddress - 1].getInverted();

            if (value) {
                // considered ACTIVE
                sensorORedSetting[i + begAddress - 1] = true;
            }
        }
    }

    /**
     * This routine is invoked at the end of each polling cycle to change those
     * Sensors that have changed during the polling cycle. After making whatever
     * changes are warranted, this routine clears the array for accumulating a
     * new polling cycle. Only sensors that are actually defined are updated
     */
    public void makeChanges() {
        // update Sensors according to poll results
        try {
            for (int i = 0; i <= lastUsedSensor; i++) {
                if (sensorArray[i] == null) {
                    continue; // skip ones that don't exist
                }
                boolean value = sensorORedSetting[i];

                if (value) {
                    // considered ACTIVE
                    if (((sensorTempSetting[i] == Sensor.ACTIVE)
                            || (sensorTempSetting[i] == Sensor.UNKNOWN))
                            && (sensorLastSetting[i] != Sensor.ACTIVE)) { // see comment at top; allows persistent local changes
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                        // log.debug("set active");
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.ACTIVE;
                } else {
                    // considered INACTIVE
                    if (((sensorTempSetting[i] == Sensor.INACTIVE)
                            || (sensorTempSetting[i] == Sensor.UNKNOWN))
                            && (sensorLastSetting[i] != Sensor.INACTIVE)) {  // see comment at top; allows persistent local changes
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                        // log.debug("set inactive");
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.INACTIVE;
                }
            }
        } catch (JmriException e) {
            log.error("exception in makeChanges: " + e);
        }

        // clear the accumulation array;
        for (int i = 0; i < lastUsedSensor; i++) {
            sensorORedSetting[i] = false;
        }
    }

    /**
     * The numbers here are 0 to MAXSENSORS, not 1 to MAXSENSORS.
     *
     * @param s - Sensor object
     * @param i - 0 to MAXSENSORS number of sensor's input bit on this node
     */
    public void registerSensor(Sensor s, int i) {
        // validate the sensor ordinal
        if ((i < 0) || (i >= mNumInputBits)) {
            log.error("Unexpected sensor ordinal in registerSensor: {}", Integer.toString(i + 1));
            return;
        }
        if (sensorArray[i] == null) {
            sensorArray[i] = s;
            if (lastUsedSensor < i) {
                lastUsedSensor = i;
            }
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
            sensorORedSetting[i] = false;
        }
    }

    @Deprecated
    public static InputBits instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(InputBits.class);

}
