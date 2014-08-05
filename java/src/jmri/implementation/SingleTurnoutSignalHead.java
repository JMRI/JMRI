// SingleTurnoutSignalHead.java

package jmri.implementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.NamedBeanHandle;

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
 * Based Upon DoubleTurnoutSignalHead by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 */
public class SingleTurnoutSignalHead extends DefaultSignalHead {

    /**
     * @param on Appearance constant from {@link jmri.SignalHead} for the output on (Turnout thrown) appearance
     * @param off Appearance constant from {@link jmri.SignalHead} for the signal off (Turnout closed) appearance
     */
    public SingleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys, user);
        mOutput = lit;
        mOnAppearance = on;
        mOffAppearance = off;
        setAppearance(off);
    }

    /**
     * @param on Appearance constant from {@link jmri.SignalHead} for the output on (Turnout thrown) appearance
     * @param off Appearance constant from {@link jmri.SignalHead} for the signal off (Turnout closed) appearance
     */
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
            (mAppearance == mOnAppearance*2) ) {
                mOutput.getBean().setCommandedState(Turnout.CLOSED);
			    return;
		} else if ( !mFlashOn && 
            (mAppearance == mOffAppearance*2)  ) {
                mOutput.getBean().setCommandedState(Turnout.THROWN);
			    return;
		}else {
            if ((mAppearance==mOffAppearance) || (mAppearance==(mOffAppearance*2))) {
                mOutput.getBean().setCommandedState(Turnout.CLOSED);
            } else if ((mAppearance==mOnAppearance) || (mAppearance==(mOnAppearance*2))){
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
    public void setOnAppearance(int on) { 
        int old = on;
        mOnAppearance = on;
        firePropertyChange("ValidStatesChanged", old, on);
    }
    public void setOffAppearance(int off) {
        int old = off;
        mOffAppearance = off;
        firePropertyChange("ValidStatesChanged", old, off);
    }
    
    public NamedBeanHandle<Turnout> getOutput() {return mOutput;}

    public void setOutput(NamedBeanHandle<Turnout> t) {mOutput=t;}
    
    public int[] getValidStates() {
        int [] validStates;
        if(mOnAppearance == mOffAppearance){
            validStates = new int[2];
            validStates[0]=mOnAppearance;
            validStates[1]=mOffAppearance;
            return validStates;
        }
        else if (mOnAppearance == DARK || mOffAppearance == DARK){
            validStates = new int[3];
        }
        else {
            validStates = new int [2];
        }
        int x = 0;
        validStates[x] = mOnAppearance;
        x++;
        if (mOffAppearance == DARK){
            validStates[x] = (mOnAppearance * 2);  // makes flashing
            x++;
        }
        validStates[x] = mOffAppearance;
        x++;
        if (mOnAppearance == DARK){
            validStates[x] = (mOffAppearance * 2);  // makes flashing
        }
        return validStates;
    }
    
    public String[] getValidStateNames() {
        String [] validStateName;
        if(mOnAppearance == mOffAppearance){
            validStateName = new String[2];
            validStateName[0]=getSignalColour(mOnAppearance);
            validStateName[1]=getSignalColour(mOffAppearance);
            return validStateName;
        }
        if (mOnAppearance == DARK || mOffAppearance == DARK){
            validStateName = new String[3];
        }
        else {
            validStateName = new String[2];
        }
        int x = 0;
        validStateName[x] = getSignalColour(mOnAppearance);
        x++;
        if (mOffAppearance == DARK){
            validStateName[x] = getSignalColour((mOnAppearance * 2));  // makes flashing
            x++;
        }
        validStateName[x] = getSignalColour(mOffAppearance);
        x++;
        if (mOnAppearance == DARK){
            validStateName[x] = getSignalColour((mOffAppearance * 2));  // makes flashing
        }
        return validStateName;
    }

    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SF_SWITCH_FALLTHROUGH")
    private String getSignalColour(int mAppearance){
        switch(mAppearance){
            case SignalHead.RED:
                    return  Bundle.getMessage("SignalHeadStateRed");
        	case SignalHead.FLASHRED:
                    return Bundle.getMessage("SignalHeadStateFlashingRed");
        	case SignalHead.YELLOW:
                    return Bundle.getMessage("SignalHeadStateYellow");
        	case SignalHead.FLASHYELLOW:
                    return Bundle.getMessage("SignalHeadStateFlashingYellow");
        	case SignalHead.GREEN:
                    return Bundle.getMessage("SignalHeadStateGreen");
        	case SignalHead.FLASHGREEN:
                    return Bundle.getMessage("SignalHeadStateFlashingGreen");
            case SignalHead.LUNAR:
                    return Bundle.getMessage("SignalHeadStateLunar");
            case SignalHead.FLASHLUNAR:
                    return Bundle.getMessage("SignalHeadStateFlashingLunar");
        	default:
                    log.warn("Unexpected appearance: "+mAppearance);
                // go dark by falling through
        	case SignalHead.DARK:
                    return  Bundle.getMessage("SignalHeadStateDark");
        }
    }
    
    boolean isTurnoutUsed(Turnout t){
        if(getOutput()!=null && t.equals(getOutput().getBean()))
            return true;
        return false;
    }


    static Logger log = LoggerFactory.getLogger(SingleTurnoutSignalHead.class.getName());
}

/* @(#)SingleTurnoutSignalHead.java */
