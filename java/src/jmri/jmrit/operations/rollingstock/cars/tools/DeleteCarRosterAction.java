package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.cars.gui.CarsTableFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will delete all cars from the operation database, or delete all
 * cars at a location, or sitting on a track.
 *
 * @author Dan Boudreau Copyright (C) 2007, 2016, 2022
 */
public class DeleteCarRosterAction extends AbstractAction {

    CarsTableFrame _carsTableFrame;

    public DeleteCarRosterAction(CarsTableFrame carsTableFrame) {
        super(Bundle.getMessage("MenuItemDelete"));
        // delete all cars on a track or location
        if (carsTableFrame.carsTableModel.trackName != null) {
            String actionName = Bundle.getMessage("MenuDeleteCarsTrack",
                    carsTableFrame.carsTableModel.trackName);
            putValue(NAME, actionName);
        } else if (carsTableFrame.carsTableModel.locationName != null) {
            String actionName = Bundle.getMessage("MenuDeleteCarsLocation",
                    carsTableFrame.carsTableModel.locationName);
            putValue(NAME, actionName);
        }
        _carsTableFrame = carsTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (_carsTableFrame.carsTableModel.trackName == null && _carsTableFrame.carsTableModel.locationName == null) {
            if (JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("carSureDelete"),
                    Bundle.getMessage("carDeleteAll"), JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
                log.debug("removing all cars from roster");
                InstanceManager.getDefault(CarManager.class).deleteAll();
            }
        } else {
            // delete all cars on track or location
            String message = Bundle.getMessage("carDeleteCarsTrack",
                    _carsTableFrame.carsTableModel.trackName);
            if (_carsTableFrame.carsTableModel.trackName == null) {
                message = Bundle.getMessage("carDeleteCarsLocation",
                        _carsTableFrame.carsTableModel.locationName);
            }
            if (JmriJOptionPane.showConfirmDialog(null, message,
                    Bundle.getMessage("carDeleteAll"), JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
                for (Car car : _carsTableFrame.carsTableModel.getSelectedCarList()) {
                    InstanceManager.getDefault(CarManager.class).deregister(car);
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteCarRosterAction.class);
}
