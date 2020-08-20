package jmri.jmrit.ctc.ctcserialdata;

import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.ctc.*;
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

    public TrafficLockingData( String userRuleNumber,
                                String ruleEnabled,
                                String destinationSignalOrComment,
                                List<TRLSwitch> switchAlignments,
                                List<NBHSensor> occupancyExternalSensors,
                                List<NBHSensor> optionalExternalSensors) {
        _mUserRuleNumber = userRuleNumber;
        _mRuleEnabled = ruleEnabled;
        _mDestinationSignalOrComment = destinationSignalOrComment;
        _mSwitchAlignments = switchAlignments;
        _mOccupancyExternalSensors = occupancyExternalSensors;
        _mOptionalExternalSensors = optionalExternalSensors;
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
            NBHSensor sensor = new NBHSensor("TrafficLockingData", "", sensorName, sensorName, false);
            if (sensor != null) {
                _mOccupancyExternalSensors.add(sensor);
            }
        }

        _mOptionalExternalSensors = new ArrayList<>();
    }

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
