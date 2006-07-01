/**
 * NceMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			NceMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrix.nce.ncemon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class NceMonAction 			extends AbstractAction {

    public NceMonAction(String s) { super(s);}
    public NceMonAction() { this("NCE message monitor");}

    public void actionPerformed(ActionEvent e) {
		// create a NceMonFrame
		NceMonFrame f = new NceMonFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("NceMonAction starting NceMonFrame: Exception: "+ex.toString());
			}
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMonAction.class.getName());

}


/* @(#)NceMonAction.java */
