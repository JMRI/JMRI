// MonitorAction.java

package jmri.jmrix.openlcb.swing.monitor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			MonitorFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2009, 2010
 * @version			$Revision$
 */
public class MonitorAction 			extends AbstractAction {

	public MonitorAction(String s) { super(s);}

    public MonitorAction() {
        this("OpenLCB monitor");
    }

    public void actionPerformed(ActionEvent e) {
		// create a SerialMonFrame
		MonitorFrame f = new MonitorFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("MonitorAction starting MonitorFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorAction.class.getName());

}


/* @(#)MonitorAction.java */
