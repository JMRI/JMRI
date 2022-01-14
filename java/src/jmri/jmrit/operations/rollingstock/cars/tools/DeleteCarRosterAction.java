package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

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
            String actionName = MessageFormat.format(Bundle.getMessage("MenuDeleteCarsTrack"),
                    new Object[] { carsTableFrame.carsTableModel.trackName });
            putValue(NAME, actionName);
        } else if (carsTableFrame.carsTableModel.locationName != null) {
            String actionName = MessageFormat.format(Bundle.getMessage("MenuDeleteCarsLocation"),
                    new Object[] { carsTableFrame.carsTableModel.locationName });
            putValue(NAME, actionName);
        }
        _carsTableFrame = carsTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (_carsTableFrame.carsTableModel.trackName == null && _carsTableFrame.carsTableModel.locationName == null) {
            if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("carSureDelete"),
                    Bundle.getMessage("carDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                log.debug("removing all cars from roster");
                InstanceManager.getDefault(CarManager.class).deleteAll();
            }
        } else {
            // delete all cars on track or location
            String message = MessageFormat.format(Bundle.getMessage("carDeleteCarsTrack"),
                    new Object[] { _carsTableFrame.carsTableModel.trackName });
            if (_carsTableFrame.carsTableModel.trackName == null) {
                message = MessageFormat.format(Bundle.getMessage("carDeleteCarsLocation"),
                        new Object[] { _carsTableFrame.carsTableModel.locationName });
            }
            if (JOptionPane.showConfirmDialog(null, message,
                    Bundle.getMessage("carDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                for (Car car : _carsTableFrame.carsTableModel.getSelectedCarList()) {
                    InstanceManager.getDefault(CarManager.class).deregister(car);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DeleteCarRosterAction.class);
}
