package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;

/**
 * Action to create the ShowTrainsServingLocationFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2014
 * 
 */
public class ShowTrainsServingLocationAction extends AbstractAction {

    public ShowTrainsServingLocationAction(String title, Location location, Track track) {
        super(title);
        _location = location;
        _track = track;
        setEnabled(_location != null);
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
