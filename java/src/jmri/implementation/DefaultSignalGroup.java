package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conditional.java
 *
 * A Conditional type to provide runtime support for Densor Groups.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Pete Cressman Copyright (C) 2009
 */
public class DefaultSignalGroup extends AbstractNamedBean implements jmri.SignalGroup {

    public DefaultSignalGroup(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalGroup(String systemName) {
        super(systemName, null);
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalGroup");
    }

    ArrayList<String> _signalMastAppearances = new ArrayList<String>();

    private NamedBeanHandle<SignalMast> _signalMast;

    private boolean headactive = false;

    private boolean enabled = true;

    public void setEnabled(boolean boo) {
        enabled = boo;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setSignalMast(String pName) {

        SignalMast mMast = InstanceManager.signalMastManagerInstance().getBySystemName(pName);
        if (mMast == null) {
            mMast = InstanceManager.signalMastManagerInstance().getByUserName(pName);
        }
        if (mMast == null) {
            log.warn("did not find a SignalHead named " + pName);
            return;
        }
        setSignalMast(mMast, pName);
    }

    public void setSignalMast(SignalMast mMast, String pName) {
        if (_signalMast != null) {
            getSignalMast().removePropertyChangeListener(mSignalMastListener);
        }
        _signalMast = new NamedBeanHandle<SignalMast>(pName, mMast);
        getSignalMast().addPropertyChangeListener(mSignalMastListener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Aspect")) {
                    String now = ((String) e.getNewValue());
                    if (isSignalMastAppearanceIncluded(now)) {
                        setHead();
                    } else {
                        resetHeads();
                    }
                }
            }
        });

    }

    public SignalMast getSignalMast() {
        return _signalMast.getBean();
    }

    public String getSignalMastName() {
        return _signalMast.getName();
    }

    public void addSignalMastAppearance(String appearance) {
        if (isSignalMastAppearanceIncluded(appearance)) {
            return;
        }
        _signalMastAppearances.add(appearance);
    }

    public boolean isSignalMastAppearanceIncluded(String appearance) {
        for (int i = 0; i < _signalMastAppearances.size(); i++) {
            if (_signalMastAppearances.get(i).equals(appearance)) {
                // Found Appearance
                return true;
            }
        }
        return false;
    }

    public void deleteSignalMastAppearance(String appearance) {
        _signalMastAppearances.remove(appearance);
    }

    public int getNumSignalMastAppearances() {
        return _signalMastAppearances.size();
    }

    public String getSignalMastAppearanceByIndex(int x) {
        try {
            return _signalMastAppearances.get(x);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public void clearSignalMastAppearance() {
        _signalMastAppearances = new ArrayList<String>();
    }

    /*
     Add a new signalhead to the group
     */
    public void addSignalHead(NamedBeanHandle<SignalHead> sh) {
        SignalHeadItem signalitem = new SignalHeadItem(sh);
        _signalHeadItem.add(signalitem);
    }
    /*
     Add a new signalhead to the group
     */

    public void addSignalHead(String pName) {
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) {
            mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        }
        if (mHead == null) {
            log.warn("did not find a SignalHead named " + pName);
        } else {
            addSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
        }
    }

    public void addSignalHead(SignalHead mHead) {
        addSignalHead(new NamedBeanHandle<SignalHead>(mHead.getDisplayName(), mHead));
    }

    protected PropertyChangeListener mSignalMastListener = null;

    public void setSignalHeadAlignTurnout(SignalHead mHead, Turnout mTurn, int state) {
        SignalHeadItem sh = getSignalHeadItem(mHead);
        sh.addTurnout(mTurn, state);
    }

    public void setSignalHeadAlignSensor(SignalHead mHead, Sensor mSen, int state) {
        SignalHeadItem sh = getSignalHeadItem(mHead);
        sh.addSensor(mSen, state);
    }

    /*
     Returns the 'n' signalheaditem
     */
    private SignalHeadItem getSignalHeadItemByIndex(int n) {
        try {
            return _signalHeadItem.get(n);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public String getSignalHeadItemNameByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public SignalHead getSignalHeadItemBeanByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getSignal();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    /*
     Returns the number of signalheads in this group
     */
    public int getNumSignalHeadItems() {
        return _signalHeadItem.size();
    }

    public int getSignalHeadOffState(SignalHead bean) {
        try {
            return getSignalHeadItem(bean).getOffAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public int getSignalHeadOnState(SignalHead bean) {
        try {
            return getSignalHeadItem(bean).getOnAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public int getSignalHeadOnStateByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getOnAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public int getSignalHeadOffStateByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getOffAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public void deleteSignalHead(SignalHead pSignal) {
        _signalHeadItem.remove(getSignalHeadItem(pSignal));
    }

    public void deleteSignalHead(NamedBeanHandle<SignalHead> sh) {
        _signalHeadItem.remove(getSignalHeadItem(sh.getName()));
    }

    public void setSignalHeadOnState(SignalHead head, int state) {
        getSignalHeadItem(head).setOnAppearance(state);
        firePropertyChange("UpdateCondition", null, null);
    }

    public void setSignalHeadOffState(SignalHead head, int state) {
        getSignalHeadItem(head).setOffAppearance(state);
        firePropertyChange("UpdateCondition", null, null);
    }

    public boolean isSignalIncluded(SignalHead bean) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getSignal() == bean) {
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
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getName().equals(name)) {
                return _signalHeadItem.get(i);
            }
        }
        return null;
    }

    private SignalHeadItem getSignalHeadItem(NamedBean bean) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getSignal().equals(bean)) {
                return _signalHeadItem.get(i);
            }
        }
        return null;
    }

    public boolean isTurnoutIncluded(SignalHead pSignal, Turnout pTurnout) {
        return getSignalHeadItem(pSignal).isTurnoutIncluded(pTurnout);
    }

    public int getTurnoutState(SignalHead pSignal, Turnout pTurnout) {
        SignalHeadItem sig = getSignalHeadItem(pSignal);
        if (sig != null) {
            return sig.getTurnoutState(pTurnout);
        }
        return -1;
    }

    public int getTurnoutStateByIndex(int x, Turnout pTurnout) {
        try {
            return getSignalHeadItemByIndex(x).getTurnoutState(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public int getTurnoutStateByIndex(int x, int pTurnout) {
        try {
            return getSignalHeadItemByIndex(x).getTurnoutState(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public String getTurnoutNameByIndex(int x, int pTurnout) {
        try {
            return getSignalHeadItemByIndex(x).getTurnoutName(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public Turnout getTurnoutByIndex(int x, int pTurnout) {
        try {
            return getSignalHeadItemByIndex(x).getTurnout(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public int getSensorStateByIndex(int x, int pSensor) {
        try {
            return getSignalHeadItemByIndex(x).getSensorState(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public String getSensorNameByIndex(int x, int pSensor) {
        try {
            return getSignalHeadItemByIndex(x).getSensorName(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public Sensor getSensorByIndex(int x, int pSensor) {
        try {
            return getSignalHeadItemByIndex(x).getSensor(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public boolean isSensorIncluded(SignalHead pSignal, Sensor pSensor) {
        return getSignalHeadItem(pSignal).isSensorIncluded(pSensor);
    }

    public int getSensorState(SignalHead pSignal, Sensor pSensor) {
        SignalHeadItem sig = getSignalHeadItem(pSignal);
        if (sig != null) {
            return sig.getSensorState(pSensor);
        }
        return -1;
    }

    public boolean getSensorTurnoutOper(SignalHead pSignal) {
        return getSignalHeadItem(pSignal).getSensorTurnoutOper();
    }

    public boolean getSensorTurnoutOperByIndex(int x) {
        return getSignalHeadItemByIndex(x).getSensorTurnoutOper();
    }

    public void setSensorTurnoutOper(SignalHead pSignal, boolean boo) {
        getSignalHeadItem(pSignal).setSensorTurnoutOper(boo);
        firePropertyChange("UpdateCondition", null, null);
    }

    public void clearSignalTurnout(SignalHead pSignal) {
        getSignalHeadItem(pSignal).clearSignalTurnouts();
    }

    public void clearSignalSensor(SignalHead pSignal) {
        getSignalHeadItem(pSignal).clearSignalSensors();
    }

    private void resetHeads() {
        if (!headactive) {
            return;
        }
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            _signalHeadItem.get(i).getSignal().setAppearance(_signalHeadItem.get(i).getOffAppearance());
        }
        headactive = false;
    }

    private void setHead() {
        boolean active = false;
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).checkActive()) {
                if (active) {
                    log.warn("two signal heads in the group should not be active at once");
                }
                active = true;
                headactive = true;
            }
        }
    }

    public int getNumSignalHeadSensorsByIndex(int x) {
        try {

            return getSignalHeadItemByIndex(x).getNumSensors();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public int getNumSignalHeadTurnoutsByIndex(int x) {
        try {
            return getSignalHeadItemByIndex(x).getNumTurnouts();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    ArrayList<SignalHeadItem> _signalHeadItem = new ArrayList<SignalHeadItem>();

    private static class SignalHeadItem implements java.io.Serializable {

        SignalHeadItem(NamedBeanHandle<SignalHead> sh) {
            namedHead = sh;
            if (namedHead.getBean().getClass().getName().contains("SingleTurnoutSignalHead")) {
                jmri.implementation.SingleTurnoutSignalHead Signal = (jmri.implementation.SingleTurnoutSignalHead) namedHead.getBean();
                if ((onAppearance == 0x00) && (offAppearance == 0x00)) {
                    onAppearance = Signal.getOnAppearance();
                    offAppearance = Signal.getOffAppearance();
                }
            }
        }

        private NamedBeanHandle<SignalHead> namedHead;

        public String getName() {
            return namedHead.getName();
        }

        public SignalHead getSignal() {
            return namedHead.getBean();
        }

        private int onAppearance = 0x00;
        private int offAppearance = 0x00;

        public void setOnAppearance(int app) {
            onAppearance = app;
        }

        public int getOnAppearance() {
            return onAppearance;
        }

        public void setOffAppearance(int app) {
            offAppearance = app;
        }

        public int getOffAppearance() {
            return offAppearance;
        }
        //Used to determine if we are using an AND or OR when testing the Sensors and Signals
        private boolean turnoutSensorOper = true;

        public boolean getSensorTurnoutOper() {
            return turnoutSensorOper;
        }

        public void setSensorTurnoutOper(boolean boo) {
            turnoutSensorOper = boo;
        }

        //Don't yet have the AND or OR set yet.
        public boolean checkActive() {
            boolean state = false;
            for (int x = 0; x < _signalTurnoutList.size(); x++) {
                log.debug("Real state " + _signalTurnoutList.get(x).getName() + " " + _signalTurnoutList.get(x).getTurnout().getKnownState() + " state we testing for " + _signalTurnoutList.get(x).getState());
                if (_signalTurnoutList.get(x).getTurnout().getKnownState() == _signalTurnoutList.get(x).getState()) {
                    state = true;
                } else {
                    state = false;
                    break;
                }
            }

            for (int x = 0; x < _signalSensorList.size(); x++) {
                if (_signalSensorList.get(x).getSensor().getKnownState() == _signalSensorList.get(x).getState()) {
                    state = true;
                } else {
                    state = false;
                    break;
                }
            }
            if (state) {
                getSignal().setAppearance(onAppearance);
            } else {
                getSignal().setAppearance(offAppearance);
            }
            return state;
        }

        ArrayList<SignalTurnout> _signalTurnoutList = new ArrayList<SignalTurnout>();

        private static class SignalTurnout implements java.io.Serializable {

            NamedBeanHandle<Turnout> _turnout;
            int _state;

            SignalTurnout(Turnout turn, int state) {
                _turnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turn.getDisplayName(), turn);
                setState(state);
            }

            String getName() {
                if (_turnout != null) {
                    return _turnout.getName();
                }
                return null;
            }

            boolean setState(int state) {
                if (_turnout == null) {
                    return false;
                }
                if ((state != Turnout.THROWN) && (state != Turnout.CLOSED)) {
                    log.warn("Illegal Turnout state " + state + ": " + getName());
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
        }

        void addTurnout(Turnout turn, int state) {
            SignalTurnout signalTurnout = new SignalTurnout(turn, state);
            _signalTurnoutList.add(signalTurnout);
        }

        Turnout getTurnout(int x) {
            return _signalTurnoutList.get(x).getTurnout();
        }

        int getTurnoutState(Turnout turn) {
            for (int i = 0; i < _signalTurnoutList.size(); i++) {
                if (_signalTurnoutList.get(i).getTurnout() == turn) {
                    return _signalTurnoutList.get(i).getState();
                }
            }
            return -1;
        }

        int getNumTurnouts() {
            return _signalTurnoutList.size();
        }

        String getTurnoutName(int x) {
            return _signalTurnoutList.get(x).getName();
        }

        int getTurnoutState(int x) {
            return _signalTurnoutList.get(x).getState();
        }

        boolean isTurnoutIncluded(Turnout pTurnout) {
            for (int i = 0; i < _signalTurnoutList.size(); i++) {
                if (_signalTurnoutList.get(i).getTurnout() == pTurnout) {
                    return true;
                }
            }
            return false;
        }

        void clearSignalTurnouts() {
            _signalTurnoutList = new ArrayList<SignalTurnout>();
        }

        void clearSignalSensors() {
            _signalSensorList = new ArrayList<SignalSensor>();
        }

        ArrayList<SignalSensor> _signalSensorList = new ArrayList<SignalSensor>();

        private static class SignalSensor implements java.io.Serializable {

            NamedBeanHandle<Sensor> _Sensor;
            int _state;

            SignalSensor(Sensor sen, int state) {
                _Sensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sen.getDisplayName(), sen);
                setState(state);
            }

            String getName() {
                if (_Sensor != null) {
                    return _Sensor.getName();
                }
                return null;
            }

            boolean setState(int state) {
                if (_Sensor == null) {
                    return false;
                }
                if ((state != Sensor.ACTIVE) && (state != Sensor.INACTIVE)) {
                    log.warn("Illegal Sensor state " + state + " for : " + getName());
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

        }

        void addSensor(Sensor sen, int state) {
            SignalSensor signalSensor = new SignalSensor(sen, state);
            _signalSensorList.add(signalSensor);
        }

        int getSensorState(Sensor sen) {
            for (int i = 0; i < _signalSensorList.size(); i++) {
                if (_signalSensorList.get(i).getSensor() == sen) {
                    return _signalSensorList.get(i).getState();
                }
            }
            return -1;
        }

        int getNumSensors() {
            return _signalSensorList.size();
        }

        /*SignalSensor getSignalSensorByIndex(int x){
         return _signalSensorList.get(x);
         }*/
        String getSensorName(int x) {
            return _signalSensorList.get(x).getName();
        }

        Sensor getSensor(int x) {
            return _signalSensorList.get(x).getSensor();
        }

        int getSensorState(int x) {
            return _signalSensorList.get(x).getState();
        }

        boolean isSensorIncluded(Sensor pSensor) {
            for (int i = 0; i < _signalSensorList.size(); i++) {
                if (_signalSensorList.get(i).getSensor() == pSensor) {
                    // Found Sensor
                    return true;
                }
            }
            return false;
        }
    }

    public int getState() {
        return 0x00;
    }

    public void setState(int state) {

    }

    /**
     * Number of current listeners. May return -1 if the information is not
     * available for some reason.
     */
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    public synchronized java.beans.PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        if (pcs != null) {
            pcs.firePropertyChange(p, old, n);
        }
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroup.class.getName());
}
