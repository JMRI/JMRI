/** 
 * NcePacketGenAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NcePacketGenFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce.packetgen;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class NcePacketGenAction 			extends AbstractAction {

	public NcePacketGenAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		NcePacketGenFrame f = new NcePacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.show();	
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NcePacketGenAction.class.getName());
}


/* @(#)NcePacketGenAction.java */
