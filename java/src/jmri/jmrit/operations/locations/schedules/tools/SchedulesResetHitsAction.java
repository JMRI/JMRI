package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;

/**
 * Action to reset the all of the schedule item hit counts for all schedules
 *
 * @author Daniel Boudreau Copyright (C) 2014
 */
public class SchedulesResetHitsAction extends AbstractAction {

    public SchedulesResetHitsAction() {
        super(Bundle.getMessage("MenuItemResetHits"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(ScheduleManager.class).resetHitCounts();
    }
}
