package jmri.jmrit.logixng.implementation;

import java.util.Locale;
import java.util.Map;

import static jmri.NamedBean.UNKNOWN;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.*;

/**
 * The default implementation of ConditionalNG.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class DefaultConditionalNG extends AbstractBase
        implements ConditionalNG, FemaleSocketListener {

    private final LogixNG_Thread _thread;
    private int _startupThreadId;
    private Base _parent = null;
    private String _socketSystemName = null;
    private final FemaleDigitalActionSocket _femaleSocket;
    private boolean _enabled = true;
    private Base.Lock _lock = Base.Lock.NONE;
    private final ExecuteLock _executeLock = new ExecuteLock();
    private boolean _runDelayed = true;
    private final Stack _stack = new DefaultStack();
    private SymbolTable _symbolTable;


    public DefaultConditionalNG(String sys, String user)
            throws BadUserNameException, BadSystemNameException  {
        this(sys, user, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
    }

    public DefaultConditionalNG(String sys, String user, int threadID)
            throws BadUserNameException, BadSystemNameException  {
        super(sys, user);

        _startupThreadId = threadID;
        _thread = LogixNG_Thread.getThread(threadID);
        _thread.setThreadInUse();

        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(ConditionalNG_Manager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
        _femaleSocket = new DefaultFemaleDigitalActionSocket(this, this, "A");
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Thread getCurrentThread() {
        return _thread;
    }

    /** {@inheritDoc} */
    @Override
    public int getStartupThreadId() {
        return _startupThreadId;
    }

    /** {@inheritDoc} */
    @Override
    public void setStartupThreadId(int threadId) {
        int oldStartupThreadId = _startupThreadId;
        _startupThreadId = threadId;
        firePropertyChange("Thread", oldStartupThreadId, _startupThreadId);
    }

    /** {@inheritDoc} */
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
    public FemaleDigitalActionSocket getFemaleSocket() {
        return _femaleSocket;
    }

    /** {@inheritDoc} */
    @Override
    public void setRunDelayed(boolean value) {
        _runDelayed = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getRunDelayed() {
        return _runDelayed;
    }

    private void runOnLogixNG_Thread(
            @Nonnull ThreadingUtil.ThreadAction ta,
            boolean allowRunDelayed) {

        if (_runDelayed && allowRunDelayed) {
            _thread.runOnLogixNGEventually(ta);
        } else {
            _thread.runOnLogixNG(ta);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_executeLock.once()) {
            runOnLogixNG_Thread(new ExecuteTask(this, _executeLock, null), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute(boolean allowRunDelayed) {
        if (_executeLock.once()) {
            runOnLogixNG_Thread(new ExecuteTask(this, _executeLock, null), allowRunDelayed);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute(FemaleDigitalActionSocket socket) {
        runOnLogixNG_Thread(() -> {internalExecute(this, socket);}, true);
    }

    private static void internalExecute(ConditionalNG conditionalNG, FemaleDigitalActionSocket femaleSocket) {
        if (conditionalNG.isEnabled()) {
            DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG);

            try {
                conditionalNG.setSymbolTable(newSymbolTable);
                if (femaleSocket != null) {
                    femaleSocket.execute();
                } else {
                    conditionalNG.getFemaleSocket().execute();
                }
            } catch (JmriException | RuntimeException e) {
//                LoggingUtil.warnOnce(log, "ConditionalNG {} got an exception during execute: {}",
//                        conditionalNG.getSystemName(), e, e);
                log.warn("ConditionalNG {} got an exception during execute: {}",
                        conditionalNG.getSystemName(), e, e);
            }

            conditionalNG.setSymbolTable(newSymbolTable.getPrevSymbolTable());
        }
    }

    private static class ExecuteTask implements ThreadingUtil.ThreadAction {

        private final ConditionalNG _conditionalNG;
        private final ExecuteLock _executeLock;
        private final FemaleDigitalActionSocket _localFemaleSocket;

        public ExecuteTask(ConditionalNG conditionalNG, ExecuteLock executeLock, FemaleDigitalActionSocket femaleSocket) {
            _conditionalNG = conditionalNG;
            _executeLock = executeLock;
            _localFemaleSocket = femaleSocket;
        }

        @Override
        public void run() {
            while (_executeLock.loop()) {
                internalExecute(_conditionalNG, _localFemaleSocket);
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public Stack getStack() {
        return _stack;
    }

    /** {@inheritDoc} */
    @Override
    public SymbolTable getSymbolTable() {
        return _symbolTable;
    }

    /** {@inheritDoc} */
    @Override
    public void setSymbolTable(SymbolTable symbolTable) {
        _symbolTable = symbolTable;
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
        if (_thread.getThreadId() != LogixNG_Thread.DEFAULT_LOGIXNG_THREAD) {
            return "ConditionalNG: "+getDisplayName() + " on thread " + _thread.getThreadName();
        }
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

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNG.class);

}
