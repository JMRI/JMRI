package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2010, 2011
 */
public class CarLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    public static final String NONE = "";

    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);

    // labels
    JLabel textSep = new JLabel();
    JLabel quanity = new JLabel("0");

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton replaceButton = new JButton(Bundle.getMessage("Replace"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // combo boxes
    JComboBox<String> loadComboBox;
    JComboBox<String> priorityComboBox;
    JComboBox<String> loadTypeComboBox = carLoads.getLoadTypesComboBox();

    // text boxes
    JTextField addTextBox = new JTextField(10);
    JTextField pickupCommentTextField = new JTextField(35);
    JTextField dropCommentTextField = new JTextField(35);

    public CarLoadEditFrame() {
    }

    String _type;
    boolean menuActive = false;

    public void initComponents(String type, String selectedItem) {

        getContentPane().removeAll();

        setTitle(MessageFormat.format(Bundle.getMessage("TitleCarEditLoad"), new Object[]{type}));

        // track which combo box is being edited
        _type = type;
        loadComboboxes();
        loadComboBox.setSelectedItem(selectedItem);
        updateLoadType();
        updatePriority();

        // general GUI config
        quanity.setVisible(showQuanity);

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
        
        deleteButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipDeleteAttributeName"), new Object[]{Bundle.getMessage("Load")}));
        addButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipAddAttributeName"), new Object[]{Bundle.getMessage("Load")}));
        replaceButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipReplaceAttributeName"), new Object[]{Bundle.getMessage("Load")}));

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
        getContentPane().add(pLoad);
        getContentPane().add(pLoadType);
        getContentPane().add(pPriority);
        getContentPane().add(pOptionalPickup);
        getContentPane().add(pOptionalDrop);
        getContentPane().add(pControl);

        addButtonAction(addButton);
        addButtonAction(deleteButton);
        addButtonAction(replaceButton);
        addButtonAction(saveButton);

        addComboBoxAction(loadComboBox);

        updateCarCommentFields();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CarLoadAttributeAction(Bundle.getMessage("CarQuantity"), this));
        toolMenu.add(new PrintCarLoadsAction(true, this));
        toolMenu.add(new PrintCarLoadsAction(false, this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_EditCarLoads", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight500));
    }

    // add, delete, replace, and save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        String loadName = addTextBox.getText().trim();
        if (ae.getSource() == addButton || ae.getSource() == replaceButton) {
            if (loadName.equals(NONE)) {
                return;
            }
            if (loadName.length() > Control.max_len_string_attibute) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carAttribute"),
                        new Object[]{Control.max_len_string_attibute}), Bundle.getMessage("canNotUseLoadName"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // can't have the " & " as part of the load name
            if (loadName.contains(CarLoad.SPLIT_CHAR)) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carNameNoAndChar"),
                        new Object[]{CarLoad.SPLIT_CHAR}), Bundle.getMessage("canNotUseLoadName"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (ae.getSource() == addButton) {
            carLoads.addName(_type, loadName);
        }
        if (ae.getSource() == deleteButton) {
            String deleteLoad = (String) loadComboBox.getSelectedItem();
            if (deleteLoad.equals(carLoads.getDefaultEmptyName()) || deleteLoad.equals(carLoads.getDefaultLoadName())) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("carLoadDefault"), MessageFormat.format(Bundle
                        .getMessage("canNotDelete"), new Object[]{Bundle.getMessage("Load")}),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            replaceLoad(_type, deleteLoad, null);
            carLoads.deleteName(_type, deleteLoad);
        }
        if (ae.getSource() == replaceButton) {
            String oldLoad = (String) loadComboBox.getSelectedItem();
            if (oldLoad.equals(carLoads.getDefaultEmptyName())) {
                if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceDefaultEmpty"),
                        new Object[]{oldLoad, loadName}), Bundle.getMessage("replaceAll"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                // don't allow the default names for load and empty to be the same
                if (loadName.equals(carLoads.getDefaultEmptyName()) || loadName.equals(carLoads.getDefaultLoadName())) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("carDefault"), MessageFormat.format(Bundle
                            .getMessage("canNotReplace"), new Object[]{Bundle.getMessage("Load")}),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                carLoads.setDefaultEmptyName(loadName);
                replaceAllLoads(oldLoad, loadName);
                return;
            }
            if (oldLoad.equals(carLoads.getDefaultLoadName())) {
                if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceDefaultLoad"),
                        new Object[]{oldLoad, loadName}), Bundle.getMessage("replaceAll"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                // don't allow the default names for load and empty to be the same
                if (loadName.equals(carLoads.getDefaultEmptyName()) || loadName.equals(carLoads.getDefaultLoadName())) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("carDefault"), MessageFormat.format(Bundle
                            .getMessage("canNotReplace"), new Object[]{Bundle.getMessage("Load")}),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                carLoads.setDefaultLoadName(loadName);
                replaceAllLoads(oldLoad, loadName);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceMsg"), new Object[]{
                    oldLoad, loadName}), Bundle.getMessage("replaceAll"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            carLoads.addName(_type, loadName);
            replaceLoad(_type, oldLoad, loadName);
            carLoads.deleteName(_type, oldLoad);
        }
        if (ae.getSource() == saveButton) {
            log.debug("CarLoadEditFrame save button pressed");
            carLoads.setLoadType(_type, (String) loadComboBox.getSelectedItem(), (String) loadTypeComboBox
                    .getSelectedItem());
            carLoads.setPriority(_type, (String) loadComboBox.getSelectedItem(),
                    (String) priorityComboBox.getSelectedItem());
            carLoads.setPickupComment(_type, (String) loadComboBox.getSelectedItem(), pickupCommentTextField.getText());
            carLoads.setDropComment(_type, (String) loadComboBox.getSelectedItem(), dropCommentTextField.getText());
            
            OperationsXml.save(); // save all files that have been modified;
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("Combo box action");
        updateCarQuanity();
        updateLoadType();
        updatePriority();
        updateCarCommentFields();
    }

    // replace the default empty and load for all car types
    private void replaceAllLoads(String oldLoad, String newLoad) {
        for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
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

    private void loadComboboxes() {
        loadComboBox = carLoads.getComboBox(_type);
        carLoads.addPropertyChangeListener(this);
        priorityComboBox = carLoads.getPriorityComboBox();
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

    private void updateCarCommentFields() {
        pickupCommentTextField.setText(carLoads.getPickupComment(_type, (String) loadComboBox.getSelectedItem()));
        dropCommentTextField.setText(carLoads.getDropComment(_type, (String) loadComboBox.getSelectedItem()));
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
            carLoads.updateComboBox(_type, loadComboBox);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CarLoadEditFrame.class);
}
