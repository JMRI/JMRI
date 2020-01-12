package jmri.jmrit.automat;

import jmri.script.JmriScriptEngineManager;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sample Automaton invokes a Jython interpreter to handle a script that
 * defines a Siglet implementation.
 * <p>
 * The python file should define two functions:
 * <ul>
 * <li>defineIO()
 * <li>setOutput()
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @deprecated since 4.17.5; use {@link JmriScriptEngineManager#eval(java.io.File)}
 * with a subclass of {@link Siglet} instead
 */
@Deprecated
public class JythonSiglet extends Siglet {

    PythonInterpreter interp;

    public JythonSiglet(String file) {
        filename = file;
    }

    String filename;

    /**
     * Initialize this object.
     * <ul>
     * <li>Create the Python interpreter.
     * <li>Load the generally-available objects
     * <li>Read the file
     * <li>Run the python defineIO routine
     * </ul>
     * Initialization of the Python in the actual script file is deferred until
     * the {@link #defineIO} method.
     */
    @Override
    public void defineIO() {

        interp = JmriScriptEngineManager.getDefault().newPythonInterpreter();

        // load some general objects
        interp.set("inputs", inputs);
        interp.set("outputs", outputs);

        // have jython read the file
        interp.execfile(filename);

        // execute the init routine in the jython class
        interp.exec("defineIO()");

        log.info("inputs[0]: {}", inputs[0]);
    }

    /**
     * Invoke the Jython setOutput function
     */
    @Override
    public void setOutput() {
        if (interp == null) {
            log.error("No interpreter, so cannot handle automat");
            return;
        }
        interp.exec("setOutput()");
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(JythonSiglet.class);

}
