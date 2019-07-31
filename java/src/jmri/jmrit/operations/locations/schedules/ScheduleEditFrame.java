package jmri.jmrit.operations.locations.schedules;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.JTablePersistenceManager;

/**
 * Frame for user edit of a schedule
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011
 */
public class ScheduleEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    ScheduleTableModel scheduleModel = new ScheduleTableModel();
    JTable scheduleTable = new JTable(scheduleModel);
    JScrollPane schedulePane;

    ScheduleManager manager;
    LocationManagerXml managerXml;

    Schedule _schedule = null;
    ScheduleItem _scheduleItem = null;
    Location _location = null;
    Track _track = null;

    // labels
    // major buttons
    JButton addTypeButton = new JButton(Bundle.getMessage("AddType"));
    JButton saveScheduleButton = new JButton(Bundle.getMessage("SaveSchedule"));
    JButton deleteScheduleButton = new JButton(Bundle.getMessage("DeleteSchedule"));
    JButton addScheduleButton = new JButton(Bundle.getMessage("AddSchedule"));

    // check boxes
    JCheckBox checkBox;

    // radio buttons
    JRadioButton addLocAtTop = new JRadioButton(Bundle.getMessage("Top"));
    JRadioButton addLocAtBottom = new JRadioButton(Bundle.getMessage("Bottom"));
    JRadioButton sequentialRadioButton = new JRadioButton(Bundle.getMessage("Sequential"));
    JRadioButton matchRadioButton = new JRadioButton(Bundle.getMessage("Match"));

    // text field
    JTextField scheduleNameTextField = new JTextField(20);
    JTextField commentTextField = new JTextField(35);

    // combo boxes
    JComboBox<String> typeBox = new JComboBox<>();

    public static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;
    public static final String NAME = Bundle.getMessage("Name");
    public static final String DISPOSE = "dispose"; // NOI18N

    public ScheduleEditFrame(Schedule schedule, Track track) {
        super();

        _schedule = schedule;
        _location = track.getLocation();
        _track = track;

        // load managers
        manager = InstanceManager.getDefault(ScheduleManager.class);
        managerXml = InstanceManager.getDefault(LocationManagerXml.class);

        // Set up the jtable in a Scroll Pane..
        schedulePane = new JScrollPane(scheduleTable);
        schedulePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scheduleModel.initTable(this, scheduleTable, schedule, _location, _track);
        if (_schedule != null) {
            scheduleNameTextField.setText(_schedule.getName());
            commentTextField.setText(_schedule.getComment());
            setTitle(MessageFormat.format(Bundle.getMessage("TitleScheduleEdit"),
                    new Object[]{_track.getName()}));
            enableButtons(true);
        } else {
            setTitle(MessageFormat.format(Bundle.getMessage("TitleScheduleAdd"),
                    new Object[]{_track.getName()}));
            enableButtons(false);
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMinimumSize(new Dimension(300,
                3 * scheduleNameTextField.getPreferredSize().height));
        p1Pane.setMaximumSize(new Dimension(2000, 200));

        // row 1a name
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, scheduleNameTextField, 0, 0);

        // row 1b comment
        JPanel pC = new JPanel();
        pC.setLayout(new GridBagLayout());
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pC, commentTextField, 0, 0);

        // row 1c mode
        JPanel pMode = new JPanel();
        pMode.setLayout(new GridBagLayout());
        pMode.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScheduleMode")));
        addItem(pMode, sequentialRadioButton, 0, 0);
        addItem(pMode, matchRadioButton, 1, 0);

        sequentialRadioButton.setToolTipText(Bundle.getMessage("TipSequential"));
        matchRadioButton.setToolTipText(Bundle.getMessage("TipMatch"));
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(sequentialRadioButton);
        modeGroup.add(matchRadioButton);

        sequentialRadioButton.setSelected(_track.getScheduleMode() == Track.SEQUENTIAL);
        matchRadioButton.setSelected(_track.getScheduleMode() == Track.MATCH);
        scheduleModel.setMatchMode(_track.getScheduleMode() == Track.MATCH);

        p1.add(pName);
        p1.add(pC);
        p1.add(pMode);

        // row 2
        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        p3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddItem")));
        addItem(p3, typeBox, 0, 1);
        addItem(p3, addTypeButton, 1, 1);
        addItem(p3, addLocAtTop, 2, 1);
        addItem(p3, addLocAtBottom, 3, 1);
        ButtonGroup group = new ButtonGroup();
        group.add(addLocAtTop);
        group.add(addLocAtBottom);
        addLocAtBottom.setSelected(true);

        p3.setMaximumSize(new Dimension(2000, 200));

        // row 11 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        pB.setBorder(BorderFactory.createTitledBorder(""));
        pB.setMaximumSize(new Dimension(2000, 200));

        // row 13
        addItem(pB, deleteScheduleButton, 0, 0);
        addItem(pB, addScheduleButton, 1, 0);
        addItem(pB, saveScheduleButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(schedulePane);
        getContentPane().add(p3);
        getContentPane().add(pB);

        // set up buttons
        addButtonAction(addTypeButton);
        addButtonAction(deleteScheduleButton);
        addButtonAction(addScheduleButton);
        addButtonAction(saveScheduleButton);

        // set up radio buttons
        addRadioButtonAction(sequentialRadioButton);
        addRadioButtonAction(matchRadioButton);

        // set up combobox
        loadTypeComboBox();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(toolMenu);
        toolMenu.add(new ScheduleCopyAction(schedule));
        toolMenu.add(new ScheduleOptionsAction(this));
        toolMenu.add(new ScheduleResetHitsAction(schedule));
        toolMenu.add(new SchedulesByLoadAction(Bundle.getMessage("MenuItemShowSchedulesByLoad")));
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Schedules", true); // NOI18N

        // get notified if car types or roads are changed
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        _location.addPropertyChangeListener(this);
        _track.addPropertyChangeListener(this);

        // set frame size and schedule for display
        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight400));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addTypeButton) {
            addNewScheduleItem();
        }
        if (ae.getSource() == saveScheduleButton) {
            log.debug("schedule save button activated");
            Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
            if (_schedule == null && schedule == null) {
                saveNewSchedule();
            } else {
                if (schedule != null && schedule != _schedule) {
                    reportScheduleExists(Bundle.getMessage("save"));
                    return;
                }
                saveSchedule();
            }
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteScheduleButton) {
            log.debug("schedule delete button activated");
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
                    Bundle.getMessage("DoYouWantToDeleteSchedule"),
                    new Object[]{scheduleNameTextField.getText()}), Bundle
                            .getMessage("DeleteSchedule?"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
            if (schedule == null) {
                return;
            }

            if (_track != null) {
                _track.setScheduleId(Track.NONE);
            }

            manager.deregister(schedule);
            _schedule = null;

            enableButtons(false);
            // save schedule file
            OperationsXml.save();
        }
        if (ae.getSource() == addScheduleButton) {
            Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
            if (schedule != null) {
                reportScheduleExists(Bundle.getMessage("add"));
                return;
            }
            saveNewSchedule();
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("Radio button action");
        scheduleModel.setMatchMode(ae.getSource() == matchRadioButton);
    }

    private void addNewScheduleItem() {
        if (typeBox.getSelectedItem() == null) {
            return;
        }
        // add item to this schedule
        if (addLocAtTop.isSelected()) {
            _schedule.addItem((String) typeBox.getSelectedItem(), 0);
        } else {
            _schedule.addItem((String) typeBox.getSelectedItem());
        }
        if (_track.getScheduleMode() == Track.MATCH && typeBox.getSelectedIndex() < typeBox.getItemCount() - 1) {
            typeBox.setSelectedIndex(typeBox.getSelectedIndex() + 1);
        }
    }

    private void saveNewSchedule() {
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        Schedule schedule = manager.newSchedule(scheduleNameTextField.getText());
        scheduleModel.initTable(this, scheduleTable, schedule, _location, _track);
        _schedule = schedule;
        // enable checkboxes
        enableButtons(true);
        saveSchedule();
    }

    private void saveSchedule() {
        if (!checkName(Bundle.getMessage("save"))) {
            return;
        }
        _schedule.setName(scheduleNameTextField.getText());
        _schedule.setComment(commentTextField.getText());

        if (scheduleTable.isEditing()) {
            log.debug("schedule table edit true");
            scheduleTable.getCellEditor().stopCellEditing();
            scheduleTable.clearSelection();
        }
        if (_track != null) {
            if (!_track.getScheduleId().equals(_schedule.getId())) {
                InstanceManager.getDefault(LocationManager.class).resetMoves();
            }
            _track.setSchedule(_schedule);
            if (sequentialRadioButton.isSelected()) {
                _track.setScheduleMode(Track.SEQUENTIAL);
            } else {
                _track.setScheduleMode(Track.MATCH);
            }
            // check for errors, ignore no schedule items error when creating a new schedule
            String status = _track.checkScheduleValid();
            if (_schedule.getItemsBySequenceList().size() != 0 && !status.equals(Track.SCHEDULE_OKAY)) {
                JOptionPane.showMessageDialog(this, status, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }

        // save schedule file
        OperationsXml.save();
    }

    private void loadTypeComboBox() {
        typeBox.removeAllItems();
        for (String typeName : InstanceManager.getDefault(CarTypes.class).getNames()) {
            if (_track.acceptsTypeName(typeName)) {
                typeBox.addItem(typeName);
            }
        }
    }

    /**
     *
     * @return true if name is less than 26 characters
     */
    private boolean checkName(String s) {
        if (scheduleNameTextField.getText().trim().equals("")) {
            return false;
        }
        if (scheduleNameTextField.getText().length() > MAX_NAME_LENGTH) {
            log.error("Schedule name must be less than 26 charaters");
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    Bundle.getMessage("ScheduleNameLengthMax"),
                    new Object[]{Integer.toString(MAX_NAME_LENGTH + 1)}), MessageFormat.format(
                            Bundle.getMessage("CanNotSchedule"), new Object[]{s}),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void reportScheduleExists(String s) {
        log.info("Can not " + s + ", schedule already exists");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ReportExists"),
                MessageFormat.format(Bundle.getMessage("CanNotSchedule"), new Object[]{s}),
                JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        typeBox.setEnabled(enabled);
        addTypeButton.setEnabled(enabled);
        addLocAtTop.setEnabled(enabled);
        addLocAtBottom.setEnabled(enabled);
        saveScheduleButton.setEnabled(enabled);
        deleteScheduleButton.setEnabled(enabled);
        scheduleTable.setEnabled(enabled);
        // the inverse!
        addScheduleButton.setEnabled(!enabled);
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        _location.removePropertyChangeListener(this);
        _track.removePropertyChangeListener(this);
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(scheduleTable);
        });
        scheduleModel.dispose();
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)) {
            loadTypeComboBox();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ScheduleEditFrame.class
            .getName());
}
