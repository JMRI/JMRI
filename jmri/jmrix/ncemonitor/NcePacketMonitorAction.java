// NcePacketMonitorAction.java

package jmri.jmrix.ncemonitor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NcePacketMonitorFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class NcePacketMonitorAction extends AbstractAction  {

	public NcePacketMonitorAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a SerialDriverFrame
		NcePacketMonitorFrame f = new NcePacketMonitorFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting NcePacketMonitorFrame caught exception: "+ex.toString());
			}
		f.show();
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NcePacketMonitorAction.class.getName());

}


/* @(#)SerialDriverAction.java */
