package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Have many items of any type.
 * <P>
 * This class is used by the clipboard.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ClipboardMany extends AbstractBase
        implements FemaleSocketListener {

    private Base _parent;
    private final List<ItemEntry> _itemEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;
    
    public ClipboardMany(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _itemEntries.add(new ItemEntry(new DefaultFemaleAnySocket(this, this, getNewSocketName())));
    }

    public ClipboardMany(String sys, String user, List<ItemData> itemSystemNames)
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
                    new DefaultFemaleAnySocket(this, this, itemData._socketName);
            
            _itemEntries.add(new ItemEntry(socket, itemData._className, itemData._systemName));
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
                                InstanceManager.getDefault(LogixNG_Manager.class)
                                        .getManager(ae._itemManagerClass).getBySystemName(socketSystemName);
                        
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
            }
        }
        
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
    
    public void ensureFreeSocketAtTop() {
        if (_itemEntries.get(0)._socket.isConnected()) {
            DefaultFemaleAnySocket socket =
                    new DefaultFemaleAnySocket(this, this, getNewSocketName());
            _itemEntries.add(0, new ItemEntry(socket));
            
            List<FemaleSocket> list = new ArrayList<>();
            list.add(socket);
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, list);
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
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (int i=0; i < _itemEntries.size(); i++) {
            ItemEntry entry = _itemEntries.get(i);
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                
                // Remove socket if not the first socket
                if (i > 0) {
                    List<FemaleSocket> list = new ArrayList<>();
                    list.add(socket);
                    _itemEntries.remove(i);
                    firePropertyChange(Base.PROPERTY_CHILD_COUNT, list, null);
                }
                break;
            }
        }
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
        return _parent;
    }

    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
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
        
        public ItemData(String socketName, String systemName, String className) {
            _systemName = systemName;
            _className = className;
            _socketName = socketName;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClipboardMany.class);

}
