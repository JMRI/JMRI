// DecoderProConfigAction.java

/** 
 * 
 *
 * Description:		Swing action to create DecoderProConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: DecoderProgConfigAction.java,v 1.1.1.1 2001-12-02 05:51:21 jacobsen Exp $
 */

package jmri.apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class DecoderProConfigAction 			extends AbstractAction {

	public DecoderProConfigAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create a SimpleProgFrame
		DecoderProConfigFrame f = new DecoderProConfigFrame("Preferences");
		f.show();	
		
	}
}


/* @(#)eDcoderProConfigFrame.java */
