// MergSD2SignalHead.java

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

import jmri.Turnout;
import jmri.util.NamedBeanHandle;

/**
 * Implement SignalHead for the MERG Signal Driver 2.
 *<p>
 * The Signal Driver, runs off of the output of a steady State Accessory decoder.
 * and can be configured to run 2, 3 or 4 Aspect signals. With 2 or 3 aspect signals
 * being able to have a feather included.
 * The class assigns turnout positions for RED, YELLOW, GREEN and Double Yellow aspects.
 * THE SD2 does not support flashing double yellow aspects on turnouts, an alternative method
 * is required to do this, as per the MERG SD2 documentation.
 * nb As there is no Double Yellow asigned within JMRI, we use the Flash Yellow instead.
 * <P>
 * For more info on the signals, see 
 * <A HREF="http://www.merg.info">http://www.merg.info</a>.
 *
 * @author	Kevin Dickerson     Copyright (C) 2009
 */
public class MergSD2SignalHead extends DefaultSignalHead {
    
    public MergSD2SignalHead(String sys, String user, int aspect,NamedBeanHandle<Turnout> t1, NamedBeanHandle<Turnout> t2, NamedBeanHandle<Turnout> t3, boolean feather, boolean home){
        super(sys, user);
        mAspects=aspect;
        mInput1=t1;
        if (t2!=null) mInput2=t2;
        if (t3!=null) mInput3=t3;
        mFeather = feather;
        mHome = home;
    }
    
    public MergSD2SignalHead(String sys, int aspect, NamedBeanHandle<Turnout> t1, NamedBeanHandle<Turnout> t2, NamedBeanHandle<Turnout> t3, boolean feather, boolean home){
        super(sys);
        mAspects=aspect;
        mInput1=t1;
        if (t2!=null) mInput2=t2;
        if (t3!=null) mInput3=t3;
        mFeather = feather;
        mHome = home;
    
    }
    
    /*
     * Modified from DefaultSignalHead
     * removed software flashing!!!
     */
    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
        boolean valid=false;
        switch (mAspects){
            case 2: if(mHome){
                        if ((newAppearance == RED) || (newAppearance == GREEN))
                            valid=true;
                    } else { 
                        if ((newAppearance == GREEN) || (newAppearance == YELLOW))
                            valid=true;
                    }
                    break;
            case 3: if ((newAppearance == RED) || (newAppearance == YELLOW) || (newAppearance == GREEN))
                        valid=true;
                    break;
            case 4: if ((newAppearance == RED) || (newAppearance == YELLOW) || (newAppearance == GREEN) || (newAppearance == FLASHYELLOW))
                        valid=true;
                    break;
        }
        if ((oldAppearance != newAppearance) && (valid)){
            updateOutput();
		
            // notify listeners, if any
            firePropertyChange("Appearance", new Integer(oldAppearance), new Integer(newAppearance));
        }
        
    }
    
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", new Boolean(oldLit), new Boolean(newLit));
        }    
    }
        
    @SuppressWarnings("fallthrough")
	protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
	/*if (mLit == false) {
            //mDark.setCommandedState(mDarkState);
            return;
        } else {*/
            switch (mAppearance) {
            case RED:
                    mInput1.getBean().setCommandedState(Turnout.CLOSED);
                    //if(mInput2!=null) mInput2.setCommandedState(Turnout.CLOSED);
                    //if(mInput3!=null) mInput3.setCommandedState(Turnout.CLOSED);
                    break;
        	case YELLOW:
                    if(mHome){                    
                        mInput1.getBean().setCommandedState(Turnout.THROWN);
                        if(mInput2!=null) mInput2.getBean().setCommandedState(Turnout.CLOSED);
                    } else {
                        mInput1.getBean().setCommandedState(Turnout.CLOSED);
                    }
                    break;
        	case FLASHYELLOW:
                    mInput1.getBean().setCommandedState(Turnout.THROWN);
                    mInput2.getBean().setCommandedState(Turnout.THROWN);
                    mInput3.getBean().setCommandedState(Turnout.CLOSED);
                    //mInput1.setCommandedState(
                    //mFlashYellow.setCommandedState(mFlashYellowState);
                    break;
        	case GREEN:
                    mInput1.getBean().setCommandedState(Turnout.THROWN);
                    if(mInput2!=null) mInput2.getBean().setCommandedState(Turnout.THROWN);
                    if(mInput3!=null) mInput3.getBean().setCommandedState(Turnout.THROWN);
                    break;
        	default:
                    mInput1.getBean().setCommandedState(Turnout.CLOSED);
                    
                    log.warn("Unexpected new appearance: "+mAppearance);
                // go dark
            }
        //}
    }
	    
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose() {
        mInput1 = null; 
        mInput2 = null; 
        mInput3 = null;
        super.dispose();
    }

    NamedBeanHandle<Turnout> mInput1; //Section directly infront of the Signal
    NamedBeanHandle<Turnout> mInput2; //Section infront of the next Signal
    NamedBeanHandle<Turnout> mInput3; //Section infront of the second Signal

    int mAspects;
    boolean mFeather = false;
    boolean mHome = true; //Home Signal = true, Distance Signal = false

    public NamedBeanHandle<Turnout> getInput1() {return mInput1;}
    public NamedBeanHandle<Turnout> getInput2() {return mInput2;}
    public NamedBeanHandle<Turnout> getInput3() {return mInput3;}

    public int getAspects() {return mAspects;}
    public boolean getFeather() {return mFeather;}
    public boolean getHome() {return mHome;}
    
    public void setInput1(NamedBeanHandle<Turnout> t) {mInput1 = t;}
    public void setInput2(NamedBeanHandle<Turnout> t) {mInput2 = t;}
    public void setInput3(NamedBeanHandle<Turnout> t) {mInput3 = t;}
    
    public void setAspects(int i) {mAspects = i;}
    public void setFeather(boolean boo) {mFeather = boo;}
    public void setHome(boolean boo) {mHome = boo;}
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MergSD2SignalHead.class.getName());
    
}
