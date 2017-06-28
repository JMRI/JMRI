package jmri.jmrit.operations.locations;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action open the yardmaster by track frame.
 *
 * @author Daniel Boudreau Copyright (C) 2015
 * 
 */
public class YardmasterByTrackAction extends AbstractAction {

    public YardmasterByTrackAction(String s, Location location) {
        super(s);
        _location = location;
    }

    public YardmasterByTrackAction(String s) {
        super(s);
    }

    public YardmasterByTrackAction() {
        super(Bundle.getMessage("TitleYardmasterByTrack"));
    }
    
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


