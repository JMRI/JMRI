//TrackEditCommentsAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to launch edit of track comments.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 17977 $
 */
public class TrackEditCommentsAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5780179267092341140L;
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
