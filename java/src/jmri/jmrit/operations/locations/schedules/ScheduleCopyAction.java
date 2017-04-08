package jmri.jmrit.operations.locations.schedules;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a ScheduleCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2015
 */
public class ScheduleCopyAction extends AbstractAction {

    public ScheduleCopyAction() {
        super(Bundle.getMessage("MenuItemCopySchedule"));
    }
    
    Schedule schedule = null;
    
    public ScheduleCopyAction(Schedule schedule) {
        this();
        this.schedule = schedule;
    }

    ScheduleCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
        if (f == null || !f.isVisible()) {
            f = new ScheduleCopyFrame(schedule);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


