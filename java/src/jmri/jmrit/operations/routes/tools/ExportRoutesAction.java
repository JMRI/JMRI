package jmri.jmrit.operations.routes.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Export Routes to a CSV file
 *
 * @author Dan Boudreau Copyright (C) 2019
 * 
 */
@API(status = MAINTAINED)
public class ExportRoutesAction extends AbstractAction {

    public ExportRoutesAction() {
        super(Bundle.getMessage("MenuItemExportRoutes"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        ExportRoutes ex = new ExportRoutes();
        ex.writeOperationsRoutesFile();
    }
}
