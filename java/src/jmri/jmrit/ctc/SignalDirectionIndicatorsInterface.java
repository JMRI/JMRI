/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

public interface SignalDirectionIndicatorsInterface {
    public void setCodeButtonHandler(CodeButtonHandler codeButtonHandler);
    public void removeAllListeners();
    public boolean isNonfunctionalObject();
    public void setPresentSignalDirectionLever(int presentSignalDirectionLever);
    public boolean isRunningTime();
    public void osSectionBecameOccupied();
    public void codeButtonPressed(int requestedDirection, boolean requestedChangeInSignalDirection);
    public void startCodingTime();
    public boolean signalsNormal();
    public boolean signalsNormalOrOutOfCorrespondence();
    public int getPresentDirection();
    public boolean inCorrespondence();
    public void forceAllSignalsToHeld();
    public int getSignalsInTheFieldDirection();
    public void setSignalDirectionIndicatorsToOUTOFCORRESPONDENCE();
    public void setRequestedDirection(int direction);
}
