package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates the state of a DestinationPoints.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionEntryExit extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private DestinationPoints _dp;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.IS;
    private EntryExitState _entryExitState = EntryExitState.ACTIVE;

    public ExpressionEntryExit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setDestinationPoints(@Nonnull String systemName) {
        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getBySystemName(systemName);
        setDestinationPoints(dp);
        if (dp == null) {
            log.error("sensor \"{}\" is not found", systemName);
        }
    }
    
    public void setDestinationPoints(@CheckForNull DestinationPoints dp) {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        if (dp != null) {
            InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
            _dp = dp;
        } else {
            _dp = null;
            InstanceManager.sensorManagerInstance().removeVetoableChangeListener(this);
        }
    }
    
    public DestinationPoints getDestinationPoints() {
        return _dp;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setEntryExitState(EntryExitState state) {
        _entryExitState = state;
    }
    
    public EntryExitState getEntryExitState() {
        return _entryExitState;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getDestinationPoints())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getDestinationPoints())) {
                    setDestinationPoints((DestinationPoints)null);
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
        if (_dp == null) return false;
        
        EntryExitState currentEntryExitState = EntryExitState.get(_dp.getState());
        if (_is_IsNot == Is_IsNot_Enum.IS) {
            return currentEntryExitState == _entryExitState;
        } else {
            return currentEntryExitState != _entryExitState;
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
        return Bundle.getMessage(locale, "EntryExit_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String sensorName;
        if (_dp != null) {
            sensorName = _dp.getDisplayName();
        } else {
            sensorName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "EntryExit_Long", sensorName, _is_IsNot.toString(), _entryExitState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_dp != null)) {
            _dp.addPropertyChangeListener("active", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _dp.removePropertyChangeListener("active", this);
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
    
    
    
    public enum EntryExitState {
        INACTIVE(0x04, Bundle.getMessage("EntryExitStateInactive")),
        ACTIVE(0x02, Bundle.getMessage("EntryExitStateActive")),
        OTHER(-1, Bundle.getMessage("EntryExitOtherStatus"));
        
        private final int _id;
        private final String _text;
        
        private EntryExitState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public EntryExitState get(int id) {
            switch (id) {
                case 0x04:
                    return INACTIVE;
                    
                case 0x02:
                    return ACTIVE;
                    
                default:
                    return OTHER;
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
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionEntryExit.class);
    
}
