package jmri.jmrit.operations.routes;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RoutesTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class RoutesTableAction extends AbstractAction {

    public RoutesTableAction(String s) {
        super(s);
    }

    public RoutesTableAction() {
        this(Bundle.getMessage("MenuRoutes")); // NOI18N
    }

    private static RoutesTableFrame routesTableFrame = null;

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Show only one RouteTableFrame")
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
        if (routesTableFrame == null || !routesTableFrame.isVisible()) {
            routesTableFrame = new RoutesTableFrame();
        }
        routesTableFrame.setExtendedState(Frame.NORMAL);
        routesTableFrame.setVisible(true); // this also brings the frame into focus
    }
}


