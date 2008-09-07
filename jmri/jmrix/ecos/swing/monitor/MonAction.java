/**
 * MonAction.java
 *
 * Description:		Swing action to create and register a
 *       			MonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version
 */

package jmri.jmrix.ecos.swing.monitor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class MonAction 			extends AbstractAction {

    public MonAction(String s) { super(s);}
    public MonAction() { this("ECOS message monitor");}

    public void actionPerformed(ActionEvent e) {
		// create a NceMonFrame
		MonFrame f = new MonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("MonAction starting MonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MonAction.class.getName());

}


/* @(#)MonAction.java */
