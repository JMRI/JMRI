package jmri.jmrit.operations.locations.schedules;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the export schedules action
 *
 * @author Dan Boudreau Copyright (C) 2018
 */
public class ExportSchedulesAction extends AbstractAction {

    public ExportSchedulesAction(String actionName) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        new ExportSchedules().writeOperationsScheduleFile();
    }
}
