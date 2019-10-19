/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.SignalMastLogicManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

public class IndicationLockingSignals {
    private final ArrayList<NBHAbstractSignalCommon> _mListOfSignals = new ArrayList<>();
    private final Turnout turnout;
    private final OtherData.SIGNAL_SYSTEM_TYPE signalType;

    public IndicationLockingSignals(String userIdentifier, String listOfCSVSignalNames, String turnoutName,
            OtherData.SIGNAL_SYSTEM_TYPE signalSystemType) {
        turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName);
        signalType = signalSystemType;

        ArrayList<String> listOfSignalNames = ProjectsCommonSubs.getArrayListFromCSV(listOfCSVSignalNames);
        for (String SignalName : listOfSignalNames) {
            _mListOfSignals.add(NBHAbstractSignalCommon.getExistingSignal("IndicationLockingSignals", userIdentifier, "SignalName " + listOfCSVSignalNames, SignalName));   // NOI18N
        }
    }

    public void removeAllListeners() {}   // None done.

    public boolean routeClearedAcross() {
        if (signalType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALHEAD) {
            return checkSignalHeads();
        } else if (signalType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALMAST) {
            return checkSignalMasts();
        }
        return false;
    }

    public boolean checkSignalHeads() {
        for (NBHAbstractSignalCommon signal : _mListOfSignals) {
            if (!signal.isDanger()) return true;
        }
        return false;
    }

    public boolean checkSignalMasts() {
        for (NBHAbstractSignalCommon signal : _mListOfSignals) {
            if (checkMast((SignalMast) signal.getBean(), turnout)) return true;
        }
        return false;
    }

    public boolean checkMast(SignalMast mast, Turnout turnout) {
        if (mast.getHeld()) return false;

        // Get the SML for the mast and check for an active destination
        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalMastLogic(mast);
        // It is possible if the user put in (for example) a "bumper signal" (end of track signal)
        // either accidentally or purposefully for there to be no logic at all.
        if (sml == null) return false;
        SignalMast activeDest = sml.getActiveDestination();
        if (activeDest == null) return false;

        // Check the auto turnouts for a match
        for (Turnout autoTurnout : sml.getAutoTurnouts(activeDest)) {
            if (autoTurnout == turnout) return true;
        }

        // Check the manual turnouts for a match
        for (Turnout manualTurnout : sml.getTurnouts(activeDest)) {
            if (manualTurnout == turnout) return true;
        }

        return false;
    }
}
