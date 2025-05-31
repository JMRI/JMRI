package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Emulates Logix.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class Logix extends AbstractDigitalAction
        implements FemaleSocketListener {

    private ExecuteType _executeType = ExecuteType.ExecuteOnChange;
    private boolean _lastExpressionResult = false;
    private String _expressionSocketSystemName;
    private String _actionSocketSystemName;
    private final FemaleDigitalExpressionSocket _expressionSocket;
    private final FemaleDigitalBooleanActionSocket _actionSocket;

    public Logix(String sys, String user) {
        super(sys, user);
        _expressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _actionSocket = InstanceManager.getDefault(DigitalBooleanActionManager.class)
                .createFemaleSocket(this, this, "A");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Logix copy = new Logix(sysName, userName);
        copy.setComment(getComment());
        copy.setExecuteType(_executeType);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.OTHER;
    }

    /**
     * Get the execute type.
     * @return the type
     */
    public ExecuteType getExecuteType() {
        return _executeType;
    }

    /**
     * Set the execute type.
     * @param type the type
     */
    public void setExecuteType(ExecuteType type) {
        _executeType = type;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        boolean result = _expressionSocket.evaluate();

        if ((_executeType == ExecuteType.ExecuteAlways) || (result != _lastExpressionResult)) {
            _actionSocket.execute(result);
        }
        _lastExpressionResult = result;
    }

    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _expressionSocket;

            case 1:
                return _actionSocket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _expressionSocket) {
            _expressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _actionSocket) {
            _actionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _expressionSocket) {
            _expressionSocketSystemName = null;
        } else if (socket == _actionSocket) {
            _actionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Logix_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Logix_Long", _executeType.toString());
    }

    public FemaleDigitalExpressionSocket getExpressionSocket() {
        return _expressionSocket;
    }

    public String getExpressionSocketSystemName() {
        return _expressionSocketSystemName;
    }

    public void setExpressionSocketSystemName(String systemName) {
        _expressionSocketSystemName = systemName;
    }

    public FemaleDigitalBooleanActionSocket getActionSocket() {
        return _actionSocket;
    }

    public String getActionSocketSystemName() {
        return _actionSocketSystemName;
    }

    public void setActionSocketSystemName(String systemName) {
        _actionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_expressionSocket.isConnected()
                    || !_expressionSocket.getConnectedSocket().getSystemName()
                            .equals(_expressionSocketSystemName)) {

                String socketSystemName = _expressionSocketSystemName;
                _expressionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _expressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression {}", socketSystemName);
                    }
                }
            } else {
                _expressionSocket.getConnectedSocket().setup();
            }

            if ( !_actionSocket.isConnected()
                    || !_actionSocket.getConnectedSocket().getSystemName()
                            .equals(_actionSocketSystemName)) {

                String socketSystemName = _actionSocketSystemName;
                _actionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalBooleanActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _actionSocket.disconnect();
                    if (maleSocket != null) {
                        _actionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital boolean action {}", socketSystemName);
                    }
                }
            } else {
                _actionSocket.getConnectedSocket().setup();
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
    public enum ExecuteType {
        /**
         * The "then" or "else" action is executed when the expression changes
         * its result. If the expression has returned "false", but now returns
         * "true", the "then" action is executed. If the expression has
         * returned "true", but now returns "false", the "else" action is executed.
         */
        ExecuteOnChange(Bundle.getMessage("Logix_ExecuteOnChange")),

        /**
         * The "then" or "else" action is always executed when this action is
         * executed. If the expression returns "true", the "then" action is
         * executed. If the expression returns "false", the "else" action is
         * executed.
         */
        ExecuteAlways(Bundle.getMessage("Logix_ExecuteAlways"));

        private final String _text;

        private ExecuteType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Logix.class);

}
