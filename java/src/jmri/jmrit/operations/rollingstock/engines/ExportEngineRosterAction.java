// ExportEngineRosterAction.java
package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the ImportEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ExportEngineRosterAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 3996636827704137431L;

    public ExportEngineRosterAction(String actionName, Component frame) {
        super(actionName);
    }

    public void actionPerformed(ActionEvent ae) {
        ExportEngines ex = new ExportEngines();
        ex.writeOperationsEngineFile();
    }

    private final static Logger log = LoggerFactory
            .getLogger(ExportEngineRosterAction.class.getName());
}
