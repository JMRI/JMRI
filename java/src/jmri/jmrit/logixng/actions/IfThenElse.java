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
public class IfThenElse extends AbstractDigitalAction
        implements FemaleSocketListener {

    private Type _type = Type.ExecuteOnChange;
    private TriState _lastExpressionResult = TriState.Unknown;
    private String _ifExpressionSocketSystemName;
    private String _thenActionSocketSystemName;
    private String _elseActionSocketSystemName;
    private final FemaleDigitalExpressionSocket _ifExpressionSocket;
    private final FemaleDigitalActionSocket _thenActionSocket;
    private final FemaleDigitalActionSocket _elseActionSocket;
    
    public IfThenElse(String sys, String user) {
        super(sys, user);
        _ifExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "If");
        _thenActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "Then");
        _elseActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "Else");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        IfThenElse copy = new IfThenElse(sysName, userName);
        copy.setComment(getComment());
        copy.setType(_type);
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
        boolean result = _ifExpressionSocket.evaluate();
        TriState _expressionResult = TriState.getValue(result);
        
        // _lastExpressionResult may be Unknown
        if ((_type == Type.AlwaysExecute) || (_expressionResult != _lastExpressionResult)) {
            if (result) {
                _thenActionSocket.execute();
            } else {
                _elseActionSocket.execute();
            }
            
            // Last expression result must be stored as a tri state value, since
            // we must know if the old value is known or not.
            _lastExpressionResult = _expressionResult;
        }
    }

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
        return Bundle.getMessage(locale, "IfThenElse_Long", _type.toString());
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

    public String getElseActionSocketSystemName() {
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
    
    
    /**
     * The type of Action. If the type is changed, the action is aborted if it
     * is currently running.
     */
    public enum Type {
        /**
         * The "then" or "else" action is executed when the expression changes
         * its result. If the expression has returned "false", but now returns
         * "true", the "then" action is executed. If the expression has
         * returned "true", but now returns "false", the "else" action is executed.
         */
        ExecuteOnChange(Bundle.getMessage("IfThenElse_ExecuteOnChange")),
        
        /**
         * The "then" or "else" action is always executed when this action is
         * executed. If the expression returns "true", the "then" action is
         * executed. If the expression returns "false", the "else" action is
         * executed.
         */
        AlwaysExecute(Bundle.getMessage("IfThenElse_AlwaysExecute"));
        
        private final String _text;
        
        private Type(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private static enum TriState {
        Unknown,
        False,
        True;
        
        public static TriState getValue(boolean value) {
            return value ? True : False;
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IfThenElse.class);

}
