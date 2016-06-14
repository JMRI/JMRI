// SetTrainIconRouteAction.java
package jmri.jmrit.operations.routes;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SetTrainIconRouteFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class SetTrainIconRouteAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -9152117316952301773L;

    public SetTrainIconRouteAction(String s) {
        super(s);
    }

    String routeName;

    public SetTrainIconRouteAction(String s, String routeName) {
        super(s);
        this.routeName = routeName;
    }

    SetTrainIconRouteFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new SetTrainIconRouteFrame(routeName);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)SetTrainIconRouteAction.java */
