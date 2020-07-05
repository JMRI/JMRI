package jmri.jmrit.operations.locations;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action open the yardmaster by track frame.
 *
 * @author Daniel Boudreau Copyright (C) 2015
 * 
 */
@API(status = MAINTAINED)
public class YardmasterByTrackAction extends AbstractAction {
    
    public YardmasterByTrackAction(Location location) {
        super(Bundle.getMessage("TitleYardmasterByTrack"));
        _location = location;
    }

    Location _location;
    YardmasterByTrackFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (f == null || !f.isVisible()) {
            f = new YardmasterByTrackFrame(_location);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


