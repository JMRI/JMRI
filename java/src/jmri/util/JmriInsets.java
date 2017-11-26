package jmri.util;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class attempts to retrieve the screen insets for all operating systems.
 * <p>
 * The standard insets command fails on Linux - this class attempts to rectify
 * that.
 * <p>
 * <a href="http://forums.sun.com/thread.jspa?threadID=5169228&start=29">
 * Borrows heavily from the Linsets class created by: A. Tres Finocchiaro
 * </a>
 *
 *
 * @author Matt Harris
 */
public class JmriInsets {

    private static final String DESKTOP_ENVIRONMENTS = "kdesktop|gnome-panel|xfce|darwin|icewm"; //NOI18N

    private static final String GNOME_CONFIG = "%gconf.xml"; //NOI18N
    private static final String GNOME_PANEL = "_panel_screen"; //NOI18N
    private static final String GNOME_ROOT = System.getProperty("user.home") + "/.gconf/apps/panel/toplevels/"; //NOI18N

    private static final String KDE_CONFIG = System.getProperty("user.home") + "/.kde/share/config/kickerrc"; //NOI18N

    //private static final String XFCE_CONFIG = System.getProperty("user.home") + "/.config/xfce4/mcs_settings/panel.xml";
    private static final String OS_NAME = SystemType.getOSName();

    // Set this to -2 initially (out of the normal range)
    // which can then be used to determine if we need to
    // determine the window manager in use.
    private static int linuxWM = -2;

    /**
     * Creates a new instance of JmriInsets
     *
     * @return the new instance
     */
    public static Insets getInsets() {

        if (linuxWM == -2) {
            linuxWM = getLinuxWindowManager();
        }

        switch (linuxWM) {
            case 0:
                return getKDEInsets();
            case 1:
                return getGnomeInsets();
            case 2:
                return getXfceInsets();
            case 3:
                return getDarwinInsets();
            case 4:
                return getIcewmInsets();
            default:
                return getDefaultInsets();
        }

    }

    /*
     * Determine the current Linux Window Manager
     */
    private static int getLinuxWindowManager() {
        if (!SystemType.isWindows()
                && !OS_NAME.toLowerCase().startsWith("mac")) { //NOI18N
            try {
                Process p = Runtime.getRuntime().exec("ps ax"); //NOI18N
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

                try {
                    java.util.List<String> desktopList = Arrays.asList(DESKTOP_ENVIRONMENTS.split("\\|")); //NOI18N

                    String line = r.readLine();
                    while (line != null) {
                        for (int i = 0; i < desktopList.size(); i++) {
                            String s = desktopList.get(i);
                            if (line.contains(s) && !line.contains("grep")) //NOI18N
                            {
                                return desktopList.indexOf(s);
                            }
                        }
                        line = r.readLine();
                    }
                } catch (java.io.IOException e1) {
                    log.error("error reading file", e1);
                    throw e1;
                } finally {
                    try {
                        r.close();
                    } catch (java.io.IOException e2) {
                        log.error("Exception closing file", e2);
                    }
                }

            } catch (IOException e) {
                log.error("IO Exception");
            }
        }
        return -1;
    }

    /*
     * Get insets for KDE 3.5+
     */
    private static Insets getKDEInsets() {
        /*
         * KDE:        2 Top          |  JAVA:        0 Top
         *      0 Left       1 Right  |        1 Left       3 Right
         *           3 Bottom         |             2 Bottom
         */
        int[] sizes = {24, 30, 46, 58, 0}; // xSmall, Small, Medium, Large, xLarge, Null
        int[] i = {0, 0, 0, 0, 0};         // Left, Right, Top, Bottom, Null

        /* Needs to be fixed. Doesn't know the difference between CustomSize and Size */
        int iniCustomSize = getKdeINI("General", "CustomSize"); //NOI18N
        int iniSize = getKdeINI("General", "Size"); //NOI18N
        int iniPosition = getKdeINI("General", "Position"); //NOI18N
        int position = iniPosition == -1 ? 3 : iniPosition;
        int size = (iniCustomSize == -1 || iniSize != 4) ? iniSize : iniCustomSize;
        size = size < 24 ? sizes[size] : size;
        i[position] = size;
        return new Insets(i[2], i[0], i[3], i[1]);
    }

    /*
     * Get insets for Gnome
     */
    private static Insets getGnomeInsets() {
        File gnomeRoot = new File(GNOME_ROOT);

        int n = 0;
        int s = 0;
        int e = 0;
        int w = 0;
        File[] files = gnomeRoot.listFiles();
        if (files != null) {
            for (File f : files) {
                String folder = f.getName();
                if (f.isDirectory() && folder.contains(GNOME_PANEL)) {
                    int val = getGnomeXML(new File(GNOME_ROOT + "/" + folder + "/" + GNOME_CONFIG));
                    if (val == -1) {
                        // Skip
                    } else if (folder.startsWith("top" + GNOME_PANEL)) { //NOI18N
                        n = Math.max(val, n);
                    } else if (folder.startsWith("bottom" + GNOME_PANEL)) { //NOI18N
                        s = Math.max(val, s);
                    } else if (folder.startsWith("right" + GNOME_PANEL)) { //NOI18N
                        e = Math.max(val, e);
                    } else if (folder.startsWith("left" + GNOME_PANEL)) { //NOI18N
                        w = Math.max(val, w);
                    }
                }
            }
        }
        return new Insets(n, w, s, e);
    }

    /*
     * Get insets for Xfce
     */
    private static Insets getXfceInsets() {
        return getDefaultInsets(false);
    }

    /*
     * Get insets for Darwin (Mac OS X)
     */
    private static Insets getDarwinInsets() {
        return getDefaultInsets(false);
    }

    /*
     * Get insets for IceWM
     */
    private static Insets getIcewmInsets() {
        // OK, this is being a bit lazy but the vast majority of
        // IceWM themes do not seem to modify the taskbar height
        // from the default 25 pixels nor do they change the
        // position of being along the bottom
        return new Insets(0, 0, 25, 0);
    }

    /*
     * Default insets (Java standard)
     * Write log entry for any OS that we don't yet now how to handle.
     */
    private static Insets getDefaultInsets() {
        if (!OS_NAME.toLowerCase().startsWith("windows") // NOI18N
                && !OS_NAME.toLowerCase().startsWith("mac")) { // NOI18N
            // MS Windows & Mac OS will always end-up here, so no need to log.
            return getDefaultInsets(false);
        } else {
            // any other OS ends up here
            return getDefaultInsets(true);
        }
    }

    private static Insets getDefaultInsets(boolean logOS) {
        if (logOS) {
            log.trace("Trying default insets for {}", OS_NAME);
        }
        try {
            GraphicsDevice gs[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for (GraphicsDevice g : gs) {
                GraphicsConfiguration[] gc = g.getConfigurations();
                for (GraphicsConfiguration element : gc) {
                    return (Toolkit.getDefaultToolkit().getScreenInsets(element));
                }
            }
        } catch (HeadlessException h) {
            log.debug("Warning: Headless error - no GUI available");
        }
        return new Insets(0, 0, 0, 0);
    }

    /*
     * Some additional routines required for specific window managers
     */
 /*
     * Parse XML files for some sizes in Gnome
     */
    private static int getGnomeXML(File xmlFile) {
        try {
            boolean found = false;
            String temp;
            try (FileReader reader = new FileReader(xmlFile); BufferedReader buffer = new BufferedReader(reader)) {
                temp = buffer.readLine();
                while (temp != null) {
                    if (temp.contains("<entry name=\"size\"")) { //NOI18N
                        found = true;
                        break;
                    }
                    temp = buffer.readLine();
                }
            }
            if (temp == null) {
                return -1;
            }
            if (found) {
                temp = temp.substring(temp.indexOf("value=\"") + 7);
                return Integer.parseInt(temp.substring(0, temp.indexOf('"')));
            }
        } catch (IOException e) {
            log.error("Error parsing Gnome XML: {}", e.getMessage());
        }
        return -1;
    }

    private static int getKdeINI(String category, String component) {
        try {
            File f = new File(KDE_CONFIG);
            if (!f.exists() || category == null || component == null) {
                return -1;
            }

            boolean found = false;
            String value = null;
            FileReader reader = new FileReader(f);
            BufferedReader buffer = new BufferedReader(reader);
            try {
                String temp = buffer.readLine();
                while (temp != null) {
                    if (temp.trim().equals("[" + category + "]")) {
                        temp = buffer.readLine();
                        while (temp != null) {
                            if (temp.trim().startsWith("[")) {
                                return -1;
                            } else if (temp.startsWith(component + "=")) {
                                value = temp.substring(component.length() + 1);
                                found = true;
                                break;
                            }
                            temp = buffer.readLine();
                        }
                    }
                    if (found == true) {
                        break;
                    }
                    temp = buffer.readLine();
                }
            } catch (java.io.IOException e1) {
                log.error("error reading file", e1);
                throw e1;
            } finally {
                try {
                    buffer.close();
                } catch (java.io.IOException e2) {
                    log.error("Exception closing file", e2);
                }
                try {
                    reader.close();
                } catch (java.io.IOException e2) {
                    log.error("Exception closing file", e2);
                }
            }

            if (found) {
                return Integer.parseInt(value);
            }
        } catch (IOException e) {
            log.error("Error parsing KDI_CONFIG: {}", e.getMessage());
        }
        return -1;
    }

    private final static Logger log = LoggerFactory.getLogger(JmriInsets.class);
}
