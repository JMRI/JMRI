package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to create the train lineups in a CSV file.
 *
 * @author Dan Boudreau Copyright (C) 2020
 * 
 */
@API(status = MAINTAINED)
public class ExportTrainLineupsAction extends AbstractAction {

    public ExportTrainLineupsAction() {
        super(Bundle.getMessage("MenuItemExportLineup"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        new ExportTrainLineups().writeOperationsTrainsFile();
    }
}
