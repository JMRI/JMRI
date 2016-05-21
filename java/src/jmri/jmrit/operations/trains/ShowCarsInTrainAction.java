// ShowCarsInTrainAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a ShowCarsInTrainFrame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */
public class ShowCarsInTrainAction extends AbstractAction {

    Train train;
    public ShowCarsInTrainAction(String s, Train train) {
    	super(s);
    	this.train = train;
    }

    ShowCarsInTrainFrame f = null;
    public void actionPerformed(ActionEvent e) {
    	// create a copy train frame
    	if (f == null || !f.isVisible()) {
    		f = new ShowCarsInTrainFrame();
    		f.initComponents(train);
    	} else {
    		f.setExtendedState(Frame.NORMAL);
    	   	f.setVisible(true);	// this also brings the frame into focus
    	}
    }
}

/* @(#)ShowCarsInTrainAction.java */
