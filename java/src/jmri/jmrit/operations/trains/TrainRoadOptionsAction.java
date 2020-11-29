package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainRoadOptionsFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrainRoadOptionsAction extends AbstractAction {

    public TrainRoadOptionsAction(TrainEditFrame frame) {
        super(Bundle.getMessage("MenuItemRoadOptions"));
        _frame = frame;
    }

    TrainEditFrame _frame; // the parent frame that is launching the TrainEditBuildOptionsFrame.

    TrainRoadOptionsFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a train edit option frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new TrainRoadOptionsFrame();
        f.initComponents(_frame);
    }
}


