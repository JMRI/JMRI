// ScheduleResetHitsAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to reset the all of the schedule item hit counts for this schedule
 *
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class ScheduleResetHitsAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 8633992345767664436L;
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
