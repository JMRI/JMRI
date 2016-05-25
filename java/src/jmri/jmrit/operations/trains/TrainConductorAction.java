// TrainConductorAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainConductor frame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class TrainConductorAction extends AbstractAction {

    Train train;
    public TrainConductorAction(String s, Train train) {
    	super(s);
    	this.train = train;
    }

    TrainConductorFrame f = null;
    public void actionPerformed(ActionEvent e) {
    	// create a copy train frame
    	if (f == null || !f.isVisible()) {
    		f = new TrainConductorFrame();
    		f.initComponents(train);
    	} else {
    		f.setExtendedState(Frame.NORMAL);
    	   	f.setVisible(true);	// this also brings the frame into focus
    	}
    }
}

/* @(#)TrainConductorAction.java */
