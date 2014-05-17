// SchedulesResetHitsAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to reset the all of the schedule item hit counts for all schedules
 * 
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */

public class SchedulesResetHitsAction extends AbstractAction {

	public SchedulesResetHitsAction(String s) {
		super(s);
	}

	public void actionPerformed(ActionEvent e) {
		ScheduleManager.instance().resetHitCounts();
	}
}
