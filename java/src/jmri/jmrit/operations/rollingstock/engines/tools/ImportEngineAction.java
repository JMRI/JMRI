package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the ImportEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
public class ImportEngineAction extends AbstractAction {

    public ImportEngineAction(String actionName) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportEngines();
        mb.setName("Import Engines"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportEngineAction.class);
}
