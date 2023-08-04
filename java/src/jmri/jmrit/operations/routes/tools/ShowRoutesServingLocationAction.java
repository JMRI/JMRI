package jmri.jmrit.operations.routes.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;

/**
 * Action to create the ShowRoutesServingLocationFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2023
 */
public class ShowRoutesServingLocationAction extends AbstractAction {

    public ShowRoutesServingLocationAction(Location location) {
        super(Bundle.getMessage("TitleShowRoutes"));
        _location = location;
    }

    Location _location;
    ShowRoutesServingLocationFrame _frame;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_frame != null) {
            _frame.dispose();
        }
        _frame = new ShowRoutesServingLocationFrame();
        _frame.initComponents(_location);
    }
}
