package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.LocationEditFrame;

/**
 * Swing action to create and register a TrackCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 */
public class TrackCopyAction extends AbstractAction {

    public TrackCopyAction() {
        super(Bundle.getMessage("MenuItemCopyTrack"));
    }
    
    public TrackCopyAction(LocationEditFrame lef) {
        super(Bundle.getMessage("MenuItemCopyTrack"));
        _lef = lef;
    }

    private LocationEditFrame _lef = null;
    TrackCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
        if (f == null || !f.isVisible()) {
            f = new TrackCopyFrame(_lef);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


