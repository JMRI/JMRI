// SprogPacketGenAction.java

package jmri.jmrix.sprog.packetgen;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class SprogPacketGenAction 			extends AbstractAction {

	public SprogPacketGenAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		SprogPacketGenFrame f = new SprogPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.show();
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogPacketGenAction.class.getName());
}


/* @(#)SprogPacketGenAction.java */
