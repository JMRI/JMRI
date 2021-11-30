package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.jmrit.logixng.util.TimerUnit;

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
 * Executes a digital action delayed.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExecuteDelayed
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    private ProtectedTimerTask _timerTask;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private int _delay;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;
    private TimerUnit _unit = TimerUnit.MilliSeconds;
    private boolean _resetIfAlreadyStarted;
    
    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();
    protected final List<SymbolTable.VariableData> _localVariables = new ArrayList<>();
    
    
    // These variables are used internally in this action
    private long _timerDelay = 0;   // Timer delay in milliseconds
    private long _timerStart = 0;   // Timer start in milliseconds
    
    public ExecuteDelayed(String sys, String user) {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExecuteDelayed copy = new ExecuteDelayed(sysName, userName);
        copy.setComment(getComment());
        copy.setDelayAddressing(_stateAddressing);
        copy.setDelay(_delay);
        copy.setDelayFormula(_stateFormula);
        copy.setDelayLocalVariable(_stateLocalVariable);
        copy.setDelayReference(_stateReference);
        copy.setUnit(_unit);
        copy.setResetIfAlreadyStarted(_resetIfAlreadyStarted);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }
/*
    private String getVariables(SymbolTable symbolTable) {
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter writer = new java.io.PrintWriter(stringWriter);
        symbolTable.printSymbolTable(writer);
        return stringWriter.toString();
    }
*/    
    /**
     * Get a new timer task.
     */
    private ProtectedTimerTask getNewTimerTask(ConditionalNG conditionalNG, SymbolTable symbolTable) throws JmriException {

        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(symbolTable);
        
        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    synchronized(ExecuteDelayed.this) {
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
        synchronized(this) {
            if (_timerTask != null) {
                if (_resetIfAlreadyStarted) _timerTask.stopTimer();
                else return;
            }
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
    
    /**
     * Get reset if timer is already started.
     * @return true if the timer should be reset if this action is executed
     *         while timer is ticking, false othervise
     */
    public boolean getResetIfAlreadyStarted() {
        return _resetIfAlreadyStarted;
    }
    
    /**
     * Set reset if timer is already started.
     * @param resetIfAlreadyStarted true if the timer should be reset if this
     *                              action is executed while timer is ticking,
     *                              false othervise
     */
    public void setResetIfAlreadyStarted(boolean resetIfAlreadyStarted) {
        _resetIfAlreadyStarted = resetIfAlreadyStarted;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _socket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ExecuteDelayed_Short");
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
                "ExecuteDelayed_Long",
                _socket.getName(),
                delay,
                _resetIfAlreadyStarted
                        ? Bundle.getMessage("ExecuteDelayed_ResetRepeat")
                        : Bundle.getMessage("ExecuteDelayed_IgnoreRepeat"));
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setSocketSystemName(String systemName) {
        _socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {
                
                String socketSystemName = _socketSystemName;
                
                _socket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog action " + socketSystemName);
                    }
                }
            } else {
                _socket.getConnectedSocket().setup();
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
        if (_timerTask != null) {
            _timerTask.stopTimer();
            _timerTask = null;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
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
            if (_socket != null) {
                SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                conditionalNG.setSymbolTable(newSymbolTable);
                _socket.execute();
                conditionalNG.setSymbolTable(oldSymbolTable);
            }
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteDelayed.class);
    
}
