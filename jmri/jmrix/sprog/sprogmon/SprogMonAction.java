/** 
 * SprogMonAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SprogMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: SprogMonAction.java,v 1.1 2003-01-27 05:35:40 jacobsen Exp $
 */

package jmri.jmrix.sprog.sprogmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class SprogMonAction 			extends AbstractAction {

	public SprogMonAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a SprogMonFrame
		SprogMonFrame f = new SprogMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SprogMonAction starting SprogMonFrame: Exception: "+ex.toString());
			}
		f.show();	
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogMonAction.class.getName());

}


/* @(#)SprogMonAction.java */
