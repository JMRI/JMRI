package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for adding and editing the car load attribute for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2010, 2011, 2023
 */
public class CarLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);
    CarTypes carTypes = InstanceManager.getDefault(CarTypes.class);

    // labels
    JLabel textSep = new JLabel();
    JLabel quanity = new JLabel("0");

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton replaceButton = new JButton(Bundle.getMessage("Replace"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // combo boxes
    JComboBox<String> typeComboBox = carTypes.getComboBox();
    JComboBox<String> loadComboBox;
    JComboBox<String> priorityComboBox = carLoads.getPriorityComboBox();
    JComboBox<String> hazardousComboBox = carLoads.getHazardousComboBox();
    JComboBox<String> loadTypeComboBox = carLoads.getLoadTypesComboBox();

    // check boxes
    JCheckBox allTypesCheckBox = new JCheckBox(Bundle.getMessage("All"));

    // text boxes
    JTextField addTextBox = new JTextField(Control.max_len_string_attibute);
    JTextField pickupCommentTextField = new JTextField(35);
    JTextField dropCommentTextField = new JTextField(35);

    public CarLoadEditFrame() {
        super(Bundle.getMessage("TitleCarEditLoad"));
    }

    String _type; // car type name
    boolean menuActive = false;

    public void initComponents(String type, String selectedItem) {

        getContentPane().removeAll();

        _type = type;
        typeComboBox.setSelectedItem(_type);

        loadComboBox = carLoads.getComboBox(_type);
        carLoads.addPropertyChangeListener(this);
        loadComboBox.setSelectedItem(selectedItem);
        updateLoadType();
        updatePriority();
        updateHazardous();

        // general GUI config
        quanity.setVisible(showQuanity);

        // car type panel
        JPanel pType = new JPanel();
        pType.setLayout(new GridBagLayout());
        pType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        addItem(pType, typeComboBox, 0, 0);
        addItem(pType, allTypesCheckBox, 1, 0);

        allTypesCheckBox.setToolTipText(Bundle.getMessage("TipCarLoadAll"));

        // load panel
        JPanel pLoad = new JPanel();
        pLoad.setLayout(new GridBagLayout());
        pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));

        // row 2
        addItem(pLoad, addTextBox, 2, 2);
        addItem(pLoad, addButton, 3, 2);

        // row 3
        addItem(pLoad, quanity, 1, 3);
        addItem(pLoad, loadComboBox, 2, 3);
        addItem(pLoad, deleteButton, 3, 3);

        // row 4
        addItem(pLoad, replaceButton, 3, 4);

        deleteButton.setToolTipText(Bundle.getMessage("TipDeleteAttributeName",
                Bundle.getMessage("Load")));
        addButton.setToolTipText(Bundle.getMessage("TipAddAttributeName",
                Bundle.getMessage("Load")));
        replaceButton.setToolTipText(Bundle.getMessage("TipReplaceAttributeName",
                Bundle.getMessage("Load")));

        // row 6
        JPanel pLoadType = new JPanel();
        pLoadType.setLayout(new BoxLayout(pLoadType, BoxLayout.Y_AXIS));
        pLoadType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutLoadType")));
        addItem(pLoadType, loadTypeComboBox, 0, 0);

        // row 8
        JPanel pPriority = new JPanel();
        pPriority.setLayout(new BoxLayout(pPriority, BoxLayout.Y_AXIS));
        pPriority.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPriority")));
        addItem(pPriority, priorityComboBox, 0, 0);

        // row 9
        JPanel pHazardous = new JPanel();
        pHazardous.setLayout(new BoxLayout(pHazardous, BoxLayout.Y_AXIS));
        pHazardous.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Hazardous")));
        addItem(pHazardous, hazardousComboBox, 0, 0);

        // row 10
        // optional panel
        JPanel pOptionalPickup = new JPanel();
        pOptionalPickup.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptionalPickup")));
        addItem(pOptionalPickup, pickupCommentTextField, 0, 0);

        // row 12
        JPanel pOptionalDrop = new JPanel();
        pOptionalDrop.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptionalDrop")));
        addItem(pOptionalDrop, dropCommentTextField, 0, 0);

        // row 14
        JPanel pControl = new JPanel();
        pControl.setLayout(new BoxLayout(pControl, BoxLayout.Y_AXIS));
        addItem(pControl, saveButton, 0, 0);

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pType);
        getContentPane().add(pLoad);
        getContentPane().add(pLoadType);
        getContentPane().add(pPriority);
        getContentPane().add(pHazardous);
        getContentPane().add(pOptionalPickup);
        getContentPane().add(pOptionalDrop);
        getContentPane().add(pControl);

        addButtonAction(addButton);
        addButtonAction(deleteButton);
        addButtonAction(replaceButton);
        addButtonAction(saveButton);

        addComboBoxAction(typeComboBox);
        addComboBoxAction(loadComboBox);

        addCheckBoxAction(allTypesCheckBox);

        updateCarCommentFields();

        enableButtons();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CarLoadAttributeAction(this));
        toolMenu.add(new PrintCarLoadsAction(true));
        toolMenu.add(new PrintCarLoadsAction(false));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_EditCarLoads", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight500));
    }

    // add, delete, replace, and save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        String loadName = addTextBox.getText().trim();
        if (ae.getSource() == addButton || ae.getSource() == replaceButton) {
            if (!checkLoadName(loadName)) {
                return;
            }
        }
        if (ae.getSource() == addButton) {
            addLoadName(loadName);
        }
        if (ae.getSource() == deleteButton) {
            deleteLoadName();
        }
        if (ae.getSource() == replaceButton) {
            replaceLoadName(loadName);
        }
        if (ae.getSource() == saveButton) {
            saveLoadName();
            OperationsXml.save(); // save all files that have been modified;
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    private boolean checkLoadName(String loadName) {
        if (loadName.equals(NONE)) {
            return false;
        }
        String[] splitLoadName = loadName.split(TrainCommon.HYPHEN);
        // hyphen feature needs at least one character to work properly
        if (loadName.contains(TrainCommon.HYPHEN)) {
            if (splitLoadName.length == 0) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("HyphenFeature"),
                        Bundle.getMessage("canNotAdd", Bundle.getMessage("load")),
                        JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if (splitLoadName[0].length() > Control.max_len_string_attibute) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("carAttribute",
                    Control.max_len_string_attibute), Bundle.getMessage("canNotUseLoadName"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        // can't have the " & " as part of the load name
        if (loadName.contains(CarLoad.SPLIT_CHAR)) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("carNameNoAndChar",
                    CarLoad.SPLIT_CHAR), Bundle.getMessage("canNotUseLoadName"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("Combo box action");
        if (ae.getSource() == typeComboBox) {
            _type = (String) typeComboBox.getSelectedItem();
            updateLoadComboBox();
            enableButtons();
        }
        updateCarQuanity();
        updateLoadType();
        updatePriority();
        updateHazardous();
        updateCarCommentFields();
    }

    @Override
    protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        typeComboBox.setEnabled(!allTypesCheckBox.isSelected());
    }

    private void addLoadName(String loadName) {
        if (allTypesCheckBox.isSelected()) {
            for (String type : carTypes.getNames()) {
                carLoads.addName(type, loadName);
            }
        } else {
            carLoads.addName(_type, loadName);
        }
    }

    private void deleteLoadName() {
        String deleteLoad = (String) loadComboBox.getSelectedItem();
        if (deleteLoad.equals(carLoads.getDefaultEmptyName()) || deleteLoad.equals(carLoads.getDefaultLoadName())) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("carLoadDefault"), Bundle
                    .getMessage("canNotDelete", Bundle.getMessage("Load")),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        if (allTypesCheckBox.isSelected()) {
            for (String type : carTypes.getNames()) {
                replaceLoad(type, deleteLoad, null);
                carLoads.deleteName(type, deleteLoad);
            }
        } else {
            replaceLoad(_type, deleteLoad, null);
            carLoads.deleteName(_type, deleteLoad);
        }
    }

    private void replaceLoadName(String loadName) {
        String oldLoadName = (String) loadComboBox.getSelectedItem();
        if (oldLoadName.equals(carLoads.getDefaultEmptyName())) {
            if (JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("replaceDefaultEmpty",
                            oldLoadName, loadName),
                    Bundle.getMessage("replaceAll"),
                    JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
                return;
            }
            // don't allow the default names for load and empty to be the
            // same
            if (loadName.equals(carLoads.getDefaultEmptyName()) || loadName.equals(carLoads.getDefaultLoadName())) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("carDefault"), Bundle
                        .getMessage("canNotReplace", Bundle.getMessage("Load")),
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            carLoads.setDefaultEmptyName(loadName);
            replaceAllLoads(oldLoadName, loadName);
            return;
        }
        if (oldLoadName.equals(carLoads.getDefaultLoadName())) {
            if (JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("replaceDefaultLoad",
                            oldLoadName, loadName),
                    Bundle.getMessage("replaceAll"),
                    JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
                return;
            }
            // don't allow the default names for load and empty to be the
            // same
            if (loadName.equals(carLoads.getDefaultEmptyName()) || loadName.equals(carLoads.getDefaultLoadName())) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("carDefault"), Bundle
                        .getMessage("canNotReplace", Bundle.getMessage("Load")),
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            carLoads.setDefaultLoadName(loadName);
            replaceAllLoads(oldLoadName, loadName);
            return;
        }
        if (JmriJOptionPane.showConfirmDialog(this,
                Bundle.getMessage("replaceMsg",
                        oldLoadName, loadName),
                Bundle.getMessage("replaceAll"),
                JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
            return;
        }
        if (oldLoadName.equals(loadName)) {
            return; // do nothing
        }
        // ComboBoxes get deselected during the addName operation
        String loadType = carLoads.getLoadType(_type, oldLoadName);
        String loadPriority = carLoads.getPriority(_type, oldLoadName);
        boolean isHazardous = carLoads.isHazardous(_type, oldLoadName);
        String pickupComment = carLoads.getPickupComment(_type, oldLoadName);
        String dropComment = carLoads.getDropComment(_type, oldLoadName);

        if (allTypesCheckBox.isSelected()) {
            for (String type : carTypes.getNames()) {
                carLoads.addName(type, loadName);
                carLoads.setLoadType(type, loadName, loadType);
                carLoads.setPriority(type, loadName, loadPriority);
                carLoads.setHazardous(type, loadName, isHazardous);
                carLoads.setPickupComment(type, loadName, pickupComment);
                carLoads.setDropComment(type, loadName, dropComment);
                replaceLoad(type, oldLoadName, loadName);
                carLoads.deleteName(type, oldLoadName);
            }
        } else {
            carLoads.addName(_type, loadName);
            carLoads.setLoadType(_type, loadName, loadType);
            carLoads.setPriority(_type, loadName, loadPriority);
            carLoads.setHazardous(_type, loadName, isHazardous);
            carLoads.setPickupComment(_type, loadName, pickupComment);
            carLoads.setDropComment(_type, loadName, dropComment);
            replaceLoad(_type, oldLoadName, loadName);
            carLoads.deleteName(_type, oldLoadName);
        }
    }

    private void saveLoadName() {
        if (allTypesCheckBox.isSelected()) {
            for (String type : carTypes.getNames()) {
                saveLoadName(type);
            }
        } else {
            saveLoadName(_type);
        }
    }

    private void saveLoadName(String type) {
        carLoads.setLoadType(type, (String) loadComboBox.getSelectedItem(), (String) loadTypeComboBox
                .getSelectedItem());
        carLoads.setPriority(type, (String) loadComboBox.getSelectedItem(),
                (String) priorityComboBox.getSelectedItem());
        carLoads.setHazardous(type, (String) loadComboBox.getSelectedItem(),
                hazardousComboBox.getSelectedItem().equals(Bundle.getMessage("ButtonYes")));
        carLoads.setPickupComment(type, (String) loadComboBox.getSelectedItem(), pickupCommentTextField.getText());
        carLoads.setDropComment(type, (String) loadComboBox.getSelectedItem(), dropCommentTextField.getText());
    }

    // replace load name for all car types
    private void replaceAllLoads(String oldLoad, String newLoad) {
        for (String type : carTypes.getNames()) {
            carLoads.addName(type, newLoad);
            replaceLoad(type, oldLoad, newLoad);
            carLoads.deleteName(type, oldLoad);
        }
    }

    private void replaceLoad(String type, String oldLoad, String newLoad) {
        // adjust all cars
        InstanceManager.getDefault(CarManager.class).replaceLoad(type, oldLoad, newLoad);
        // now adjust schedules
        InstanceManager.getDefault(ScheduleManager.class).replaceLoad(type, oldLoad, newLoad);
        // now adjust trains
        InstanceManager.getDefault(TrainManager.class).replaceLoad(type, oldLoad, newLoad);
        // now adjust tracks
        InstanceManager.getDefault(LocationManager.class).replaceLoad(type, oldLoad, newLoad);
    }

    boolean showQuanity = false;

    public void toggleShowQuanity() {
        if (showQuanity) {
            showQuanity = false;
        } else {
            showQuanity = true;
        }
        quanity.setVisible(showQuanity);
        updateCarQuanity();
    }

    private void updateCarQuanity() {
        if (!showQuanity) {
            return;
        }
        int number = 0;
        String item = (String) loadComboBox.getSelectedItem();
        for (Car car : InstanceManager.getDefault(CarManager.class).getList()) {
            if (car.getLoadName().equals(item)) {
                number++;
            }
        }
        quanity.setText(Integer.toString(number));
    }

    private void updateLoadComboBox() {
        carLoads.updateComboBox(_type, loadComboBox);
        loadComboBox.setSelectedItem(addTextBox.getText().trim());
    }

    private void updateLoadType() {
        String loadName = (String) loadComboBox.getSelectedItem();
        loadTypeComboBox.setSelectedItem(carLoads.getLoadType(_type, loadName));
        if (loadName != null &&
                (loadName.equals(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName()) ||
                        loadName.equals(InstanceManager.getDefault(CarLoads.class)
                                .getDefaultLoadName()))) {
            loadTypeComboBox.setEnabled(false);
        } else {
            loadTypeComboBox.setEnabled(true);
        }
    }

    private void updatePriority() {
        priorityComboBox.setSelectedItem(carLoads.getPriority(_type, (String) loadComboBox.getSelectedItem()));
    }

    private void updateHazardous() {
        hazardousComboBox.setSelectedItem(carLoads.isHazardous(_type, (String) loadComboBox.getSelectedItem())
                ? Bundle.getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));
    }

    private void updateCarCommentFields() {
        pickupCommentTextField.setText(carLoads.getPickupComment(_type, (String) loadComboBox.getSelectedItem()));
        dropCommentTextField.setText(carLoads.getDropComment(_type, (String) loadComboBox.getSelectedItem()));
    }

    private void enableButtons() {
        addButton.setEnabled(_type != null);
        deleteButton.setEnabled(_type != null);
        replaceButton.setEnabled(_type != null);
        saveButton.setEnabled(_type != null);
    }

    @Override
    public void dispose() {
        carLoads.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
            updateLoadComboBox();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarLoadEditFrame.class);
}
