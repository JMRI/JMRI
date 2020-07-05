package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the ImportRosterEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class ImportRosterEngineAction extends AbstractAction {

    public ImportRosterEngineAction() {
        super(Bundle.getMessage("MenuItemImportRoster"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportRosterEngines();
        mb.setName("Import Roster Engines"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportRosterEngineAction.class);
}
