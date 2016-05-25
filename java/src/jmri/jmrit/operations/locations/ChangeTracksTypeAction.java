//ChangeTracksTypeAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to change all of tracks at a location to the same type of track. Track
 * types are Spurs, Yards, Interchanges and Staging.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 18559 $
 */
public class ChangeTracksTypeAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -4301236628809396753L;
    private LocationEditFrame _lef;

    public ChangeTracksTypeAction(LocationEditFrame lef) {
        super(Bundle.getMessage("MenuItemChangeTrackType"));
        _lef = lef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ChangeTracksFrame(_lef);
    }

}
