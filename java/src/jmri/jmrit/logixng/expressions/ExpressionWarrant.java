package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;

/**
 * Evaluates the state of a Warrant.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionWarrant extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private Warrant _warrant;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private Type _type = Type.ROUTE_ALLOCATED;

    public ExpressionWarrant(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionWarrant copy = new ExpressionWarrant(sysName, userName);
        copy.setComment(getComment());
        copy.setType(_type);
        copy.set_Is_IsNot(_is_IsNot);
        if (_warrant != null) copy.setWarrant(_warrant);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setWarrant(@Nonnull String warrantName) {
        assertListenersAreNotRegistered(log, "setWarrant");
        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant(warrantName);
        if (warrant != null) {
            setWarrant(warrant);
        } else {
            removeWarrant();
            log.error("warrant \"{}\" is not found", warrantName);
        }
    }
    
    public void setWarrant(@Nonnull Warrant warrant) {
        assertListenersAreNotRegistered(log, "setWarrant");
        _warrant = warrant;
        InstanceManager.getDefault(WarrantManager.class).addVetoableChangeListener(this);
    }
    
    public void removeWarrant() {
        assertListenersAreNotRegistered(log, "setWarrant");
        if (_warrant != null) {
            InstanceManager.getDefault(WarrantManager.class).removeVetoableChangeListener(this);
            _warrant = null;
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
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Warrant_WarrantInUseWarrantExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Warrant) {
                if (evt.getOldValue().equals(getWarrant())) {
                    removeWarrant();
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
        return Bundle.getMessage(locale, "Warrant_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String warrantName;
        if (_warrant != null) {
            warrantName = _warrant.getDisplayName();
        } else {
            warrantName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Warrant_Long", warrantName, _is_IsNot.toString(), _type._text);
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
