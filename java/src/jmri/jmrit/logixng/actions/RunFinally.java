package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * This action executes an action and then runs some code.
 * It is used internally.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public final class RunFinally extends AbstractDigitalAction
        implements FemaleSocketListener {

    /**
     * The caller can send its own data to the runFinally() method using this
     * interface.
     */
    public interface Data {}

    /**
     * The function the caller wants called after the action has been executed.
     */
    public interface RunFinallyFunction {
        void runFinally(ConditionalNG conditionalNG, Exception ex, Data data);
    }

    private final FemaleDigitalActionSocket _socket;
    private final RunFinallyFunction _runFinally;
    private final Data _data;

    public RunFinally(String sysName, RunFinallyFunction runFinally, Data data)
            throws BadUserNameException {
        super(sysName, null);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
        this._data = data;
        this._runFinally = runFinally;
    }

    public RunFinally(String sysName, String userName, RunFinallyFunction runFinally, Data data)
            throws BadUserNameException {
        super(sysName, userName);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
        this._data = data;
        this._runFinally = runFinally;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames)
            throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        DigitalActionBean copy = new RunFinally(sysName, userName, _runFinally, _data);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Exception ex = null;
        try {
            if (_socket != null) {
                _socket.execute();
            }
        } catch (JmriException | RuntimeException e) {
            ex = e;
        }
        _runFinally.runFinally(getConditionalNG(), ex, _data);
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _socket;
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SetLocalVariables_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "SetLocalVariables_Long");
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing. This class is never stored to disk.
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    @Override
    public void connected(FemaleSocket socket) {
        // Do nothing. This class is never stored to disk.
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        // Do nothing. This class is never stored to disk.
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

}
