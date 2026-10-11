package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.gui.TrackEditFrame;

/**
 * Action to change the type of track. Track types are Spurs, Yards,
 * Interchanges and Staging.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class ChangeTrackTypeAction extends AbstractAction {

    private TrackEditFrame _tef;
    private ChangeTrackFrame _ctf;

    public ChangeTrackTypeAction(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemChangeTrackType"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_ctf != null) {
            _ctf.dispose();
        }
        _ctf = new ChangeTrackFrame(_tef);
    }

}
