package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.util.ThreadingUtil;

/**
 * This action sets the state of a sensor.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionSensor extends AbstractDigitalAction implements VetoableChangeListener {

    private ActionSensor _template;
    private NamedBeanHandle<Sensor> _sensorHandle;
    private SensorState _sensorState = SensorState.ACTIVE;
    
    public ActionSensor(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    private ActionSensor(ActionSensor template) {
        super(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        _template = template;
        if (_template == null) throw new NullPointerException();    // Temporary solution to make variable used.
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new ActionSensor(this);
    }
    
    public void setSensorName(String sensorName) {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
        if (sensor != null) {
            _sensorHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
            InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        } else {
            _sensorHandle = null;
            InstanceManager.sensorManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public void setSensor(NamedBeanHandle<Sensor> handle) {
        _sensorHandle = handle;
        if (_sensorHandle != null) {
            InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        } else {
            InstanceManager.sensorManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public void setSensor(@CheckForNull Sensor sensor) {
        if (sensor != null) {
            _sensorHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(sensor.getDisplayName(), sensor);
            InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        } else {
            _sensorHandle = null;
            InstanceManager.sensorManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Sensor> getSensor() {
        return _sensorHandle;
    }
    
    public void setSensorState(SensorState state) {
        _sensorState = state;
    }
    
    public SensorState getSensorState() {
        return _sensorState;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor().getBean())) {
                    setSensor((Sensor)null);
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        final Sensor t = _sensorHandle.getBean();
//        final int newState = _sensorState.getID();
        ThreadingUtil.runOnLayout(() -> {
            if (_sensorState == SensorState.TOGGLE) {
//                t.setCommandedState(newState);
                if (t.getCommandedState() == Sensor.INACTIVE) {
                    t.setCommandedState(Sensor.ACTIVE);
                } else {
                    t.setCommandedState(Sensor.INACTIVE);
                }
            } else {
                t.setCommandedState(_sensorState.getID());
            }
        });
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Sensor_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String sensorName;
        if (_sensorHandle != null) {
            sensorName = _sensorHandle.getBean().getDisplayName();
        } else {
            sensorName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Sensor_Long", sensorName, _sensorState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum SensorState {
        INACTIVE(Sensor.INACTIVE, Bundle.getMessage("SensorStateInactive")),
        ACTIVE(Sensor.ACTIVE, Bundle.getMessage("SensorStateActive")),
        TOGGLE(-1, Bundle.getMessage("SensorToggleStatus"));
        
        private final int _id;
        private final String _text;
        
        private SensorState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public SensorState get(int id) {
            switch (id) {
                case Sensor.INACTIVE:
                    return INACTIVE;
                    
                case Sensor.ACTIVE:
                    return ACTIVE;
                    
                default:
                    throw new IllegalArgumentException("invalid sensor state");
            }
        }
        
        public int getID() {
            return _id;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
}
