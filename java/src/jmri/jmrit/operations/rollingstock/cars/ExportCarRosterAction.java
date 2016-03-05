// ExportCarRosterAction.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Starts the ImportCars thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ExportCarRosterAction extends AbstractAction {

    CarsTableFrame _carsTableFrame;

    public ExportCarRosterAction(String actionName, CarsTableFrame carsTableFrame) {
        super(actionName);
        _carsTableFrame = carsTableFrame;
    }

    public void actionPerformed(ActionEvent ae) {
        ExportCars exportCars = new ExportCars(_carsTableFrame.carsTableModel.getSelectedCarList());
        exportCars.writeOperationsCarFile();
    }

//    private final static Logger log = LoggerFactory.getLogger(ExportCarRosterAction.class.getName());
}
