// ExportCarRosterAction.java
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
public class ExportCarRosterAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 4463124347780716468L;

    public ExportCarRosterAction(String actionName, Component frame) {
        super(actionName);
    }

    public void actionPerformed(ActionEvent ae) {
        ExportCars ex = new ExportCars();
        ex.writeOperationsCarFile();
    }

//    private final static Logger log = LoggerFactory.getLogger(ExportCarRosterAction.class.getName());
}
