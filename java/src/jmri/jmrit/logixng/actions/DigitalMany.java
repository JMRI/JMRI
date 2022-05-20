package jmri.jmrit.logixng.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Execute many Actions in a specific order.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DigitalMany extends AbstractDigitalAction
        implements FemaleSocketListener {

    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;
    
    public DigitalMany(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
    }

    public DigitalMany(String sys, String user, List<Map.Entry<String, String>> actionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setActionSystemNames(actionSystemNames);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalMany copy = new DigitalMany(sysName, userName);
        copy.setComment(getComment());
        copy.setNumSockets(getChildCount());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    private void setActionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_actionEntries.isEmpty()) {
            throw new RuntimeException("action system names cannot be set more than once");
        }
        
        for (Map.Entry<String, String> entry : systemNames) {
            FemaleDigitalActionSocket socket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, entry.getKey());
            
            _actionEntries.add(new ActionEntry(socket, entry.getValue()));
        }
    }

    public String getActionSystemName(int index) {
        return _actionEntries.get(index)._socketSystemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // We don't want to check for unconnected sockets while setup sockets
        disableCheckForUnconnectedSocket = true;
        
        for (ActionEntry ae : _actionEntries) {
            try {
                if ( !ae._socket.isConnected()
                        || !ae._socket.getConnectedSocket().getSystemName()
                                .equals(ae._socketSystemName)) {

                    String socketSystemName = ae._socketSystemName;
                    ae._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(DigitalActionManager.class)
                                        .getBySystemName(socketSystemName);
                        if (maleSocket != null) {
                            ae._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load digital action " + socketSystemName);
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
        
//        checkFreeSocket();
        
        disableCheckForUnconnectedSocket = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        for (ActionEntry actionEntry : _actionEntries) {
            actionEntry._socket.execute();
        }
    }

    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _actionEntries.get(index)._socket;
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return _actionEntries.size();
    }
    
    // This method ensures that we have enough of children
    private void setNumSockets(int num) {
        List<FemaleSocket> addList = new ArrayList<>();
        
        // Is there not enough children?
        while (_actionEntries.size() < num) {
            FemaleDigitalActionSocket socket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName());
            _actionEntries.add(new ActionEntry(socket));
            addList.add(socket);
        }
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }
    
    private void checkFreeSocket() {
        boolean hasFreeSocket = false;
        
        for (ActionEntry entry : _actionEntries) {
            hasFreeSocket |= !entry._socket.isConnected();
        }
        if (!hasFreeSocket) {
            FemaleDigitalActionSocket socket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName());
            _actionEntries.add(new ActionEntry(socket));
            
            List<FemaleSocket> list = new ArrayList<>();
            list.add(socket);
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, list);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isSocketOperationAllowed(int index, FemaleSocketOperation oper) {
        switch (oper) {
            case Remove:    // Possible if socket is not connected and there are at least two sockets
                if (_actionEntries.size() == 1) return false;
                return ! getChild(index).isConnected();
            case InsertBefore:
                return true;    // Always possible
            case InsertAfter:
                return true;    // Always possible
            case MoveUp:
                return index > 0;   // Possible if not first socket
            case MoveDown:
                return index+1 < getChildCount();   // Possible if not last socket
            default:
                throw new UnsupportedOperationException("Oper is unknown" + oper.name());
        }
    }
    
    private void insertNewSocket(int index) {
        FemaleDigitalActionSocket socket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName());
        _actionEntries.add(index, new ActionEntry(socket));
        
        List<FemaleSocket> addList = new ArrayList<>();
        addList.add(socket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }
    
    private void removeSocket(int index) {
        List<FemaleSocket> removeList = new ArrayList<>();
        removeList.add(_actionEntries.remove(index)._socket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, null);
    }
    
    private void moveSocketDown(int index) {
        ActionEntry temp = _actionEntries.get(index);
        _actionEntries.set(index, _actionEntries.get(index+1));
        _actionEntries.set(index+1, temp);
        
        List<FemaleSocket> list = new ArrayList<>();
        list.add(_actionEntries.get(index)._socket);
        list.add(_actionEntries.get(index+1)._socket);
        firePropertyChange(Base.PROPERTY_CHILD_REORDER, null, list);
    }
    
    /** {@inheritDoc} */
    @Override
    public void doSocketOperation(int index, FemaleSocketOperation oper) {
        switch (oper) {
            case Remove:
                if (getChild(index).isConnected()) throw new UnsupportedOperationException("Socket is connected");
                removeSocket(index);
                break;
            case InsertBefore:
                insertNewSocket(index);
                break;
            case InsertAfter:
                insertNewSocket(index+1);
                break;
            case MoveUp:
                if (index == 0) throw new UnsupportedOperationException("cannot move up first child");
                moveSocketDown(index-1);
                break;
            case MoveDown:
                if (index+1 == getChildCount()) throw new UnsupportedOperationException("cannot move down last child");
                moveSocketDown(index);
                break;
            default:
                throw new UnsupportedOperationException("Oper is unknown" + oper.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void connected(FemaleSocket socket) {
        if (disableCheckForUnconnectedSocket) return;
        
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        
        checkFreeSocket();
    }

    /** {@inheritDoc} */
    @Override
    public void disconnected(FemaleSocket socket) {
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                break;
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Many_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Many_Long");
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
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    private static class ActionEntry {
        private String _socketSystemName;
        private final FemaleDigitalActionSocket _socket;
        
        private ActionEntry(FemaleDigitalActionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }
        
        private ActionEntry(FemaleDigitalActionSocket socket) {
            this._socket = socket;
        }
        
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalMany.class);

}
