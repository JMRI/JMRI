package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Have many items of any type.
 * <P>
 * This class is used by the clipboard.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Many extends AbstractBase
        implements FemaleSocketListener {

    private final List<ItemEntry> _itemEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;
    
    public Many(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _itemEntries.add(new ItemEntry(new DefaultFemaleAnySocket(this, this, getNewSocketName())));
    }

    public Many(String sys, String user, List<ItemData> itemSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setItemSystemNames(itemSystemNames);
    }
    
    private void setItemSystemNames(List<ItemData> systemNamesAndClasses) {
        if (!_itemEntries.isEmpty()) {
            throw new RuntimeException("action system names cannot be set more than once");
        }
        
        for (ItemData itemData : systemNamesAndClasses) {
            FemaleAnySocket socket =
                    new DefaultFemaleAnySocket(this, this, itemData._systemName);
            
            _itemEntries.add(new ItemEntry(socket, itemData._className, itemData._socketName));
        }
    }

    public String getItemSystemName(int index) {
        return _itemEntries.get(index)._socketSystemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // We don't want to check for unconnected sockets while setup sockets
        disableCheckForUnconnectedSocket = true;
        
        for (ItemEntry ae : _itemEntries) {
            try {
                if ( !ae._socket.isConnected()
                        || !ae._socket.getConnectedSocket().getSystemName()
                                .equals(ae._socketSystemName)) {

                    String socketSystemName = ae._socketSystemName;
                    ae._socket.disconnect();
                    if (socketSystemName != null) {
                        NamedBean namedBean =
                                ((Manager)InstanceManager.getDefault(Class.forName(ae._itemManagerClass)))
                                        .getBySystemName(socketSystemName);
                        
                        if (namedBean != null) {
                            if (namedBean instanceof MaleSocket) {
                                MaleSocket maleSocket = (MaleSocket)namedBean;
                                ae._socket.connect(maleSocket);
                                maleSocket.setup();
                            } else {
                                log.error("item " + socketSystemName + " is not a male socket");
                            }
                        } else {
                            log.error("cannot load item " + socketSystemName);
                        }
                    }
                } else {
                    ae._socket.getConnectedSocket().setup();
                }
            } catch (SocketAlreadyConnectedException ex) {
                // This shouldn't happen and is a runtime error if it does.
                throw new RuntimeException("socket is already connected");
            } catch (ClassNotFoundException ex) {
                log.error("cannot load class " + ae._itemManagerClass, ex);
            }
        }
        
        checkFreeSocket();
        
        disableCheckForUnconnectedSocket = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _itemEntries.get(index)._socket;
    }

    @Override
    public int getChildCount() {
        return _itemEntries.size();
    }
    
    private void checkFreeSocket() {
        int numChilds = getChildCount();
        boolean hasFreeSocket = false;
        
        for (ItemEntry entry : _itemEntries) {
            hasFreeSocket |= !entry._socket.isConnected();
        }
        if (!hasFreeSocket) {
            _itemEntries.add(new ItemEntry(
                    new DefaultFemaleAnySocket(this, this, getNewSocketName())));
        }
        
        if (numChilds != getChildCount()) {
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, this);
        }
    }
    
    @Override
    public void connected(FemaleSocket socket) {
        if (disableCheckForUnconnectedSocket) return;
        
        for (ItemEntry entry : _itemEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
        
        checkFreeSocket();
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ItemEntry entry : _itemEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                break;
            }
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Many_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Many_Long");
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
    public Base getParent() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setParent(Base parent) {
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
    
    
    private static class ItemEntry {
        private String _socketSystemName;
        private String _itemManagerClass;
        private final FemaleAnySocket _socket;
        
        private ItemEntry(FemaleAnySocket socket, String itemManagerClass, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _itemManagerClass = itemManagerClass;
            _socket = socket;
        }
        
        private ItemEntry(FemaleAnySocket socket) {
            this._socket = socket;
        }
        
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing
    }
    
    private String getNewSocketName() {
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            for (int i=0; i < getChildCount(); i++) {
//                String name = "*" + Integer.toString(x);
                String name = "X" + Integer.toString(x);
                if (name.equals(getChild(i).getName())) {
                    validName = false;
                    break;
                }
            }
            if (validName) {
//                return "*" + Integer.toString(x);
                return "X" + Integer.toString(x);
            }
            x++;
        }
        throw new RuntimeException("Unable to find a new socket name");
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    public static class ItemData {
        
        public final String _systemName;
        public final String _className;
        public final String _socketName;
        
        public ItemData(String systemName, String className, String socketName) {
            _systemName = systemName;
            _className = className;
            _socketName = socketName;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Many.class);

}
