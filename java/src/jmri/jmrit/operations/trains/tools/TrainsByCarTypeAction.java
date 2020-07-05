package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a TrainsByCarTypeFrame object.
 *
 * @author Daniel Boudreau Copyright (C) 2009
 */
@API(status = MAINTAINED)
public class TrainsByCarTypeAction extends AbstractAction {

    public TrainsByCarTypeAction() {
        super(Bundle.getMessage("TitleModifyTrains"));
    }

    TrainsByCarTypeFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (f == null || !f.isVisible()) {
            f = new TrainsByCarTypeFrame();
            f.initComponents("");
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


