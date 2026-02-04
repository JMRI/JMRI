package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.gui.TrackEditFrame;

/**
 * Action to launch selection of alternate track.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 */
public class AlternateTrackAction extends AbstractAction {

    private AlternateTrackFrame _atf;
    private TrackEditFrame _tef;

    public AlternateTrackAction(TrackEditFrame tef) {
        super(Bundle.getMessage("AlternateTrack"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_atf != null) {
            _atf.dispose();
        }
        _atf = new AlternateTrackFrame(_tef);
    }

}
