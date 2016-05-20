// AcelaPacketGenAction.java

package jmri.jmrix.acela.packetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an AcelaPacketGenFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public class AcelaPacketGenAction extends AbstractAction {

	public AcelaPacketGenAction(String s) { super(s);}

	public AcelaPacketGenAction() {
        	this("Generate Acela message");
	}

	public void actionPerformed(ActionEvent e) {
		AcelaPacketGenFrame f = new AcelaPacketGenFrame();
		try {
			f.initComponents();
		} catch (Exception ex) {
			log.error("Exception: "+ex.toString());
		}
		f.setVisible(true);
	}

	static Logger log = LoggerFactory.getLogger(AcelaPacketGenAction.class.getName());
}

/* @(#)AcelaPacketGenAction.java */
