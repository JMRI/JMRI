package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.Location;

/**
 * Opens the location track blocking order window.
 *
 * @author Daniel Boudreau Copyright (C) 2015
 * 
 */
public class LocationTrackBlockingOrderAction extends AbstractAction {

    public LocationTrackBlockingOrderAction(Location location) {
        super(Bundle.getMessage("TitleTrackBlockingOrder"));
        _location = location;
    }

    Location _location;

    LocationTrackBlockingOrderFrame _frame = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (_frame == null || !_frame.isVisible()) {
            _frame = new LocationTrackBlockingOrderFrame();
            _frame.initComponents(_location);
        }
        _frame.setExtendedState(Frame.NORMAL);
        _frame.setVisible(true); // this also brings the frame into focus
    }
}


