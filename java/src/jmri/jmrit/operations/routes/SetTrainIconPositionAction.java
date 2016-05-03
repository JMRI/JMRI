// SetTrainIconPositionAction.java
package jmri.jmrit.operations.routes;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RouteCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class SetTrainIconPositionAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1305604823720347502L;

    public SetTrainIconPositionAction(String s) {
        super(s);
    }

    SetTrainIconPositionFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new SetTrainIconPositionFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)SetTrainIconPositionAction.java */
