package jmri.jmrit.logixng.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleAnalogActionSocket;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Execute many Actions in a specific order.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class AnalogMany extends AbstractAnalogAction
        implements FemaleSocketListener {

    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;
    
    public AnalogMany(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(AnalogActionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
    }

    public AnalogMany(String sys, String user, List<Map.Entry<String, String>> actionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setActionSystemNames(actionSystemNames);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        AnalogActionManager manager = InstanceManager.getDefault(AnalogActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AnalogMany copy = new AnalogMany(sysName, userName);
        copy.setComment(getComment());
        copy.setNumSockets(getChildCount());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    private void setActionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_actionEntries.isEmpty()) {
            throw new RuntimeException("action system names cannot be set more than once");
        }
        
        for (Map.Entry<String, String> entry : systemNames) {
            FemaleAnalogActionSocket socket =
                    InstanceManager.getDefault(AnalogActionManager.class)
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
                                InstanceManager.getDefault(AnalogActionManager.class)
                                        .getBySystemName(socketSystemName);
                        if (maleSocket != null) {
                            ae._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load analog action " + socketSystemName);
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
    public void setValue(double value) throws JmriException {
        for (ActionEntry actionEntry : _actionEntries) {
            actionEntry._socket.setValue(value);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _actionEntries.get(index)._socket;
    }

    @Override
    public int getChildCount() {
        return _actionEntries.size();
    }
    
    // This method ensures that we have enough of children
    private void setNumSockets(int num) {
        List<FemaleSocket> addList = new ArrayList<>();
        
        // Is there not enough children?
        while (_actionEntries.size() < num) {
            FemaleAnalogActionSocket socket =
                    InstanceManager.getDefault(AnalogActionManager.class)
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
            FemaleAnalogActionSocket socket =
                    InstanceManager.getDefault(AnalogActionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName());
            _actionEntries.add(new ActionEntry(socket));
            
            List<FemaleSocket> list = new ArrayList<>();
            list.add(socket);
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, list);
        }
    }
    
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

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
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
    
    
    private static class ActionEntry {
        private String _socketSystemName;
        private final FemaleAnalogActionSocket _socket;
        
        private ActionEntry(FemaleAnalogActionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }
        
        private ActionEntry(FemaleAnalogActionSocket socket) {
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
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogMany.class);

}
