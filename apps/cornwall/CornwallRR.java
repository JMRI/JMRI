/**
 * CornwallRR.java
 */

package apps.cornwall;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import apps.JmriDemo.JmriDemoConfigAction;

/**
 * The JMRI program for Nick Kulp's Cornwall Railroad.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author			Bob Jacobsen   Copyright 2003
 * @version         $Revision: 1.1 $
 */
public class CornwallRR extends JPanel {
    public CornwallRR() {

        super(true);

	// create basic GUI
        setLayout(new BorderLayout());
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

        // load preferences
        JmriDemoConfigAction prefs = null;
        if (configFile != null) {
            log.debug("configure from specified file");
    	    prefs = new JmriDemoConfigAction("Preferences...", configFile);
        } else {
            log.debug("configure from default file");
    	    prefs = new JmriDemoConfigAction("Preferences...");
        }

	// populate GUI

        // Create menu categories and add to the menu bar, add actions to menus
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new AbstractAction("Quit"){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        editMenu.add(prefs);

        // if the configuration completed OK, the input menu is made inactive
        log.debug("Find configOK is "+prefs.configOK);

        JMenu funcMenu = new JMenu("Tools");
        menuBar.add(funcMenu);
        funcMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction("Simple Programmer"));
        funcMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction("DecoderPro service programmer"));
        funcMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction("DecoderPro ops-mode programmer"));
        funcMenu.add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction("Turnout Control"));
        funcMenu.add(new jmri.jmrit.powerpanel.PowerPanelAction("Power Control"));
        funcMenu.add(new jmri.jmrit.speedometer.SpeedometerAction( "Speedometer" ));

        menuBar.add(new jmri.jmrit.roster.RosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, this));

        JMenu diagMenu = new JMenu("Panels");
        menuBar.add(diagMenu);
        diagMenu.add(new jmri.jmrit.display.PanelEditorAction( "New panel..." ));
        diagMenu.add(new jmri.configurexml.LoadXmlConfigAction("Load panels..."));
        diagMenu.add(new jmri.configurexml.StoreXmlConfigAction("Store panels..."));

        JMenu locoMenu = new JMenu("LocoNet");
        menuBar.add(locoMenu);
        locoMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor"));
        locoMenu.add(new jmri.jmrix.loconet.slotmon.SlotMonAction("Slot Monitor"));
        locoMenu.add(new jmri.jmrix.loconet.locogen.LocoGenAction("Send Packet"));
        locoMenu.add(new jmri.jmrix.loconet.locoio.LocoIOAction("LocoIO Programmer"));
        locoMenu.add(new jmri.jmrix.loconet.pm4.PM4Action("PM4 Programmer"));
        locoMenu.add(new jmri.jmrix.loconet.bdl16.BDL16Action("BDL16 Programmer"));
        locoMenu.add(new jmri.jmrit.messager.MessageFrameAction( "Throttle Messages" ));
        locoMenu.add(new jmri.jmrix.loconet.locormi.LnMessageServerAction( "Start LocoNet Server" ));

        JMenu throttleMenu = new JMenu("Throttles");
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction( "New Throttle..." ));
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottleAction( "Save Throttle Layout" ));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottleAction( "Load Throttle Layout" ));
        locoMenu.add(throttleMenu);

        JMenu cmriMenu = new JMenu("CMRI");
        menuBar.add(cmriMenu);
        cmriMenu.add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction("Command Monitor"));
        cmriMenu.add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction("Send Command"));

        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new jmri.jmrit.MemoryFrameAction("Memory usage monitor"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.XmlFileCheckAction("Check XML File", this));
        devMenu.add(new jmri.jmrit.decoderdefn.NameCheckAction("Check decoder names", this));
        devMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction("Check programmer names", this));
        devMenu.add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction("Create decoder index"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrix.loconet.locormi.LnMessageClientAction( "Start LocoNet Client" ));

        // Label & text
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource("resources/cornwall_logo.gif"),"Decoder Pro label"), JLabel.LEFT));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(" Cornwall Railroad "));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(" Connected via "+prefs.getCurrentProtocolName()));
        pane2.add(new JLabel(" on port "+prefs.getCurrentPortName()));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(" JRMI version "+jmri.Version.name()));
        pane2.add(new JLabel(" Java version "+System.getProperty("java.version","<unknown>")));
        pane1.add(pane2);
        add(pane1);
    }

    // Main entry point
    public static void main(String args[]) {

        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        log.info("program starts");

        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFile = args[0];
            log.debug("Config file was specified as: "+configFile);
        }

    	// create the demo frame and menus
        CornwallRR containedPane = new CornwallRR();
        JFrame frame = new JFrame("Cornwall main panel");
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
        log.info("main initialization done");

        // start automation
        new CrrSection().start();

    }

    static String configFile = null;

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CornwallRR.class.getName());
}


