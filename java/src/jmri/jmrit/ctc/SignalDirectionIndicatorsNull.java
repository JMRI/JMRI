/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

// This object "does nothing" but provide routines that do nothing that the "CodeButtonHandler" object calls.
// This is typically used when there is ONLY a "turnout lock" object and a code button, nothing else.
// I did this as an expedient, since I didn't want to put complex conditional if statements all over CodeButtonHandler
// testing whether "_mSignalDirectionIndicators" was null and dealing with the side effects.

public class SignalDirectionIndicatorsNull  implements SignalDirectionIndicatorsInterface {
    public SignalDirectionIndicatorsNull() {}

    @Override
    public void setCodeButtonHandler(CodeButtonHandler codeButtonHandler) {}
    @Override
    public void removeAllListeners() {}
    @Override
    public boolean isNonfunctionalObject() { return true; }
    @Override
    public void setPresentSignalDirectionLever(int presentSignalDirectionLever) {}
    @Override
    public boolean isRunningTime() { return false; }
    @Override
    public void osSectionBecameOccupied() {}
    @Override
    public void codeButtonPressed(int requestedDirection, boolean requestedChangeInSignalDirection) {}
    @Override
    public void startCodingTime() {}
    @Override
    public boolean signalsNormal() { return true; }
    @Override
    public boolean signalsNormalOrOutOfCorrespondence()  { return true; }
    @Override
    public int getPresentDirection() { return CTCConstants.SIGNALSNORMAL; }
    @Override
    public boolean inCorrespondence() { return true; }
    @Override
    public void forceAllSignalsToHeld() {}
    @Override
    public int getSignalsInTheFieldDirection() { return CTCConstants.OUTOFCORRESPONDENCE; }
    @Override
    public void setSignalDirectionIndicatorsToOUTOFCORRESPONDENCE() {}
    @Override
    public void setRequestedDirection(int direction) {}
}
