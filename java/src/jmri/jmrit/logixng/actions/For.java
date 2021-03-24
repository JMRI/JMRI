package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Executes an action when the expression is True.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class For extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _initActionSocketSystemName;
    private String _whileExpressionSocketSystemName;
    private String _afterEachActionSocketSystemName;
    private String _doActionSocketSystemName;
    private final FemaleDigitalActionSocket _initActionSocket;
    private final FemaleDigitalExpressionSocket _whileExpressionSocket;
    private final FemaleDigitalActionSocket _afterEachActionSocket;
    private final FemaleDigitalActionSocket _doActionSocket;
    
    public For(String sys, String user) {
        super(sys, user);
        _initActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("For_SocketName_Init"));
        _whileExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("For_SocketName_While"));
        _afterEachActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("For_SocketName_AfterEach"));
        _doActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("For_SocketName_Do"));
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        For copy = new For(sysName, userName);
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
    public boolean isExternal() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        _initActionSocket.execute();
        while (_whileExpressionSocket.evaluate()) {
            _doActionSocket.execute();
            _afterEachActionSocket.execute();
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _initActionSocket;
                
            case 1:
                return _whileExpressionSocket;
                
            case 2:
                return _afterEachActionSocket;
                
            case 3:
                return _doActionSocket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 4;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _initActionSocket) {
            _initActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _whileExpressionSocket) {
            _whileExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _afterEachActionSocket) {
            _afterEachActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _doActionSocket) {
            _doActionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _initActionSocket) {
            _initActionSocketSystemName = null;
        } else if (socket == _whileExpressionSocket) {
            _whileExpressionSocketSystemName = null;
        } else if (socket == _afterEachActionSocket) {
            _afterEachActionSocketSystemName = null;
        } else if (socket == _doActionSocket) {
            _doActionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "For_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "For_Long",
                _initActionSocket.getName(),
                _whileExpressionSocket.getName(),
                _afterEachActionSocket.getName(),
                _doActionSocket.getName());
    }

    public FemaleDigitalActionSocket getThenActionSocket() {
        return _initActionSocket;
    }

    public String getInitActionSocketSystemName() {
        return _initActionSocketSystemName;
    }

    public void setInitActionSocketSystemName(String systemName) {
        _initActionSocketSystemName = systemName;
    }

    public FemaleDigitalExpressionSocket getWhileExpressionSocket() {
        return _whileExpressionSocket;
    }

    public String getWhileExpressionSocketSystemName() {
        return _whileExpressionSocketSystemName;
    }

    public void setWhileExpressionSocketSystemName(String systemName) {
        _whileExpressionSocketSystemName = systemName;
    }

    public FemaleDigitalActionSocket getAfterEachActionSocket() {
        return _afterEachActionSocket;
    }

    public String getAfterEachExpressionSocketSystemName() {
        return _afterEachActionSocketSystemName;
    }

    public void setAfterEachActionSocketSystemName(String systemName) {
        _afterEachActionSocketSystemName = systemName;
    }

    public FemaleDigitalActionSocket getDoActionSocket() {
        return _doActionSocket;
    }

    public String getDoExpressionSocketSystemName() {
        return _doActionSocketSystemName;
    }

    public void setDoActionSocketSystemName(String systemName) {
        _doActionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_initActionSocket.isConnected()
                    || !_initActionSocket.getConnectedSocket().getSystemName()
                            .equals(_initActionSocketSystemName)) {
                
                String socketSystemName = _initActionSocketSystemName;
                _initActionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _initActionSocket.disconnect();
                    if (maleSocket != null) {
                        _initActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _initActionSocket.getConnectedSocket().setup();
            }
            
            if ( !_whileExpressionSocket.isConnected()
                    || !_whileExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_whileExpressionSocketSystemName)) {
                
                String socketSystemName = _whileExpressionSocketSystemName;
                _whileExpressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _whileExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression " + socketSystemName);
                    }
                }
            } else {
                _whileExpressionSocket.getConnectedSocket().setup();
            }
            
            if ( !_afterEachActionSocket.isConnected()
                    || !_afterEachActionSocket.getConnectedSocket().getSystemName()
                            .equals(_afterEachActionSocketSystemName)) {
                
                String socketSystemName = _afterEachActionSocketSystemName;
                _afterEachActionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _afterEachActionSocket.disconnect();
                    if (maleSocket != null) {
                        _afterEachActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _afterEachActionSocket.getConnectedSocket().setup();
            }
            
            if ( !_doActionSocket.isConnected()
                    || !_doActionSocket.getConnectedSocket().getSystemName()
                            .equals(_doActionSocketSystemName)) {
                
                String socketSystemName = _doActionSocketSystemName;
                _doActionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _doActionSocket.disconnect();
                    if (maleSocket != null) {
                        _doActionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _doActionSocket.getConnectedSocket().setup();
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(For.class);

}
