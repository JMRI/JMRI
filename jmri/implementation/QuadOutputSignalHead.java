// QuadOutputSignalHead.java

package jmri.implementation;

import jmri.*;

/**
 * Drive a single signal head via four "Turnout" objects.
 * <P>
 * After much confusion, the user-level terminology 
 * was changed to call these "Triple Output"; the class
 * name remains the same to reduce recoding.
 * <P>
 * The four Turnout objects are provided during construction,
 * and each drives a specific color (RED, YELLOW, GREEN, and LUNAR).
 * Normally, "THROWN" is on, and "CLOSED" is off.
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've
 * been changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version	$Revision: 1.3 $
 */
public class QuadOutputSignalHead extends TripleTurnoutSignalHead {

    public QuadOutputSignalHead(String sys, String user, Turnout green, Turnout yellow, Turnout red, Turnout lunar) {
        super(sys, user, green, yellow, red);
        mLunar = lunar;
    }

    public QuadOutputSignalHead(String sys, Turnout green, Turnout yellow, Turnout red, Turnout lunar) {
        super(sys, green, yellow, red);
        mLunar = lunar;
    }
	
	@SuppressWarnings("fallthrough")
	protected void updateOutput() {
	    if (mLit == false) {
	        super.updateOutput();
        } else if ( !mFlashOn &&
            ( (mAppearance == FLASHGREEN) ||
            (mAppearance == FLASHYELLOW) ||
            (mAppearance == FLASHRED) ||
            (mAppearance == FLASHLUNAR) ) ) {
                // flash says to make output dark
                mRed.setCommandedState(Turnout.CLOSED);
                mYellow.setCommandedState(Turnout.CLOSED);
                mGreen.setCommandedState(Turnout.CLOSED);
                mLunar.setCommandedState(Turnout.CLOSED);
			    return;

		} else {
        	switch (mAppearance) {
        		case LUNAR:
        		case FLASHLUNAR:
            		mLunar.setCommandedState(Turnout.THROWN);
                    mRed.setCommandedState(Turnout.CLOSED);
                    mYellow.setCommandedState(Turnout.CLOSED);
                    mGreen.setCommandedState(Turnout.CLOSED);
            		break;
        		default:
        		    // let parent handle rest of cases
            		mLunar.setCommandedState(Turnout.CLOSED);
            		super.updateOutput();
            		break;
            }
        }
	}
	
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        mLunar = null;
        super.dispose();
    }

    Turnout mLunar;

    public Turnout getLunar() {return mLunar;}
	public void setLunar(Turnout t) {mLunar=t;}

    final static private java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

    // claim support for Lunar aspects
    
    final static private int[] validStates = new int[]{
        DARK, 
        RED, 
        LUNAR,
        YELLOW,
        GREEN,
        FLASHRED, 
        FLASHLUNAR,
        FLASHYELLOW,
        FLASHGREEN
    };
    final static private String[] validStateNames = new String[]{
        rb.getString("SignalHeadStateDark"),
        rb.getString("SignalHeadStateRed"),
        rb.getString("SignalHeadStateLunar"),
        rb.getString("SignalHeadStateYellow"),
        rb.getString("SignalHeadStateGreen"),
        rb.getString("SignalHeadStateFlashingRed"),
        rb.getString("SignalHeadStateFlashingLunar"),
        rb.getString("SignalHeadStateFlashingYellow"),
        rb.getString("SignalHeadStateFlashingGreen")
    };
    
    public int[] getValidStates() {
        return validStates;
    }
    public String[] getValidStateNames() {
        return validStateNames;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(QuadOutputSignalHead.class.getName());
}

/* @(#)QuadOutputSignalHead.java */
