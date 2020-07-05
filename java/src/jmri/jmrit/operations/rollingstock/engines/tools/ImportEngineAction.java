package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the ImportEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class ImportEngineAction extends AbstractAction {

    public ImportEngineAction() {
        super(Bundle.getMessage("MenuItemImport"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportEngines();
        mb.setName("Import Engines"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportEngineAction.class);
}
