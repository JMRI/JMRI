package jmri.jmrit.logixng.tools.debugger;

import jmri.jmrit.logixng.ConditionalNG;

/**
 * LogixNG Debugger
 * <P>
 * This class is _not_ thread safe. It must be called on the thread that is
 * used for the conditionalNG that the debugger is activated for.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class Debugger {
    
    private ConditionalNG _debugConditionalNG = null;
    
    
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
    
}
