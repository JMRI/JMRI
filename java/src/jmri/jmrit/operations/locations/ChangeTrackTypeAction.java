//ChangeTrackTypeAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to change the type of track. Track types are Spurs, Yards,
 * Interchanges and Staging.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class ChangeTrackTypeAction extends AbstractAction {

    private TrackEditFrame _tef;

    public ChangeTrackTypeAction(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemChangeTrackType"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ChangeTrackFrame(_tef);
    }

}
