// RunJythonScript.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.*;

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
 * @version     $Revision: 1.5 $
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
            jmri.util.PythonInterp.runScript(file.toString());
        }
    }


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RunJythonScript.class.getName());

}

/* @(#)RunJythonScript.java */
