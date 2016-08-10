// TrackCopyAction.java
package jmri.jmrit.operations.locations;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrackCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class TrackCopyAction extends AbstractAction {

    public TrackCopyAction(LocationEditFrame lef) {
        super(Bundle.getMessage("MenuItemCopyTrack"));
        _lef = lef;
    }

    private LocationEditFrame _lef;
    TrackCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
        if (f == null || !f.isVisible()) {
            f = new TrackCopyFrame(_lef);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrackCopyAction.java */
