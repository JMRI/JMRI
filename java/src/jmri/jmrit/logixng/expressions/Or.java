package jmri.jmrit.logixng.expressions;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Evaluates to True if any of the children expressions evaluate to true.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Or extends AbstractDigitalExpression implements FemaleSocketListener {

    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private boolean disableCheckForUnconnectedSocket = false;
    
    public Or(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
    }
    
    public Or(String sys, String user, List<Map.Entry<String, String>> expressionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Or copy = new Or(sysName, userName);
        copy.setComment(getComment());
        copy.setNumSockets(getChildCount());
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    private void setExpressionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_expressionEntries.isEmpty()) {
            throw new RuntimeException("expression system names cannot be set more than once");
        }
        
        for (Map.Entry<String, String> entry : systemNames) {
            FemaleDigitalExpressionSocket socket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(this, this, entry.getKey());
            
            _expressionEntries.add(new ExpressionEntry(socket, entry.getValue()));
        }
    }
    
    public String getExpressionSystemName(int index) {
        return _expressionEntries.get(index)._socketSystemName;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        boolean result = false;
        for (ExpressionEntry e : _expressionEntries) {
            if (e._socket.isConnected() && e._socket.evaluate()) {
                result = true;
            }
        }
        return result;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _expressionEntries.get(index)._socket;
    }
    
    @Override
    public int getChildCount() {
        return _expressionEntries.size();
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Or_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Or_Long");
    }
    
    // This method ensures that we have enough of children
    private void setNumSockets(int num) {
        List<FemaleSocket> addList = new ArrayList<>();
        
        // Is there not enough children?
        while (_expressionEntries.size() < num) {
            FemaleDigitalExpressionSocket socket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName());
            _expressionEntries.add(new ExpressionEntry(socket));
            addList.add(socket);
        }
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }

    private void checkFreeSocket() {
        boolean hasFreeSocket = false;
        
        for (ExpressionEntry entry : _expressionEntries) {
            hasFreeSocket |= !entry._socket.isConnected();
        }
        if (!hasFreeSocket) {
            FemaleDigitalExpressionSocket socket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName());
            _expressionEntries.add(new ExpressionEntry(socket));
            
            List<FemaleSocket> list = new ArrayList<>();
            list.add(socket);
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, list);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isSocketOperationAllowed(int index, FemaleSocketOperation oper) {
        switch (oper) {
            case Remove:        // Possible if socket is not connected
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
        FemaleDigitalExpressionSocket socket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName());
        _expressionEntries.add(index, new ExpressionEntry(socket));
        
        List<FemaleSocket> addList = new ArrayList<>();
        addList.add(socket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }
    
    private void removeSocket(int index) {
        List<FemaleSocket> removeList = new ArrayList<>();
        removeList.add(_expressionEntries.remove(index)._socket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, null);
    }
    
    private void moveSocketDown(int index) {
        ExpressionEntry temp = _expressionEntries.get(index);
        _expressionEntries.set(index, _expressionEntries.get(index+1));
        _expressionEntries.set(index+1, temp);
        
        List<FemaleSocket> list = new ArrayList<>();
        list.add(_expressionEntries.get(index)._socket);
        list.add(_expressionEntries.get(index)._socket);
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
    
    @Override
    public void connected(FemaleSocket socket) {
        if (disableCheckForUnconnectedSocket) return;
        
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        
        checkFreeSocket();
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                break;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // We don't want to check for unconnected sockets while setup sockets
        disableCheckForUnconnectedSocket = true;
        
        for (ExpressionEntry ee : _expressionEntries) {
            try {
                if ( !ee._socket.isConnected()
                        || !ee._socket.getConnectedSocket().getSystemName()
                                .equals(ee._socketSystemName)) {

                    String socketSystemName = ee._socketSystemName;
                    ee._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(DigitalExpressionManager.class)
                                        .getBySystemName(socketSystemName);
                        if (maleSocket != null) {
                            ee._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load digital expression " + socketSystemName);
                        }
                    }
                } else {
                    ee._socket.getConnectedSocket().setup();
                }
            } catch (SocketAlreadyConnectedException ex) {
                // This shouldn't happen and is a runtime error if it does.
                throw new RuntimeException("socket is already connected");
            }
        }
        
        checkFreeSocket();
        
        disableCheckForUnconnectedSocket = false;
    }
    
    
    /* This class is public since ExpressionOrXml needs to access it. */
    private static class ExpressionEntry {
        private String _socketSystemName;
        private final FemaleDigitalExpressionSocket _socket;
        
        private ExpressionEntry(FemaleDigitalExpressionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }
        
        private ExpressionEntry(FemaleDigitalExpressionSocket socket) {
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Or.class);
    
}
