package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates what a reference points to.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionReference extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private String _reference;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.IS;
    private PointsTo _pointsTo = PointsTo.NOTHING;
    private boolean _listenersAreRegistered = false;

    public ExpressionReference(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setReference(String reference) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setTurnout must not be called when listeners are registered");
            log.error("setTurnout must not be called when listeners are registered", e);
            throw e;
        }
        _reference = reference;
    }
    
    public String getRefernce() {
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
        boolean result;
        Object ref = null;
//        Object ref = getReference(_reference);

        switch (_pointsTo) {
            case NOTHING:
                result = "".equals(ref);
                break;
            
            case VECTOR:
                result = (ref instanceof String) && !"".equals(ref);
                break;
            
            case AUDIO:
                result = ref instanceof jmri.Audio;
                break;
            
            case LIGHT:
                result = ref instanceof jmri.Light;
                break;
            
            case MEMORY:
                result = ref instanceof jmri.Memory;
                break;
            
            case SENSOR:
                result = ref instanceof jmri.Sensor;
                break;
            
            case SIGNAL_HEAD:
                result = ref instanceof jmri.SignalHead;
                break;
            
            case SIGNAL_MAST:
                result = ref instanceof jmri.SignalMast;
                break;
            
            case TURNOUT:
                result = ref instanceof jmri.Turnout;
                break;
            
            default:
                throw new UnsupportedOperationException("_pointsTo has unknown value: "+_pointsTo.name());
        }
        
        if (_is_IsNot == Is_IsNot_Enum.IS) {
            return !result;
        } else {
            return result;
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
        getConditionalNG().execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum PointsTo {
        NOTHING(Bundle.getMessage("PointsTo_Nothing")),
        VECTOR(Bundle.getMessage("PointsTo_Vector")),
        AUDIO(Bundle.getMessage("PointsTo_Audio")),
        LIGHT(Bundle.getMessage("PointsTo_Light")),
        MEMORY(Bundle.getMessage("PointsTo_Memory")),
        SENSOR(Bundle.getMessage("PointsTo_Sensor")),
        SIGNAL_HEAD(Bundle.getMessage("PointsTo_SignalHead")),
        SIGNAL_MAST(Bundle.getMessage("PointsTo_SignalMast")),
        LOGIX(Bundle.getMessage("PointsTo_Logix")),
        LOGIX_NG(Bundle.getMessage("PointsTo_LogixNG")),
        SCRIPT(Bundle.getMessage("PointsTo_Script")),
        TURNOUT(Bundle.getMessage("PointsTo_Turnout")),
        LOGIX1(Bundle.getMessage("PointsTo_")),
        LOGIX2(Bundle.getMessage("PointsTo_")),
        LOGIX3(Bundle.getMessage("PointsTo_")),
        LOGIX4(Bundle.getMessage("PointsTo_")),
        LOGIX5(Bundle.getMessage("PointsTo_")),
        NOT_MATCH_REGEX(Bundle.getMessage("PointsTo_"));
        
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
