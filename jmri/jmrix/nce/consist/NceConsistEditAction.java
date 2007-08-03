/** 
 * NceConsistEditAction.java
 *
 * Description:		Swing action to create and register a 
 *       			NceConsistEditFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @author			Daniel Boudreau Copyright (C) 2007
 * @version			
 */

package jmri.jmrix.nce.consist;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class NceConsistEditAction  extends AbstractAction {

	public NceConsistEditAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		NceConsistEditFrame f = new NceConsistEditFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceConsistEditAction.class.getName());
}


/* @(#)NceConsistEditAction.java */

