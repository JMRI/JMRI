/**
 * JMRIdemo.java
 */

package apps.JmriDemo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Main program, intended to demonstrate the contents of the JMRI libraries.
 * Since it contains everything, it's also used for testing.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2002
 * @version     $Revision: 1.51 $
 */
public class JMRIdemo extends JPanel {
    public JMRIdemo(JFrame frame) {

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

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(frame));
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
        funcMenu.add(new jmri.jmrit.dualdecoder.DualDecoderToolAction());
        funcMenu.add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction("Turnout Control"));
        funcMenu.add(new jmri.jmrit.powerpanel.PowerPanelAction("Power Control"));
        funcMenu.add(new jmri.jmrit.speedometer.SpeedometerAction( "Speedometer" ));
        funcMenu.add(new jmri.jmrit.sendpacket.SendPacketAction( "Send DCC Packet" ));

        menuBar.add(new jmri.jmrit.roster.RosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, this));

        JMenu diagMenu = new JMenu("Panels");
        menuBar.add(diagMenu);
        diagMenu.add(new jmri.jmrit.display.PanelEditorAction( "New panel" ));
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
        locoMenu.add(new jmri.jmrix.loconet.se8.SE8Action("SE8c Programmer"));
        locoMenu.add(new jmri.jmrit.messager.MessageFrameAction( "Throttle Messages" ));
        locoMenu.add(new jmri.jmrix.loconet.locormi.LnMessageServerAction( "Start LocoNet Server" ));

        JMenu throttleMenu = new JMenu("Throttles");
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction( "Create New Throttle" ));
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottleAction( "Save Throttles" ));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottleAction( "Load Throttles" ));
        throttleMenu.add(new jmri.jmrit.throttle.EditThrottlePreferencesAction( "Edit Throttle Preferences" ));
        funcMenu.add(throttleMenu);

        JMenu nceMenu = new JMenu("NCE");
        menuBar.add(nceMenu);
        nceMenu.add(new jmri.jmrix.nce.ncemon.NceMonAction("Command Monitor"));
        nceMenu.add(new jmri.jmrix.nce.packetgen.NcePacketGenAction("Send Command"));
        nceMenu.add(new jmri.jmrix.ncemonitor.NcePacketMonitorAction("Track Packet Monitor"));

        JMenu easydccMenu = new JMenu("EasyDcc");
        menuBar.add(easydccMenu);
        easydccMenu.add(new jmri.jmrix.easydcc.easydccmon.EasyDccMonAction("Command Monitor"));
        easydccMenu.add(new jmri.jmrix.easydcc.packetgen.EasyDccPacketGenAction("Send Command"));

        JMenu lenzMenu = new JMenu("XpressNet");
        menuBar.add(lenzMenu);
        lenzMenu.add(new jmri.jmrix.lenz.mon.XNetMonAction("Command Monitor"));
        lenzMenu.add(new jmri.jmrix.lenz.packetgen.PacketGenAction("Send Command"));

        JMenu cmriMenu = new JMenu("CMRI");
        menuBar.add(cmriMenu);
        cmriMenu.add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction("Command Monitor"));
        cmriMenu.add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction("Send Command"));

        JMenu sprogMenu = new JMenu("SPROG");
        menuBar.add(sprogMenu);
        sprogMenu.add(new jmri.jmrix.sprog.sprogmon.SprogMonAction("Command Monitor"));
        sprogMenu.add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction("Send Command"));

        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new jmri.jmrit.MemoryFrameAction("Memory usage monitor"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.XmlFileCheckAction("Check XML File", this));
        devMenu.add(new jmri.jmrit.decoderdefn.NameCheckAction("Check decoder names", this));
        devMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction("Check programmer names", this));
        devMenu.add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction("Create decoder index"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.roster.RecreateRosterAction("Recreate roster"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrix.loconet.locormi.LnMessageClientAction( "Start LocoNet Client" ));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction("Auto-speed tool"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.automat.SampleAutomatonAction( "Sample automaton 1"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton2Action("Sample automaton 2"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton3Action("Sample automaton 3"));
        devMenu.add(new jmri.jmrit.automat.JythonAutomatonAction("Jython automaton"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));

        // Label & text
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource("resources/logo.gif"),"Decoder Pro label"), JLabel.LEFT));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(" JMRI Demo "+jmri.Version.name()+", part of the JMRI project "));
        pane2.add(new JLabel("   http://jmri.sf.net/ "));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(" Connected via "+prefs.getCurrentProtocolName()));
        pane2.add(new JLabel(" on port "+prefs.getCurrentPortName()));
        pane2.add(new JLabel(" "));
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
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        log.info("JMRIdemo starts");

        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFile = args[0];
            log.debug("Config file was specified as: "+configFile);
        }

    	// create the demo frame and menus
        JFrame frame = new JFrame("JMRI demo main panel");
        JMRIdemo containedPane = new JMRIdemo(frame);
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
        log.info("JMRIdemo main initialization done");
    }

    static String configFile = null;

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JMRIdemo.class.getName());
}


