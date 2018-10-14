package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.trains.TrainEditFrame;

/**
 * Swing action to create and register a TrainScriptFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class TrainScriptAction extends AbstractAction {

    public TrainScriptAction(String s, TrainEditFrame frame) {
        super(s);
        this.frame = frame;
    }

    TrainEditFrame frame; // the parent frame that is launching the TrainScriptFrame.

    TrainScriptFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new TrainScriptFrame();
        f.setLocation(frame.getLocation());
        f.initComponents(frame);
        f.setExtendedState(Frame.NORMAL);
    }
}


