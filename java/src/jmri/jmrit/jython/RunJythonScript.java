package jmri.jmrit.jython;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.script.ScriptException;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import jmri.script.JmriScriptEngineManager;
import jmri.script.ScriptFileChooser;
import jmri.util.FileUtil;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Action runs a script using an available script engine.
 * <p>
 * The script engine to use is determined by the script's extension.
 * <p>
 * There are two constructors. One, without a script file name, will open a
 * FileDialog to prompt for the file to use. The other, with a File object, will
 * directly invoke that file.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 */
public class RunJythonScript extends JmriAbstractAction {

    public RunJythonScript(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RunJythonScript(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Constructor that, when action is invoked, opens a JFileChooser to select
     * file to invoke.
     *
     * @param name Action name
     */
    public RunJythonScript(String name) {
        super(name);
        configuredFile = null;
    }

    /**
     * Constructor that, when action is invoked, directly invokes the provided
     * File.
     *
     * @param name Action name
     * @param file the script file to invoke
     */
    public RunJythonScript(String name, File file) {
        super(name);
        this.configuredFile = file;
    }

    File configuredFile;

    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    static JFileChooser fci = null;

    /**
     * Invoking this action via an event triggers display of a file dialog. If a
     * file is selected, it's then invoked as a script.
     *
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        File thisFile;
        if (configuredFile != null) {
            thisFile = configuredFile;
        } else {
            thisFile = selectFile();
        }

        // and invoke that file
        if (thisFile != null) {
            invoke(thisFile);
        } else {
            log.info("No file selected");
        }
    }

    File selectFile() {
        if (fci == null) {
            fci = new ScriptFileChooser(FileUtil.getScriptsPath());
            fci.setDialogTitle("Find desired script file");
        } else {
            // when reusing the chooser, make sure new files are included
            fci.rescanCurrentDirectory();
        }

        int retVal = fci.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            // Run the script from its filename
            return file;
        }
        return null;
    }

    void invoke(File file) {
        try {
            JmriScriptEngineManager.getDefault().eval(file);
        } catch (ScriptException | java.io.IOException ex) {
            log.error("Unable to execute script.", ex);
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(RunJythonScript.class);

}
