package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.trains.TrainEditFrame;

/**
 * Swing action to load the train manifest options frame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class TrainManifestOptionAction extends AbstractAction {

    TrainEditFrame frame = null;

    public TrainManifestOptionAction(String s, TrainEditFrame frame) {
        super(s);
        this.frame = frame;
    }

    TrainManifestOptionFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (f == null || !f.isVisible()) {
            f = new TrainManifestOptionFrame();
            f.initComponents(frame);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }

//    private final static Logger log = LoggerFactory.getLogger(TrainManifestOptionAction.class);
}


