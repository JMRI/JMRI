// Paned.java

package apps.gui3.dp3;

import java.io.File;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;

import java.util.ResourceBundle;
import javax.swing.AbstractAction;


/**
 * The JMRI application for developing the DecoderPro 3 GUI
 * <P>
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2004, 2007, 2009, 2010
 * @version     $Revision$
 */
public class DecoderPro3 extends apps.gui3.Apps3 {

    protected void createMainFrame() {
        // create and populate main window
        File menuFile = new File("dp3/Gui3Menus.xml");
        // decide whether name is absolute or relative
        if (!menuFile.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            menuFile = new File(XmlFile.prefsDir()+"dp3/Gui3Menus.xml");
        }
        if (!menuFile.exists()) {
            menuFile = new File("xml/config/apps/decoderpro/Gui3Menus.xml");
        } else {
            log.info("Found user created menu structure this will be used instead of the system default");
        }
        
        File toolbarFile = new File("dp3/Gui3MainToolBar.xml");
        // decide whether name is absolute or relative
        if (!toolbarFile.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            toolbarFile = new File(XmlFile.prefsDir()+"dp3/Gui3MainToolBar.xml");
        }
        if (!toolbarFile.exists()) {
            toolbarFile = new File("xml/config/apps/decoderpro/Gui3MainToolBar.xml");
        } else {
           log.info("Found user created toolbar structure this will be used instead of the system default");
        }
        mainFrame = new DecoderPro3Window(menuFile, toolbarFile);
    }
    
    /**
     * Force our test size. Superclass method set to max size, filling
     * real window.
     */
    @Override
    protected void displayMainFrame(java.awt.Dimension d) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if(!p.isWindowPositionSaved(mainFrame.getWindowFrameRef())) {
            mainFrame.setSize(new java.awt.Dimension(1024, 600));
            mainFrame.setPreferredSize(new java.awt.Dimension(1024, 600));
        }
        
        mainFrame.setVisible(true);
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("apps.gui3.dp3.DecoderPro3ActionListBundle");
    }

    // Main entry point
    public static void main(String args[]) {
        // do processing needed immediately, before
        // we attempt anything else
        preInit();
        setConfigFilename("DecoderProConfig3.xml", args);
        
        // create the program object
        DecoderPro3 app = new DecoderPro3();
        // do final post initialization processing
        app.postInit();
    
    }
    
    /**
     * Final actions before releasing control of app to user
     */
    @Override
    protected void postInit() {
        super.postInit();
        
        if((!configOK) || (!configDeferredLoadOK)){
            if(preferenceFileExists){
                //if the preference file already exists then we will launch the normal preference window
                AbstractAction prefsAction = new apps.gui3.TabbedPreferencesAction("Preferences");
                prefsAction.actionPerformed(null);
            } else {
                //if this is down to the preference file missing then we do something else!
                //would like to create a wizard for setting this up at some point.
                jmri.util.HelpUtil.displayHelpRef("package.apps.AppConfigPanelErrorPage");
                AbstractAction prefsAction = new apps.gui3.TabbedPreferencesAction("Preferences");
                //AbstractAction prefsAction = new apps.gui3.FirstTimeStartUpWizardAction("Start Up Wizard");
                prefsAction.actionPerformed(null);
            }
        }
        addToActionModel();
        
        Runnable r = new Runnable() {
          public void run() {
            try {
                DecoderIndexFile.instance();
            } catch (Exception ex) {
                log.error("Error in trying to setup preferences " + ex.toString());
            }
          }
        };
        Thread thr = new Thread(r);
        thr.start();
        jmri.InstanceManager.tabbedPreferencesInstance().disablePreferenceItem("STARTUP", "apps.PerformFilePanel");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderPro3.class.getName());
}


