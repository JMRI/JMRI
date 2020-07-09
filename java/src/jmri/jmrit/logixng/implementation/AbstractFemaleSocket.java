package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;

/**
 * Abstract female socket.
 * 
 * @author Daniel Bergqvist 2019
 */
public abstract class AbstractFemaleSocket implements FemaleSocket, InternalBase {
    
    private Base _parent;
    protected final FemaleSocketListener _listener;
    private MaleSocket _socket = null;
    private String _name = null;
    private boolean _listenersAreRegistered = false;
    
    
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
        if (isConnected()) {
            getConnectedSocket().setParent(this);
            getConnectedSocket().setParentForAllChildren();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Lock getLock() {
        if (isConnected()) {
            return getConnectedSocket().getLock();
        } else {
            throw new UnsupportedOperationException("Socket is not connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setLock(Lock lock) {
        if (isConnected()) {
            getConnectedSocket().setLock(lock);
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
        
        if (_listenersAreRegistered) {
            throw new UnsupportedOperationException("A socket must not be connected when listeners are registered");
        }
        
        if (isConnected()) {
            throw new SocketAlreadyConnectedException("Socket is already connected");
        }
        
        if (!isCompatible(socket)) {
            throw new IllegalArgumentException("Socket "+socket.getClass().getName()+" is not compatible with "+this.getClass().getName());
//            throw new IllegalArgumentException("Socket "+socket.getClass().getName()+" is not compatible with "+this.getClass().getName()+". Socket.getObject: "+socket.getObject().getClass().getName());
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
        
        if (_listenersAreRegistered) {
            throw new UnsupportedOperationException("A socket must not be disconnected when listeners are registered");
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
        if (_listenersAreRegistered) {
            throw new UnsupportedOperationException("This is not currently supported");
        }
        
        if (isConnected()) {
            MaleSocket aSocket = getConnectedSocket();
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
        _listenersAreRegistered = true;
        registerListenersForThisClass();
        if (isConnected()) {
            ((InternalBase)getConnectedSocket()).registerListeners();
        }
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListeners() {
        unregisterListenersForThisClass();
        if (isConnected()) {
            ((InternalBase)getConnectedSocket()).unregisterListeners();
        }
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isActive());
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
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public final LogixNG getLogixNG() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    protected void printTreeRow(Locale locale, PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);

        if (isConnected()) {
            getConnectedSocket().printTree(locale, writer, indent, currentIndent+indent);
        } else {
            writer.append(currentIndent);
            writer.append(indent);
            writer.append("Socket not connected");
            writer.println();
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getNumPropertyChangeListeners() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        throw new UnsupportedOperationException("Not supported");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractFemaleSocket.class);
    
}
