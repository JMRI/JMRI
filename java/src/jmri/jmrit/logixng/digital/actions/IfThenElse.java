package jmri.jmrit.logixng.digital.actions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
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
 * Executes an action when the expression is True.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class IfThenElse extends AbstractDigitalAction
        implements FemaleSocketListener, DigitalActionWithEnableExecution {

    /**
     * The type of Action. If the type is changed, the action is aborted if it
     * is currently running.
     */
    public enum Type {
        /**
         * Action is triggered when the expression is True. The action may
         * continue even if the expression becomes False.
         * 
         * If the expression is False and then True again before the action
         * is finished, action.executeAgain() is called instead of action.execute().
         * 
         * Note that in a tree of actions, some actions may have been finished
         * and some actions still running. In this case, the actions that are
         * still running will be called with executeAgain() but those actions
         * that are finished will be called with execute(). Actions that have
         * child actions need to deal with this.
         */
        TRIGGER_ACTION,
        
        /**
         * Action is executed when the expression is True but only as long as
         * the expression stays True. If the expression becomes False, the
         * action is aborted.
         */
        CONTINOUS_ACTION,
    }

    private boolean _enableExecution;
    private Type _type;
    private boolean _lastExpressionResult = false;
    private String _ifExpressionSocketSystemName;
    private String _thenActionSocketSystemName;
    private String _elseActionSocketSystemName;
    private final FemaleDigitalExpressionSocket _ifExpressionSocket;
    private final FemaleDigitalActionSocket _thenActionSocket;
    private final FemaleDigitalActionSocket _elseActionSocket;
    
    public IfThenElse(String sys, String user, Type type) {
        super(sys, user);
        _type = type;
        _ifExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _thenActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A1");
        _elseActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A2");
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
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
    public void evaluateOnly() throws JmriException {
        _lastExpressionResult = _ifExpressionSocket.evaluate();
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        _lastExpressionResult = _ifExpressionSocket.evaluate();

        if (_lastExpressionResult) {
            _thenActionSocket.execute();
        } else {
            _elseActionSocket.execute();
        }
    }

    /*.*
     * Continue execution of this Action.
     * This method is called if Type == TRIGGER_ACTION, the previous call to
     * one of the execute???() methods returned True and the expression is
     * still True.
     * 
     * @return true if this action is not finished.
     *./
    @Override
    public boolean executeContinue() {
        _isExpressionCompleted.set(true);
        switch (_type) {
            case TRIGGER_ACTION:
                _lastActionResult = _thenActionSocket.executeContinue();
                break;
                
            case CONTINOUS_ACTION:
                boolean exprResult = _ifExpressionSocket.evaluate(_isExpressionCompleted);
                if (exprResult) {
                    _lastActionResult = _thenActionSocket.executeContinue();
                } else {
                    _thenActionSocket.abort();
                    _lastActionResult = false;
                }
                break;
                
            default:
                throw new RuntimeException(String.format("Unknown type '%s'", _type.name()));
        }
        
        return _lastActionResult || !_isExpressionCompleted.get();
    }
*/
    /**
     * Get the type.
     * @return the type
     */
    public Type getType() {
        return _type;
    }
    
    /**
     * Set the type.
     * @param type the type
     */
    public void setType(Type type) {
        _type = type;
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
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
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
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
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

    public String getThenActionSocketSystemName() {
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
                                    .getBySystemName(socketSystemName);
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
                                    .getBySystemName(socketSystemName);
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
                                    .getBySystemName(socketSystemName);
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

    private final static Logger log = LoggerFactory.getLogger(IfThenElse.class);

}
