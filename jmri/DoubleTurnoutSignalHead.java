// DoubleTurnoutSignalHead.java

package jmri;

/**
 * Drive a single signal head via two "Turnout" objects.
 * <P>
 * The two Turnout objects are provided during construction,
 * and each drives a specific color (RED and GREEN).
 * Normally, "THROWN" is on, and "CLOSED" is off.
 * YELLOW is provided by turning both on ("THROWN")
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've
 * been changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.3 $
 */
public class DoubleTurnoutSignalHead extends DefaultSignalHead {

    public DoubleTurnoutSignalHead(String sys, String user, Turnout green, Turnout red) {
        super(sys, user);
        mRed = red;
        mGreen = green;
    }

    public DoubleTurnoutSignalHead(String sys, Turnout green, Turnout red) {
        super(sys);
        mRed = red;
        mGreen = green;
    }

	protected void updateOutput() {
	    // assumes that writing a turnout to an existing state is cheap!
		if (mLit == false) {
            mRed.setCommandedState(Turnout.CLOSED);
            mGreen.setCommandedState(Turnout.CLOSED);
			return;
        } else if ( !mFlashOn &&
            ( (mAppearance == FLASHGREEN) ||
            (mAppearance == FLASHYELLOW) ||
            (mAppearance == FLASHRED) ) ) {
                // flash says to make output dark
                mRed.setCommandedState(Turnout.CLOSED);
                mGreen.setCommandedState(Turnout.CLOSED);
			    return;

		} else {
        	switch (mAppearance) {
        		case RED:
        		case FLASHRED:
            		mRed.setCommandedState(Turnout.THROWN);
            		mGreen.setCommandedState(Turnout.CLOSED);
            		break;
        		case YELLOW:
        		case FLASHYELLOW:
            		mRed.setCommandedState(Turnout.THROWN);
            		mGreen.setCommandedState(Turnout.THROWN);
            		break;
        		case GREEN:
        		case FLASHGREEN:
            		mRed.setCommandedState(Turnout.CLOSED);
            		mGreen.setCommandedState(Turnout.THROWN);
            		break;
        		default:
            		log.warn("Unexpected new appearance: "+mAppearance);
            		// go dark
        		case DARK:
            		mRed.setCommandedState(Turnout.CLOSED);
            		mGreen.setCommandedState(Turnout.CLOSED);
            		break;
            }
        }
	}
	
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        mRed = null;
        mGreen = null;
        super.dispose();
    }

    Turnout mRed;
    Turnout mGreen;

    public Turnout getRed() {return mRed;}
    public Turnout getGreen() {return mGreen;}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DoubleTurnoutSignalHead.class.getName());
}

/* @(#)DoubleTurnoutSignalHead.java */
