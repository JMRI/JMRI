package jmri.jmrit.logixng.digital.actions;

import java.util.Locale;
import jmri.util.TimerUtil;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.util.ProtectedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an action when the after some time.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionTimer extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _actionSocketSystemName;
    private final FemaleDigitalActionSocket _actionSocket;
    private ProtectedTimerTask _timerTask;
    private boolean _listenersAreRegistered = false;
    private long _delay = 0;
    
    public ActionTimer(String sys, String user) {
        super(sys, user);
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
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
        final jmri.jmrit.logixng.ConditionalNG c = getConditionalNG();
        
        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    _actionSocket.execute();
                } catch (Exception e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_actionSocket.isConnected()) {
            synchronized(this) {
                if (_timerTask != null) _timerTask.stopTimer();
                _timerTask = getNewTimerTask();
                TimerUtil.schedule(_timerTask, _delay);
            }
        }
    }

    /**
     * Get the type.
     */
    public long getDelay() {
        return _delay;
    }
    
    /**
     * Set the type.
     */
    public void setDelay(long delay) {
        _delay = delay;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _actionSocket;
                
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
        if (socket == _actionSocket) {
            _actionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _actionSocket) {
            _actionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Timer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Timer_Long", _actionSocket.getName(), _delay);
    }

    public FemaleDigitalActionSocket getThenActionSocket() {
        return _actionSocket;
    }

    public String getTimerActionSocketSystemName() {
        return _actionSocketSystemName;
    }

    public void setTimerActionSocketSystemName(String systemName) {
        _actionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
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
        if (!_listenersAreRegistered) {
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

    private final static Logger log = LoggerFactory.getLogger(ActionTimer.class);

}
