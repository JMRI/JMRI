// XpaConfigureAction.java

package jmri.jmrix.xpa.xpaconfig;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			XpaConfigureFrame object
 *
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision: 1.1 $
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
		f.show();
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XpaConfigureAction.class.getName());
}


/* @(#)XpaPacketGenAction.java */
