// JythonSiglet.java

package jmri.jmrit.automat;

import jmri.*;

/**
 * This sample Automaton invokes a Jython interpreter to handle a script
 * that defines a Siglet implementation.
 * <P>
 * The python file should define two functions:
 * <UL>
 * <LI>defineIO()
 * <LI>setOutput()
 * </UL>
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath. To make it easier to
 * read the code, the "non-reflection" statements are in the comments
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class JythonSiglet extends Siglet {
    Object interp;

    public JythonSiglet(String file) {
        filename = file;
    }

    String filename;

    /**
     * Initialize this object.
     * <UL>
     * <LI>Create the Python interpreter.
     * <LI>Load the generally-available objects
     * <LI>Read the file
     * <LI>Run the python defineIO routine
     * </UL>
     * Initialization of the Python in the actual
     * script file is deferred until the {@link #handle} method.
     */
    public void defineIO() {

        try {
            // PySystemState.initialize();
            Class cs = Class.forName("org.python.core.PySystemState");
            java.lang.reflect.Method initialize =
                        cs.getMethod("initialize",null);
            initialize.invoke(null, null);

            // interp = new PythonInterpreter();
            interp = Class.forName("org.python.util.PythonInterpreter").newInstance();

            // load some general objects
            // interp.set("dcc", InstanceManager.commandStationInstance());
            // interp.set("self", this);
            java.lang.reflect.Method set =
                        interp.getClass().getMethod("set", new Class[]{String.class, Object.class});
            set.invoke(interp, new Object[]{"self", this});

            set.invoke(interp, new Object[]{"inputs", inputs});
            set.invoke(interp, new Object[]{"outputs", outputs});

            set.invoke(interp, new Object[]{"turnouts", InstanceManager.turnoutManagerInstance()});
            set.invoke(interp, new Object[]{"sensors", InstanceManager.sensorManagerInstance()});
            set.invoke(interp, new Object[]{"signals", InstanceManager.signalHeadManagerInstance()});
            set.invoke(interp, new Object[]{"dcc", InstanceManager.commandStationInstance()});

            set.invoke(interp, new Object[]{"CLOSED", new Integer(jmri.Turnout.CLOSED)});
            set.invoke(interp, new Object[]{"THROWN", new Integer(jmri.Turnout.THROWN)});
            set.invoke(interp, new Object[]{"ACTIVE", new Integer(jmri.Sensor.ACTIVE)});
            set.invoke(interp, new Object[]{"INACTIVE", new Integer(jmri.Sensor.INACTIVE)});
            set.invoke(interp, new Object[]{"GREEN", new Integer(jmri.SignalHead.GREEN)});
            set.invoke(interp, new Object[]{"YELLOW", new Integer(jmri.SignalHead.YELLOW)});
            set.invoke(interp, new Object[]{"RED", new Integer(jmri.SignalHead.RED)});

            // set up the method to exec python functions
            exec = interp.getClass().getMethod("exec", new Class[]{String.class});

            // have jython read the file
            exec.invoke(interp, new Object[]{"execfile(\""+filename+"\")"});

            // execute the init routine in the jython class
            exec.invoke(interp, new Object[]{"defineIO()"});

            System.out.println("inputs[0]: "+inputs[0]);

        } catch (Exception e) {
            log.error("Exception creating jython system objects: "+e);
            e.printStackTrace();
        }
    }

    public void setInputs(NamedBean[] in) {
        inputs = in;
    }

    /**
     * Invoke the Jython setOutput function
     */
    public void setOutput() {
        if (interp == null) {
            log.error("No interpreter, so cannot handle automat");
            return;
        }
        try {
            // execute the handle routine in the jython
            exec.invoke(interp, new Object[]{"setOutput()"});
        } catch (Exception e) {
            log.error("Exception invoking jython command: "+e);
            e.printStackTrace();
        }
    }

    java.lang.reflect.Method exec;

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JythonSiglet.class.getName());

}

/* @(#)JythonAutomaton.java */
