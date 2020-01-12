package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the ImportRosterEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
public class ImportRosterEngineAction extends AbstractAction {

    public ImportRosterEngineAction(String actionName) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportRosterEngines();
        mb.setName("Import Roster Engines"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportRosterEngineAction.class);
}
