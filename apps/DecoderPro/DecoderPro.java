// DecoderPro.java

package jmri.apps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import BasicWindowMonitor;

/** 
 * DecoderPro application. 
 *
 * @author			Bob Jacobsen
 * @version			$Id: DecoderPro.java,v 1.5 2002-01-12 22:26:00 jacobsen Exp $
 */
public class DecoderPro extends JPanel {
	public DecoderPro() {

        super(true);
	
	// create basic GUI
		setLayout(new BorderLayout());
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
    
    // load preferences
    	jmri.apps.DecoderProConfigAction prefs 
    				= new jmri.apps.DecoderProConfigAction("Preferences...");
        
	// populate GUI
			
        // Create menu categories and add to the menu bar, add actions to menus
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
	  		fileMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction("New Programmer..."));
        	fileMenu.add(new JSeparator());        	
        	fileMenu.add(new AbstractAction("Quit"){
    				public void actionPerformed(ActionEvent e) {
    					System.exit(0);
    				}
        		});

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
	        editMenu.add(prefs);

        JMenu toolMenu = new JMenu("Tools");
        menuBar.add(toolMenu);
	        toolMenu.add(new jmri.jmrit.XmlFileCheckAction("Check XML File", this));
	        toolMenu.add(new jmri.jmrit.NameCheckAction("Check decoder names", this));
	        toolMenu.add(new jmri.jmrit.tabbedframe.ProgCheckAction("Check programmer names", this));

        JMenu debugMenu = new JMenu("Debug");
        menuBar.add(debugMenu);
	        debugMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor"));
	        debugMenu.add(new jmri.jmrix.nce.ncemon.NceMonAction("Nce Command Monitor"));
	        debugMenu.add(new jmri.jmrit.MemoryFrameAction("Memory usage monitor"));

	}

	// Main entry point
    public static void main(String s[]) {
    
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

		log.info("DecoderPro starts");
		    		
    	// create the demo frame and menus
        DecoderPro containedPane = new DecoderPro();
        JFrame frame = new JFrame("DecoderPro");
        frame.addWindowListener(new BasicWindowMonitor());
        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        frame.pack();
        frame.setVisible(true);
    }
	
	// GUI members
    private JMenuBar menuBar;	
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderPro.class.getName());
}

