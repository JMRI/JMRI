package jmri.jmrit.operations.routes.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Starts the Import Routes Thread
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ImportRoutesAction extends AbstractAction {

    public ImportRoutesAction() {
        super(Bundle.getMessage("TitleImportRoutes"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Thread mb = new ImportRoutes();
        mb.setName("Import Routes"); // NOI18N
        mb.start();
    }

}
