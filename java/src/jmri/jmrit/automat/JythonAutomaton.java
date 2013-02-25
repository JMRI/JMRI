// JythonAutomaton.java

package jmri.jmrit.automat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;

/**
 * This sample Automaton invokes a Jython interpreter to handle a script.
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath. To make it easier to
 * read the code, the "non-reflection" statements are in the comments
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision$
 */
public class JythonAutomaton extends AbstractAutomaton {
    Object interp;

    public JythonAutomaton(String file) {
        filename = file;
    }

    String filename;

    /**
     * Initialize this object.
     * <UL>
     * <LI>Create the Python interpreter.
     * <LI>Load the generally-available objects
     * <LI>Read the file
     * <LI>Run the python init routine
     * </UL>
     * Initialization of the Python in the actual
     * script file is deferred until the {@link #handle} method.
     */
    protected void init() {

        try {
            // PySystemState.initialize();
            Class<?> cs = Class.forName("org.python.core.PySystemState");
            java.lang.reflect.Method initialize =
                        cs.getMethod("initialize",(Class[])null);
            initialize.invoke(null, (Object[])null);

            // interp = new PythonInterpreter();
            interp = Class.forName("org.python.util.PythonInterpreter").newInstance();

            // load some general objects
            // interp.set("dcc", InstanceManager.commandStationInstance());
            // interp.set("self", this);
            java.lang.reflect.Method set =
                        interp.getClass().getMethod("set", new Class[]{String.class, Object.class});
            set.invoke(interp, new Object[]{"dcc", InstanceManager.commandStationInstance()});
            set.invoke(interp, new Object[]{"self", this});

            // set up the method to exec python functions
            exec = interp.getClass().getMethod("exec", new Class[]{String.class});

            // have jython read the file
            exec.invoke(interp, new Object[]{"execfile(\""+filename+"\")"});

            // execute the init routine in the jython class
            exec.invoke(interp, new Object[]{"init()"});

        } catch (Exception e) {
            log.error("Exception creating jython system objects: "+e);
            e.printStackTrace();
        }
    }

    /**
     * Invoke the Jython automat function
     * @return True to continue operation if successful
     */
    protected boolean handle() {
        if (interp == null) {
            log.error("No interpreter, so cannot handle automat");
            return false; // to terminate operation
        }
        try {
            // execute the handle routine in the jython and check return value
            exec.invoke(interp, new Object[]{"retval = handle()"});
            java.lang.reflect.Method get =
                        interp.getClass().getMethod("get", new Class[]{String.class});
            Object retval = get.invoke(interp, new Object[]{"retval"});
            System.out.println("retval = "+retval);
            if (retval.toString().equals("1")) return true;
            return false;
        } catch (Exception e) {
            log.error("Exception invoking jython command: "+e);
            e.printStackTrace();
            return false;
        }
    }

    java.lang.reflect.Method exec;

    // initialize logging
    static Logger log = LoggerFactory.getLogger(JythonAutomaton.class.getName());

}

/* @(#)JythonAutomaton.java */
