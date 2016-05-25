// TrainConductorAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
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
    TrainConductorFrame f = null;

    public TrainConductorAction(String s, Train train) {
        super(s);
        this.train = train;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy train frame
        if (f == null || !f.isVisible()) {
            f = new TrainConductorFrame(train);
        } else {
            f.setExtendedState(Frame.NORMAL);
        }
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrainConductorAction.java */
