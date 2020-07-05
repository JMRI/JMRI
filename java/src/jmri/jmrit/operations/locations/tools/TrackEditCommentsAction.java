package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to launch edit of track comments.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 */
@API(status = MAINTAINED)
public class TrackEditCommentsAction extends AbstractAction {

    private TrackEditFrame _tef;

    public TrackEditCommentsAction(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemComments"));
        _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new TrackEditCommentsFrame(_tef._track);
    }
}
