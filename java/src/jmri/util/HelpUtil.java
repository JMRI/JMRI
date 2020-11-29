package jmri.util;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.*;

import javax.annotation.Nonnull;
import javax.help.*;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with Java Help.
 * <p>
 * This class was created to contain common Java Help information.
 * <p>
 * It assumes that Java Help 1.1.8 is in use
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class HelpUtil {

    private HelpUtil(){
        // this is a class of static methods
    }

    /**
     * Append a help menu to the menu bar.
     *
     * @param menuBar the menu bar to add the help menu to
     * @param ref     context-sensitive help reference
     * @param direct  true if this call should complete the help menu by adding
     *                the general help
     * @return new Help menu, in case user wants to add more items or null if
     *         unable to create the help menu
     */
    public static JMenu helpMenu(JMenuBar menuBar, String ref, boolean direct) {
        JMenu helpMenu = makeHelpMenu(ref, direct);
        if (helpMenu != null) {
            menuBar.add(helpMenu);
        }
        return helpMenu;
    }

    public static JMenu makeHelpMenu(String ref, boolean direct) {
        if (!initOK()) {
            log.warn("help initialization not completed");
            return null;  // initialization failed
        }
        JMenu helpMenu = new JMenu(Bundle.getMessage("ButtonHelp"));
        JMenuItem item = makeHelpMenuItem(ref);
        if (item == null) {
            log.error("Can't make help menu item for {}", ref);
            return null;
        }
        helpMenu.add(item);

        if (direct) {
            ServiceLoader<MenuProvider> providers = ServiceLoader.load(MenuProvider.class);
            providers.forEach(provider -> provider.getHelpMenuItems().forEach(i -> {
                if (i != null) {
                    helpMenu.add(i);
                } else {
                    helpMenu.addSeparator();
                }
            }));
        }
        return helpMenu;
    }

    public static JMenuItem makeHelpMenuItem(String ref) {
        if (!initOK()) {
            return null;  // initialization failed
        }
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("MenuItemWindowHelp"));
        globalHelpBroker.enableHelpOnButton(menuItem, ref, null);

        // start help to see what happend
        log.debug("help: {}:{}:{}", globalHelpSet.getHomeID(), globalHelpSet.getTitle(), globalHelpSet.getHelpSetURL());

        return menuItem;
    }

    public static void addHelpToComponent(java.awt.Component component, String ref) {
        if (globalHelpBroker != null) {
            globalHelpBroker.enableHelpOnButton(component, ref, null);
            log.debug("Help added for {}", ref);
        } else {
            log.debug("globalHelpBroker is null");
        }
    }

    public static void displayHelpRef(String ref) {
        if (globalHelpBroker == null) {
            log.debug("can't display {} help page because help system reference is null", ref);
            return;
        }
        try {
            globalHelpBroker.setCurrentID(ref);
            globalHelpBroker.setDisplayed(true);
        } catch (javax.help.BadIDException e) {
            log.error("unable to show help page {} due to:", ref, e);
        }
    }

    static boolean init = false;
    static boolean failed = true;

    public static boolean initOK() {
        if (!init) {
            init = true;
            try {
                Locale locale = Locale.getDefault();
                String language = locale.getLanguage();
                String helpsetName = "help/" + language + "/JmriHelp_" + language + ".hs";
                URL hsURL = FileUtil.findURL(helpsetName);
                if (hsURL != null) {
                    log.debug("JavaHelp using {}", helpsetName);
                } else {
                    log.info("JavaHelp: File {} not found, dropping to default", helpsetName);
                    language = "en";
                    helpsetName = "help/" + language + "/JmriHelp_" + language + ".hs";
                    hsURL = FileUtil.findURL(helpsetName);
                }
                try {
                    globalHelpSet = new HelpSet(null, hsURL);
                } catch (NoClassDefFoundError ee) {
                    log.debug("classpath={}", System.getProperty("java.class.path", "<unknown>"));
                    log.debug("classversion={}", System.getProperty("java.class.version", "<unknown>"));
                    log.error("Help classes not found, help system omitted");
                    return false;
                } catch (HelpSetException e2) {
                    log.error("HelpSet {} not found, help system omitted", helpsetName);
                    return false;
                }
                globalHelpBroker = globalHelpSet.createHelpBroker();

            } catch (NoSuchMethodError e2) {
                log.error("Is jh.jar available? Error starting help system", e2);
            }
            failed = false;
        }
        return !failed;
    }

    public static HelpBroker getGlobalHelpBroker() {
        if (globalHelpBroker == null) {
            HelpUtil.initOK();
        }
        return globalHelpBroker;
    }

    public static Action getHelpAction(final String name, final Icon icon, final String id) {
        return new AbstractAction(name, icon) {

            String helpID = id;

            @Override
            public void actionPerformed(ActionEvent event) {
                globalHelpBroker.setCurrentID(helpID);
                globalHelpBroker.setDisplayed(true);
            }
        };
    }

    /**
     * Set the default content viewer UI.
     *
     * @param ui full class name of the content viewer UI
     * @see SwingHelpUtilities#setContentViewerUI(java.lang.String)
     */
    public static void setContentViewerUI(String ui) {
        SwingHelpUtilities.setContentViewerUI(ui);
    }

    static HelpSet globalHelpSet;
    static HelpBroker globalHelpBroker;

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(HelpUtil.class);

    public interface MenuProvider {

        /**
         * Get the menu items to include in the menu. Any menu item that is null
         * will be replaced with a separator.
         *
         * @return the list of menu items
         */
        @Nonnull
        List<JMenuItem> getHelpMenuItems();
    
    }
}
