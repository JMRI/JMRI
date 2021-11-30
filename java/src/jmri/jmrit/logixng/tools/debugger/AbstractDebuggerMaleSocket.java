package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * Abstract debugger male socket
 * @author Daniel Bergqvist 2020
 */
public abstract class AbstractDebuggerMaleSocket extends AbstractMaleSocket {
    
    private final Debugger _debugger = InstanceManager.getDefault(Debugger.class);
//    protected final MaleSocket ((MaleSocket)getObject());
    
    private boolean _breakpointBefore = false;
    private boolean _breakpointAfter = false;
    
    private boolean _stepInto = true;
    private boolean _lastDoBreak = true;
    
    
    public AbstractDebuggerMaleSocket(BaseManager<? extends MaleSocket> manager, MaleSocket maleSocket) {
        super(manager, maleSocket);
    }
    
    /**
     * Get information about this action/expression before it is executed or
     * evaluated.
     * @return an information string
     */
    public abstract String getBeforeInfo();
    
    /**
     * Get information about this action/expression after it is executed or
     * evaluated.
     * @return an information string
     */
    public abstract String getAfterInfo();
    
    protected boolean isDebuggerActive() {
        return _debugger.isDebuggerActive()
                && (_debugger.getDebugConditionalNG() == this.getConditionalNG());
    }
    
    protected void before() {
        _lastDoBreak = _debugger.getBreak();
        if (isDebuggerActive() && (_debugger.getBreak() || _breakpointBefore)) {
//            System.out.format("Before: %s%n", getLongDescription());
            _debugger.firePropertyChange(Debugger.STEP_BEFORE, null, this);
            _debugger.setBreak(_stepInto);
        }
    }
    
    protected void after() {
        if (isDebuggerActive()) {
            _debugger.setBreak(_lastDoBreak);
            if (_debugger.getBreak() || _breakpointAfter) {
//                System.out.format("After: %s%n", getLongDescription());
                _debugger.firePropertyChange(Debugger.STEP_AFTER, null, this);
            }
        }
    }
    
    public void setStepInto(boolean value) {
        _stepInto = value;
    }
    
    public void setBreakpointBefore(boolean value) {
        _breakpointBefore = value;
    }
    
    public boolean getBreakpointBefore() {
        return _breakpointBefore;
    }
    
    public void setBreakpointAfter(boolean value) {
        _breakpointAfter = value;
    }
    
    public boolean getBreakpointAfter() {
        return _breakpointAfter;
    }
    
    @Override
    protected final void registerListenersForThisClass() {
        ((MaleSocket)getObject()).registerListeners();
    }

    @Override
    protected final void unregisterListenersForThisClass() {
        ((MaleSocket)getObject()).unregisterListeners();
    }

    @Override
    protected final void disposeMe() {
        ((MaleSocket)getObject()).dispose();
    }

    @Override
    public final void setEnabled(boolean enable) {
        ((MaleSocket)getObject()).setEnabled(enable);
    }

    @Override
    public void setEnabledFlag(boolean enable) {
        ((MaleSocket)getObject()).setEnabledFlag(enable);
    }

    @Override
    public final boolean isEnabled() {
        return ((MaleSocket)getObject()).isEnabled();
    }

    @Override
    public final void setDebugConfig(DebugConfig config) {
        ((MaleSocket)getObject()).setDebugConfig(config);
    }

    @Override
    public final DebugConfig getDebugConfig() {
        return ((MaleSocket)getObject()).getDebugConfig();
    }

    @Override
    public final DebugConfig createDebugConfig() {
        return ((MaleSocket)getObject()).createDebugConfig();
    }

    @Override
    public final String getComment() {
        return ((MaleSocket)getObject()).getComment();
    }

    @Override
    public final void setComment(String comment) {
        ((MaleSocket)getObject()).setComment(comment);
    }

    @Override
    public void setParent(Base parent) {
        super.setParent(parent);
        ((MaleSocket)getObject()).setParent(this);
    }

}
