package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.*;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;

/**
 * Executes a digital action delayed.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionShutDownTask
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _callSocketSystemName;
    private String _runSocketSystemName;
    private final FemaleDigitalExpressionSocket _callSocket;
    private final FemaleDigitalActionSocket _runSocket;
    private final Object _lock = new Object();


    public ActionShutDownTask(String sys, String user) {
        super(sys, user);
        _callSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E");
        _runSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionShutDownTask copy = new ActionShutDownTask(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ConditionalNG conditionalNG = getConditionalNG();
        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

        ShutDownTask shutDownTask = new AbstractShutDownTask("LogixNG action ShutDownTask") {
            @Override
            public Boolean call() {
                if (_callSocket.isConnected()) {
                    InternalCallSocket internalSocket
                            = new InternalCallSocket();
                    internalSocket.conditionalNG = conditionalNG;
                    internalSocket.newSymbolTable = newSymbolTable;

                    try {
                        synchronized(_lock) {
                            conditionalNG.execute(internalSocket);
                            while (!internalSocket._completed) _lock.wait();
                            return internalSocket._result;
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted exception: {}", e, e);
                        return true;
                    }
                } else {
                    return true;
                }
            }

            @Override
            public void runEarly() {
                if (_runSocket.isConnected()) {
                    InternalRunSocket internalSocket
                            = new InternalRunSocket();
                    internalSocket.conditionalNG = conditionalNG;
                    internalSocket.newSymbolTable = newSymbolTable;

                    try {
                        synchronized(_lock) {
                            conditionalNG.execute(internalSocket);
                            while (!internalSocket._completed) _lock.wait();
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted exception: {}", e, e);
                    }
                }
            }

            @Override
            public void run() {
                // Do nothing
            }
        };
        InstanceManager.getDefault(ShutDownManager.class).register(shutDownTask);
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _callSocket;

            case 1:
                return _runSocket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _callSocket) {
            _callSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else if (socket == _runSocket) {
            _runSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _callSocket) {
            _callSocketSystemName = null;
        } else if (socket == _runSocket) {
            _runSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionShutDownTask_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionShutDownTask_Long", _callSocket.getName(), _runSocket.getName());
    }

    public FemaleDigitalExpressionSocket getCallSocket() {
        return _callSocket;
    }

    public String getCallSocketSystemName() {
        return _callSocketSystemName;
    }

    public void setCallSocketSystemName(String systemName) {
        _callSocketSystemName = systemName;
    }

    public FemaleDigitalActionSocket getRunSocket() {
        return _runSocket;
    }

    public String getRunSocketSystemName() {
        return _runSocketSystemName;
    }

    public void setRunSocketSystemName(String systemName) {
        _runSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_callSocket.isConnected()
                    || !_callSocket.getConnectedSocket().getSystemName()
                            .equals(_callSocketSystemName)) {

                String socketSystemName = _callSocketSystemName;

                _callSocket.disconnect();

                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _callSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression {}", socketSystemName);
                    }
                }
            } else {
                _callSocket.getConnectedSocket().setup();
            }

            if (!_runSocket.isConnected()
                    || !_runSocket.getConnectedSocket().getSystemName()
                            .equals(_runSocketSystemName)) {

                String socketSystemName = _runSocketSystemName;

                _runSocket.disconnect();

                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _runSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
                    }
                }
            } else {
                _runSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }



    private class InternalCallSocket
            extends DefaultFemaleDigitalActionSocket {

        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;
        private boolean _completed = false;
        private boolean _result = true;     // If no connected socket, return true

        public InternalCallSocket() {
            super(null, new FemaleSocketListener(){
                @Override
                public void connected(FemaleSocket socket) {
                    // Do nothing
                }

                @Override
                public void disconnected(FemaleSocket socket) {
                    // Do nothing
                }
            }, "E");
        }

        @Override
        public void execute() throws JmriException {
            if (conditionalNG == null) { throw new NullPointerException("conditionalNG is null"); }
            if (_callSocket != null) {
                SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                conditionalNG.setSymbolTable(newSymbolTable);
                _result = _callSocket.evaluate();
                conditionalNG.setSymbolTable(oldSymbolTable);
                synchronized(_lock) {
                    _completed = true;
                    _lock.notifyAll();
                }
            }
        }

    }



    private class InternalRunSocket
            extends DefaultFemaleDigitalActionSocket {

        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;
        private boolean _completed = false;

        public InternalRunSocket() {
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
        }

        @Override
        public void execute() throws JmriException {
            if (conditionalNG == null) { throw new NullPointerException("conditionalNG is null"); }
            if (_runSocket != null) {
                SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                conditionalNG.setSymbolTable(newSymbolTable);
                _runSocket.execute();
                conditionalNG.setSymbolTable(oldSymbolTable);
                synchronized(_lock) {
                    _completed = true;
                    _lock.notifyAll();
                }
            }
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionShutDownTask.class);

}
