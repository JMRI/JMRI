package jmri.jmrix.powerline.cp290;

import jmri.Sensor;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialSensorManager;
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
 * Sensors are not created automatically as there are frequently other X10 codes
 * seen on the wire that you don't want in your panels.
 * <p>
 * Created from the cm11 version
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, 2010 sensors from poll replies Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificSensorManager extends SerialSensorManager {

    public SpecificSensorManager(SerialTrafficController tc) {
        super(tc);
    }

    /**
     * Process a reply to a poll of Sensors of one node
     */
    @Override
    public synchronized void reply(SerialReply r) {
        // process for updates
        processForPollReq(r);
    }

    private void processForPollReq(SerialReply m) {
        boolean goodSync = true;
        boolean goodCheckSum = true;
        int sum = 0;
        String newHouseCode = null;
        int newCmdCode = -1;
        int newAddrCode = -1;
        Sensor sensor = null;
        if (m.getNumDataElements() == 12) {
            for (int i = 0; i < 6; i++) {
                if ((m.getElement(i) & 0xFF) != 0xFF) {
                    goodSync = false;
                }
            }
            for (int i = 7; i < 11; i++) {
                sum = (sum + (m.getElement(i) & 0xFF)) & 0xFF;
            }
            if (sum != (m.getElement(11) & 0xFF)) {
                goodCheckSum = false;
            }
            newCmdCode = m.getElement(7) & 0x0F;
            newHouseCode = X10Sequence.houseCodeToText((m.getElement(7) >> 4) & 0x0F);
            newAddrCode = (m.getElement(8) & 0x00FF) + ((m.getElement(9) & 0x00FF) << 8);
            if (goodSync && goodCheckSum) {
                int unitMask = 1 << 16;
                int unitCnt = 0;
                while (unitMask > 0) {
                    unitMask = unitMask >> 1;
                    unitCnt++;
                    int hCode = newAddrCode & unitMask;
                    if (hCode != 0) {
                        String sysName = getSystemPrefix() + "S" + newHouseCode + unitCnt;
                        sensor = provideSensor(sysName);

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

                    }
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificSensorManager.class);
}
