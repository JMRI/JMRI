package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleAnalogActionSocket;
import jmri.jmrit.logixng.FemaleAnalogExpressionSocket;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Executes an analog action with the result of an analog expression.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DoAnalogAction
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _analogExpressionSocketSystemName;
    private String _analogActionSocketSystemName;
    private final FemaleAnalogExpressionSocket _analogExpressionSocket;
    private final FemaleAnalogActionSocket _analogActionSocket;
    
    public DoAnalogAction(String sys, String user) {
        super(sys, user);
        _analogExpressionSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _analogActionSocket = InstanceManager.getDefault(AnalogActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DoAnalogAction copy = new DoAnalogAction(sysName, userName);
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
        double result = _analogExpressionSocket.evaluate();
        
        _analogActionSocket.setValue(result);
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _analogExpressionSocket;
                
            case 1:
                return _analogActionSocket;
                
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
        if (socket == _analogExpressionSocket) {
            _analogExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _analogActionSocket) {
            _analogActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _analogExpressionSocket) {
            _analogExpressionSocketSystemName = null;
        } else if (socket == _analogActionSocket) {
            _analogActionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DoAnalogAction_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DoAnalogAction_Long", _analogExpressionSocket.getName(), _analogActionSocket.getName());
    }

    public FemaleAnalogActionSocket getAnalogActionSocket() {
        return _analogActionSocket;
    }

    public String getAnalogActionSocketSystemName() {
        return _analogActionSocketSystemName;
    }

    public void setAnalogActionSocketSystemName(String systemName) {
        _analogActionSocketSystemName = systemName;
    }

    public FemaleAnalogExpressionSocket getAnalogExpressionSocket() {
        return _analogExpressionSocket;
    }

    public String getAnalogExpressionSocketSystemName() {
        return _analogExpressionSocketSystemName;
    }

    public void setAnalogExpressionSocketSystemName(String systemName) {
        _analogExpressionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_analogExpressionSocket.isConnected()
                    || !_analogExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_analogExpressionSocketSystemName)) {
                
                String socketSystemName = _analogExpressionSocketSystemName;
                
                _analogExpressionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _analogExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression " + socketSystemName);
                    }
                }
            } else {
                _analogExpressionSocket.getConnectedSocket().setup();
            }
            
            if (!_analogActionSocket.isConnected()
                    || !_analogActionSocket.getConnectedSocket().getSystemName()
                            .equals(_analogActionSocketSystemName)) {
                
                String socketSystemName = _analogActionSocketSystemName;
                
                _analogActionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _analogActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog action " + socketSystemName);
                    }
                }
            } else {
                _analogActionSocket.getConnectedSocket().setup();
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoAnalogAction.class);
    
}
