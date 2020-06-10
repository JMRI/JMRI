package jmri.jmrit.logixng.string.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleStringActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.StringActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute many Actions in a specific order.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Many extends AbstractStringAction
        implements FemaleSocketListener {

    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    
    public Many(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        init();
    }

//    public Many(String sys, String user, List<Map.Entry<String, String>> actionSystemNames)
//            throws BadUserNameException, BadSystemNameException {
//        super(sys, user);
//        setActionSystemNames(actionSystemNames);
//    }
    
    private void init() {
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(StringActionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
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
    
    /** {@inheritDoc} */
    @Override
    public void setValue(String value) throws JmriException {
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
    
    @Override
    public void connected(FemaleSocket socket) {
        boolean hasFreeSocket = false;
        for (ActionEntry entry : _actionEntries) {
            hasFreeSocket = !entry._socket.isConnected();
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        if (!hasFreeSocket) {
            _actionEntries.add(
                    new ActionEntry(
                            InstanceManager.getDefault(StringActionManager.class)
                                    .createFemaleSocket(this, this, getNewSocketName())));
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ActionEntry entry : _actionEntries) {
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

    private void setActionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_actionEntries.isEmpty()) {
            throw new RuntimeException("action system names cannot be set more than once");
        }
        
        for (Map.Entry<String, String> entry : systemNames) {
            FemaleStringActionSocket socket =
                    InstanceManager.getDefault(StringActionManager.class)
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
        for (ActionEntry ae : _actionEntries) {
            try {
                if ( !ae._socket.isConnected()
                        || !ae._socket.getConnectedSocket().getSystemName()
                                .equals(ae._socketSystemName)) {

                    String socketSystemName = ae._socketSystemName;
                    ae._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(StringActionManager.class)
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
    }


    private static class ActionEntry {
        private String _socketSystemName;
        private final FemaleStringActionSocket _socket;
        
        private ActionEntry(FemaleStringActionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }
        
        private ActionEntry(FemaleStringActionSocket socket) {
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
    
    
    private final static Logger log = LoggerFactory.getLogger(Many.class);

}
