package jmri.jmrit.automat;

import jmri.script.JmriScriptEngineManager;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sample Automaton invokes a Jython interpreter to handle a script.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @deprecated since 4.17.5; use {@link JmriScriptEngineManager#eval(java.io.File)}
 * with a subclass of {@link AbstractAutomaton} instead
 */
@Deprecated
public class JythonAutomaton extends AbstractAutomaton {

    PythonInterpreter interp;

    public JythonAutomaton(String file) {
        filename = file;
    }

    String filename;

    /**
     * Initialize this object.
     * <ul>
     * <li>Create the Python interpreter.
     * <li>Load the generally-available objects
     * <li>Read the file
     * <li>Run the python init routine
     * </ul>
     * Initialization of the Python in the actual script file is deferred until
     * the {@link #handle} method.
     */
    @Override
    protected void init() {

        interp = JmriScriptEngineManager.getDefault().newPythonInterpreter();

        // have jython read the file
        interp.execfile(filename);

        // execute the init routine in the jython class
        interp.exec("init()");
    }

    /**
     * Invoke the Jython automat function
     *
     * @return True to continue operation if successful
     */
    @Override
    protected boolean handle() {
        if (interp == null) {
            log.error("No interpreter, so cannot handle automat");
            return false; // to terminate operation
        }
        // execute the handle routine in the jython and check return value
        interp.exec("retval = handle()");
        Object retval = interp.get("retval");
        log.info("retval = {}", retval);
        return retval.toString().equals("1");
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(JythonAutomaton.class);

}
