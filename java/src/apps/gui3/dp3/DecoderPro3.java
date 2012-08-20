// Paned.java
package apps.gui3.dp3;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;

/**
 * The JMRI application for developing the DecoderPro 3 GUI <P>
 *
 * <hr> This file is part of JMRI. <P> JMRI is free software; you can
 * redistribute it and/or modify it under the terms of version 2 of the GNU
 * General Public License as published by the Free Software Foundation. See the
 * "COPYING" file for a copy of this license. <P> JMRI is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * @author	Bob Jacobsen Copyright 2003, 2004, 2007, 2009, 2010
 * @version $Revision$
 */
public class DecoderPro3 extends apps.gui3.Apps3 {

    private static File menuFile = null;
    private static File toolbarFile = null;
    private static String applicationName = "DecoderPro 3";

    public DecoderPro3(String[] args) {
        super(applicationName, "DecoderProConfig3.xml", args);
    }
    
    public synchronized static File getMenuFile() {
        if (menuFile == null) {
            menuFile = new File("dp3/Gui3Menus.xml");
            // decide whether name is absolute or relative
            if (!menuFile.isAbsolute()) {
                // must be relative, but we want it to
                // be relative to the preferences directory
                menuFile = new File(XmlFile.prefsDir() + "dp3/Gui3Menus.xml");
            }
            if (!menuFile.exists()) {
                menuFile = new File("xml/config/parts/jmri/jmrit/roster/swing/RosterFrameMenu.xml");
            } else {
                log.info("Found user created menu structure this will be used instead of the system default");
            }
        }
        return menuFile;
    }

    public synchronized static File getToolbarFile() {
        if (toolbarFile == null) {
            toolbarFile = new File("dp3/Gui3MainToolBar.xml");
            // decide whether name is absolute or relative
            if (!toolbarFile.isAbsolute()) {
                // must be relative, but we want it to
                // be relative to the preferences directory
                toolbarFile = new File(XmlFile.prefsDir() + "dp3/Gui3MainToolBar.xml");
            }
            if (!toolbarFile.exists()) {
                toolbarFile = new File("xml/config/parts/jmri/jmrit/roster/swing/RosterFrameToolBar.xml");
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
     */
    @Override
    protected void displayMainFrame(java.awt.Dimension d) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (!p.isWindowPositionSaved(mainFrame.getWindowFrameRef())) {
            mainFrame.setSize(new java.awt.Dimension(1024, 600));
            mainFrame.setPreferredSize(new java.awt.Dimension(1024, 600));
        }

        mainFrame.setVisible(true);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("apps.gui3.dp3.DecoderPro3ActionListBundle");
    }

    // Main entry point
    public static void main(String args[]) {
        DecoderPro3 app = new DecoderPro3(args);
        app.start();
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
                AbstractAction prefsAction = new apps.gui3.TabbedPreferencesAction("Preferences");
                prefsAction.actionPerformed(null);
            }
        }
        addToActionModel();

        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    DecoderIndexFile.instance();
                } catch (Exception ex) {
                    log.error("Error in trying to initialize decoder index file " + ex.toString());
                }
            }
        };
        Thread thr = new Thread(r, "initialize decoder index");
        thr.start();
        jmri.InstanceManager.tabbedPreferencesInstance().disablePreferenceItem("STARTUP", "apps.PerformFilePanel");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderPro3.class.getName());
}
