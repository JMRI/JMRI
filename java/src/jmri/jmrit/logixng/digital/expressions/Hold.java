package jmri.jmrit.logixng.digital.expressions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Expression that keeps its status even if its child expression doesn't.
 * 
 * This expression stays False until both the 'hold' expression and the 'trigger'
 * expression becomes True. It stays true until the 'hold' expression goes to
 * False. The 'trigger' expression can for example be a push button that stays
 * True for a short time.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Hold extends AbstractDigitalExpression implements FemaleSocketListener {

    private String _holdExpressionSocketSystemName;
    private String _triggerExpressionSocketSystemName;
    private final FemaleDigitalExpressionSocket _holdExpressionSocket;
    private final FemaleDigitalExpressionSocket _triggerExpressionSocket;
    private boolean _isActive = false;
    
    public Hold(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        
        super(sys, user);
        
        _holdExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E1");
        _triggerExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E2");
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
    public boolean evaluate() throws JmriException {
        if (_isActive) {
            _isActive = _holdExpressionSocket.evaluate();
        } else {
            _isActive = _holdExpressionSocket.evaluate() && _triggerExpressionSocket.evaluate();
        }
        return _isActive;
    }
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        _holdExpressionSocket.reset();
        _triggerExpressionSocket.reset();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _holdExpressionSocket;
                
            case 1:
                return _triggerExpressionSocket;
                
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
        if (socket == _holdExpressionSocket) {
            _holdExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _triggerExpressionSocket) {
            _triggerExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }
    
    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _holdExpressionSocket) {
            _holdExpressionSocketSystemName = null;
        } else if (socket == _triggerExpressionSocket) {
            _triggerExpressionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Hold_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Hold_Long",
                _holdExpressionSocket.getName(),
                _triggerExpressionSocket.getName());
    }

    public String getHoldActionSocketSystemName() {
        return _holdExpressionSocketSystemName;
    }

    public void setHoldActionSocketSystemName(String systemName) {
        _holdExpressionSocketSystemName = systemName;
    }

    public String getTriggerExpressionSocketSystemName() {
        return _triggerExpressionSocketSystemName;
    }

    public void setTriggerExpressionSocketSystemName(String systemName) {
        _triggerExpressionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_holdExpressionSocket.isConnected()
                    || !_holdExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_holdExpressionSocketSystemName)) {
                
                String socketSystemName = _holdExpressionSocketSystemName;
                _holdExpressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _holdExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression " + socketSystemName);
                    }
                }
            } else {
                _holdExpressionSocket.getConnectedSocket().setup();
            }
            
            if ( !_triggerExpressionSocket.isConnected()
                    || !_triggerExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_triggerExpressionSocketSystemName)) {
                
                String socketSystemName = _triggerExpressionSocketSystemName;
                _triggerExpressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _triggerExpressionSocket.disconnect();
                    if (maleSocket != null) {
                        _triggerExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _triggerExpressionSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
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

    private final static Logger log = LoggerFactory.getLogger(Hold.class);

}
