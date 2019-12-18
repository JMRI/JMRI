/*
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import java.util.ArrayList;
import java.util.HashSet;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.jmrit.ctc.ctcserialdata.CallOnEntry;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/*
This module supports Call On functionality.

SignalHeads: Clears the "held" bit and sets the signal to whatever the user
specified.   CalledOnSensor can be "" if you want the Dispatcher to call on to
an unoccupied block, or the real sensor for the block being called on to.

SignalMasts: Clears the "held" bit, and set the permissive value in the called
on block.  In addition, see the block comment above where this is done regarding
the bug in JMRI we fix.  When the O.S. section becomes occupied, we clear the
permissive value in the called on block.

Both:
If the occupancy sensor is specified and that sensor is INACTIVE (indicating
nothing in block), then the call on is IGNORED.
Resets the "callOnToggleSensor" to INACTIVE, thereby turning off the
Call On request by the dispatcher.

Issues with a computer vs. a real CTC machine:

If you have a call on button AND you set it to momentary and you have a
computer monitor (vs. a physical dispatchers panel):
It is IMPOSSIBLE to press both the Call on button AND the code button at the
same time.  You could make the call on button NON momentary, but from my experiment
(please perform you own), the physical indication of the button pressed is not
good, and if you press it again, it toggles off which is not what you want.
I suggest using a toggle switch.  This object when configured for using it
will "reset" the toggle to off once the code button has been pressed
thus providing a better indication that it occurred.  In the future, if you
substitute a push button and forget to set the "momentary" attribute, this
routine will still reset it for the next use.

Rules from http://www.ctcparts.com/about.htm

"An important note though for programming logic is that the interlocking limits
must be clear and all power switches within the interlocking limits aligned
appropriately for the back to train route for this feature to activate."

I was either told or read somewhere that the block being called on to MUST be
occupied for the call on to work, otherwise the CODE BUTTON PRESS is IGNORE!

By the way, there is NO way to do flashing any color with a semaphore!
You should probably use "YELLOW" in that case!

*/
public class CallOn {
    private static class GroupingData {
        public final NBHAbstractSignalCommon _mSignal;    // Signal
        public final int _mSignalHeadFaces;      // Which way above faces.
        public final int _mCallOnAspect;         // What it should be set to if CallOn sucessful.
        public final NBHSensor _mCalledOnExternalSensor;
        public final NamedBeanHandle<Block> _mNamedBeanHandleBlock;
        public final SwitchIndicatorsRoute _mRoute;
//  NOTE: When calling this constructor, ALL values MUST BE VALID!  NO check is done!
        public GroupingData(NBHAbstractSignalCommon signal, String signalHeadFaces, int callOnAspect, NBHSensor calledOnExternalSensor, NamedBeanHandle<Block> namedBeanHandleBlock, SwitchIndicatorsRoute route) {
            _mSignal = signal;
            _mSignalHeadFaces = signalHeadFaces.equals(Bundle.getMessage("InfoDlgCOLeftTraffic")) ? CTCConstants.LEFTTRAFFIC : CTCConstants.RIGHTTRAFFIC;   // NOI18N
            _mCallOnAspect = callOnAspect;
            _mCalledOnExternalSensor = calledOnExternalSensor;
            _mNamedBeanHandleBlock = namedBeanHandleBlock;
            _mRoute = route;
        }
    }

    private final LockedRoutesManager _mLockedRoutesManager;
    private final boolean _mSignalHeadSelected;
    private final NBHSensor _mCallOnToggleSensor;
    private final ArrayList<GroupingData> _mGroupingDataArrayList = new ArrayList<>();

    public CallOn(LockedRoutesManager lockedRoutesManager, String userIdentifier, String callOnToggleSensor, String groupingsListString, OtherData.SIGNAL_SYSTEM_TYPE signalSystemType) {
        _mLockedRoutesManager = lockedRoutesManager;
        _mSignalHeadSelected = (signalSystemType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALHEAD);
        _mCallOnToggleSensor = new NBHSensor("CallOn", userIdentifier, "callOnToggleSensor", callOnToggleSensor, false);    // NOI18N
        if (!ProjectsCommonSubs.isNullOrEmptyString(groupingsListString)) {
            ArrayList<String> groupingList = ProjectsCommonSubs.getArrayListFromSSV(groupingsListString);
            for (String groupingString : groupingList) {
                CallOnEntry callOnEntry = new CallOnEntry(groupingString);
                try {
                    NBHAbstractSignalCommon signal = NBHAbstractSignalCommon.getExistingSignal("CallOn", userIdentifier, "groupingString" + " " + groupingString, callOnEntry._mExternalSignal);    // NOI18N
                    String trafficDirection = callOnEntry._mSignalFacingDirection;
                    if (!trafficDirection.equals(Bundle.getMessage("InfoDlgCOLeftTraffic")) && !trafficDirection.equals(Bundle.getMessage("InfoDlgCORightTraffic"))) {  // NOI18N
                        throw new CTCException("CallOn", userIdentifier, "groupingString", groupingString + " not " + Bundle.getMessage("InfoDlgCOLeftTraffic") + " or " + Bundle.getMessage("InfoDlgCORightTraffic") + ".");   // NOI18N
                    }
                    SwitchIndicatorsRoute route = new SwitchIndicatorsRoute("CallOn", userIdentifier, "groupingString", // NOI18N
                                                                            callOnEntry._mSwitchIndicator1,
                                                                            callOnEntry._mSwitchIndicator2,
                                                                            callOnEntry._mSwitchIndicator3,
                                                                            callOnEntry._mSwitchIndicator4,
                                                                            callOnEntry._mSwitchIndicator5,
                                                                            callOnEntry._mSwitchIndicator6);
                    if (_mSignalHeadSelected) {
//  Technically, I'd have liked to call this only once, but in reality, each signalhead could have a different value list:
                        String[] validStateNames = signal.getValidStateNames(); // TODO consider using getValidStateKeys() to skip localisation issue
                        int validStateNamesIndex = arrayFind(validStateNames, convertFromForeignLanguageColor(callOnEntry._mSignalAspectToDisplay));
                        // TODO use non-localized validStateNKeys instead of localized validStateNames
                        if (validStateNamesIndex == -1) { // Not found:
                            throw new CTCException("CallOn", userIdentifier, "groupingString", groupingString + " " + Bundle.getMessage("CallOnNotValidAspect"));   // NOI18N
                        }
                        NBHSensor calledOnExternalSensor = new NBHSensor("CallOn", userIdentifier, "groupingString", callOnEntry._mCalledOnExternalSensor, false);
                        int[] correspondingValidStates = signal.getValidStates();   // I ASSUME it's a correlated 1 for 1 with "getValidStateNames", via tests it seems to be.
                        _mGroupingDataArrayList.add(new GroupingData(signal, trafficDirection, correspondingValidStates[validStateNamesIndex], calledOnExternalSensor, null, route));
                    } else {
                        String externalBlockName = callOnEntry._mExternalBlock;
                        if (ProjectsCommonSubs.isNullOrEmptyString(externalBlockName)) {
                            throw new CTCException("CallOn", userIdentifier, "groupingString", groupingString + " " + Bundle.getMessage("CallOnSignalMastBlockError")); // NOI18N
                        }
                        Block externalBlock = InstanceManager.getDefault(BlockManager.class).getBlock(externalBlockName);
                        if (externalBlock == null) {
                            throw new CTCException("CallOn", userIdentifier, "groupingString", groupingString + " " + Bundle.getMessage("CallOnSignalMastBlockError2"));    // NOI18N
                        }
                        NamedBeanHandle<Block> namedBeanHandleExternalBlock = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(externalBlockName, externalBlock);
                        _mGroupingDataArrayList.add(new GroupingData(signal, trafficDirection, 0, null, namedBeanHandleExternalBlock, route));
                    }
                } catch (CTCException e) { e.logError(); return; }
            }
        }
        resetToggle();
    }

    public void removeAllListeners() {}   // None done.

    public void resetToggle() {
        _mCallOnToggleSensor.setKnownState(Sensor.INACTIVE);
    }

/*  Call On requested.  CodeButtonHandler has determined from it's "limited"
    viewpoint that it is OK to attempt the call on.  This routine determines
    if it is fully valid to allow it at this time.

NOTE:
    We "fake out" the caller: we return "true" AS IF we actually did the
    call on.  Why?  So higher level code in CodeButtonHandler DOES NOT attempt
    to try normal signal direction lever handling!  When the dispatcher requests
    call on, normal signal direction lever handling should be bypassed.  If we
    didn't, then when the dispatcher requested call on to an unoccupied block,
    the signals would go to yellow/green instead of staying at signals normal
    (all stop) which would be the result of the call on only, which is what we
    want.  After all, that's what the dispatcher asked for explicitly!
*/
    public TrafficLockingInfo codeButtonPressed(HashSet<Sensor> sensors,
                                                String userIdentifier,
                                                SignalDirectionIndicatorsInterface signalDirectionIndicatorsObject,
                                                int signalDirectionLever) {
        if (_mCallOnToggleSensor.getKnownState() == Sensor.INACTIVE) return new TrafficLockingInfo(false);    // Dispatcher didn't want it at this time!
        if (signalDirectionLever != CTCConstants.LEFTTRAFFIC && signalDirectionLever != CTCConstants.RIGHTTRAFFIC) return new TrafficLockingInfo(false); // Doesn't make sense, don't do anything

        GroupingData foundGroupingData = null;

        int ruleNumber = 0;
        for (GroupingData groupingData : _mGroupingDataArrayList) {
            ruleNumber++;
            if (groupingData._mSignalHeadFaces == signalDirectionLever) {
                if (groupingData._mRoute.isRouteSelected()) {
                    foundGroupingData = groupingData;
                    break;
                }
            }
        }
//  From NOW ON, the "returnValue" status will be true:
        TrafficLockingInfo returnValue = new TrafficLockingInfo(true);
        if (foundGroupingData == null) return returnValue;    // Has to be active, pretend we did it, but we didn't!

        if (_mSignalHeadSelected) {
            if (Sensor.ACTIVE != foundGroupingData._mCalledOnExternalSensor.getKnownState()) return returnValue;    // Has to be active EXACTLY, pretend we did it, but we didn't!
            if (foundGroupingData._mCalledOnExternalSensor.valid()) {
                sensors.add(foundGroupingData._mCalledOnExternalSensor.getBean());
            }
//  Check to see if the route specified is free:
//  The route is the O.S. section that called us, along with the called on occupancy sensor:
            returnValue._mLockedRoute = _mLockedRoutesManager.checkRouteAndAllocateIfAvailable(sensors, userIdentifier, "Rule #" + ruleNumber);
            if (returnValue._mLockedRoute == null) return returnValue;         // Not available, fake out

            foundGroupingData._mSignal.setHeld(false);    // Original order in .py code
            foundGroupingData._mSignal.setAppearance(foundGroupingData._mCallOnAspect);
        } else {
//  We get this EVERY time (we don't cache this in "foundGroupingData._mCalledOnExternalSensor") because this property of Block
//  can be changed DYNAMICALLY at runtime (I believe) via the Block Editor:
            NBHSensor sensor = new NBHSensor(foundGroupingData._mNamedBeanHandleBlock.getBean().getNamedSensor());
            if (Sensor.ACTIVE != sensor.getKnownState()) return returnValue;       // Has to be active EXACTLY, pretend we did it, but we didn't!
            if (sensor.valid()) {
                sensors.add(sensor.getBean());
            }

//  Check to see if the route specified is free:
//  The route is the O.S. section that called us, along with the called on occupancy sensor:
            returnValue._mLockedRoute = _mLockedRoutesManager.checkRouteAndAllocateIfAvailable(sensors, userIdentifier, "Rule #" + ruleNumber);
            if (returnValue._mLockedRoute == null) return returnValue;         // Not available, fake out
            foundGroupingData._mSignal.setHeld(false);
        }

        signalDirectionIndicatorsObject.setRequestedDirection(signalDirectionLever);
// These two statements MUST be last thing in this order:
        signalDirectionIndicatorsObject.setSignalDirectionIndicatorsToOUTOFCORRESPONDENCE();
        signalDirectionIndicatorsObject.startCodingTime();
        return returnValue;
    }

    static private int arrayFind(String[] array, String aString) {
        for (int index = 0; index < array.length; index++) {
            if (aString.equals(array[index])) return index;
        }
        return -1;
    }

//  When we went to foreign language support, I had to convert to English here, so that these lines worked above:
//  String[] validStateNames = signal.getValidStateNames(); // use getValidStateKeys instead?
//  int validStateNamesIndex = arrayFind(validStateNames, convertFromForeignLanguageColor(callOnEntry._mSignalAspectToDisplay));
//
//  I SUSPECT (not verified) that "signal.getValidStateNames()" ALWAYS returns English no matter what language is selected.
//  If I AM WRONG, then this routine can be removed, and the call to it removed:
    private String convertFromForeignLanguageColor(String foreignLanguageColor) {
        String color = "Red"; // should NEVER be used directly, but if programmers screw up, default to some "sane" value.
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateDark"))) color = "Dark";     // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateRed"))) color = "Red";       // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateYellow"))) color = "Yellow"; // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateGreen"))) color = "Green";   // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateFlashingRed"))) color = "Flashing Red";       // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateFlashingYellow"))) color = "Flashing Yellow"; // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateFlashingGreen"))) color = "Flashing Green";   // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateLunar"))) color = "Lunar";                    // NOI18N
        if (foreignLanguageColor.equals(Bundle.getMessage("SignalHeadStateFlashingLunar"))) color = "Flashing Lunar";   // NOI18N
        return color;
    }

}
