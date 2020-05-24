package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Track;

/**
 * Action to create the TrackDestinationEditFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrackDestinationEditAction extends AbstractAction {

    private Track _track;
    private TrackDestinationEditFrame tdef = null;

    public TrackDestinationEditAction(Track track) {
        super(Bundle.getMessage("MenuItemDestinations"));
        _track = track;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (tdef != null) {
            tdef.dispose();
        }
        tdef = new TrackDestinationEditFrame();
        tdef.initComponents(_track);
    }
}
