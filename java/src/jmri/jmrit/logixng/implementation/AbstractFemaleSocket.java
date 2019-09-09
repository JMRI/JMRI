package jmri.jmrit.logixng.implementation;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Locale;
import jmri.NamedBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Abstract female socket.
 * 
 * @author Daniel Bergqvist 2019
 */
public abstract class AbstractFemaleSocket implements FemaleSocket {
    
    private Base _parent;
    protected final FemaleSocketListener _listener;
    private MaleSocket _socket = null;
    private String _name = null;
    
    
    public AbstractFemaleSocket(Base parent, FemaleSocketListener listener, String name) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("the name is not valid: " + name);
        }
        _parent = parent;
        _listener = listener;
        _name = name;
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return _parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParentForAllChildren() {
        if (_socket != null) {
            _socket.setParent(this);
            _socket.setParentForAllChildren();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Lock getLock() {
        if (_socket != null) {
            return _socket.getLock();
        } else {
            throw new UnsupportedOperationException("Socket is not connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setLock(Lock lock) {
        if (_socket != null) {
            _socket.setLock(lock);
        } else {
            throw new UnsupportedOperationException("Socket is not connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void connect(MaleSocket socket) throws SocketAlreadyConnectedException {
        if (socket == null) {
            throw new NullPointerException("socket cannot be null");
        }
        
        if (_socket != null) {
            throw new SocketAlreadyConnectedException("Socket is already connected");
        }
        
        if (!isCompatible(socket)) {
            throw new UnsupportedOperationException("Socket is not compatible");
        }
        
        _socket = socket;
        _socket.setParent(this);
        _listener.connected(this);
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect() {
        if (_socket == null) {
            return;
        }
        
        _socket.setParent(null);
        _socket = null;
        _listener.disconnected(this);
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket getConnectedSocket() {
        return _socket;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isConnected() {
        return _socket != null;
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean validateName(String name) {
        for (int i=0; i < name.length(); i++) {
            if ((i == 0) && !Character.isLetter(name.charAt(i))) {
                return false;
            } else if (!Character.isLetterOrDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("the name is not valid: " + name);
        }
        _name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return _name;
    }

    abstract public void disposeMe();
    
    /** {@inheritDoc} */
    @Override
    public final void dispose() {
        if (_socket != null) {
            MaleSocket aSocket = _socket;
            disconnect();
            aSocket.dispose();
        }
        disposeMe();
    }

    /**
     * Register listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    protected void registerListenersForThisClass() {
        // Do nothing
    }
    
    /**
     * Unregister listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    protected void unregisterListenersForThisClass() {
        // Do nothing
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListeners() {
        registerListenersForThisClass();
        if (_socket != null) {
            _socket.registerListeners();
        }
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListeners() {
        unregisterListenersForThisClass();
        if (_socket != null) {
            _socket.unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isEnabled());
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public void setUserName(String s) throws NamedBean.BadUserNameException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public String getSystemName() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public final ConditionalNG getConditionalNG() {
        Base parent = getParent();
        while ((parent != null) && !(parent instanceof ConditionalNG)) {
            parent = parent.getParent();
        }
        return (ConditionalNG) parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public final LogixNG getLogixNG() {
        Base parent = getParent();
        while ((parent != null) && !(parent instanceof LogixNG)) {
            parent = parent.getParent();
        }
        return (LogixNG) parent;
    }
    
    /** {@inheritDoc} */
    protected void printTreeRow(Locale locale, PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(Locale.getDefault(), writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        printTree(locale, writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);

        if (_socket != null) {
            _socket.printTree(locale, writer, indent, currentIndent+indent);
        }
    }
    
}
