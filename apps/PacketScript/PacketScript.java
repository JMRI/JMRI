/**
 * PacketScript.java
 */

package apps.PacketScript;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import apps.*;
import java.util.*;
import java.text.*;
import javax.swing.*;

/**
 * Main program for the NMRA PacketScript program based on JMRI.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2002
 * @version     $Revision: 1.2 $
 */
public class PacketScript extends Apps {

    PacketScript(JFrame p) {
        super(p);
        }

    protected void createMenus(JMenuBar menuBar, JFrame frame) {
        fileMenu(menuBar, frame);
        editMenu(menuBar, frame);
        toolsMenu(menuBar, frame);
        panelMenu(menuBar, frame);
        systemsMenu(menuBar, frame);
        debugMenu(menuBar, frame);
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("PacketScriptVersionCredit"),
                                new String[]{jmri.Version.name()});
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        SplashWindow sp = new SplashWindow();

        initLog4J();
        log.info("program starts");
        setConfigFilename("PacketScriptConfig.xml", args);
        JFrame f = new JFrame("PanelPro");
        createFrame(new PacketScript(f), f);

        log.info("main initialization done");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PacketScript.class.getName());
}


