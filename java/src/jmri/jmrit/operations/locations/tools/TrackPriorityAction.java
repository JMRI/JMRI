package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Track;

/**
 * Action to create a track priority window.
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class TrackPriorityAction extends AbstractAction {

    private TrackPriorityFrame _tpf;
    private Track _track;

    public TrackPriorityAction(Track track) {
        super(Bundle.getMessage("MenuItemTrackPriority"));
        _track = track;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_tpf != null) {
            _tpf.dispose();
        }
        _tpf = new TrackPriorityFrame(_track);
    }
}
