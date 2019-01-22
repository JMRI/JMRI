package jmri.jmrit.operations.rollingstock.cars.tools;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.tools.LocationsByCarTypeFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.tools.TrainsByCarTypeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for editing a car attribute.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    CarManager carManager = InstanceManager.getDefault(CarManager.class);

    // labels
    JLabel textAttribute = new JLabel();
    JLabel textSep = new JLabel();
    JLabel quanity = new JLabel("0");

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton replaceButton = new JButton(Bundle.getMessage("Replace"));

    // combo box
    JComboBox<String> comboBox;

    // text box
    JTextField addTextBox = new JTextField(Control.max_len_string_attibute);

    public static final String ROAD = Bundle.getMessage("Road");
    public static final String TYPE = Bundle.getMessage("Type");
    public static final String COLOR = Bundle.getMessage("Color");
    public static final String LENGTH = Bundle.getMessage("Length");
    public static final String OWNER = Bundle.getMessage("Owner");
    public static final String KERNEL = Bundle.getMessage("Kernel");

    boolean showQuanity = false;

    // property change
    public static final String DISPOSE = "dispose"; // NOI18N

    public CarAttributeEditFrame() {
    }

    public String _comboboxName; // used to determine which combo box is being edited

    public void initComponents(String comboboxName) {
        initComponents(comboboxName, NONE);
    }

    public void initComponents(String comboboxName, String select) {

        getContentPane().removeAll();

        setTitle(MessageFormat.format(Bundle.getMessage("TitleCarEditAtrribute"), new Object[]{comboboxName}));

        // track which combo box is being edited
        _comboboxName = comboboxName;
        loadCombobox();
        comboBox.setSelectedItem(select);

        // general GUI config
        getContentPane().setLayout(new GridBagLayout());

        textAttribute.setText(comboboxName);

        quanity.setVisible(showQuanity);

        // row 1
        addItem(textAttribute, 2, 1);
        // row 2
        addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);

        // row 3
        addItem(quanity, 1, 3);
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);

        // row 4
        addItem(replaceButton, 3, 4);

        addButtonAction(addButton);
        addButtonAction(deleteButton);
        addButtonAction(replaceButton);

        addComboBoxAction(comboBox);
        carManager.addPropertyChangeListener(this);
        
        deleteButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipDeleteAttributeName"), new Object[]{comboboxName}));
        addButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipAddAttributeName"), new Object[]{comboboxName}));
        replaceButton.setToolTipText( MessageFormat.format(Bundle.getMessage("TipReplaceAttributeName"), new Object[]{comboboxName}));

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CarAttributeAction(Bundle.getMessage("CarQuantity"), this));
        toolMenu.add(new CarDeleteAttributeAction(Bundle.getMessage("DeleteUnusedAttributes"), this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_EditCarAttributes", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight250));
    }

    // add, delete, or replace button
    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("edit frame button activated");
        if (ae.getSource() == addButton) {
            String addItem = addTextBox.getText().trim();
            if (!checkItemName(addItem, Bundle.getMessage("canNotAdd"))) {
                return;
            }
            addItemToCombobox(addItem);
        }
        if (ae.getSource() == deleteButton) {
            deleteItemFromCombobox((String) comboBox.getSelectedItem());
        }
        if (ae.getSource() == replaceButton) {
            String newItem = addTextBox.getText().trim();
            if (!checkItemName(newItem, Bundle.getMessage("canNotReplace"))) {
                return;
            }
            String oldItem = (String) comboBox.getSelectedItem();
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceMsg"), new Object[]{
                    oldItem, newItem}), Bundle.getMessage("replaceAll"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            if (newItem.equals(oldItem)) {
                return;
            }
            // need to make sure locations and trains are loaded
            InstanceManager.getDefault(TrainManager.class);
            // InstanceManager.getDefault(LocationManager.class);
            // don't show dialog, save current state
            boolean oldShow = showDialogBox;
            showDialogBox = false;
            addItemToCombobox(newItem);
            showDialogBox = oldShow;
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
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("NameResChar") + NEW_LINE + Bundle.getMessage("ReservedChar"),
                        MessageFormat.format(errorMessage, new Object[]{_comboboxName}),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        String[] item = {itemName};
        if (_comboboxName.equals(TYPE)) {
            item = itemName.split("-");
            // can't have the " & " as part of the type name
            if (itemName.contains(CarLoad.SPLIT_CHAR)) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carNameNoAndChar"),
                        new Object[]{CarLoad.SPLIT_CHAR}), MessageFormat.format(errorMessage, new Object[]{_comboboxName}),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if (item[0].length() > Control.max_len_string_attibute) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carAttribute"),
                    new Object[]{Control.max_len_string_attibute}),
                    MessageFormat.format(errorMessage,
                            new Object[]{_comboboxName}),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("Combo box action");
        updateCarQuanity();
    }

    private void deleteItemFromCombobox(String deleteItem) {
        log.debug("delete attribute {}", deleteItem);
        if (_comboboxName.equals(ROAD)) {
            // purge train and locations by using replace
            InstanceManager.getDefault(CarRoads.class).replaceName(deleteItem, null);
        }
        if (_comboboxName.equals(TYPE)) {
            InstanceManager.getDefault(CarTypes.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(COLOR)) {
            InstanceManager.getDefault(CarColors.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(LENGTH)) {
            InstanceManager.getDefault(CarLengths.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).deleteName(deleteItem);
        }
        if (_comboboxName.equals(KERNEL)) {
            carManager.deleteKernel(deleteItem);
        }
    }

    static boolean showDialogBox = true;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    private void addItemToCombobox(String addItem) {
        if (_comboboxName.equals(ROAD)) {
            InstanceManager.getDefault(CarRoads.class).addName(addItem);
        }
        if (_comboboxName.equals(TYPE)) {
            InstanceManager.getDefault(CarTypes.class).addName(addItem);
            if (showDialogBox) {
                int results = JOptionPane.showOptionDialog(this, Bundle.getMessage("AddNewCarType"), Bundle
                        .getMessage("ModifyLocations"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                                Bundle.getMessage("ButtonDontShow")},
                        Bundle.getMessage("ButtonNo"));
                if (results == JOptionPane.YES_OPTION) {
                    LocationsByCarTypeFrame lf = new LocationsByCarTypeFrame();
                    lf.initComponents(addItem);
                }
                if (results == JOptionPane.CANCEL_OPTION) {
                    showDialogBox = false;
                }
                results = JOptionPane.showOptionDialog(this, Bundle.getMessage("AddNewCarType"), Bundle
                        .getMessage("ModifyTrains"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                                Bundle.getMessage("ButtonDontShow")},
                        Bundle.getMessage("ButtonNo"));
                if (results == JOptionPane.YES_OPTION) {
                    TrainsByCarTypeFrame lf = new TrainsByCarTypeFrame();
                    lf.initComponents(addItem);
                }
                if (results == JOptionPane.CANCEL_OPTION) {
                    showDialogBox = false;
                }
            }
        }
        if (_comboboxName.equals(COLOR)) {
            InstanceManager.getDefault(CarColors.class).addName(addItem);
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
                            .getMessage("ErrorCarLength"), JOptionPane.ERROR_MESSAGE);
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
                            .getMessage("ErrorCarLength"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // confirm that length is a number and less than 10000 feet
            try {
                int length = Integer.parseInt(addItem);
                if (length < 0) {
                    log.error("length ({}) has to be a positive number", addItem);
                    return;
                }
                if (addItem.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carAttribute"),
                            new Object[]{Control.max_len_string_length_name}),
                            MessageFormat.format(Bundle
                                    .getMessage("canNotAdd"), new Object[]{_comboboxName}),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                log.error("length ({}) is not an integer", addItem);
                return;
            }
            InstanceManager.getDefault(CarLengths.class).addName(addItem);
            comboBox.setSelectedItem(addItem);
        }
        if (_comboboxName.equals(KERNEL)) {
            carManager.newKernel(addItem);
        }
        if (_comboboxName.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).addName(addItem);
        }
    }

    private void replaceItem(String oldItem, String newItem) {
        // replace kernel
        if (_comboboxName.equals(KERNEL)) {
            carManager.replaceKernelName(oldItem, newItem);
        }
        // now adjust cars, locations and trains
        if (_comboboxName.equals(TYPE)) {
            InstanceManager.getDefault(CarTypes.class).replaceName(oldItem, newItem);
            InstanceManager.getDefault(CarLoads.class).replaceType(oldItem, newItem);
        }
        if (_comboboxName.equals(ROAD)) {
            InstanceManager.getDefault(CarRoads.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(LENGTH)) {
            InstanceManager.getDefault(CarLengths.class).replaceName(oldItem, newItem);
        }
        if (_comboboxName.equals(COLOR)) {
            InstanceManager.getDefault(CarColors.class).replaceName(oldItem, newItem);
        }
    }

    private void loadCombobox() {
        if (_comboboxName.equals(ROAD)) {
            comboBox = InstanceManager.getDefault(CarRoads.class).getComboBox();
            InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(TYPE)) {
            comboBox = InstanceManager.getDefault(CarTypes.class).getComboBox();
            InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(COLOR)) {
            comboBox = InstanceManager.getDefault(CarColors.class).getComboBox();
            InstanceManager.getDefault(CarColors.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(LENGTH)) {
            comboBox = InstanceManager.getDefault(CarLengths.class).getComboBox();
            InstanceManager.getDefault(CarLengths.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(OWNER)) {
            comboBox = InstanceManager.getDefault(CarOwners.class).getComboBox();
            InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        }
        if (_comboboxName.equals(KERNEL)) {
            comboBox = carManager.getKernelComboBox();
        }
    }

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
        String item = (String) comboBox.getSelectedItem();
        log.debug("Selected item {}", item);
        for (Car car : carManager.getList()) {
            if (_comboboxName.equals(ROAD)) {
                if (car.getRoadName().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName.equals(TYPE)) {
                if (car.getTypeName().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName.equals(COLOR)) {
                if (car.getColor().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName.equals(LENGTH)) {
                if (car.getLength().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName.equals(OWNER)) {
                if (car.getOwner().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName.equals(KERNEL)) {
                if (car.getKernelName().equals(item)) {
                    number++;
                }
            }
        }
        quanity.setText(Integer.toString(number));
        // Tool to delete all attributes that haven't been assigned to a car
        if (number == 0 && deleteUnused) {
            // need to check if an engine is using the road name
            if (_comboboxName.equals(ROAD)) {
                for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList()) {
                    if (rs.getRoadName().equals(item)) {
                        log.info("Engine (" +
                                rs.getRoadName() +
                                " " +
                                rs.getNumber() +
                                ") has assigned road name (" +
                                item +
                                ")"); // NOI18N
                        return;
                    }
                }
            }
            // confirm that attribute is to be deleted
            if (!cancel) {
                int results = JOptionPane.showOptionDialog(null, MessageFormat.format(Bundle
                        .getMessage("ConfirmDeleteAttribute"), new Object[]{_comboboxName, item}), Bundle
                                .getMessage("DeleteAttribute?"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new Object[]{Bundle.getMessage("ButtonYes"),
                                Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonCancel")},
                        Bundle
                                .getMessage("ButtonYes"));
                if (results == JOptionPane.YES_OPTION) {
                    deleteItemFromCombobox((String) comboBox.getSelectedItem());
                }
                if (results == JOptionPane.CANCEL_OPTION || results == JOptionPane.CLOSED_OPTION) {
                    cancel = true;
                }
            }
        }
    }

    boolean deleteUnused = false;
    boolean cancel = false;

    public void deleteUnusedAttributes() {
        if (!showQuanity) {
            toggleShowQuanity();
        }
        deleteUnused = true;
        cancel = false;
        int items = comboBox.getItemCount() - 1;
        for (int i = items; i >= 0; i--) {
            comboBox.setSelectedIndex(i);
        }
        deleteUnused = false; // done
        comboBox.setSelectedIndex(0); // update count
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarColors.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarLengths.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        carManager.removePropertyChangeListener(this);
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
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarTypes.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarColors.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarLengths.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarOwners.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarManager.KERNEL_LISTLENGTH_CHANGED_PROPERTY)) {
            carManager.updateKernelComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateCarQuanity();
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
        log.debug("CarAttribute firePropertyChange {}", p);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarAttributeEditFrame.class);
}
