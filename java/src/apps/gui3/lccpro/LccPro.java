package apps.gui3.lccpro;

import java.io.File;
import javax.swing.AbstractAction;

import apps.gui3.Apps3;
import apps.gui3.FirstTimeStartUpWizard;
import apps.gui3.FirstTimeStartUpWizardAction;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import jmri.util.FileUtil;

/**
 * The JMRI application for configuring an LCC network.
 * <p>
 * Inserts LccPro interface elements stored in xml/config/parts/jmri/
 * that are also used in the web server interface.
 *
 * @author Bob Jacobsen Copyright 2024
 */
public class LccPro extends Apps3 {

    private static String menuFile = null;
    private static String toolbarFile = null;
    private static final String applicationName = "LccPro";

    public LccPro(String[] args) {
        super(applicationName, "LccProConfig.xml", args);
    }

    public synchronized static String getMenuFile() {
        if (menuFile == null) {
            menuFile = "lccpro/Gui3Menus.xml";
            File file = new File(menuFile);
            // decide whether name is absolute or relative
            if (!file.isAbsolute()) {
                // must be relative, but we want it to
                // be relative to the preferences directory
                menuFile = FileUtil.getUserFilesPath() + "lccpro/Gui3Menus.xml";
                file = new File(menuFile);
            }
            if (!file.exists()) {
                menuFile = "xml/config/parts/apps/gui3/lccpro/LccProFrameMenu.xml";
            } else {
                log.info("Found user created menu structure this will be used instead of the system default");
            }
        }
        return menuFile;
    }

    public synchronized static String getToolbarFile() {
        if (toolbarFile == null) {
            toolbarFile = "lccpro/Gui3MainToolBar.xml";
            File file = new File(toolbarFile);
            // decide whether name is absolute or relative
            if (!file.isAbsolute()) {
                // must be relative, but we want it to
                // be relative to the preferences directory
                toolbarFile = FileUtil.getUserFilesPath() + "lccpro/Gui3MainToolBar.xml";
                file = new File(toolbarFile);
            }
            if (!file.exists()) {
                toolbarFile = "xml/config/parts/apps/gui3/lccpro/LccProFrameToolBar.xml";
            } else {
                log.info("Found user created toolbar structure this will be used instead of the system default");
            }
        }
        return toolbarFile;
    }

    /** If we don't have the right kind of connection, launch the
     * start up wizard.
     */
    @Override
    protected boolean wizardLaunchCheck() {
        var memo = jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class);
        return memo == null;
    }

    /**
     * Provide a custom first-time wizard
     */
    @Override
    public void launchFirstTimeStartupWizard() {
        FirstTimeStartUpWizardAction prefsAction = new FirstTimeStartUpWizardAction("Start Up Wizard"){
            @Override
            public FirstTimeStartUpWizard makeWizard(JmriJFrame f, Apps3 app) {
                f.setTitle("LccPro Wizard");
                return new FirstTimeStartUpWizard(f, app){
                    @Override
                    protected void customizeConnection() {
                        connectionConfigPane.manuBox.setSelectedItem("LCC");
                        connectionConfigPane.manuBox.setEnabled(false);
                    }
                    @Override
                    protected String firstPrompt() {
                        return "Next you need to configure your LCC connection.\n\nThen select the serial port or enter in the IP address of the device";
                    }
                };
            }      
        };
        prefsAction.setApp(this);
        prefsAction.actionPerformed(null);
    }

    @Override
    protected void createMainFrame() {
        // create and populate main window
        mainFrame = new LccProWindow(getMenuFile(), getToolbarFile());
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
    public static void main(String[] args) {
        preInit(args);
        LccPro app = new LccPro(args);
        app.start();
    }

    static public void preInit(String[] args) {
        apps.gui3.Apps3.preInit(applicationName);
        apps.gui3.Apps3.setConfigFilename("LccProConfig.xml", args);
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

        // kick off update of decoder index if needed
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            try {
                jmri.jmrit.decoderdefn.DecoderIndexFile.updateIndexIfNeeded();
            } catch (org.jdom2.JDOMException| java.io.IOException e) {
                log.error("Exception trying to pre-load decoderIndex", e);
            }
        });
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LccPro.class);

}
