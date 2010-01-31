package jmri.implementation;

import jmri.*;
import jmri.util.NamedBeanHandle;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;


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


public class DefaultSignalGroup extends AbstractNamedBean implements jmri.SignalGroup{

    public DefaultSignalGroup(String systemName, String userName) {
        super(systemName, userName);
    }
    
    public DefaultSignalGroup(String systemName) {
        super(systemName, null);
    }
    
    ArrayList <String> _triggerAppearances = new ArrayList<String>();
    
    private NamedBeanHandle<SignalMast> primaryTrigger;
    private boolean primaryInversed=false;
      
    private boolean headactive=false;
    
    private boolean enabled=true;
    
    public void setEnabled(boolean boo){
        enabled = boo;
    }
    
    public boolean getEnabled(){
        return enabled;
    }
    
    //Will need to add procedure to remove the old listener if it exists first.
    public void setPrimaryTrigger(String pName){

        SignalMast mMast = InstanceManager.signalMastManagerInstance().getBySystemName(pName);
        if (mMast == null) mMast = InstanceManager.signalMastManagerInstance().getByUserName(pName);
        if (mMast == null) log.warn("did not find a SignalHead named "+pName);
        else {
            primaryTrigger = new NamedBeanHandle<SignalMast>(pName, mMast);
            //addTriggerAppearance(state);
            //primaryInversed = inverse;
        }
        getPrimary().addPropertyChangeListener(mSignalMastListener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                System.out.println(e.getPropertyName());
                if (e.getPropertyName().equals("Aspect")){
                    String now = ((String) e.getNewValue());
                    System.out.println("Signal head state change" + now);
                    if (isTriggerAppearanceIncluded(now)){
                        System.out.println("Signal head in correct state");
                        setHead();
                    } else {
                        resetHeads();
                    }
                }
            }
        });
    }
    
    public void addTriggerAppearance(String appearance){
        if(isTriggerAppearanceIncluded(appearance)){
            return;
        }
        _triggerAppearances.add(appearance);
    }
    
    public boolean isTriggerAppearanceIncluded(String appearance){
        for (int i=0; i<_triggerAppearances.size(); i++) {
            if ( _triggerAppearances.get(i).equals(appearance) ) {
                // Found Appearance
                return true;
            }
        }
        return false;
    }
    
    public void deleteTriggerAppearance(String appearance){
        _triggerAppearances.remove(appearance);
    }
    
    public int getNumTriggerAppearances() {
        return _triggerAppearances.size();
    }
    
    public String getTriggerAppearanceByIndex(int x){
        return _triggerAppearances.get(x);
    }
    
    public void clearAppearanceTrigger(){
        _triggerAppearances = new ArrayList<String>();
    }
    
    public SignalMast getPrimary(){
        return primaryTrigger.getBean();
    }
    
    public String getPrimaryTriggerName(){
        return primaryTrigger.getName();
    }
    
    public void setPrimaryInversed(boolean boo){
        primaryInversed = boo;
    }
       
    public boolean getPrimaryInversed(){
        return primaryInversed;
    }
    
    /*
        Add a new signalhead to the group
    */
    public void addSignalHead(NamedBeanHandle<SignalHead> sh){
        SignalHeadItem signalitem = new SignalHeadItem(sh);
        _signalHeadItem.add(signalitem);
    }
    /*
        Add a new signalhead to the group
    */
    public void addSignalHead(String pName){
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {
            addSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
        }
    }
    
    protected PropertyChangeListener mSignalMastListener = null;
    
    public void setSignalHeadAlignTurnout(String mHead, String mTurn, int state){
        SignalHeadItem sh = getSignalHeadItem(mHead);
        sh.addTurnout(mTurn, state);
    }
    
    public void setSignalHeadAlignSensor(String mHead, String mSen, int state){
        SignalHeadItem sh = getSignalHeadItem(mHead);
        sh.addSensor(mSen, state);
    }
    

    /*
    Returns the 'n' signalheaditem
    */
    public SignalHeadItem getSignalHeadItemByIndex(int n)
    {
        return _signalHeadItem.get(n);
    }
    /*
        Returns the 'n' signalheaditem
    */
    public SignalHeadItem getSignalHeadItem(int n)
    {
        return getSignalHeadItemByIndex(n);
    }
    
    public String getSignalHeadItemNameByIndex(int n)
    {
        return getSignalHeadItemByIndex(n).getName();
    }
    /*
        Returns the number of signalheads in this group
    */
    
    
    public int getNumSignalHeadItems() {
        return _signalHeadItem.size();
    }
    
    public int getSignalHeadOnState(String name){
        return getSignalHeadItem(name).getOnAppearance();
    }
    
    public int getSignalHeadOffState(String name){
        return getSignalHeadItem(name).getOffAppearance();
    }
    
    public int getSignalHeadOnStateByIndex(int n){
        return getSignalHeadItemByIndex(n).getOnAppearance();
    }
    
    public int getSignalHeadOffStateByIndex(int n){
        return getSignalHeadItemByIndex(n).getOffAppearance();
    }
    
    public void deleteSignalHead(String pName){
        /*SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {*/
            //deleteSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
            _signalHeadItem.remove(getSignalHeadItem(pName));
        //}
    }
    
    public void deleteSignalHead(NamedBeanHandle<SignalHead> sh){
        _signalHeadItem.remove(sh);
    }
    
    public void setSignalHeadOnState(String name, int state){
        getSignalHeadItem(name).setOnAppearance(state);
    }
    
    public void setSignalHeadOffState(String name, int state){
        getSignalHeadItem(name).setOffAppearance(state);
    }
    
    public boolean isSignalIncluded(String pName) {
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        //Signal s1 = InstanceManager.signalHeadManagerInstance().getSignal(systemName);
        for (int i=0; i<_signalHeadItem.size(); i++) {
            if ( _signalHeadItem.get(i).getSignal() == mHead ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }
    /*
        Returns a signalhead item
    */
    private SignalHeadItem getSignalHeadItem(String name) {
        for (int i=0; i<_signalHeadItem.size(); i++) {
            if ( _signalHeadItem.get(i).getName().equals(name) ) {
                // Found turnout
                return _signalHeadItem.get(i);
            }
        }
        return null;
	}
    
    public boolean isTurnoutIncluded(String pSignal, String pTurnout){
        return getSignalHeadItem(pSignal).isTurnoutIncluded(pTurnout);
    }
    
    public int getTurnoutState(String pSignal, String pTurnout){
        return getSignalHeadItem(pSignal).getTurnoutState(pTurnout);
    }
    
    public int getTurnoutStateByIndex(int x, String pTurnout){
        return getSignalHeadItemByIndex(x).getTurnoutState(pTurnout);
    }
   
    public int getTurnoutStateByIndex(int x, int pTurnout){
        return getSignalHeadItemByIndex(x).getTurnoutState(pTurnout);
    }
    public String getTurnoutNameByIndex(int x, int pTurnout){
        return getSignalHeadItemByIndex(x).getTurnoutName(pTurnout);
    }
    
    public int getSensorStateByIndex(int x, int pSensor){
        return getSignalHeadItemByIndex(x).getSensorState(pSensor);
    }
    
    public String getSensorNameByIndex(int x, int pSensor){
        return getSignalHeadItemByIndex(x).getSensorName(pSensor);
    }

    public boolean isSensorIncluded(String pSignal, String pSensor){
        return getSignalHeadItem(pSignal).isSensorIncluded(pSensor);
    }
    
    public int getSensorState(String pSignal, String pSensor){
        return getSignalHeadItem(pSignal).getSensorState(pSensor);
    }
    
    public boolean getSensorTurnoutOper(String pSignal){
        return getSignalHeadItem(pSignal).getSensorTurnoutOper();
    }
    
    public boolean getSensorTurnoutOperByIndex(int x){
        return getSignalHeadItemByIndex(x).getSensorTurnoutOper();
    }
    
    public void setSensorTurnoutOper(String pSignal, boolean boo){
        getSignalHeadItem(pSignal).setSensorTurnoutOper(boo);
    }
    
    public void clearSignalTurnout(String pSignal){
        getSignalHeadItem(pSignal).clearSignalTurnouts();
    }
    public void clearSignalSensor(String pSignal){
        getSignalHeadItem(pSignal).clearSignalSensors();
    }
    
    private void resetHeads(){
        if (!headactive)
            return;
        for (int i=0; i<_signalHeadItem.size(); i++) {
            _signalHeadItem.get(i).getSignal().setAppearance(_signalHeadItem.get(i).getOffAppearance());
        }
        headactive=false;
    }
    
    private void setHead(){
        boolean active = false;
        for (int i=0; i<_signalHeadItem.size(); i++) {
            if ( _signalHeadItem.get(i).checkActive() ) {
                System.out.println("setting active");
                if (active)
                    System.out.println("two indicators could be lit at once");
                active = true;
                headactive = true;
            }
        }
    }
    
    public int getNumSignalHeadSensorsByIndex(int x){
        return getSignalHeadItemByIndex(x).getNumSensors();
    }
    
    public int getNumSignalHeadTurnoutsByIndex(int x){
        return getSignalHeadItemByIndex(x).getNumTurnouts();
    }
    ArrayList <SignalHeadItem> _signalHeadItem = new ArrayList<SignalHeadItem>();
    private class SignalHeadItem {
        SignalHeadItem(NamedBeanHandle<SignalHead> sh){
            namedHead = sh;
        }
        
        private NamedBeanHandle<SignalHead> namedHead;
        
        public String getName(){
            return namedHead.getName();
        }
        
        public SignalHead getSignal(){
            return namedHead.getBean();
        }
        
        private int onAppearance = 0x00;
        private int offAppearance = 0x00;
        
        public void setOnAppearance(int app){
            onAppearance = app;
        }
        
        public int getOnAppearance(){
            return onAppearance;
        }
        
        public void setOffAppearance(int app){
            offAppearance = app;
        }
        
        public int getOffAppearance(){
            return offAppearance;
        }
        //Used to determine if we are using an AND or OR when testing the Sensors and Signals
        private boolean turnoutSensorOper = true;
        public boolean getSensorTurnoutOper(){ return turnoutSensorOper;}
        public void setSensorTurnoutOper(boolean boo) { turnoutSensorOper=boo; }
        
        //Don't yet have the AND or OR set yet.
        public boolean checkActive(){
            System.out.println("checkActive");
            boolean state = false;
            for (int x = 0; x<_signalTurnoutList.size(); x++){
                System.out.println("Real state " + _signalTurnoutList.get(x).getName()+ " " + _signalTurnoutList.get(x).getTurnout().getKnownState() + " state we testing for " + _signalTurnoutList.get(x).getState());
                if (_signalTurnoutList.get(x).getTurnout().getKnownState()==_signalTurnoutList.get(x).getState())
                    state = true;
                else {
                    state = false;
                    break;
                }
            }

            for (int x = 0; x<_signalSensorList.size(); x++){
                if (_signalSensorList.get(x).getSensor().getKnownState()==_signalSensorList.get(x).getState())
                    state = true;
                else {
                    state = false;
                    break;
                }
            }
            System.out.println(state);
            if (state)
                getSignal().setAppearance(onAppearance);
            else
                getSignal().setAppearance(offAppearance);
            return state;
        }
        
        ArrayList <SignalTurnout> _signalTurnoutList = new ArrayList<SignalTurnout>();
        private class SignalTurnout {
            NamedBeanHandle<Turnout> _turnout;
            int _state;

            SignalTurnout(String pName, int state) {
                Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout(pName);
                _turnout = new NamedBeanHandle<Turnout>(pName, turnout);
                setState(state);
            }

            String getName() {
                if (_turnout != null)
                {
                    return _turnout.getName();
                }
                return null;
            }
            boolean setState(int state) {
                if (_turnout == null) {
                    return false;
                }
                if ((state!=Turnout.THROWN) && (state!=Turnout.CLOSED)) {
    //                log.warn("Illegal Turnout state for Route: "+getName() );
                    return false;
                }        
                _state = state;
                return true;
            }
            int getState() {
                return _state;
            }
            Turnout getTurnout() {
                return _turnout.getBean();
            }
            
            NamedBeanHandle<Turnout> getNamedTurnout() {
                return _turnout;
            }
        }
                  
        void addTurnout(String name, int state){
            SignalTurnout signalTurnout = new SignalTurnout(name, state);
            _signalTurnoutList.add(signalTurnout);
        }
        
        int getTurnoutState(String name) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(name);
            for (int i=0; i<_signalTurnoutList.size(); i++) {
                if( _signalTurnoutList.get(i).getTurnout() == t1 ) {
                    // Found turnout
                    System.out.println(_signalTurnoutList.get(i).getState());
                    return _signalTurnoutList.get(i).getState();
                }
            }
            return -1;
        }
        
        public int getNumTurnouts() {
            return _signalTurnoutList.size();
        }
        
        public SignalTurnout getSignalTurnoutByIndex(int x){
            return _signalTurnoutList.get(x);
        }
        
        public String getTurnoutName(int x){
            return _signalTurnoutList.get(x).getName();
        }
        
        public int getTurnoutState(int x){
            return _signalTurnoutList.get(x).getState();
        }
        
        public boolean isTurnoutIncluded(String pName) {
            //Signal s1 = InstanceManager.signalHeadManagerInstance().getSignal(systemName);
            System.out.println("looking for " + pName);
            for (int i=0; i<_signalTurnoutList.size(); i++) {
                System.out.println(_signalTurnoutList.get(i).getName());
                if ( _signalTurnoutList.get(i).getName().equals(pName) ) {
                    // Found turnout
                    return true;
                }
            }
            return false;
        }
        
        public void clearSignalTurnouts() {
            _signalTurnoutList = new ArrayList<SignalTurnout>();
        }
        
        public void clearSignalSensors() {
            _signalSensorList = new ArrayList<SignalSensor>();
        }
        ArrayList <SignalSensor> _signalSensorList = new ArrayList<SignalSensor>();
        private class SignalSensor {
            NamedBeanHandle<Sensor> _Sensor;
            int _state;

            SignalSensor(String pName, int state) {
                Sensor Sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                _Sensor = new NamedBeanHandle<Sensor>(pName, Sensor);
                setState(state);
            }

            String getName() {
                if (_Sensor != null)
                {
                    return _Sensor.getName();
                }
                return null;
            }
            boolean setState(int state) {
                if (_Sensor == null) {
                    return false;
                }
                if ((state!=Sensor.ACTIVE) && (state!=Sensor.INACTIVE)) {
    //                log.warn("Illegal Sensor state for Signal Indicator: "+getName() );
                    return false;
                }        
                _state = state;
                return true;
            }
            int getState() {
                return _state;
            }
            Sensor getSensor() {
                return _Sensor.getBean();
            }
            
            NamedBeanHandle<Sensor> getNamedSensor() {
                return _Sensor;
            }
        }
        
        void addSensor(String name, int state){
            SignalSensor signalSensor = new SignalSensor(name, state);
            _signalSensorList.add(signalSensor);
        }
        
        int getSensorState(String name) {
            Sensor t1 = InstanceManager.sensorManagerInstance().provideSensor(name);
            for (int i=0; i<_signalSensorList.size(); i++) {
                if( _signalSensorList.get(i).getSensor() == t1 ) {
                    // Found Sensor
                    System.out.println(_signalSensorList.get(i).getState());
                    return _signalSensorList.get(i).getState();
                }
            }
            return -1;
        }
        
        public int getNumSensors() {
            return _signalSensorList.size();
        }
        
        public SignalSensor getSignalSensorByIndex(int x){
            return _signalSensorList.get(x);
        }
        
        public String getSensorName(int x){
            return _signalSensorList.get(x).getName();
        }
        
        public int getSensorState(int x){
            return _signalSensorList.get(x).getState();
        }
        
        public boolean isSensorIncluded(String pName) {
            //Signal s1 = InstanceManager.signalHeadManagerInstance().getSignal(systemName);
            System.out.println("looking for " + pName);
            for (int i=0; i<_signalSensorList.size(); i++) {
                System.out.println(_signalSensorList.get(i).getName());
                if ( _signalSensorList.get(i).getName().equals(pName) ) {
                    // Found Sensor
                    return true;
                }
            }
            return false;
        }
    }

    public int getState() {
        //return _currentState;
        return 0x00;
    }
    
    public void setState(int state) {
    /*    if (_currentState != state) {
            int oldState = _currentState;
            _currentState = state;
            firePropertyChange("KnownState", new Integer(oldState), new Integer(_currentState));
        }*/
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalGroup.class.getName());
}