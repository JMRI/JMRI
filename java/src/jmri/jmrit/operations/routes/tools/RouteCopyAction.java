package jmri.jmrit.operations.routes.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.routes.Route;

/**
 * Swing action to create and register a RouteCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class RouteCopyAction extends AbstractAction {

    public RouteCopyAction() {
        super(Bundle.getMessage("MenuItemCopy"));
    }
    
    public RouteCopyAction(Route route) {
        this();
        _route = route;
    }

    Route _route;
    RouteCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new RouteCopyFrame(_route);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}
