/** 
 * EasyDccMonAction.java
 *
 * Description:		Swing action to create and register a 
 *       			EasyDccMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: EasyDccMonAction.java,v 1.1 2002-03-23 07:28:30 jacobsen Exp $
 */

package jmri.jmrix.easydcc.easydccmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


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
