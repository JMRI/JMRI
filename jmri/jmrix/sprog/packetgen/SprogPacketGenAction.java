/**
 * SprogPacketGenAction.java
 *
 * Description:		Swing action to create and register a
 *       			SprogPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SprogPacketGenAction.java,v 1.1 2003-01-27 05:35:40 jacobsen Exp $
 */

package jmri.jmrix.sprog.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

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
