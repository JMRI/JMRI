package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;

/**
 * Evaluates the state of a Sensor.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionSensor extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<Sensor> _sensorHandle;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private SensorState _sensorState = SensorState.Active;

    public ExpressionSensor(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setSensor(@Nonnull String sensorName) {
        assertListenersAreNotRegistered(log, "setSensor");
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(sensorName);
        if (sensor != null) {
            setSensor(sensor);
        } else {
            removeSensor();
            log.error("sensor \"{}\" is not found", sensorName);
        }
    }
    
    public void setSensor(@Nonnull NamedBeanHandle<Sensor> handle) {
        assertListenersAreNotRegistered(log, "setSensor");
        _sensorHandle = handle;
        InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setSensor(@Nonnull Sensor sensor) {
        assertListenersAreNotRegistered(log, "setSensor");
        setSensor(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(sensor.getDisplayName(), sensor));
    }
    
    public void removeSensor() {
        assertListenersAreNotRegistered(log, "setSensor");
        if (_sensorHandle != null) {
            InstanceManager.sensorManagerInstance().removeVetoableChangeListener(this);
            _sensorHandle = null;
        }
    }
    
    public NamedBeanHandle<Sensor> getSensor() {
        return _sensorHandle;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
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
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Sensor_SensorInUseSensorExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor().getBean())) {
                    removeSensor();
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
    public boolean evaluate() {
        if (_sensorHandle == null) return false;
        
        SensorState currentSensorState = SensorState.get(_sensorHandle.getBean().getCommandedState());
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentSensorState == _sensorState;
        } else {
            return currentSensorState != _sensorState;
        }
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
        return Bundle.getMessage(locale, "Sensor_Long", sensorName, _is_IsNot.toString(), _sensorState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_sensorHandle != null)) {
            _sensorHandle.getBean().addPropertyChangeListener("KnownState", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _sensorHandle.getBean().removePropertyChangeListener("KnownState", this);
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum SensorState {
        Inactive(Sensor.INACTIVE, Bundle.getMessage("SensorStateInactive")),
        Active(Sensor.ACTIVE, Bundle.getMessage("SensorStateActive")),
        Other(-1, Bundle.getMessage("SensorOtherStatus"));
        
        private final int _id;
        private final String _text;
        
        private SensorState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public SensorState get(int id) {
            switch (id) {
                case Sensor.INACTIVE:
                    return Inactive;
                    
                case Sensor.ACTIVE:
                    return Active;
                    
                default:
                    return Other;
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensor.class);
    
}
