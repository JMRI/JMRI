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
 * DefaultSignalGroup.java
 *
 * A Conditional type to provide Signal Groups (n Signal Heads w/Conditionals for a main Mast).
 * <P>
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse 2017
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

    ArrayList<String> _signalMastAspects = new ArrayList<String>();

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

        SignalMast mMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(pName);
        if (mMast == null) {
            mMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName(pName);
        }
        if (mMast == null) {
            log.warn("did not find a Signal Mast named {}", pName);
            return;
        }
        setSignalMast(mMast, pName);
    }

    public void setSignalMast(SignalMast signalMast, String mastName) {
        if (_signalMast != null) {
            getSignalMast().removePropertyChangeListener(mSignalMastListener);
        }
        _signalMast = new NamedBeanHandle<SignalMast>(mastName, signalMast);
        getSignalMast().addPropertyChangeListener(mSignalMastListener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Aspect")) {
                    String now = ((String) e.getNewValue());
                    if (isSignalMastAspectIncluded(now)) {
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

    public void addSignalMastAspect(String aspect) {
        if (isSignalMastAspectIncluded(aspect)) {
            return;
        }
        _signalMastAspects.add(aspect);
    }

    public boolean isSignalMastAspectIncluded(String aspect) {
        for (int i = 0; i < _signalMastAspects.size(); i++) {
            if (_signalMastAspects.get(i).equals(aspect)) {
                // Found Aspect
                return true;
            }
        }
        return false;
    }

    public void deleteSignalMastAspect(String aspect) {
        _signalMastAspects.remove(aspect);
    }

    public int getNumSignalMastAspects() {
        return _signalMastAspects.size();
    }

    public String getSignalMastAspectByIndex(int x) {
        try {
            return _signalMastAspects.get(x);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public void clearSignalMastAspect() {
        _signalMastAspects = new ArrayList<String>();
    }

    /**
     * Add a new Signal Head to the group
     */
    public void addSignalHead(NamedBeanHandle<SignalHead> headBean) {
        SignalHeadItem signalitem = new SignalHeadItem(headBean);
        _signalHeadItem.add(signalitem);
    }

    /**
     * Add a new Signal Head to the group
     */
    public void addSignalHead(String pName) {
        SignalHead mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(pName);
        if (mHead == null) {
            mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(pName);
        }
        if (mHead == null) {
            log.warn("did not find a SignalHead named " + pName);
        } else {
            addSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
        }
    }

    public void addSignalHead(SignalHead signalHead) {
        addSignalHead(new NamedBeanHandle<SignalHead>(signalHead.getDisplayName(), signalHead));
    }

    protected PropertyChangeListener mSignalMastListener = null;

    public void setHeadAlignTurnout(SignalHead signalHead, Turnout turnout, int state) {
        SignalHeadItem sh = getSignalHeadItem(signalHead);
        sh.addTurnout(turnout, state);
    }

    public void setHeadAlignSensor(SignalHead signalHead, Sensor sensor, int state) {
        SignalHeadItem sh = getSignalHeadItem(signalHead);
        sh.addSensor(sensor, state);
    }

    /**
     * Returns the 'n' Signal Head item
     */
    private SignalHeadItem getSignalHeadItemByIndex(int n) {
        try {
            return _signalHeadItem.get(n);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public String getHeadItemNameByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public SignalHead getHeadItemBeanByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getSignalHead();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    /**
     * Returns the number of Signal Heads in this Group
     */
    public int getNumHeadItems() {
        return _signalHeadItem.size();
    }

    public int getHeadOffState(SignalHead headBean) {
        try {
            return getSignalHeadItem(headBean).getOffAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public int getHeadOnState(SignalHead headBean) {
        try {
            return getSignalHeadItem(headBean).getOnAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public int getHeadOnStateByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getOnAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public int getHeadOffStateByIndex(int n) {
        try {
            return getSignalHeadItemByIndex(n).getOffAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public void deleteSignalHead(SignalHead sh) {
        _signalHeadItem.remove(getSignalHeadItem(sh));
    }

    public void deleteSignalHead(NamedBeanHandle<SignalHead> headBean) {
        _signalHeadItem.remove(getSignalHeadItem(headBean.getName()));
    }

    public void setHeadOnState(SignalHead head, int state) {
        getSignalHeadItem(head).setOnAppearance(state);
        firePropertyChange("UpdateCondition", null, null);
    }

    public void setHeadOffState(SignalHead head, int state) {
        getSignalHeadItem(head).setOffAppearance(state);
        firePropertyChange("UpdateCondition", null, null);
    }

    public boolean isHeadIncluded(SignalHead signalHead) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getSignalHead() == signalHead) {
                // Found head
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a Signal Head item by its name from the Signal Group
     */
    private SignalHeadItem getSignalHeadItem(String name) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getName().equals(name)) {
                return _signalHeadItem.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a Signal Head item by its Bean from the Signal Group
     */
    private SignalHeadItem getSignalHeadItem(NamedBean headBean) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getSignalHead().equals(headBean)) {
                return _signalHeadItem.get(i);
            }
        }
        return null;
    }

    public boolean isTurnoutIncluded(SignalHead signalHead, Turnout turnout) {
        return getSignalHeadItem(signalHead).isTurnoutIncluded(turnout);
    }

    public int getTurnoutState(SignalHead signalHead, Turnout turnout) {
        SignalHeadItem shi = getSignalHeadItem(signalHead);
        if (shi != null) {
            return shi.getTurnoutState(turnout);
        }
        return -1;
    }

    public int getTurnoutStateByIndex(int x, Turnout turnout) {
        try {
            return getSignalHeadItemByIndex(x).getTurnoutState(turnout);
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

    public boolean isSensorIncluded(SignalHead signalHead, Sensor sensor) {
        return getSignalHeadItem(signalHead).isSensorIncluded(sensor);
    }

    public int getSensorState(SignalHead signalHead, Sensor sensor) {
        SignalHeadItem shi = getSignalHeadItem(signalHead);
        if (shi != null) {
            return shi.getSensorState(sensor);
        }
        return -1;
    }

    public boolean getSensorTurnoutOper(SignalHead signalHead) {
        return getSignalHeadItem(signalHead).getSensorTurnoutOper();
    }

    public boolean getSensorTurnoutOperByIndex(int x) {
        return getSignalHeadItemByIndex(x).getSensorTurnoutOper();
    }

    public void setSensorTurnoutOper(SignalHead signalHead, boolean boo) {
        getSignalHeadItem(signalHead).setSensorTurnoutOper(boo);
        firePropertyChange("UpdateCondition", null, null);
    }

    public void clearHeadTurnout(SignalHead signalHead) {
        getSignalHeadItem(signalHead).clearSignalTurnouts();
    }

    public void clearHeadSensor(SignalHead signalHead) {
        getSignalHeadItem(signalHead).clearSignalSensors();
    }

    private void resetHeads() {
        if (!headactive) {
            return;
        }
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            _signalHeadItem.get(i).getSignalHead().setAppearance(_signalHeadItem.get(i).getOffAppearance());
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

    public int getNumHeadSensorsByIndex(int x) {
        try {

            return getSignalHeadItemByIndex(x).getNumSensors();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    public int getNumHeadTurnoutsByIndex(int x) {
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
                jmri.implementation.SingleTurnoutSignalHead stsh = (jmri.implementation.SingleTurnoutSignalHead) namedHead.getBean();
                if ((onAppearance == 0x00) && (offAppearance == 0x00)) {
                    onAppearance = stsh.getOnAppearance();
                    offAppearance = stsh.getOffAppearance();
                }
            }
        }

        private NamedBeanHandle<SignalHead> namedHead;

        public String getName() {
            return namedHead.getName();
        }

        public SignalHead getSignalHead() {
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

        /**
         * Sets whether the sensors and turnouts should be treated as separate
         * calculations (OR) or as one (AND), when determining if the Signal Head in this item
         * should be On or Off.
         * @param boo Provide true for AND, false for OR
         */
        public void setSensorTurnoutOper(boolean boo) {
            turnoutSensorOper = boo;
        }

        // Don't yet have the AND or OR set.
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
                getSignalHead().setAppearance(onAppearance);
            } else {
                getSignalHead().setAppearance(offAppearance);
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
                    log.warn("Illegal Turnout state " + state + " for : " + getName());
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

        boolean isTurnoutIncluded(Turnout turnout) {
            for (int i = 0; i < _signalTurnoutList.size(); i++) {
                if (_signalTurnoutList.get(i).getTurnout() == turnout) {
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

        boolean isSensorIncluded(Sensor sensor) {
            for (int i = 0; i < _signalSensorList.size(); i++) {
                if (_signalSensorList.get(i).getSensor() == sensor) {
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
