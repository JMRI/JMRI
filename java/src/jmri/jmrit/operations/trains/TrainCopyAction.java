// TrainCopyAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainCopyFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class TrainCopyAction extends AbstractAction {

    public TrainCopyAction(String s) {
    	super(s);
    }
    
    String trainName;
    public TrainCopyAction(String s, String trainName) {
    	super(s);
    	this.trainName = trainName;
    }


    TrainCopyFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a copy train frame
    	if (f == null || !f.isVisible()){
    		f = new TrainCopyFrame();
    	}
    	if (trainName != null)
    		f.setTrainName(trainName);
    	f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrainCopyAction.java */
