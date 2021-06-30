package jmri.jmrit.logixng.expressions;

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
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

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

    private String _triggerExpressionSocketSystemName;
    private String _holdExpressionSocketSystemName;
    private final FemaleDigitalExpressionSocket _triggerExpressionSocket;
    private final FemaleDigitalExpressionSocket _holdExpressionSocket;
    private boolean _isActive = false;
    
    public Hold(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        
        super(sys, user);
        
        _triggerExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("Hold_SocketName_Trigger"));
        _holdExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("Hold_SocketName_Hold"));
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Hold copy = new Hold(sysName, userName);
        copy.setComment(getComment());
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
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
            _isActive = _holdExpressionSocket.evaluate()
                    || _triggerExpressionSocket.evaluate();
        } else {
            _isActive = _triggerExpressionSocket.evaluate();
        }
        return _isActive;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _triggerExpressionSocket;
                
            case 1:
                return _holdExpressionSocket;
                
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
        if (socket == _triggerExpressionSocket) {
            _triggerExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _holdExpressionSocket) {
            _holdExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }
    
    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _triggerExpressionSocket) {
            _triggerExpressionSocketSystemName = null;
        } else if (socket == _holdExpressionSocket) {
            _holdExpressionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Hold_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Hold_Long",
                _triggerExpressionSocket.getName(),
                _holdExpressionSocket.getName());
    }

    public String getTriggerExpressionSocketSystemName() {
        return _triggerExpressionSocketSystemName;
    }

    public void setTriggerExpressionSocketSystemName(String systemName) {
        _triggerExpressionSocketSystemName = systemName;
    }

    public String getHoldActionSocketSystemName() {
        return _holdExpressionSocketSystemName;
    }

    public void setHoldActionSocketSystemName(String systemName) {
        _holdExpressionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
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
                        log.error("cannot load digital expression " + socketSystemName);
                    }
                }
            } else {
                _triggerExpressionSocket.getConnectedSocket().setup();
            }
            
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Hold.class);

}
