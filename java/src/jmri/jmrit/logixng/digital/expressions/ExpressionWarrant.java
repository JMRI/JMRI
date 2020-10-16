package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;

/**
 * Evaluates the state of a Warrant.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionWarrant extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private Warrant _warrant;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.IS;
    private Type _type = Type.ROUTE_ALLOCATED;
    private boolean _listenersAreRegistered = false;

    public ExpressionWarrant(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setWarrant(@Nonnull String warrantName) {
        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant(warrantName);
        setWarrant(warrant);
        if (warrant == null) {
            log.error("conditional \"{}\" is not found", warrantName);
        }
    }
    
    public void setWarrant(@CheckForNull Warrant warrant) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setWarrant must not be called when listeners are registered");
            log.error("setWarrant must not be called when listeners are registered", e);
            throw e;
        }
        if (warrant != null) {
            InstanceManager.getDefault(WarrantManager.class).addVetoableChangeListener(this);
            _warrant = warrant;
        } else {
            _warrant = null;
            InstanceManager.getDefault(WarrantManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public Warrant getWarrant() {
        return _warrant;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setType(Type state) {
        _type = state;
    }
    
    public Type getType() {
        return _type;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Warrant) {
                if (evt.getOldValue().equals(getWarrant())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Warrant) {
                if (evt.getOldValue().equals(getWarrant())) {
                    setWarrant((Warrant)null);
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
        if (_warrant == null) return false;
        
        boolean result;
        
        switch (_type) {
            case ROUTE_FREE:
                result = _warrant.routeIsFree();
                break;
            case ROUTE_OCCUPIED:
                result = _warrant.routeIsOccupied();
                break;
            case ROUTE_ALLOCATED:
                result = _warrant.isAllocated();
                break;
            case ROUTE_SET:
                result = _warrant.hasRouteSet();
                break;
            case TRAIN_RUNNING:
                result = ! (_warrant.getRunMode() == Warrant.MODE_NONE);
                break;
            default:
                throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
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
        return Bundle.getMessage(locale, "Warrant_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String conditionalName;
        if (_warrant != null) {
            conditionalName = _warrant.getDisplayName();
        } else {
            conditionalName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Warrant_Long", conditionalName, _is_IsNot.toString(), _type._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_warrant != null)) {
            _warrant.addPropertyChangeListener(null, this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _warrant.removePropertyChangeListener(null, this);
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
    
    
    
    public enum Type {
        ROUTE_FREE(Bundle.getMessage("WarrantTypeRouteFree")),
        ROUTE_OCCUPIED(Bundle.getMessage("WarrantTypeOccupied")),
        ROUTE_ALLOCATED(Bundle.getMessage("WarrantTypeAllocated")),
        ROUTE_SET(Bundle.getMessage("WarrantTypeRouteSet")),
        TRAIN_RUNNING(Bundle.getMessage("WarrantTypeTrainRunning"));
        
        private final String _text;
        
        private Type(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionWarrant.class);
    
}
