package jmri.jmrit.operations.routes.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.routes.Route;

/**
 * Swing action to create and register a SetTrainIconRouteFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class SetTrainIconRouteAction extends AbstractAction {

    Route _route;

    public SetTrainIconRouteAction(Route route) {
        super(Bundle.getMessage("MenuSetTrainIconRoute"));
        _route = route;
        setEnabled(route != null);
    }

    SetTrainIconRouteFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            f = new SetTrainIconRouteFrame(_route);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}
