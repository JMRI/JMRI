package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

/**
 * Starts the ImportCars thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
public class ExportCarRosterAction extends AbstractAction {

    CarsTableFrame _carsTableFrame;

    public ExportCarRosterAction(String actionName, CarsTableFrame carsTableFrame) {
        super(actionName);
        _carsTableFrame = carsTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        ExportCars exportCars = new ExportCars(_carsTableFrame.carsTableModel.getSelectedCarList());
        exportCars.writeOperationsCarFile();
    }

//    private final static Logger log = LoggerFactory.getLogger(ExportCarRosterAction.class);
}
