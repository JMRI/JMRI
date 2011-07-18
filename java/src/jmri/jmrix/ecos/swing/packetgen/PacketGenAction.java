// PacketGenAction.java

package jmri.jmrix.ecos.swing.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a 
 *       			PacketGenFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001, 2008
 * @version		$Revision$	
 * @deprecated 2.11.3
 */
@Deprecated
public class PacketGenAction extends AbstractAction {

	public PacketGenAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		PacketGenFrame f = new PacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PacketGenAction.class.getName());
}


/* @(#)PacketGenAction.java */
