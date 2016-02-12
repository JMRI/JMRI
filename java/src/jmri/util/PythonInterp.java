// PythonInterp.java
package jmri.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JTextArea;
import jmri.script.JmriScriptEngineManager;
import jmri.script.ScriptOutput;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support a single Jython interpreter for JMRI.
 * <P>
 * A standard JMRI-Jython dialog is defined by invoking the
 * "jython/jmri-defaults.py" file before starting the user code.
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath. To make it easier to read the
 * code, the "non-reflection" statements are in the comments.
 *
 * Note that there is Windows-specific handling of filenames in the execFile
 * routine. Since Java will occasionally treat the backslash character as a
 * character escape, we have to double it (to quote it) on Windows machines
 * where it might normally appear in a filename.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version $Revision$
 * @deprecated Since 4.1.2 use {@link jmri.script.JmriScriptEngineManager}
 */
@Deprecated
public class PythonInterp {

    /**
     * Run a script file from it's filename.
     *
     * This method calls {@link #execFile(java.lang.String) }, but traps the
     * {@link java.io.FileNotFoundException } generated if filename does not
     * refer to an existing file.
     *
     * @param filename
     */
    static public void runScript(String filename) {
        try {
            execFile(filename);
        } catch (FileNotFoundException ex) {
            log.error("File {} not found.", filename);
        } catch (ScriptException ex) {
            log.error("Error in script {}.", filename, ex);
        }
    }

    /**
     * Run a script file by name.
     *
     * Unlike {@link #runScript(java.lang.String) }, this method throws a
     * {@link java.io.FileNotFoundException} when the file does not exist,
     * allowing calling code to deal with that issue.
     *
     * @param filename
     * @throws FileNotFoundException if the named file does not exist.
     * @throws javax.script.ScriptException if there is an error in the script
     */
    static public void execFile(String filename) throws FileNotFoundException, ScriptException {
        execFile(new File(filename));
    }

    /**
     * Run a script file.
     *
     * @param file
     * @throws FileNotFoundException if the file does not exist.
     * @throws javax.script.ScriptException if there is an error in the script
     */
    static public void execFile(File file) throws FileNotFoundException, ScriptException {
        ScriptEngine python = JmriScriptEngineManager.getDefault().getEngineByName("python");
        if (python == null) {
            log.error("Can't contine to execute command, could not create interpreter");
            return;
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        python.eval(new FileReader(file));
    }

    static public void execCommand(String command) throws ScriptException {
        ScriptEngine python = JmriScriptEngineManager.getDefault().getEngineByName("python");
        if (python == null) {
            log.error("Can't contine to execute command, could not create interpreter");
            return;
        }

        python.eval(command);
    }

    /**
     * All instance of this class share a single interpreter.
     */
    static private PythonInterpreter interp;

    /**
     * Provide an initialized Python interpreter.
     * <P>
     * If necessary to create one:
     * <UL>
     * <LI>Create the Python interpreter.
     * <LI>Read the default-setting file
     * </UL>
     * <P>
     * Interpreter is returned as an Object, which is to be invoked via
     * reflection.
     *
     * @return the Python interpreter for this session
     */
    synchronized static public PythonInterpreter getPythonInterpreter() {

        if (interp != null) {
            return interp;
        }

        // must create one.
        try {
            log.debug("create interpreter");
            JmriScriptEngineManager.getDefault().initializePython();

            interp = new PythonInterpreter();

            // have jython execute the default setup
            log.debug("load defaults from {}", defaultContextFile);
            interp.execfile(FileUtil.getExternalFilename(defaultContextFile));

            return interp;
        } catch (Exception e) {
            log.error("Exception creating jython system objects", e);
        }
        return null;
    }

    /**
     * Provide access to the JTextArea containing the Jython VM output.
     * <P>
     * The output JTextArea is not created until this is invoked, so that code
     * that doesn't use this feature can run on GUI-less machines.
     *
     * @return component containing python output
     * @deprecated use {@link jmri.script.ScriptOutput#getOutputArea()} instead
     */
    @Deprecated
    static public JTextArea getOutputArea() {
        return ScriptOutput.getDefault().getOutputArea();
    }

    /**
     * Name of the file containing the Python code defining JMRI defaults
     */
    static String defaultContextFile = "program:jython/jmri_defaults.py";

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PythonInterp.class.getName());

}

/* @(#)RunJythonScript.java */
