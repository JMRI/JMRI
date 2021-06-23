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
public class DigitalBooleanOnChange extends AbstractDigitalBooleanAction
        implements FemaleSocketListener {

    /**
     * The trigger of Action.
     */
    public enum Trigger {
        CHANGE_TO_TRUE(Bundle.getMessage("DigitalBooleanOnChange_Trigger_ChangeToTrue")),
        CHANGE_TO_FALSE(Bundle.getMessage("DigitalBooleanOnChange_Trigger_ChangeToFalse")),
        CHANGE(Bundle.getMessage("DigitalBooleanOnChange_Trigger_Change"));

        private final String _text;

        private Trigger(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }
    }

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    Trigger _trigger = Trigger.CHANGE;
    
    public DigitalBooleanOnChange(String sys, String user, Trigger trigger) {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
        _trigger = trigger;
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalBooleanActionManager manager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalBooleanOnChange copy = new DigitalBooleanOnChange(sysName, userName, _trigger);
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
    public void execute(boolean hasChangedToTrue, boolean hasChangedToFalse) throws JmriException {
        if (_socket.isConnected()) {
            switch (_trigger) {
                case CHANGE_TO_TRUE:
                    // Call execute() if change to true
                    if (hasChangedToTrue) {
                        _socket.execute();
                    }
                    break;
                case CHANGE_TO_FALSE:
                    // Call execute() if change to false
                    if (hasChangedToFalse) {
                        _socket.execute();
                    }
                    break;
                case CHANGE:
                    // Always call execute()
                    _socket.execute();
                    break;
                default:
                    throw new UnsupportedOperationException("_whichChange has unknown value: "+_trigger);
            }
        }
    }

    /**
     * Get the type.
     * @return the trigger
     */
    public Trigger getTrigger() {
        return _trigger;
    }
    
    /**
     * Set the type.
     * @param trigger the trigger
     */
    public void setTrigger(Trigger trigger) {
        _trigger = trigger;
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
        return Bundle.getMessage(locale, "DigitalBooleanOnChange_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DigitalBooleanOnChange_Long", _trigger.toString());
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
                        log.error("cannot load digital action " + socketSystemName);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalBooleanOnChange.class);

}
