// ImportCarRosterAction.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the ImportCars thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ImportCarRosterAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8894977271081874448L;

    public ImportCarRosterAction(String actionName, Component frame) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new ImportCars();
        mb.setName("Import Cars"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(ImportCarRosterAction.class.getName());
}
