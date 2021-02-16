package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.util.TimerUnit;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ProtectedTimerTask;
import jmri.util.TimerUtil;

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
    private int _delay;
    private TimerUnit _unit = TimerUnit.MilliSeconds;
    private boolean _resetIfAlreadyStarted;
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
        copy.setDelay(_delay);
        copy.setUnit(_unit);
        copy.setResetIfAlreadyStarted(_resetIfAlreadyStarted);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
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
    
    /**
     * Get a new timer task.
     */
    private ProtectedTimerTask getNewTimerTask(ConditionalNG conditionalNG) {
        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    synchronized(ExecuteDelayed.this) {
                        _timerTask = null;
                        long currentTimerTime = System.currentTimeMillis() - _timerStart;
                        if (currentTimerTime < _timerDelay) {
                            scheduleTimer(conditionalNG, _timerDelay - currentTimerTime);
                        } else {
                            conditionalNG.execute(_socket);
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }
    
    private void scheduleTimer(ConditionalNG conditionalNG, long delay) {
        if (_timerTask != null) _timerTask.stopTimer();
        _timerTask = getNewTimerTask(conditionalNG);
        TimerUtil.schedule(_timerTask, delay);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        synchronized(this) {
            if (_timerTask != null) {
                if (_resetIfAlreadyStarted) _timerTask.stopTimer();
                else return;
            }
            _timerDelay = _delay * _unit.getMultiply();
            _timerStart = System.currentTimeMillis();
            scheduleTimer(getConditionalNG(), _delay * _unit.getMultiply());
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
        return Bundle.getMessage(locale,
                "ExecuteDelayed_Long",
                _socket.getName(),
                _unit.getTimeWithUnit(_delay),
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteDelayed.class);
    
}
