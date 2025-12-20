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

    private boolean _breakpointBefore = false;
    private boolean _breakpointAfter = false;

    private boolean _stepInto = true;
    private boolean _lastDoBreak = true;

    private boolean _logBefore = false;
    private boolean _logAfter = false;


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

    protected boolean isLogAllBefore() {
        return InstanceManager.getDefault(LogixNGPreferences.class).getLogAllBefore();
    }

    protected boolean isLogAllAfter() {
        return InstanceManager.getDefault(LogixNGPreferences.class).getLogAllAfter();
    }

    protected boolean isDebuggerActive() {
        return _debugger.isDebuggerActive()
                && (_debugger.getDebugConditionalNG() == this.getConditionalNG());
    }

    protected void before() {
        if (isLogAllBefore() || _logBefore) {
            ConditionalNG cng = getConditionalNG();
            String info = getBeforeInfo();
            if (!info.isBlank()) {
                info = " --- " + info;
            }
            log.warn("LogixNG Before: {}, {}: {}{}", cng.getLogixNG().getSystemName(), cng.getSystemName(), getLongDescription(), info);
        }
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
        if (isLogAllAfter() || _logAfter) {
            ConditionalNG cng = getConditionalNG();
            String info = getAfterInfo();
            if (!info.isBlank()) {
                info = " --- " + info;
            }
            log.warn("LogixNG  After: {}, {}: {}{}", cng.getLogixNG().getSystemName(), cng.getSystemName(), getLongDescription(), info);
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

    public void setLogBefore(boolean value) {
        _logBefore = value;
    }

    public boolean getLogBefore() {
        return _logBefore;
    }

    public void setLogAfter(boolean value) {
        _logAfter = value;
    }

    public boolean getLogAfter() {
        return _logAfter;
    }

    @Override
    protected final void registerListenersForThisClass() {
        getObject().registerListeners();
    }

    @Override
    protected final void unregisterListenersForThisClass() {
        getObject().unregisterListeners();
    }

    @Override
    protected final void disposeMe() {
        getObject().dispose();
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
        return getObject().getComment();
    }

    @Override
    public final void setComment(String comment) {
        getObject().setComment(comment);
    }

    @Override
    public void setParent(Base parent) {
        super.setParent(parent);
        getObject().setParent(this);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractDebuggerMaleSocket.class);
}
