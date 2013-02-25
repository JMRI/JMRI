// EasyDccPacketGenAction.java

package jmri.jmrix.easydcc.packetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			EasyDccPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision$
 */
public class EasyDccPacketGenAction 			extends AbstractAction {

	public EasyDccPacketGenAction(String s) { super(s);}

    public EasyDccPacketGenAction() {
        this("Generate EasyDCC message");
    }

    public void actionPerformed(ActionEvent e) {
		EasyDccPacketGenFrame f = new EasyDccPacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static Logger log = LoggerFactory.getLogger(EasyDccPacketGenAction.class.getName());
}


/* @(#)EasyDccPacketGenAction.java */
