package jmri.jmrit.operations.trains;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of a train's load options
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class TrainLoadOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    private static boolean loadAndType = false;

    Train _train = null;

    JPanel pLoadControls = new JPanel();
    JPanel panelLoads = new JPanel();
    JScrollPane paneLoads = new JScrollPane(panelLoads);

    // labels
    JLabel trainName = new JLabel();
    JLabel trainDescription = new JLabel();

    // major buttons
    JButton addLoadButton = new JButton(Bundle.getMessage("AddLoad"));
    JButton deleteLoadButton = new JButton(Bundle.getMessage("DeleteLoad"));
    JButton deleteAllLoadsButton = new JButton(Bundle.getMessage("DeleteAll"));
    JButton saveTrainButton = new JButton(Bundle.getMessage("SaveTrain"));

    // radio buttons
    JRadioButton loadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton loadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton loadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    ButtonGroup loadGroup = new ButtonGroup();

    // check boxes
    JCheckBox loadAndTypeCheckBox = new JCheckBox(Bundle.getMessage("TypeAndLoad"));

    // text field
    // combo boxes
    JComboBox<String> comboBoxTypes = InstanceManager.getDefault(CarTypes.class).getComboBox();
    JComboBox<String> comboBoxLoads = InstanceManager.getDefault(CarLoads.class).getComboBox(null);

    public static final String DISPOSE = "dispose"; // NOI18N

    public TrainLoadOptionsFrame() {
        super(Bundle.getMessage("MenuItemLoadOptions"));
    }

    public void initComponents(TrainEditFrame parent) {

        parent.setChildFrame(this);
        _train = parent._train;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setMaximumSize(new Dimension(2000, 250));

        // Layout the panel by rows
        // row 1a
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, trainName, 0, 0);

        // row 1b
        JPanel pDesc = new JPanel();
        pDesc.setLayout(new GridBagLayout());
        pDesc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
        addItem(pDesc, trainDescription, 0, 0);

        p1.add(pName);
        p1.add(pDesc);

        // row 3
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        JScrollPane pane3 = new JScrollPane(p3);
        pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LoadsTrain")));
        pane3.setMaximumSize(new Dimension(2000, 400));

        JPanel pLoadRadioButtons = new JPanel();
        pLoadRadioButtons.setLayout(new FlowLayout());

        pLoadRadioButtons.add(loadNameAll);
        pLoadRadioButtons.add(loadNameInclude);
        pLoadRadioButtons.add(loadNameExclude);
        pLoadRadioButtons.add(loadAndTypeCheckBox);

        pLoadControls.setLayout(new FlowLayout());

        pLoadControls.add(comboBoxTypes);
        pLoadControls.add(comboBoxLoads);
        pLoadControls.add(addLoadButton);
        pLoadControls.add(deleteLoadButton);
        pLoadControls.add(deleteAllLoadsButton);

        pLoadControls.setVisible(false);

        p3.add(pLoadRadioButtons);
        p3.add(pLoadControls);

        // row 4
        panelLoads.setLayout(new GridBagLayout());
        paneLoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Loads")));

        ButtonGroup loadGroup = new ButtonGroup();
        loadGroup.add(loadNameAll);
        loadGroup.add(loadNameInclude);
        loadGroup.add(loadNameExclude);

        // row 12
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        panelButtons.setBorder(BorderFactory.createTitledBorder(""));
        panelButtons.setMaximumSize(new Dimension(2000, 200));

        // row 13
        addItem(panelButtons, saveTrainButton, 0, 0);

        getContentPane().add(p1);
        getContentPane().add(pane3);
        getContentPane().add(paneLoads);
        getContentPane().add(panelButtons);

        // setup buttons
        addButtonAction(saveTrainButton);

        addButtonAction(deleteLoadButton);
        addButtonAction(deleteAllLoadsButton);
        addButtonAction(addLoadButton);

        addRadioButtonAction(loadNameAll);
        addRadioButtonAction(loadNameInclude);
        addRadioButtonAction(loadNameExclude);

        addComboBoxAction(comboBoxTypes);

        if (_train != null) {
            trainName.setText(_train.getName());
            trainDescription.setText(_train.getDescription());
            updateButtons(true);
            // listen for train changes
            _train.addPropertyChangeListener(this);
        } else {
            updateButtons(false);
        }
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainLoadOptions", true); // NOI18N
        updateTypeComboBoxes();
        updateLoadComboBoxes();
        updateLoadNames();

        // get notified if car roads, loads, and owners gets modified
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarLoads.class).addPropertyChangeListener(this);
        loadAndTypeCheckBox.setSelected(loadAndType);

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight400));
    }

    // Save
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (_train != null) {
            if (ae.getSource() == saveTrainButton) {
                log.debug("train save button activated");
                saveTrain();
            }
            if (ae.getSource() == addLoadButton) {
                String loadName = (String) comboBoxLoads.getSelectedItem();
                if (loadAndTypeCheckBox.isSelected()) {
                    loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
                }
                if (_train.addLoadName(loadName)) {
                    updateLoadNames();
                }
                selectNextItemComboBox(comboBoxLoads);
            }
            if (ae.getSource() == deleteLoadButton) {
                String loadName = (String) comboBoxLoads.getSelectedItem();
                if (loadAndTypeCheckBox.isSelected()) {
                    loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
                }
                if (_train.deleteLoadName(loadName)) {
                    updateLoadNames();
                }
                selectNextItemComboBox(comboBoxLoads);
            }
            if (ae.getSource() == deleteAllLoadsButton) {
                deleteAllLoads();
            }
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (_train != null) {
            if (ae.getSource() == loadNameAll) {
                _train.setLoadOption(Train.ALL_LOADS);
                updateLoadNames();
            }
            if (ae.getSource() == loadNameInclude) {
                _train.setLoadOption(Train.INCLUDE_LOADS);
                updateLoadNames();
            }
            if (ae.getSource() == loadNameExclude) {
                _train.setLoadOption(Train.EXCLUDE_LOADS);
                updateLoadNames();
            }
        }
    }

    // Car type combo box has been changed, show loads associated with this car type
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == comboBoxTypes) {
            updateLoadComboBoxes();
        }
    }

    protected void updateButtons(boolean enabled) {
        saveTrainButton.setEnabled(enabled);

        loadNameAll.setEnabled(enabled);
        loadNameInclude.setEnabled(enabled);
        loadNameExclude.setEnabled(enabled);
        loadAndTypeCheckBox.setEnabled(enabled);
    }

    private void updateLoadNames() {
        log.debug("Update load names");
        panelLoads.removeAll();
        if (_train != null) {
            // set radio button
            loadNameAll.setSelected(_train.getLoadOption().equals(Train.ALL_LOADS));
            loadNameInclude.setSelected(_train.getLoadOption().equals(Train.INCLUDE_ROADS));
            loadNameExclude.setSelected(_train.getLoadOption().equals(Train.EXCLUDE_ROADS));

            pLoadControls.setVisible(!loadNameAll.isSelected());

            if (!loadNameAll.isSelected()) {
                int x = 0;
                int y = 0; // vertical position in panel

                int numberOfLoads = getNumberOfCheckboxesPerLine() / 2 + 1;
                for (String loadName : _train.getLoadNames()) {
                    JLabel load = new JLabel();
                    load.setText(loadName);
                    addItemTop(panelLoads, load, x++, y);
                    // limit the number of loads per line
                    if (x > numberOfLoads) {
                        y++;
                        x = 0;
                    }
                }
                revalidate();
            }
        } else {
            loadNameAll.setSelected(true);
        }
        panelLoads.repaint();
        panelLoads.revalidate();
    }

    private void deleteAllLoads() {
        if (_train != null) {
            for (String load : _train.getLoadNames()) {
                _train.deleteLoadName(load);
            }
        }
        updateLoadNames();
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    private void saveTrain() {
        // save the last state of the "Use car type and load" checkbox
        loadAndType = loadAndTypeCheckBox.isSelected();
        OperationsXml.save();
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    private void updateTypeComboBoxes() {
        InstanceManager.getDefault(CarTypes.class).updateComboBox(comboBoxTypes);
        // remove types not serviced by this train
        for (int i = comboBoxTypes.getItemCount() - 1; i >= 0; i--) {
            String type = comboBoxTypes.getItemAt(i);
            if (_train != null && !_train.acceptsTypeName(type)) {
                comboBoxTypes.removeItem(type);
            }
        }
    }

    private void updateLoadComboBoxes() {
        String carType = (String) comboBoxTypes.getSelectedItem();
        InstanceManager.getDefault(CarLoads.class).updateComboBox(carType, comboBoxLoads);
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarLoads.class).removePropertyChangeListener(this);
        if (_train != null) {
            _train.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)
                || e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
            updateLoadComboBoxes();
            updateLoadNames();
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)) {
            updateTypeComboBoxes();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainLoadOptionsFrame.class);
}
