package jmri.jmrit.logixng.digital.actions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleStringActionSocket;
import jmri.jmrit.logixng.FemaleStringExpressionSocket;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.StringExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an string action with the result of an string expression.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class DoStringAction
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _stringExpressionSocketSystemName;
    private String _stringActionSocketSystemName;
    private final FemaleStringExpressionSocket _stringExpressionSocket;
    private final FemaleStringActionSocket _stringActionSocket;
    
    public DoStringAction(String sys, String user) {
        super(sys, user);
        _stringExpressionSocket = InstanceManager.getDefault(StringExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _stringActionSocket = InstanceManager.getDefault(StringActionManager.class)
                .createFemaleSocket(this, this, "A");
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
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        String result = _stringExpressionSocket.evaluate();
        
        _stringActionSocket.setValue(result);
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _stringExpressionSocket;
                
            case 1:
                return _stringActionSocket;
                
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
        if (socket == _stringExpressionSocket) {
            _stringExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _stringActionSocket) {
            _stringActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _stringExpressionSocket) {
            _stringExpressionSocketSystemName = null;
        } else if (socket == _stringActionSocket) {
            _stringActionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DoStringAction_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DoStringAction_Long", _stringExpressionSocket.getName(), _stringActionSocket.getName());
    }

    public FemaleStringActionSocket getStringActionSocket() {
        return _stringActionSocket;
    }

    public String getStringActionSocketSystemName() {
        return _stringActionSocketSystemName;
    }

    public void setStringActionSocketSystemName(String systemName) {
        _stringActionSocketSystemName = systemName;
    }

    public FemaleStringExpressionSocket getStringExpressionSocket() {
        return _stringExpressionSocket;
    }

    public String getStringExpressionSocketSystemName() {
        return _stringExpressionSocketSystemName;
    }

    public void setStringExpressionSocketSystemName(String systemName) {
        _stringExpressionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_stringExpressionSocket.isConnected()
                    || !_stringExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_stringExpressionSocketSystemName)) {
                
                String socketSystemName = _stringExpressionSocketSystemName;
                
                _stringExpressionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(StringExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _stringExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load string expression " + socketSystemName);
                    }
                }
            } else {
                _stringExpressionSocket.getConnectedSocket().setup();
            }
            
            if (!_stringActionSocket.isConnected()
                    || !_stringActionSocket.getConnectedSocket().getSystemName()
                            .equals(_stringActionSocketSystemName)) {
                
                String socketSystemName = _stringActionSocketSystemName;
                
                _stringActionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(StringActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _stringActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load string action " + socketSystemName);
                    }
                }
            } else {
                _stringActionSocket.getConnectedSocket().setup();
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
    
    private final static Logger log = LoggerFactory.getLogger(DoStringAction.class);
    
}
