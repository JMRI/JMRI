package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AbstractDigitalExpression;

/**
 * Executes an action after some time.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class Sequence extends AbstractDigitalAction
        implements FemaleSocketListener {

    public static final int EXPRESSION_START = 0;
    public static final int EXPRESSION_STOP = 1;
    public static final int EXPRESSION_RESET = 2;
    public static final int NUM_STATIC_EXPRESSIONS = 3;

    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private int _currentStep = -1;
    private boolean _isRunning = false;
    private boolean _startImmediately = false;
    private boolean _runContinuously = false;
    private boolean disableCheckForUnconnectedSocket = false;
    
    public Sequence(String sys, String user) {
        super(sys, user);
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("SequenceSocketStart"))));
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("SequenceSocketStop"))));
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("SequenceSocketReset"))));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewActionSocketName())));
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, getNewExpressionSocketName())));
    }
    
    public Sequence(String sys, String user,
            List<Map.Entry<String, String>> expressionSystemNames,
            List<Map.Entry<String, String>> actionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
        setActionSystemNames(actionSystemNames);
    }
    
    public String getNewActionSocketName() {
        String[] names = new String[getChildCount()];
        for (int i=0; i < getChildCount(); i++) {
            names[i] = getChild(i).getName();
        }
        return getNewSocketName(names);
    }
    
    public String getNewExpressionSocketName() {
        String[] names = new String[getChildCount()];
        for (int i=0; i < getChildCount(); i++) {
            names[i] = getChild(i).getName();
        }
        return AbstractDigitalExpression.getNewSocketName(names);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
//        if (1==1) throw new RuntimeException("Not implemented yet");
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Sequence copy = new Sequence(sysName, userName);
        copy.setComment(getComment());
        copy.setComment(getComment());
        copy.setStartImmediately(_startImmediately);
        copy.setRunContinuously(_runContinuously);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
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
    public void execute() throws JmriException {
        if (_expressionEntries.get(EXPRESSION_STOP)._socket.isConnected()
                && _expressionEntries.get(EXPRESSION_STOP)._socket.evaluate()) {
            _isRunning = false;
            System.out.format("Stop: _currentStep: %d%n", _currentStep);
            return;
        }
        
        if (_expressionEntries.get(EXPRESSION_START)._socket.isConnected()
                && _expressionEntries.get(EXPRESSION_START)._socket.evaluate()) {
            _isRunning = true;
            System.out.format("Start: _currentStep: %d%n", _currentStep);
        }
        
        if (_expressionEntries.get(EXPRESSION_RESET)._socket.isConnected()
                && _expressionEntries.get(EXPRESSION_RESET)._socket.evaluate()) {
            _currentStep = -1;
            System.out.format("Reset: _currentStep: %d%n", _currentStep);
        }
        
        if (!_isRunning) return;
        
        if (_currentStep == -1) {
            _currentStep = 0;
            FemaleDigitalActionSocket socket =
                    _actionEntries.get(_currentStep)._socket;
            if (socket.isConnected()) socket.execute();
        }
        
        FemaleDigitalExpressionSocket exprSocket =
                _expressionEntries.get(_currentStep + NUM_STATIC_EXPRESSIONS)._socket;
        if (exprSocket.isConnected() && exprSocket.evaluate()) {
            _currentStep++;
            System.out.format("_currentStep: %d, size: %d%n", _currentStep, _actionEntries.size());
            if (_currentStep >= _actionEntries.size()) {
                _currentStep = 0;
                System.out.format("_currentStep set to 0: %d%n", _currentStep);
            }
            
            FemaleDigitalActionSocket actionSocket =
                    _actionEntries.get(_currentStep)._socket;
            if (exprSocket.isConnected()) actionSocket.execute();
        }
    }

    /**
     * Get if to start immediately
     * @return true if to start immediately
     */
    public boolean getStartImmediately() {
        return _startImmediately;
    }
    
    /**
     * Set if to start immediately
     * @param startImmediately true if to start immediately
     */
    public void setStartImmediately(boolean startImmediately) {
        _startImmediately = startImmediately;
        if (_startImmediately) _isRunning = true;
    }
    
    /**
     * Get if run continuously
     * @return true if run continuously
     */
    public boolean getRunContinuously() {
        return _runContinuously;
    }
    
    /**
     * Set if run continuously
     * @param runContinuously true if run continuously
     */
    public void setRunContinuously(boolean runContinuously) {
        _runContinuously = runContinuously;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index == EXPRESSION_START) return _expressionEntries.get(EXPRESSION_START)._socket;
        if (index == EXPRESSION_STOP) return _expressionEntries.get(EXPRESSION_STOP)._socket;
        if (index == EXPRESSION_RESET) return _expressionEntries.get(EXPRESSION_RESET)._socket;
        
        index -= NUM_STATIC_EXPRESSIONS;
        
        if ((index % 2) == 0) return _actionEntries.get(index >> 1)._socket;
        else return _expressionEntries.get((index >> 1) + NUM_STATIC_EXPRESSIONS)._socket;
    }

    @Override
    public int getChildCount() {
        return _expressionEntries.size() + _actionEntries.size();
    }
/*
    private void checkFreeSocket() {
        boolean hasFreeSocket = false;
        
        for (ActionEntry entry : _actionEntries) {
            hasFreeSocket |= !entry._socket.isConnected();
        }
        for (ExpressionEntry entry : _expressionEntries) {
            hasFreeSocket |= !entry._socket.isConnected();
        }
        if (!hasFreeSocket) {
            FemaleDigitalActionSocket actionSocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, getNewActionSocketName());
            _actionEntries.add(new ActionEntry(actionSocket));
            
            FemaleDigitalExpressionSocket exprSocket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(this, this, getNewExpressionSocketName());
            _expressionEntries.add(new ExpressionEntry(exprSocket));
            
            List<FemaleSocket> list = new ArrayList<>();
            list.add(actionSocket);
            list.add(exprSocket);
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, list);
        }
    }
*/    
    /** {@inheritDoc} */
    @Override
    public boolean isSocketOperationAllowed(int index, FemaleSocketOperation oper) {
        switch (oper) {
            case Remove:
                // Possible if not the three static sockets,
                // the socket is not connected and the next socket is not connected
                return (index >= NUM_STATIC_EXPRESSIONS)
                        && (index+1 < getChildCount())
                        && !getChild(index).isConnected()
                        && !getChild(index+1).isConnected();
            case InsertBefore:
                // Possible if not the three static sockets
                return index >= NUM_STATIC_EXPRESSIONS;
            case InsertAfter:
                return true;    // Always possible
            case MoveUp:
                return index >= NUM_STATIC_EXPRESSIONS+2;   // Possible if not the three static sockets and the first two sockets after that
            case MoveDown:
                return index+2 < getChildCount();   // Possible if not last two sockets
            default:
                throw new UnsupportedOperationException("Oper is unknown" + oper.name());
        }
    }
    
    private void insertNewSocket(int index) {
        int actionIndex = (index-NUM_STATIC_EXPRESSIONS) >> 1;
        int expressionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + NUM_STATIC_EXPRESSIONS;
        
        // Does index points to an expression socket instead of an action socket?
        if (((index-NUM_STATIC_EXPRESSIONS) % 2) != 0) {
            expressionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + NUM_STATIC_EXPRESSIONS;
            actionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + 1;
        }
        
        FemaleDigitalActionSocket actionSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewActionSocketName());
        _actionEntries.add(actionIndex, new ActionEntry(actionSocket));
        
        FemaleDigitalExpressionSocket exprSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, getNewExpressionSocketName());
        _expressionEntries.add(expressionIndex, new ExpressionEntry(exprSocket));
        
        List<FemaleSocket> addList = new ArrayList<>();
        addList.add(actionSocket);
        addList.add(exprSocket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }
    
    private void removeSocket(int index) {
        int actionIndex = (index-NUM_STATIC_EXPRESSIONS) >> 1;
        int expressionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + NUM_STATIC_EXPRESSIONS;
        
        List<FemaleSocket> removeList = new ArrayList<>();
        removeList.add(_actionEntries.remove(actionIndex)._socket);
        removeList.add(_expressionEntries.remove(expressionIndex)._socket);
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, null);
    }
    
    private void moveSocketDown(int index) {
        int actionIndex = (index-NUM_STATIC_EXPRESSIONS) >> 1;
        int expressionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + NUM_STATIC_EXPRESSIONS;
        
        // Does index points to an expression socket instead of an action socket?
        if (((index-NUM_STATIC_EXPRESSIONS) % 2) != 0) {
            expressionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + NUM_STATIC_EXPRESSIONS;
            actionIndex = ((index-NUM_STATIC_EXPRESSIONS) >> 1) + 1;
        }
        
        ActionEntry actionTemp = _actionEntries.get(actionIndex);
        _actionEntries.set(actionIndex, _actionEntries.get(actionIndex+1));
        _actionEntries.set(actionIndex+1, actionTemp);
        
        ExpressionEntry exprTemp = _expressionEntries.get(expressionIndex);
        _expressionEntries.set(expressionIndex, _expressionEntries.get(expressionIndex+1));
        _expressionEntries.set(expressionIndex+1, exprTemp);
        
        List<FemaleSocket> list = new ArrayList<>();
        list.add(_actionEntries.get(actionIndex)._socket);
        list.add(_actionEntries.get(actionIndex+1)._socket);
        list.add(_expressionEntries.get(expressionIndex)._socket);
        list.add(_expressionEntries.get(expressionIndex+1)._socket);
        firePropertyChange(Base.PROPERTY_CHILD_REORDER, null, list);
    }
    
    /** {@inheritDoc} */
    @Override
    public void doSocketOperation(int index, FemaleSocketOperation oper) {
        switch (oper) {
            case Remove:
                if (index+1 >= getChildCount()) throw new UnsupportedOperationException("Cannot remove only the last socket");
                if (getChild(index).isConnected()) throw new UnsupportedOperationException("Socket is connected");
                if (getChild(index+1).isConnected()) throw new UnsupportedOperationException("Socket below is connected");
                removeSocket(index);
                break;
            case InsertBefore:
                insertNewSocket(index);
                break;
            case InsertAfter:
                insertNewSocket(index+1);
                break;
            case MoveUp:
                if (index < NUM_STATIC_EXPRESSIONS) throw new UnsupportedOperationException("cannot move up static sockets");
                if (index <= NUM_STATIC_EXPRESSIONS+1) throw new UnsupportedOperationException("cannot move up first two children");
                moveSocketDown(index-2);
                break;
            case MoveDown:
                if (index+2 >= getChildCount()) throw new UnsupportedOperationException("cannot move down last two children");
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
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        
//        checkFreeSocket();
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
            }
        }
        for (ActionEntry entry : _actionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
            }
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Sequence_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Sequence_Long");
    }

    public int getNumExpressions() {
        return _expressionEntries.size();
    }
    
    public FemaleDigitalExpressionSocket getExpressionSocket(int socket) {
        return _expressionEntries.get(socket)._socket;
    }

    public String getExpressionSocketSystemName(int socket) {
        return _expressionEntries.get(socket)._socketSystemName;
    }

    public void setExpressionSocketSystemName(int socket, String systemName) {
        _expressionEntries.get(socket)._socketSystemName = systemName;
    }

    public int getNumActions() {
        return _actionEntries.size();
    }
/*    
    public void setNumActions(int num) {
        List<FemaleSocket> addList = new ArrayList<>();
        List<FemaleSocket> removeList = new ArrayList<>();
        
        // Is there too many children?
        while (_actionEntries.size() > num) {
            ActionEntry ae = _actionEntries.get(num);
            if (ae._socket.isConnected()) {
                throw new IllegalArgumentException("Cannot remove sockets that are connected");
            }
            removeList.add(_actionEntries.get(_actionEntries.size()-1)._socket);
            _actionEntries.remove(_actionEntries.size()-1);
        }
        
        // Is there not enough children?
        while (_actionEntries.size() < num) {
            FemaleDigitalActionSocket socket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName());
            _actionEntries.add(new ActionEntry(socket));
            addList.add(socket);
        }
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, addList);
    }
*/    
    public FemaleDigitalActionSocket getActionSocket(int socket) {
        return _actionEntries.get(socket)._socket;
    }

    public String getActionSocketSystemName(int socket) {
        return _actionEntries.get(socket)._socketSystemName;
    }

    public void setActionSocketSystemName(int socket, String systemName) {
        _actionEntries.get(socket)._socketSystemName = systemName;
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
                        ee._socket.disconnect();
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
                        ae._socket.disconnect();
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
        
        disableCheckForUnconnectedSocket = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Sequence.class);

}
