/**
 * Mx1MonAction.java
 *
 * Description:		Swing action to create and register a
 *       		Mx1MonFrame object
 *
 * @author		Bob Jacobsen    Copyright (C) 2002
 * @version             $Revision: 1.1 $
 *
 * Adapted by Sip Bosch for use with MX-1
 *
 */

package jmri.jmrix.zimo.zimomon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.zimo.Mx1TrafficController;


public class Mx1MonAction 			extends AbstractAction {

	public Mx1MonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a Mx1MonFrame
		Mx1MonFrame f = new Mx1MonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("Mx1MonAction starting Mx1MonFrame: Exception: "+ex.toString());
			}
		f.show();

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Mx1MonAction.class.getName());

}


/* @(#)Mx1MonAction.java */
