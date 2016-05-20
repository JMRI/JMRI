// SignallingAction.java

package jmri.jmrit.signalling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Swing action to create and register a 
 *       			SignallingFrame object
 *
 * @author	    Kevin Dickerson    Copyright (C) 2011
 * @version		$Revision$	
 */

public class SignallingAction extends AbstractAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

	public SignallingAction(String s) {
        super(s);
    }
    
    public SignallingAction() {
        super(rb.getString("SignallingPairs"));
    }

    public void setMast(jmri.SignalMast source, jmri.SignalMast dest){
        this.source = source;
        this.dest=dest;

    }

    jmri.SignalMast source = null;
    jmri.SignalMast dest = null;

    public void actionPerformed(ActionEvent e) {
		SignallingFrame f = new SignallingFrame();
		try {
			f.initComponents(source, dest);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			ex.printStackTrace();
			}
		f.setVisible(true);	
	}
   static Logger log = LoggerFactory.getLogger(SignallingAction.class.getName());
}


/* @(#)SignallingAction.java */
