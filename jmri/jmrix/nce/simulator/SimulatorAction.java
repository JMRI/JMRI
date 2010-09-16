// SimulatorAction.java

package jmri.jmrix.nce.simulator;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a SimulatorFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
 
@Deprecated
public class SimulatorAction extends AbstractAction  {

	public SimulatorAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a NCE SimulatorFrame
    	SimulatorFrame f = new SimulatorFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting SimulatorFrame caught exception: "+ex.toString());
			}
		f.setVisible(true);
	}

   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimulatorAction.class.getName());
}

/* @(#)SimulatorAction.java */
