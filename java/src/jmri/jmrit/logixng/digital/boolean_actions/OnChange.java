package jmri.jmrit.logixng.digital.boolean_actions;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an action depending on the parameter.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class OnChange extends AbstractDigitalBooleanAction
        implements FemaleSocketListener {

    /**
     * The type of Action. If the type is changed, the action is aborted if it
     * is currently running.
     */
    public enum ChangeType {
        CHANGE_TO_TRUE,
        CHANGE_TO_FALSE,
        CHANGE,
    }

    private String _actionSocketSystemName;
    private final FemaleDigitalActionSocket _actionSocket;
    ChangeType _whichChange = ChangeType.CHANGE;
    
    public OnChange(String sys, String user, ChangeType whichChange) {
        super(sys, user);
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
        _whichChange = whichChange;
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
    public void execute(boolean hasChangedToTrue) throws JmriException {
        if (_actionSocket.isConnected()) {
            switch (_whichChange) {
                case CHANGE_TO_TRUE:
                    // Call execute() if change to true
                    if (hasChangedToTrue) {
                        _actionSocket.execute();
                    }
                    break;
                case CHANGE_TO_FALSE:
                    // Call execute() if change to false
                    if (!hasChangedToTrue) {
                        _actionSocket.execute();
                    }
                    break;
                case CHANGE:
                    // Always call execute()
                    _actionSocket.execute();
                    break;
                default:
                    throw new UnsupportedOperationException("_whichChange has unknown value: "+_whichChange);
            }
        }
    }

    /**
     * Get the type.
     * @return the type
     */
    public ChangeType getWhichChange() {
        return _whichChange;
    }
    
    /**
     * Set the type.
     * @param whichChange the type
     */
    public void setType(ChangeType whichChange) {
        _whichChange = whichChange;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _actionSocket;
                
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
        if (socket == _actionSocket) {
            _actionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _actionSocket) {
            _actionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "OnChange_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        switch (_whichChange) {
            case CHANGE_TO_TRUE:
                return Bundle.getMessage(locale, "OnChange_Long_ChangeToTrue");
                
            case CHANGE_TO_FALSE:
                return Bundle.getMessage(locale, "OnChange_Long_ChangeToFalse");
                
            case CHANGE:
                return Bundle.getMessage(locale, "OnChange_Long_Change");
                
            default:
                throw new UnsupportedOperationException("_whichChange has unknown value: "+_whichChange);
        }
    }

    public FemaleDigitalActionSocket getActionSocket() {
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
            if ( !_actionSocket.isConnected()
                    || !_actionSocket.getConnectedSocket().getSystemName()
                            .equals(_actionSocketSystemName)) {
                
                String socketSystemName = _actionSocketSystemName;
                _actionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _actionSocket.disconnect();
                    if (maleSocket != null) {
                        _actionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
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

    private final static Logger log = LoggerFactory.getLogger(OnChange.class);

}
