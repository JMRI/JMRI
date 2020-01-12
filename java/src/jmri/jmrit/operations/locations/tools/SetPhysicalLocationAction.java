package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.Location;

/**
 * Swing action to create a SetPhysicalLocation dialog.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2011
 */
public class SetPhysicalLocationAction extends AbstractAction {

    Location _location;

    public SetPhysicalLocationAction(String s, Location location) {
        super(s);
        _location = location;
    }

    SetPhysicalLocationFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
        if (f == null || !f.isVisible()) {
            f = new SetPhysicalLocationFrame(_location);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }

    //private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationAction.class);
}


