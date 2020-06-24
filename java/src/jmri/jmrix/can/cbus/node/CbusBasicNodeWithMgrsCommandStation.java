package jmri.jmrix.can.cbus.node;

import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicNodeWithMgrsCommandStation extends CbusBasicNodeWithManagers {

    private int _csNum;
    private boolean _StatResponseFlagsAccurate;
    private int _csFlags;

    /**
     * Create a new CbusBasicNode with Managers and Command Station
     *
     * @param connmemo   The CAN Connection to use
     * @param nodenumber The Node Number
     */
    public CbusBasicNodeWithMgrsCommandStation(CanSystemConnectionMemo connmemo, int nodenumber) {
        super(connmemo, nodenumber);
        _csNum = -1;
        _csFlags = -1;
        _StatResponseFlagsAccurate = false;
    }

    /**
     * Set a Command Station Number for this Node
     *
     * @param csnum Command station Number, normally 0 if using a single command
     *              station
     */
    public void setCsNum(int csnum) {
        _csNum = csnum;
    }

    /**
     * Get Command station number.
     * <p>
     * 0 is normally default for a command station
     *
     * @return -1 if node is NOT a Command Station, else CS Number.
     */
    public int getCsNum() {
        return _csNum;
    }

    /**
     * Set the flags reported by a Command Station
     * <p>
     * This will update Track Power On / Off, etc. as per the values passed.
     * Currently unused by CANCMD v4 which sets the
     * setStatResponseFlagsAccurate(false)
     *
     * @param flags the int value of the Command Station flags
     */
    public void setCsFlags(int flags) {
        log.debug("flags value {}", flags);

        // flags value in STAT response not accurate for CANCMD v4
        if (!getStatResponseFlagsAccurate()) {
            return;
        }
        _csFlags = flags;

        // 0 - Hardware Error (self test)
        // 1 - Track Error
        // 2 - Track On/ Off
        // 3 - Bus On/ Halted
        // 4 - EM. Stop all performed
        // 5 - Reset done
        // 6 - Service mode (programming) On/ Off
        checkSingleFlag(0, "Command Station {} Reporting Hardware Error (self test)");
        checkSingleFlag(1, "Command Station {} Reporting Track Error");

        // flag 2 handled by CbusPowerManager
        // listening for RSTAT flag bit 2 here rather than power manager in case in future 
        // we can direct to power zones rather than whole layout power
        // it's also a per command station report than a per layout report
        // TODO: JMRI has multiple PowerManagers so let the correct PowerManager
        // just handle this correctly instead of assuming that the default
        // is a CbusPowerManager
        setTrackPower(((flags >> 2) & 1) == 1);

        checkSingleFlag(3, "Command Station {} Reporting Bus Halted");

    }

    private void setTrackPower(boolean powerOn) {
        CbusPowerManager pm = (CbusPowerManager) _memo.get(PowerManager.class);
        pm.updatePower(powerOn ? PowerManager.ON : PowerManager.OFF);
    }

    private void checkSingleFlag(int flagNum, String errorText) {
        if (((_csFlags >> flagNum) & 1) == 1) {
            log.error(errorText, "" + getCsNum() + " " + getNodeNumber());
        }
    }

    /**
     * Set Disable Command Station Flag Reporting
     *
     * @param accurate set false to ignore the Command Station Flags
     */
    public void setStatResponseFlagsAccurate(boolean accurate) {
        _StatResponseFlagsAccurate = accurate;
    }

    /**
     * Get if Command Station Flag Reporting is accurate. Defaults to false
     *
     * @return true if accurate, else false
     */
    public boolean getStatResponseFlagsAccurate() {
        return _StatResponseFlagsAccurate;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusBasicNodeWithMgrsCommandStation.class);

}
