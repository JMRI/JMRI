package jmri.jmrit.operations.locations.schedules;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the export schedules action
 *
 * @author Dan Boudreau Copyright (C) 2018
 */
@API(status = MAINTAINED)
public class ExportSchedulesAction extends AbstractAction {

    public ExportSchedulesAction() {
        super(Bundle.getMessage("MenuItemExportSchedules"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        new ExportSchedules().writeOperationsScheduleFile();
    }
}
