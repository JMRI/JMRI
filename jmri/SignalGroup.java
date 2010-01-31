package jmri;

import jmri.util.NamedBeanHandle;

/**
 * Conditional.java
 *
 * A Conditional type to provide runtime support for Densor Groups.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision 1.0 $
 */


public interface SignalGroup extends NamedBean {

    public void addSignalHead(NamedBeanHandle<SignalHead> sh);
    
    public void addSignalHead(String pName);
    
    public void setEnabled(boolean boo);
    
    public boolean getEnabled();
    
    //public jmri.implementation.DefaultSignalGroup.SignalHeadItem getSignalHeadItemByIndex(int x);
    
    //public jmri.implementation.DefaultSignalGroup.SignalHeadItem getSignalHeadItem(int x);
    
    public int getNumSignalHeadItems();
    
    public boolean isSignalIncluded(String systemName);
    
    public int getSignalHeadOnState(String name);
    
    public int getSignalHeadOffState(String name);
    
    public void setSignalHeadOnState(String name, int state);
    
    public void setSignalHeadOffState(String name, int state);
   
    public void setPrimaryTrigger(String pName);
    
    public void setPrimaryInversed(boolean boo);
     
    public String getPrimaryTriggerName();
    public void clearAppearanceTrigger();
    
    public boolean getPrimaryInversed();
    
    public void addTriggerAppearance(String sppearance);

    public int getNumTriggerAppearances();
    
    public String getTriggerAppearanceByIndex(int x);
    
    public String getSignalHeadItemNameByIndex(int n);
    public int getSignalHeadOnStateByIndex(int n);

    public int getSignalHeadOffStateByIndex(int n);
    
    public boolean getSensorTurnoutOperByIndex(int x);
    
    public int getNumSignalHeadTurnoutsByIndex(int x);
    
    public boolean isTriggerAppearanceIncluded(String appearance);
    
    public void deleteTriggerAppearance(String appearance);
    
    public void deleteSignalHead(String pName);
    
    public void deleteSignalHead(NamedBeanHandle<SignalHead> sh);
    
    public void setSignalHeadAlignTurnout(String mHead, String mTurn, int state);
    
    public boolean isTurnoutIncluded(String pSignal, String pTurnout);
    public int getTurnoutState(String pSignal, String pTurnout);
    public int getTurnoutStateByIndex(int x, String pTurnout);
    public int getTurnoutStateByIndex(int x, int pTurnout);
    public String getTurnoutNameByIndex(int x, int pTurnout);
    
    public void setSignalHeadAlignSensor(String mHead, String mTurn, int state);
    public boolean isSensorIncluded(String pSignal, String pTurnout);
    public int getSensorState(String pSignal, String pTurnout);
    public int getSensorStateByIndex(int x, int pSensor);
    public String getSensorNameByIndex(int x, int pSensor);
    public boolean getSensorTurnoutOper(String pSignal);
    
    public int getNumSignalHeadSensorsByIndex(int x);
    
    public void setSensorTurnoutOper(String pSignal, boolean boo);
    
    public void clearSignalTurnout(String pSignal);
    public void clearSignalSensor(String pSignal);
    
    public int getState();
    
    public void setState(int state);
    
    static final int ONACTIVE = 0;    // route fires if sensor goes active
    static final int ONINACTIVE = 1;  // route fires if sensor goes inactive
    static final int VETOACTIVE = 2;  // sensor must be active for route to fire
    static final int VETOINACTIVE = 3;  // sensor must be inactive for route to fire
    
    static final int ONCLOSED = 2;    // route fires if turnout goes closed
    static final int ONTHROWN = 4;  // route fires if turnout goes thrown
    static final int VETOCLOSED = 8;  // turnout must be closed for route to fire
    static final int VETOTHROWN = 16;  // turnout must be thrown for route to fire


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalGroup.class.getName());
}