package jmri.jmrit.automat;

import java.lang.reflect.InvocationTargetException;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sample Automaton invokes a Jython interpreter to handle a script.
 * <p>
 * Access is via Java reflection so that both users and developers can work
 * without the jython-standalone-2.7.0.jar file in the classpath. To make it easier to read the
 * code, the "non-reflection" statements are in the comments
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class JythonAutomaton extends AbstractAutomaton {

    Object interp;

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

        try {
            // PySystemState.initialize();
            Class<?> cs = Class.forName("org.python.core.PySystemState");
            java.lang.reflect.Method initialize
                    = cs.getMethod("initialize", (Class[]) null);
            initialize.invoke(null, (Object[]) null);

            // interp = new PythonInterpreter();
            interp = Class.forName("org.python.util.PythonInterpreter").getDeclaredConstructor().newInstance();

            // load some general objects
            java.lang.reflect.Method set
                    = interp.getClass().getMethod("set", new Class[]{String.class, Object.class});
            set.invoke(interp, new Object[]{"dcc", InstanceManager.getNullableDefault(jmri.CommandStation.class)});
            set.invoke(interp, new Object[]{"self", this});

            // set up the method to exec python functions
            exec = interp.getClass().getMethod("exec", new Class[]{String.class});

            // have jython read the file
            exec.invoke(interp, new Object[]{"execfile(\"" + filename + "\")"});

            // execute the init routine in the jython class
            exec.invoke(interp, new Object[]{"init()"});

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            log.error("Exception creating jython system objects", e);
        }
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
        try {
            // execute the handle routine in the jython and check return value
            exec.invoke(interp, new Object[]{"retval = handle()"});
            java.lang.reflect.Method get
                    = interp.getClass().getMethod("get", new Class[]{String.class});
            Object retval = get.invoke(interp, new Object[]{"retval"});
            System.out.println("retval = " + retval);
            if (retval.toString().equals("1")) {
                return true;
            }
            return false;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Exception during handle routine", e);
            return false;
        }
    }

    java.lang.reflect.Method exec;

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JythonAutomaton.class);

}
