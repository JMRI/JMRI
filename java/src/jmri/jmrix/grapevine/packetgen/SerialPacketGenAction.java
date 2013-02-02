// SerialPacketGenAction.java

package jmri.jmrix.grapevine.packetgen;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SerialPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2007
 * @version	$Revision$
 */
public class SerialPacketGenAction extends AbstractAction {

	public SerialPacketGenAction(String s) { super(s);}

    public SerialPacketGenAction() {
        this("Send Grapevine message");
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
   static Logger log = Logger.getLogger(SerialPacketGenAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */
