package jmri.jmrit.logixng.tools.debugger;

import java.beans.*;
import java.util.*;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * Abstract debugger male socket
 * @author Daniel Bergqvist 2020
 */
public class AbstractDebuggerMaleSocket extends AbstractMaleSocket {
    
    private final Debugger _debugger = InstanceManager.getDefault(Debugger.class);
    protected final MaleSocket _maleSocket;
    
    private boolean _breakpointBefore = false;
    private boolean _breakpointAfter = false;
    
    private boolean _stepInto = true;
    private boolean _lastDoBreak = true;
    
    
    public AbstractDebuggerMaleSocket(BaseManager<? extends MaleSocket> manager, MaleSocket maleSocket) {
        super(manager);
        _maleSocket = maleSocket;
    }

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
        _maleSocket.registerListeners();
    }

    @Override
    protected final void unregisterListenersForThisClass() {
        _maleSocket.unregisterListeners();
    }

    @Override
    protected final void disposeMe() {
        _maleSocket.dispose();
    }

    @Override
    public final void setEnabled(boolean enable) {
        _maleSocket.setEnabled(enable);
    }

    @Override
    public final boolean isEnabled() {
        return _maleSocket.isEnabled();
    }

    @Override
    public final Base getObject() {
        return _maleSocket;
    }

    @Override
    public final void setDebugConfig(DebugConfig config) {
        _maleSocket.setDebugConfig(config);
    }

    @Override
    public final DebugConfig getDebugConfig() {
        return _maleSocket.getDebugConfig();
    }

    @Override
    public final DebugConfig createDebugConfig() {
        return _maleSocket.createDebugConfig();
    }

    @Override
    public final String getSystemName() {
        return _maleSocket.getSystemName();
    }

    @Override
    public final String getUserName() {
        return _maleSocket.getUserName();
    }

    @Override
    public final String getComment() {
        return _maleSocket.getComment();
    }

    @Override
    public final void setUserName(String s) throws NamedBean.BadUserNameException {
        _maleSocket.setUserName(s);
    }

    @Override
    public final void setComment(String comment) {
        _maleSocket.setComment(comment);
    }

    @Override
    public final String getShortDescription(Locale locale) {
        return _maleSocket.getShortDescription(locale);
    }

    @Override
    public final String getLongDescription(Locale locale) {
        return _maleSocket.getLongDescription(locale);
    }

    @Override
    public void setParent(Base parent) {
        super.setParent(parent);
        _maleSocket.setParent(this);
    }

    @Override
    public final Base getRoot() {
        return _maleSocket.getRoot();
    }

    @Override
    public final FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _maleSocket.getChild(index);
    }

    @Override
    public final int getChildCount() {
        return _maleSocket.getChildCount();
    }

    @Override
    public final Category getCategory() {
        return _maleSocket.getCategory();
    }

    @Override
    public final boolean isExternal() {
        return _maleSocket.isExternal();
    }

    @Override
    public final Lock getLock() {
        return _maleSocket.getLock();
    }

    @Override
    public final void setLock(Lock lock) {
        _maleSocket.setLock(lock);
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
        _maleSocket.addPropertyChangeListener(listener, name, listenerRef);
    }

    @Override
    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
        _maleSocket.addPropertyChangeListener(propertyName, listener, name, listenerRef);
    }

    @Override
    public final void updateListenerRef(PropertyChangeListener l, String newName) {
        _maleSocket.updateListenerRef(l, newName);
    }

    @Override
    public final void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        _maleSocket.vetoableChange(evt);
    }

    @Override
    public final String getListenerRef(PropertyChangeListener l) {
        return _maleSocket.getListenerRef(l);
    }

    @Override
    public final ArrayList<String> getListenerRefs() {
        return _maleSocket.getListenerRefs();
    }

    @Override
    public final int getNumPropertyChangeListeners() {
        return _maleSocket.getNumPropertyChangeListeners();
    }

    @Override
    public final PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        return _maleSocket.getPropertyChangeListenersByReference(name);
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        _maleSocket.addPropertyChangeListener(listener);
    }

    @Override
    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _maleSocket.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public final PropertyChangeListener[] getPropertyChangeListeners() {
        return _maleSocket.getPropertyChangeListeners();
    }

    @Override
    public final PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _maleSocket.getPropertyChangeListeners(propertyName);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        _maleSocket.removePropertyChangeListener(listener);
    }

    @Override
    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _maleSocket.removePropertyChangeListener(propertyName, listener);
    }

}
