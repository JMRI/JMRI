//AlternateTrackAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to launch selection of alternate track.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class AlternateTrackAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5179558203243699849L;
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
