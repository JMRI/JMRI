// HelpUtil.java

package jmri.util;

import apps.AboutAction;
import javax.help.HelpBroker;
import javax.help.HelpSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;

import java.net.URL;
import java.util.EventObject;
import javax.swing.*;
import jmri.plaf.macosx.AboutHandler;
import jmri.plaf.macosx.Application;
import jmri.swing.AboutDialog;

/**
 * Common utility methods for working with Java Help.
 * <P>
 * This class was created to contain common Java Help information.
 * <P>
 * It assumes that Java Help 1.1.8 is in use
 *
 * @author Bob Jacobsen  Copyright 2007
 * @version $Revision$
 */

public class HelpUtil {

    /**
     * @param direct true if this call should complete the help menu
     * by adding the general help
     * @return new Help menu, in case user wants to add more items
     */
    static public JMenu helpMenu(JMenuBar menuBar, String ref, boolean direct) {
        JMenu helpMenu = makeHelpMenu(ref, direct);
        menuBar.add(helpMenu);
        return helpMenu;
    }
    
    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("apps.AppsBundle");
    
    static public JMenu makeHelpMenu(String ref, boolean direct) {
        if (!initOK()) {
            log.warn("help initialization not completed");
            return null;  // initialization failed
        }
        JMenu helpMenu = new JMenu(Bundle.getMessage("HELP"));
        JMenuItem item = makeHelpMenuItem(ref);
        if (item == null) {
            log.error("Can't make help menu item for "+ref);
            return null;
        }
        helpMenu.add(item);
        
        if (direct) {
            item = new JMenuItem(rb.getString("MenuItemHelp"));
            globalHelpBroker.enableHelpOnButton(item, "index", null);
            helpMenu.add(item);
            
            // add standard items
            JMenuItem license = new JMenuItem(rb.getString("MenuItemLicense"));
            helpMenu.add(license);
            license.addActionListener(new apps.LicenseAction());

            JMenuItem directories = new JMenuItem(rb.getString("MenuItemLocations"));
            helpMenu.add(directories);
            directories.addActionListener(new jmri.jmrit.XmlFileLocationAction());
    
            JMenuItem context = new JMenuItem(rb.getString("MenuItemContext"));
            helpMenu.add(context);
            context.addActionListener(new apps.ReportContextAction());

            JMenuItem console = new JMenuItem(rb.getString("MenuItemConsole"));
            helpMenu.add(console);
            console.addActionListener(new apps.SystemConsoleAction());

            helpMenu.add(new jmri.jmrit.mailreport.ReportAction());

            // Put about dialog in Apple's prefered area on Mac OS X
            if (SystemType.isMacOSX()) {
                try {
                    Application.getApplication().setAboutHandler(new AboutHandler() {

                        @Override
                        public void handleAbout(EventObject eo) {
                            new AboutDialog(null, true).setVisible(true);
                        }
                    });
                } catch (java.lang.RuntimeException re) {
                    log.error("Unable to put About handler in default location", re);
                }
            }
            // Include About in Help menu if not on Mac OS X or not using Aqua Look and Feel
            if (!SystemType.isMacOSX() || !UIManager.getLookAndFeel().isNativeLookAndFeel()) {
                helpMenu.addSeparator();
                JMenuItem about = new JMenuItem(rb.getString("MenuItemAbout") + " " + jmri.Application.getApplicationName());
                helpMenu.add(about);
                about.addActionListener(new AboutAction());
            }
        }
        return helpMenu;
    }
    
    static public JMenuItem makeHelpMenuItem(String ref) {
        if (!initOK()) return null;  // initialization failed
        
        JMenuItem menuItem = new JMenuItem(rb.getString("MenuItemWindowHelp"));
        globalHelpBroker.enableHelpOnButton(menuItem, ref, null);

        // start help to see what happend
        log.debug("help: "+globalHelpSet.getHomeID()+":"+globalHelpSet.getTitle()
                           +":"+globalHelpSet.getHelpSetURL());

        return menuItem;
    }

    static public void addHelpToComponent(java.awt.Component component, String ref) {
        if (globalHelpBroker!=null)
            globalHelpBroker.enableHelpOnButton(component, ref, null);
    }
    
    static public void displayHelpRef(String ref) {
        if (globalHelpBroker == null) {
            log.debug("can't display "+ref+" help page because help system reference is null");
            return;
        }
        try {
            globalHelpBroker.setCurrentID(ref);
            globalHelpBroker.setDisplayed(true);
        } catch (javax.help.BadIDException e) {
            log.error("unable to show help page "+ref+" due to "+e);
        }
    }
    
    static boolean init = false;
    static boolean failed = true;
        
    static public boolean initOK() {
        if (!init) {
            init = true;
            try {
                Locale locale = Locale.getDefault();
                String language = locale.getLanguage();
                String helpsetName = "help/"+language+"/JmriHelp_"+language+".hs";
                File file = new File(helpsetName);
                if (file.isFile()) {
                    log.debug("JavaHelp using "+helpsetName);
                } else {
                    System.out.println("JavaHelp: File "+helpsetName+" not found, dropping to default");
                    language = "en";
                    helpsetName = "help/"+language+"/JmriHelp_"+language+".hs";
                }
                URL hsURL;
                try {
                    hsURL = new URL("file:"+helpsetName);
                    globalHelpSet = new HelpSet(null, hsURL);
                } catch (java.lang.NoClassDefFoundError ee) {
                    log.debug("classpath="+System.getProperty("java.class.path","<unknown>"));
                    log.debug("classversion="+System.getProperty("java.class.version","<unknown>"));
                    log.error("Help classes not found, help system omitted");
                    return false;
                } catch (java.lang.Exception e2) {
                    log.error("HelpSet "+helpsetName+" not found, help system omitted");
                    return false;
                }
                globalHelpBroker = globalHelpSet.createHelpBroker();

            } catch (java.lang.NoSuchMethodError e2) {
                log.error("Is jh.jar available? Error starting help system: "+e2);
            }
            failed = false;
        }
        return !failed;
    }
    
    static public HelpBroker getGlobalHelpBroker() {
        return globalHelpBroker;
    }

    static public Action getHelpAction(final String name, final Icon icon, final String id) {
        return new AbstractAction(name, icon) {
            String helpID = id;
            public void actionPerformed(ActionEvent event) {
                globalHelpBroker.setCurrentID(helpID);
                globalHelpBroker.setDisplayed(true);
            }
        };
    }
    
    static HelpSet globalHelpSet;
    static HelpBroker globalHelpBroker;
    
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HelpUtil.class.getName());
}
