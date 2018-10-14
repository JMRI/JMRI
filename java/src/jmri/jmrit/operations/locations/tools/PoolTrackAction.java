package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

/**
 * Action to create a track pool and place a track in that pool.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 */
public class PoolTrackAction extends AbstractAction {

    private TrackEditFrame _tef;
    private PoolTrackFrame _ptf;

    public PoolTrackAction(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemPoolTrack"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_ptf != null) {
            _ptf.dispose();
        }
        _ptf = new PoolTrackFrame(_tef._track);
        _ptf.initComponents();
    }
}
