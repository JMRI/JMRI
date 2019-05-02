package apps.gui3.dp3;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI application for developing the DecoderPro 3 GUI.
 * <p>
 * Inserts DP3 interface elements stored in xml/config/parts/jmri/
 * that are also used in the web server interface.
 *
 * @author Bob Jacobsen Copyright 2003, 2004, 2007, 2009, 2010
 */
public class DecoderPro3 extends apps.gui3.Apps3 {

    private static String menuFile = null;
    private static String toolbarFile = null;
    private static final String applicationName = "DecoderPro";

    public DecoderPro3(String[] args) {
        super(applicationName, "DecoderProConfig3.xml", args);
    }

    public synchronized static String getMenuFile() {
        if (menuFile == null) {
            menuFile = "dp3/Gui3Menus.xml";
            File file = new File(menuFile);
            // decide whether name is absolute or relative
            if (!file.isAbsolute()) {
                // must be relative, but we want it to
                // be relative to the preferences directory
                menuFile = FileUtil.getUserFilesPath() + "dp3/Gui3Menus.xml";
                file = new File(menuFile);
            }
            if (!file.exists()) {
                menuFile = "xml/config/parts/jmri/jmrit/roster/swing/RosterFrameMenu.xml";
            } else {
                log.info("Found user created menu structure this will be used instead of the system default");
            }
        }
        return menuFile;
    }

    public synchronized static String getToolbarFile() {
        if (toolbarFile == null) {
            toolbarFile = "dp3/Gui3MainToolBar.xml";
            File file = new File(toolbarFile);
            // decide whether name is absolute or relative
            if (!file.isAbsolute()) {
                // must be relative, but we want it to
                // be relative to the preferences directory
                toolbarFile = FileUtil.getUserFilesPath() + "dp3/Gui3MainToolBar.xml";
                file = new File(toolbarFile);
            }
            if (!file.exists()) {
                toolbarFile = "xml/config/parts/jmri/jmrit/roster/swing/RosterFrameToolBar.xml";
            } else {
                log.info("Found user created toolbar structure this will be used instead of the system default");
            }
        }
        return toolbarFile;
    }

    @Override
    protected void createMainFrame() {
        // create and populate main window
        mainFrame = new DecoderPro3Window(getMenuFile(), getToolbarFile());
    }

    /**
     * Force our test size. Superclass method set to max size, filling real
     * window.
     *
     * @param d size to use (ignored in this case)
     */
    @Override
    protected void displayMainFrame(java.awt.Dimension d) {
        jmri.UserPreferencesManager p = InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (!p.hasProperties(mainFrame.getWindowFrameRef())) {
            mainFrame.setSize(new java.awt.Dimension(1024, 600));
            mainFrame.setPreferredSize(new java.awt.Dimension(1024, 600));
        }

        mainFrame.setVisible(true);
    }

    // Main entry point
    public static void main(String args[]) {
        preInit(args);
        DecoderPro3 app = new DecoderPro3(args);
        app.start();
    }

    static public void preInit(String[] args) {
        apps.gui3.Apps3.preInit(applicationName);
        apps.gui3.Apps3.setConfigFilename("DecoderProConfig3.xml", args);
    }

    /**
     * Final actions before releasing control of app to user
     */
    @Override
    protected void start() {
        super.start();

        if ((!configOK) || (!configDeferredLoadOK)) {
            if (preferenceFileExists) {
                //if the preference file already exists then we will launch the normal preference window
                AbstractAction prefsAction = new apps.gui3.tabbedpreferences.TabbedPreferencesAction(Bundle.getMessage("MenuItemPreferences"));
                prefsAction.actionPerformed(null);
            }
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    InstanceManager.getDefault(DecoderIndexFile.class);
                } catch (Exception ex) {
                    log.error("Error in trying to initialize decoder index file " + ex.toString());
                }
            }
        };
        Thread thr = new Thread(r, "initialize decoder index");
        thr.start();
    }

    private final static Logger log = LoggerFactory.getLogger(DecoderPro3.class);
}
