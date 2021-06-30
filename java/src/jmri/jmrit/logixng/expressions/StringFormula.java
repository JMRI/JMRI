package jmri.jmrit.logixng.expressions;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.jmrit.logixng.util.parser.Variable;
import jmri.jmrit.logixng.util.parser.GenericExpressionVariable;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates to True if the formula evaluates to true
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class StringFormula extends AbstractStringExpression implements FemaleSocketListener {

    private String _formula = "";
    private ExpressionNode _expressionNode;
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private boolean _disableCheckForUnconnectedSocket = false;
    
    /**
     * Create a new instance of Formula with system name and user name.
     * @param sys the system name
     * @param user the user name
     */
    public StringFormula(@Nonnull String sys, @CheckForNull String user) {
        super(sys, user);
        _expressionEntries
                .add(new ExpressionEntry(createFemaleSocket(this, this, getNewSocketName())));
    }

    /**
     * Create a new instance of Formula with system name and user name.
     * @param sys the system name
     * @param user the user name
     * @param expressionSystemNames a list of system names for the expressions
     * this formula uses
     */
    public StringFormula(@Nonnull String sys, @CheckForNull String user,
            List<SocketData> expressionSystemNames) {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        StringExpressionManager manager = InstanceManager.getDefault(StringExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        StringFormula copy = new StringFormula(sysName, userName);
        copy.setComment(getComment());
        copy.setNumSockets(getChildCount());
        copy.setFormula(_formula);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    private void setExpressionSystemNames(List<SocketData> systemNames) {
        if (!_expressionEntries.isEmpty()) {
            throw new RuntimeException("expression system names cannot be set more than once");
        }
        
        for (SocketData socketData : systemNames) {
            FemaleGenericExpressionSocket socket =
                    createFemaleSocket(this, this, socketData._socketName);
//            FemaleGenericExpressionSocket socket =
//                    InstanceManager.getDefault(AnalogExpressionManager.class)
//                            .createFemaleSocket(this, this, entry.getKey());
            
            _expressionEntries.add(new ExpressionEntry(socket, socketData._socketSystemName, socketData._manager));
        }
    }
    
    public String getExpressionSystemName(int index) {
        return _expressionEntries.get(index)._socketSystemName;
    }
    
    public String getExpressionManager(int index) {
        return _expressionEntries.get(index)._manager;
    }
    
    private FemaleGenericExpressionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        
        return new DefaultFemaleGenericExpressionSocket(
                FemaleGenericExpressionSocket.SocketType.GENERIC, parent, listener, socketName);
    }

    public final void setFormula(String formula) throws ParserException {
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser parser = new RecursiveDescentParser(variables);
        for (int i=0; i < getChildCount(); i++) {
            Variable v = new GenericExpressionVariable((FemaleGenericExpressionSocket)getChild(i));
            variables.put(v.getName(), v);
        }
        _expressionNode = parser.parseExpression(formula);
        // parseExpression() may throw an exception and we don't want to set
        // the field _formula until we now parseExpression() has succeeded.
        _formula = formula;
    }
    
    public String getFormula() {
        return _formula;
    }
    
    private void parseFormula() {
        try {
            setFormula(_formula);
        } catch (ParserException e) {
            log.error("Unexpected exception when parsing the formula", e);
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
    public String evaluate() throws JmriException {
        
        if (_formula.isEmpty()) {
            return "";
        }
        
        return TypeConversionUtil.convertToString(_expressionNode.calculate(
                getConditionalNG().getSymbolTable()), false);
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _expressionEntries.get(index)._socket;
    }
    
    @Override
    public int getChildCount() {
        return _expressionEntries.size();
    }
    
    public void setChildCount(int count) {
        List<FemaleSocket> addList = new ArrayList<>();
        List<FemaleSocket> removeList = new ArrayList<>();
        
        // Is there too many children?
        while (_expressionEntries.size() > count) {
            int childNo = _expressionEntries.size()-1;
            FemaleSocket socket = _expressionEntries.get(childNo)._socket;
            if (socket.isConnected()) {
                socket.disconnect();
            }
            removeList.add(_expressionEntries.get(childNo)._socket);
            _expressionEntries.remove(childNo);
        }
        
        // Is there not enough children?
        while (_expressionEntries.size() < count) {
            FemaleGenericExpressionSocket socket =
                    createFemaleSocket(this, this, getNewSocketName());
            _expressionEntries.add(new ExpressionEntry(socket));
            addList.add(socket);
        }
        parseFormula();
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, addList);
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringFormula_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        if (_formula.isEmpty()) {
            return Bundle.getMessage(locale, "StringFormula_Long_Empty");
        } else {
            return Bundle.getMessage(locale, "StringFormula_Long", _formula);
        }
    }

    // This method ensures that we have enough of children
    private void setNumSockets(int num) {
        List<FemaleSocket> addList = new ArrayList<>();
        
        // Is there not enough children?
        while (_expressionEntries.size() < num) {
            FemaleGenericExpressionSocket socket =
                    createFemaleSocket(this, this, getNewSocketName());
            _expressionEntries.add(new ExpressionEntry(socket));
            addList.add(socket);
        }
        parseFormula();
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }
    
    private void checkFreeSocket() {
        boolean hasFreeSocket = false;
        
        for (ExpressionEntry entry : _expressionEntries) {
            hasFreeSocket |= !entry._socket.isConnected();
        }
        if (!hasFreeSocket) {
            FemaleGenericExpressionSocket socket =
                    createFemaleSocket(this, this, getNewSocketName());
            _expressionEntries.add(new ExpressionEntry(socket));
            
            List<FemaleSocket> list = new ArrayList<>();
            list.add(socket);
            parseFormula();
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
        FemaleGenericExpressionSocket socket =
                createFemaleSocket(this, this, getNewSocketName());
        _expressionEntries.add(index, new ExpressionEntry(socket));
        
        List<FemaleSocket> addList = new ArrayList<>();
        addList.add(socket);
        parseFormula();
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, addList);
    }
    
    private void removeSocket(int index) {
        List<FemaleSocket> removeList = new ArrayList<>();
        removeList.add(_expressionEntries.remove(index)._socket);
        parseFormula();
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, null);
    }
    
    private void moveSocketDown(int index) {
        ExpressionEntry temp = _expressionEntries.get(index);
        _expressionEntries.set(index, _expressionEntries.get(index+1));
        _expressionEntries.set(index+1, temp);
        
        List<FemaleSocket> list = new ArrayList<>();
        list.add(_expressionEntries.get(index)._socket);
        list.add(_expressionEntries.get(index)._socket);
        parseFormula();
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
        if (_disableCheckForUnconnectedSocket) return;
        
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
    public void socketNameChanged(FemaleSocket socket) {
        parseFormula();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // We don't want to check for unconnected sockets while setup sockets
        _disableCheckForUnconnectedSocket = true;
        
        for (ExpressionEntry ee : _expressionEntries) {
            try {
                if ( !ee._socket.isConnected()
                        || !ee._socket.getConnectedSocket().getSystemName()
                                .equals(ee._socketSystemName)) {

                    String socketSystemName = ee._socketSystemName;
                    String manager = ee._manager;
                    ee._socket.disconnect();
                    if (socketSystemName != null) {
                        Manager<? extends MaleSocket> m =
                                InstanceManager.getDefault(LogixNG_Manager.class)
                                        .getManager(manager);
                        MaleSocket maleSocket = m.getBySystemName(socketSystemName);
                        if (maleSocket != null) {
                            ee._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load string expression " + socketSystemName);
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
        
        parseFormula();
        checkFreeSocket();
        
        _disableCheckForUnconnectedSocket = false;
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
    
    
    public static class SocketData {
        public final String _socketName;
        public final String _socketSystemName;
        public final String _manager;
        
        public SocketData(String socketName, String socketSystemName, String manager) {
            _socketName = socketName;
            _socketSystemName = socketSystemName;
            _manager = manager;
        }
    }
    
    
    /* This class is public since ExpressionFormulaXml needs to access it. */
    public static class ExpressionEntry {
        private final FemaleGenericExpressionSocket _socket;
        private String _socketSystemName;
        public String _manager;
        
        public ExpressionEntry(FemaleGenericExpressionSocket socket, String socketSystemName, String manager) {
            _socket = socket;
            _socketSystemName = socketSystemName;
            _manager = manager;
        }
        
        private ExpressionEntry(FemaleGenericExpressionSocket socket) {
            this._socket = socket;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringFormula.class);
}
