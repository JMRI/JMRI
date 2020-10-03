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
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;

/**
 * Evaluates the state of a SignalHead.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSignalHead extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<SignalHead> _signalHeadHandle;
    private QueryType _queryType = QueryType.Appearance;
    private int _signalHeadAppearance = SignalHead.DARK;
    private boolean _listenersAreRegistered = false;

    public ExpressionSignalHead(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setSignalHead(@Nonnull String signalHeadName) {
        SignalHead turnout = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName);
        setSignalHead(turnout);
        if (turnout == null) {
            log.error("turnout \"{}\" is not found", signalHeadName);
        }
    }
    
    public void setSignalHead(@Nonnull NamedBeanHandle<SignalHead> handle) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setSignalHead must not be called when listeners are registered");
            log.error("setSignalHead must not be called when listeners are registered", e);
            throw e;
        }
        _signalHeadHandle = handle;
    }
    
    public void setSignalHead(@CheckForNull SignalHead turnout) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setSignalHead must not be called when listeners are registered");
            log.error("setSignalHead must not be called when listeners are registered", e);
            throw e;
        }
        if (turnout != null) {
            if (_signalHeadHandle != null) {
                InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
            }
            _signalHeadHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(turnout.getDisplayName(), turnout);
        } else {
            _signalHeadHandle = null;
            InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<SignalHead> getSignalHead() {
        return _signalHeadHandle;
    }
    
    public void setAppearance(int appearance) {
        _signalHeadAppearance = appearance;
    }
    
    public int getAppearance() {
        return _signalHeadAppearance;
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
            if (evt.getOldValue() instanceof SignalHead) {
                if (evt.getOldValue().equals(getSignalHead().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalHead) {
                if (evt.getOldValue().equals(getSignalHead().getBean())) {
                    setSignalHead((SignalHead)null);
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
        if (_signalHeadHandle == null) return false;
        
        boolean result;
        
        switch (_queryType) {
            case Appearance:
                result = _signalHeadHandle.getBean().getAppearance() == _signalHeadAppearance;
                break;
            case NotAppearance:
                result = ! (_signalHeadHandle.getBean().getAppearance() == _signalHeadAppearance);
                break;
            case Lit:
                result = _signalHeadHandle.getBean().getLit();
                break;
            case NotLit:
                result = ! _signalHeadHandle.getBean().getLit();
                break;
            case Held:
                result = _signalHeadHandle.getBean().getHeld();
                break;
            case NotHeld:
                result = ! _signalHeadHandle.getBean().getHeld();
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
        return Bundle.getMessage(locale, "SignalHead_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String turnoutName;
        if (_signalHeadHandle != null) {
            turnoutName = _signalHeadHandle.getBean().getDisplayName();
        } else {
            turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        String appearence;
        if ((_signalHeadHandle != null) && (_signalHeadHandle.getBean() != null)) {
            appearence = _signalHeadHandle.getBean().getAppearanceName(_signalHeadAppearance);
        } else {
            appearence = "";
        }
        return Bundle.getMessage(locale, "SignalHead_Long", turnoutName, _queryType._text, appearence);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_signalHeadHandle != null)) {
            
            switch (_queryType) {
                case Appearance:
                case NotAppearance:
                    _signalHeadHandle.getBean().addPropertyChangeListener("Appearance", this);
                    break;
                    
                case Lit:
                case NotLit:
                    _signalHeadHandle.getBean().addPropertyChangeListener("Lit", this);
                    break;
                    
                case Held:
                case NotHeld:
                    _signalHeadHandle.getBean().addPropertyChangeListener("Held", this);
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
                case Appearance:
                case NotAppearance:
                    _signalHeadHandle.getBean().removePropertyChangeListener("Appearance", this);
                    break;
                    
                case Lit:
                case NotLit:
                    _signalHeadHandle.getBean().removePropertyChangeListener("Lit", this);
                    break;
                    
                case Held:
                case NotHeld:
                    _signalHeadHandle.getBean().removePropertyChangeListener("Held", this);
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
        Appearance(Bundle.getMessage("SignalHeadQueryType_Appearance")),
        NotAppearance(Bundle.getMessage("SignalHeadQueryType_NotAppearance")),
        Lit(Bundle.getMessage("SignalHeadQueryType_Lit")),
        NotLit(Bundle.getMessage("SignalHeadQueryType_NotLit")),
        Held(Bundle.getMessage("SignalHeadQueryType_Held")),
        NotHeld(Bundle.getMessage("SignalHeadQueryType_NotHeld"));
        
        private final String _text;
        
        private QueryType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalHead.class);
    
}
