/**
 * EasyDccPacketGenAction.java
 *
 * Description:		Swing action to create and register a
 *       			EasyDccPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: EasyDccPacketGenAction.java,v 1.1 2002-03-23 07:28:30 jacobsen Exp $
 */

package jmri.jmrix.easydcc.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class EasyDccPacketGenAction 			extends AbstractAction {

	public EasyDccPacketGenAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		EasyDccPacketGenFrame f = new EasyDccPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.show();
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccPacketGenAction.class.getName());
}


/* @(#)EasyDccPacketGenAction.java */
