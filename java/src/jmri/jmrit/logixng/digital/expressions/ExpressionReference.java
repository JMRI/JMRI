package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.AudioManager;
import jmri.LightManager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.util.ReferenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates what a reference points to.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionReference extends AbstractDigitalExpression
        implements PropertyChangeListener {
//        implements PropertyChangeListener, VetoableChangeListener {

    private String _reference;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.IS;
    private PointsTo _pointsTo = PointsTo.NOTHING;

    public ExpressionReference(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setReference(String reference) {
        assertListenersAreNotRegistered(log, "setReference");
        _reference = reference;
    }
    
    public String getReference() {
        return _reference;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setPointsTo(PointsTo state) {
        _pointsTo = state;
    }
    
    public PointsTo getPointsTo() {
        return _pointsTo;
    }
/*
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof String) {
                if (evt.getOldValue().equals(getReference().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof String) {
                if (evt.getOldValue().equals(getReference().getBean())) {
                    setReference((Turnout)null);
                }
            }
        }
    }
*/    
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
        if (_reference == null) return false;
        
        boolean result;
        String ref = ReferenceUtil.getReference(_reference);
        NamedBean t;

        switch (_pointsTo) {
            case NOTHING:
                result = "".equals(ref);
                break;
            
            case TABLE:
                t = InstanceManager.getDefault(NamedTableManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case AUDIO:
                t = InstanceManager.getDefault(AudioManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case LIGHT:
                t = InstanceManager.getDefault(LightManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case MEMORY:
                t = InstanceManager.getDefault(MemoryManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case SENSOR:
                t = InstanceManager.getDefault(SensorManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case SIGNAL_HEAD:
                t = InstanceManager.getDefault(SignalHeadManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case SIGNAL_MAST:
                t = InstanceManager.getDefault(SignalMastManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case TURNOUT:
                t = InstanceManager.getDefault(TurnoutManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            default:
                throw new UnsupportedOperationException("_pointsTo has unknown value: "+_pointsTo.name());
        }
        
        if (_is_IsNot == Is_IsNot_Enum.IS) {
            return result;
        } else {
            return !result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing.
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
        return Bundle.getMessage(locale, "Reference_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String reference;
        if (_reference != null) {
            reference = _reference;
        } else {
            reference = Bundle.getMessage(locale, "ReferenceNotSelected");
        }
        return Bundle.getMessage(locale, "Reference_Long", reference, _is_IsNot.toString(), _pointsTo._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_reference != null)) {
//            _reference.getBean().addPropertyChangeListener("KnownState", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
//            _reference.getBean().removePropertyChangeListener("KnownState", this);
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
    
    
    
    public enum PointsTo {
        NOTHING(Bundle.getMessage("ReferencePointsTo_Nothing")),
        TABLE(Bundle.getMessage("ReferencePointsTo_Table")),
        AUDIO(Bundle.getMessage("ReferencePointsTo_Audio")),
        LIGHT(Bundle.getMessage("ReferencePointsTo_Light")),
        MEMORY(Bundle.getMessage("ReferencePointsTo_Memory")),
        SENSOR(Bundle.getMessage("ReferencePointsTo_Sensor")),
        SIGNAL_HEAD(Bundle.getMessage("ReferencePointsTo_SignalHead")),
        SIGNAL_MAST(Bundle.getMessage("ReferencePointsTo_SignalMast")),
        TURNOUT(Bundle.getMessage("ReferencePointsTo_Turnout"));
        
        private final String _text;
        
        private PointsTo(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionReference.class);
    
}
