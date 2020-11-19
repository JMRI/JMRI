package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.util.ThreadingUtil;

/**
 * This action sets the state of a turnout.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionTurnout extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Turnout> _turnoutHandle;
    private TurnoutState _turnoutState = TurnoutState.Thrown;
    
    public ActionTurnout(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = systemNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionTurnout copy = new ActionTurnout(sysName, userName);
        copy.setTurnout(_turnoutHandle);
        copy.setTurnoutState(_turnoutState);
        return manager.registerAction(copy);
    }
    
    public void setTurnout(@Nonnull String turnoutName) {
        assertListenersAreNotRegistered(log, "setTurnout");
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName);
        if (turnout != null) {
            setTurnout(turnout);
        } else {
            removeTurnout();
            log.error("turnout \"{}\" is not found", turnoutName);
        }
    }
    
    public void setTurnout(@Nonnull NamedBeanHandle<Turnout> handle) {
        assertListenersAreNotRegistered(log, "setTurnout");
        _turnoutHandle = handle;
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setTurnout(@Nonnull Turnout turnout) {
        assertListenersAreNotRegistered(log, "setTurnout");
        setTurnout(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(turnout.getDisplayName(), turnout));
    }
    
    public void removeTurnout() {
        assertListenersAreNotRegistered(log, "setTurnout");
        if (_turnoutHandle != null) {
            InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
            _turnoutHandle = null;
        }
    }
    
    public NamedBeanHandle<Turnout> getTurnout() {
        return _turnoutHandle;
    }
    
    public void setTurnoutState(TurnoutState state) {
        _turnoutState = state;
    }
    
    public TurnoutState getTurnoutState() {
        return _turnoutState;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Turnout_TurnoutInUseTurnoutExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    removeTurnout();
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
    public void execute() {
        if (_turnoutHandle == null) return;
        
        final Turnout t = _turnoutHandle.getBean();
        ThreadingUtil.runOnLayout(() -> {
            if (_turnoutState == TurnoutState.Toggle) {
                if (t.getCommandedState() == Turnout.CLOSED) {
                    t.setCommandedState(Turnout.THROWN);
                } else {
                    t.setCommandedState(Turnout.CLOSED);
                }
            } else {
                t.setCommandedState(_turnoutState.getID());
            }
        });
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
        return Bundle.getMessage(locale, "Turnout_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String turnoutName;
        if (_turnoutHandle != null) {
            turnoutName = _turnoutHandle.getBean().getDisplayName();
        } else {
            turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Turnout_Long", turnoutName, _turnoutState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    
    // This constant is only used internally in TurnoutState but must be outside
    // the enum.
    private static final int TOGGLE_ID = -1;
    
    
    public enum TurnoutState {
        Closed(Turnout.CLOSED, InstanceManager.getDefault(TurnoutManager.class).getClosedText()),
        Thrown(Turnout.THROWN, InstanceManager.getDefault(TurnoutManager.class).getThrownText()),
        Toggle(TOGGLE_ID, Bundle.getMessage("TurnoutToggleStatus"));
        
        private final int _id;
        private final String _text;
        
        private TurnoutState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public TurnoutState get(int id) {
            switch (id) {
                case Turnout.CLOSED:
                    return Closed;
                    
                case Turnout.THROWN:
                    return Thrown;
                    
                case TOGGLE_ID:
                    return Toggle;
                    
                default:
                    throw new IllegalArgumentException("invalid turnout state");
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnout.class);
    
}
