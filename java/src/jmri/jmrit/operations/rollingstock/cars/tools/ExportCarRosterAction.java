package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the ImportCars thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class ExportCarRosterAction extends AbstractAction {

    CarsTableFrame _carsTableFrame;

    public ExportCarRosterAction(CarsTableFrame carsTableFrame) {
        super(Bundle.getMessage("MenuItemExport"));
        _carsTableFrame = carsTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        ExportCars exportCars = new ExportCars(_carsTableFrame.carsTableModel.getSelectedCarList());
        exportCars.writeOperationsCarFile();
    }

//    private final static Logger log = LoggerFactory.getLogger(ExportCarRosterAction.class);
}
