package jmri.jmrit.logixng.actions;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TimerUtil;
import jmri.util.TypeConversionUtil;

/**
 * Executes an action when the expression is True.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class Timeout extends AbstractDigitalAction
        implements FemaleSocketListener {

    private ProtectedTimerTask _timerTask;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private int _delay;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;
    private TimerUnit _unit = TimerUnit.MilliSeconds;
    private String _expressionSocketSystemName;
    private String _actionSocketSystemName;
    private final FemaleDigitalExpressionSocket _expressionSocket;
    private final FemaleDigitalActionSocket _actionSocket;
    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();

    // These variables are used internally in this action
    private long _timerDelay = 0;   // Timer delay in milliseconds
    private long _timerStart = 0;   // Timer start in milliseconds

    public Timeout(String sys, String user) {
        super(sys, user);
        _expressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Timeout copy = new Timeout(sysName, userName);
        copy.setComment(getComment());
        copy.setDelayAddressing(_stateAddressing);
        copy.setDelay(_delay);
        copy.setDelayFormula(_stateFormula);
        copy.setDelayLocalVariable(_stateLocalVariable);
        copy.setDelayReference(_stateReference);
        copy.setUnit(_unit);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /**
     * Get a new timer task.
     */
    private ProtectedTimerTask getNewTimerTask(ConditionalNG conditionalNG, SymbolTable symbolTable) throws JmriException {

        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(symbolTable);
        
        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    synchronized(Timeout.this) {
                        _timerTask = null;
                        long currentTimerTime = System.currentTimeMillis() - _timerStart;
                        if (currentTimerTime < _timerDelay) {
                            scheduleTimer(conditionalNG, symbolTable, _timerDelay - currentTimerTime);
                        } else {
                            _internalSocket.conditionalNG = conditionalNG;
                            _internalSocket.newSymbolTable = newSymbolTable;
                            conditionalNG.execute(_internalSocket);
                        }
                    }
                } catch (RuntimeException | JmriException e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }
    
    private void scheduleTimer(ConditionalNG conditionalNG, SymbolTable symbolTable, long delay) throws JmriException {
        if (_timerTask != null) _timerTask.stopTimer();
        _timerTask = getNewTimerTask(conditionalNG, symbolTable);
        TimerUtil.schedule(_timerTask, delay);
    }
    
    private long getNewDelay() throws JmriException {
        
        switch (_stateAddressing) {
            case Direct:
                return _delay;
                
            case Reference:
                return TypeConversionUtil.convertToLong(ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference));
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToLong(symbolTable.getValue(_stateLocalVariable));
                
            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToLong(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()))
                        : 0;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _stateAddressing.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        boolean result = _expressionSocket.evaluate();

        synchronized(this) {
            if (result) {
                if (_timerTask != null) {
                    _timerTask.stopTimer();
                    _timerTask = null;
                }
                return;
            }
            
            // Don't restart timer if it's still running
            if (_timerTask != null) return;
            
            _timerDelay = getNewDelay() * _unit.getMultiply();
            _timerStart = System.currentTimeMillis();
            ConditionalNG conditonalNG = getConditionalNG();
            scheduleTimer(conditonalNG, conditonalNG.getSymbolTable(), _delay * _unit.getMultiply());
        }
    }
    
    public void setDelayAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseDelayFormula();
    }
    
    public NamedBeanAddressing getDelayAddressing() {
        return _stateAddressing;
    }
    
    /**
     * Get the delay.
     * @return the delay
     */
    public int getDelay() {
        return _delay;
    }
    
    /**
     * Set the delay.
     * @param delay the delay
     */
    public void setDelay(int delay) {
        _delay = delay;
    }
    
    public void setDelayReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }
    
    public String getDelayReference() {
        return _stateReference;
    }
    
    public void setDelayLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }
    
    public String getDelayLocalVariable() {
        return _stateLocalVariable;
    }
    
    public void setDelayFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseDelayFormula();
    }
    
    public String getDelayFormula() {
        return _stateFormula;
    }
    
    private void parseDelayFormula() throws ParserException {
        if (_stateAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _stateExpressionNode = parser.parseExpression(_stateFormula);
        } else {
            _stateExpressionNode = null;
        }
    }
    
    /**
     * Get the unit
     * @return the unit
     */
    public TimerUnit getUnit() {
        return _unit;
    }
    
    /**
     * Set the unit
     * @param unit the unit
     */
    public void setUnit(TimerUnit unit) {
        _unit = unit;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _expressionSocket;
                
            case 1:
                return _actionSocket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _expressionSocket) {
            _expressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _actionSocket) {
            _actionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _expressionSocket) {
            _expressionSocketSystemName = null;
        } else if (socket == _actionSocket) {
            _actionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Timeout_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String delay;
        
        switch (_stateAddressing) {
            case Direct:
                delay = Bundle.getMessage(locale, "ExecuteDelayed_DelayByDirect", _unit.getTimeWithUnit(_delay));
                break;
                
            case Reference:
                delay = Bundle.getMessage(locale, "ExecuteDelayed_DelayByReference", _stateReference, _unit.toString());
                break;
                
            case LocalVariable:
                delay = Bundle.getMessage(locale, "ExecuteDelayed_DelayByLocalVariable", _stateLocalVariable, _unit.toString());
                break;
                
            case Formula:
                delay = Bundle.getMessage(locale, "ExecuteDelayed_DelayByFormula", _stateFormula, _unit.toString());
                break;
                
            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _stateAddressing.name());
        }
        
        return Bundle.getMessage(locale,
                "Timeout_Long",
                _expressionSocket.getName(),
                _actionSocket.getName(),
                delay);
    }

    public FemaleDigitalExpressionSocket getExpressionSocket() {
        return _expressionSocket;
    }

    public String getExpressionSocketSystemName() {
        return _expressionSocketSystemName;
    }

    public void setExpressionSocketSystemName(String systemName) {
        _expressionSocketSystemName = systemName;
    }

    public FemaleDigitalActionSocket getActionSocket() {
        return _actionSocket;
    }

    public String getActionSocketSystemName() {
        return _actionSocketSystemName;
    }

    public void setActionSocketSystemName(String systemName) {
        _actionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_expressionSocket.isConnected()
                    || !_expressionSocket.getConnectedSocket().getSystemName()
                            .equals(_expressionSocketSystemName)) {
                
                String socketSystemName = _expressionSocketSystemName;
                _expressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _expressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression " + socketSystemName);
                    }
                }
            } else {
                _expressionSocket.getConnectedSocket().setup();
            }
            
            if ( !_actionSocket.isConnected()
                    || !_actionSocket.getConnectedSocket().getSystemName()
                            .equals(_actionSocketSystemName)) {
                
                String socketSystemName = _actionSocketSystemName;
                _actionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _actionSocket.disconnect();
                    if (maleSocket != null) {
                        _actionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _actionSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (_timerTask != null) {
            _timerTask.stopTimer();
            _timerTask = null;
        }
    }
    
    
    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {
        
        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;
        
        public InternalFemaleSocket() {
            super(null, new FemaleSocketListener(){
                @Override
                public void connected(FemaleSocket socket) {
                    // Do nothing
                }

                @Override
                public void disconnected(FemaleSocket socket) {
                    // Do nothing
                }
            }, "A");
        }
        
        @Override
        public void execute() throws JmriException {
            if (_actionSocket != null) {
                SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                conditionalNG.setSymbolTable(newSymbolTable);
                _actionSocket.execute();
                conditionalNG.setSymbolTable(oldSymbolTable);
            }
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Timeout.class);

}
