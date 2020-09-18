package jmri.jmrit.ctc.ctcserialdata;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class TrafficLockingEntry {
    public String _mUserRuleNumber;
    public String _mRuleEnabled;
    public final String _mDestinationSignalOrComment;
    public String _mUserText1;
    public final String _mSwitchAlignment1;
    public String _mUserText2;
    public final String _mSwitchAlignment2;
    public String _mUserText3;
    public final String _mSwitchAlignment3;
    public String _mUserText4;
    public final String _mSwitchAlignment4;
    public String _mUserText5;
    public final String _mSwitchAlignment5;
    public final String _mOccupancyExternalSensor1;
    public final String _mOccupancyExternalSensor2;
    public final String _mOccupancyExternalSensor3;
    public final String _mOccupancyExternalSensor4;
    public final String _mOccupancyExternalSensor5;
    public final String _mOccupancyExternalSensor6;
    public final String _mOccupancyExternalSensor7;
    public final String _mOccupancyExternalSensor8;
    public final String _mOccupancyExternalSensor9;
    public final String _mOptionalExternalSensor1;
    public final String _mOptionalExternalSensor2;
    public String _mUniqueID1;
    public String _mUniqueID2;
    public String _mUniqueID3;
    public String _mUniqueID4;
    public String _mUniqueID5;

    private static final int USER_RULE_NUMBER_INDEX = 0;
    private static final int RULE_ENABLED_INDEX = 1;
    private static final int DESTINATION_SIGNAL_OR_COMMENT_INDEX = 2;
//  Unused index 3 (was terminating O.S. section info)
    private static final int USER_TEXT1_INDEX = 4;
    private static final int SWITCH_ALIGNMENT1_INDEX = 5;
    private static final int USER_TEXT2_INDEX = 6;
    private static final int SWITCH_ALIGNMENT2_INDEX = 7;
    private static final int USER_TEXT3_INDEX = 8;
    private static final int SWITCH_ALIGNMENT3_INDEX = 9;
    private static final int USER_TEXT4_INDEX = 10;
    private static final int SWITCH_ALIGNMENT4_INDEX = 11;
    private static final int USER_TEXT5_INDEX = 12;
    private static final int SWITCH_ALIGNMENT5_INDEX = 13;
    private static final int OCCUPANCY_EXTERNAL_SENSOR1_INDEX  = 14;
    private static final int OCCUPANCY_EXTERNAL_SENSOR2_INDEX  = 15;
    private static final int OCCUPANCY_EXTERNAL_SENSOR3_INDEX  = 16;
    private static final int OCCUPANCY_EXTERNAL_SENSOR4_INDEX  = 17;
    private static final int OCCUPANCY_EXTERNAL_SENSOR5_INDEX  = 18;
    private static final int OCCUPANCY_EXTERNAL_SENSOR6_INDEX  = 19;
    private static final int OCCUPANCY_EXTERNAL_SENSOR7_INDEX  = 20;
    private static final int OCCUPANCY_EXTERNAL_SENSOR8_INDEX  = 21;
    private static final int OCCUPANCY_EXTERNAL_SENSOR9_INDEX  = 22;
    private static final int OPTIONAL_EXTERNAL_SENSOR1_INDEX = 23;
    private static final int OPTIONAL_EXTERNAL_SENSOR2_INDEX = 24;
//  Computer maintained:
//  Unused index 25 (was terminating O.S. section info)
    private static final int UNIQUE_ID1_INDEX = 26;
    private static final int UNIQUE_ID2_INDEX = 27;
    private static final int UNIQUE_ID3_INDEX = 28;
    private static final int UNIQUE_ID4_INDEX = 29;
    private static final int UNIQUE_ID5_INDEX = 30;
    private static final int ARRAY_SIZE = 31;

    public TrafficLockingEntry(String csvString) {
        ArrayList<String> arrayListOfStrings = ProjectsCommonSubs.getFixedArrayListSizeFromCSV(csvString, ARRAY_SIZE);
        _mUserRuleNumber = arrayListOfStrings.get(USER_RULE_NUMBER_INDEX);
        _mRuleEnabled = arrayListOfStrings.get(RULE_ENABLED_INDEX);
        _mDestinationSignalOrComment = arrayListOfStrings.get(DESTINATION_SIGNAL_OR_COMMENT_INDEX);
        _mUserText1 = arrayListOfStrings.get(USER_TEXT1_INDEX);
        _mSwitchAlignment1 = arrayListOfStrings.get(SWITCH_ALIGNMENT1_INDEX);
        _mUserText2 = arrayListOfStrings.get(USER_TEXT2_INDEX);
        _mSwitchAlignment2 = arrayListOfStrings.get(SWITCH_ALIGNMENT2_INDEX);
        _mUserText3 = arrayListOfStrings.get(USER_TEXT3_INDEX);
        _mSwitchAlignment3 = arrayListOfStrings.get(SWITCH_ALIGNMENT3_INDEX);
        _mUserText4 = arrayListOfStrings.get(USER_TEXT4_INDEX);
        _mSwitchAlignment4 = arrayListOfStrings.get(SWITCH_ALIGNMENT4_INDEX);
        _mUserText5 = arrayListOfStrings.get(USER_TEXT5_INDEX);
        _mSwitchAlignment5 = arrayListOfStrings.get(SWITCH_ALIGNMENT5_INDEX);
        _mOccupancyExternalSensor1 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR1_INDEX);
        _mOccupancyExternalSensor2 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR2_INDEX);
        _mOccupancyExternalSensor3 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR3_INDEX);
        _mOccupancyExternalSensor4 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR4_INDEX);
        _mOccupancyExternalSensor5 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR5_INDEX);
        _mOccupancyExternalSensor6 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR6_INDEX);
        _mOccupancyExternalSensor7 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR7_INDEX);
        _mOccupancyExternalSensor8 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR8_INDEX);
        _mOccupancyExternalSensor9 = arrayListOfStrings.get(OCCUPANCY_EXTERNAL_SENSOR9_INDEX);
        _mOptionalExternalSensor1 = arrayListOfStrings.get(OPTIONAL_EXTERNAL_SENSOR1_INDEX);
        _mOptionalExternalSensor2 = arrayListOfStrings.get(OPTIONAL_EXTERNAL_SENSOR2_INDEX);
        _mUniqueID1 = arrayListOfStrings.get(UNIQUE_ID1_INDEX);
        _mUniqueID2 = arrayListOfStrings.get(UNIQUE_ID2_INDEX);
        _mUniqueID3 = arrayListOfStrings.get(UNIQUE_ID3_INDEX);
        _mUniqueID4 = arrayListOfStrings.get(UNIQUE_ID4_INDEX);
        _mUniqueID5 = arrayListOfStrings.get(UNIQUE_ID5_INDEX);
    }
}
