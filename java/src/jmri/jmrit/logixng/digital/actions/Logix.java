package jmri.jmrit.logixng.digital.actions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emulates Logix.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Logix extends AbstractDigitalAction
        implements FemaleSocketListener, DigitalActionWithEnableExecution {

    private Logix _template;
    private boolean _enableExecution;
    private boolean _lastExpressionResult = false;
    private String _ifExpressionSocketSystemName;
    private String _thenActionSocketSystemName;
    private String _elseActionSocketSystemName;
    private final FemaleDigitalExpressionSocket _ifExpressionSocket;
    private final FemaleDigitalActionSocket _thenActionSocket;
    private final FemaleDigitalActionSocket _elseActionSocket;
    
    public Logix(String sys, String user) {
        super(sys, user);
        _ifExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _thenActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A1");
        _elseActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A2");
    }
    
    private Logix(Logix template) {
        super(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        _template = template;
        _ifExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, _template._ifExpressionSocket.getName());
        _thenActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, _template._thenActionSocket.getName());
        _elseActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, _template._elseActionSocket.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new Logix(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsEnableExecution() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnableExecution(boolean b) {
        _enableExecution = b;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExecutionEnabled() {
        return _enableExecution;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    @Override
    public void evaluateOnly() {
        _lastExpressionResult = _ifExpressionSocket.evaluate();
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        _lastExpressionResult = _ifExpressionSocket.evaluate();

        if (_lastExpressionResult) {
            _thenActionSocket.execute();
        } else {
            _elseActionSocket.execute();
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _ifExpressionSocket;
                
            case 1:
                return _thenActionSocket;
                
            case 2:
                return _elseActionSocket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 3;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _ifExpressionSocket) {
            _ifExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _thenActionSocket) {
            _thenActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _elseActionSocket) {
            _elseActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _ifExpressionSocket) {
            _ifExpressionSocketSystemName = null;
        } else if (socket == _thenActionSocket) {
            _thenActionSocketSystemName = null;
        } else if (socket == _elseActionSocket) {
            _elseActionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "IfThenElse_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "IfThenElse_Long",
                _ifExpressionSocket.getName(),
                _thenActionSocket.getName(),
                _elseActionSocket.getName());
    }

    public FemaleDigitalExpressionSocket getIfExpressionSocket() {
        return _ifExpressionSocket;
    }

    public String getIfExpressionSocketSystemName() {
        return _ifExpressionSocketSystemName;
    }

    public void setIfExpressionSocketSystemName(String systemName) {
        _ifExpressionSocketSystemName = systemName;
    }

    public FemaleDigitalActionSocket getThenActionSocket() {
        return _thenActionSocket;
    }

    public String getThenExpressionSocketSystemName() {
        return _thenActionSocketSystemName;
    }

    public void setThenActionSocketSystemName(String systemName) {
        _thenActionSocketSystemName = systemName;
    }

    public FemaleDigitalActionSocket getElseActionSocket() {
        return _elseActionSocket;
    }

    public String getElseExpressionSocketSystemName() {
        return _elseActionSocketSystemName;
    }

    public void setElseActionSocketSystemName(String systemName) {
        _elseActionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_ifExpressionSocket.isConnected()
                    || !_ifExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_ifExpressionSocketSystemName)) {
                
                String socketSystemName = _ifExpressionSocketSystemName;
                _ifExpressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBeanBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _ifExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression " + socketSystemName);
                    }
                }
            } else {
                _ifExpressionSocket.getConnectedSocket().setup();
            }
            
            if ( !_thenActionSocket.isConnected()
                    || !_thenActionSocket.getConnectedSocket().getSystemName()
                            .equals(_thenActionSocketSystemName)) {
                
                String socketSystemName = _thenActionSocketSystemName;
                _thenActionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBeanBySystemName(socketSystemName);
                    _thenActionSocket.disconnect();
                    if (maleSocket != null) {
                        _thenActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _thenActionSocket.getConnectedSocket().setup();
            }
            
            if ( !_elseActionSocket.isConnected()
                    || !_elseActionSocket.getConnectedSocket().getSystemName()
                            .equals(_elseActionSocketSystemName)) {
                
                String socketSystemName = _elseActionSocketSystemName;
                _elseActionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBeanBySystemName(socketSystemName);
                    _elseActionSocket.disconnect();
                    if (maleSocket != null) {
                        _elseActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _elseActionSocket.getConnectedSocket().setup();
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

    private final static Logger log = LoggerFactory.getLogger(Logix.class);

}
