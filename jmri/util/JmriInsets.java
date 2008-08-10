// JmriInsets.java

package jmri.util;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * This class attempts to retrieve the screen insets for all
 * operating systems.
 *
 * The standard insets command fails on Linux - this class attempts
 * to rectify that.
 *
 * Borrows heavily from the Linsets class created by:
 *   A. Tres Finocchiaro
 *   http://forums.sun.com/thread.jspa?threadID=5169228&start=29
 *
 * 
 * @author      Matt Harris
 * @version     $Revision: 1.1 $
 */
public class JmriInsets {

    private static final String DESKTOP_ENVIRONMENTS = "kdesktop|gnome-panel|xfce|darwin";
    
    private static final String GNOME_CONFIG = "%gconf.xml";
    private static final String GNOME_PANEL = "_panel_screen";
    private static final String GNOME_ROOT = System.getProperty("user.home") + "/.gconf/apps/panel/toplevels/";
    
    private static final String KDE_CONFIG = System.getProperty("user.home") + "/.kde/share/config/kickerrc";
    
    private static final String XFCE_CONFIG = System.getProperty("user.home") + "/.config/xfce4/mcs_settings/panel.xml";
    
    private static final String OS_NAME = System.getProperty("os.name");

    
    /**
     * Creates a new instance of JmriInsets
     */
    public static Insets getInsets() {
    
        //JFrame dummy = new JFrame();
        
        //Insets insets = dummy.getToolkit().getScreenInsets(dummy.getGraphicsConfiguration());
        
        //if ( OS_NAME == "Linux" ) {
        //    insets.top = 36;
        //    insets.bottom = 36;
        //}
        //return insets;
        switch (isCompatibleOS()) {
            case 0: return getKDEInsets();
            case 1: return getGnomeInsets();
            case 2: return getXfceInsets();
            case 3: return getDarwinInsets();
            default: return getDefaultInsets();
        }

    }
    
    /*
     * Determine the current non-MS Windows Operating System
     */
    private static int isCompatibleOS(){
        if(!OS_NAME.toLowerCase().startsWith("windows")) {
            try {
                Process p = Runtime.getRuntime().exec("ps ax");
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                
                java.util.List desktopList = Arrays.asList(DESKTOP_ENVIRONMENTS.split("\\|"));
                
                String line = r.readLine();
                while (line != null) {
                    for (int i=0; i < desktopList.size(); i++) {
                        String s = desktopList.get(i).toString();
                        if(line.contains(s) && !line.contains("grep"))
                            return desktopList.indexOf(s);
                    }
                    line = r.readLine();
                }
            }
            catch (Exception e) {
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
        int iniCustomSize = getKdeINI("General", "CustomSize");
        int iniSize = getKdeINI("General", "Size");
        int iniPosition = getKdeINI("General", "Position");
        int position = iniPosition==-1?3:iniPosition;
        int size = (iniCustomSize==-1 || iniSize!=4)?iniSize:iniCustomSize;
        size = size<24?sizes[size]:size;
        i[position]=size;
        return new Insets(i[2],i[0],i[3],i[1]);
    }
    
    /*
     * Get insets for Gnome
     */
    private static Insets getGnomeInsets() {
        File gnomeRoot = new File(GNOME_ROOT);
        
        int n=0; int s=0; int e=0; int w=0;
        for(int i=0; i<gnomeRoot.listFiles().length; i++) {
            File f = gnomeRoot.listFiles()[i];
            String folder = f.getName();
            if(f.isDirectory() && folder.contains(GNOME_PANEL)) {
                int val = getGnomeXML(new File(GNOME_ROOT + "/" + folder + "/" + GNOME_CONFIG));
                if(val == -1)
                    ; //Skip
                else if (folder.startsWith("top" + GNOME_PANEL))
                    n = Math.max(val, n);
                else if (folder.startsWith("bottom" + GNOME_PANEL))
                    s = Math.max(val, s);
                else if (folder.startsWith("right" + GNOME_PANEL))
                    e = Math.max(val, e);
                else if (folder.startsWith("left" + GNOME_PANEL))
                    w = Math.max(val, w);
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
     * Default insets (Java standard)
     * Write log entry for any OS that we don't yet now how to handle.
     */
    private static Insets getDefaultInsets() {
        if(!OS_NAME.toLowerCase().startsWith("windows"))
            // MS Windows will always end-up here, so no need to log.
            return getDefaultInsets(true);
        else
            // any other OS ends up here
            return getDefaultInsets(false);
    }
    
    private static Insets getDefaultInsets(boolean logOS) {
        if (logOS) log.error("Trying default insets for " + OS_NAME);
        try {
            GraphicsDevice gs[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for(int i = 0; i < gs.length; i++) {
                GraphicsConfiguration gc[] = gs[i].getConfigurations();
                for(int j = 0; j < gc.length; j++) {
                    return(Toolkit.getDefaultToolkit().getScreenInsets(gc[j]));                    
                }
            }
        }
        catch (HeadlessException h) {
            log.error("Error: Headless error - no GUI available");
        }
        return new Insets(0,0,0,0);
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
            FileReader reader = new FileReader(xmlFile);
            BufferedReader buffer = new BufferedReader(reader);
            String temp = buffer.readLine();
            while(temp != null) {
                if(temp.contains("<entry name=\"size\"")) {
                    found = true;
                    break;
                }
                temp = buffer.readLine();
            }
            buffer.close();
            reader.close();
            if(found) {
                temp = temp.substring(temp.indexOf("value=\"") + 7);
                return Integer.parseInt(temp.substring(0, temp.indexOf("\">")));
            }
        }
        catch (Exception e) {
            log.error("Error: " + e.getMessage());
        }
        return -1;
    }
    
    private static int getKdeINI(String category, String component) {
        try {
            File f = new File(KDE_CONFIG);
            if(!f.exists() || category == null || component == null)
                return -1;
            
            boolean found = false;
            FileReader reader = new FileReader(f);
            BufferedReader buffer = new BufferedReader(reader);
            String value = null;
            String temp = buffer.readLine();
            while(temp !=null) {
                if(temp.trim().equals("[" + category + "]")) {
                    temp = buffer.readLine();
                    while(temp != null) {
                        if(temp.trim().startsWith("["))
                            return -1;
                        else if (temp.startsWith(component + "=")) {
                            value = temp.substring(component.length() + 1);
                            found = true;
                            break;
                        }
                        temp = buffer.readLine();
                    }
                }
                if(found == true)
                    break;
                temp = buffer.readLine();
            }
            buffer.close();
            reader.close();
            if(found)
                return Integer.parseInt(value);
        }
        catch (Exception e) {
            log.error("Error: " + e.getMessage());
        }
        return -1;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriInsets.class.getName());
}
