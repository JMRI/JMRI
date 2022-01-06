package jmri.jmrit.operations.locations.tools;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Starts the Import Locations Thread
 *
 * @author J. Scott Walton Copyright (C) 2022
 */
public class ImportLocationsRosterAction extends AbstractAction {

    public ImportLocationsRosterAction() {
        super( Bundle.getMessage("TitleImportLocations"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Thread mb = new ImportLocations();
        mb.setName("Import Locations"); // NOI18N
        mb.start();
    }

}
