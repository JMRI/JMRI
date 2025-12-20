package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.gui.LocationEditFrame;

/**
 * Action to change all of tracks at a location to the same type of track. Track
 * types are Spurs, Yards, Interchanges and Staging.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * 
 */
public class ChangeTracksTypeAction extends AbstractAction {

    private LocationEditFrame _lef;
    private ChangeTracksFrame _ctf;

    public ChangeTracksTypeAction(LocationEditFrame lef) {
        super(Bundle.getMessage("MenuItemChangeTrackType"));
        _lef = lef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_ctf != null) {
            _ctf.dispose();
        }
        _ctf = new ChangeTracksFrame(_lef);
    }

}
