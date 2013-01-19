// RunJythonScript.java

package jmri.jmrit.jython;

import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.*;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import jmri.util.FileUtil;

/**
 * This Action runs a script by invoking a Jython interpreter.
 * <P>
 * A standard JMRI-Jython dialog is defined by
 * invoking the "jython/jmri-defaults.py" file before starting the
 * user code.
 * <P>
 * There are two constructors. One, without a script file name,
 * will open a FileDialog to prompt for the file to use. The other, 
 * with a File object, will directly invoke that file.
 * <P>
 * Access is via Java reflection so that both users and developers can work
 * without the jython.jar file in the classpath. To make it easier to
 * read the code, the "non-reflection" statements are in the comments.
 *
 * @author	Bob Jacobsen    Copyright (C) 2004, 2007
 * @version     $Revision$
 */
public class RunJythonScript extends JmriAbstractAction {
    
    public  RunJythonScript(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public  RunJythonScript(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    /**
     * Constructor that, when action is invoked, opens a JFileChooser
     * to select file to invoke.
     * @param name Action name
     */
    public RunJythonScript(String name) {
        super(name);
        configuredFile = null;
    }

    /**
     * Constructor that, when action is invoked, directly
     * invokes the provided File
     * @param name Action name
     */
    public RunJythonScript(String name, File file) {
        super(name);
        this.configuredFile = file;
    }

    File configuredFile;
    
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
        File thisFile;
        if (configuredFile != null) 
            thisFile = configuredFile;
        else
            thisFile = selectFile();
        
        // and invoke that file
        if (thisFile != null)
            invoke(thisFile);
        else
            log.info("No file selected");
    }

    File selectFile() {
        if (fci==null) {
            //fci = new JFileChooser(System.getProperty("user.dir")+java.io.File.separator+"jython");
            fci = new JFileChooser(FileUtil.getScriptsPath());
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");
            filt.addExtension("py");
            fci.setFileFilter(filt);
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
            return file;
        }
        return null;
    }

    void invoke(File file) {
        jmri.util.PythonInterp.runScript(jmri.util.FileUtil.getExternalFilename(file.toString()));
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RunJythonScript.class.getName());

}

/* @(#)RunJythonScript.java */
