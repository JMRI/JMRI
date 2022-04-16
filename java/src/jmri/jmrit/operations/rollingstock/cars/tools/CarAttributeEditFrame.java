package jmri.jmrit.operations.rollingstock.cars.tools;

import java.text.MessageFormat;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.tools.LocationsByCarTypeFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockAttributeEditFrame;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.tools.TrainsByCarTypeFrame;

/**
 * Frame for editing a car attribute.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014, 2020
 */
public class CarAttributeEditFrame extends RollingStockAttributeEditFrame {

    CarManager carManager = InstanceManager.getDefault(CarManager.class);

    // incremental attributes for this frame
    public static final String COLOR = "Color";
    public static final String KERNEL = "Kernel";

    public CarAttributeEditFrame(){
    }

    /**
     * 
     * @param attribute One of the six possible attributes for a car.
     */
    public void initComponents(String attribute) {
        initComponents(attribute, NONE);
    }

    /**
     * 
     * @param attribute One of the six possible attributes for a car.
     * @param name      The name of the attribute to edit.
     */
    @Override
    public void initComponents(String attribute, String name) {
        super.initComponents(attribute, name);

        setTitle(MessageFormat.format(Bundle.getMessage("TitleCarEditAtrribute"), new Object[] { attribute }));
        carManager.addPropertyChangeListener(this);

        addComboBoxAction(comboBox);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CarAttributeAction(this));
        toolMenu.add(new CarDeleteAttributeAction(this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_EditCarAttributes", true); // NOI18N
    }

    @Override
    protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("Combo box action");
        updateCarQuanity();
    }

    @Override
    protected void deleteAttributeName(String deleteItem) {
        super.deleteAttributeName(deleteItem);
        if (_attribute.equals(TYPE)) {
            InstanceManager.getDefault(CarTypes.class).deleteName(deleteItem);
        }
        if (_attribute.equals(COLOR)) {
            InstanceManager.getDefault(CarColors.class).deleteName(deleteItem);
        }
        if (_attribute.equals(LENGTH)) {
            InstanceManager.getDefault(CarLengths.class).deleteName(deleteItem);
        }
        if (_attribute.equals(KERNEL)) {
            InstanceManager.getDefault(KernelManager.class).deleteKernel(deleteItem);
        }
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected void addAttributeName(String addItem) {
        super.addAttributeName(addItem);
        if (_attribute.equals(TYPE)) {
            InstanceManager.getDefault(CarTypes.class).addName(addItem);
            if (showDialogBox) {
                int results = JOptionPane.showOptionDialog(this, Bundle.getMessage("AddNewCarType"),
                        Bundle.getMessage("ModifyLocations"), JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new Object[] { Bundle.getMessage("ButtonYes"),
                                Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonDontShow") },
                        Bundle.getMessage("ButtonNo"));
                if (results == JOptionPane.YES_OPTION) {
                    LocationsByCarTypeFrame lf = new LocationsByCarTypeFrame();
                    lf.initComponents(addItem);
                }
                if (results == JOptionPane.CANCEL_OPTION) {
                    showDialogBox = false;
                }
                results = JOptionPane.showOptionDialog(this, Bundle.getMessage("AddNewCarType"),
                        Bundle.getMessage("ModifyTrains"), JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new Object[] { Bundle.getMessage("ButtonYes"),
                                Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonDontShow") },
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
        if (_attribute.equals(COLOR)) {
            InstanceManager.getDefault(CarColors.class).addName(addItem);
        }
        if (_attribute.equals(LENGTH)) {
            InstanceManager.getDefault(CarLengths.class).addName(addItem);
            comboBox.setSelectedItem(addItem);
        }
        if (_attribute.equals(KERNEL)) {
            InstanceManager.getDefault(KernelManager.class).newKernel(addItem);
        }
        if (_attribute.equals(OWNER)) {
            InstanceManager.getDefault(CarOwners.class).addName(addItem);
        }
    }

    @Override
    protected void replaceItem(String oldItem, String newItem) {
        super.replaceItem(oldItem, newItem);
        // replace kernel
        if (_attribute.equals(KERNEL)) {
            InstanceManager.getDefault(KernelManager.class).replaceKernelName(oldItem, newItem);
        }
        // now adjust cars, locations and trains
        if (_attribute.equals(TYPE)) {
            InstanceManager.getDefault(CarTypes.class).replaceName(oldItem, newItem);
            InstanceManager.getDefault(CarLoads.class).replaceType(oldItem, newItem);
        }
        if (_attribute.equals(LENGTH)) {
            InstanceManager.getDefault(CarLengths.class).replaceName(oldItem, newItem);
        }
        if (_attribute.equals(COLOR)) {
            InstanceManager.getDefault(CarColors.class).replaceName(oldItem, newItem);
        }
    }

    @Override
    protected void loadCombobox() {
        super.loadCombobox();
        if (_attribute.equals(TYPE)) {
            comboBox = InstanceManager.getDefault(CarTypes.class).getComboBox();
            InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        }
        if (_attribute.equals(COLOR)) {
            comboBox = InstanceManager.getDefault(CarColors.class).getComboBox();
            InstanceManager.getDefault(CarColors.class).addPropertyChangeListener(this);
        }
        if (_attribute.equals(LENGTH)) {
            comboBox = InstanceManager.getDefault(CarLengths.class).getComboBox();
            InstanceManager.getDefault(CarLengths.class).addPropertyChangeListener(this);
        }
        if (_attribute.equals(KERNEL)) {
            comboBox = InstanceManager.getDefault(KernelManager.class).getComboBox();
            InstanceManager.getDefault(KernelManager.class).addPropertyChangeListener(this);
        }
    }

    public void toggleShowQuanity() {
        showQuanity = !showQuanity;
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
            if (_attribute.equals(ROAD)) {
                if (car.getRoadName().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(TYPE)) {
                if (car.getTypeName().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(COLOR)) {
                if (car.getColor().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(LENGTH)) {
                if (car.getLength().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(OWNER)) {
                if (car.getOwner().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(KERNEL)) {
                if (car.getKernelName().equals(item)) {
                    number++;
                }
            }
        }
        quanity.setText(Integer.toString(number));
        // Tool to delete all attributes that haven't been assigned to a car
        if (number == 0 && deleteUnused) {
            // need to check if an engine is using the road name
            if (_attribute.equals(ROAD)) {
                for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList()) {
                    if (rs.getRoadName().equals(item)) {
                        log.info("Engine ({} {}) is assigned road name ({})", rs.getRoadName(), rs.getNumber(), item); // NOI18N
                        return;
                    }
                }
            }
            // need to check if an engine is using the road name
            if (_attribute.equals(OWNER)) {
                for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList()) {
                    if (rs.getOwner().equals(item)) {
                        log.info("Engine ({} {}) is assigned owner name ({})", rs.getRoadName(), rs.getNumber(), item); // NOI18N
                        return;
                    }
                }
            }
            // confirm that attribute is to be deleted
            if (!cancel) {
                int results = JOptionPane.showOptionDialog(null,
                        MessageFormat
                                .format(Bundle.getMessage("ConfirmDeleteAttribute"), new Object[] { _attribute, item }),
                        Bundle.getMessage("DeleteAttribute?"), JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new Object[] { Bundle.getMessage("ButtonYes"),
                                Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonCancel") },
                        Bundle.getMessage("ButtonYes"));
                if (results == JOptionPane.YES_OPTION) {
                    deleteAttributeName((String) comboBox.getSelectedItem());
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
        comboBox.setSelectedIndex(0);
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarColors.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarLengths.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(KernelManager.class).removePropertyChangeListener(this);
        carManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
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
        if (e.getPropertyName().equals(KernelManager.LISTLENGTH_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(KernelManager.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateCarQuanity();
        }
        super.propertyChange(e);
    }

    private final static Logger log = LoggerFactory.getLogger(CarAttributeEditFrame.class);
}
