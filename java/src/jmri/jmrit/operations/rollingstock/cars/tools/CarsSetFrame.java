package jmri.jmrit.operations.rollingstock.cars.tools;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user to place a group of cars on the layout
 *
 * @author Dan Boudreau Copyright (C) 2011, 2013, 2023
 */
public class CarsSetFrame extends CarSetFrame {

    CarsTableModel _carsTableModel;
    JTable _carsTable;

    public CarsSetFrame() {
        super();
    }

    // Ignore checkbox states
    private static boolean ignoreStatusCheckBoxSelected = true;
    private static boolean ignoreLocationCheckBoxSelected = true;
    private static boolean ignoreDivisionCheckBoxSelected = true;
    private static boolean ignoreRWECheckBoxSelected = true;
    private static boolean ignoreRWLCheckBoxSelected = true;
    private static boolean ignoreLoadCheckBoxSelected = true;
    private static boolean ignoreKernelCheckBoxSelected = true;
    private static boolean ignoreDestinationCheckBoxSelected = true;
    private static boolean ignoreFinalDestinationCheckBoxSelected = true;
    private static boolean ignoreTrainCheckBoxSelected = true;

    public void initComponents(JTable carsTable) {
        _carsTable = carsTable;
        _carsTableModel = (CarsTableModel) carsTable.getModel();

        super.initComponents("package.jmri.jmrit.operations.Operations_SetCars");

        setTitle(Bundle.getMessage("TitleSetCars"));
        // modify Save button text to "Apply";
        saveButton.setText(Bundle.getMessage("ButtonApply"));
        // disable edit load button if no cars selected
        editLoadButton.setEnabled(false);
        // show ignore checkboxes
        ignoreStatusCheckBox.setVisible(true);
        ignoreLocationCheckBox.setVisible(true);
        ignoreDivisionCheckBox.setVisible(true);
        ignoreRWECheckBox.setVisible(true);
        ignoreRWLCheckBox.setVisible(true);
        ignoreLoadCheckBox.setVisible(true);
        ignoreKernelCheckBox.setVisible(true);
        ignoreDestinationCheckBox.setVisible(true);
        ignoreFinalDestinationCheckBox.setVisible(true);
        ignoreTrainCheckBox.setVisible(true);
        ignoreAllButton.setVisible(true);

        // set the last state
        ignoreStatusCheckBox.setSelected(ignoreStatusCheckBoxSelected);
        ignoreLocationCheckBox.setSelected(ignoreLocationCheckBoxSelected);
        ignoreDivisionCheckBox.setSelected(ignoreDivisionCheckBoxSelected);
        ignoreRWECheckBox.setSelected(ignoreRWECheckBoxSelected);
        ignoreRWLCheckBox.setSelected(ignoreRWLCheckBoxSelected);
        ignoreLoadCheckBox.setSelected(ignoreLoadCheckBoxSelected);
        ignoreKernelCheckBox.setSelected(ignoreKernelCheckBoxSelected);
        ignoreDestinationCheckBox.setSelected(ignoreDestinationCheckBoxSelected);
        ignoreFinalDestinationCheckBox.setSelected(ignoreFinalDestinationCheckBoxSelected);
        ignoreTrainCheckBox.setSelected(ignoreTrainCheckBoxSelected);

        // first car in the list becomes the master
        int rows[] = _carsTable.getSelectedRows();
        if (rows.length > 0) {
            Car car = _carsTableModel.getCarAtIndex(_carsTable.convertRowIndexToModel(rows[0]));
            super.load(car);
        } else {
            enableComponents(true);
            showMessageDialogWarning();
        }
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        super.buttonActionPerformed(ae);
        if (ae.getSource() == ignoreAllButton) {
            ignoreAll(toggle);
        }
    }

    boolean toggle = false;

    protected void ignoreAll(boolean b) {
        ignoreStatusCheckBox.setSelected(!locationUnknownCheckBox.isSelected() & b);
        ignoreLocationCheckBox.setSelected(b);
        ignoreDivisionCheckBox.setSelected(b);
        ignoreRWECheckBox.setSelected(b);
        ignoreRWLCheckBox.setSelected(b);
        ignoreLoadCheckBox.setSelected(b);
        ignoreKernelCheckBox.setSelected(b);
        ignoreDestinationCheckBox.setSelected(b);
        ignoreFinalDestinationCheckBox.setSelected(b);
        ignoreTrainCheckBox.setSelected(b);
        enableComponents(!locationUnknownCheckBox.isSelected());
        toggle = !b;
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected boolean save() {
        // save ignore states
        ignoreStatusCheckBoxSelected = ignoreStatusCheckBox.isSelected();
        ignoreLocationCheckBoxSelected = ignoreLocationCheckBox.isSelected();
        ignoreDivisionCheckBoxSelected = ignoreDivisionCheckBox.isSelected();
        ignoreRWECheckBoxSelected = ignoreRWECheckBox.isSelected();
        ignoreRWLCheckBoxSelected = ignoreRWLCheckBox.isSelected();
        ignoreLoadCheckBoxSelected = ignoreLoadCheckBox.isSelected();
        ignoreKernelCheckBoxSelected = ignoreKernelCheckBox.isSelected();
        ignoreDestinationCheckBoxSelected = ignoreKernelCheckBox.isSelected();
        ignoreFinalDestinationCheckBoxSelected = ignoreFinalDestinationCheckBox.isSelected();
        ignoreTrainCheckBoxSelected = ignoreTrainCheckBox.isSelected();

        // need to get selected cars before they are modified their location in the table can change
        List<Car> cars = new ArrayList<Car>();
        int rows[] = _carsTable.getSelectedRows();
        for (int row : rows) {
            Car car = _carsTableModel.getCarAtIndex(_carsTable.convertRowIndexToModel(row));
            log.debug("Adding selected car {} to change list", car.toString());
            cars.add(car);
        }
        if (rows.length == 0) {
            showMessageDialogWarning();
            return false;
        } else if (cars.get(0) != _car) {
            log.debug("Default car isn't the first one selected");
            if (JmriJOptionPane.showConfirmDialog(this, Bundle
                    .getMessage("doYouWantToChange", cars.get(0).toString()), Bundle
                    .getMessage("changeDefaultCar"), JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                super.load(cars.get(0)); // new default car
                return false; // done, don't modify any of the cars selected
            }
        }

        // don't ask for to change cars in a kernel when giving a selected group of cars a new kernel name
        askKernelChange = false;
        
        // determine if all cars in every kernel are selected
        for (Car car : cars) {
            if (car.getKernel() != null) {
                for (Car c : car.getKernel().getCars()) {
                    if (!cars.contains(c)) {
                        askKernelChange = true; // not all selected
                        break;
                    }
                }
            }
        }

        for (Car car : cars) {
            if (!super.change(car)) {
                return false;
            } else if (car.getKernel() != null && !ignoreKernelCheckBox.isSelected()) {
                askKernelChange = false; // changing kernel name
            }
        }
        return false; // all good, but don't close window
    }
    
    private void showMessageDialogWarning() {
        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("selectCars"), Bundle
                .getMessage("carNoneSelected"), JmriJOptionPane.WARNING_MESSAGE);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarsSetFrame.class);
}
