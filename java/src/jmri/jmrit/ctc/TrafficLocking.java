package jmri.jmrit.ctc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import jmri.Sensor;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingEntry;
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
                                    String occupancyExternalSensor1,
                                    String occupancyExternalSensor2,
                                    String occupancyExternalSensor3,
                                    String occupancyExternalSensor4,
                                    String occupancyExternalSensor5,
                                    String occupancyExternalSensor6,
                                    String occupancyExternalSensor7,
                                    String occupancyExternalSensor8,
                                    String occupancyExternalSensor9,
                                    String optionalSensor1,
                                    String optionalSensor2,
                                    String ruleEnabled) {
            _mSwitchIndicatorsRoute = new SwitchIndicatorsRoute(switchIndicator1, switchIndicator2, switchIndicator3, switchIndicator4, switchIndicator5, null);
            _mOccupancyExternalSensor1 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor1", occupancyExternalSensor1, true);  // NOI18N
            _mOccupancyExternalSensor2 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor2", occupancyExternalSensor2, true);  // NOI18N
            _mOccupancyExternalSensor3 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor3", occupancyExternalSensor3, true);  // NOI18N
            _mOccupancyExternalSensor4 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor4", occupancyExternalSensor4, true);  // NOI18N
            _mOccupancyExternalSensor5 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor5", occupancyExternalSensor5, true);  // NOI18N
            _mOccupancyExternalSensor6 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor6", occupancyExternalSensor6, true);  // NOI18N
            _mOccupancyExternalSensor7 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor7", occupancyExternalSensor7, true);  // NOI18N
            _mOccupancyExternalSensor8 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor8", occupancyExternalSensor8, true);  // NOI18N
            _mOccupancyExternalSensor9 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " occupancyExternalSensor9", occupancyExternalSensor9, true);  // NOI18N
            _mOptionalSensor1 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " optionalSensor1", optionalSensor1, true); // NOI18N
            _mOptionalSensor2 = new NBHSensor("TrafficLocking", userIdentifier, parameter + " optionalSensor2", optionalSensor2, true); // NOI18N
            _mRuleEnabled = !ruleEnabled.equals(Bundle.getMessage("TLE_RuleDisabled")); // NOI18N  Any problem, default is ENABLED!
        }

        public boolean isEnabled() { return _mRuleEnabled; }
        public boolean isValid() {
            if (!_mRuleEnabled) return false;    // If disabled, treat as invalid so we skip this rule and try the next rule.
//  For all of these, ONLY unoccupied(INACTIVE) is "valid".  ACTIVE, INCONSISTENT and UNKNOWN all are considered occupied (ACTIVE):
            if (_mOccupancyExternalSensor1.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor2.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor3.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor4.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor5.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor6.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor7.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor8.getKnownState() != Sensor.INACTIVE) return false;
            if (_mOccupancyExternalSensor9.getKnownState() != Sensor.INACTIVE) return false;
            if (!_mSwitchIndicatorsRoute.isRouteSelected()) return false;
            if (!isOptionalSensorActive(_mOptionalSensor1)) return false;
            if (!isOptionalSensorActive(_mOptionalSensor2)) return false;
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
    private final String _mLeftTrafficLockingRulesSSVList;
    private final String _mRightTrafficLockingRulesSSVList;
    private final LockedRoutesManager _mLockedRoutesManager;

    public TrafficLocking(String userIdentifier, String leftTrafficLockingRulesSSVList, String rightTrafficLockingRulesSSVList, LockedRoutesManager lockedRoutesManager)
    {
        _mUserIdentifier = userIdentifier;                                      // Squirrel it
        _mLeftTrafficLockingRulesSSVList = leftTrafficLockingRulesSSVList;      // away for later
        _mRightTrafficLockingRulesSSVList = rightTrafficLockingRulesSSVList;    // "fileReadComplete"
        _mLockedRoutesManager = lockedRoutesManager;
    }

    public void removeAllListeners() {}   // None done.

//  Since the user may specify "forward referenced" O/S sections (i.e. an entry references an O.S. section that hasn't been read in and created yet),
//  we delay processing of everything until after the file has been completely read in.  Here we do the real work:
    public void fileReadComplete(HashMap<Integer, CodeButtonHandler> cbHashMap, HashMap<Integer, SwitchDirectionIndicators> swdiHashMap) {
        addAllTrafficLockingEntries(_mUserIdentifier, _mLeftTrafficLockingRulesSSVList, "leftTrafficLockingRulesSSVList", cbHashMap, swdiHashMap, _mLeftTrafficLockingRulesArrayList);     // NOI18N
        addAllTrafficLockingEntries(_mUserIdentifier, _mRightTrafficLockingRulesSSVList, "rightTrafficLockingRulesSSVList", cbHashMap, swdiHashMap, _mRightTrafficLockingRulesArrayList);  // NOI18N
    }

    private void addAllTrafficLockingEntries(   String                                                  userIdentifier,
                                                String                                                  trafficLockingRulesSSVList,
                                                String                                                  parameter,
                                                HashMap<Integer, CodeButtonHandler>                     cbHashMap,
                                                HashMap<Integer, SwitchDirectionIndicators>             swdiHashMap,
                                                ArrayList<TrafficLockingRecord>                         trafficLockingRecordsArrayList) {  // <- Output
        ArrayList<String> arrayListOfSSVEntries;
        arrayListOfSSVEntries = ProjectsCommonSubs.getArrayListFromSSV(trafficLockingRulesSSVList);
        for (String csvEntry : arrayListOfSSVEntries) {
            TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(csvEntry);
            int osSection1UniqueID = getUniqueID(trafficLockingEntry._mUniqueID1);
            int osSection2UniqueID = getUniqueID(trafficLockingEntry._mUniqueID2);
            int osSection3UniqueID = getUniqueID(trafficLockingEntry._mUniqueID3);
            int osSection4UniqueID = getUniqueID(trafficLockingEntry._mUniqueID4);
            int osSection5UniqueID = getUniqueID(trafficLockingEntry._mUniqueID5);

            TrafficLockingRecord trafficLockingRecord
                = new TrafficLockingRecord( userIdentifier,
                                            parameter,
                                            getSwitchDirectionIndicatorSensor(osSection1UniqueID, trafficLockingEntry._mSwitchAlignment1, swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection2UniqueID, trafficLockingEntry._mSwitchAlignment2, swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection3UniqueID, trafficLockingEntry._mSwitchAlignment3, swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection4UniqueID, trafficLockingEntry._mSwitchAlignment4, swdiHashMap),
                                            getSwitchDirectionIndicatorSensor(osSection5UniqueID, trafficLockingEntry._mSwitchAlignment5, swdiHashMap),
                                            trafficLockingEntry._mOccupancyExternalSensor1,
                                            trafficLockingEntry._mOccupancyExternalSensor2,
                                            trafficLockingEntry._mOccupancyExternalSensor3,
                                            trafficLockingEntry._mOccupancyExternalSensor4,
                                            trafficLockingEntry._mOccupancyExternalSensor5,
                                            trafficLockingEntry._mOccupancyExternalSensor6,
                                            trafficLockingEntry._mOccupancyExternalSensor7,
                                            trafficLockingEntry._mOccupancyExternalSensor8,
                                            trafficLockingEntry._mOccupancyExternalSensor9,
                                            trafficLockingEntry._mOptionalExternalSensor1,
                                            trafficLockingEntry._mOptionalExternalSensor2,
                                            trafficLockingEntry._mRuleEnabled);
            if (!trafficLockingRecord.getOccupancySensors().isEmpty()) {
                trafficLockingRecordsArrayList.add(trafficLockingRecord);
            }
        }
    }

    public TrafficLockingInfo valid(int presentSignalDirectionLever) {
        if (presentSignalDirectionLever == CTCConstants.LEFTTRAFFIC) return validForTraffic(_mLeftTrafficLockingRulesArrayList);
        return validForTraffic(_mRightTrafficLockingRulesArrayList);
    }

    private TrafficLockingInfo validForTraffic(ArrayList<TrafficLockingRecord> trafficLockingRecordArrayList) {
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
            if (trafficLockingRecord.isValid()) {
//  Ah, we found a rule that matches the route, and is not occupied.  See if that route
//  is in conflict with any other routes presently in effect:
                String ruleNumber = Integer.toString(index+1);
                returnValue._mLockedRoute = _mLockedRoutesManager.checkRouteAndAllocateIfAvailable(trafficLockingRecord.getOccupancySensors(), _mUserIdentifier, "Rule #" + ruleNumber);
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

    private int getUniqueID(String aString) {
        return ProjectsCommonSubs.getIntFromStringNoThrow(aString, -1); // If a problem, -1!
    }

/*
    private NBHSensor getOSSectionOccupancySensor(int uniqueID, HashMap<Integer, CodeButtonHandler> cbHashMap) {
        if (uniqueID < 0) return null;
        CodeButtonHandler codeButtonHandler = cbHashMap.get(uniqueID);
        if (codeButtonHandler == null) return null;     // Safety, technically shouldn't happen....
        return codeButtonHandler.getOSSectionOccupiedExternalSensor();
    }
*/
}
