/** 
 * JMRIdemo.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.hexfile.LnHexFileAction;
import jmri.jmrix.loconet.ms100.MS100Action;
import jmri.jmrix.loconet.locomon.LocoMonAction;
import jmri.jmrix.loconet.locogen.LocoGenAction;
import jmri.jmrix.loconet.locoecho.LocoEchoAction;
import jmri.jmrix.loconet.slotmon.SlotMonAction;
import jmri.simpleprog.SimpleProgAction;
import jmri.symbolicprog.SymbolicProgAction;
import ErrLoggerJ. ErrLogFrameAction;

public class JMRIdemo extends JPanel {
	public JMRIdemo() {

        super(true);

	// create basic GUI
		setLayout(new BorderLayout());
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        
        // create text box for advice
        helpLabel1 = new JLabel();
		add(helpLabel1, BorderLayout.NORTH);     
		helpLabel1.setText("Pick an input source from the left menu,");
		
        helpLabel2 = new JLabel();
		add(helpLabel2, BorderLayout.SOUTH);     
		helpLabel2.setText("then one or more things from the right.");
	
	// create infrastructure objects for LocoNet
		LnTrafficController tc = new LnTrafficController();

	// create actions for user control
		hexfileAction  = new LnHexFileAction("Hex File");
		ms100Action    = new MS100Action("MS100");
		locomonAction  = new LocoMonAction("LocoNet Monitor");
		locogenAction  = new LocoGenAction("Layout Commands");
		locoechoAction = new LocoEchoAction("Turnout Control");
		slotmonAction  = new SlotMonAction("Slot Monitor");
		simpleprogAction  = new SimpleProgAction("Simple Programmer");
		symbolicprogAction  = new SymbolicProgAction("Symbolic Programmer");
		errlogAction  = new ErrLogFrameAction("error logging");
		
	// populate GUI
        // Create menu categories and add to the menu bar
        JMenu inputMenu = new JMenu("Input");
        menuBar.add(inputMenu);
        JMenu funcMenu = new JMenu("Functions");
        menuBar.add(funcMenu);
        JMenu debugMenu = new JMenu("Debugging");
        menuBar.add(debugMenu);
		// add actions to menus
        inputMenu.add(hexfileAction);
        inputMenu.add(ms100Action);
        funcMenu.add(simpleprogAction);
        funcMenu.add(locoechoAction);
        funcMenu.add(locomonAction);
        funcMenu.add(locogenAction);
        funcMenu.add(slotmonAction);
        funcMenu.add(symbolicprogAction);
        debugMenu.add(errlogAction);
	}

	// Main entry point
    public static void main(String s[]) {
        JMRIdemo containedPane = new JMRIdemo();
        JFrame frame = new JFrame("JMRI demo main panel");
        frame.addWindowListener(new BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
    }
	
	// data members
	
	private LnHexFileAction hexfileAction;
	private MS100Action ms100Action;

	private LocoMonAction locomonAction;
	private LocoGenAction locogenAction;
	private LocoEchoAction locoechoAction;
	private SlotMonAction slotmonAction;
	private SimpleProgAction simpleprogAction;
	private SymbolicProgAction symbolicprogAction;

	private ErrLogFrameAction errlogAction;

	private LnTrafficController tc;
	
	// GUI members
    private JMenuBar menuBar;
	private JLabel helpLabel1;	
	private JLabel helpLabel2;	
}

