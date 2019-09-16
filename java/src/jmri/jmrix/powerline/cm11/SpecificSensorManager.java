package jmri.jmrix.powerline.cm11;

import java.util.List;
import jmri.Sensor;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the system-specific Sensor implementation.
 * <p>
 * System names are "PSann", where a is the unit id, nn is the unit number
 * without padding.
 * <p>
 * Sensors are numbered from 1.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificSensorManager extends jmri.jmrix.powerline.SerialSensorManager {

    public SpecificSensorManager(SerialTrafficController tc) {
        super(tc);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    /**
     * Process a reply to a poll of Sensors of one node
     */
    @Override
    public synchronized void reply(SerialReply r) {
        // process for updates
        processForPollReq(r);
    }

    /**
     * These values need to persist between calls as the address is a different
     * reply from the command packet and timing might have them in separate
     * reads
     */
    private String newHouseCode = null;
    private int newCmdCode = -1;
    private int newAddrCode = -1;

    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    private void processForPollReq(SerialReply l) {
        // process the POLL_REQ and update/create sensors as needed
        if ((l.getElement(0) & 0xFF) == Constants.POLL_REQ) {
            // must be received data
            int last = (l.getElement(1) & 0xFF) + 1;
            int bits = (l.getElement(2) & 0xFF);
            Sensor sensor = null;
            for (int i = 3; i <= last; i++) {
                int dat = l.getElement(i) & 0xFF;
                if ((bits & 0x01) != 0) {
                    // this is a function byte, so the address came from prior pass
                    newHouseCode = X10Sequence.houseValueToText(X10Sequence.decode((dat >> 4) & 0x0F));
                    newCmdCode = dat & 0x0f;

                    if (newHouseCode != null && (newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_ON)) {
                        // some sort of 'global' command, process for all matching the house code
                        List<String> sensors = getSystemNameList();
                        for (int ii = 0; ii < sensors.size(); ii++) {
                            String sName = sensors.get(ii);
                            if (newHouseCode.compareTo(tc.getAdapterMemo().getSerialAddress().houseCodeFromSystemName(sName)) == 0) {
                                try {
                                    sensor = provideSensor(sName);
                                } catch(java.lang.IllegalArgumentException iae){
                                    // if provideSensor fails, it will throw an IllegalArgumentException, so catch that,log it if debugging is enabled, and then re-throw it.
                                    if (log.isDebugEnabled()) {
                                        log.debug("Attempt access sensor " + sName + " failed");
                                    }
                                    throw iae;
                                }
                                try {
                                    if (newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF) {
                                        sensor.setKnownState(Sensor.INACTIVE);
                                    } else {
                                        sensor.setKnownState(Sensor.ACTIVE);
                                    }
                                } catch (jmri.JmriException e) {
                                    if (newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF) {
                                        log.error("Exception setting " + sName + " sensor INACTIVE: " + e);
                                    } else {
                                        log.error("Exception setting " + sName + " sensor ACTIVE: " + e);
                                    }
                                }
                            }
                        }
                    } else {
                        // was not a global command, so might be a sensor
                        if (newAddrCode > 0) {
                            String sysName = getSystemPrefix() + "S" + newHouseCode + newAddrCode;
                            try {
                                sensor = provideSensor(sysName);
                            } catch(java.lang.IllegalArgumentException iae){
                                // if provideSensor fails, it will throw an IllegalArgumentException, so catch that,log it if debugging is enabled, and then re-throw it.
                                if (log.isDebugEnabled()) {
                                    log.debug("Attempt access sensor " + sysName + " failed");
                                }
                                throw iae;
                            }
                            if (newCmdCode == X10Sequence.FUNCTION_ON || newCmdCode == X10Sequence.FUNCTION_BRIGHT || newCmdCode == X10Sequence.FUNCTION_STATUS_ON) {
                                try {
                                    sensor.setKnownState(Sensor.ACTIVE);
                                } catch (jmri.JmriException e) {
                                    log.error("Exception setting " + sysName + " sensor ACTIVE: " + e);
                                }
                            }
                            if (newCmdCode == X10Sequence.FUNCTION_OFF || newCmdCode == X10Sequence.FUNCTION_DIM || newCmdCode == X10Sequence.FUNCTION_STATUS_OFF) {
                                try {
                                    sensor.setKnownState(Sensor.INACTIVE);
                                } catch (jmri.JmriException e) {
                                    log.error("Exception setting " + sysName + " sensor INACTIVE: " + e);
                                }
                            }
 
                            // if we decide we want to add sensors automatically when seen on the wire, this is the place
                        }
                    }
                    // used the pair of address/function, so clear them
                    newHouseCode = null;
                    newCmdCode = -1;
                    newAddrCode = -1;
                } else {
                    // this is an address byte, so just save it
                    newHouseCode = X10Sequence.houseValueToText(X10Sequence.decode((dat >> 4) & 0x0F));
                    newAddrCode = X10Sequence.decode(dat & 0x0f);
                }
                bits = bits >> 1;  // shift over before next byte
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificSensorManager.class);

}
