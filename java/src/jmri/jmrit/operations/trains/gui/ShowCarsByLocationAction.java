package jmri.jmrit.operations.trains.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.cars.gui.CarsTableFrame;

/**
 * Swing action to create a CarsTableFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class ShowCarsByLocationAction extends AbstractAction {

    public ShowCarsByLocationAction(TrainConductorPanel tcp) {
        super(Bundle.getMessage("MenuItemShowCars"));
        _tcp = tcp;
    }

    TrainConductorPanel _tcp;
    boolean showAllCars = false;
    String trackName = null;
    CarsTableFrame _ctf;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a car table frame
        if (_ctf != null) {
            _ctf.dispose();
        }
        _ctf = new CarsTableFrame(showAllCars, _tcp.getLocationName(), trackName);
    }
}
