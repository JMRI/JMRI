// EasyDccMonAction.java

package jmri.jmrix.easydcc.easydccmon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Swing action to create and register a
 *       			EasyDccMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class EasyDccMonAction 			extends AbstractAction {

	public EasyDccMonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
		// create a EasyDccMonFrame
		EasyDccMonFrame f = new EasyDccMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("EasyDccMonAction starting EasyDccMonFrame: Exception: "+ex.toString());
			}
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccMonAction.class.getName());

}


/* @(#)EasyDccMonAction.java */
