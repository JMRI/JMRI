package jmri.jmrit.logixng.tools.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.beans.PropertyChangeProvider;
import jmri.jmrit.logixng.ConditionalNG;

/**
 * LogixNG Debugger
 * <P>
 * This class is _not_ thread safe. It must be called on the thread that is
 * used for the conditionalNG that the debugger is activated for.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class Debugger implements PropertyChangeProvider {
    
    public static final String STEP_BEFORE = "StepBefore";
    public static final String STEP_AFTER = "StepAfter";
    
    private ConditionalNG _debugConditionalNG = null;
    private boolean _break = false;
    
    private final PropertyChangeSupport _pcs = new PropertyChangeSupport(this);
    
    
    
    /*
    Breakpoint before/after - Always stop
    Step over - 
    Step into - 
    */
    
    public void activateDebugger(ConditionalNG conditionalNG) {
        if (_debugConditionalNG != null) {
            throw new IllegalStateException("Debugger is already active");
        }
        _debugConditionalNG = conditionalNG;
    }
    
    public void deActivateDebugger() {
        _debugConditionalNG = null;
    }
    
    public boolean isDebuggerActive() {
        return _debugConditionalNG != null;
    }
    
    public ConditionalNG getDebugConditionalNG() {
        return _debugConditionalNG;
    }
    
    public void setBreak(boolean value) {
        _break = value;
    }
    
    public boolean getBreak() {
        return _break;
    }
    
    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        _pcs.firePropertyChange(p, old, n);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        _pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return _pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _pcs.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        _pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _pcs.removePropertyChangeListener(propertyName, listener);
    }
    
}
