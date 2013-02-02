// SignallingFrameAction.java

package jmri.jmrit.signalling;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Swing action to create and register a 
 *       			SignallingFrame object
 *
 * @author	    Kevin Dickerson Copyright (C) 2011
 * @version		$Revision$	
 */

public class SignallingFrameAction extends AbstractAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");
    
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
   static Logger log = Logger.getLogger(SignallingFrameAction.class.getName());
}


/* @(#)SignallingFrameAction.java */
