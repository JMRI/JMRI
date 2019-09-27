package jmri.jmrit.operations.routes;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RouteCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class RouteCopyAction extends AbstractAction {

    public RouteCopyAction(String s) {
        super(s);
    }

    Route route;

    public RouteCopyAction(String s, Route route) {
        super(s);
        this.route = route;
    }

    RouteCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new RouteCopyFrame(route);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}
