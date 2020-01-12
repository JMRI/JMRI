package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Export trains to a CSV file
 *
 * @author Dan Boudreau Copyright (C) 2015
 * 
 */
public class ExportTrainRosterAction extends AbstractAction {

    public ExportTrainRosterAction() {
        super(Bundle.getMessage("MenuItemExport"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        ExportTrains ex = new ExportTrains();
        ex.writeOperationsTrainsFile();
    }
}
