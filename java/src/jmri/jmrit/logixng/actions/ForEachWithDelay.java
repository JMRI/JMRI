package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.*;

/**
 * Executes an action when the expression is True.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ForEachWithDelay extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener {

    private final LogixNG_SelectString _selectVariable =
            new LogixNG_SelectString(this, this);

    private final LogixNG_SelectNamedBean<Memory> _selectMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private boolean _useCommonSource = true;
    private CommonManager _commonManager = CommonManager.Sensors;
    private UserSpecifiedSource _userSpecifiedSource = UserSpecifiedSource.Variable;
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private int _delay;
    private TimerUnit _unit = TimerUnit.MilliSeconds;
    private String _variableName = "";
    private boolean _resetIfAlreadyStarted;
    private boolean _useIndividualTimers;
    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    private ProtectedTimerTask _defaultTimerTask;

    private final InternalFemaleSocket _defaultInternalSocket = new InternalFemaleSocket();


    public ForEachWithDelay(String sys, String user) {
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
        ForEachWithDelay copy = new ForEachWithDelay(sysName, userName);
        copy.setComment(getComment());
        copy.setUseCommonSource(_useCommonSource);
        copy.setCommonManager(_commonManager);
        copy.setUserSpecifiedSource(_userSpecifiedSource);
        copy.setDelay(_delay);
        copy.setUnit(_unit);
        _selectVariable.copy(copy._selectVariable);
        _selectMemoryNamedBean.copy(copy._selectMemoryNamedBean);
        copy.setFormula(_formula);
        copy.setLocalVariableName(_variableName);
        copy.setResetIfAlreadyStarted(_resetIfAlreadyStarted);
        copy.setUseIndividualTimers(_useIndividualTimers);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectString getSelectVariable() {
        return _selectVariable;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectMemoryNamedBean() {
        return _selectMemoryNamedBean;
    }

    public void setUseCommonSource(boolean commonSource) {
        this._useCommonSource = commonSource;
    }

    public boolean isUseCommonSource() {
        return _useCommonSource;
    }

    public void setCommonManager(CommonManager commonManager) throws ParserException {
        _commonManager = commonManager;
        parseFormula();
    }

    public CommonManager getCommonManager() {
        return _commonManager;
    }

    public void setUserSpecifiedSource(UserSpecifiedSource userSpecifiedSource) throws ParserException {
        _userSpecifiedSource = userSpecifiedSource;
        parseFormula();
    }

    public UserSpecifiedSource getUserSpecifiedSource() {
        return _userSpecifiedSource;
    }

    public void setFormula(String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_userSpecifiedSource == UserSpecifiedSource.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
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
     * Get name of local variable
     * @return name of local variable
     */
    public String getLocalVariableName() {
        return _variableName;
    }

    /**
     * Set name of local variable
     * @param localVariableName name of local variable
     */
    public void setLocalVariableName(String localVariableName) {
        _variableName = localVariableName;
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

    /**
     * Get use individual timers.
     * @return true if the timer should use individual timers, false othervise
     */
    public boolean getUseIndividualTimers() {
        return _useIndividualTimers;
    }

    /**
     * Set reset if timer is already started.
     * @param useIndividualTimers true if the timer should use individual timers,
     *                              false othervise
     */
    public void setUseIndividualTimers(boolean useIndividualTimers) {
        _useIndividualTimers = useIndividualTimers;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.FLOW_CONTROL;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws JmriException {
        final AtomicReference<Collection<? extends Object>> collectionRef = new AtomicReference<>();
        final AtomicReference<JmriException> ref = new AtomicReference<>();

        final ConditionalNG conditionalNG = getConditionalNG();
        final SymbolTable symbolTable = getConditionalNG().getSymbolTable();

        if (_useCommonSource) {
            collectionRef.set(_commonManager.getManager().getNamedBeanSet());
        } else {
            ThreadingUtil.runOnLayoutWithJmriException(() -> {

                Object value = null;

                switch (_userSpecifiedSource) {
                    case Variable:
                        String otherLocalVariable = _selectVariable.evaluateValue(getConditionalNG());
                        Object variableValue = symbolTable.getValue(otherLocalVariable);

                        value = variableValue;
                        break;

                    case Memory:
                        Memory memory = _selectMemoryNamedBean.evaluateNamedBean(getConditionalNG());
                        if (memory != null) {
                            value = memory.getValue();
                        } else {
                            log.warn("ForEachWithDelay memory is null");
                        }
                        break;

                    case Formula:
                        if (!_formula.isEmpty() && _expressionNode != null) {
                            value = _expressionNode.calculate(conditionalNG.getSymbolTable());
                        }
                        break;

                    default:
                        // Throw exception
                        throw new IllegalArgumentException("_userSpecifiedSource has invalid value: {}" + _userSpecifiedSource.name());
                }

                if (value instanceof Manager) {
                    collectionRef.set(((Manager<? extends NamedBean>) value).getNamedBeanSet());
                } else if (value != null && value.getClass().isArray()) {
                    // Note: (Object[]) is needed to tell that the parameter is an array and not a vararg argument
                    // See: https://stackoverflow.com/questions/2607289/converting-array-to-list-in-java/2607327#2607327
                    collectionRef.set(Arrays.asList((Object[])value));
                } else if (value instanceof Collection) {
                    collectionRef.set((Collection<? extends Object>) value);
                } else if (value instanceof Map) {
                    collectionRef.set(((Map<?,?>) value).entrySet());
                } else {
                    throw new JmriException(Bundle.getMessage("ForEachWithDelay_InvalidValue",
                                    value != null ? value.getClass().getName() : null));
                }
            });
        }

        if (ref.get() != null) throw ref.get();

        List<Object> list = new ArrayList<>(collectionRef.get());

        synchronized(this) {
            if (!_useIndividualTimers && (_defaultTimerTask != null)) {
                if (_resetIfAlreadyStarted) _defaultTimerTask.stopTimer();
                else return;
            }
            long timerDelay = _delay * _unit.getMultiply();
            long timerStart = System.currentTimeMillis();
            ConditionalNG conditonalNG = getConditionalNG();
            scheduleTimer(conditonalNG, conditonalNG.getSymbolTable(), timerDelay, timerStart, list, 0);
        }
    }

    /**
     * Get a new timer task.
     * @param conditionalNG  the ConditionalNG
     * @param symbolTable    the symbol table
     * @param timerDelay     the time the timer should wait
     * @param timerStart     the time when the timer was started
     */
    private ProtectedTimerTask getNewTimerTask(
            ConditionalNG conditionalNG,
            SymbolTable symbolTable,
            long timerDelay,
            long timerStart,
            List<? extends Object> list,
            int nextIndex)
            throws JmriException {

        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(symbolTable);

        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    synchronized(ForEachWithDelay.this) {
                        if (!_useIndividualTimers) _defaultTimerTask = null;
                        long currentTime = System.currentTimeMillis();
                        long currentTimerTime = currentTime - timerStart;
                        if (currentTimerTime < timerDelay) {
                            scheduleTimer(conditionalNG, newSymbolTable, timerDelay - currentTimerTime, currentTime, list, nextIndex);
                        } else {
                            InternalFemaleSocket internalSocket;
                            if (_useIndividualTimers) {
                                internalSocket = new InternalFemaleSocket();
                            } else {
                                internalSocket = _defaultInternalSocket;
                            }
                            internalSocket.conditionalNG = conditionalNG;
                            internalSocket.newSymbolTable = newSymbolTable;
                            internalSocket.newSymbolTable.setValue(_variableName, list.get(nextIndex));
                            conditionalNG.execute(internalSocket);

                            if (nextIndex+1 < list.size()) {
                                scheduleTimer(conditionalNG, newSymbolTable, timerDelay, currentTime, list, nextIndex+1);
                            }
                        }
                    }
                } catch (RuntimeException | JmriException e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }

    private void scheduleTimer(
            ConditionalNG conditionalNG,
            SymbolTable symbolTable,
            long timerDelay,
            long timerStart,
            List<? extends Object> list,
            int nextIndex)
            throws JmriException {

        synchronized(ForEachWithDelay.this) {
            if (!_useIndividualTimers && (_defaultTimerTask != null)) {
                _defaultTimerTask.stopTimer();
            }
            ProtectedTimerTask timerTask =
                    getNewTimerTask(conditionalNG, symbolTable, timerDelay, timerStart, list, nextIndex);
            if (!_useIndividualTimers) {
                _defaultTimerTask = timerTask;
            }
            TimerUtil.schedule(timerTask, timerDelay);
        }
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
        return Bundle.getMessage(locale, "ForEachWithDelay_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_useCommonSource) {
            return Bundle.getMessage(locale, "ForEachWithDelay_Long_Common",
                    _commonManager.toString(), _variableName, _socket.getName(), _unit.getTimeWithUnit(_delay),
                    _resetIfAlreadyStarted
                            ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_ResetRepeat"))
                            : Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_IgnoreRepeat")),
                    _useIndividualTimers
                            ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_UseIndividualTimers"))
                            : "");
        } else {
            switch (_userSpecifiedSource) {
                case Variable:
                    return Bundle.getMessage(locale, "ForEachWithDelay_Long_LocalVariable",
                            _selectVariable.getDescription(locale), _variableName, _socket.getName(), _unit.getTimeWithUnit(_delay),
                            _resetIfAlreadyStarted
                                    ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_ResetRepeat"))
                                    : Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_IgnoreRepeat")),
                            _useIndividualTimers
                                    ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_UseIndividualTimers"))
                                    : "");

                case Memory:
                    return Bundle.getMessage(locale, "ForEachWithDelay_Long_Memory",
                            _selectMemoryNamedBean.getDescription(locale), _variableName, _socket.getName(), _unit.getTimeWithUnit(_delay),
                            _resetIfAlreadyStarted
                                    ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_ResetRepeat"))
                                    : Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_IgnoreRepeat")),
                            _useIndividualTimers
                                    ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_UseIndividualTimers"))
                                    : "");

                case Formula:
                    return Bundle.getMessage(locale, "ForEachWithDelay_Long_Formula",
                            _formula, _variableName, _socket.getName(), _unit.getTimeWithUnit(_delay),
                            _resetIfAlreadyStarted
                                    ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_ResetRepeat"))
                                    : Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_IgnoreRepeat")),
                            _useIndividualTimers
                                    ? Bundle.getMessage("ForEachWithDelay_Options", Bundle.getMessage("ForEachWithDelay_UseIndividualTimers"))
                                    : "");

                default:
                    throw new IllegalArgumentException("_variableOperation has invalid value: " + _userSpecifiedSource.name());
            }
        }
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
            if ( !_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {

                String socketSystemName = _socketSystemName;
                _socket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _socket.disconnect();
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
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
        if (!_listenersAreRegistered) {
            if (_userSpecifiedSource == UserSpecifiedSource.Memory) {
                _selectMemoryNamedBean.registerListeners();
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_userSpecifiedSource == UserSpecifiedSource.Memory) {
                _selectMemoryNamedBean.unregisterListeners();
            }
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }


    public enum UserSpecifiedSource {
        Variable(Bundle.getMessage("ForEachWithDelay_UserSpecifiedSource_Variable")),
        Memory(Bundle.getMessage("ForEachWithDelay_UserSpecifiedSource_Memory")),
        Formula(Bundle.getMessage("ForEachWithDelay_UserSpecifiedSource_Formula"));

        private final String _text;

        private UserSpecifiedSource(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
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
            if (conditionalNG == null) { throw new NullPointerException("conditionalNG is null"); }
            if (_socket != null) {
                SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                conditionalNG.setSymbolTable(newSymbolTable);
                _socket.execute();
                conditionalNG.setSymbolTable(oldSymbolTable);
            }
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForEachWithDelay.class);

}
