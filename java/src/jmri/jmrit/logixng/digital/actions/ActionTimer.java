package jmri.jmrit.logixng.digital.actions;

import java.util.Timer;
import java.util.TimerTask;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an action when the after some time.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionTimer extends AbstractDigitalAction
        implements FemaleSocketListener {

    private ActionTimer _template;
    private String _actionSocketSystemName;
    private final FemaleDigitalActionSocket _actionSocket;
    private final Timer _timer = new Timer(true);
    long _delay = 0;
    boolean _isActive = false;
    
    /**
     * Create a new instance of ActionIfThen and generate a new system name.
     */
    public ActionTimer() {
        super(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName());
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    public ActionTimer(String sys) {
        super(sys);
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    public ActionTimer(String sys, String user) {
        super(sys, user);
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    private ActionTimer(ActionTimer template, String sys) {
        super(sys);
        _template = template;
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, _template._actionSocket.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate(String sys) {
        return new ActionTimer(this, sys);
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
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_actionSocket.isConnected()) {
            _timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    _actionSocket.execute();
                }
            }, _delay);
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
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _actionSocket) {
            _actionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription() {
        return Bundle.getMessage("Timer_Short");
    }

    @Override
    public String getLongDescription() {
        return Bundle.getMessage("Timer_Long", _actionSocket.getName(), _delay);
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
                                    .getBeanBySystemName(socketSystemName);
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
        _timer.cancel();
    }

    private final static Logger log = LoggerFactory.getLogger(ActionTimer.class);

}
