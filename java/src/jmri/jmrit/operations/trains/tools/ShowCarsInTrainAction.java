package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.trains.Train;

/**
 * Swing action to create and register a ShowCarsInTrainFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2012
 */
public class ShowCarsInTrainAction extends AbstractAction {

    Train _train;

    public ShowCarsInTrainAction(String s, Train train) {
        super(s);
        this._train = train;
    }

    ShowCarsInTrainFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            f = new ShowCarsInTrainFrame();
            f.initComponents(_train);
        } else {
            f.setExtendedState(Frame.NORMAL);
            f.setVisible(true); // this also brings the frame into focus
        }
    }
}


