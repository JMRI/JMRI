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
public class AnalogFormula extends AbstractAnalogExpression implements FemaleSocketListener {

    private String _formula = "";
    private ExpressionNode _expressionNode;
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private boolean _disableCheckForUnconnectedSocket = false;
    
    /**
     * Create a new instance of Formula with system name and user name.
     * @param sys the system name
     * @param user the user name
     */
    public AnalogFormula(@Nonnull String sys, @CheckForNull String user) {
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
    public AnalogFormula(@Nonnull String sys, @CheckForNull String user,
            List<SocketData> expressionSystemNames) {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        AnalogExpressionManager manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AnalogFormula copy = new AnalogFormula(sysName, userName);
        copy.setComment(getComment());
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
                FemaleGenericExpressionSocket.SocketType.GENERIC, parent, listener, socketName)
                .getGenericSocket();
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
    public double evaluate() throws JmriException {
        
        if (_formula.isEmpty()) {
            return 0.0;
        }
        
        return TypeConversionUtil.convertToDouble(_expressionNode.calculate(), false);
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
        firePropertyChange(Base.PROPERTY_CHILD_COUNT, removeList, addList);
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Formula_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        if (_formula.isEmpty()) {
            return Bundle.getMessage(locale, "Formula_Long_Empty");
        } else {
            return Bundle.getMessage(locale, "Formula_Long", _formula);
        }
    }

    public String getFormula() {
        return _formula;
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
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, list);
        }
    }
    
    @Override
    public void connected(FemaleSocket socket) {
        if (_disableCheckForUnconnectedSocket) return;
        
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
                entry._manager =
                        socket.getConnectedSocket().getManager().getClass().getName();
            }
        }
        
        checkFreeSocket();
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                entry._manager = null;
                break;
            }
        }
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
                            log.error("cannot load analog expression " + socketSystemName);
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogFormula.class);
}
