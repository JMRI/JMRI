// ResetCheckboxesCarsTableAction.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to reset checkboxes in the cars window.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 22219 $
 */
public class ResetCheckboxesCarsTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 1076812683689273095L;
    CarsTableModel _carsTableModel;

    public ResetCheckboxesCarsTableAction(String s) {
        super(s);
    }

    public ResetCheckboxesCarsTableAction(CarsTableModel carsTableModel) {
        this(Bundle.getMessage("TitleResetCheckboxes"));
        _carsTableModel = carsTableModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _carsTableModel.resetCheckboxes();
    }
}

/* @(#)ResetCheckboxesCarsTableAction.java */
