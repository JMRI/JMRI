// SerialPacketGenAction.java

package jmri.jmrix.powerline.swing.packetgen;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Swing action to create and register a
 *       			SerialPacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SerialPacketGenAction extends AbstractAction {

	public SerialPacketGenAction(String s, SerialTrafficController tc) {
		super(s);
		this.tc = tc;
	}

    public SerialPacketGenAction(SerialTrafficController tc) {
        this("Send powerline device message", tc);
        this.tc = tc;
    }
    
    SerialTrafficController tc = null;

    public void actionPerformed(ActionEvent e) {
		SerialPacketGenFrame f = new SerialPacketGenFrame(tc);
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
