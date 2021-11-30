package jmri.jmrit.operations.routes.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.routes.Route;

/**
 * Swing action to create RouteBlockingOrderEditFrame
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class RouteBlockingOrderEditFrameAction extends AbstractAction {

    public RouteBlockingOrderEditFrameAction() {
        super(Bundle.getMessage("MenuBlockingOrder"));
    }
    
    public RouteBlockingOrderEditFrameAction(Route route) {
        this();
        _route = route;
    }

    Route _route;
    RouteBlockingOrderEditFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new RouteBlockingOrderEditFrame(_route);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}
