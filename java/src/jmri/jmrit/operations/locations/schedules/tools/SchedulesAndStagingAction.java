package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action to load the schedules by car type and load frame.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 */
public class SchedulesAndStagingAction extends AbstractAction {

    private SchedulesAndStagingFrame _slf;

    public SchedulesAndStagingAction() {
        super(Bundle.getMessage("MenuItemStagingSchedules"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_slf != null) {
            _slf.dispose();
        }
        _slf = new SchedulesAndStagingFrame();
    }
}
