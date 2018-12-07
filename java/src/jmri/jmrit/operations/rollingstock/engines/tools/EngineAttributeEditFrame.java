package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineLengths;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the engine roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class EngineAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

    // labels
    JLabel textAttribute = new JLabel();
    JLabel textSep = new JLabel();

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton replaceButton = new JButton(Bundle.getMessage("Replace"));

    // combo box
    JComboBox<String> comboBox;

    // text box
    JTextField addTextBox = new JTextField(Control.max_len_string_attibute);
    
    public static final String ROAD = Bundle.getMessage("Road");
    public static final String MODEL = Bundle.getMessage("Model");
    public static final String TYPE = Bundle.getMessage("Type");
    public static final String COLOR = Bundle.getMessage("Color");
    public static final String LENGTH = Bundle.getMessage("Length");
    public static final String OWNER = Bundle.getMessage("Owner");
    public static final String CONSIST = Bundle.getMessage("Consist");

    // property change
    public static final String DISPOSE = "dispose"; // NOI18N

    public EngineAttributeEditFrame() {
    }

    public String _comboboxName; // track which combo box is being edited
    boolean menuActive = false;

    public void initComponents(String comboboxName) {
        initComponents(comboboxName, "");
    }

    public void initComponents(String comboboxName, String select) {

        getContentPane().removeAll();

        setTitle(MessageFormat.format(Bundle.getMessage("TitleEngineEditAtrribute"), new Object[]{comboboxName}));
        
        deleteButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipDeleteAttributeName"), new Object[]{comboboxName}));
        addButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipAddAttributeName"), new Object[]{comboboxName}));
        replaceButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipReplaceAttributeName"), new Object[]{comboboxName}));

        // track which combo box is being edited
        _comboboxName = comboboxName;
        loadCombobox();
        comboBox.setSelectedItem(select);

        // general GUI config
        getContentPane().setLayout(new GridBagLayout());

        textAttribute.setText(comboboxName);

        // row 1
        addItem(textAttribute, 1, 1);
        // row 2
        addItem(addTextBox, 1, 2);
        addItem(addButton, 2, 2);

        // row 3
        addItem(comboBox, 1, 3);
        addItem(deleteButton, 2, 3);

        // row 4
        addItem(replaceButton, 2, 4);

        addButtonAction(addButton);
        addButtonAction(deleteButton);
        addButtonAction(replaceButton);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Locomotives", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight250));

    }

    // add, delete or replace button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("edit frame button activated");
        if (ae.getSource() == addButton) {
            String addItem = addTextBox.getText();
            if (!checkItemName(addItem, Bundle.getMessage("canNotAdd"))) {
                return;
            }
            addItemToCombobox(addItem);
        }
        if (ae.getSource() == deleteButton) {
            String deleteItem = (String) comboBox.getSelectedItem();
            deleteItemFromCombobox(deleteItem);
        }
        if (ae.getSource() == replaceButton) {
            String newItem = addTextBox.getText();
            if (!checkItemName(newItem, Bundle.getMessage("canNotReplace"))) {
                return;
            }
            String oldItem = (String) comboBox.getSelectedItem();
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceMsg"), new Object[]{
                    oldItem, newItem}), Bundle.getMessage("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            if (newItem.equals(oldItem)) {
                return;
            }
            // need to make sure locations and trains are loaded
            InstanceManager.getDefault(TrainManager.class);
            addItemToCombobox(newItem);
            replaceItem(oldItem, newItem);
            deleteItemFromCombobox(oldItem);
        }
    }

    private boolean checkItemName(String itemName, String errorMessage) {
        if (itemName.equals(NONE)) {
            return false;
        }
        if (_comboboxName.equals(ROAD)) {
            if (!OperationsXml.checkFileName(itemName)) { // NOI18N
                JOptionPane.showMessageDialog(this, Bundle.getMessage("NameResChar") + NEW_LINE
                        + Bundle.getMessage("ReservedChar"),
                        MessageFormat.format(errorMessage, new Object[]{_comboboxName}),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        String[] item = {itemName};
        if (_comboboxName.equals(TYPE)) {
            item = itemName.split("-");
        }
        if (item[0].length() > Control.max_len_string_attibute) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                    new Object[]{Control.max_len_string_attibute}), MessageFormat.format(errorMessage,
                    new Object[]{_comboboxName}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void deleteItemFromCombobox(String deleteItem) {
        if (_comboboxName.equals(ROAD)) {
            // purge train and locations by using replace
            InstanceManager.getDefault(CarRoads.class).replaceName(deleteItem, null);
        }
        if (_comboboxName.equals(MODEL)) {
            InstanceManager.getDefault(EngineModels.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(TYPE)) {
            InstanceManager.getDefault(EngineTypes.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(LENGTH)) {
            InstanceManager.getDefault(EngineLengths.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(CONSIST)) {
            engineManager.deleteConsist(deleteItem);
        }
    }

    private void addItemToCombobox(String addItem) {
        if (_comboboxName.equals(ROAD)) {
            InstanceManager.getDefault(CarRoads.class).addName(addItem);
        }
        if (_comboboxName.equals(MODEL)) {
            InstanceManager.getDefault(EngineModels.class).addName(addItem);
        }
        if (_comboboxName.equals(TYPE)) {
            InstanceManager.getDefault(EngineTypes.class).addName(addItem);
        }
        if (_comboboxName.equals(LENGTH)) {
            // convert from inches to feet if needed
            if (addItem.endsWith("\"")) { // NOI18N
                addItem = addItem.substring(0, addItem.length() - 1);
                try {
                    double inches = Double.parseDouble(addItem);
                    int feet = (int) (inches * Setup.getScaleRatio() / 12);
                    addItem = Integer.toString(feet);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertFeet"), Bundle
                            .getMessage("ErrorEngineLength"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (addItem.endsWith("cm")) { // NOI18N
                addItem = addItem.substring(0, addItem.length() - 2);
                try {
                    double cm = Double.parseDouble(addItem);
                    int meter = (int) (cm * Setup.getScaleRatio() / 100);
                    addItem = Integer.toString(meter);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertMeter"), Bundle
                            .getMessage("ErrorEngineLength"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // confirm that length is a number and less than 10000 feet
            try {
                int length = Integer.parseInt(addItem);
                if (length < 0) {
                    log.error("engine length has to be a positive number");
                    return;
                }
                if (addItem.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                            new Object[]{Control.max_len_string_length_name}), MessageFormat.format(Bundle
                            .getMessage("canNotAdd"), new Object[]{_comboboxName}), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                log.error("length not an integer");
                return;
            }
            InstanceManager.getDefault(EngineLengths.class).addName(addItem);
            comboBox.setSelectedItem(addItem);
        }
        if (_comboboxName.equals(CONSIST)) {
            engineManager.newConsist(addItem);
        }
        if (_comboboxName.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).addName(addItem);
        }
    }

    private void replaceItem(String oldItem, String newItem) {
        List<Engine> engines = engineManager.getList();
        for (Engine engine : engines) {
            if (_comboboxName.equals(MODEL)) {
                // we need to copy the old model attributes, so find an engine.
                if (engine.getModel().equals(oldItem)) {
                    // Has this model been configured?
                    if (InstanceManager.getDefault(EngineModels.class).getModelLength(newItem) != null) {
                        engine.setModel(newItem);
                    } else {
                        // get the old configuration for this model
                        String length = engine.getLength();
                        String hp = engine.getHp();
                        String type = engine.getTypeName();
                        // now update the new model
                        engine.setModel(newItem);
                        engine.setLength(length);
                        engine.setHp(hp);
                        engine.setTypeName(type);
                    }
                }
            }
        }
        if (_comboboxName.equals(CONSIST)) {
            engineManager.replaceConsistName(oldItem, newItem);
        }
        // now adjust locations and trains
        if (_comboboxName.equals(TYPE)) {
            InstanceManager.getDefault(EngineTypes.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(ROAD)) {
            InstanceManager.getDefault(CarRoads.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(LENGTH)) {
            InstanceManager.getDefault(EngineLengths.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(MODEL)) {
            InstanceManager.getDefault(EngineModels.class).replaceName(oldItem, newItem);
        }
    }

    private void loadCombobox() {
        if (_comboboxName.equals(ROAD)) {
            comboBox = InstanceManager.getDefault(CarRoads.class).getComboBox();
            InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(MODEL)) {
            comboBox = InstanceManager.getDefault(EngineModels.class).getComboBox();
            InstanceManager.getDefault(EngineModels.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(TYPE)) {
            comboBox = InstanceManager.getDefault(EngineTypes.class).getComboBox();
            InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(LENGTH)) {
            comboBox = InstanceManager.getDefault(EngineLengths.class).getComboBox();
            InstanceManager.getDefault(EngineLengths.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(OWNER)) {
            comboBox = InstanceManager.getDefault(CarOwners.class).getComboBox();
            InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(CONSIST)) {
            comboBox = engineManager.getConsistComboBox();
            engineManager.addPropertyChangeListener(this);
        }
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineLengths.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        engineManager.removePropertyChangeListener(this);
        firePcs(DISPOSE, _comboboxName, null);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarRoads.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineModels.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineTypes.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineLengths.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarOwners.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)) {
            engineManager.updateConsistComboBox(comboBox);
        }
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // note firePropertyChange occurs during frame creation
    private void firePcs(String p, Object old, Object n) {
        log.debug("EngineAttribute firePropertyChange {}", p);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineAttributeEditFrame.class);
}
