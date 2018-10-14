package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will remove all cars from the operation database,
 * or remove all cars sitting on a track.
 *
 * @author Dan Boudreau Copyright (C) 2007, 2016
 */
public class DeleteCarRosterAction extends AbstractAction {

    CarsTableFrame _carsTableFrame;

    public DeleteCarRosterAction(String actionName, Component frame) {
        super(actionName);
    }

    public DeleteCarRosterAction(CarsTableFrame carsTableFrame) {
        super(carsTableFrame.carsTableModel.trackName == null ?
                Bundle.getMessage("MenuItemDelete") :
                MessageFormat.format(Bundle.getMessage("MenuDeleteCarsTrack"), new Object[]{carsTableFrame.carsTableModel.trackName}));
        _carsTableFrame = carsTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (_carsTableFrame.carsTableModel.trackName == null) {
            if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("carSureDelete"),
                    Bundle.getMessage("carDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                log.debug("removing all cars from roster");
                InstanceManager.getDefault(CarManager.class).deleteAll();
            }
        } else {
            if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("carDeleteCarsTrack"),
                    new Object[]{_carsTableFrame.carsTableModel.trackName}),
                    Bundle.getMessage("carDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                for (Car car : _carsTableFrame.carsTableModel.getSelectedCarList()) {
                    InstanceManager.getDefault(CarManager.class).deregister(car);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DeleteCarRosterAction.class);
}
