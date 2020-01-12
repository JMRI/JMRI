package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Conditional type to provide Signal Groups (n Signal Heads w/Conditionals
 * for a main Mast).
 *
 * @see jmri.SignalGroup SignalGroup
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse 2017
 */
public class DefaultSignalGroup extends AbstractNamedBean implements jmri.SignalGroup {

    /**
     * Constructor for SignalGroup instance.
     *
     * @param systemName suggested system name
     * @param userName   provided user name
     */
    public DefaultSignalGroup(String systemName, String userName) {
        super(systemName, userName);
    }

    /**
     * Constructor for SignalGroup instance.
     *
     * @param systemName suggested system name
     */
    public DefaultSignalGroup(String systemName) {
        super(systemName, null);
        log.debug("default SignalGroup {} created", systemName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalGroup");
    }

    ArrayList<String> _signalMastAspects = new ArrayList<String>();

    private NamedBeanHandle<SignalMast> _signalMast;

    private boolean headactive = false;

    private boolean enabled = true;

    @Override
    public void setEnabled(boolean boo) {
        enabled = boo;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
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

    @Override
    public void setSignalMast(SignalMast signalMast, String mastName) {
        if (_signalMast != null) {
            getSignalMast().removePropertyChangeListener(mSignalMastListener);
        }
        _signalMast = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(mastName, signalMast);
        getSignalMast().addPropertyChangeListener(mSignalMastListener = new java.beans.PropertyChangeListener() {
            @Override
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

    @Override
    public SignalMast getSignalMast() {
        return _signalMast.getBean();
    }

    @Override
    public String getSignalMastName() {
        return _signalMast.getName();
    }

    @Override
    public void addSignalMastAspect(String aspect) {
        if (isSignalMastAspectIncluded(aspect)) {
            return;
        }
        _signalMastAspects.add(aspect);
    }

    @Override
    public boolean isSignalMastAspectIncluded(String aspect) {
        for (int i = 0; i < _signalMastAspects.size(); i++) {
            if (_signalMastAspects.get(i).equals(aspect)) {
                // Found Aspect
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteSignalMastAspect(String aspect) {
        _signalMastAspects.remove(aspect);
    }

    @Override
    public int getNumSignalMastAspects() {
        return _signalMastAspects.size();
    }

    @Override
    public String getSignalMastAspectByIndex(int x) {
        try {
            return _signalMastAspects.get(x);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public void clearSignalMastAspect() {
        _signalMastAspects = new ArrayList<String>();
    }

    @Override
    public void addSignalHead(NamedBeanHandle<SignalHead> headBean) {
        SignalHeadItem shi = new SignalHeadItem(headBean);
        _signalHeadItem.add(shi);
    }

    /**
     * Add a new Signal Head to the group by name.
     *
     * @param pName system or username of existing signal head to add to group
     */
    public void addSignalHead(String pName) {
        SignalHead mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(pName);
        if (mHead == null) {
            mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(pName);
        }
        if (mHead == null) {
            log.warn("did not find a SignalHead named {}", pName);
        } else {
            addSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(pName, mHead));
        }
    }

    @Override
    public void addSignalHead(SignalHead signalHead) {
        addSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead));
    }

    protected PropertyChangeListener mSignalMastListener = null;

    @Override
    public void setHeadAlignTurnout(SignalHead signalHead, Turnout turnout, int state) {
        SignalHeadItem shi = getHeadItem(signalHead);
        shi.addTurnout(turnout, state);
    }

    @Override
    public void setHeadAlignSensor(SignalHead signalHead, Sensor sensor, int state) {
        SignalHeadItem shi = getHeadItem(signalHead);
        shi.addSensor(sensor, state);
    }

    private SignalHeadItem getHeadItemByIndex(int x) {
        try {
            return _signalHeadItem.get(x);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public String getHeadItemNameByIndex(int x) {
        try {
            return getHeadItemByIndex(x).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public SignalHead getHeadItemBeanByIndex(int x) {
        try {
            return getHeadItemByIndex(x).getSignalHead();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public int getNumHeadItems() {
        return _signalHeadItem.size();
    }

    @Override
    public int getHeadOffState(SignalHead headBean) {
        try {
            return getHeadItem(headBean).getOffAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    @Override
    public int getHeadOnState(SignalHead headBean) {
        try {
            return getHeadItem(headBean).getOnAppearance();
        } catch (NullPointerException e) {
            return -1;
        }
    }

    @Override
    public int getHeadOnStateByIndex(int x) {
        try {
            return getHeadItemByIndex(x).getOnAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    @Override
    public int getHeadOffStateByIndex(int x) {
        try {
            return getHeadItemByIndex(x).getOffAppearance();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    @Override
    public void deleteSignalHead(SignalHead sh) {
        _signalHeadItem.remove(getHeadItem(sh));
    }

    @Override
    public void deleteSignalHead(NamedBeanHandle<SignalHead> headBean) {
        _signalHeadItem.remove(getHeadItem(headBean.getName()));
    }

    @Override
    public void setHeadOnState(SignalHead head, int state) {
        getHeadItem(head).setOnAppearance(state);
        firePropertyChange("UpdateCondition", null, null);
    }

    @Override
    public void setHeadOffState(SignalHead head, int state) {
        getHeadItem(head).setOffAppearance(state);
        firePropertyChange("UpdateCondition", null, null);
    }

    @Override
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
     * Get a Signal Head item by its name from the Signal Group
     */
    private SignalHeadItem getHeadItem(String name) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getName().equals(name)) {
                return _signalHeadItem.get(i);
            }
        }
        return null;
    }

    /**
     * Get a Signal Head item by its Bean from the Signal Group
     */
    private SignalHeadItem getHeadItem(NamedBean headBean) {
        for (int i = 0; i < _signalHeadItem.size(); i++) {
            if (_signalHeadItem.get(i).getSignalHead().equals(headBean)) {
                return _signalHeadItem.get(i);
            }
        }
        return null;
    }

    @Override
    public boolean isTurnoutIncluded(SignalHead signalHead, Turnout turnout) {
        return getHeadItem(signalHead).isTurnoutIncluded(turnout);
    }

    @Override
    public int getTurnoutState(SignalHead signalHead, Turnout turnout) {
        SignalHeadItem shi = getHeadItem(signalHead);
        if (shi != null) {
            return shi.getTurnoutState(turnout);
        }
        return -1;
    }

    @Override
    public int getTurnoutStateByIndex(int x, Turnout turnout) {
        try {
            return getHeadItemByIndex(x).getTurnoutState(turnout);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    @Override
    public int getTurnoutStateByIndex(int x, int pTurnout) {
        try {
            return getHeadItemByIndex(x).getTurnoutState(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    @Override
    public String getTurnoutNameByIndex(int x, int pTurnout) {
        try {
            return getHeadItemByIndex(x).getTurnoutName(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public Turnout getTurnoutByIndex(int x, int pTurnout) {
        try {
            return getHeadItemByIndex(x).getTurnout(pTurnout);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public int getSensorStateByIndex(int x, int pSensor) {
        try {
            return getHeadItemByIndex(x).getSensorState(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    @Override
    public String getSensorNameByIndex(int x, int pSensor) {
        try {
            return getHeadItemByIndex(x).getSensorName(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public Sensor getSensorByIndex(int x, int pSensor) {
        try {
            return getHeadItemByIndex(x).getSensor(pSensor);
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public boolean isSensorIncluded(SignalHead signalHead, Sensor sensor) {
        return getHeadItem(signalHead).isSensorIncluded(sensor);
    }

    @Override
    public int getSensorState(SignalHead signalHead, Sensor sensor) {
        SignalHeadItem shi = getHeadItem(signalHead);
        if (shi != null) {
            return shi.getSensorState(sensor);
        }
        return -1;
    }

    @Override
    public boolean getSensorTurnoutOper(SignalHead signalHead) {
        return getHeadItem(signalHead).getSensorTurnoutOper();
    }

    @Override
    public boolean getSensorTurnoutOperByIndex(int x) {
        return getHeadItemByIndex(x).getSensorTurnoutOper();
    }

    @Override
    public void setSensorTurnoutOper(SignalHead signalHead, boolean boo) {
        getHeadItem(signalHead).setSensorTurnoutOper(boo);
        firePropertyChange("UpdateCondition", null, null);
    }

    @Override
    public void clearHeadTurnout(SignalHead signalHead) {
        getHeadItem(signalHead).clearSignalTurnouts();
    }

    @Override
    public void clearHeadSensor(SignalHead signalHead) {
        getHeadItem(signalHead).clearSignalSensors();
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

    @Override
    public int getNumHeadSensorsByIndex(int x) {
        try {

            return getHeadItemByIndex(x).getNumSensors();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }

    @Override
    public int getNumHeadTurnoutsByIndex(int x) {
        try {
            return getHeadItemByIndex(x).getNumTurnouts();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
    }
    ArrayList<SignalHeadItem> _signalHeadItem = new ArrayList<SignalHeadItem>();

    private static class SignalHeadItem {

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
         * Set whether the sensors and turnouts should be treated as separate
         * calculations (OR) or as one (AND), when determining if the Signal
         * Head in this item should be On or Off.
         *
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

        private static class SignalTurnout {

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

        private static class SignalSensor {

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

    @Override
    public int getState() {
        return 0x00;
    }

    @Override
    public void setState(int state) {
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroup.class);

}
