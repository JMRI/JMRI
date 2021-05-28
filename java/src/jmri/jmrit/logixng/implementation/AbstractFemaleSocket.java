package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;

import org.apache.commons.lang3.mutable.MutableInt;

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
    private boolean _listenersAreRegistered = false;
    boolean _enableListeners = true;


    public AbstractFemaleSocket(Base parent, FemaleSocketListener listener, String name) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("the name is not valid: " + name);
        }
        if (listener == null) throw new IllegalArgumentException("FemaleSocketListener is null");
        _parent = parent;
        _listener = listener;
        _name = name;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableListeners(boolean enable) {
        _enableListeners = enable;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getEnableListeners() {
        return _enableListeners;
    }

    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return _parent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(@Nonnull Base parent) {
        _parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public boolean setParentForAllChildren(List<String> errors) {
        if (isConnected()) {
            getConnectedSocket().setParent(this);
            return getConnectedSocket().setParentForAllChildren(errors);
        }
        return true;
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
        pcs.firePropertyChange(new PropertyChangeEvent(this, Base.PROPERTY_SOCKET_CONNECTED, null, _socket));
//        pcs.firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, _socket);
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect() {
        MaleSocket maleSocket = _socket;

        if (_socket == null) {
            return;
        }

        if (_listenersAreRegistered) {
            throw new UnsupportedOperationException("A socket must not be disconnected when listeners are registered");
        }

        _socket.setParent(null);
        _socket = null;
        _listener.disconnected(this);
        pcs.firePropertyChange(new PropertyChangeEvent(this, Base.PROPERTY_SOCKET_DISCONNECTED, maleSocket, null));
//        pcs.firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, _socket);
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
        if (name.isEmpty()) return false;
        if (!Character.isLetter(name.charAt(0))) return false;
        for (int i=0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i)) && (name.charAt(i) != '_')) {
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
        _listener.socketNameChanged(this);
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
        if (!_enableListeners) return;

        _listenersAreRegistered = true;
        registerListenersForThisClass();
        if (isConnected()) {
            getConnectedSocket().registerListeners();
        }
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListeners() {
        if (!_enableListeners) return;

        unregisterListenersForThisClass();
        if (isConnected()) {
            getConnectedSocket().unregisterListeners();
        }
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((_parent == null) || _parent.isActive());
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
    public String getComment() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public void setComment(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public String getSystemName() {
        return getParent().getSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public final ConditionalNG getConditionalNG() {
        if (_parent == null) return null;
        return _parent.getConditionalNG();
    }

    /** {@inheritDoc} */
    @Override
    public final LogixNG getLogixNG() {
        if (_parent == null) return null;
        return _parent.getLogixNG();
    }

    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        if (_parent == null) return null;
        return _parent.getRoot();
    }

    protected void printTreeRow(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String currentIndent,
            MutableInt lineNumber) {
        
        if (settings._printLineNumbers) {
            writer.append(String.format(PRINT_LINE_NUMBERS_FORMAT, lineNumber.addAndGet(1)));
        }
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {
        
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {
        
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            String currentIndent,
            MutableInt lineNumber) {
        
        printTreeRow(settings, locale, writer, currentIndent, lineNumber);

        if (isConnected()) {
            getConnectedSocket().printTree(settings, locale, writer, indent, currentIndent+indent, lineNumber);
        } else {
            if (settings._printLineNumbers) {
                writer.append(String.format(PRINT_LINE_NUMBERS_FORMAT, lineNumber.addAndGet(1)));
            }
            writer.append(currentIndent);
            writer.append(indent);
            if (settings._printNotConnectedSockets) writer.append("Socket not connected");
            writer.println();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageTree(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("** {} :: {}", level, this.getLongDescription());
        level++;

        if (isConnected()) {
            getConnectedSocket().getUsageTree(level, bean, report, cdl);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
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
        return pcs.getPropertyChangeListeners().length;
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Do something on every item in the sub tree of this item.
     * @param r the action to do on all items.
     */
    @Override
    public void forEntireTree(RunnableWithBase r) {
        r.run(this);
        if (isConnected()) getConnectedSocket().forEntireTree(r);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }

   private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractFemaleSocket.class);

}
