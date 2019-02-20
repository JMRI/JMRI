package jmri.jmrit.ctc.ctcserialdata;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * This describes a single line of Call On Data from the GUI screen.
 */
public class CallOnEntry {
    public final String _mExternalSignal;
    public final String _mSignalFacingDirection;
    public final String _mSignalAspectToDisplay;
    public final String _mCalledOnExternalSensor;
    public final String _mExternalBlock;
    public final String _mSwitchIndicator1;
    public final String _mSwitchIndicator2;
    public final String _mSwitchIndicator3;
    public final String _mSwitchIndicator4;
    public final String _mSwitchIndicator5;
    public final String _mSwitchIndicator6;
    
    private static final int EXTERNAL_SIGNAL_INDEX = 0;
    private static final int SIGNAL_FACING_DIRECTION_INDEX = 1;
    private static final int SIGNAL_ASPECT_TO_DISPLAY_INDEX = 2;
    private static final int CALLED_ON_EXTERNAL_SENSOR_INDEX = 3;
    private static final int EXTERNAL_BLOCK_INDEX = 4;
    private static final int SWITCHINDICATOR1_INDEX = 5;
    private static final int SWITCHINDICATOR2_INDEX = 6;
    private static final int SWITCHINDICATOR3_INDEX = 7;
    private static final int SWITCHINDICATOR4_INDEX = 8;
    private static final int SWITCHINDICATOR5_INDEX = 9;
    private static final int SWITCHINDICATOR6_INDEX = 10;
    private static final int ARRAY_SIZE = 11;

    public CallOnEntry(String csvString) {
        ArrayList<String> arrayListOfStrings = ProjectsCommonSubs.getFixedArrayListSizeFromCSV(csvString, ARRAY_SIZE);
        _mExternalSignal = arrayListOfStrings.get(EXTERNAL_SIGNAL_INDEX);
        _mSignalFacingDirection = arrayListOfStrings.get(SIGNAL_FACING_DIRECTION_INDEX);
        _mSignalAspectToDisplay = arrayListOfStrings.get(SIGNAL_ASPECT_TO_DISPLAY_INDEX);
        _mCalledOnExternalSensor = arrayListOfStrings.get(CALLED_ON_EXTERNAL_SENSOR_INDEX);
        _mExternalBlock = arrayListOfStrings.get(EXTERNAL_BLOCK_INDEX);
        _mSwitchIndicator1 = arrayListOfStrings.get(SWITCHINDICATOR1_INDEX);
        _mSwitchIndicator2 = arrayListOfStrings.get(SWITCHINDICATOR2_INDEX);
        _mSwitchIndicator3 = arrayListOfStrings.get(SWITCHINDICATOR3_INDEX);
        _mSwitchIndicator4 = arrayListOfStrings.get(SWITCHINDICATOR4_INDEX);
        _mSwitchIndicator5 = arrayListOfStrings.get(SWITCHINDICATOR5_INDEX);
        _mSwitchIndicator6 = arrayListOfStrings.get(SWITCHINDICATOR6_INDEX);
    }
    
    public CallOnEntry( String externalSignal,
                        String signalFacingDirection,
                        String signalAspectToDisplay,
                        String calledOnExternalSensor,
                        String externalBlock,
                        String switchIndicator1,
                        String switchIndicator2,
                        String switchIndicator3,
                        String switchIndicator4,
                        String switchIndicator5,
                        String switchIndicator6) {
        _mExternalSignal = externalSignal;
        _mSignalFacingDirection = signalFacingDirection;
        _mSignalAspectToDisplay = signalAspectToDisplay;
        _mCalledOnExternalSensor = calledOnExternalSensor;
        _mExternalBlock = externalBlock;
        _mSwitchIndicator1 = switchIndicator1;
        _mSwitchIndicator2 = switchIndicator2;
        _mSwitchIndicator3 = switchIndicator3;
        _mSwitchIndicator4 = switchIndicator4;
        _mSwitchIndicator5 = switchIndicator5;
        _mSwitchIndicator6 = switchIndicator6;
    }
    
    public String toCSVString() {
        ArrayList<String> newValueArrayList = new ArrayList<>(Arrays.asList(new String[ARRAY_SIZE]));
        newValueArrayList.set(EXTERNAL_SIGNAL_INDEX, _mExternalSignal);
        newValueArrayList.set(SIGNAL_FACING_DIRECTION_INDEX, _mSignalFacingDirection);
        newValueArrayList.set(SIGNAL_ASPECT_TO_DISPLAY_INDEX, _mSignalAspectToDisplay);
        newValueArrayList.set(CALLED_ON_EXTERNAL_SENSOR_INDEX, _mCalledOnExternalSensor);
        newValueArrayList.set(EXTERNAL_BLOCK_INDEX, _mExternalBlock);
        newValueArrayList.set(SWITCHINDICATOR1_INDEX, _mSwitchIndicator1);
        newValueArrayList.set(SWITCHINDICATOR2_INDEX, _mSwitchIndicator2);
        newValueArrayList.set(SWITCHINDICATOR3_INDEX, _mSwitchIndicator3);
        newValueArrayList.set(SWITCHINDICATOR4_INDEX, _mSwitchIndicator4);
        newValueArrayList.set(SWITCHINDICATOR5_INDEX, _mSwitchIndicator5);
        newValueArrayList.set(SWITCHINDICATOR6_INDEX, _mSwitchIndicator6);
        return ProjectsCommonSubs.constructCSVStringFromArrayList(newValueArrayList);
    }
}
