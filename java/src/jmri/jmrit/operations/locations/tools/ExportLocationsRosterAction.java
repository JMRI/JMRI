package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the export locations action
 *
 * @author Dan Boudreau Copyright (C) 2018
 */
@API(status = MAINTAINED)
public class ExportLocationsRosterAction extends AbstractAction {

    public ExportLocationsRosterAction() {
        super(Bundle.getMessage("TitleExportLocations"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        new ExportLocations().writeOperationsLocationFile();
    }
}
