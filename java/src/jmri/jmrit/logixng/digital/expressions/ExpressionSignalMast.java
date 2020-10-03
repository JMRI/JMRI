package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;

/**
 * Evaluates the state of a SignalMast.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSignalMast extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<SignalMast> _signalMastHandle;
    private QueryType _queryType = QueryType.Aspect;
    private String _signalMastAspect = "";
    private boolean _listenersAreRegistered = false;

    public ExpressionSignalMast(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setSignalMast(@Nonnull String signalHeadName) {
        SignalMast turnout = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalHeadName);
        setSignalMast(turnout);
        if (turnout == null) {
            log.error("turnout \"{}\" is not found", signalHeadName);
        }
    }
    
    public void setSignalMast(@Nonnull NamedBeanHandle<SignalMast> handle) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setSignalMast must not be called when listeners are registered");
            log.error("setSignalMast must not be called when listeners are registered", e);
            throw e;
        }
        _signalMastHandle = handle;
    }
    
    public void setSignalMast(@CheckForNull SignalMast turnout) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setSignalMast must not be called when listeners are registered");
            log.error("setSignalMast must not be called when listeners are registered", e);
            throw e;
        }
        if (turnout != null) {
            if (_signalMastHandle != null) {
                InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
            }
            _signalMastHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(turnout.getDisplayName(), turnout);
        } else {
            _signalMastHandle = null;
            InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<SignalMast> getSignalMast() {
        return _signalMastHandle;
    }
    
    public void setAspect(String aspect) {
        _signalMastAspect = aspect;
    }
    
    public String getAspect() {
        return _signalMastAspect;
    }
    
    public void setQueryType(QueryType state) {
        _queryType = state;
    }
    
    public QueryType getQueryType() {
        return _queryType;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalMast) {
                if (evt.getOldValue().equals(getSignalMast().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalMast) {
                if (evt.getOldValue().equals(getSignalMast().getBean())) {
                    setSignalMast((SignalMast)null);
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
        if (_signalMastHandle == null) return false;
        
        boolean result;
        
        switch (_queryType) {
            case Aspect:
                result = _signalMastHandle.getBean().getAspect().equals(_signalMastAspect);
                break;
            case NotAspect:
                result = ! (_signalMastHandle.getBean().getAspect().equals(_signalMastAspect));
                break;
            case Lit:
                result = _signalMastHandle.getBean().getLit();
                break;
            case NotLit:
                result = ! _signalMastHandle.getBean().getLit();
                break;
            case Held:
                result = _signalMastHandle.getBean().getHeld();
                break;
            case NotHeld:
                result = ! _signalMastHandle.getBean().getHeld();
                break;
            default:
                throw new RuntimeException("Unknown enum: "+_queryType.name());
        }
        
        return result;
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
        return Bundle.getMessage(locale, "SignalMast_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String turnoutName;
        if (_signalMastHandle != null) {
            turnoutName = _signalMastHandle.getBean().getDisplayName();
        } else {
            turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        String aspect;
        if ((_signalMastHandle != null) && (_signalMastHandle.getBean() != null)) {
            aspect = _signalMastHandle.getBean().getAspect();
        } else {
            aspect = "";
        }
        return Bundle.getMessage(locale, "SignalMast_Long", turnoutName, _queryType._text, aspect);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_signalMastHandle != null)) {
            
            switch (_queryType) {
                case Aspect:
                case NotAspect:
                    _signalMastHandle.getBean().addPropertyChangeListener("Aspect", this);
                    break;
                    
                case Lit:
                case NotLit:
                    _signalMastHandle.getBean().addPropertyChangeListener("Lit", this);
                    break;
                    
                case Held:
                case NotHeld:
                    _signalMastHandle.getBean().addPropertyChangeListener("Held", this);
                    break;
                    
                default:
                    throw new RuntimeException("Unknown enum: "+_queryType.name());
            }
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            
            switch (_queryType) {
                case Aspect:
                case NotAspect:
                    _signalMastHandle.getBean().removePropertyChangeListener("Aspect", this);
                    break;
                    
                case Lit:
                case NotLit:
                    _signalMastHandle.getBean().removePropertyChangeListener("Lit", this);
                    break;
                    
                case Held:
                case NotHeld:
                    _signalMastHandle.getBean().removePropertyChangeListener("Held", this);
                    break;
                    
                default:
                    throw new RuntimeException("Unknown enum: "+_queryType.name());
            }
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
    
    
    
    public enum QueryType {
        Aspect(Bundle.getMessage("SignalMastQueryType_Aspect")),
        NotAspect(Bundle.getMessage("SignalMastQueryType_NotAspect")),
        Lit(Bundle.getMessage("SignalMastQueryType_Lit")),
        NotLit(Bundle.getMessage("SignalMastQueryType_NotLit")),
        Held(Bundle.getMessage("SignalMastQueryType_Held")),
        NotHeld(Bundle.getMessage("SignalMastQueryType_NotHeld"));
        
        private final String _text;
        
        private QueryType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalMast.class);
    
}
