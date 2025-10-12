package jmri.jmrit.operations.locations.tools;

import java.awt.*;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of track loads
 *
 * @author Dan Boudreau Copyright (C) 2013, 2014, 2015, 2023
 * 
 */
public class TrackLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    private static boolean loadAndType = false;
    private static boolean shipLoadAndType = false;

    Location _location = null;
    Track _track = null;
    String _type = "";
    JMenu _toolMenu = null;

    // panels
    JPanel pLoadControls = new JPanel();
    JPanel panelLoads = new JPanel();
    JScrollPane paneLoads = new JScrollPane(panelLoads);

    JPanel pShipLoadControls = new JPanel();
    JPanel panelShipLoads = new JPanel();
    JScrollPane paneShipLoadControls;
    JScrollPane paneShipLoads = new JScrollPane(panelShipLoads);

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    JButton addLoadButton = new JButton(Bundle.getMessage("AddLoad"));
    JButton deleteLoadButton = new JButton(Bundle.getMessage("DeleteLoad"));
    JButton deleteAllLoadsButton = new JButton(Bundle.getMessage("DeleteAll"));

    JButton addShipLoadButton = new JButton(Bundle.getMessage("AddLoad"));
    JButton deleteShipLoadButton = new JButton(Bundle.getMessage("DeleteLoad"));
    JButton deleteAllShipLoadsButton = new JButton(Bundle.getMessage("DeleteAll"));

    // check boxes
    JCheckBox loadAndTypeCheckBox = new JCheckBox(Bundle.getMessage("TypeAndLoad"));
    JCheckBox shipLoadAndTypeCheckBox = new JCheckBox(Bundle.getMessage("TypeAndLoad"));
    JCheckBox holdCars = new JCheckBox(Bundle.getMessage("HoldCarsWithCustomLoads"));
    JCheckBox disableLoadChange = new JCheckBox(Bundle.getMessage("DisableLoadChange"));
    JCheckBox quickLoadService = new JCheckBox(Bundle.getMessage("QuickLoadService"));

    // radio buttons
    JRadioButton loadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton loadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton loadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    JRadioButton shipLoadNameAll = new JRadioButton(Bundle.getMessage("ShipsAllLoads"));
    JRadioButton shipLoadNameInclude = new JRadioButton(Bundle.getMessage("ShipOnly"));
    JRadioButton shipLoadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    // combo box
    JComboBox<String> comboBoxLoads = InstanceManager.getDefault(CarLoads.class).getComboBox(null);
    JComboBox<String> comboBoxShipLoads = InstanceManager.getDefault(CarLoads.class).getComboBox(null);
    JComboBox<String> comboBoxTypes = InstanceManager.getDefault(CarTypes.class).getComboBox();
    JComboBox<String> comboBoxShipTypes = InstanceManager.getDefault(CarTypes.class).getComboBox();

    JTextField factorTextField = new JTextField(5);

    // labels
    JLabel trackName = new JLabel();
    JLabel factor = new JLabel(Bundle.getMessage("ScheduleFactor"));

    public static final String DISPOSE = "dispose"; // NOI18N
    public static final int MAX_NAME_LENGTH = Control.max_len_string_track_name;

    public TrackLoadEditFrame() {
        super(Bundle.getMessage("TitleEditTrackLoads"));
    }

    public void initComponents(Location location, Track track) {
        _location = location;
        _track = track;

        // property changes
        _location.addPropertyChangeListener(this);
        // listen for car load name and type changes
        InstanceManager.getDefault(CarLoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        // Layout the panel by rows
        // row 1
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setMaximumSize(new Dimension(2000, 250));

        // row 1a
        JPanel pTrackName = new JPanel();
        pTrackName.setLayout(new GridBagLayout());
        pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        addItem(pTrackName, trackName, 0, 0);

        // row 1b
        JPanel pLocationName = new JPanel();
        pLocationName.setLayout(new GridBagLayout());
        pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        addItem(pLocationName, new JLabel(_location.getName()), 0, 0);

        p1.add(pTrackName);
        p1.add(pLocationName);

        // row 3
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        JScrollPane pane3 = new JScrollPane(p3);
        pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LoadsTrack")));
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

        // row 6
        JPanel p6 = new JPanel();
        p6.setLayout(new BoxLayout(p6, BoxLayout.Y_AXIS));
        paneShipLoadControls = new JScrollPane(p6);
        paneShipLoadControls.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ShipLoadsTrack")));
        paneShipLoadControls.setMaximumSize(new Dimension(2000, 400));

        JPanel pShipLoadRadioButtons = new JPanel();
        pShipLoadRadioButtons.setLayout(new FlowLayout());

        pShipLoadRadioButtons.add(shipLoadNameAll);
        pShipLoadRadioButtons.add(shipLoadNameInclude);
        pShipLoadRadioButtons.add(shipLoadNameExclude);
        pShipLoadRadioButtons.add(shipLoadAndTypeCheckBox);

        pShipLoadControls.setLayout(new FlowLayout());

        pShipLoadControls.add(comboBoxShipTypes);
        pShipLoadControls.add(comboBoxShipLoads);
        pShipLoadControls.add(addShipLoadButton);
        pShipLoadControls.add(deleteShipLoadButton);
        pShipLoadControls.add(deleteAllShipLoadsButton);

        pShipLoadControls.setVisible(false);

        p6.add(pShipLoadRadioButtons);
        p6.add(pShipLoadControls);

        // row 7
        panelShipLoads.setLayout(new GridBagLayout());
        paneShipLoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Loads")));

        ButtonGroup shipLoadGroup = new ButtonGroup();
        shipLoadGroup.add(shipLoadNameAll);
        shipLoadGroup.add(shipLoadNameInclude);
        shipLoadGroup.add(shipLoadNameExclude);
        
        JPanel pOptions = new JPanel();
        pOptions.setLayout(new GridBagLayout());
        pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        pOptions.setMaximumSize(new Dimension(2000, 400));
        addItemLeft(pOptions, disableLoadChange, 0, 0);
        addItemLeft(pOptions, quickLoadService, 0, 1);
        addItemLeft(pOptions, holdCars, 0, 2);
        disableLoadChange.setToolTipText(Bundle.getMessage("DisableLoadChangeTip"));
        quickLoadService.setToolTipText(Bundle.getMessage("QuickLoadServiceTip"));
        holdCars.setToolTipText(Bundle.getMessage("HoldCarsWithCustomLoadsTip"));
        
        addItemLeft(pOptions, factor, 0, 3);
        addItemLeft(pOptions, factorTextField, 1, 3);
        factorTextField.setToolTipText(Bundle.getMessage("TipScheduleFactor"));

        // row 12
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        panelButtons.setBorder(BorderFactory.createTitledBorder(""));
        panelButtons.setMaximumSize(new Dimension(2000, 200));

        // row 13
        addItem(panelButtons, saveButton, 0, 0);

        getContentPane().add(p1);
        getContentPane().add(pane3);
        getContentPane().add(paneLoads);
        getContentPane().add(paneShipLoadControls);
        getContentPane().add(paneShipLoads);
        getContentPane().add(pOptions);
        getContentPane().add(panelButtons);

        // setup buttons
        addButtonAction(saveButton);

        addButtonAction(deleteLoadButton);
        addButtonAction(deleteAllLoadsButton);
        addButtonAction(addLoadButton);

        addButtonAction(deleteShipLoadButton);
        addButtonAction(deleteAllShipLoadsButton);
        addButtonAction(addShipLoadButton);

        addRadioButtonAction(loadNameAll);
        addRadioButtonAction(loadNameInclude);
        addRadioButtonAction(loadNameExclude);

        addRadioButtonAction(shipLoadNameAll);
        addRadioButtonAction(shipLoadNameInclude);
        addRadioButtonAction(shipLoadNameExclude);

        addComboBoxAction(comboBoxTypes);
        addComboBoxAction(comboBoxShipTypes);

        paneShipLoadControls.setVisible(false);
        paneShipLoads.setVisible(false);
        pOptions.setVisible(false);

        // load fields and enable buttons
        if (_track != null) {
            _track.addPropertyChangeListener(this);
            trackName.setText(_track.getName());
            // only show ship loads for staging tracks
            paneShipLoadControls.setVisible(_track.isStaging());
            paneShipLoads.setVisible(_track.isStaging());
            pOptions.setVisible(_track.isSpur());
            holdCars.setEnabled(_track.getSchedule() != null && _track.getAlternateTrack() != null);
            holdCars.setSelected(_track.isHoldCarsWithCustomLoadsEnabled());
            disableLoadChange.setSelected(_track.isDisableLoadChangeEnabled());
            quickLoadService.setSelected(_track.isQuickServiceEnabled());
            factor.setEnabled(_track.getSchedule() != null);
            factorTextField.setEnabled(_track.getSchedule() != null);
            factorTextField.setText(Integer.toString(_track.getReservationFactor()));
            updateButtons(true);
        } else {
            updateButtons(false);
        }

        updateTypeComboBoxes();
        updateLoadComboBoxes();
        updateLoadNames();
        updateShipLoadNames();

        loadAndTypeCheckBox.setSelected(loadAndType);
        shipLoadAndTypeCheckBox.setSelected(shipLoadAndType);
        
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_LoadOptions", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight400));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (_track == null) {
            return;
        }
        if (ae.getSource() == saveButton) {
            log.debug("track save button activated");
            save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == addLoadButton) {
            String loadName = (String) comboBoxLoads.getSelectedItem();
            if (loadAndTypeCheckBox.isSelected()) {
                loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
            }
            _track.addLoadName(loadName);
            selectNextItemComboBox(comboBoxLoads);
        }
        if (ae.getSource() == deleteLoadButton) {
            String loadName = (String) comboBoxLoads.getSelectedItem();
            if (loadAndTypeCheckBox.isSelected()) {
                loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
            }
            _track.deleteLoadName(loadName);
            selectNextItemComboBox(comboBoxLoads);
        }
        if (ae.getSource() == deleteAllLoadsButton) {
            deleteAllLoads();
        }
        if (ae.getSource() == addShipLoadButton) {
            String loadName = (String) comboBoxShipLoads.getSelectedItem();
            if (shipLoadAndTypeCheckBox.isSelected()) {
                loadName = comboBoxShipTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
            }
            _track.addShipLoadName(loadName);
            selectNextItemComboBox(comboBoxShipLoads);
        }
        if (ae.getSource() == deleteShipLoadButton) {
            String loadName = (String) comboBoxShipLoads.getSelectedItem();
            if (shipLoadAndTypeCheckBox.isSelected()) {
                loadName = comboBoxShipTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
            }
            _track.deleteShipLoadName(loadName);
            selectNextItemComboBox(comboBoxShipLoads);
        }
        if (ae.getSource() == deleteAllShipLoadsButton) {
            deleteAllShipLoads();
        }
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected void save() {
        checkForErrors();
        _track.setHoldCarsWithCustomLoadsEnabled(holdCars.isSelected());
        _track.setDisableLoadChangeEnabled(disableLoadChange.isSelected());
        _track.setQuickServiceEnabled(quickLoadService.isSelected());
        saveReservationFactor();
        // save the last state of the "Use car type and load" checkbox
        loadAndType = loadAndTypeCheckBox.isSelected();
        shipLoadAndType = shipLoadAndTypeCheckBox.isSelected();
        // save location file
        OperationsXml.save();
    }

    /*
     * percentage from staging
     */
    private void saveReservationFactor() {
        boolean okay = false;
        try {
            int factor = Integer.parseInt(factorTextField.getText());
            if (0 <= factor && factor <= 1000) {
                okay = true;
            }
        } catch (NumberFormatException e) {
            // do nothing
        }
        if (okay) {
            _track.setReservationFactor(Integer.parseInt(factorTextField.getText()));
        } else {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("FactorMustBeNumber"),
                    Bundle.getMessage("ErrorFactor"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    protected void updateButtons(boolean enabled) {
        saveButton.setEnabled(enabled);

        loadNameAll.setEnabled(enabled);
        loadNameInclude.setEnabled(enabled);
        loadNameExclude.setEnabled(enabled);
        loadAndTypeCheckBox.setEnabled(enabled);

        // enable ship options if any of the three generate loads from staging is selected
        // or if there are any ship load restrictions for this track
        boolean en = enabled
                && (_track.isAddCustomLoadsAnyStagingTrackEnabled() || _track.isAddCustomLoadsAnySpurEnabled() || _track
                .isAddCustomLoadsEnabled() || !_track.getShipLoadOption().equals(Track.ALL_LOADS));

        shipLoadNameAll.setEnabled(en);
        shipLoadNameInclude.setEnabled(en);
        shipLoadNameExclude.setEnabled(en);
        shipLoadAndTypeCheckBox.setEnabled(en);

        addShipLoadButton.setEnabled(en);
        deleteShipLoadButton.setEnabled(en);
        deleteAllShipLoadsButton.setEnabled(en);

        comboBoxShipLoads.setEnabled(en);
        comboBoxShipTypes.setEnabled(en);
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (ae.getSource() == loadNameAll) {
            _track.setLoadOption(Track.ALL_LOADS);
        }
        if (ae.getSource() == loadNameInclude) {
            _track.setLoadOption(Track.INCLUDE_LOADS);
        }
        if (ae.getSource() == loadNameExclude) {
            _track.setLoadOption(Track.EXCLUDE_LOADS);
        }
        if (ae.getSource() == shipLoadNameAll) {
            _track.setShipLoadOption(Track.ALL_LOADS);
        }
        if (ae.getSource() == shipLoadNameInclude) {
            _track.setShipLoadOption(Track.INCLUDE_LOADS);
        }
        if (ae.getSource() == shipLoadNameExclude) {
            _track.setShipLoadOption(Track.EXCLUDE_LOADS);
        }
    }

    // Car type combo box has been changed, show loads associated with this car type
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        updateLoadComboBoxes();
    }

    private void updateLoadComboBoxes() {
        String carType = (String) comboBoxTypes.getSelectedItem();
        InstanceManager.getDefault(CarLoads.class).updateComboBox(carType, comboBoxLoads);
        carType = (String) comboBoxShipTypes.getSelectedItem();
        InstanceManager.getDefault(CarLoads.class).updateComboBox(carType, comboBoxShipLoads);
    }

    private void updateLoadNames() {
        log.debug("Update load names");
        panelLoads.removeAll();
        if (_track != null) {
            // set radio button
            loadNameAll.setSelected(_track.getLoadOption().equals(Track.ALL_LOADS));
            loadNameInclude.setSelected(_track.getLoadOption().equals(Track.INCLUDE_LOADS));
            loadNameExclude.setSelected(_track.getLoadOption().equals(Track.EXCLUDE_LOADS));

            pLoadControls.setVisible(!loadNameAll.isSelected());

            if (!loadNameAll.isSelected()) {
                int x = 0;
                int y = 0; // vertical position in panel

                int numberOfLoads = getNumberOfCheckboxesPerLine() / 2 + 1;
                for (String loadName : _track.getLoadNames()) {
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

    private void updateShipLoadNames() {
        log.debug("Update ship load names");
        panelShipLoads.removeAll();
        if (_track != null) {
            // set radio button
            shipLoadNameAll.setSelected(_track.getShipLoadOption().equals(Track.ALL_LOADS));
            shipLoadNameInclude.setSelected(_track.getShipLoadOption().equals(Track.INCLUDE_LOADS));
            shipLoadNameExclude.setSelected(_track.getShipLoadOption().equals(Track.EXCLUDE_LOADS));

            pShipLoadControls.setVisible(!shipLoadNameAll.isSelected());

            if (!shipLoadNameAll.isSelected()) {
                int x = 0;
                int y = 0; // vertical position in panel

                int numberOfLoads = getNumberOfCheckboxesPerLine() / 2 + 1;
                for (String loadName : _track.getShipLoadNames()) {
                    JLabel load = new JLabel();
                    load.setText(loadName);
                    addItemTop(panelShipLoads, load, x++, y);
                    // limit the number of loads per line
                    if (x > numberOfLoads) {
                        y++;
                        x = 0;
                    }
                }
                revalidate();
            }
        } else {
            shipLoadNameAll.setSelected(true);
        }
        panelShipLoads.repaint();
        panelShipLoads.revalidate();
    }

    private void deleteAllLoads() {
        if (_track != null) {
            for (String loadName : _track.getLoadNames()) {
                _track.deleteLoadName(loadName);
            }
        }
    }

    private void deleteAllShipLoads() {
        if (_track != null) {
            for (String loadName : _track.getShipLoadNames()) {
                _track.deleteShipLoadName(loadName);
            }
        }
    }

    private void updateTypeComboBoxes() {
        InstanceManager.getDefault(CarTypes.class).updateComboBox(comboBoxTypes);
        // remove car types not serviced by this location and track
        for (int i = comboBoxTypes.getItemCount() - 1; i >= 0; i--) {
            String type = comboBoxTypes.getItemAt(i);
            if (_track != null && !_track.isTypeNameAccepted(type)) {
                comboBoxTypes.removeItem(type);
            }
        }
        InstanceManager.getDefault(CarTypes.class).updateComboBox(comboBoxShipTypes);
        // remove car types not serviced by this location and track
        for (int i = comboBoxShipTypes.getItemCount() - 1; i >= 0; i--) {
            String type = comboBoxShipTypes.getItemAt(i);
            if (_track != null && !_track.isTypeNameAccepted(type)) {
                comboBoxShipTypes.removeItem(type);
            }
        }
    }

    private void checkForErrors() {
        if (_track.getLoadOption().equals(Track.INCLUDE_LOADS) && _track.getLoadNames().length == 0
                || _track.getShipLoadOption().equals(Track.INCLUDE_LOADS) && _track.getShipLoadNames().length == 0) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorNeedLoads"), Bundle.getMessage("ErrorNoLoads"),
                    JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        _location.removePropertyChangeListener(this);
        InstanceManager.getDefault(CarLoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N
        }
        if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)
                || e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
            updateTypeComboBoxes();
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)
                || e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
            updateLoadComboBoxes();
            updateLoadNames();
            updateShipLoadNames();
        }
        if (e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY)) {
            updateLoadNames();
            updateShipLoadNames();
        }
        if (_track != null) {
            if (e.getPropertyName().equals(Track.LOAD_OPTIONS_CHANGED_PROPERTY)) {
                updateButtons(true);
                disableLoadChange.setSelected(_track.isDisableLoadChangeEnabled());
                quickLoadService.setSelected(_track.isQuickServiceEnabled());
            }
            if (e.getPropertyName().equals(Track.HOLD_CARS_CHANGED_PROPERTY)) {
                holdCars.setSelected(_track.isHoldCarsWithCustomLoadsEnabled());
            }
            if (e.getPropertyName().equals(Track.ALTERNATE_TRACK_CHANGED_PROPERTY) ||
                    e.getPropertyName().equals(Track.SCHEDULE_ID_CHANGED_PROPERTY)) {
                holdCars.setEnabled(_track.getSchedule() != null && _track.getAlternateTrack() != null);
            }
            if (e.getPropertyName().equals(Track.TRACK_FACTOR_CHANGED_PROPERTY)) {
                factorTextField.setText(Integer.toString(_track.getReservationFactor()));
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackLoadEditFrame.class);
}
