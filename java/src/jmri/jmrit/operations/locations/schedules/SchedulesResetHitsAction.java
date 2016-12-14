// SchedulesResetHitsAction.java
package jmri.jmrit.operations.locations.schedules;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to reset the all of the schedule item hit counts for all schedules
 *
 * @author Daniel Boudreau Copyright (C) 2014
 */
public class SchedulesResetHitsAction extends AbstractAction {

    public SchedulesResetHitsAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScheduleManager.instance().resetHitCounts();
    }
}
