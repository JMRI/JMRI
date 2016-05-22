// SprogPacketGenAction.java

package jmri.jmrix.sprog.packetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			SprogPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
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
		f.setVisible(true);
	}
   static Logger log = LoggerFactory.getLogger(SprogPacketGenAction.class.getName());
}


/* @(#)SprogPacketGenAction.java */
