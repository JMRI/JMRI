/** 
 * LocoMonAppl.java
 *
 * Title:			LocoMon application
 * Description:		Stand-alone Java app for LocoMon
 * @author			Bob Jacobsen  Copyright 2001
 * @version			
 */

package LocoMonAppl;

import LocoMonAppl.LocoMonApplFrame;
import LocoMon.MS100Frame;
import javax.swing.*;

public class LocoMonAppl {
	public LocoMonAppl() {
		try {
			// For native Look and Feel, uncomment the following code.
			try {
				System.out.println(" getSystemLookAndFeelClassName(): "+UIManager.getSystemLookAndFeelClassName());
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				System.out.println("Finished OK, look elsewhere for problem");
			} 
			catch (Exception e) { 
				System.err.println("Error loading L&F: " + e);
			}

			// there are two options here, now controlled at compile/link time
		
			// option 1 - read from a file for development
			LocoMonApplFrame frame = new LocoMonApplFrame();
			
			// option 2 - run from a MS100 on a serial port
			//MS100Frame frame = new MS100Frame();

			// back to common code
			frame.initComponents();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Main entry point
	static public void main(String[] args) {
		new LocoMonAppl();
	}
	
}
