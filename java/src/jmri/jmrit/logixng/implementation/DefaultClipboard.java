package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Default implementation of the clipboard
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class DefaultClipboard extends AbstractBase implements Clipboard {

    private ClipboardMany _clipboardItems = new ClipboardMany("", null);
    
    private final FemaleAnySocket _femaleSocket = new DefaultFemaleAnySocket(this, new FemaleSocketListener() {
        @Override
        public void connected(FemaleSocket socket) {
            // Do nothing
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            // Do nothing
        }
    }, "A");
    
    
    public DefaultClipboard() {
        super("IQClipboard");
        
        // Listeners should never be enabled for the clipboard
        _femaleSocket.setEnableListeners(false);
        
        try {
            _femaleSocket.connect(new MaleRootSocket(null));
        } catch (SocketAlreadyConnectedException ex) {
            // This should never happen
            throw new RuntimeException("Program error", ex);
        }
        _femaleSocket.setParentForAllChildren();
        _clipboardItems.setParent(_femaleSocket.getConnectedSocket());
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
            _clipboardItems.setParentForAllChildren();
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
    public FemaleSocket getFemaleSocket() {
        return _femaleSocket;
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

    @Override
    public void setup() {
        _clipboardItems.setup();
    }
    
    public void replaceClipboardItems(ClipboardMany clipboardItems) {
        _clipboardItems = clipboardItems;
        
        _femaleSocket.disconnect();
        
        try {
            _femaleSocket.connect(new MaleRootSocket(null));
        } catch (SocketAlreadyConnectedException ex) {
            // This should never happen
            throw new RuntimeException("Program error", ex);
        }
        _femaleSocket.setParentForAllChildren();
        _clipboardItems.setParent(_femaleSocket.getConnectedSocket());
    }

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
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getBeanType() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getShortDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getLongDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base getParent() {
        return null;
    }

    @Override
    public void setParent(Base parent) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Lock getLock() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setLock(Lock lock) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    
    private class MaleRootSocket extends AbstractMaleSocket {

        public MaleRootSocket(BaseManager<? extends NamedBean> manager) {
            super(manager, _clipboardItems);
        }
        
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
        public void setEnabledFlag(boolean enable) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isEnabled() {
            return _clipboardItems.isEnabled();
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
        public String getComment() {
            return _clipboardItems.getComment();
        }

        @Override
        public void setComment(String s) throws NamedBean.BadUserNameException {
            throw new UnsupportedOperationException("Not supported");
        }

    }
    
}
