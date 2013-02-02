//  Mx1MonAction.java

package jmri.jmrix.zimo.zimomon;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


/**
 * Swing action to create and register a
 *       		Mx1MonFrame object.
 *
 * @author		Bob Jacobsen    Copyright (C) 2002
 * @version             $Revision$
 *
 * Adapted by Sip Bosch for use with MX-1
 *
 */
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
		f.setVisible(true);

	}

	static Logger log = Logger.getLogger(Mx1MonAction.class.getName());

}


/* @(#)Mx1MonAction.java */
