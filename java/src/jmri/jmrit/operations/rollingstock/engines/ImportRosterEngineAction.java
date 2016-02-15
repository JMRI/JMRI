// ImportEngineRosterAction.java
package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the ImportRosterEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ImportRosterEngineAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -2868823519457819338L;

    public ImportRosterEngineAction(String actionName, Component frame) {
        super(actionName);
    }

    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportRosterEngines();
        mb.setName("Import Roster Engines"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportRosterEngineAction.class.getName());
}
