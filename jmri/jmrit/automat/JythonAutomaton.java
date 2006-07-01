// JythonAutomaton.java

package jmri.jmrit.automat;

import jmri.InstanceManager;

/**
 * This sample Automaton invokes a Jython interpreter to handle a script.
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */
public class JythonAutomaton extends AbstractAutomaton {
    Object interp;

    /**
     * Create the interpreter
     */
    protected void init() {
        // PySystemState.initialize();
        try {
            Class cs = Class.forName("org.python.core.PySystemState");
            java.lang.reflect.Method initialize =
                        cs.getMethod("initialize",null);
            initialize.invoke(null, null);

            // interp = new PythonInterpreter();
            interp = Class.forName("org.python.util.PythonInterpreter").newInstance();

            // interp.set("dcc", InstanceManager.commandStationInstance());
            // interp.set("self", this);
            java.lang.reflect.Method set =
                        interp.getClass().getMethod("set", new Class[]{String.class, Object.class});
            set.invoke(interp, new Object[]{"dcc", InstanceManager.commandStationInstance()});
            set.invoke(interp, new Object[]{"self", this});

        } catch (Exception e) {
            log.error("Exception creating jython system objects: "+e);
            e.printStackTrace();
        }
    }

    /**
     * Invoke a command...
     * @return Always returns true to continue operation
     */
    protected boolean handle() {
        try {

            String file = "test.py";

            // have jython read the file
            java.lang.reflect.Method exec =
                        interp.getClass().getMethod("exec", new Class[]{String.class});
            exec.invoke(interp, new Object[]{"execfile(\""+file+"\")"});
            // and execute the handle routine in the jython
            exec.invoke(interp, new Object[]{"retval = handle()"});
            java.lang.reflect.Method get =
                        interp.getClass().getMethod("get", new Class[]{String.class});
            Object retval = get.invoke(interp, new Object[]{"retval"});
            System.out.println("ret: "+retval.getClass());
        } catch (Exception e) {
            log.error("Exception invoking jython command: "+e);
            e.printStackTrace();
            return false;
        }

        wait(20000);
        System.out.println("ending");
        return false;   // never terminate voluntarily
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JythonAutomaton.class.getName());

}

/* @(#)JythonAutomaton.java */
