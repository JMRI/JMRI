package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the ImportCars thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class ImportCarRosterAction extends AbstractAction {

    public ImportCarRosterAction() {
        super(Bundle.getMessage("MenuItemImport"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportCars();
        mb.setName("Import Cars"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportCarRosterAction.class);
}
