// SignallingFrameAction.java

package jmri.jmrit.signalling;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Swing action to create and register a 
 *       			SignallingFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2008
 * @version		$Revision: 1.1 $	
 */

public class SignallingFrameAction extends AbstractAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.signallingBundle");
    
	public SignallingFrameAction(String s) {
        super(s);
    }
    
    public SignallingFrameAction() {
        super(rb.getString("SignallingPairs"));
    }


    public void actionPerformed(ActionEvent e) {
		SignallingFrame f = new SignallingFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			ex.printStackTrace();
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignallingFrameAction.class.getName());
}


/* @(#)SignallingFrameAction.java */
