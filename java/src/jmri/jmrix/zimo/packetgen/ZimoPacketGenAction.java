// ZimoPacketGenAction.java

package jmri.jmrix.zimo.packetgen;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.zimo.Mx1TrafficController;

/**
 * Swing action to create and register a
 *       		MX-1 PacketGenFrame object.
 *
 * @author		Bob Jacobsen    Copyright (C) 2001, 2002
 * @version		$Revision$
 *
 * Adapted for use with Zimo MX-1 by Sip Bosch
 *
 */
public class ZimoPacketGenAction 			extends AbstractAction {

	public ZimoPacketGenAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a PacketGenFrame
		ZimoPacketGenFrame f = new ZimoPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);

		// connect to the TrafficController
		f.connect(Mx1TrafficController.instance());
	}
   static Logger log = Logger.getLogger(ZimoPacketGenAction.class.getName());
}


/* @(#)ZimoPacketGenAction.java */
