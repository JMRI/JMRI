package jmri.jmrit.logixng.implementation;

import static jmri.NamedBean.UNKNOWN;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.util.ThreadingUtil;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.FemaleDigitalActionSocket;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.Log4JUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final FemaleDigitalActionSocket _femaleActionSocket;
    private boolean _enabled = true;
    private final ExecuteLock executeLock = new ExecuteLock();
    private boolean _runOnGUIDelayed = true;
    
    
    public DefaultConditionalNG(String sys, String user) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(ConditionalNG_Manager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
        _femaleActionSocket = InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(this, this, "");
    }
    
    @Override
    public Base getParent() {
        return _parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public FemaleSocket getFemaleSocket() {
        return _femaleActionSocket;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsEnableExecution() {
        
        // This action does not support EnableExecution if the user may change
        // the child.
        if (getLock().isChangeableByUser()) {
            return false;
        }
        
        return _femaleActionSocket.supportsEnableExecution();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnableExecution(boolean b) {
        if (supportsEnableExecution()
                && (_femaleActionSocket instanceof DigitalActionWithEnableExecution)) {
            ((DigitalActionWithEnableExecution)_femaleActionSocket).setEnableExecution(b);
        } else {
            log.error("This conditionalNG does not supports the method setEnableExecution()");
            throw new UnsupportedOperationException("This digital action does not supports the method setEnableExecution()");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExecutionEnabled() {
        if (supportsEnableExecution()
                && (_femaleActionSocket instanceof DigitalActionWithEnableExecution)) {
            return ((DigitalActionWithEnableExecution)_femaleActionSocket).isExecutionEnabled();
        } else {
            log.error("This conditionalNG does not supports the method isExecutionEnabled()");
            throw new UnsupportedOperationException("This digital action does not supports the method isExecutionEnabled()");
        }
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
//                        _femaleActionSocket.execute();
                        try {
                            _femaleActionSocket.execute();
                        } catch (JmriException | RuntimeException e) {
                            switch (_errorHandlingType) {
                                case LOG_ERROR_ONCE:
                                    Log4JUtil.warnOnce(log, "female socket {} thrown an exception: {}", _femaleActionSocket.toString(), e);
                                    break;
                                    
                                case SHOW_DIALOG_BOX:
                                    // We don't show a dialog box yet so fall thrue.
                                    // fall through
                                case LOG_ERROR:
                                    // fall through
                                default:
                                    log.error("female socket {} thrown an exception: {}", _femaleActionSocket.toString(), e);
                            }
                        }
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

    private final static Logger log = LoggerFactory.getLogger(DefaultConditionalNG.class);

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
        throw new UnsupportedOperationException("Not supported.");
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
        
        return _femaleActionSocket;
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
        return Lock.NONE;
    }

    @Override
    public void setLock(Lock lock) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void setSocketSystemName(String systemName) {
        if ((systemName == null) || (!systemName.equals(_socketSystemName))) {
            _femaleActionSocket.disconnect();
        }
        _socketSystemName = systemName;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    /** {@inheritDoc} */
    @Override
    final public void setup() {
        if (!_femaleActionSocket.isConnected()
                || !_femaleActionSocket.getConnectedSocket().getSystemName()
                        .equals(_socketSystemName)) {
            
            _femaleActionSocket.disconnect();
            
            if (_socketSystemName != null) {
                try {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(_socketSystemName);
                    if (maleSocket != null) {
                        _femaleActionSocket.connect(maleSocket);
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
            _femaleActionSocket.setup();
        }
    }

    /** {@inheritDoc} */
    @Override
    final public void disposeMe() {
        _femaleActionSocket.dispose();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        _enabled = enable;
        if (enable) {
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

}
