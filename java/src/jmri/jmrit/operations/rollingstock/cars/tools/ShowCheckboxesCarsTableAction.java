package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.rollingstock.cars.CarsTableModel;

/**
 * Swing action to show checkboxes in the cars window.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * 
 */
public class ShowCheckboxesCarsTableAction extends AbstractAction {

    CarsTableModel _carsTableModel;

    public ShowCheckboxesCarsTableAction(String s) {
        super(s);
    }

    public ShowCheckboxesCarsTableAction(CarsTableModel carsTableModel) {
        this(Bundle.getMessage("TitleShowCheckboxes"));
        _carsTableModel = carsTableModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _carsTableModel.toggleSelectVisible();
    }
}


