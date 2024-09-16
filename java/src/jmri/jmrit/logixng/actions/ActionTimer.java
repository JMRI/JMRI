package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.util.TimerUnit;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ProtectedTimerTask;
import jmri.util.TimerUtil;

/**
 * Executes an action after some time.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionTimer extends AbstractDigitalAction
        implements FemaleSocketListener {

    private static class State{
        private ProtectedTimerTask _timerTask;
        private int _currentTimer = -1;
        private TimerState _timerState = TimerState.Off;
        private long _currentTimerDelay = 0;
        private long _currentTimerStart = 0;
        private boolean _startIsActive = false;
    }

    public static final int EXPRESSION_START = 0;
    public static final int EXPRESSION_STOP = 1;
    public static final int NUM_STATIC_EXPRESSIONS = 2;

    private String _startExpressionSocketSystemName;
    private String _stopExpressionSocketSystemName;
    private final FemaleDigitalExpressionSocket _startExpressionSocket;
    private final FemaleDigitalExpressionSocket _stopExpressionSocket;
    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private boolean _startImmediately = false;
    private boolean _runContinuously = false;
    private boolean _startAndStopByStartExpression = false;
    private TimerUnit _unit = TimerUnit.MilliSeconds;
    private boolean _delayByLocalVariables = false;
    private String _delayLocalVariablePrefix = "";  // An index is appended, for example Delay1, Delay2, ... Delay15.
    private final Map<ConditionalNG, State> _stateMap = new HashMap<>();


    public ActionTimer(String sys, String user) {
        super(sys, user);
        _startExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionTimerSocketStart"));
        _stopExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionTimerSocketStop"));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
    }

    public ActionTimer(String sys, String user,
            List<Map.Entry<String, String>> expressionSystemNames,
            List<ActionData> actionDataList)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _startExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionTimerSocketStart"));
        _stopExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionTimerSocketStop"));
        setActionData(actionDataList);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionTimer copy = new ActionTimer(sysName, userName);
        copy.setComment(getComment());
        copy.setNumActions(getNumActions());
        for (int i=0; i < getNumActions(); i++) {
            copy.setDelay(i, getDelay(i));
        }
        copy.setStartImmediately(_startImmediately);
        copy.setRunContinuously(_runContinuously);
        copy.setStartAndStopByStartExpression(_startAndStopByStartExpression);
        copy.setUnit(_unit);
        copy.setDelayByLocalVariables(_delayByLocalVariables);
        copy.setDelayLocalVariablePrefix(_delayLocalVariablePrefix);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    private void setActionData(List<ActionData> actionDataList) {
        if (!_actionEntries.isEmpty()) {
            throw new RuntimeException("action system names cannot be set more than once");
        }

        for (ActionData data : actionDataList) {
            FemaleDigitalActionSocket socket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, data._socketName);

            _actionEntries.add(new ActionEntry(data._delay, socket, data._socketSystemName));
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /**
     * Get a new timer task.
     */
    private ProtectedTimerTask getNewTimerTask(ConditionalNG conditionalNG, State state) {
        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    long currentTimerTime = System.currentTimeMillis() - state._currentTimerStart;
                    if (currentTimerTime < state._currentTimerDelay) {
                        scheduleTimer(conditionalNG, state, state._currentTimerDelay - currentTimerTime);
                    } else {
                        state._timerState = TimerState.Completed;
                        conditionalNG.execute();
                    }
                } catch (Exception e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }

    private void scheduleTimer(ConditionalNG conditionalNG, State state, long delay) {
        synchronized(this) {
            if (state._timerTask != null) {
                state._timerTask.stopTimer();
                state._timerTask = null;
            }
        }
        state._timerTask = getNewTimerTask(conditionalNG, state);
        TimerUtil.schedule(state._timerTask, delay);
    }

    private void schedule(ConditionalNG conditionalNG, SymbolTable symbolTable, State state) {
        synchronized(this) {
            long delay;

            if (_delayByLocalVariables) {
                delay = jmri.util.TypeConversionUtil
                        .convertToLong(symbolTable.getValue(
                                _delayLocalVariablePrefix + Integer.toString(state._currentTimer+1)));
            } else {
                delay = _actionEntries.get(state._currentTimer)._delay;
            }

            state._currentTimerDelay = delay * _unit.getMultiply();
            state._currentTimerStart = System.currentTimeMillis();
            state._timerState = TimerState.WaitToRun;
            scheduleTimer(conditionalNG, state, delay * _unit.getMultiply());
        }
    }

    private boolean start(State state) throws JmriException {
        boolean lastStartIsActive = state._startIsActive;
        state._startIsActive = _startExpressionSocket.isConnected() && _startExpressionSocket.evaluate();
        return state._startIsActive && !lastStartIsActive;
    }

    private boolean checkStart(ConditionalNG conditionalNG, SymbolTable symbolTable, State state) throws JmriException {
        if (start(state)) state._timerState = TimerState.RunNow;

        if (state._timerState == TimerState.RunNow) {
            synchronized(this) {
                if (state._timerTask != null) {
                    state._timerTask.stopTimer();
                    state._timerTask = null;
                }
            }
            state._currentTimer = 0;
            while (state._currentTimer < _actionEntries.size()) {
                ActionEntry ae = _actionEntries.get(state._currentTimer);
                if (ae._delay > 0) {
                    schedule(conditionalNG, symbolTable, state);
                    return true;
                }
                else {
                    state._currentTimer++;
                }
            }
            // If we get here, all timers have a delay of 0 ms
            state._timerState = TimerState.Off;
            return true;
        }

        return false;
    }

    private boolean stop(State state) throws JmriException {
        boolean stop;

        if (_startAndStopByStartExpression) {
            stop = _startExpressionSocket.isConnected() && !_startExpressionSocket.evaluate();
        } else {
            stop = _stopExpressionSocket.isConnected() && _stopExpressionSocket.evaluate();
        }

        if (stop) {
            synchronized(this) {
                if (state._timerTask != null) state._timerTask.stopTimer();
                state._timerTask = null;
            }
            state._timerState = TimerState.Off;
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ConditionalNG conditionalNG = getConditionalNG();
        State state = _stateMap.computeIfAbsent(conditionalNG, o -> new State());

        if (stop(state)) {
            state._startIsActive = false;
            return;
        }

        if (checkStart(conditionalNG, conditionalNG.getSymbolTable(), state)) return;

        if (state._timerState == TimerState.Off) return;
        if (state._timerState == TimerState.Running) return;

        int startTimer = state._currentTimer;
        while (state._timerState == TimerState.Completed) {
            // If the timer has passed full time, execute the action
            if ((state._timerState == TimerState.Completed) && _actionEntries.get(state._currentTimer)._socket.isConnected()) {
                _actionEntries.get(state._currentTimer)._socket.execute();
            }

            // Move to them next timer
            state._currentTimer++;
            if (state._currentTimer >= _actionEntries.size()) {
                state._currentTimer = 0;
                if (!_runContinuously) {
                    state._timerState = TimerState.Off;
                    return;
                }
            }

            ActionEntry ae = _actionEntries.get(state._currentTimer);
            if (ae._delay > 0) {
                schedule(conditionalNG, conditionalNG.getSymbolTable(), state);
                return;
            }

            if (startTimer == state._currentTimer) {
                // If we get here, all timers have a delay of 0 ms
                state._timerState = TimerState.Off;
            }
        }
    }

    /**
     * Get the delay.
     * @param actionSocket the socket
     * @return the delay
     */
    public int getDelay(int actionSocket) {
        return _actionEntries.get(actionSocket)._delay;
    }

    /**
     * Set the delay.
     * @param actionSocket the socket
     * @param delay the delay
     */
    public void setDelay(int actionSocket, int delay) {
        _actionEntries.get(actionSocket)._delay = delay;
    }

    /**
     * Get if to start immediately
     * @return true if to start immediately
     */
    public boolean isStartImmediately() {
        return _startImmediately;
    }

    /**
     * Set if to start immediately
     * @param startImmediately true if to start immediately
     */
    public void setStartImmediately(boolean startImmediately) {
        _startImmediately = startImmediately;
    }

    /**
     * Get if run continuously
     * @return true if run continuously
     */
    public boolean isRunContinuously() {
        return _runContinuously;
    }

    /**
     * Set if run continuously
     * @param runContinuously true if run continuously
     */
    public void setRunContinuously(boolean runContinuously) {
        _runContinuously = runContinuously;
    }

    /**
     * Is both start and stop is controlled by the start expression.
     * @return true if to start immediately
     */
    public boolean isStartAndStopByStartExpression() {
        return _startAndStopByStartExpression;
    }

    /**
     * Set if both start and stop is controlled by the start expression.
     * @param startAndStopByStartExpression true if to start immediately
     */
    public void setStartAndStopByStartExpression(boolean startAndStopByStartExpression) {
        _startAndStopByStartExpression = startAndStopByStartExpression;
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
     * Is delays given by local variables?
     * @return value true if delay is given by local variables
     */
    public boolean isDelayByLocalVariables() {
        return _delayByLocalVariables;
    }

    /**
     * Set if delays should be given by local variables.
     * @param value true if delay is given by local variables
     */
    public void setDelayByLocalVariables(boolean value) {
        _delayByLocalVariables = value;
    }

    /**
     * Is both start and stop is controlled by the start expression.
     * @return true if to start immediately
     */
    public String getDelayLocalVariablePrefix() {
        return _delayLocalVariablePrefix;
    }

    /**
     * Set if both start and stop is controlled by the start expression.
     * @param value true if to start immediately
     */
    public void setDelayLocalVariablePrefix(String value) {
        _delayLocalVariablePrefix = value;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index == EXPRESSION_START) return _startExpressionSocket;
        if (index == EXPRESSION_STOP) return _stopExpressionSocket;
        if ((index < 0) || (index >= (NUM_STATIC_EXPRESSIONS + _actionEntries.size()))) {
            throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
        return _actionEntries.get(index - NUM_STATIC_EXPRESSIONS)._socket;
    }

    @Override
    public int getChildCount() {
        return NUM_STATIC_EXPRESSIONS + _actionEntries.size();
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _startExpressionSocket) {
            _startExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _stopExpressionSocket) {
            _stopExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            for (ActionEntry entry : _actionEntries) {
                if (socket == entry._socket) {
                    entry._socketSystemName =
                            socket.getConnectedSocket().getSystemName();
                }
            }
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _startExpressionSocket) {
            _startExpressionSocketSystemName = null;
        } else if (socket == _stopExpressionSocket) {
            _stopExpressionSocketSystemName = null;
        } else {
            for (ActionEntry entry : _actionEntries) {
                if (socket == entry._socket) {
                    entry._socketSystemName = null;
                }
            }
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionTimer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String options = "";
        if (_delayByLocalVariables) {
            options = Bundle.getMessage("ActionTimer_Options_DelayByLocalVariable", _delayLocalVariablePrefix);
        }
        if (_startAndStopByStartExpression) {
            return Bundle.getMessage(locale, "ActionTimer_Long2",
                    Bundle.getMessage("ActionTimer_StartAndStopByStartExpression"), options);
        } else {
            return Bundle.getMessage(locale, "ActionTimer_Long", options);
        }
    }

    public FemaleDigitalExpressionSocket getStartExpressionSocket() {
        return _startExpressionSocket;
    }

    public String getStartExpressionSocketSystemName() {
        return _startExpressionSocketSystemName;
    }

    public void setStartExpressionSocketSystemName(String systemName) {
        _startExpressionSocketSystemName = systemName;
    }

    public FemaleDigitalExpressionSocket getStopExpressionSocket() {
        return _stopExpressionSocket;
    }

    public String getStopExpressionSocketSystemName() {
        return _stopExpressionSocketSystemName;
    }

    public void setStopExpressionSocketSystemName(String systemName) {
        _stopExpressionSocketSystemName = systemName;
    }

    public int getNumActions() {
        return _actionEntries.size();
    }

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
        try {
            if ( !_startExpressionSocket.isConnected()
                    || !_startExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_startExpressionSocketSystemName)) {

                String socketSystemName = _startExpressionSocketSystemName;
                _startExpressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _startExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression {}", socketSystemName);
                    }
                }
            } else {
                _startExpressionSocket.getConnectedSocket().setup();
            }

            if ( !_stopExpressionSocket.isConnected()
                    || !_stopExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_stopExpressionSocketSystemName)) {

                String socketSystemName = _stopExpressionSocketSystemName;
                _stopExpressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _stopExpressionSocket.disconnect();
                    if (maleSocket != null) {
                        _stopExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression {}", socketSystemName);
                    }
                }
            } else {
                _stopExpressionSocket.getConnectedSocket().setup();
            }

            for (ActionEntry ae : _actionEntries) {
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
                            log.error("cannot load digital action {}", socketSystemName);
                        }
                    }
                } else {
                    ae._socket.getConnectedSocket().setup();
                }
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
            _stateMap.computeIfAbsent(getConditionalNG(), o -> new State());
            _stateMap.forEach((conditionalNG, state) -> {
                // If _timerState is not TimerState.Off, the timer was running when listeners wss unregistered
                if ((_startImmediately) || (state._timerState != TimerState.Off)) {
                    if (state._timerState == TimerState.Off) {
                        state._timerState = TimerState.RunNow;
                    }
                    conditionalNG.execute();
                }
            });
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        synchronized(this) {
            _stateMap.forEach((conditionalNG, state) -> {
                // stopTimer() will not return until the timer task
                // is cancelled and stopped.
                if (state._timerTask != null) state._timerTask.stopTimer();
                state._timerTask = null;
                state._timerState = TimerState.Off;
            });
        }
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        synchronized(this) {
            _stateMap.forEach((conditionalNG, state) -> {
                if (state._timerTask != null) state._timerTask.stopTimer();
                state._timerTask = null;
            });
        }
    }


    private static class ActionEntry {
        private int _delay;
        private String _socketSystemName;
        private final FemaleDigitalActionSocket _socket;

        private ActionEntry(int delay, FemaleDigitalActionSocket socket, String socketSystemName) {
            _delay = delay;
            _socketSystemName = socketSystemName;
            _socket = socket;
        }

        private ActionEntry(FemaleDigitalActionSocket socket) {
            this._socket = socket;
        }

    }


    public static class ActionData {
        private int _delay;
        private String _socketName;
        private String _socketSystemName;

        public ActionData(int delay, String socketName, String socketSystemName) {
            _delay = delay;
            _socketName = socketName;
            _socketSystemName = socketSystemName;
        }
    }


    private enum TimerState {
        Off,
        RunNow,
        WaitToRun,
        Running,
        Completed,
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTimer.class);

}
