/**
 * LocoTools.java
 */

package apps.LocoTools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Main program for a collection of LocoNet tools.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */
public class LocoTools extends JPanel {
	public LocoTools() {

        super(true);

	// create basic GUI
		setLayout(new BorderLayout());
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

    // load preferences
        LocoToolsConfigAction prefs = null;
        if (configFile != null) {
            log.debug("configure from specified file");
    	    prefs = new LocoToolsConfigAction("Preferences...", configFile);
        } else {
            log.debug("configure from default file");
    	    prefs = new LocoToolsConfigAction("Preferences...");
        }

	// populate GUI
        // create text box for advice
        JLabel helpLabel1 = new JLabel();
		add(helpLabel1, BorderLayout.NORTH);
		helpLabel1.setText("Pick an input source from the input menu first");

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

        JMenu funcMenu = new JMenu("Tools");
        menuBar.add(funcMenu);
	        funcMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction("Simple Programmer"));
	        funcMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction("Decoder Pro programmer"));
	        funcMenu.add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction("Turnout Control"));
	        funcMenu.add(new jmri.jmrit.powerpanel.PowerPanelAction("Power Control"));

        menuBar.add(new jmri.jmrit.roster.RosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, this));

        JMenu locoMenu = new JMenu("LocoNet");
        menuBar.add(locoMenu);
	        locoMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor"));
    	    locoMenu.add(new jmri.jmrix.loconet.slotmon.SlotMonAction("Slot Monitor"));
        	locoMenu.add(new jmri.jmrix.loconet.locogen.LocoGenAction("Send Packet"));
            locoMenu.add(locoio = new jmri.jmrix.loconet.locoio.LocoIOAction("LocoIO programmer"));

        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
          	devMenu.add(new jmri.jmrix.loconet.locormi.LnMessageServerAction( "Start LocoNet Server" ));
          	devMenu.add(new jmri.jmrix.loconet.locormi.LnMessageClientAction( "Start LocoNet Client" ));
        	devMenu.add(new JSeparator());
          	devMenu.add(new jmri.jmrit.display.PanelEditorAction( "PanelEditor" ));
	        devMenu.add(new jmri.configurexml.LoadXmlConfigAction("Load config"));
	        devMenu.add(new jmri.configurexml.StoreXmlConfigAction("Store config"));
        	devMenu.add(new JSeparator());
	        devMenu.add(new jmri.jmrit.MemoryFrameAction("Memory usage monitor"));
        	devMenu.add(new JSeparator());
	        devMenu.add(new jmri.jmrit.XmlFileCheckAction("Check XML File", this));
	        devMenu.add(new jmri.jmrit.decoderdefn.NameCheckAction("Check decoder names", this));
	        devMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction("Check programmer names", this));
	        devMenu.add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction("Create decoder index"));

		// Label & text
		JPanel pane1 = new JPanel();
			pane1.setLayout(new FlowLayout());
			pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource("resources/logo.gif"),"Decoder Pro label"), JLabel.LEFT));
			JPanel pane2 = new JPanel();
				pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
				pane2.add(new JLabel(" LocoTools "+jmri.Version.name()+", part of the JRMI project "));
				pane2.add(new JLabel("   http://jmri.sf.net/LocoTools "));
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
	   	 		org.apache.log4j.PropertyConfigurator.configure("default.lcf");
	    	} else {
		    	org.apache.log4j.BasicConfigurator.configure();
	    	}
	    }
		catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

		log.info("LocoTools starts");

        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFile = args[0];
            log.debug("Config file was specified as: "+configFile);
        }

    	// create the demo frame and menus
        LocoTools containedPane = new LocoTools();
        JFrame frame = new JFrame("LocoTools main panel");
        frame.addWindowListener(new jmri.util.oreilly.BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
		log.info("LocoTools main initialization done");

		// for debugging, start the LocoIO programmer always
		//locoio.actionPerformed(null);
    }

	static Action locoio = null;

    static String configFile = null;

	// GUI members
    private JMenuBar menuBar;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoTools.class.getName());
}

