package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.util.ProtectedTimerTask;
import jmri.util.TimerUtil;

/**
 * Executes an action after some time.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionTimer extends AbstractDigitalAction
        implements FemaleSocketListener {

    public static final int EXPRESSION_START = 0;
    public static final int EXPRESSION_STOP = 1;
    
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    private final List<ActionEntry> _actionEntries = new ArrayList<>();
    private ProtectedTimerTask _timerTask;
    private int _currentTimer = -1;
    private TimerState _timerState = TimerState.Off;
    private boolean _startImmediately = false;
    private boolean _runContinuously = false;
    private Unit _unit = Unit.MilliSeconds;
    
    public ActionTimer(String sys, String user) {
        super(sys, user);
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("TimerSocketStart"))));
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, Bundle.getMessage("TimerSocketStop"))));
        _actionEntries
                .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
    }
    
    public ActionTimer(String sys, String user,
            List<Map.Entry<String, String>> expressionSystemNames,
            List<ActionData> actionDataList)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
        setActionData(actionDataList);
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
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    /**
     * Get a new timer task.
     */
    private ProtectedTimerTask getNewTimerTask() {
        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    _timerState = TimerState.Completed;
                    getConditionalNG().execute();
                } catch (Exception e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (_expressionEntries.get(EXPRESSION_STOP)._socket.isConnected()
                && _expressionEntries.get(EXPRESSION_STOP)._socket.evaluate()) {
            synchronized(this) {
                if (_timerTask != null) _timerTask.stopTimer();
                _timerTask = null;
            }
            _timerState = TimerState.Off;
            return;
        }
        
        if (_expressionEntries.get(EXPRESSION_START)._socket.isConnected()
                && _expressionEntries.get(EXPRESSION_START)._socket.evaluate()) {
            synchronized(this) {
                if (_timerTask != null) {
                    _timerTask.stopTimer();
                    _timerTask = null;
                }
            }
            _currentTimer = -1;
            _timerState = TimerState.WaitToRun;
        }
        
        if (_timerState == TimerState.Off) return;
        if (_timerState == TimerState.Running) return;
        
        int startTimer = _currentTimer;
        while ((_timerState == TimerState.WaitToRun) || (_timerState == TimerState.Completed)) {
            // If the timer has passed full time, execute the action
            if ((_timerState == TimerState.Completed) && _actionEntries.get(_currentTimer)._socket.isConnected()) {
                _actionEntries.get(_currentTimer)._socket.execute();
            }
            
            // Start new timer
            _currentTimer++;
            if (_currentTimer >= _actionEntries.size()) _currentTimer = 0;
            
            ActionEntry ae = _actionEntries.get(_currentTimer);
            if (ae._delay > 0) {
                synchronized(this) {
                    _timerTask = getNewTimerTask();
                    TimerUtil.schedule(_timerTask, ae._delay * _unit._multiply);
                }
                return;
            }
            
            if (startTimer == _currentTimer) {
                // If we get here, all timers has a delay of 0 ms
                _timerState = TimerState.Off;
                return;
            }
        }
    }

    /**
     * Get the type.
     * @param actionSocket the socket
     * @return the delay
     */
    public int getDelay(int actionSocket) {
        return _actionEntries.get(actionSocket)._delay;
    }
    
    /**
     * Set the type.
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
    public boolean getStartImmediately() {
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
    
    /**
     * Get the unit
     * @return the unit
     */
    public Unit getUnit() {
        return _unit;
    }
    
    /**
     * Set the unit
     * @param unit the unit
     */
    public void setUnit(Unit unit) {
        _unit = unit;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index == EXPRESSION_START) return _expressionEntries.get(EXPRESSION_START)._socket;
        if (index == EXPRESSION_STOP) return _expressionEntries.get(EXPRESSION_STOP)._socket;
        if ((index < 0) || (index >= (_expressionEntries.size() + _actionEntries.size()))) {
            throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
        return _actionEntries.get(index - _expressionEntries.size())._socket;
    }

    @Override
    public int getChildCount() {
        return _expressionEntries.size() + _actionEntries.size();
    }

    @Override
    public void connected(FemaleSocket socket) {
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
        return Bundle.getMessage(locale, "ActionTimer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionTimer_Long");
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
    
    public void setNumActions(int num) {
        int numActions = _actionEntries.size();
        
        while (_actionEntries.size() > num) {
            ActionEntry ae = _actionEntries.get(num);
            if (ae._socket.isConnected()) {
                throw new IllegalArgumentException("Cannot remove sockets that are connected");
            }
            _actionEntries.remove(_actionEntries.size()-1);
        }
        
        while (_actionEntries.size() < num) {
            _actionEntries
                    .add(new ActionEntry(InstanceManager.getDefault(DigitalActionManager.class)
                            .createFemaleSocket(this, this, getNewSocketName())));
        }
        
        if (numActions != _actionEntries.size()) {
            firePropertyChange(Base.PROPERTY_CHILD_COUNT, null, this);
        }
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
        for (ExpressionEntry ee : _expressionEntries) {
            try {
                if ( !ee._socket.isConnected()
                        || !ee._socket.getConnectedSocket().getSystemName()
                                .equals(ee._socketSystemName)) {

                    String socketSystemName = ee._socketSystemName;
                    ee._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(DigitalActionManager.class)
                                        .getBySystemName(socketSystemName);
                        ee._socket.disconnect();
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
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            if (_startImmediately) {
                _timerState = TimerState.WaitToRun;
                getConditionalNG().execute();
            }
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        synchronized(this) {
            // stopTimer() will not return until the timer task
            // is cancelled and stopped.
            if (_timerTask != null) _timerTask.stopTimer();
            _timerTask = null;
        }
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        synchronized(this) {
            if (_timerTask != null) _timerTask.stopTimer();
            _timerTask = null;
        }
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
    
    
    public enum Unit {
        MilliSeconds(1, Bundle.getMessage("ActionTimer_UnitMilliSeconds")),
        Seconds(1000, Bundle.getMessage("ActionTimer_UnitSeconds")),
        Minutes(1000*60, Bundle.getMessage("ActionTimer_UnitMinutes")),
        Hours(1000*60*60, Bundle.getMessage("ActionTimer_UnitHours"));
        
        private long _multiply;
        private final String _text;
        
        private Unit(long multiply, String text) {
            this._multiply = multiply;
            this._text = text;
        }
        
        public long getMultiply() {
            return _multiply;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private enum TimerState {
        Off,
        WaitToRun,
        Running,
        Completed,
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTimer.class);

}
