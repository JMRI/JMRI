package jmri.util;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.web.server.WebServerPreferences;

/**
 * Common utility methods for displaying JMRI help pages.
 * <p>
 * This class was created to contain common Java Help information but is now
 * changed to use a web browser instead.
 *
 * @author Bob Jacobsen      Copyright 2007
 * @author Daniel Bergqvist  Copyright 2021
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
        if (menuBar != null) {
            menuBar.add(helpMenu);
        }
        return helpMenu;
    }

    public static JMenu makeHelpMenu(String ref, boolean direct) {
        JMenu helpMenu = new JMenu(Bundle.getMessage("ButtonHelp"));
        helpMenu.add(makeHelpMenuItem(ref));

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
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("MenuItemWindowHelp"));

        menuItem.addActionListener((ignore) -> {
            displayHelpRef(ref);
        });

        return menuItem;
    }

    public static void addHelpToComponent(java.awt.Component component, String ref) {
        enableHelpOnButton(component, ref);
    }

    // https://coderanch.com/how-to/javadoc/javahelp-2.0_05/javax/help/HelpBroker.html#enableHelpOnButton(java.awt.Component,%20java.lang.String,%20javax.help.HelpSet)
    public static void enableHelpOnButton(java.awt.Component comp, String id) {
        if (comp instanceof javax.swing.AbstractButton) {
            ((javax.swing.AbstractButton)comp).addActionListener((ignore) -> {
                displayHelpRef(id);
            });
        } else if (comp instanceof java.awt.Button) {
            ((java.awt.Button)comp).addActionListener((ignore) -> {
                displayHelpRef(id);
            });
        } else {
            throw new IllegalArgumentException("comp is not a javax.swing.AbstractButton or a java.awt.Button");
        }
    }

    public static void displayHelpRef(String ref) {
        // We only have English (en) and French (fr) help files.
        Boolean isFrench = "fr".equals(
                InstanceManager.getDefault(
                        GuiLafPreferencesManager.class).getLocale().getLanguage());
        String localeStr = isFrench ? "fr" : "en";

        HelpUtilPreferences preferences = InstanceManager.getDefault(HelpUtilPreferences.class);

        String tempFile = "help/" + localeStr + "/" + ref.replace(".", "/");
        String[] fileParts = tempFile.split("_", 2);
        String file = fileParts[0] + ".shtml";
        if (fileParts.length > 1) {
            file = file + "#" + fileParts[1];
        }

        String url;
        if (preferences.getOpenHelpOnline()) {
            url = "https://www.jmri.org/" + file;
        } else if (preferences.getOpenHelpOnJMRIWebServer()) {
            WebServerPreferences webServerPreferences =
                    InstanceManager.getDefault(WebServerPreferences.class);
            String port = Integer.toString(webServerPreferences.getPort());
            url = "http://localhost:"+port+"/" + file;
        } else {    // Assume open help on file if no other option is selected
            String fileName = FileUtil.getProgramPath().replace("\\", "/") + "help/" + localeStr + "/local/stub/" + ref + ".html";
            File f = new File(fileName);
            if (!f.exists()) {
                log.error("The help reference \"{}\" is not found. File is not found: {}", ref, fileName);
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("HelpError_ReferenceNotFound", ref),
                        Bundle.getMessage("HelpError_Title"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            url = "file://" + fileName;
        }
        try {
            jmri.util.HelpUtil.openWebPage(url);
        } catch (JmriException e) {
            log.error("unable to show help page {} due to:", ref, e);
        }
    }

    public static void openWebPage(String url) throws JmriException {
        try {
            URI uri = new URI(url);
            if (!url.toLowerCase().startsWith("file://")) {
                HttpURLConnection request = (HttpURLConnection) uri.toURL().openConnection();
                request.setRequestMethod("GET");
                request.connect();
                if (request.getResponseCode() != 200) {
                    throw new JmriException(String.format(
                            "Failed to connect to web page: %d, %s",
                            request.getResponseCode(), request.getResponseMessage()));
                }
            }
            if ( Desktop.getDesktop().isSupported( Desktop.Action.BROWSE) ) {
                // Open browser to URL with draft report
                Desktop.getDesktop().browse(uri);
            } else {
                throw new JmriException(String.format(
                        "Failed to connect to web page. java.awt.Desktop doesn't suppport Action.BROWSE"));
            }
        } catch (IOException | URISyntaxException e) {
            throw new JmriException(String.format(
                    "Failed to connect to web page. Exception thrown: %s",
                    e.getMessage()), e);
        }
    }

    public static Action getHelpAction(final String name, final Icon icon, final String id) {
        return new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent event) {
                displayHelpRef(id);
            }
        };
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelpUtil.class);

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
