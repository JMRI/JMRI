package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;


/**
 * Priority First in, First out Queue
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class Delay
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _timeExpressionSocketSystemName;
    private final FemaleAnalogExpressionSocket _timeExpressionSocket;
    
    public Delay(String sys, String user) {
        super(sys, user);
        _timeExpressionSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, "Time");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Delay copy = new Delay(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
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
    public void execute() throws JmriException {
        double time = _timeExpressionSocket.evaluate();
        
        try {
            Thread.sleep(Math.round(time));
        } catch (InterruptedException ex) {
            log.warn("Delay was aborted by InterruptedException", ex);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _timeExpressionSocket;
                
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
        if (socket == _timeExpressionSocket) {
            _timeExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _timeExpressionSocket) {
            _timeExpressionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Delay_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Delay_Long", _timeExpressionSocket.getName());
    }

    public FemaleAnalogExpressionSocket getAnalogExpressionSocket() {
        return _timeExpressionSocket;
    }

    public String getAnalogExpressionSocketSystemName() {
        return _timeExpressionSocketSystemName;
    }

    public void setAnalogExpressionSocketSystemName(String systemName) {
        _timeExpressionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_timeExpressionSocket.isConnected()
                    || !_timeExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_timeExpressionSocketSystemName)) {
                
                String socketSystemName = _timeExpressionSocketSystemName;
                
                _timeExpressionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _timeExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression " + socketSystemName);
                    }
                }
            } else {
                _timeExpressionSocket.getConnectedSocket().setup();
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
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Delay.class);
    
}
