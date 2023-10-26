package jmri.jmrit.logixng;

import jmri.NamedBean;
import jmri.jmrit.logixng.util.LogixNG_Thread;

/**
 * ConditionalNG.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public interface ConditionalNG extends Base, NamedBean {

    /**
     * Get the thread that this conditionalNG executes on.
     * @return the thread
     */
    LogixNG_Thread getCurrentThread();

    /**
     * Get the thread id that this conditionalNG should execute on when JMRI
     * starts next time.
     * It's not currently possible to move a ConditionalNG from one thread to
     * another without restarting JMRI.
     * @return the thread ID
     */
    int getStartupThreadId();

    /**
     * Set the thread id that this conditionalNG should execute on when JMRI
     * starts next time.
     * It's not currently possible to move a ConditionalNG from one thread to
     * another without restarting JMRI.
     * @param threadId the thread ID
     */
    void setStartupThreadId(int threadId);

    /**
     * Get the female socket of this ConditionalNG.
     * @return the female socket
     */
    FemaleDigitalActionSocket getFemaleSocket();

    void setSocketSystemName(String systemName);

    String getSocketSystemName();

    /**
     * Set whenether this ConditionalNG is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     *
     * @param enable true if this ConditionalNG should be enabled, false otherwise
     */
    void setEnabled(boolean enable);

    /**
     * Determines whether this ConditionalNG is enabled.
     *
     * @return true if the ConditionalNG is enabled, false otherwise
     */
    @Override
    boolean isEnabled();

    /**
     * Set whenether this ConditionalNG should be executed at startup or at
     * panel load.
     *
     * @param value true if this ConditionalNG should be executed at startup
     * or at panel load.
     */
    void setExecuteAtStartup(boolean value);

    /**
     * Determines whenether this ConditionalNG should be executed at startup
     * or at panel load.
     *
     * @return true if this ConditionalNG should be executed at startup or at
     * panel load.
     */
    boolean isExecuteAtStartup();

    /**
     * Set whenether execute() should run on the LogixNG thread at once or
     * should dispatch the call until later.
     * Most tests turns off the delay to simplify the tests.
     *
     * @param value true if execute() should run on LogixNG thread delayed,
     *              false otherwise.
     */
    void setRunDelayed(boolean value);

    /**
     * Get whenether execute() should run on the LogixNG thread at once or
     * should dispatch the call until later.
     * Most tests turns off the delay to simplify the tests.
     * @return true if execute() should run on LogixNG thread delayed,
     * false otherwise.
     */
    boolean getRunDelayed();

    /**
     * Execute the ConditionalNG.
     */
    void execute();

    /**
     * Execute the ConditionalNG.
     * @param allowRunDelayed true if it's ok to run delayed, false otherwise
     */
    void execute(boolean allowRunDelayed);

    /**
     * Execute the female socket.
     * @param socket the female socket
     */
    void execute(FemaleDigitalActionSocket socket);

    /**
     * Are listeners registered?
     * @return true if listeners are registered, otherwise return false
     */
    boolean isListenersRegistered();

    /**
     * Get the stack
     * @return the stack
     */
    Stack getStack();

    /**
     * Get the current symbol table
     * @return the symbol table
     */
    SymbolTable getSymbolTable();

    /**
     * Set the current symbol table
     * @param symbolTable the symbol table
     */
    void setSymbolTable(SymbolTable symbolTable);

    /**
     * Set the current ConditionalNG.
     * @param conditionalNG the current ConditionalNG
     */
    void setCurrentConditionalNG(ConditionalNG conditionalNG);

}
