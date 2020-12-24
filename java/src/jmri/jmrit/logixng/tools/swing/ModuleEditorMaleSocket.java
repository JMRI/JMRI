package jmri.jmrit.logixng.tools.swing;

import java.beans.*;
import java.util.ArrayList;
import java.util.Locale;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * MaleSocket for a Module.
 * This class is used by the ModuleEditor class
 * 
 * @author Daniel Bergqvist 2020
 */
class ModuleEditorMaleSocket extends AbstractMaleSocket {
    
    Module _module;

    public ModuleEditorMaleSocket(BaseManager<? extends NamedBean> manager, Module module) {
        super(manager);
        _module = module;
    }

    @Override
    protected void registerListenersForThisClass() {
        // Do nothing
    }

    @Override
    protected void unregisterListenersForThisClass() {
        // Do nothing
    }

    @Override
    protected void disposeMe() {
        _module.dispose();
    }

    @Override
    public void setEnabled(boolean enable) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Base getObject() {
        return _module;
    }

    @Override
    public void setDebugConfig(DebugConfig config) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DebugConfig getDebugConfig() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DebugConfig createDebugConfig() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getSystemName() {
        return _module.getSystemName();
    }

    @Override
    public String getUserName() {
        return _module.getUserName();
    }

    @Override
    public String getComment() {
        return _module.getComment();
    }

    @Override
    public void setUserName(String s) throws NamedBean.BadUserNameException {
        _module.setUserName(s);
    }

    @Override
    public void setComment(String comment) {
        _module.setComment(comment);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return _module.getShortDescription(locale);
    }

    @Override
    public String getLongDescription(Locale locale) {
        return _module.getLongDescription(locale);
    }

    @Override
    public ConditionalNG getConditionalNG() {
        return null;
    }

    @Override
    public LogixNG getLogixNG() {
        return null;
    }

    @Override
    public Base getRoot() {
        return _module.getRoot();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _module.getChild(index);
    }

    @Override
    public int getChildCount() {
        return _module.getChildCount();
    }

    @Override
    public Category getCategory() {
        return _module.getCategory();
    }

    @Override
    public boolean isExternal() {
        return _module.isExternal();
    }

    @Override
    public Lock getLock() {
        return _module.getLock();
    }

    @Override
    public void setLock(Lock lock) {
        _module.setLock(lock);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
        _module.addPropertyChangeListener(listener, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
        _module.addPropertyChangeListener(propertyName, listener, name, listenerRef);
    }

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        _module.updateListenerRef(l, newName);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        _module.vetoableChange(evt);
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        return _module.getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        return _module.getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        return _module.getNumPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        return _module.getPropertyChangeListenersByReference(name);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        _module.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _module.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return _module.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _module.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        _module.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _module.removePropertyChangeListener(propertyName, listener);
    }
    
}
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalNGEditor.class);
