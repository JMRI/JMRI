package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainLoadOptionsFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrainLoadOptionsAction extends AbstractAction {

    public TrainLoadOptionsAction(String s, TrainEditFrame frame) {
        super(s);
        this.frame = frame;
    }

    TrainEditFrame frame; // the parent frame that is launching the TrainEditBuildOptionsFrame.

    TrainLoadOptionsFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a train edit option frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new TrainLoadOptionsFrame();
        f.initComponents(frame);
    }
}


