package jmri.jmrit.logixng.analog.expressions;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.jmrit.logixng.util.parser.Variable;
import jmri.jmrit.logixng.util.parser.variables.GenericExpressionVariable;
import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;
import jmri.util.TypeConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates to True if the formula evaluates to true
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class Formula extends AbstractAnalogExpression implements FemaleSocketListener {

    private String _formula = "";
    private ExpressionNode _expressionNode;
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    
    /**
     * Create a new instance of Formula with system name and user name.
     */
    public Formula(@Nonnull String sys, @CheckForNull String user) {
        super(sys, user);
        init();
    }

    public Formula(@Nonnull String sys, @CheckForNull String user,
            List<Map.Entry<String, String>> expressionSystemNames) {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
    }

    private void init() {
        _expressionEntries
                .add(new ExpressionEntry(createFemaleSocket(this, this, getNewSocketName())));
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
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        for (ExpressionEntry e : _expressionEntries) {
            e._socket.reset();
        }
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
        // Is there too many children?
        while (_expressionEntries.size() > count) {
            int childNo = _expressionEntries.size()-1;
            FemaleSocket socket = _expressionEntries.get(childNo)._socket;
            if (socket.isConnected()) {
                socket.disconnect();
            }
            _expressionEntries.remove(childNo);
        }
        
        // Is there not enough children?
        while (_expressionEntries.size() < count) {
            _expressionEntries
                    .add(new ExpressionEntry(createFemaleSocket(this, this, getNewSocketName())));
        }
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

    private void setExpressionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_expressionEntries.isEmpty()) {
            throw new RuntimeException("expression system names cannot be set more than once");
        }
        
        for (Map.Entry<String, String> entry : systemNames) {
            FemaleGenericExpressionSocket socket =
                    createFemaleSocket(this, this, entry.getKey());
//            FemaleGenericExpressionSocket socket =
//                    InstanceManager.getDefault(AnalogExpressionManager.class)
//                            .createFemaleSocket(this, this, entry.getKey());
            
            _expressionEntries.add(new ExpressionEntry(socket, entry.getValue()));
        }
    }
    
    public String getExpressionSystemName(int index) {
        return _expressionEntries.get(index)._socketSystemName;
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
    
    @Override
    public void connected(FemaleSocket socket) {
        boolean hasFreeSocket = false;
        for (ExpressionEntry entry : _expressionEntries) {
            hasFreeSocket = !entry._socket.isConnected();
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        if (!hasFreeSocket) {
            _expressionEntries
                    .add(new ExpressionEntry(createFemaleSocket(this, this, getNewSocketName())));
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                break;
            }
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        for (ExpressionEntry ee : _expressionEntries) {
            try {
                if ( !ee._socket.isConnected()
                        || !ee._socket.getConnectedSocket().getSystemName()
                                .equals(ee._socketSystemName)) {

                    String socketSystemName = ee._socketSystemName;
                    ee._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(AnalogExpressionManager.class)
                                        .getBySystemName(socketSystemName);
                        if (maleSocket != null) {
                            ee._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load digital action " + socketSystemName);
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
    }
    
    
    
    /* This class is public since ExpressionFormulaXml needs to access it. */
    public static class ExpressionEntry {
        private String _socketSystemName;
        private final FemaleGenericExpressionSocket _socket;
        
        public ExpressionEntry(FemaleGenericExpressionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }
        
        private ExpressionEntry(FemaleGenericExpressionSocket socket) {
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
    
    private final static Logger log = LoggerFactory.getLogger(Formula.class);
}
