//OptionAction.java

package jmri.jmrit.operations.setup;

import org.apache.log4j.Logger;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Swing action to load the options frame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class OptionAction extends AbstractAction {

    public OptionAction(String s) {
    	super(s);
    }

    OptionFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new OptionFrame();
    		f.initComponents();
    	}
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	
    }
    
	static Logger log = org.apache.log4j.Logger
	.getLogger(OptionAction.class.getName());
}

/* @(#)OptionAction.java */
