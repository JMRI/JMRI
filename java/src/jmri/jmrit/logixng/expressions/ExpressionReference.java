package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;
import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;

/**
 * Evaluates what a reference points to.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionReference extends AbstractDigitalExpression
        implements PropertyChangeListener {
//        implements PropertyChangeListener, VetoableChangeListener {

    private String _reference;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private PointsTo _pointsTo = PointsTo.Nothing;

    public ExpressionReference(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionReference copy = new ExpressionReference(sysName, userName);
        copy.setComment(getComment());
        copy.setReference(_reference);
        copy.set_Is_IsNot(_is_IsNot);
        copy.setPointsTo(_pointsTo);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
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
    
    public void setPointsTo(PointsTo pointsTo) {
        _pointsTo = pointsTo;
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
        String ref = ReferenceUtil.getReference(
                getConditionalNG().getSymbolTable(), _reference);
        NamedBean t;

        switch (_pointsTo) {
            case Nothing:
                result = "".equals(ref);
                break;
            
            case LogixNGTable:
                t = InstanceManager.getDefault(NamedTableManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case Audio:
                t = InstanceManager.getDefault(AudioManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case Light:
                t = InstanceManager.getDefault(LightManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case Memory:
                t = InstanceManager.getDefault(MemoryManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case Sensor:
                t = InstanceManager.getDefault(SensorManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case SignalHead:
                t = InstanceManager.getDefault(SignalHeadManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case SignalMast:
                t = InstanceManager.getDefault(SignalMastManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            case Turnout:
                t = InstanceManager.getDefault(TurnoutManager.class).getNamedBean(ref);
                result = (t != null);
                break;
            
            default:
                throw new UnsupportedOperationException("_pointsTo has unknown value: "+_pointsTo.name());
        }
        
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return result;
        } else {
            return !result;
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
        
        return Bundle.getMessage(
                locale,
                "Reference_Long",
                reference.isEmpty() ? "''" : reference,
                _is_IsNot.toString(),
                _pointsTo._text);
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
        getConditionalNG().execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum PointsTo {
        Nothing(Bundle.getMessage("ReferencePointsTo_Nothing")),
        Audio(Bundle.getMessage("ReferencePointsTo_Audio")),
        Light(Bundle.getMessage("ReferencePointsTo_Light")),
        Memory(Bundle.getMessage("ReferencePointsTo_Memory")),
        Sensor(Bundle.getMessage("ReferencePointsTo_Sensor")),
        SignalHead(Bundle.getMessage("ReferencePointsTo_SignalHead")),
        SignalMast(Bundle.getMessage("ReferencePointsTo_SignalMast")),
        Turnout(Bundle.getMessage("ReferencePointsTo_Turnout")),
        LogixNGTable(Bundle.getMessage("ReferencePointsTo_LogixNGTable"));
        
        private final String _text;
        
        private PointsTo(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionReference.class);
    
}
