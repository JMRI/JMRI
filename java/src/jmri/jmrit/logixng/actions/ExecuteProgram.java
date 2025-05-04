package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;

/**
 * This action executes a program.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ExecuteProgram extends AbstractDigitalAction
        implements PropertyChangeListener, FemaleSocketListener {

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;

    private final LogixNG_SelectString _selectProgram =
            new LogixNG_SelectString(this, this);
    private final LogixNG_SelectStringList _selectParameters =
            new LogixNG_SelectStringList();

    private String _outputLocalVariable = "";
    private String _errorLocalVariable = "";
    private String _exitCodeLocalVariable = "";
    private boolean _launchThread = false;
    private boolean _callChildOnEveryOutput = false;
    private boolean _joinOutput = true;


    public ExecuteProgram(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExecuteProgram copy = new ExecuteProgram(sysName, userName);
        copy.setComment(getComment());
        _selectProgram.copy(copy._selectProgram);
        _selectParameters.copy(copy._selectParameters);
        copy.setOutputLocalVariable(_outputLocalVariable);
        copy.setLaunchThread(_launchThread);
        copy.setCallChildOnEveryOutput(_callChildOnEveryOutput);
        copy.setJoinOutput(_joinOutput);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectString getSelectProgram() {
        return _selectProgram;
    }

    public LogixNG_SelectStringList getSelectParameters() {
        return _selectParameters;
    }

    public void setOutputLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setResultLocalVariable");
        _outputLocalVariable = localVariable;
    }

    public String getOutputLocalVariable() {
        return _outputLocalVariable;
    }

    public void setErrorLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setErrorLocalVariable");
        _errorLocalVariable = localVariable;
    }

    public String getErrorLocalVariable() {
        return _errorLocalVariable;
    }

    public void setExitCodeLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setExitCodeLocalVariable");
        _exitCodeLocalVariable = localVariable;
    }

    public String getExitCodeLocalVariable() {
        return _exitCodeLocalVariable;
    }

    public void setLaunchThread(boolean launchThread) {
        this._launchThread = launchThread;
    }

    public boolean getLaunchThread() {
        return _launchThread;
    }

    public void setCallChildOnEveryOutput(boolean callChildOnEveryOutput) {
        this._callChildOnEveryOutput = callChildOnEveryOutput;
    }

    public boolean getCallChildOnEveryOutput() {
        return _callChildOnEveryOutput;
    }

    public void setJoinOutput(boolean joinOutput) {
        this._joinOutput = joinOutput;
    }

    public boolean getJoinOutput() {
        return _joinOutput;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    private void executeChild(ConditionalNG conditionalNG, SymbolTable symbolTable, Object output, Object error, Integer exitCode) {
        synchronized(this) {
            if (_socket.isConnected()) {
                DefaultSymbolTable tempSymbolTable = new DefaultSymbolTable(symbolTable);
                if (_outputLocalVariable != null && !_outputLocalVariable.isBlank()) {
                    tempSymbolTable.setValue(_outputLocalVariable, output);
                }
                if (_errorLocalVariable != null && !_errorLocalVariable.isBlank()) {
                    tempSymbolTable.setValue(_errorLocalVariable, error);
                }
                if (_exitCodeLocalVariable != null && !_exitCodeLocalVariable.isBlank()) {
                    tempSymbolTable.setValue(_exitCodeLocalVariable, exitCode);
                }
                InternalFemaleSocket internalSocket = new InternalFemaleSocket();
                internalSocket.conditionalNG = conditionalNG;
                internalSocket.newSymbolTable = tempSymbolTable;
                conditionalNG.execute(internalSocket);
            }
        }
    }

    private Object getOutput(List<String> data) {
        if (_joinOutput) {
            return String.join("\n", data);
        } else {
            return data;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

/*
        Note!!!!

        This does NOT work!!! Ensure the user cannot select this!!!
        _launchThread == false && _callChildOnEveryOutput == true



        Note:
        Every part of the parameter list must be on its own line.
        For example: find /home/daniel/Dokument/GitHub/JMRI/java -iname *.java

        Should be:

        Program: find

        Parameters:
            /home/daniel/Dokument/GitHub/JMRI/java
            -iname
            *.java
*/


        final ConditionalNG conditionalNG;
        final DefaultSymbolTable newSymbolTable;

        synchronized(this) {
            conditionalNG = getConditionalNG();
            newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());
        }

        String program = _selectProgram.evaluateValue(conditionalNG);
        List<String> parameters = _selectParameters.evaluateValue(conditionalNG);

        List<String> programAndParameters = new ArrayList<>();
        programAndParameters.add(program);
        programAndParameters.addAll(parameters);

        for (String s : programAndParameters) {
            log.error("Execute program: \"{}\"", s);
        }

        Process process;
        try {
            String[] temp = programAndParameters.toArray(String[]::new);
            for (String s : temp) {
                log.error("Execute: \"{}\"", s);
            }

//            process = Runtime.getRuntime().exec(new String[]{program,parameters});
            process = Runtime.getRuntime().exec(programAndParameters.toArray(String[]::new));
        } catch (IOException e) {
            throw new JmriException(e);
        }

        Runnable readAllOnce = () -> {
            List<String> output = new ArrayList<>();
            try {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream())))  {
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        output.add(line);
                    }
                }
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                return;
            }

            List<String> error = new ArrayList<>();
            try {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getErrorStream())))  {
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        error.add(line);
                    }
                }
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                return;
            }

            try {
                var completableFuture = process.onExit();
                completableFuture.get();
            } catch (ExecutionException e) {
                log.error("Unexpected failure", e);
                return;
            } catch (InterruptedException e) {
                log.error("Task interrupted", e);
                return;
            }

            if (!_callChildOnEveryOutput) {
                executeChild(conditionalNG, newSymbolTable, getOutput(output), getOutput(error), process.exitValue());
            }
        };

        Runnable readInput = () -> {
            try {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream())))  {
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        executeChild(conditionalNG, newSymbolTable, line, null, null);
                    }
                }
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        };

        Runnable readError = () -> {
            try {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getErrorStream())))  {
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        executeChild(conditionalNG, newSymbolTable, null, line, null);
                    }
                }
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        };

        Runnable onExit = () -> {
            try {
                var completableFuture = process.onExit();
                completableFuture.get();
                executeChild(conditionalNG, newSymbolTable, null, null, process.exitValue());
            } catch (ExecutionException e) {
                log.error("Unexpected failure", e);
            } catch (InterruptedException e) {
                log.error("Task interrupted", e);
            }
        };

        if (_callChildOnEveryOutput) {
            ThreadingUtil.newThread(readInput).start();
            ThreadingUtil.newThread(readError).start();
            ThreadingUtil.newThread(onExit).start();
        } else if (_launchThread) {
            ThreadingUtil.newThread(readAllOnce).start();
        } else {
            // Run and wait for the process to complete
            readAllOnce.run();
        }
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
        return Bundle.getMessage(locale, "ExecuteProgram_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String program = _selectProgram.getDescription(locale);
        String parameters = _selectParameters.getDescription(locale);

        return Bundle.getMessage(locale, "ExecuteProgram_Long", program, parameters);
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
            if (!_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {

                String socketSystemName = _socketSystemName;

                _socket.disconnect();

                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
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

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _selectProgram.registerListeners();
            _selectParameters.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectProgram.unregisterListeners();
        _selectParameters.unregisterListeners();
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {

        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;

        public InternalFemaleSocket() {
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
            if (_socket != null) {
                SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                conditionalNG.setSymbolTable(newSymbolTable);
                _socket.execute();
                conditionalNG.setSymbolTable(oldSymbolTable);
            }
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgram.class);

}
