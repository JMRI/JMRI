package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Executes an action only one time.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class RunOnce extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    private boolean _hasExecuted = false;

    public RunOnce(String sys, String user) {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("RunOnce_Socket_Action"));
    }

    public RunOnce(String sys, String user, String socketName, String socketSystemName)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, socketName);
        _socketSystemName = socketSystemName;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        RunOnce copy = new RunOnce(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.FLOW_CONTROL;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (!_hasExecuted && _socket != null) {
            _socket.execute();
        }
        _hasExecuted = true;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index == 0) {
            return _socket;
        } else {
            throw new IllegalArgumentException(String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == this._socket) {
            this._socketSystemName = socket.getConnectedSocket().getSystemName();
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == this._socket) {
            this._socketSystemName = null;
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "RunOnce_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "RunOnce_Long");
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setSocketSystemName(String systemName) {
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


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RunOnce.class);

}
