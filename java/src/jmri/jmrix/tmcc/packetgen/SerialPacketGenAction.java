/**
 * SerialPacketGenAction.java
 *
 * Description:		Swing action to create and register a
 *       			SerialPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrix.tmcc.packetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SerialPacketGenAction 			extends AbstractAction {

	public SerialPacketGenAction(String s) { super(s);}

    public SerialPacketGenAction() {
        this("Send TMCC message");
    }

    public void actionPerformed(ActionEvent e) {
		SerialPacketGenFrame f = new SerialPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */
