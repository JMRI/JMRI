package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.Schedule;

/**
 * Swing action to create and register a ScheduleCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2015, 2025
 */
public class ScheduleCopyAction extends AbstractAction {

    public ScheduleCopyAction() {
        super(Bundle.getMessage("MenuItemCopySchedule"));
    }
    
    Schedule _schedule = null;
    Track _track = null;
    
    public ScheduleCopyAction(Schedule schedule, Track track) {
        this();
        _schedule = schedule;
        _track = track;
    }

    ScheduleCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            f = new ScheduleCopyFrame(_schedule, _track);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


