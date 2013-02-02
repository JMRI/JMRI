// XpaConfigureAction.java

package jmri.jmrix.xpa.xpaconfig;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			XpaConfigureFrame object
 *
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision$
 */
public class XpaConfigureAction  extends AbstractAction {

	public XpaConfigureAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		XpaConfigureFrame f = new XpaConfigureFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
   static Logger log = Logger.getLogger(XpaConfigureAction.class.getName());
}


/* @(#)XpaPacketGenAction.java */
