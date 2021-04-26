package jmri.jmrit.ctc.ctcserialdata;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.ctc.*;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.gui.FrmTRL_Rules;
import jmri.jmrit.ctc.topology.TopologyInfo;

/**
 * This describes one traffic locking rule.
 * <p>
 * The TRLSwitch sub-class contains switch, alignment and ID values.
 * <p>
 * The list of traffic locking rules for each OS section are in the
 * _mTRL_LeftTrafficLockingRules and _mTRL_RightTrafficLockingRuless variable
 * in {@link CodeButtonHandlerData}.
 * @author Dave Sand Copyright (C) 2020
 */
public class TrafficLockingData {
    public String _mUserRuleNumber;
    public String _mRuleEnabled;
    public String _mDestinationSignalOrComment;
    public List<TRLSwitch> _mSwitchAlignments;              // Up to 5 entries
    public List<NBHSensor> _mOccupancyExternalSensors;      // Up to 9 entries
    public List<NBHSensor> _mOptionalExternalSensors;       // Up to 2 entries

    public TrafficLockingData() {
    }

    /**
     * Constructor to take a TopologyInfo entry and create a properly formed "this".
     *
     * @param ruleNumber    Rule # (just an integer, starting with 1)
     * @param destinationSignalMast String representation of the destination signal mast so user can see on the form.
     * @param topologyInfo  Source of data.
     */
    public TrafficLockingData(int ruleNumber, String destinationSignalMast, TopologyInfo topologyInfo) {
//         Note:  The Topology process needs to updated.  It needs to create this class for each
//         row and update the appropriate instance of CodeButtonHandleData.
        _mUserRuleNumber = FrmTRL_Rules.getRuleNumberString(ruleNumber);
        _mRuleEnabled = FrmTRL_Rules.getRuleEnabledString();
        _mDestinationSignalOrComment = topologyInfo.getDestinationSignalMast();

        _mSwitchAlignments = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (topologyInfo.getOSSectionText(i) != null) {
                int intID;
                try {
                    intID = Integer.parseInt(topologyInfo.getUniqueID(i));
                } catch (NumberFormatException ex) {
                    log.warn("TrafficLockingData format exception: id = {}", topologyInfo.getUniqueID(i));
                    intID = 0;
                }
               _mSwitchAlignments.add(new TRLSwitch(topologyInfo.getOSSectionText(i), topologyInfo.getNormalReversed(i), intID));
            }
        }

        _mOccupancyExternalSensors = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            String sensorName = topologyInfo.getSensorDisplayName(i);
            NBHSensor sensor = CommonSubs.getNBHSensor(sensorName, false);
            if (sensor != null && sensor.valid()) {
                _mOccupancyExternalSensors.add(sensor);
            }
        }

        _mOptionalExternalSensors = new ArrayList<>();
    }

    /**
     * Create a list of occupancy sensors with 9 entries. Unused entries will be set to a dummy NBHSensor.
     * @return a list of occupancy sensors.
     */
    public ArrayList<NBHSensor> getOccupancySensors() {
        NBHSensor dummy = new NBHSensor("TrafficLockingData", "", "", "", true);  // NOI18N
        NBHSensor[] occupancyArray = new NBHSensor[9];
        Arrays.fill(occupancyArray, dummy);
        ArrayList<NBHSensor> occupancyList = new ArrayList<>(Arrays.asList(occupancyArray));
        for (int index = 0; index < _mOccupancyExternalSensors.size(); index++) {
            occupancyList.set(index, _mOccupancyExternalSensors.get(index));
        }
        return occupancyList;
    }

    /**
     * Create a list of optional sensors with 2 entries. Unused entries will be set to a dummy NBHSensor.
     * @return a list of optional sensors.
     */
    public ArrayList<NBHSensor> getOptionalSensors() {
        NBHSensor dummy = new NBHSensor("TrafficLockingData", "", "", "", true);  // NOI18N
        NBHSensor[] optionalArray = new NBHSensor[2];
        Arrays.fill(optionalArray, dummy);
        ArrayList<NBHSensor> optionalList = new ArrayList<>(Arrays.asList(optionalArray));
        for (int index = 0; index < _mOptionalExternalSensors.size(); index++) {
            optionalList.set(index, _mOptionalExternalSensors.get(index));
        }
        return optionalList;
    }

    /**
     * Create a list of unique ids with 5 entries.  Unused entries are set to -1.
     * @return a list of ids
     */
    public ArrayList<Integer> getUniqueIDs() {
        Integer[] ids = new Integer[5];
        Arrays.fill(ids, -1);
        ArrayList<Integer> idList = new ArrayList<>(Arrays.asList(ids));
        for (int index = 0; index < _mSwitchAlignments.size(); index++) {
            idList.set(index, _mSwitchAlignments.get(index)._mUniqueID);
        }
        return idList;
    }

    /**
     * Create a list of alignments with 5 entries.  Unused entries are to to Normal.
     * @return a list of alignments
     */
    public ArrayList<String> getAlignments() {
        String[] alignment = new String[5];
        Arrays.fill(alignment, Bundle.getMessage("TLE_Normal"));    // NOI18N
        ArrayList<String> alignmentList = new ArrayList<>(Arrays.asList(alignment));
        for (int index = 0; index < _mSwitchAlignments.size(); index++) {
            alignmentList.set(index, _mSwitchAlignments.get(index)._mSwitchAlignment);
        }
        return alignmentList;
    }

    @Override
    public String toString() {
        String formattedString = String.format("%s,%s,%s",
                _mUserRuleNumber != null ? _mUserRuleNumber : "",
                _mRuleEnabled != null ? _mRuleEnabled : "",
                _mDestinationSignalOrComment != null ? _mDestinationSignalOrComment : "");
        StringBuilder buildString = new StringBuilder(formattedString);
        _mSwitchAlignments.forEach(tlrSw -> {
            buildString.append(",");
            buildString.append(tlrSw._mUserText);
            buildString.append(",");
            buildString.append(tlrSw._mSwitchAlignment);
            buildString.append(",");
            buildString.append(tlrSw._mUniqueID);
        });
        _mOccupancyExternalSensors.forEach(sw -> {
            buildString.append(",");
            buildString.append(sw.getHandleName());
        });
        _mOptionalExternalSensors.forEach(sw -> {
            buildString.append(",");
            buildString.append(sw.getHandleName());
        });
        return buildString.toString();
    }

    public static class TRLSwitch {
        public String _mUserText;
        public String _mSwitchAlignment;
        public int _mUniqueID;

        public TRLSwitch(String text, String alignment, int uniqueID) {
            _mUserText = text;
            _mSwitchAlignment = alignment;
            _mUniqueID = uniqueID;
        }
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrafficLockingData.class);
}
