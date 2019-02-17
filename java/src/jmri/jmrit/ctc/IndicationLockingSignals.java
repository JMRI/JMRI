/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import java.util.ArrayList;
import jmri.SignalHead;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

public class IndicationLockingSignals {
    private final ArrayList<NBHAbstractSignalCommon> _mListOfSignals = new ArrayList<>();
    public IndicationLockingSignals(String userIdentifier, String listOfCSVSignalNames) {
        ArrayList<String> listOfSignalNames = ProjectsCommonSubs.getArrayListFromCSV(listOfCSVSignalNames);
        for (String SignalName : listOfSignalNames) {
            _mListOfSignals.add(NBHAbstractSignalCommon.getExistingSignal("IndicationLockingSignals", userIdentifier, "SignalName " + listOfCSVSignalNames, SignalName));   // NOI18N
        }
    }
    
    public void removeAllListeners() {}   // None done.

    public boolean routeClearedAcross() {
        for (NBHAbstractSignalCommon signal : _mListOfSignals) {
            if (!signal.isDanger()) return true;
        }
        return false;
    }
}
