package jmri.jmrit.ctc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import jmri.Sensor;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

public class TrafficLocking {
    private final static Logger log = LoggerFactory.getLogger(TrafficLocking.class);

    private static class TrafficLockingRecord {
        private final SwitchIndicatorsRoute _mSwitchIndicatorsRoute;
        private final NBHSensor _mOccupancyExternalSensor1;
        private final NBHSensor _mOccupancyExternalSensor2;
        private final NBHSensor _mOccupancyExternalSensor3;
        private final NBHSensor _mOccupancyExternalSensor4;
        private final NBHSensor _mOccupancyExternalSensor5;
        private final NBHSensor _mOccupancyExternalSensor6;
        private final NBHSensor _mOccupancyExternalSensor7;
        private final NBHSensor _mOccupancyExternalSensor8;
        private final NBHSensor _mOccupancyExternalSensor9;
        private final NBHSensor _mOptionalSensor1;
        private final NBHSensor _mOptionalSensor2;
        private final boolean _mRuleEnabled;

        public TrafficLockingRecord(String userIdentifier,
                                    String parameter,
                                    NBHSensor switchIndicator1,
                                    NBHSensor switchIndicator2,
                                    NBHSensor switchIndicator3,
                                    NBHSensor switchIndicator4,
                                    NBHSensor switchIndicator5,
                                    NBHSensor occupancyExternalSensor1,
                                    NBHSensor occupancyExternalSensor2,
                                    NBHSensor occupancyExternalSensor3,
                                    NBHSensor occupancyExternalSensor4,
                                    NBHSensor occupancyExternalSensor5,
                                    NBHSensor occupancyExternalSensor6,
                                    NBHSensor occupancyExternalSensor7,
                                    NBHSensor occupancyExternalSensor8,
                                    NBHSensor occupancyExternalSensor9,
                                    NBHSensor optionalSensor1,
                                    NBHSensor optionalSensor2,
                                    String ruleEnabled) {
            _mSwitchIndicatorsRoute = new SwitchIndicatorsRoute(switchIndicator1, switchIndicator2, switchIndicator3, switchIndicator4, switchIndicator5, null);
            _mOccupancyExternalSensor1 = occupancyExternalSensor1;
            _mOccupancyExternalSensor2 = occupancyExternalSensor2;
            _mOccupancyExternalSensor3 = occupancyExternalSensor3;
            _mOccupancyExternalSensor4 = occupancyExternalSensor4;
            _mOccupancyExternalSensor5 = occupancyExternalSensor5;
            _mOccupancyExternalSensor6 = occupancyExternalSensor6;
            _mOccupancyExternalSensor7 = occupancyExternalSensor7;
            _mOccupancyExternalSensor8 = occupancyExternalSensor8;
            _mOccupancyExternalSensor9 = occupancyExternalSensor9;
            _mOptionalSensor1 = optionalSensor1;
            _mOptionalSensor2 = optionalSensor2;
            _mRuleEnabled = !ruleEnabled.equals(Bundle.getMessage("TLE_RuleDisabled")); // NOI18N  Any problem, default is ENABLED!
        }

        public boolean isEnabled() { return _mRuleEnabled; }
        public boolean isValid(boolean fleetingEnabled) {
            if (!_mRuleEnabled) return false;    // If disabled, treat as invalid so we skip this rule and try the next rule.

            if (!_mSwitchIndicatorsRoute.isRouteSelected()
            || !isOptionalSensorActive(_mOptionalSensor1)
            || !isOptionalSensorActive(_mOptionalSensor2)) return false;
            return true;
        }

//  Put all non null and valid OCCUPANCY NBHSensor's in a HashSet and return it (the "ROUTE"!) for use by LockedRoutesManager.
        public HashSet<Sensor> getOccupancySensors() {
            HashSet<Sensor> returnValue = new HashSet<>();
            if (_mOccupancyExternalSensor1 != null && _mOccupancyExternalSensor1.valid()) returnValue.add(_mOccupancyExternalSensor1.getBean());
            if (_mOccupancyExternalSensor2 != null && _mOccupancyExternalSensor2.valid()) returnValue.add(_mOccupancyExternalSensor2.getBean());
            if (_mOccupancyExternalSensor3 != null && _mOccupancyExternalSensor3.valid()) returnValue.add(_mOccupancyExternalSensor3.getBean());
            if (_mOccupancyExternalSensor4 != null && _mOccupancyExternalSensor4.valid()) returnValue.add(_mOccupancyExternalSensor4.getBean());
            if (_mOccupancyExternalSensor5 != null && _mOccupancyExternalSensor5.valid()) returnValue.add(_mOccupancyExternalSensor5.getBean());
            if (_mOccupancyExternalSensor6 != null && _mOccupancyExternalSensor6.valid()) returnValue.add(_mOccupancyExternalSensor6.getBean());
            if (_mOccupancyExternalSensor7 != null && _mOccupancyExternalSensor7.valid()) returnValue.add(_mOccupancyExternalSensor7.getBean());
            if (_mOccupancyExternalSensor8 != null && _mOccupancyExternalSensor8.valid()) returnValue.add(_mOccupancyExternalSensor8.getBean());
            if (_mOccupancyExternalSensor9 != null && _mOccupancyExternalSensor9.valid()) returnValue.add(_mOccupancyExternalSensor9.getBean());
            returnValue.remove(null);   // Safety: Remove null entry if it exists (There will ONLY be one in a set!)
            return returnValue;
        }

//  Quick and Dirty Routine: If it doesn't exist, it's lit.  If it exists, ACTIVE = lit.  Can't use CTCMain.getSensorKnownState() because of this.
        private boolean isOptionalSensorActive(NBHSensor sensor) {
            if (sensor.valid()) return sensor.getKnownState() == Sensor.ACTIVE;
            return true;    // Doesn't exist.
        }

    }

    private final ArrayList<TrafficLockingRecord> _mLeftTrafficLockingRulesArrayList = new ArrayList<>();
    private final ArrayList<TrafficLockingRecord> _mRightTrafficLockingRulesArrayList = new ArrayList<>();
    private final String _mUserIdentifier;
    private final ArrayList<TrafficLockingData> _mLeftTrafficLockingRulesList;
    private final ArrayList<TrafficLockingData> _mRightTrafficLockingRulesList;
    private final LockedRoutesManager _mLockedRoutesManager;

    public TrafficLocking(String userIdentifier, ArrayList<TrafficLockingData> _mTRL_LeftTrafficLockingRules, ArrayList<TrafficLockingData> _mTRL_RightTrafficLockingRules, LockedRoutesManager lockedRoutesManager)
    {
        _mUserIdentifier = userIdentifier;                                      // Squirrel it
        _mLeftTrafficLockingRulesList = _mTRL_LeftTrafficLockingRules;      // away for later
        _mRightTrafficLockingRulesList = _mTRL_RightTrafficLockingRules;    // "fileReadComplete"
        _mLockedRoutesManager = lockedRoutesManager;
    }

    public void removeAllListeners() {}   // None done.

//  Since the user may specify "forward referenced" O/S sections (i.e. an entry references an O.S. section that hasn't been read in and created yet),
//  we delay processing of everything until after the file has been completely read in.  Here we do the real work:
    public void fileReadComplete(HashMap<Integer, CodeButtonHandler> cbHashMap, HashMap<Integer, SwitchDirectionIndicators> swdiHashMap) {
        addAllTrafficLockingEntries(_mUserIdentifier, _mLeftTrafficLockingRulesList, "leftTrafficLockingRulesList", cbHashMap, swdiHashMap, _mLeftTrafficLockingRulesArrayList);     // NOI18N
        addAllTrafficLockingEntries(_mUserIdentifier, _mRightTrafficLockingRulesList, "rightTrafficLockingRulesList", cbHashMap, swdiHashMap, _mRightTrafficLockingRulesArrayList);  // NOI18N
    }

    private void addAllTrafficLockingEntries(   String                                                  userIdentifier,
                                                ArrayList<TrafficLockingData>                           trafficLockingRulesList,
                                                String                                                  parameter,
                                                HashMap<Integer, CodeButtonHandler>                     cbHashMap,
                                                HashMap<Integer, SwitchDirectionIndicators>             swdiHashMap,
                                                ArrayList<TrafficLockingRecord>                         trafficLockingRecordsArrayList) {  // <- Output
        trafficLockingRulesList.forEach(row -> {
            // Convert TrafficLockingData into a set of fixed size ArrayLists
            ArrayList<NBHSensor> occupancySensors = row.getOccupancySensors();
            ArrayList<NBHSensor> optionalSensors = row.getOptionalSensors();
            ArrayList<Integer> ids = row.getUniqueIDs();
            ArrayList<String> alignments = row.getAlignments();

            int osSection1UniqueID = ids.get(0);
            int osSection2UniqueID = ids.get(1);
            int osSection3UniqueID = ids.get(2);
            int osSection4UniqueID = ids.get(3);
            int osSection5UniqueID = ids.get(4);

            TrafficLockingRecord trafficLockingRecord
                = new TrafficLockingRecord( userIdentifier,
                                            parameter,
                                            getSwitchDirectionIndicatorSensor(osSection1UniqueID, alignments.get(0), swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection2UniqueID, alignments.get(1), swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection3UniqueID, alignments.get(2), swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection4UniqueID, alignments.get(3), swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection5UniqueID, alignments.get(4), swdiHashMap),
                                            occupancySensors.get(0),
                                            occupancySensors.get(1),
                                            occupancySensors.get(2),
                                            occupancySensors.get(3),
                                            occupancySensors.get(4),
                                            occupancySensors.get(5),
                                            occupancySensors.get(6),
                                            occupancySensors.get(7),
                                            occupancySensors.get(8),
                                            optionalSensors.get(0),
                                            optionalSensors.get(1),
                                            row._mRuleEnabled);
            if (!trafficLockingRecord.getOccupancySensors().isEmpty()) {
                trafficLockingRecordsArrayList.add(trafficLockingRecord);
            }
        });
    }

    public TrafficLockingInfo valid(int presentSignalDirectionLever, boolean fleetingEnabled) {
        if (presentSignalDirectionLever == CTCConstants.LEFTTRAFFIC) return validForTraffic(_mLeftTrafficLockingRulesArrayList, false, fleetingEnabled);
        return validForTraffic(_mRightTrafficLockingRulesArrayList, true, fleetingEnabled);
    }

    private TrafficLockingInfo validForTraffic(ArrayList<TrafficLockingRecord> trafficLockingRecordArrayList, boolean rightTraffic, boolean fleetingEnabled) {
        TrafficLockingInfo returnValue = new TrafficLockingInfo(true);          // ASSUME valid return status
        if (trafficLockingRecordArrayList.isEmpty()) return returnValue; // No rules, OK all of the time.
//  If ALL are disabled, then treat as if nothing in there, always allow, otherwise NONE would be valid!
        boolean anyEnabled = false;
        for (int index = 0; index < trafficLockingRecordArrayList.size(); index++) {
            if (trafficLockingRecordArrayList.get(index).isEnabled()) { anyEnabled = true; break; }
        }
        if (!anyEnabled) return returnValue; // None enabled, always allow.

        for (int index = 0; index < trafficLockingRecordArrayList.size(); index++) {
            TrafficLockingRecord trafficLockingRecord = trafficLockingRecordArrayList.get(index);
            if (trafficLockingRecord.isValid(fleetingEnabled)) {
//  Ah, we found a rule that matches the route.  See if that route
//  is in conflict with any other routes presently in effect:
                String ruleNumber = Integer.toString(index+1);
                returnValue._mLockedRoute = _mLockedRoutesManager.checkRouteAndAllocateIfAvailable(trafficLockingRecord.getOccupancySensors(), _mUserIdentifier, "Rule #" + ruleNumber, rightTraffic);
                if (returnValue._mLockedRoute != null) { // OK:
                    if (jmri.InstanceManager.getDefault(CTCMain.class)._mCTCDebug_TrafficLockingRuleTriggeredDisplayLoggingEnabled) log.info("Rule {} valid", ruleNumber);
                    return returnValue;
                }
            }
        }
        returnValue._mReturnStatus = false;
        return returnValue;
    }

    private NBHSensor getSwitchDirectionIndicatorSensor(int uniqueID, String switchAlignment, HashMap<Integer, SwitchDirectionIndicators> swdiHashMap) {
        if (uniqueID < 0) return null;
        boolean isNormalAlignment = !switchAlignment.equals(Bundle.getMessage("TLE_Reverse"));  // NOI18N
        SwitchDirectionIndicators switchDirectionIndicators = swdiHashMap.get(uniqueID);
        if (switchDirectionIndicators == null) return null;     // Safety, technically shouldn't happen....
        return switchDirectionIndicators.getProperIndicatorSensor(isNormalAlignment);
    }
}
