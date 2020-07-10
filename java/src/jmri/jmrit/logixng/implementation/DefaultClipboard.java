package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.util.ArrayList;
import java.util.Locale;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;

/**
 * Default implementation of the clipboard
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class DefaultClipboard implements Clipboard {

    private final Many _clipboardItems = new Many("", null);
    
    private final FemaleAnySocket _femaleRootSocket = new DefaultFemaleAnySocket(null, new FemaleSocketListener() {
        @Override
        public void connected(FemaleSocket socket) {
            // Do nothing
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            // Do nothing
        }
    }, "");
    
    
    public DefaultClipboard() {
        try {
            _femaleRootSocket.connect(new MaleRootSocket());
        } catch (SocketAlreadyConnectedException ex) {
            // This should never happen
            throw new RuntimeException("Program error", ex);
        }
    }
    
    @Override
    public boolean isEmpty() {
        return _clipboardItems.getChildCount() == 0;
    }

    @Override
    public void add(MaleSocket maleSocket) {
        _clipboardItems.ensureFreeSocketAtTop();
        try {
            _clipboardItems.getChild(0).connect(maleSocket);
        } catch (SocketAlreadyConnectedException ex) {
            throw new RuntimeException("Cannot add socket", ex);
        }
    }
    
    @Override
    public MaleSocket fetchTopItem() {
        if (!isEmpty()) {
            MaleSocket maleSocket = _clipboardItems.getChild(0).getConnectedSocket();
            _clipboardItems.getChild(0).disconnect();
            return maleSocket;
        }
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public MaleSocket getTopItem() {
        if (!isEmpty()) {
            return _clipboardItems.getChild(0).getConnectedSocket();
        }
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public FemaleSocket getRoot() {
        return _femaleRootSocket;
    }

    @Override
    public void moveItemToTop(MaleSocket maleSocket) {
        _clipboardItems.ensureFreeSocketAtTop();
        if (maleSocket.getParent() != null) {
            if (!(maleSocket.getParent() instanceof FemaleSocket)) {
                throw new UnsupportedOperationException("maleSocket.parent() is not a female socket");
            }
            ((FemaleSocket)maleSocket.getParent()).disconnect();
        }
        try {
            _clipboardItems.getChild(0).connect(maleSocket);
        } catch (SocketAlreadyConnectedException ex) {
            throw new UnsupportedOperationException("Cannot move item to clipboard", ex);
        }
    }
    
    
    private class MaleRootSocket extends AbstractMaleSocket {

        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void disposeMe() {
            _clipboardItems.dispose();
        }

        @Override
        public void setEnabled(boolean enable) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isEnabled() {
            return _clipboardItems.isEnabled();
        }

        @Override
        public Base getObject() {
            return _clipboardItems;
        }

        @Override
        public void setDebugConfig(DebugConfig config) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public DebugConfig getDebugConfig() {
            return null;
        }

        @Override
        public DebugConfig createDebugConfig() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getSystemName() {
            return _clipboardItems.getSystemName();
        }

        @Override
        public String getUserName() {
            return _clipboardItems.getUserName();
        }

        @Override
        public void setUserName(String s) throws NamedBean.BadUserNameException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getShortDescription(Locale locale) {
            return _clipboardItems.getShortDescription(locale);
        }

        @Override
        public String getLongDescription(Locale locale) {
            return _clipboardItems.getLongDescription(locale);
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
            return this;
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            return _clipboardItems.getChild(index);
        }

        @Override
        public int getChildCount() {
            return _clipboardItems.getChildCount();
        }

        @Override
        public Category getCategory() {
            return _clipboardItems.getCategory();
        }

        @Override
        public boolean isExternal() {
            return _clipboardItems.isExternal();
        }

        @Override
        public Lock getLock() {
            return _clipboardItems.getLock();
        }

        @Override
        public void setLock(Lock lock) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
            _clipboardItems.addPropertyChangeListener(listener, name, listenerRef);
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
            _clipboardItems.addPropertyChangeListener(propertyName, listener, name, listenerRef);
        }

        @Override
        public void updateListenerRef(PropertyChangeListener l, String newName) {
            _clipboardItems.updateListenerRef(l, newName);
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            _clipboardItems.vetoableChange(evt);
        }

        @Override
        public String getListenerRef(PropertyChangeListener l) {
            return _clipboardItems.getListenerRef(l);
        }

        @Override
        public ArrayList<String> getListenerRefs() {
            return _clipboardItems.getListenerRefs();
        }

        @Override
        public int getNumPropertyChangeListeners() {
            return _clipboardItems.getNumPropertyChangeListeners();
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
            return _clipboardItems.getPropertyChangeListenersByReference(name);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            _clipboardItems.addPropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            _clipboardItems.addPropertyChangeListener(propertyName, listener);
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners() {
            return _clipboardItems.getPropertyChangeListeners();
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
            return _clipboardItems.getPropertyChangeListeners(propertyName);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            _clipboardItems.removePropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            _clipboardItems.removePropertyChangeListener(propertyName, listener);
        }
        
    }
    
}
