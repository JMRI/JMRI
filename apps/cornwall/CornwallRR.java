// CornwallRR.java

package apps.cornwall;

import apps.Apps;

import java.text.MessageFormat;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * Nick Kulp's Cornwall Railroad.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.19 $
 */
public class CornwallRR extends Apps {

    protected void createMenus(JMenuBar menuBar, JFrame frame) {
        fileMenu(menuBar, frame);
        editMenu(menuBar, frame);
        toolsMenu(menuBar, frame);
        rosterMenu(menuBar, frame);
        panelMenu(menuBar, frame);
        systemsMenu(menuBar, frame);
        debugMenu(menuBar, frame);
    }

    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        // separate C/MRI and LocoNet menus
        jmri.jmrix.ActiveSystemsMenu.addItems(menuBar);
    }

    protected String line1() {
        return MessageFormat.format("Cornwall RR, based on JMRI {0}",
                                new String[]{jmri.Version.name()});
    }
    protected String line2() {
        return "http://jmri.sf.net/Panels/Cornwall.html";
    }
    protected String logo() {
        return "resources/icons/cornwall/cornwall_logo.gif";
    }

    CornwallRR(JFrame p) {
        super(p);
    }


    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        initLog4J();
        log.info("program starts");
        setConfigFilename("CornwallConfig2.xml", args);
        JFrame f = new JFrame("Cornwall Railroad");
        createFrame(new CornwallRR(f), f);

        log.info("main initialization done");
        splash(false);

        // start automation
        if (configOK) {
            // load definitions
            loadFile("CornwallDefinitions.xml");

            // start automation (whith will work in parallel)
            new CrrInit().start();

            // show panel
            loadFile("CornwallMain.xml");
        } else {
            log.warn("Truncating startup because couldn't connect to layout");
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CornwallRR.class.getName());
}


