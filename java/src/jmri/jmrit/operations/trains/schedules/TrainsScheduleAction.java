package jmri.jmrit.operations.trains.schedules;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainsScheduleTableFrame object.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class TrainsScheduleAction extends AbstractAction {
    
    public TrainsScheduleAction() {
        super(Bundle.getMessage("TitleScheduleTrains"));
    }

    public TrainsScheduleAction(String s) {
        super(s);
    }

    TrainsScheduleTableFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (f == null || !f.isVisible()) {
            f = new TrainsScheduleTableFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


