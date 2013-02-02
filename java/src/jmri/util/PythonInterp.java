// PythonInterp.java

package jmri.util;

import org.apache.log4j.Logger;
import java.io.*;

/**
 * Support a single Jython interpreter for JMRI.
 * <P>
 * A standard JMRI-Jython dialog is defined by
 * invoking the "jython/jmri-defaults.py" file before starting the
 * user code.
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath. To make it easier to
 * read the code, the "non-reflection" statements are in the comments.
 *
 * Note that there is Windows-specific handling of filenames in the 
 * execFile routine. Since Java will occasionally treat the backslash
 * character as a character escape, we have to double it (to quote it)
 * on Windows machines where it might normally appear in a filename.
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision$
 */
public class PythonInterp {

    /**
     * Run a script file from it's filename.
     */
    static public void runScript(String filename) {
        // get a Python interpreter context, make sure it's ok
        getPythonInterpreter();
        if (interp == null) {
            log.error("Can't contine to execute script, could not create interpreter");
            return;
        }

        // execute the file
        execFile(filename);
    }

     static public void execFile(String filename) {
        // if windows, need to process backslashes in filename
        if (SystemType.isWindows())
            filename = filename.replaceAll("\\\\", "\\\\\\\\");
        
        execCommand("execfile(\""+filename+"\")");
    }

    static public void execCommand(String command) {
        // get a Python interpreter context, make sure it's ok
        getPythonInterpreter();
        if (interp == null) {
            log.error("Can't contine to execute command, could not create interpreter");
            return;
        }

        // interp.execfile(command);
        try {
            // set up the method to exec python functions
            java.lang.reflect.Method exec
                = interp.getClass().getMethod("exec", new Class[]{String.class});

            exec.invoke(interp, new Object[]{command});
        } catch (java.lang.reflect.InvocationTargetException e2) {
            try {
                log.error("InvocationTargetException while invoking command "+command
                    +": "+e2.getCause());
                // Send error message to script output window if open
                if (outputlog != null)
                	getOutputArea().append("Error: "+e2.getCause());
            } catch (java.lang.NoSuchMethodError e3) {
                // most likely, this is 1.1.8 JVM
                log.error("InvocationTargetException while invoking command "+command
                    +": "+e2.getTargetException());
                // Send error message to script output window if open
                if (outputlog != null)
                	getOutputArea().append("Error: "+e2.getTargetException());
            }
        } catch (NoSuchMethodException e1) {
            log.error("NoSuchMethod error while invoking command "+command);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException error while invoking command "+command);
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
    synchronized static public Object getPythonInterpreter() {

        if (interp!=null) return interp;

        // must create one.
        try {
            log.debug("create interpreter");
            // PySystemState.initialize();
            Class<?> cs = Class.forName("org.python.core.PySystemState");
            java.lang.reflect.Method initialize =
                        cs.getMethod("initialize",(Class[])null);
            initialize.invoke(null, (Object[])null);

            // interp = new PythonInterpreter();
            interp = Class.forName("org.python.util.PythonInterpreter").newInstance();

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
     * Provide access to the JTextArea containing the Jython VM 
     * output.
     * <P>
     * The output JTextArea is not created until this is invoked,
     * so that code that doesn't use this feature can run
     * on GUI-less machines.
     */
    static public javax.swing.JTextArea getOutputArea() {
        if (outputlog == null) {
            // convert to stored output
            
            try {
                // create the output area
                outputlog = new javax.swing.JTextArea();

                // Add the I/O pipes
                PipedWriter pw = new PipedWriter();

                // interp.setErr(pw);
                java.lang.reflect.Method method
                    = interp.getClass().getMethod("setErr", new Class[]{Writer.class});
                method.invoke(interp, new Object[]{pw});
                // interpreter.setOut(pw);
                method
                    = interp.getClass().getMethod("setOut", new Class[]{Writer.class});
                method.invoke(interp, new Object[]{pw});

                // ensure the output pipe is read and stored into a
                // Swing TextArea data model
                PipedReader pr = new PipedReader(pw);
                PipeListener pl = new PipeListener(pr, outputlog);
                pl.start();
            } catch (Exception e) {
                log.error("Exception creating jython output area: "+e);
                e.printStackTrace();
                return null;
            }
        }
        return outputlog;
    }
    
    /**
     * JTextArea containing the output
     */
    static private javax.swing.JTextArea outputlog = null;

    /**
     * Name of the file containing the Python code defining JMRI defaults
     */
    static String defaultContextFile = "jython/jmri_defaults.py";

    // initialize logging
    static Logger log = Logger.getLogger(PythonInterp.class.getName());

}

/* @(#)RunJythonScript.java */
