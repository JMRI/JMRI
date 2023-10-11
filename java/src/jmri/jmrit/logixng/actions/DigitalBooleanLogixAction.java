package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Executes an action depending on the parameter.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class DigitalBooleanLogixAction extends AbstractDigitalBooleanAction
        implements FemaleSocketListener {

    /**
     * The trigger of Action.
     */
    public enum When {
        True(Bundle.getMessage("DigitalBooleanLogixAction_When_True")),
        False(Bundle.getMessage("DigitalBooleanLogixAction_When_False")),
        Either(Bundle.getMessage("DigitalBooleanLogixAction_When_Either"));

        private final String _text;

        private When(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }
    }

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    When _when = When.Either;

    public DigitalBooleanLogixAction(String sys, String user, When trigger) {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
        _when = trigger;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalBooleanActionManager manager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalBooleanLogixAction copy = new DigitalBooleanLogixAction(sysName, userName, _when);
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
    public void execute(boolean value) throws JmriException {
        if (_socket.isConnected()) {
            switch (_when) {
                case True:
                    // Call execute() if true
                    if (value) {
                        _socket.execute();
                    }
                    break;
                case False:
                    // Call execute() if false
                    if (!value) {
                        _socket.execute();
                    }
                    break;
                case Either:
                    // Always call execute()
                    _socket.execute();
                    break;
                default:
                    throw new UnsupportedOperationException("_whichChange has unknown value: "+_when);
            }
        }
    }

    /**
     * Get the type.
     * @return the trigger
     */
    public When getTrigger() {
        return _when;
    }

    /**
     * Set the type.
     * @param trigger the trigger
     */
    public void setTrigger(When trigger) {
        _when = trigger;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _socket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DigitalBooleanLogixAction_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DigitalBooleanLogixAction_Long", _when.toString());
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setActionSocketSystemName(String systemName) {
        _socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {

                String socketSystemName = _socketSystemName;
                _socket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _socket.disconnect();
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
                    }
                }
            } else {
                _socket.getConnectedSocket().setup();
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalBooleanLogixAction.class);

}
