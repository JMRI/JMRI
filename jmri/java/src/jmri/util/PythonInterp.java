// PythonInterp.java
package jmri.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.Properties;
import javax.swing.JTextArea;
import org.python.core.PySystemState;
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
 */
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
     */
    static public void execFile(String filename) throws FileNotFoundException {
        execFile(new File(filename));
    }

    /**
     * Run a script file.
     *
     * @param file
     * @throws FileNotFoundException if the file does not exist.
     */
    static public void execFile(File file) throws FileNotFoundException {
        // get a Python interpreter context, make sure it's ok
        getPythonInterpreter();
        if (interp == null) {
            log.error("Can't contine to execute command, could not create interpreter");
            return;
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        interp.execfile(new FileInputStream(file));
    }

    static public void execCommand(String command) {
        // get a Python interpreter context, make sure it's ok
        getPythonInterpreter();
        if (interp == null) {
            log.error("Can't contine to execute command, could not create interpreter");
            return;
        }

        interp.exec(command);
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

        Properties properties = new Properties();
        // Get properties for interpreter
        // Search in user files, the settings directory, and in the program path
        InputStream is = FileUtil.findInputStream("python.properties", new String[]{
            FileUtil.getUserFilesPath(),
            FileUtil.getPreferencesPath(),
            FileUtil.getProgramPath()
        });
        if (is != null) {
            try {
                properties = new Properties(System.getProperties());
                properties.load(is);
            } catch (IOException ex) {
                log.error("Found, but unable to read python.properties: {}", ex.getMessage());
                properties = null;
            }
        }

        // must create one.
        try {
            log.debug("create interpreter");
            PySystemState.initialize(null, properties);

            interp = new PythonInterpreter();

            // have jython execute the default setup
            log.debug("load defaults from {}", defaultContextFile);
            execFile(FileUtil.getExternalFilename(defaultContextFile));

            return interp;
        } catch (FileNotFoundException ex) {
            log.error("Python is not using the default JMRI context, since {} could not be found to provide it.", defaultContextFile);
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
     */
    static public JTextArea getOutputArea() {
        if (outputlog == null) {
            // convert to stored output

            try {
                // create the output area
                outputlog = new JTextArea();

                // Add the I/O pipes
                PipedWriter pw = new PipedWriter();

                interp.setErr(pw);
                interp.setOut(pw);

                // ensure the output pipe is read and stored into a
                // Swing TextArea data model
                PipedReader pr = new PipedReader(pw);
                PipeListener pl = new PipeListener(pr, outputlog);
                pl.start();
            } catch (Exception e) {
                log.error("Exception creating jython output area", e);
                return null;
            }
        }
        return outputlog;
    }

    /**
     * JTextArea containing the output
     */
    static private JTextArea outputlog = null;

    /**
     * Name of the file containing the Python code defining JMRI defaults
     */
    static String defaultContextFile = "program:jython/jmri_defaults.py";

    // initialize logging
    static Logger log = LoggerFactory.getLogger(PythonInterp.class.getName());

}

/* @(#)RunJythonScript.java */
