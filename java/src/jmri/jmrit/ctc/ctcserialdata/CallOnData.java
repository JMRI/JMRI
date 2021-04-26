package jmri.jmrit.ctc.ctcserialdata;

import java.util.ArrayList;
import java.util.Arrays;
import jmri.*;
import jmri.jmrit.ctc.*;

/**
 * This describes a single line of Call On data.  The list of call on rules
 * for each OS section are in the _mCO_GroupingsList variable in {@link CodeButtonHandlerData}.
 *
 * During panel loading, the switch indicator names are saved as strings.  Once all of the
 * data is loaded, the NBHSensors are copied from the related CodeButtonHandlerData.
 * @author Dave Sand Copyright (C) 2020
 */
public class CallOnData {
    public NBHSignal _mExternalSignal;
    public String _mSignalFacingDirection;
    public String _mSignalAspectToDisplay;
    public NBHSensor _mCalledOnExternalSensor;
    public NamedBeanHandle<Block> _mExternalBlock;
    public ArrayList<NBHSensor> _mSwitchIndicators;      // Up to 6 entries
    public ArrayList<String> _mSwitchIndicatorNames;     // Temporary names during XML loading

    public CallOnData() {
    }

    @Override
    public String toString() {
        String formattedString = String.format("%s,%s,%s,%s,%s",
                _mExternalSignal != null ? _mExternalSignal.getHandleName() : "",
                _mSignalFacingDirection != null ? _mSignalFacingDirection : "",
                _mSignalAspectToDisplay != null ? _mSignalAspectToDisplay : "",
                _mCalledOnExternalSensor != null ? _mCalledOnExternalSensor.getHandleName() : "",
                _mExternalBlock != null ? _mExternalBlock.getName() : "");
        StringBuilder buildString = new StringBuilder(formattedString);
        _mSwitchIndicators.forEach(sw -> {
            buildString.append(",");
            buildString.append(sw.getHandleName());
        });
        return buildString.toString();
    }
}
