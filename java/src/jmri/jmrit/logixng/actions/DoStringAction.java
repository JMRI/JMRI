package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

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
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DoStringAction copy = new DoStringAction(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoStringAction.class);
    
}
