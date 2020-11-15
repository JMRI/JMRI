package jmri.jmrit.logixng.implementation;

import java.util.Locale;

import static jmri.NamedBean.UNKNOWN;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.util.*;

/**
 * The default implementation of ConditionalNG.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class DefaultConditionalNG extends AbstractBase
        implements ConditionalNG, FemaleSocketListener {
    
    private MaleSocket.ErrorHandlingType _errorHandlingType = MaleSocket.ErrorHandlingType.LOG_ERROR;
    private Base _parent = null;
    private String _socketSystemName = null;
    private final FemaleDigitalActionSocket _femaleSocket;
    private boolean _enabled = true;
    private Base.Lock _lock = Base.Lock.NONE;
    private final ExecuteLock executeLock = new ExecuteLock();
    private boolean _runOnGUIDelayed = true;
    
    
    public DefaultConditionalNG(String sys, String user) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(ConditionalNG_Manager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
        _femaleSocket = new DefaultFemaleDigitalActionSocket(this, this, "A");
    }
    
    @Override
    public Base getParent() {
        return _parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        _parent = parent;
        
        if (isActive()) registerListeners();
        else unregisterListeners();
    }
    
    /** {@inheritDoc} */
    @Override
    public FemaleSocket getFemaleSocket() {
        return _femaleSocket;
    }

    /** {@inheritDoc} */
    @Override
    public void setRunOnGUIDelayed(boolean value) {
        _runOnGUIDelayed = value;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getRunOnGUIDelayed() {
        return _runOnGUIDelayed;
    }
    
    private void runOnGUI(@Nonnull ThreadingUtil.ThreadAction ta) {
        if (_runOnGUIDelayed) {
            ThreadingUtil.runOnGUIEventually(ta);
        } else {
            ThreadingUtil.runOnGUI(ta);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (executeLock.once()) {
            runOnGUI(() -> {
                while (executeLock.loop()) {
                    if (isEnabled()) {
                        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable();
                        
                        try {
                            InstanceManager.getDefault(LogixNG_Manager.class).setSymbolTable(newSymbolTable);
                            
                            _femaleSocket.execute();
                        } catch (JmriException | RuntimeException e) {
                            switch (_errorHandlingType) {
                                case LOG_ERROR_ONCE:
                                    LoggingUtil.warnOnce(log, "ConditionalNG {} got an exception during execute: {}",
                                            getSystemName(), e, e);
                                    break;
                                    
//                                case SHOW_DIALOG_BOX:
//                                    InstanceManager.getDefault(ErrorHandlerManager.class)
//                                            .notifyError(this, Bundle.getMessage("ExceptionExecute", getSystemName(), e), e);
//                                    break;
                                    
                                case LOG_ERROR:
                                    // fall through
                                default:
                                    log.error("ConditionalNG {} got an exception during execute: {}",
                                            getSystemName(), e, e);
                            }
                        }
                        
                        InstanceManager.getDefault(LogixNG_Manager.class).setSymbolTable(newSymbolTable.getPrevSymbolTable());
                    }
                }
            });
        }
    }
    
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameConditionalNG");
    }
    
    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in DefaultConditionalNG.");  // NOI18N
    }
    
    @Override
    public int getState() {
        log.warn("Unexpected call to getState in DefaultConditionalNG.");  // NOI18N
        return UNKNOWN;
    }

    /*.*
     * Set enabled status. Enabled is a bound property All conditionals are set
     * to UNKNOWN state and recalculated when the Logix is enabled, provided the
     * Logix has been previously activated.
     *./
    @Override
    public void setEnabled(boolean state) {

        boolean old = _enabled;
        _enabled = state;
        if (old != state) {
/*            
            boolean active = _isActivated;
            deActivateLogix();
            activateLogix();
            _isActivated = active;
            for (int i = _listeners.size() - 1; i >= 0; i--) {
                _listeners.once(i).setEnabled(state);
            }
            firePropertyChange("Enabled", Boolean.valueOf(old), Boolean.valueOf(state));  // NOI18N
*/            
//        }
//    }

    /*.*
     * Get enabled status
     */
//    @Override
//    public boolean getEnabled() {
//        return _enabled;
//    }

    @Override
    public void connected(FemaleSocket socket) {
        _socketSystemName = socket.getConnectedSocket().getSystemName();
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        _socketSystemName = null;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return "ConditionalNG: "+getDisplayName();
    }

    @Override
    public String getLongDescription(Locale locale) {
        return "ConditionalNG: "+getDisplayName();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index != 0) {
            throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
        
        return _femaleSocket;
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    public MaleSocket.ErrorHandlingType getErrorHandlingType() {
        return _errorHandlingType;
    }
    
    public void setErrorHandlingType(MaleSocket.ErrorHandlingType errorHandlingType)
    {
        _errorHandlingType = errorHandlingType;
    }
    
    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Lock getLock() {
        return _lock;
    }

    @Override
    public void setLock(Lock lock) {
        _lock = lock;
    }

    public void setSocketSystemName(String systemName) {
        if ((systemName == null) || (!systemName.equals(_socketSystemName))) {
            _femaleSocket.disconnect();
        }
        _socketSystemName = systemName;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    /** {@inheritDoc} */
    @Override
    final public void setup() {
        if (!_femaleSocket.isConnected()
                || !_femaleSocket.getConnectedSocket().getSystemName()
                        .equals(_socketSystemName)) {
            
            _femaleSocket.disconnect();
            
            if (_socketSystemName != null) {
                try {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(_socketSystemName);
                    if (maleSocket != null) {
                        _femaleSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("digital action is not found: " + _socketSystemName);
                    }
                } catch (SocketAlreadyConnectedException ex) {
                    // This shouldn't happen and is a runtime error if it does.
                    throw new RuntimeException("socket is already connected");
                }
            }
        } else {
            _femaleSocket.setup();
        }
    }

    /** {@inheritDoc} */
    @Override
    final public void disposeMe() {
        _femaleSocket.dispose();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        _enabled = enable;
        if (isActive()) {
            LogixNG logixNG = getLogixNG();
            if ((logixNG != null) && logixNG.isActive()) {
                registerListeners();
                execute();
            }
        } else {
            unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return _enabled;
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNG.class);

}
