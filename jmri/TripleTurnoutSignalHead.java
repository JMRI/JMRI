// TripleTurnoutSignalHead.java

package jmri;

/**
 * Drive a single signal head via three "Turnout" objects.
 * <P>
 * The three Turnout objects are provided during construction,
 * and each drives a specific color (RED, YELLOW and GREEN).
 * Normally, "THROWN" is on, and "CLOSED" is off.
 * <P>
 * This class doesn't currently do flashing aspects.
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've
 * been changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public class TripleTurnoutSignalHead extends AbstractSignalHead {

    public TripleTurnoutSignalHead(Turnout green, Turnout yellow, Turnout red) {
        super();
        mRed = red;
        mYellow = yellow;
        mGreen = green;
    }

    public void setAppearance(int newAppearance) {}

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        mRed = null;
        mYellow = null;
        mGreen = null;
        super.dispose();
    }

    Turnout mRed;
    Turnout mYellow;
    Turnout mGreen;

}


/* @(#)TripleTurnoutSignalHead.java */
