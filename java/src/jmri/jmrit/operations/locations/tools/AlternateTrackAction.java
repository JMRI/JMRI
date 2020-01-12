package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

/**
 * Action to launch selection of alternate track.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 */
public class AlternateTrackAction extends AbstractAction {

    private TrackEditFrame _tef;

    public AlternateTrackAction(TrackEditFrame tef) {
        super(Bundle.getMessage("AlternateTrack"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new AlternateTrackFrame(_tef);
    }

}
