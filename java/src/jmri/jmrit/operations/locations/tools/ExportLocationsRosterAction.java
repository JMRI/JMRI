package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the export locations action
 *
 * @author Dan Boudreau Copyright (C) 2018
 */
public class ExportLocationsRosterAction extends AbstractAction {

    public ExportLocationsRosterAction(String actionName) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        new ExportLocations().writeOperationsLocationFile();
    }
}
