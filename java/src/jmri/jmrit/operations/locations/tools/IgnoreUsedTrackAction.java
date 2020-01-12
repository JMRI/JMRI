package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

/**
 * Action to allow a user to define how much used track space is to be ignored
 * by the program when placing new rolling stock to a track.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 */
public class IgnoreUsedTrackAction extends AbstractAction {

    private TrackEditFrame _tef;
    private IgnoreUsedTrackFrame _iutf;

    public IgnoreUsedTrackAction(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemPlannedPickups"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_iutf != null) {
            _iutf.dispose();
        }
        _iutf = new IgnoreUsedTrackFrame(_tef);
    }
}
