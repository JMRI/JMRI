package jmri.jmrit.logixng.implementation;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Stack;
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
    private boolean _executeAtStartup = true;
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
        if (_executeAtStartup || !getLogixNG().isStartup()) {
            if (_executeLock.once()) {
                runOnLogixNG_Thread(new ExecuteTask(this, _executeLock, null), true);
            }
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

    /**
     * Executes a LogixNG Module.
     * @param module      The module to be executed
     * @param parameters  The parameters
     */
    public static void executeModule(Module module, Map<String, Object> parameters)
            throws IllegalArgumentException {

        if (module == null) {
            throw new IllegalArgumentException("The parameter \"module\" is null");
        }
        if (!(module.getRootSocket() instanceof DefaultFemaleDigitalActionSocket)) {
            throw new IllegalArgumentException("The module " + module.getDisplayName() + " is not a DigitalActionModule");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("The parameter \"parameters\" is null");
        }

        LogixNG_Thread thread = LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        ConditionalNG conditionalNG = new DefaultConditionalNG("IQC0000000", null);
        InternalFemaleSocket socket = new InternalFemaleSocket(conditionalNG, module, parameters);
        thread.runOnLogixNGEventually(() -> { internalExecute(conditionalNG, socket); });
    }

    private static class InternalFemaleSocket extends DefaultFemaleDigitalActionSocket {

        private final ConditionalNG _conditionalNG;
        private final Module _module;
        private final Map<String, Object> _parameters;

        public InternalFemaleSocket(ConditionalNG conditionalNG, Module module, Map<String, Object> parameters) {
            super(null, new FemaleSocketListener(){
                @Override
                public void connected(FemaleSocket socket) {
                    // Do nothing
                }

                @Override
                public void disconnected(FemaleSocket socket) {
                    // Do nothing
                }
            }, "A");
            _conditionalNG = conditionalNG;
            _module = module;
            _parameters = parameters;
        }

        @Override
        public void execute() throws JmriException {
            FemaleSocket socket = _module.getRootSocket();
            if (!(socket instanceof DefaultFemaleDigitalActionSocket)) {
                throw new IllegalArgumentException("The module " + _module.getDisplayName() + " is not a DigitalActionModule");
            }

            synchronized(this) {
                SymbolTable oldSymbolTable = _conditionalNG.getSymbolTable();
                DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(_conditionalNG);
                List<Module.ParameterData> _parameterData = new ArrayList<>();
                for (Module.Parameter p : _module.getParameters()) {
                    _parameterData.add(new Module.ParameterData(
                            p.getName(), SymbolTable.InitialValueType.None, "",
                            Module.ReturnValueType.None, ""));
                }
                newSymbolTable.createSymbols(_conditionalNG.getSymbolTable(), _parameterData);
                for (var entry : _parameters.entrySet()) {
                    newSymbolTable.setValue(entry.getKey(), entry.getValue());
                }
                _conditionalNG.setSymbolTable(newSymbolTable);

                ((DefaultFemaleDigitalActionSocket)socket).execute();
                _conditionalNG.setSymbolTable(oldSymbolTable);
            }
        }
    }

    private static void internalExecute(ConditionalNG conditionalNG, FemaleDigitalActionSocket femaleSocket) {
        if (conditionalNG.isEnabled()) {
            DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG);

            try {
                conditionalNG.setCurrentConditionalNG(conditionalNG);

                conditionalNG.setSymbolTable(newSymbolTable);

                LogixNG logixNG = conditionalNG.getLogixNG();
                InlineLogixNG inlineLogixNG = null;
                if (logixNG != null) {
                    inlineLogixNG = logixNG.getInlineLogixNG();
                }
                if (inlineLogixNG != null) {
                    List<SymbolTable.VariableData> localVariables = new ArrayList<>();
                    localVariables.add(new SymbolTable.VariableData(
                            "__InlineLogixNG__", SymbolTable.InitialValueType.String,
                            inlineLogixNG.getNameString()));
//                    localVariables.add(new SymbolTable.VariableData(
//                            "__PositionableId__", SymbolTable.InitialValueType.String,
//                            inlineLogixNG.getId()));
                    localVariables.add(new SymbolTable.VariableData(
                            "__Editor__", SymbolTable.InitialValueType.String,
                            inlineLogixNG.getEditorName()));
                    newSymbolTable.createSymbols(localVariables);
                }

                if (femaleSocket != null) {
                    femaleSocket.execute();
                } else {
                    conditionalNG.getFemaleSocket().execute();
                }
            } catch (ReturnException | ExitException e) {
                // A Return action in a ConditionalNG causes a ReturnException so this is okay.
                // An Exit action in a ConditionalNG causes a ExitException so this is okay.
            } catch (PassThruException e) {
                // This happens due to a a Break action or a Continue action that isn't handled.
                log.info("ConditionalNG {} was aborted during execute: {}",
                        conditionalNG.getSystemName(), e.getCause(), e.getCause());
            } catch (AbortConditionalNGExecutionException e) {
                if (InstanceManager.getDefault(LogixNGPreferences.class).getShowSystemNameInException()) {
                    log.warn("ConditionalNG {} was aborted during execute in the item {}: {}",
                            conditionalNG.getSystemName(), e.getMaleSocket().getSystemName(), e.getCause(), e.getCause());
                } else {
                    log.warn("ConditionalNG {} was aborted during execute: {}",
                            conditionalNG.getSystemName(), e.getCause(), e.getCause());
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

    /**
     * Set the current ConditionalNG.
     * @param conditionalNG the current ConditionalNG
     */
    @Override
    public void setCurrentConditionalNG(ConditionalNG conditionalNG) {
        if (this != conditionalNG) {
            throw new UnsupportedOperationException("The new conditionalNG must be the same as myself");
        }
        for (Module m : InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet()) {
            m.setCurrentConditionalNG(conditionalNG);
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
    public void setSocketSystemName(String systemName) {
        if ((systemName == null) || (!systemName.equals(_socketSystemName))) {
            _femaleSocket.disconnect();
        }
        _socketSystemName = systemName;
    }

    @Override
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
                        log.error("digital action is not found: {}", _socketSystemName);
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
                if (_executeAtStartup) {
                    execute();
                }
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
    public void setExecuteAtStartup(boolean value) {
        _executeAtStartup = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExecuteAtStartup() {
        return _executeAtStartup;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isListenersRegistered() {
        return _listenersAreRegistered;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void registerListenersForThisClass() {
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean existsInTree() {
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNG.class);

}
