//OptionAction.java

package jmri.jmrit.operations.trains;

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
	
	TrainEditFrame frame = null;

    public OptionAction(String s, TrainEditFrame frame) {
    	super(s);
    	this.frame = frame;
    }

    OptionFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
    	if (f == null || !f.isVisible()){
    		f = new OptionFrame();
    		f.initComponents(frame);
    	}
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OptionAction.class.getName());
}

/* @(#)OptionAction.java */
