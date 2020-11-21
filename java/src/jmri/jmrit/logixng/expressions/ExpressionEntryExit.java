package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logixng.*;

/**
 * Evaluates the state of a DestinationPoints.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionEntryExit extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private DestinationPoints _dp;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private EntryExitState _entryExitState = EntryExitState.ACTIVE;

    public ExpressionEntryExit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionEntryExit copy = new ExpressionEntryExit(sysName, userName);
        copy.setComment(getComment());
        copy.set_Is_IsNot(_is_IsNot);
        if (_dp != null) copy.setDestinationPoints(_dp);
        copy.setEntryExitState(_entryExitState);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setDestinationPoints(@Nonnull String destinationPointsName) {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        DestinationPoints destinationPoints =
                InstanceManager.getDefault(EntryExitPairs.class)
                        .getBySystemName(destinationPointsName);
        if (destinationPoints != null) {
            setDestinationPoints(destinationPoints);
        } else {
            removeDestinationPoints();
            log.error("destinationPoints \"{}\" is not found", destinationPointsName);
        }
    }
    
    public void setDestinationPoints(@Nonnull DestinationPoints destinationPoints) {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        _dp = destinationPoints;
        InstanceManager.getDefault(EntryExitPairs.class).addVetoableChangeListener(this);
    }
    
    public void removeDestinationPoints() {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        if (_dp != null) {
            InstanceManager.getDefault(EntryExitPairs.class).removeVetoableChangeListener(this);
            _dp = null;
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
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("EntryExit_DestinationPointsInUseEntryExitExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getDestinationPoints())) {
                    removeDestinationPoints();
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
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentEntryExitState == _entryExitState;
        } else {
            return currentEntryExitState != _entryExitState;
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionEntryExit.class);
    
}
