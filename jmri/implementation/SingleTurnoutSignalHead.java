// SingleTurnoutSignalHead.java

package jmri.implementation;
import jmri.*;
import jmri.util.NamedBeanHandle;

/**
 * Drive a single signal head via one "Turnout" objects.
 * <P>
 * After much confusion, the user-level terminology 
 * was changed to call these "Single Output"; the class
 * name remains the same to reduce recoding.
 * <P>
 * One Turnout object is provided during construction,
 * and drives the aspect to be either ON or OFF.
 * Normally, "THROWN" is on, and "CLOSED" is off.
 * The facility to set the aspect via any of the four
 * aspect colors is provided, however they all do the same.
 * 
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've
 * been changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @version	$Revision: 1.2 $
 */
public class SingleTurnoutSignalHead extends DefaultSignalHead {

    public SingleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys, user);
        mOutput = lit;
        mOnAppearance = on;
        mOffAppearance = off;
        setAppearance(off);
    }

    public SingleTurnoutSignalHead(String sys, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys);
        mOutput = lit;
        mOnAppearance = on;
        mOffAppearance = off;
        setAppearance(off);
    }
    
    int mOnAppearance = DARK;
    int mOffAppearance = LUNAR;

	protected void updateOutput() {
	    // assumes that writing a turnout to an existing state is cheap!
		if (mLit == false) {
            mOutput.getBean().setCommandedState(Turnout.CLOSED);
			return;
        } else if ( !mFlashOn &&
            ( (mAppearance == FLASHGREEN) ||
            (mAppearance == FLASHYELLOW) ||
            (mAppearance == FLASHRED) ) ) {
                // flash says to make output dark
                mOutput.getBean().setCommandedState(Turnout.CLOSED);
			    return;

		} else {
            if (mAppearance==mOffAppearance) {
                mOutput.getBean().setCommandedState(Turnout.CLOSED);
            } else if (mAppearance==mOnAppearance){
                mOutput.getBean().setCommandedState(Turnout.THROWN);
            } else {
                log.warn("Unexpected new appearance: "+mAppearance);
            }
        }
	}
    
	
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        mOutput = null;
        super.dispose();
    }

    NamedBeanHandle<Turnout> mOutput;
    
    public int getOnAppearance() {return mOnAppearance;}
    public int getOffAppearance() {return mOffAppearance;}
    public void setOnAppearance(int on) {mOnAppearance = on;}
    public void setOffAppearance(int off) {mOffAppearance = off;}
    
    public NamedBeanHandle<Turnout> getOutput() {return mOutput;}

    public void setOutput(NamedBeanHandle<Turnout> t) {mOutput=t;}
    
    public int[] getValidStates() {
        int [] validStates = new int[] { mOnAppearance, mOffAppearance};
        return validStates;
    }
    
    public String[] getValidStateNames() {
        String[] validStateNames = new String[] { getSignalColour(mOnAppearance), getSignalColour(mOffAppearance)};
        return validStateNames;
    }
    final static private java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");
    @SuppressWarnings("fallthrough")
    private String getSignalColour(int mAppearance){
        switch(mAppearance){
            case SignalHead.RED:
                    return  rb.getString("SignalHeadStateRed");
        	case SignalHead.FLASHRED:
                    return rb.getString("SignalHeadStateFlashingRed");
        	case SignalHead.YELLOW:
                    return rb.getString("SignalHeadStateYellow");
        	case SignalHead.FLASHYELLOW:
                    return rb.getString("SignalHeadStateFlashingYellow");
        	case SignalHead.GREEN:
                    return rb.getString("SignalHeadStateGreen");
        	case SignalHead.FLASHGREEN:
                    return rb.getString("SignalHeadStateFlashingGreen");
            case SignalHead.LUNAR:
                    return rb.getString("SignalHeadStateLunar");
            case SignalHead.FLASHLUNAR:
                    return rb.getString("SignalHeadStateFlashingLunar");
        	default:
                    log.warn("Unexpected appearance: "+mAppearance);
                // go dark
        	case SignalHead.DARK:
                    return  rb.getString("SignalHeadStateDark");
        }
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleTurnoutSignalHead.class.getName());
}

/* @(#)SingleTurnoutSignalHead.java */
