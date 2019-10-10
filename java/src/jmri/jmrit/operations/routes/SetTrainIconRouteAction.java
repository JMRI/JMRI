package jmri.jmrit.operations.routes;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SetTrainIconRouteFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class SetTrainIconRouteAction extends AbstractAction {

    public SetTrainIconRouteAction(String s) {
        super(s);
    }

    Route route;

    public SetTrainIconRouteAction(String s, Route route) {
        super(s);
        this.route = route;
        setEnabled(route != null);
    }

    SetTrainIconRouteFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            f = new SetTrainIconRouteFrame(route);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}
