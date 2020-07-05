package jmri.jmrit.operations.locations.schedules;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to reset the all of the schedule item hit counts for this schedule
 *
 * @author Daniel Boudreau Copyright (C) 2014
 */
@API(status = MAINTAINED)
public class ScheduleResetHitsAction extends AbstractAction {

    Schedule _schedule;

    public ScheduleResetHitsAction(Schedule schedule) {
        super(Bundle.getMessage("MenuItemResetHits"));
        _schedule = schedule;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _schedule.resetHitCounts();
    }
}
