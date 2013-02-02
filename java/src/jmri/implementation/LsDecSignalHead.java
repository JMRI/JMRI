// LsDecSignalHead.java

// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under 
// the terms of version 2 of the GNU General Public License as published 
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
// 
// JMRI is distributed in the hope that it will be useful, but WITHOUT 
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
// for more details.

package jmri.implementation;

import org.apache.log4j.Logger;
import jmri.*;

/**
 * Implement SignalHead for Littfinski Daten Technik (LDT) signals.
 *<p>
 * These decoders can display up
 * to 8 aspects. One position of a turnout is associated with one signal aspect.
 * The class assigns turnout positions to all 7 JMRI signal aspects.
 * <P>
 * For more info on the signals, see 
 * <A HREF="http://www.ldt-infocenter.com">http://www.ldt-infocenter.com</a>.
 *
 * @author	Petr Koud'a     Copyright (C) 2007
 */
public class LsDecSignalHead extends DefaultSignalHead {
    
    public LsDecSignalHead(String sys, String user, NamedBeanHandle<Turnout> t1, int s1, NamedBeanHandle<Turnout> t2, int s2, NamedBeanHandle<Turnout> t3, int s3, NamedBeanHandle<Turnout> t4, int s4, NamedBeanHandle<Turnout> t5, int s5, NamedBeanHandle<Turnout> t6, int s6, NamedBeanHandle<Turnout> t7, int s7) {
        super(sys, user);
        mGreen = t1;
        mYellow = t2;
        mRed = t3;
        mFlashGreen = t4;
        mFlashYellow = t5;
        mFlashRed = t6;
        mDark = t7;
        mGreenState = s1;
        mYellowState = s2;
        mRedState = s3;
        mFlashGreenState = s4;
        mFlashYellowState = s5;
        mFlashRedState = s6;
        mDarkState = s7;
    }

    public LsDecSignalHead(String sys, NamedBeanHandle<Turnout> t1, int s1, NamedBeanHandle<Turnout> t2, int s2, NamedBeanHandle<Turnout> t3, int s3, NamedBeanHandle<Turnout> t4, int s4, NamedBeanHandle<Turnout> t5, int s5, NamedBeanHandle<Turnout> t6, int s6, NamedBeanHandle<Turnout> t7, int s7) {
        super(sys);
        mGreen = t1;
        mYellow = t2;
        mRed = t3;
        mFlashGreen = t4;
        mFlashYellow = t5;
        mFlashRed = t6;
        mDark = t7;
        mGreenState = s1;
        mYellowState = s2;
        mRedState = s3;
        mFlashGreenState = s4;
        mFlashYellowState = s5;
        mFlashRedState = s6;
        mDarkState = s7;
    }

    /*
     * Modified from DefaultSignalHead
     * removed software flashing!!!
     */
    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
 
        if (oldAppearance != newAppearance) {
            updateOutput();
		
            // notify listeners, if any
            firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(newAppearance));
        }
    }
    
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", Boolean.valueOf(oldLit), Boolean.valueOf(newLit));
        }    
    }
        
    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SF_SWITCH_FALLTHROUGH")
	protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
	if (mLit == false) {
            mDark.getBean().setCommandedState(mDarkState);
            return;
        } else {
            switch (mAppearance) {
                case RED:
                    mRed.getBean().setCommandedState(mRedState);
                    break;
        	case FLASHRED:
                    mFlashRed.getBean().setCommandedState(mFlashRedState);
                    break;
        	case YELLOW:
                    mYellow.getBean().setCommandedState(mYellowState);
                    break;
        	case FLASHYELLOW:
                    mFlashYellow.getBean().setCommandedState(mFlashYellowState);
                    break;
        	case GREEN:
                    mGreen.getBean().setCommandedState(mGreenState);
                    break;
        	case FLASHGREEN:
                    mFlashGreen.getBean().setCommandedState(mFlashGreenState);
                    break;
        	default:
                    log.warn("Unexpected new appearance: "+mAppearance);
                // go dark by falling through
        	case DARK:
                    mDark.getBean().setCommandedState(mDarkState);
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
        mYellow = null;
        mGreen = null;
        mFlashRed = null;
        mFlashYellow = null;
        mFlashGreen = null;
        mDark = null;
        super.dispose();
    }

    NamedBeanHandle<Turnout> mRed;
    NamedBeanHandle<Turnout> mYellow;
    NamedBeanHandle<Turnout> mGreen;
    NamedBeanHandle<Turnout> mFlashRed;
    NamedBeanHandle<Turnout> mFlashYellow;
    NamedBeanHandle<Turnout> mFlashGreen;
    NamedBeanHandle<Turnout> mDark;

    int mRedState;
    int mYellowState;
    int mGreenState;
    int mFlashRedState;
    int mFlashYellowState;
    int mFlashGreenState;
    int mDarkState;

    public NamedBeanHandle<Turnout> getRed() {return mRed;}
    public NamedBeanHandle<Turnout> getYellow() {return mYellow;}
    public NamedBeanHandle<Turnout> getGreen() {return mGreen;}
    public NamedBeanHandle<Turnout> getFlashRed() {return mFlashRed;}
    public NamedBeanHandle<Turnout> getFlashYellow() {return mFlashYellow;}
    public NamedBeanHandle<Turnout> getFlashGreen() {return mFlashGreen;}
    public NamedBeanHandle<Turnout> getDark() {return mDark;}
    public int getRedState() {return mRedState;}
    public int getYellowState() {return mYellowState;}
    public int getGreenState() {return mGreenState;}
    public int getFlashRedState() {return mFlashRedState;}
    public int getFlashYellowState() {return mFlashYellowState;}
    public int getFlashGreenState() {return mFlashGreenState;}
    public int getDarkState() {return mDarkState;}
    public void setRed(NamedBeanHandle<Turnout> t) {mRed = t;}
    public void setYellow(NamedBeanHandle<Turnout> t) {mYellow = t;}
    public void setGreen(NamedBeanHandle<Turnout> t) {mGreen = t;}
    public void setFlashRed(NamedBeanHandle<Turnout> t) {mFlashRed = t;}
    public void setFlashYellow(NamedBeanHandle<Turnout> t) {mFlashYellow = t;}
    public void setFlashGreen(NamedBeanHandle<Turnout> t) {mFlashGreen = t;}
    public void setDark(NamedBeanHandle<Turnout> t) {mDark = t;}
    public void setRedState(int i) {mRedState = i;}
    public void setYellowState(int i) {mYellowState = i;}
    public void setGreenState(int i) {mGreenState = i;}
    public void setFlashRedState(int i) {mFlashRedState = i;}
    public void setFlashYellowState(int i) {mFlashYellowState = i;}
    public void setFlashGreenState(int i) {mFlashGreenState = i;}
    public void setDarkState(int i) {mDarkState = i;}
    
    static Logger log = Logger.getLogger(LsDecSignalHead.class.getName());
    
}
