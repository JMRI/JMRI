// TrainSetColorAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainSetColorFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class TrainSetColorAction extends AbstractAction {

	public TrainSetColorAction() {
		super(Bundle.getMessage("MenuItemSetTrainColor"));
	}

	Train _train = null;

	public TrainSetColorAction(String s, Train train) {
		super(s);
		_train = train;
	}

	TrainSetColorFrame f = null;

	public void actionPerformed(ActionEvent e) {
		if (f == null || !f.isVisible()) {
			f = new TrainSetColorFrame(_train);
		}
		f.setExtendedState(Frame.NORMAL);
		f.setVisible(true); // this also brings the frame into focus
	}
}

/* @(#)TrainSetColorAction.java */
