// RunJythonScript.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.File;

/**
 * This Action runs a script by invoking a Jython interpreter.
 * <P>
 * A standard JMRI-Jython dialog is defined by
 * invoking the "jython/jmri-defaults.py" file before starting the
 * user code.
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath. To make it easier to
 * read the code, the "non-reflection" statements are in the comments.
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.2 $
 */
public class RunJythonScript extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     * @param name Action name
     */
    public RunJythonScript(String name) {
        super(name);
    }

    /**
     * We always use the same file chooser in this class, so that
     * the user's last-accessed directory remains available.
     */
    static JFileChooser fci = null;

    /**
     * Invoking this action via an event triggers
     * display of a file dialog. If a file is selected,
     * it's then invoked as a script.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        if (fci==null) {
            fci = new JFileChooser(" ");
            fci.setDialogTitle("Find desired script file");
        } else {
            // when reusing the chooser, make sure new files are included
            fci.rescanCurrentDirectory();
        }

        int retVal = fci.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            // Run the script from it's filename
            runScript(file.toString());
        }
    }

    /**
     * Run a script file from it's filename.
     */
    static public void runScript(String filename) {
        // get a Python interpreter context, make sure it's ok
        getPythonInterpreter();
        if (interp == null) {
            log.error("Can't contine to execute script "+filename+", could not create interpreter");
            return;
        }

        // execute the file
        execFile(filename);
    }

    static protected void execFile(String filename) {
        // interp.execfile(defaultContentFile);
        try {
            // set up the method to exec python functions
            java.lang.reflect.Method exec
                = interp.getClass().getMethod("exec", new Class[]{String.class});

            // have jython execute the default setup
            // interp.execfile(defaultContentFile);
            exec.invoke(interp, new Object[]{"execfile(\""+filename+"\")"});
        } catch (NoSuchMethodException e1) {
            log.error("NoSuchMethod error while invoking script "+filename);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            log.error("InvocationTargetException while invoking script "+filename+": "+e2.getCause()
                    +": "+e2.getTargetException());
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException error while invoking script "+filename);
        }
    }

    /**
     * All instance of this class share a single interpreter.
     */
    static private Object interp;

    /**
     * Provide an initialized Python interpreter.
     * <P>
     * If necessary to create one:
     * <UL>
     * <LI>Create the Python interpreter.
     * <LI>Read the default-setting file
     * </UL>
     * <P>
     * Interpreter is returned as an Object, which is to
     * be invoked via reflection.
     */
    static public Object getPythonInterpreter() {

        if (interp!=null) return interp;

        // must create one.
        try {
            log.debug("create interpreter");
            // PySystemState.initialize();
            Class cs = Class.forName("org.python.core.PySystemState");
            java.lang.reflect.Method initialize =
                        cs.getMethod("initialize",null);
            initialize.invoke(null, null);

            // interp = new PythonInterpreter();
            interp = Class.forName("org.python.util.PythonInterpreter").newInstance();

            // set up the method to exec python functions
            java.lang.reflect.Method exec
                    = interp.getClass().getMethod("exec", new Class[]{String.class});

            // have jython execute the default setup
            log.debug("load defaults from "+defaultContextFile);
            execFile(defaultContextFile);

            return interp;

        } catch (Exception e) {
            log.error("Exception creating jython system objects: "+e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Name of the file containing the Python code defining JMRI defaults
     */
    static String defaultContextFile = "jython/jmri_defaults.py";

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RunJythonScript.class.getName());

}

/* @(#)RunJythonScript.java */
