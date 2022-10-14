package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;

/**
 * Action to create the ShowTrainsServingLocationFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2014
 */
public class ShowTrainsServingLocationAction extends AbstractAction {

    public ShowTrainsServingLocationAction(Location location, Track track) {
        super(location == null ? Bundle.getMessage("TitleShowTrains")
                : track == null ? Bundle.getMessage("MenuItemShowTrainsLocation")
                        : Bundle.getMessage("MenuItemShowTrainsTrack"));
        _location = location;
        _track = track;
    }

    Location _location;
    Track _track;
    ShowTrainsServingLocationFrame _frame;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_frame != null) {
            _frame.dispose();
        }
        _frame = new ShowTrainsServingLocationFrame();
        _frame.initComponents(_location, _track);
    }
}
