package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Runs an engine.
 * This action reads an analog expression with the loco address and sets its
 * speed according to an alaog expression and the direction according to a
 * digital expression.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public final class ActionThrottle extends AbstractDigitalAction
        implements FemaleSocketListener {

    public static final int LOCO_ADDRESS_SOCKET = 0;
    public static final int LOCO_SPEED_SOCKET = LOCO_ADDRESS_SOCKET + 1;
    public static final int LOCO_DIRECTION_SOCKET = LOCO_SPEED_SOCKET + 1;
    public static final int LOCO_FUNCTION_SOCKET = LOCO_DIRECTION_SOCKET + 1;
    public static final int LOCO_FUNCTION_ONOFF_SOCKET = LOCO_FUNCTION_SOCKET + 1;
    public static final int NUM_LOCO_SOCKETS = LOCO_FUNCTION_ONOFF_SOCKET + 1;

    private SystemConnectionMemo _memo;
    private ThrottleManager _throttleManager;
    private ThrottleManager _oldThrottleManager;

    // The throttle if we have one or if a request is sent, null otherwise
    private DccThrottle _throttle;
    private ThrottleListener _throttleListener;

    private String _locoAddressSocketSystemName;
    private String _locoSpeedSocketSystemName;
    private String _locoDirectionSocketSystemName;
    private String _locoFunctionSocketSystemName;
    private String _locoFunctionOnOffSocketSystemName;
    private final FemaleAnalogExpressionSocket _locoAddressSocket;
    private final FemaleAnalogExpressionSocket _locoSpeedSocket;
    private final FemaleDigitalExpressionSocket _locoDirectionSocket;
    private final FemaleAnalogExpressionSocket _locoFunctionSocket;
    private final FemaleDigitalExpressionSocket _locoFunctionOnOffSocket;
    private boolean _stopLocoWhenSwitchingLoco = true;


    public ActionThrottle(String sys, String user) {
        super(sys, user);
        _locoAddressSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionThrottle_SocketName_Address"));
        _locoSpeedSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionThrottle_SocketName_Speed"));
        _locoDirectionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionThrottle_SocketName_Direction"));
        _locoFunctionSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionThrottle_SocketName_Function"));
        _locoFunctionOnOffSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionThrottle_SocketName_FunctionOnOff"));

        // Set the _throttleManager variable
        setMemo(null);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionThrottle copy = new ActionThrottle(sysName, userName);
        copy.setComment(getComment());
        copy.setMemo(_memo);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public void setMemo(SystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
        if (_memo != null) {
            _throttleManager = _memo.get(jmri.ThrottleManager.class);
            if (_throttleManager == null) {
                throw new IllegalArgumentException("Memo "+memo.getUserName()+" doesn't have a ThrottleManager");
            }
        } else {
            _throttleManager = InstanceManager.getDefault(ThrottleManager.class);
        }
    }

    public SystemConnectionMemo getMemo() {
        return _memo;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        int currentLocoAddress = -1;
        int newLocoAddress = -1;

        if (_throttle != null) {
            currentLocoAddress = _throttle.getLocoAddress().getNumber();
        }

        if (_locoAddressSocket.isConnected()) {
            newLocoAddress =
                    (int) ((MaleAnalogExpressionSocket)_locoAddressSocket.getConnectedSocket())
                            .evaluate();
        }

        if (_throttleManager != _oldThrottleManager) {
            currentLocoAddress = -1;    // Force request of new throttle
            _oldThrottleManager = _throttleManager;
        }

        if (newLocoAddress != currentLocoAddress) {

            if (_throttle != null) {
                if (_stopLocoWhenSwitchingLoco) {
                    // Stop the loco
                    _throttle.setSpeedSetting(0);
                }
                // Release the loco
                _throttleManager.releaseThrottle(_throttle, _throttleListener);
                _throttle = null;
            }

            if (newLocoAddress != -1) {

                _throttleListener =  new ThrottleListener() {
                    @Override
                    public void notifyThrottleFound(DccThrottle t) {
                        _throttle = t;
                        executeConditionalNG();
                    }

                    @Override
                    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                        log.warn("loco {} cannot be aquired", address.getNumber());
                    }

                    @Override
                    public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
                        log.warn("Loco {} cannot be aquired. Decision required.", address.getNumber());
                    }
                };

                boolean result = _throttleManager.requestThrottle(newLocoAddress, _throttleListener);

                if (!result) {
                    log.warn("loco {} cannot be aquired", newLocoAddress);
                }
            }

        }

        // We have a throttle if _throttle is not null
        if (_throttle != null) {

            double speed = 0;
            boolean isForward = true;
            int function = 0;
            boolean isFunctionOn = true;

            if (_locoSpeedSocket.isConnected()) {
                speed =
                        ((MaleAnalogExpressionSocket)_locoSpeedSocket.getConnectedSocket())
                                .evaluate();
            }

            if (_locoDirectionSocket.isConnected()) {
                isForward =
                        ((MaleDigitalExpressionSocket)_locoDirectionSocket.getConnectedSocket())
                                .evaluate();
            }

            if (_locoFunctionSocket.isConnected()) {
                function = (int) Math.round(
                        ((MaleAnalogExpressionSocket)_locoFunctionSocket.getConnectedSocket())
                                .evaluate());
            }

            if (_locoFunctionOnOffSocket.isConnected()) {
                isFunctionOn =
                        ((MaleDigitalExpressionSocket)_locoFunctionOnOffSocket.getConnectedSocket())
                                .evaluate();
            }

            DccThrottle throttle = _throttle;
            float spd = (float) speed;
            boolean fwd = isForward;
            int func = function;
            boolean funcState = isFunctionOn;
            jmri.util.ThreadingUtil.runOnLayoutWithJmriException(() -> {
                if (_locoSpeedSocket.isConnected()) throttle.setSpeedSetting(spd);
                if (_locoDirectionSocket.isConnected()) throttle.setIsForward(fwd);
                if (_locoFunctionSocket.isConnected() && _locoFunctionOnOffSocket.isConnected()) {
                    throttle.setFunction(func, funcState);
                }
            });
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case LOCO_ADDRESS_SOCKET:
                return _locoAddressSocket;

            case LOCO_SPEED_SOCKET:
                return _locoSpeedSocket;

            case LOCO_DIRECTION_SOCKET:
                return _locoDirectionSocket;

            case LOCO_FUNCTION_SOCKET:
                return _locoFunctionSocket;

            case LOCO_FUNCTION_ONOFF_SOCKET:
                return _locoFunctionOnOffSocket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return NUM_LOCO_SOCKETS;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _locoAddressSocket) {
            _locoAddressSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else if (socket == _locoSpeedSocket) {
            _locoSpeedSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else if (socket == _locoDirectionSocket) {
            _locoDirectionSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else if (socket == _locoFunctionSocket) {
            _locoFunctionSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else if (socket == _locoFunctionOnOffSocket) {
            _locoFunctionOnOffSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _locoAddressSocket) {
            if (_throttle != null) {
                // Stop the loco
                _throttle.setSpeedSetting(0);
                // Release the loco
                _throttleManager.releaseThrottle(_throttle, _throttleListener);
            }
            _locoAddressSocketSystemName = null;
            executeConditionalNG();
        } else if (socket == _locoSpeedSocket) {
            _locoSpeedSocketSystemName = null;
            executeConditionalNG();
        } else if (socket == _locoDirectionSocket) {
            _locoDirectionSocketSystemName = null;
            executeConditionalNG();
        } else if (socket == _locoFunctionSocket) {
            _locoFunctionSocketSystemName = null;
            executeConditionalNG();
        } else if (socket == _locoFunctionOnOffSocket) {
            _locoFunctionOnOffSocketSystemName = null;
            executeConditionalNG();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    private void executeConditionalNG() {
        if (_listenersAreRegistered) {
            ConditionalNG c = getConditionalNG();
            if (c != null) {
                c.execute();
            }
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionThrottle_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_memo != null) {
            return Bundle.getMessage(locale, "ActionThrottle_LongConnection",
                    _memo.getUserName());
        } else {
            return Bundle.getMessage(locale, "ActionThrottle_Long");
        }
    }

    public FemaleAnalogExpressionSocket getLocoAddressSocket() {
        return _locoAddressSocket;
    }

    public String getLocoAddressSocketSystemName() {
        return _locoAddressSocketSystemName;
    }

    public void setLocoAddressSocketSystemName(String systemName) {
        _locoAddressSocketSystemName = systemName;
    }

    public FemaleAnalogExpressionSocket getLocoSpeedSocket() {
        return _locoSpeedSocket;
    }

    public String getLocoSpeedSocketSystemName() {
        return _locoSpeedSocketSystemName;
    }

    public void setLocoSpeedSocketSystemName(String systemName) {
        _locoSpeedSocketSystemName = systemName;
    }

    public FemaleDigitalExpressionSocket getLocoDirectionSocket() {
        return _locoDirectionSocket;
    }

    public String getLocoDirectionSocketSystemName() {
        return _locoDirectionSocketSystemName;
    }

    public void setLocoDirectionSocketSystemName(String systemName) {
        _locoDirectionSocketSystemName = systemName;
    }

    public FemaleAnalogExpressionSocket getLocoFunctionSocket() {
        return _locoFunctionSocket;
    }

    public String getLocoFunctionSocketSystemName() {
        return _locoFunctionSocketSystemName;
    }

    public void setLocoFunctionSocketSystemName(String systemName) {
        _locoFunctionSocketSystemName = systemName;
    }

    public FemaleDigitalExpressionSocket getLocoFunctionOnOffSocket() {
        return _locoFunctionOnOffSocket;
    }

    public String getLocoFunctionOnOffSocketSystemName() {
        return _locoFunctionOnOffSocketSystemName;
    }

    public void setLocoFunctionOnOffSocketSystemName(String systemName) {
        _locoFunctionOnOffSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_locoAddressSocket.isConnected()
                    || !_locoAddressSocket.getConnectedSocket().getSystemName()
                            .equals(_locoAddressSocketSystemName)) {

                String socketSystemName = _locoAddressSocketSystemName;
                _locoAddressSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _locoAddressSocket.disconnect();
                    if (maleSocket != null) {
                        _locoAddressSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression {}", socketSystemName);
                    }
                }
            } else {
                _locoAddressSocket.getConnectedSocket().setup();
            }

            if ( !_locoSpeedSocket.isConnected()
                    || !_locoSpeedSocket.getConnectedSocket().getSystemName()
                            .equals(_locoSpeedSocketSystemName)) {

                String socketSystemName = _locoSpeedSocketSystemName;
                _locoSpeedSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _locoSpeedSocket.disconnect();
                    if (maleSocket != null) {
                        _locoSpeedSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression {}", socketSystemName);
                    }
                }
            } else {
                _locoSpeedSocket.getConnectedSocket().setup();
            }

            if ( !_locoDirectionSocket.isConnected()
                    || !_locoDirectionSocket.getConnectedSocket().getSystemName()
                            .equals(_locoDirectionSocketSystemName)) {

                String socketSystemName = _locoDirectionSocketSystemName;
                _locoDirectionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _locoDirectionSocket.disconnect();
                    if (maleSocket != null) {
                        _locoDirectionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression {}", socketSystemName);
                    }
                }
            } else {
                _locoDirectionSocket.getConnectedSocket().setup();
            }

            if ( !_locoFunctionSocket.isConnected()
                    || !_locoFunctionSocket.getConnectedSocket().getSystemName()
                            .equals(_locoFunctionSocketSystemName)) {

                String socketSystemName = _locoFunctionSocketSystemName;
                _locoFunctionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _locoFunctionSocket.disconnect();
                    if (maleSocket != null) {
                        _locoFunctionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression {}", socketSystemName);
                    }
                }
            } else {
                _locoFunctionSocket.getConnectedSocket().setup();
            }

            if ( !_locoFunctionOnOffSocket.isConnected()
                    || !_locoFunctionOnOffSocket.getConnectedSocket().getSystemName()
                            .equals(_locoFunctionOnOffSocketSystemName)) {

                String socketSystemName = _locoFunctionOnOffSocketSystemName;
                _locoFunctionOnOffSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _locoFunctionOnOffSocket.disconnect();
                    if (maleSocket != null) {
                        _locoFunctionOnOffSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression {}", socketSystemName);
                    }
                }
            } else {
                _locoFunctionOnOffSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }

    public boolean isStopLocoWhenSwitchingLoco() {
        return _stopLocoWhenSwitchingLoco;
    }

    public void setStopLocoWhenSwitchingLoco(boolean value) {
        _stopLocoWhenSwitchingLoco = value;
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (_throttle != null) {
            _throttleManager.releaseThrottle(_throttle, _throttleListener);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionThrottle.class);

}
