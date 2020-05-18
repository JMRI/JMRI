package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.swing.NamedBeanComboBox;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class RouteAddFrame extends JmriJFrame {

    protected ArrayList<RouteTurnout> _turnoutList;      // array of all Turnouts
    protected ArrayList<RouteTurnout> _includedTurnoutList;

    protected ArrayList<RouteSensor> _sensorList;        // array of all Sensors
    protected ArrayList<RouteSensor> _includedSensorList;

    JTextField _systemName = new JTextField(10);
    JTextField _userName = new JTextField(22);
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";
    jmri.UserPreferencesManager pref;

    RouteTurnoutModel _routeTurnoutModel;
    JScrollPane _routeTurnoutScrollPane;
    RouteSensorModel _routeSensorModel;
    JScrollPane _routeSensorScrollPane;

    JTextField soundFile = new JTextField(20);
    JTextField scriptFile = new JTextField(20);
    NamedBeanComboBox<Sensor> turnoutsAlignedSensor;

    NamedBeanComboBox<Sensor> sensor1;

    JComboBox<String> sensor1mode = new JComboBox<>(sensorInputModes);
    NamedBeanComboBox<Sensor> sensor2;
    JComboBox<String> sensor2mode = new JComboBox<>(sensorInputModes);
    NamedBeanComboBox<Sensor> sensor3;
    JComboBox<String> sensor3mode = new JComboBox<>(sensorInputModes);

    NamedBeanComboBox<Turnout> cTurnout;
    NamedBeanComboBox<Turnout> cLockTurnout;
    JSpinner timeDelay = new JSpinner();

    JComboBox<String> cTurnoutStateBox = new JComboBox<>(turnoutInputModes);
    JComboBox<String> cLockTurnoutStateBox = new JComboBox<>(lockTurnoutInputModes);

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    JLabel nameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");

    JButton createButton = new JButton(Bundle.getMessage("ButtonCreate"));
    JButton editButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton cancelEditButton = new JButton(Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit"))); // I18N for word sequence "Cancel Edit"
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete") + " " + Bundle.getMessage("BeanNameRoute")); // I18N "Delete Route"
    JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
    JButton exportButton = new JButton(Bundle.getMessage("ButtonExport"));

    static final String createInst = Bundle.getMessage("RouteAddStatusInitial1", Bundle.getMessage("ButtonCreate")); // I18N to include original button name in help string
    static final String editInst = Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit"));
    static final String updateInst = Bundle.getMessage("RouteAddStatusInitial3", Bundle.getMessage("ButtonUpdate"));
    static final String cancelInst = Bundle.getMessage("RouteAddStatusInitial4", Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit")));

    JLabel status1 = new JLabel(createInst);
    JLabel status2 = new JLabel(editInst);

    JPanel p2xt = null;   // Turnout list table
    JPanel p2xs = null;   // Sensor list table

    Route curRoute = null;
    boolean routeDirty = false;  // true to fire reminder to save work
    boolean editMode = false;
    private boolean showAll = true;   // false indicates show only included Turnouts

    public final String LOGIX_SYS_NAME;
    public final String CONDITIONAL_SYS_PREFIX;
    protected static int ROW_HEIGHT;

    public RouteAddFrame() {
        this(Bundle.getMessage("TitleAddRoute"), false, true);
    }

    public RouteAddFrame(String name) {
        this(name,false,true);
    }

    public RouteAddFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        String logixPrefix = InstanceManager.getDefault(jmri.LogixManager.class).getSystemNamePrefix();
        LOGIX_SYS_NAME = logixPrefix + ":RTX:";
        CONDITIONAL_SYS_PREFIX = LOGIX_SYS_NAME + "C";
        setClosedString(Bundle.getMessage("Set") + " "
                + InstanceManager.turnoutManagerInstance().getClosedText());
        setThrownString(Bundle.getMessage("Set") + " "
                + InstanceManager.turnoutManagerInstance().getThrownText());
        setTurnoutInputModes(new String[]{
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getClosedText(),
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getThrownText(),
                Bundle.getMessage("OnConditionChange"),
                "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("TurnoutStateClosed"),
                "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("TurnoutStateThrown")
        });
        setLockTurnoutModes(new String[]{
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getClosedText(),
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getThrownText(),
                Bundle.getMessage("OnConditionChange")
        });
    }

    @Override
    public void initComponents() {
        super.initComponents();

        pref = InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (editMode) {
            cancelEdit();
        }
        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        _turnoutList = new ArrayList<>();
        for (Turnout t : tm.getNamedBeanSet()) {
            String systemName = t.getSystemName();
            String userName = t.getUserName();
            _turnoutList.add(new RouteTurnout(systemName, userName));
        }

        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        _sensorList = new ArrayList<>();
        for (Sensor s : sm.getNamedBeanSet()) {
            String systemName = s.getSystemName();
            String userName = s.getUserName();
            _sensorList.add(new RouteSensor(systemName, userName));
        }
        initializeIncludedList();

        turnoutsAlignedSensor = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
        sensor1 = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
        sensor2 = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
        sensor3 = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
        cTurnout = new NamedBeanComboBox<>(InstanceManager.turnoutManagerInstance());
        cLockTurnout = new NamedBeanComboBox<>(InstanceManager.turnoutManagerInstance());

        // Set combo max rows
        JComboBoxUtil.setupComboBoxMaxRows(turnoutsAlignedSensor);
        JComboBoxUtil.setupComboBoxMaxRows(sensor1);
        JComboBoxUtil.setupComboBoxMaxRows(sensor2);
        JComboBoxUtil.setupComboBoxMaxRows(sensor3);
        JComboBoxUtil.setupComboBoxMaxRows(cTurnout);
        JComboBoxUtil.setupComboBoxMaxRows(cLockTurnout);

        addHelpMenu("package.jmri.jmrit.beantable.RouteAddEdit", true);
        setLocation(100, 30);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        // add system name
        JPanel ps = new JPanel();
        ps.setLayout(new FlowLayout());
        ps.add(nameLabel);
        ps.add(_systemName);
        ps.add(_autoSystemName);
        _autoSystemName.addActionListener((ActionEvent e1) -> {
            autoSystemName();
        });
        if (pref.getSimplePreferenceState(systemNameAuto)) {
            _autoSystemName.setSelected(true);
        }
        _systemName.setToolTipText(Bundle.getMessage("TooltipRouteSystemName"));
        ps.add(fixedSystemName);
        fixedSystemName.setVisible(false);
        contentPanel.add(ps);
        // add user name
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(userLabel);
        p.add(_userName);
        _userName.setToolTipText(Bundle.getMessage("TooltipRouteUserName"));
        contentPanel.add(p);
        // add Turnout Display Choice
        JPanel py = new JPanel();
        py.add(new JLabel(Bundle.getMessage("Show") + ":"));
        selGroup = new ButtonGroup();
        allButton = new JRadioButton(Bundle.getMessage("All"), true);
        selGroup.add(allButton);
        py.add(allButton);
        allButton.addActionListener((ActionEvent e1) -> {
            // Setup for display of all Turnouts, if needed
            if (!showAll) {
                showAll = true;
                _routeTurnoutModel.fireTableDataChanged();
                _routeSensorModel.fireTableDataChanged();
            }
        });
        includedButton = new JRadioButton(Bundle.getMessage("Included"), false);
        selGroup.add(includedButton);
        py.add(includedButton);
        includedButton.addActionListener((ActionEvent e1) -> {
            // Setup for display of included Turnouts only, if needed
            if (showAll) {
                showAll = false;
                initializeIncludedList();
                _routeTurnoutModel.fireTableDataChanged();
                _routeSensorModel.fireTableDataChanged();
            }
        });
        py.add(new JLabel(Bundle.getMessage("_and_", Bundle.getMessage("Turnouts"), Bundle.getMessage("Sensors"))));
        // keys are in jmri.jmrit.Bundle
        contentPanel.add(py);

        // add Turnout table
        p2xt = new JPanel();
        JPanel p2xtSpace = new JPanel();
        p2xtSpace.setLayout(new BoxLayout(p2xtSpace, BoxLayout.Y_AXIS));
        p2xtSpace.add(new JLabel("XXX"));
        p2xt.add(p2xtSpace);

        JPanel p21t = new JPanel();
        p21t.setLayout(new BoxLayout(p21t, BoxLayout.Y_AXIS));
        p21t.add(new JLabel(Bundle.getMessage("SelectInRoute", Bundle.getMessage("Turnouts"))));
        p2xt.add(p21t);
        _routeTurnoutModel = new RouteTurnoutModel();
        JTable routeTurnoutTable = new JTable(_routeTurnoutModel);
        TableRowSorter<RouteTurnoutModel> rtSorter = new TableRowSorter<>(_routeTurnoutModel);

        // Use AlphanumComparator for SNAME and UNAME columns.  Start with SNAME sort.
        rtSorter.setComparator(RouteTurnoutModel.SNAME_COLUMN, new AlphanumComparator());
        rtSorter.setComparator(RouteTurnoutModel.UNAME_COLUMN, new AlphanumComparator());
        RowSorterUtil.setSortOrder(rtSorter, RouteTurnoutModel.SNAME_COLUMN, SortOrder.ASCENDING);

        routeTurnoutTable.setRowSorter(rtSorter);
        routeTurnoutTable.setRowSelectionAllowed(false);
        routeTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 80));

        setRowHeight(routeTurnoutTable.getRowHeight());
        JComboBox<String> stateTCombo = new JComboBox<>();
        stateTCombo.addItem(SET_TO_CLOSED);
        stateTCombo.addItem(SET_TO_THROWN);
        stateTCombo.addItem(SET_TO_TOGGLE);
        TableColumnModel routeTurnoutColumnModel = routeTurnoutTable.getColumnModel();
        TableColumn includeColumnT = routeTurnoutColumnModel.
                getColumn(RouteTurnoutModel.INCLUDE_COLUMN);
        includeColumnT.setResizable(false);
        includeColumnT.setMinWidth(50);
        includeColumnT.setMaxWidth(60);
        TableColumn sNameColumnT = routeTurnoutColumnModel.
                getColumn(RouteTurnoutModel.SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(75);
        sNameColumnT.setMaxWidth(95);
        TableColumn uNameColumnT = routeTurnoutColumnModel.
                getColumn(RouteTurnoutModel.UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(210);
        uNameColumnT.setMaxWidth(260);
        TableColumn stateColumnT = routeTurnoutColumnModel.
                getColumn(RouteTurnoutModel.STATE_COLUMN);
        stateColumnT.setCellEditor(new DefaultCellEditor(stateTCombo));
        stateColumnT.setResizable(false);
        stateColumnT.setMinWidth(90);
        stateColumnT.setMaxWidth(100);
        _routeTurnoutScrollPane = new JScrollPane(routeTurnoutTable);
        p2xt.add(_routeTurnoutScrollPane, BorderLayout.CENTER);
        contentPanel.add(p2xt);
        p2xt.setVisible(true);

        // add Sensor table
        p2xs = new JPanel();
        JPanel p2xsSpace = new JPanel();
        p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
        p2xsSpace.add(new JLabel("XXX"));
        p2xs.add(p2xsSpace);

        JPanel p21s = new JPanel();
        p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
        p21s.add(new JLabel(Bundle.getMessage("SelectInRoute", Bundle.getMessage("Sensors"))));
        p2xs.add(p21s);
        _routeSensorModel = new RouteSensorModel();
        JTable routeSensorTable = new JTable(_routeSensorModel);
        TableRowSorter<RouteSensorModel> rsSorter = new TableRowSorter<>(_routeSensorModel);

        // Use AlphanumComparator for SNAME and UNAME columns.  Start with SNAME sort.
        rsSorter.setComparator(RouteTurnoutModel.SNAME_COLUMN, new AlphanumComparator());
        rsSorter.setComparator(RouteTurnoutModel.UNAME_COLUMN, new AlphanumComparator());
        RowSorterUtil.setSortOrder(rsSorter, RouteSensorModel.SNAME_COLUMN, SortOrder.ASCENDING);
        routeSensorTable.setRowSorter(rsSorter);
        routeSensorTable.setRowSelectionAllowed(false);
        routeSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 80));
        JComboBox<String> stateSCombo = new JComboBox<>();
        stateSCombo.addItem(SET_TO_ACTIVE);
        stateSCombo.addItem(SET_TO_INACTIVE);
        stateSCombo.addItem(SET_TO_TOGGLE);
        TableColumnModel routeSensorColumnModel = routeSensorTable.getColumnModel();
        TableColumn includeColumnS = routeSensorColumnModel.
                getColumn(RouteSensorModel.INCLUDE_COLUMN);
        includeColumnS.setResizable(false);
        includeColumnS.setMinWidth(50);
        includeColumnS.setMaxWidth(60);
        TableColumn sNameColumnS = routeSensorColumnModel.
                getColumn(RouteSensorModel.SNAME_COLUMN);
        sNameColumnS.setResizable(true);
        sNameColumnS.setMinWidth(75);
        sNameColumnS.setMaxWidth(95);
        TableColumn uNameColumnS = routeSensorColumnModel.
                getColumn(RouteSensorModel.UNAME_COLUMN);
        uNameColumnS.setResizable(true);
        uNameColumnS.setMinWidth(210);
        uNameColumnS.setMaxWidth(260);
        TableColumn stateColumnS = routeSensorColumnModel.
                getColumn(RouteSensorModel.STATE_COLUMN);
        stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo));
        stateColumnS.setResizable(false);
        stateColumnS.setMinWidth(90);
        stateColumnS.setMaxWidth(100);
        _routeSensorScrollPane = new JScrollPane(routeSensorTable);
        p2xs.add(_routeSensorScrollPane, BorderLayout.CENTER);
        contentPanel.add(p2xs);
        p2xs.setVisible(true);

        // Enter filenames for sound, script
        JPanel p25 = new JPanel();
        p25.setLayout(new FlowLayout());
        p25.add(new JLabel(Bundle.getMessage("LabelPlaySound")));
        p25.add(soundFile);
        JButton ss = new JButton("..."); //NO18N
        ss.addActionListener((ActionEvent e1) -> {
            setSoundPressed();
        });
        ss.setToolTipText(Bundle.getMessage("TooltipOpenFile", Bundle.getMessage("BeanNameAudio")));
        p25.add(ss);
        p25.add(new JLabel(Bundle.getMessage("LabelRunScript")));
        p25.add(scriptFile);
        ss = new JButton("..."); //NO18N
        ss.addActionListener((ActionEvent e1) -> {
            setScriptPressed();
        });
        ss.setToolTipText(Bundle.getMessage("TooltipOpenFile", Bundle.getMessage("Script")));
        p25.add(ss);
        contentPanel.add(p25);

        //add turnouts aligned Sensor
        JPanel p27 = new JPanel();
        p27.setLayout(new FlowLayout());
        p27.add(new JLabel(Bundle.getMessage("LabelEnterSensorAligned")));
        p27.add(turnoutsAlignedSensor);
        turnoutsAlignedSensor.setAllowNull(true);
        turnoutsAlignedSensor.setSelectedItem(null);
        turnoutsAlignedSensor.setToolTipText(Bundle.getMessage("TooltipEnterSensor"));
        contentPanel.add(p27);

        // add Control Sensor table
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        JPanel p31 = new JPanel();
        p31.add(new JLabel(Bundle.getMessage("LabelEnterSensors")));
        p3.add(p31);
        JPanel p32 = new JPanel();
        //Sensor 1
        JPanel pS = new JPanel();
        pS.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BeanNameSensor") + " 1"));
        pS.add(sensor1);
        pS.add(sensor1mode);
        p32.add(pS);
        //Sensor 2
        pS = new JPanel();
        pS.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BeanNameSensor") + " 2"));
        pS.add(sensor2);
        pS.add(sensor2mode);
        p32.add(pS);
        //Sensor 3
        pS = new JPanel();
        pS.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BeanNameSensor") + " 3"));
        pS.add(sensor3);
        pS.add(sensor3mode);
        p32.add(pS);

        sensor1.setAllowNull(true);
        sensor2.setAllowNull(true);
        sensor3.setAllowNull(true);
        sensor1.setSelectedItem(null);
        sensor2.setSelectedItem(null);
        sensor3.setSelectedItem(null);
        String sensorHint = Bundle.getMessage("TooltipEnterSensors");
        sensor1.setToolTipText(sensorHint);
        sensor2.setToolTipText(sensorHint);
        sensor3.setToolTipText(sensorHint);
        p3.add(p32);
        // add control turnout
        JPanel p33 = new JPanel();
        p33.add(new JLabel(Bundle.getMessage("LabelEnterTurnout")));
        p3.add(p33);
        JPanel p34 = new JPanel();
        p34.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))));
        p34.add(cTurnout);
        cTurnout.setAllowNull(true);
        cTurnout.setSelectedItem(null);
        cTurnout.setToolTipText(Bundle.getMessage("TooltipEnterTurnout"));
        p34.add(new JLabel("   " + Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelCondition"))));
        cTurnoutStateBox.setToolTipText(Bundle.getMessage("TooltipTurnoutCondition"));
        p34.add(cTurnoutStateBox);
        p3.add(p34);
        // add additional route-specific delay
        JPanel p36 = new JPanel();
        p36.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelTurnoutDelay"))));
        timeDelay.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
        // timeDelay.setValue(0); // reset from possible previous use
        timeDelay.setPreferredSize(new JTextField(5).getPreferredSize());
        p36.add(timeDelay);
        timeDelay.setToolTipText(Bundle.getMessage("TooltipTurnoutDelay"));
        p36.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
        p3.add(p36);
        // complete this panel
        Border p3Border = BorderFactory.createEtchedBorder();
        p3.setBorder(p3Border);
        contentPanel.add(p3);

        // add lock control table
        JPanel p4 = new JPanel();
        p4.setLayout(new BoxLayout(p4, BoxLayout.Y_AXIS));
        // add lock control turnout
        JPanel p43 = new JPanel();
        p43.add(new JLabel(Bundle.getMessage("LabelLockTurnout")));
        p4.add(p43);
        JPanel p44 = new JPanel();
        p44.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))));
        p44.add(cLockTurnout);
        cLockTurnout.setAllowNull(true);
        cLockTurnout.setSelectedItem(null);
        cLockTurnout.setToolTipText(Bundle.getMessage("TooltipEnterTurnout"));
        p44.add(new JLabel("   " + Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelCondition"))));
        cLockTurnoutStateBox.setToolTipText(Bundle.getMessage("TooltipLockTurnout"));
        p44.add(cLockTurnoutStateBox);
        p4.add(p44);
        // complete this panel
        Border p4Border = BorderFactory.createEtchedBorder();
        p4.setBorder(p4Border);
        contentPanel.add(p4);

        // add notes panel
        JPanel pa = new JPanel();
        pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        status1.setFont(status1.getFont().deriveFont(0.9f * nameLabel.getFont().getSize())); // a bit smaller
        status1.setForeground(Color.gray);
        p1.add(status1);
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        status2.setFont(status1.getFont().deriveFont(0.9f * nameLabel.getFont().getSize())); // a bit smaller
        status2.setForeground(Color.gray);
        p2.add(status2);
        pa.add(p1);
        pa.add(p2);
        Border pBorder = BorderFactory.createEtchedBorder();
        pa.setBorder(pBorder);
        contentPanel.add(pa);
        // add Buttons panel
        JPanel pb = new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.TRAILING));
        // Cancel (Add) button
        pb.add(cancelButton);
        cancelButton.addActionListener(this::cancelAddPressed);
        // CancelEdit button
        pb.add(cancelEditButton);
        cancelEditButton.addActionListener(this::cancelPressed);
        cancelEditButton.setToolTipText(Bundle.getMessage("TooltipCancelRoute"));
        // Add Create Route button
        pb.add(createButton);
        createButton.addActionListener(this::createPressed);
        createButton.setToolTipText(Bundle.getMessage("TooltipCreateRoute"));
        // Edit Route button
        pb.add(editButton);
        editButton.addActionListener(this::editPressed);
        editButton.setToolTipText(Bundle.getMessage("TooltipEditRoute"));
        // Delete Route button
        pb.add(deleteButton);
        deleteButton.addActionListener(this::deletePressed);
        deleteButton.setToolTipText(Bundle.getMessage("TooltipDeleteRoute"));
        // Update Route button
        pb.add(updateButton);
        updateButton.addActionListener((ActionEvent e1) -> {
            updatePressed(e1, false);
        });
        updateButton.setToolTipText(Bundle.getMessage("TooltipUpdateRoute"));
        // Export button
        pb.add(exportButton);
        exportButton.addActionListener(this::exportPressed);
        exportButton.setToolTipText(Bundle.getMessage("TooltipExportRoute"));

        // Show the initial buttons, and hide the others
        exportButton.setVisible(false);
        cancelButton.setVisible(true); // show CancelAdd button
        cancelEditButton.setVisible(false);
        updateButton.setVisible(true);
        editButton.setVisible(true);
        createButton.setVisible(true);
        deleteButton.setVisible(false);
        contentPanel.add(pb);

        getContentPane().add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        // pack and release space
        pack();
        p2xsSpace.setVisible(false);
        p2xtSpace.setVisible(false);

        // set listener for window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // remind to save, if Route was created or edited
                if (routeDirty) {
                    showReminderMessage();
                    routeDirty = false;
                }
                // hide addFrame
                setVisible(false);
                // addFrame.dispose(); // causes multiple empty Routes next time

                // if in Edit, cancel edit mode
                if (editMode) {
                    cancelEdit();
                }
                _routeSensorModel.dispose();
                _routeTurnoutModel.dispose();
            }
        });
    }

    /**
     * Initialize list of included turnout positions.
     */
    void initializeIncludedList() {
        _includedTurnoutList = new ArrayList<>();
        for (int i = 0; i < _turnoutList.size(); i++) {
            if (_turnoutList.get(i).isIncluded()) {
                _includedTurnoutList.add(_turnoutList.get(i));
            }
        }
        _includedSensorList = new ArrayList<>();
        for (int i = 0; i < _sensorList.size(); i++) {
            if (_sensorList.get(i).isIncluded()) {
                _includedSensorList.add(_sensorList.get(i));
            }
        }
    }

    void autoSystemName() {
        if (_autoSystemName.isSelected()) {
            _systemName.setEnabled(false);
            nameLabel.setEnabled(false);
        } else {
            _systemName.setEnabled(true);
            nameLabel.setEnabled(true);
        }
    }

    void showReminderMessage() {
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                showInfoMessage(Bundle.getMessage("ReminderTitle"),  // NOI18N
                        Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemRouteTable")),  // NOI18N
                        getClassName(), "remindSaveRoute"); // NOI18N
    }

    int sensorModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in sensorMode: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setSensorModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, sensorInputModeValues, sensorInputModes);
        box.setSelectedItem(result);
    }

    int turnoutModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutInputModeValues, turnoutInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setTurnoutModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }


    /**
     * Respond to the Create button.
     *
     * @param e the action event
     */
    void createPressed(ActionEvent e) {
        if (!_autoSystemName.isSelected()) {
            if (!checkNewNamesOK()) {
                return;
            }
        }
        updatePressed(e, true); // close pane after creating
        status2.setText(editInst);
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        // activate the route
    }

    /**
     * Respond to the Edit button.
     *
     * @param e the action event
     */
    void editPressed(ActionEvent e) {
        // identify the Route with this name if it already exists
        String sName = _systemName.getText();
        Route g = InstanceManager.getDefault(jmri.RouteManager.class).getBySystemName(sName);
        if (g == null) {
            sName = _userName.getText();
            g = InstanceManager.getDefault(jmri.RouteManager.class).getByUserName(sName);
            if (g == null) {
                // Route does not exist, so cannot be edited
                status1.setText(Bundle.getMessage("RouteAddStatusErrorNotFound"));
                return;
            }
        }
        // Route was found, make its system name not changeable
        curRoute = g;
        _autoSystemName.setVisible(false);
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        nameLabel.setEnabled(true);
        _autoSystemName.setVisible(false);
        // deactivate this Route
        curRoute.deActivateRoute();
        // get information for this route
        _userName.setText(g.getUserName());
        // set up Turnout list for this route
        int setRow = 0;
        for (int i = _turnoutList.size() - 1; i >= 0; i--) {
            RouteTurnout turnout = _turnoutList.get(i);
            String tSysName = turnout.getSysName();
            if (g.isOutputTurnoutIncluded(tSysName)) {
                turnout.setIncluded(true);
                turnout.setState(g.getOutputTurnoutSetState(tSysName));
                setRow = i;
            } else {
                turnout.setIncluded(false);
                turnout.setState(Turnout.CLOSED);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _routeTurnoutScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _routeTurnoutModel.fireTableDataChanged();
        // set up Sensor list for this route
        for (int i = _sensorList.size() - 1; i >= 0; i--) {
            RouteSensor sensor = _sensorList.get(i);
            String tSysName = sensor.getSysName();
            if (g.isOutputSensorIncluded(tSysName)) {
                sensor.setIncluded(true);
                sensor.setState(g.getOutputSensorSetState(tSysName));
                setRow = i;
            } else {
                sensor.setIncluded(false);
                sensor.setState(Sensor.INACTIVE);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _routeSensorScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _routeSensorModel.fireTableDataChanged();
        // get Sound and  Script file names
        scriptFile.setText(g.getOutputScriptName());
        soundFile.setText(g.getOutputSoundName());

        // get Turnout Aligned sensor
        turnoutsAlignedSensor.setSelectedItem(g.getTurnoutsAlgdSensor());

        // set up Sensors if there are any
        Sensor[] temNames = new Sensor[Route.MAX_CONTROL_SENSORS];
        int[] temModes = new int[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k < Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensor(k);
            temModes[k] = g.getRouteSensorMode(k);
        }
        sensor1.setSelectedItem(temNames[0]);
        setSensorModeBox(temModes[0], sensor1mode);

        sensor2.setSelectedItem(temNames[1]);
        setSensorModeBox(temModes[1], sensor2mode);

        sensor3.setSelectedItem(temNames[2]);
        setSensorModeBox(temModes[2], sensor3mode);

        // set up Control Turnout if there is one
        cTurnout.setSelectedItem(g.getCtlTurnout());

        setTurnoutModeBox(g.getControlTurnoutState(), cTurnoutStateBox);

        // set up Lock Control Turnout if there is one
        cLockTurnout.setSelectedItem(g.getLockCtlTurnout());

        setTurnoutModeBox(g.getLockControlTurnoutState(), cLockTurnoutStateBox);

        // set up additional route specific Delay
        timeDelay.setValue(g.getRouteCommandDelay());
        // begin with showing all Turnouts
        // set up buttons and notes
        status1.setText(updateInst);
        status2.setText(cancelInst);
        status2.setVisible(true);
        deleteButton.setVisible(true);
        cancelButton.setVisible(false);
        cancelEditButton.setVisible(true);
        updateButton.setVisible(true);
        exportButton.setVisible(true);
        editButton.setVisible(false);
        createButton.setVisible(false);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        setTitle(Bundle.getMessage("TitleEditRoute"));
        editMode = true;
    }


    /**
     * Check name for a new Route object using the _systemName field on the addFrame pane.
     *
     * @return whether name entered is allowed
     */
    boolean checkNewNamesOK() {
        // Get system name and user name from Add Route pane
        String sName = _systemName.getText();
        String uName = _userName.getText();
        if (sName.length() == 0) {
            status1.setText(Bundle.getMessage("AddBeanStatusEnter"));
            status1.setForeground(Color.red);
            return false;
        }
        Route g;
        // check if a Route with the same user name exists
        if (!uName.equals("")) {
            g = InstanceManager.getDefault(jmri.RouteManager.class).getByUserName(uName);
            if (g != null) {
                // Route already exists
                status1.setText(Bundle.getMessage("LightError8"));
                return false;
            }
        }
        // check if a Route with this system name already exists
        sName = InstanceManager.getDefault(jmri.RouteManager.class).makeSystemName(sName);
        g = InstanceManager.getDefault(jmri.RouteManager.class).getBySystemName(sName);
        if (g != null) {
            // Route already exists
            status1.setText(Bundle.getMessage("LightError1"));
            return false;
        }
        return true;
    }

    /**
     * Check name and return a new or existing Route object with the name as entered in the _systemName field on the
     * addFrame pane.
     *
     * @return the new/updated Route object
     */
    Route checkNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText();
        String uName = _userName.getText();
        Route g;
        if (_autoSystemName.isSelected() && !editMode) {
            log.debug("checkNamesOK new autogroup");
            // create new Route with auto system name
            g = InstanceManager.getDefault(jmri.RouteManager.class).newRoute(uName);
        } else {
            if (sName.length() == 0) {
                status1.setText(Bundle.getMessage("AddBeanStatusEnter"));
                status1.setForeground(Color.red);
                return null;
            }
            try {
                sName = InstanceManager.getDefault(jmri.RouteManager.class).makeSystemName(sName);
                g = InstanceManager.getDefault(jmri.RouteManager.class).provideRoute(sName, uName);
            } catch (IllegalArgumentException ex) {
                g = null; // for later check
            }
        }
        if (g == null) {
            // should never get here
            log.error("Unknown failure to create Route with System Name: {}", sName); // NOI18N
        } else {
            g.deActivateRoute();
        }
        return g;
    }

    /**
     * Set the Turnout information for adding or editing.
     *
     * @param g the route to add the turnout to
     * @return number of turnouts in route
     */
    int setTurnoutInformation(Route g) {
        for (int i = 0; i < _includedTurnoutList.size(); i++) {
            RouteTurnout t = _includedTurnoutList.get(i);
            g.addOutputTurnout(t.getDisplayName(), t.getState());
        }
        return _includedTurnoutList.size();
    }

    /**
     * Sets the Sensor information for adding or editing.
     *
     * @param g the route to add the sensor to
     * @return number of sensors in route
     */
    int setSensorInformation(Route g) {
        for (int i = 0; i < _includedSensorList.size(); i++) {
            RouteSensor s = _includedSensorList.get(i);
            g.addOutputSensor(s.getDisplayName(), s.getState());
        }
        return _includedSensorList.size();
    }

    /**
     * Set the Sensor, Turnout, and delay control information for adding or editing.
     *
     * @param g the route to configure
     */
    void setControlInformation(Route g) {
        // Get sensor control information if any
        Sensor sensor = sensor1.getSelectedItem();
        if (sensor != null) {
            if ((!g.addSensorToRoute(sensor.getSystemName(), sensorModeFromBox(sensor1mode)))) {
                log.error("Unexpected failure to add Sensor '{}' to route '{}'.", sensor.getSystemName(), g.getSystemName());
            }
        }

        if (sensor2.getSelectedItem() != null) {
            if ((!g.addSensorToRoute(sensor2.getSelectedItemDisplayName(), sensorModeFromBox(sensor2mode)))) {
                log.error("Unexpected failure to add Sensor '{}' to Route '{}'.", sensor2.getSelectedItemDisplayName(), g.getSystemName());
            }
        }

        if (sensor3.getSelectedItem() != null) {
            if ((!g.addSensorToRoute(sensor3.getSelectedItemDisplayName(), sensorModeFromBox(sensor3mode)))) {
                log.error("Unexpected failure to add Sensor '{}' to Route '{}'.", sensor3.getSelectedItemDisplayName(), g.getSystemName());
            }
        }

        //Turnouts Aligned sensor
        if (turnoutsAlignedSensor.getSelectedItem() != null) {
            g.setTurnoutsAlignedSensor(turnoutsAlignedSensor.getSelectedItemDisplayName());
        } else {
            g.setTurnoutsAlignedSensor("");
        }

        // Set turnout information if there is any
        if (cTurnout.getSelectedItem() != null) {
            g.setControlTurnout(cTurnout.getSelectedItemDisplayName());
            // set up Control Turnout state
            g.setControlTurnoutState(turnoutModeFromBox(cTurnoutStateBox));
        } else {
            // No Control Turnout was entered
            g.setControlTurnout("");
        }
        // set route specific Delay information, see jmri.implementation.DefaultRoute#SetRouteThread()
        int addDelay = (Integer) timeDelay.getValue(); // from a JSpinner with 0 set as minimum
        g.setRouteCommandDelay(addDelay);

        // Set Lock Turnout information if there is any
        if (cLockTurnout.getSelectedItem() != null) {
            g.setLockControlTurnout(cLockTurnout.getSelectedItemDisplayName());
            // set up control turnout state
            g.setLockControlTurnoutState(turnoutModeFromBox(cLockTurnoutStateBox));
        } else {
            // No Lock Turnout was entered
            g.setLockControlTurnout("");
        }
    }

    JFileChooser soundChooser = null;

    /**
     * Set the sound file.
     */
    void setSoundPressed() {
        if (soundChooser == null) {
            soundChooser = new JFileChooser(FileUtil.getUserFilesPath());
            soundChooser.setFileFilter(new jmri.util.NoArchiveFileFilter());
        }
        soundChooser.rescanCurrentDirectory();
        int retVal = soundChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                soundFile.setText(soundChooser.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException e) {
                log.error("exception setting sound file: ", e);
            }
        }
    }

    JFileChooser scriptChooser = null;

    /**
     * Set the script file.
     */
    void setScriptPressed() {
        if (scriptChooser == null) {
            scriptChooser = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");
        }
        scriptChooser.rescanCurrentDirectory();
        int retVal = scriptChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                scriptFile.setText(scriptChooser.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException e) {
                log.error("exception setting script file: ", e);
            }
        }
    }

    /**
     * Respond to the Delete button.
     *
     * @param e the action event
     */
    void deletePressed(ActionEvent e) {
        // route is already deactivated, just delete it
        InstanceManager.getDefault(jmri.RouteManager.class).deleteRoute(curRoute);

        curRoute = null;
        finishUpdate();
    }

    /**
     * Respond to the Update button - update to Route Table.
     *
     * @param e        the action event
     * @param newRoute true if a new route; false otherwise
     */
    void updatePressed(ActionEvent e, boolean newRoute) {
        // Check if the User Name has been changed
        String uName = _userName.getText();
        Route g = checkNamesOK();
        if (g == null) {
            return;
        }
        // User Name is unique, change it
        g.setUserName(uName);
        // clear the current Turnout information for this Route
        g.clearOutputTurnouts();
        g.clearOutputSensors();
        // clear the current Sensor information for this Route
        g.clearRouteSensors();
        // add those indicated in the panel
        initializeIncludedList();
        setTurnoutInformation(g);
        setSensorInformation(g);
        // set the current values of the file names
        g.setOutputScriptName(scriptFile.getText());
        g.setOutputSoundName(soundFile.getText());
        // add Control Sensors and a Control Turnout if entered in the panel
        setControlInformation(g);
        curRoute = g;
        finishUpdate();
        status1.setForeground(Color.gray);
        status1.setText((newRoute ? Bundle.getMessage("RouteAddStatusCreated") : Bundle.getMessage("RouteAddStatusUpdated")) + ": \"" + uName + "\" (" + _includedTurnoutList.size() + " " + Bundle.getMessage("Turnouts") + ", " + _includedSensorList.size() + " " + Bundle.getMessage("Sensors") + ")");
    }

    void finishUpdate() {
        // move to show all Turnouts if not there
        cancelIncludedOnly();
        // Provide feedback to user
        // switch GUI back to selection mode
        status2.setText(editInst);
        status2.setVisible(true);
        deleteButton.setVisible(false);
        cancelButton.setVisible(true);
        cancelEditButton.setVisible(false);
        updateButton.setVisible(false);
        exportButton.setVisible(false);
        editButton.setVisible(true);
        createButton.setVisible(true);
        fixedSystemName.setVisible(false);
        _autoSystemName.setVisible(true);
        autoSystemName();
        _systemName.setVisible(true);
        setTitle(Bundle.getMessage("TitleAddRoute"));
        clearPage();
        // reactivate the Route
        routeDirty = true;
        // get out of edit mode
        editMode = false;
        if (curRoute != null) {
            curRoute.activateRoute();
        }
    }

    void clearPage() {
        _systemName.setText("");
        _userName.setText("");
        sensor1.setSelectedItem(null);
        sensor2.setSelectedItem(null);
        sensor3.setSelectedItem(null);
        cTurnout.setSelectedItem(null);
        cLockTurnout.setSelectedItem(null);
        turnoutsAlignedSensor.setSelectedItem(null);
        soundFile.setText("");
        scriptFile.setText("");
        for (int i = _turnoutList.size() - 1; i >= 0; i--) {
            _turnoutList.get(i).setIncluded(false);
        }
        for (int i = _sensorList.size() - 1; i >= 0; i--) {
            _sensorList.get(i).setIncluded(false);
        }
    }
    /////////////////////// Export to Logix ////////////////////////////

    /**
     * Respond to the Export button - export to Logix.
     *
     * @param e the action event
     */
    void exportPressed(ActionEvent e) {
        curRoute = checkNamesOK();
        String sName = _systemName.getText();
        if (sName.length() == 0) {
            sName = fixedSystemName.getText();
        }
        String uName = _userName.getText();
        String logixSystemName = LOGIX_SYS_NAME + sName;
        Logix logix = InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(logixSystemName);
        if (logix == null) {
            logix = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix(logixSystemName, uName);
            if (logix == null) {
                log.error("Failed to create Logix {}, {}", logixSystemName, uName);
                return;
            }
        }
        logix.deActivateLogix();
        initializeIncludedList();

        /////////////////// Construct output actions for change to true //////////////////////
        ArrayList<ConditionalAction> actionList = new ArrayList<>();

        for (int i = 0; i < _includedSensorList.size(); i++) {
            RouteSensor rSensor = _includedSensorList.get(i);
            String name = rSensor.getUserName();
            if (name == null || name.length() == 0) {
                name = rSensor.getSysName();
            }
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, name, rSensor.getState(), ""));
        }
        for (int i = 0; i < _includedTurnoutList.size(); i++) {
            RouteTurnout rTurnout = _includedTurnoutList.get(i);
            String name = rTurnout.getUserName();
            if (name == null || name.length() == 0) {
                name = rTurnout.getSysName();
            }
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_TURNOUT, name, rTurnout.getState(), ""));
        }
        String file = soundFile.getText();
        if (file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.PLAY_SOUND, "", -1, FileUtil.getPortableFilename(file)));
        }
        file = scriptFile.getText();
        if (file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.RUN_SCRIPT, "", -1, FileUtil.getPortableFilename(file)));
        }

        ///// Construct 'AND' clause from 'VETO' controls ////////
        ArrayList<ConditionalVariable> vetoList = new ArrayList<>();

        // String andClause = null;
        ConditionalVariable cVar = makeCtrlSensorVar(sensor1, sensor1mode, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(sensor2, sensor2mode, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlSensorVar(sensor3, sensor3mode, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }
        cVar = makeCtrlTurnoutVar(cTurnout, cTurnoutStateBox, true, false);
        if (cVar != null) {
            vetoList.add(cVar);
        }

        // remove old Conditionals for actions (ver 2.5.2 only -remove a bad idea)
        char[] ch = sName.toCharArray();
        int hash = 0;
        for (int i = 0; i < ch.length; i++) {
            hash += ch[i];
        }
        String cSystemName = CONDITIONAL_SYS_PREFIX + "T" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX + "F" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX + "A" + hash;
        removeConditionals(cSystemName, logix);
        cSystemName = CONDITIONAL_SYS_PREFIX + "L" + hash;
        removeConditionals(cSystemName, logix);

        int n = 0;
        do {
            n++;
            cSystemName = logixSystemName + n + "A";
        } while (removeConditionals(cSystemName, logix));
        n = 0;
        do {
            n++;
            cSystemName = logixSystemName + n + "T";
        } while (removeConditionals(cSystemName, logix));
        cSystemName = logixSystemName + "L";
        removeConditionals(cSystemName, logix);

        String cUserName;

        ///////////////// Make Trigger Conditionals //////////////////////
        //ArrayList <ConditionalVariable> onChangeList = new ArrayList<>();
        int numConds = 1; // passed through all these, with new value returned each time
        numConds = makeSensorConditional(sensor1, sensor1mode, numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor2, sensor2mode, numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor3, sensor3mode, numConds, false, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, false, actionList, vetoList, logix, logixSystemName, uName);

        ////// Construct actions for false from the 'any change' controls ////////////
        numConds = makeSensorConditional(sensor1, sensor1mode, numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor2, sensor2mode, numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor3, sensor3mode, numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, true, actionList, vetoList, logix, logixSystemName, uName);
        log.debug("Final number of conditionals: {}", numConds);

        ///////////////// Set up Alignment Sensor, if there is one //////////////////////////
        //String sensorSystemName = turnoutsAlignedSensor.getText();
        if (turnoutsAlignedSensor.getSelectedItem() != null) {
            // verify name (logix doesn't use "provideXXX")
            //Sensor s = turnoutsAlignedSensor.getSelectedItem();
            /*if (s == null) {
             s = InstanceManager.sensorManagerInstance().getBySystemName(sensorSystemName);
             }*/
            //if (s != null) {
            String sensorSystemName = turnoutsAlignedSensor.getSelectedItemDisplayName();
            cSystemName = logixSystemName + "1A"; // NOI18N
            cUserName = turnoutsAlignedSensor.getSelectedItemDisplayName() + "A " + uName; // NOI18N

            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            for (int i = 0; i < _includedTurnoutList.size(); i++) {
                RouteTurnout rTurnout = _includedTurnoutList.get(i);
                String name = rTurnout.getUserName();
                if (name == null || name.length() == 0) {
                    name = rTurnout.getSysName();
                }
                // exclude toggled outputs
                switch (rTurnout.getState()) {
                    case Turnout.CLOSED:
                        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_CLOSED, name, true));
                        break;
                    case Turnout.THROWN:
                        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND, Conditional.Type.TURNOUT_THROWN, name, true));
                        break;
                    default:
                        log.warn("Turnout {} was {}, neither CLOSED nor THROWN; not handled", name, rTurnout.getState()); // NOI18N
                }
            }
            actionList = new ArrayList<>();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, Conditional.Action.SET_SENSOR, sensorSystemName, Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE, Conditional.Action.SET_SENSOR, sensorSystemName, Sensor.INACTIVE, ""));

            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            //}
        }

        ///////////////// Set lock turnout information if there is any //////////////////////////
        if (cLockTurnout.getSelectedItem() != null) {
            String turnoutLockSystemName = cLockTurnout.getSelectedItemDisplayName();
            // verify name (logix doesn't use "provideXXX")
            cSystemName = logixSystemName + "1L"; // NOI18N
            cUserName = turnoutLockSystemName + "L " + uName; // NOI18N
            ArrayList<ConditionalVariable> variableList = new ArrayList<>();
            //String devName = cTurnout.getText();
            int mode = turnoutModeFromBox(cTurnoutStateBox);
            Conditional.Type conditionalType = Conditional.Type.TURNOUT_CLOSED;
            if (mode == Route.ONTHROWN) {
                conditionalType = Conditional.Type.TURNOUT_THROWN;
            }
            variableList.add(new ConditionalVariable(false, Conditional.Operator.NONE, conditionalType, turnoutLockSystemName, true));

            actionList = new ArrayList<>();
            int option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            int type = Turnout.LOCKED;
            if (mode == Route.ONCHANGE) {
                option = Conditional.ACTION_OPTION_ON_CHANGE;
                type = Route.TOGGLE;
            }
            for (int i = 0; i < _includedTurnoutList.size(); i++) {
                RouteTurnout rTurnout = _includedTurnoutList.get(i);
                String name = rTurnout.getUserName();
                if (name == null || name.length() == 0) {
                    name = rTurnout.getSysName();
                }
                actionList.add(new DefaultConditionalAction(option, Conditional.Action.LOCK_TURNOUT, name, type, ""));
            }
            if (mode != Route.ONCHANGE) {
                // add non-toggle actions on
                option = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
                type = Turnout.UNLOCKED;
                for (int i = 0; i < _includedTurnoutList.size(); i++) {
                    RouteTurnout rTurnout = _includedTurnoutList.get(i);
                    String name = rTurnout.getUserName();
                    if (name == null || name.length() == 0) {
                        name = rTurnout.getSysName();
                    }
                    actionList.add(new DefaultConditionalAction(option, Conditional.Action.LOCK_TURNOUT, name, type, ""));
                }
            }

            // add new Conditionals for action on 'locks'
            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
        }
        logix.activateLogix();
        if (curRoute != null) {
            InstanceManager.getDefault(jmri.RouteManager.class).deleteRoute(curRoute);
            curRoute = null;
        }
        status1.setText(Bundle.getMessage("BeanNameRoute") + "\"" + uName + "\" " + Bundle.getMessage("RouteAddStatusExported") + " (" + _includedTurnoutList.size() + Bundle.getMessage("Turnouts") + ", " + _includedSensorList.size() + " " + Bundle.getMessage("Sensors") + ")");
        finishUpdate();
    }

    boolean removeConditionals(String cSystemName, Logix logix) {
        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(cSystemName);
        if (c != null) {
            logix.deleteConditional(cSystemName);
            InstanceManager.getDefault(jmri.ConditionalManager.class).deleteConditional(c);
            return true;
        }
        return false;
    }

    /**
     * Create a new sensor conditional.
     *
     * @param jmriBox    control, the selection from which, will be used to determine which sensor to make a conditional
     *                   for
     * @param sensorbox  control, the selection from which, will be used to determine the mode for the conditional
     * @param numConds   number of existing route conditionals
     * @param onChange   ???
     * @param actionList actions to take in conditional
     * @param vetoList   conditionals that can veto an action
     * @param logix      Logix to add the conditional to
     * @param prefix     system prefix for conditional
     * @param uName      user name for conditional
     * @return number of conditionals after the creation
     * @throws IllegalArgumentException if "user input no good"
     */
    // why are the controls being passed, and not their selections?
    int makeSensorConditional(NamedBeanComboBox<Sensor> jmriBox, JComboBox<String> sensorbox, int numConds, boolean onChange, ArrayList<ConditionalAction> actionList, ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlSensorVar(jmriBox, sensorbox, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<>();
            varList.add(cVar);
            for (int i = 0; i < vetoList.size(); i++) {
                varList.add(cloneVariable(vetoList.get(i)));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = jmriBox.getSelectedItemDisplayName() + numConds + "C " + uName;
            Conditional c = null;
            try {
                c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(cSystemName);
                // throw without creating any
                throw new IllegalArgumentException("user input no good");
            }
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    /**
     * Create a new turnout conditional.
     *
     * @param jmriBox    control, the selection from which, will be used to determine which sensor to make a conditional
     *                   for
     * @param box        control, the selection from which, will be used to determine the mode for the conditional
     * @param numConds   number of existing route conditionals
     * @param onChange   ???
     * @param actionList actions to take in conditional
     * @param vetoList   conditionals that can veto an action
     * @param logix      Logix to add the conditional to
     * @param prefix     system prefix for conditional
     * @param uName      user name for conditional
     * @return number of conditionals after the creation
     * @throws IllegalArgumentException if "user input no good"
     */
    // why are the controls being passed, and not their selections?
    int makeTurnoutConditional(NamedBeanComboBox<Turnout> jmriBox, JComboBox<String> box, int numConds, boolean onChange, ArrayList<ConditionalAction> actionList, ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlTurnoutVar(jmriBox, box, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<>();
            varList.add(cVar);
            for (int i = 0; i < vetoList.size(); i++) {
                varList.add(cloneVariable(vetoList.get(i)));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = jmriBox.getSelectedItemDisplayName() + numConds + "C " + uName;
            Conditional c = null;
            try {
                c = InstanceManager.getDefault(jmri.ConditionalManager.class).createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(cSystemName);
                // throw without creating any
                throw new IllegalArgumentException("user input no good");
            }
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorRouteAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"), Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
    }

    ConditionalVariable cloneVariable(ConditionalVariable v) {
        return new ConditionalVariable(v.isNegated(), v.getOpern(), v.getType(), v.getName(), v.doTriggerActions());
    }

    ArrayList<ConditionalAction> cloneActionList(ArrayList<ConditionalAction> actionList, int option) {
        ArrayList<ConditionalAction> list = new ArrayList<>();
        for (int i = 0; i < actionList.size(); i++) {
            ConditionalAction action = actionList.get(i);
            ConditionalAction clone = new DefaultConditionalAction();
            clone.setType(action.getType());
            clone.setOption(option);
            clone.setDeviceName(action.getDeviceName());
            clone.setActionData(action.getActionData());
            clone.setActionString(action.getActionString());
            list.add(clone);
        }
        return list;
    }

    ConditionalVariable makeCtrlSensorVar(NamedBeanComboBox<Sensor> jmriBox, JComboBox<String> sensorbox, boolean makeVeto, boolean onChange) {
        String devName = jmriBox.getSelectedItemDisplayName();
        if (jmriBox.getSelectedItem() == null /*|| devName.length() == 0*/) {
            return null;
        }
        Conditional.Operator oper = Conditional.Operator.AND;
        int mode = sensorModeFromBox(sensorbox);
        boolean trigger = true;
        boolean negated = false;
        Conditional.Type type;
        switch (mode) {
            case Route.ONACTIVE:    // route fires if sensor goes active
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_ACTIVE;
                break;
            case Route.ONINACTIVE:  // route fires if sensor goes inactive
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_INACTIVE;
                break;
            case Route.ONCHANGE:  // route fires if sensor goes active or inactive
                if (makeVeto || !onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_ACTIVE;
                break;
            case Route.VETOACTIVE:  // sensor must be active for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_ACTIVE;
                negated = true;
                trigger = false;
                break;
            case Route.VETOINACTIVE:
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.SENSOR_INACTIVE;
                negated = true;
                trigger = false;
                break;
            default:
                log.error("Control Sensor {} has bad mode= {}", devName, mode);
                return null;
        }
        return new ConditionalVariable(negated, oper, type, devName, trigger);
    }

    ConditionalVariable makeCtrlTurnoutVar(NamedBeanComboBox<Turnout> jmriBox, JComboBox<String> box, boolean makeVeto, boolean onChange) {

        if (jmriBox.getSelectedItem() == null) {
            return null;
        }
        String devName = jmriBox.getSelectedItemDisplayName();
        int mode = turnoutModeFromBox(box);
        Conditional.Operator oper = Conditional.Operator.AND;
        Conditional.Type type;
        boolean negated = false;
        boolean trigger = true;
        switch (mode) {
            case Route.ONCLOSED:    // route fires if turnout goes closed
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_CLOSED;
                break;
            case Route.ONTHROWN:  // route fires if turnout goes thrown
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_THROWN;
                break;
            case Route.ONCHANGE:    // route fires if turnout goes active or inactive
                if (makeVeto || !onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_CLOSED;
                break;
            case Route.VETOCLOSED:  // turnout must be closed for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_CLOSED;
                trigger = false;
                negated = true;
                break;
            case Route.VETOTHROWN:  // turnout must be thrown for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.Type.TURNOUT_THROWN;
                trigger = false;
                negated = true;
                break;
            default:
                log.error("Control Turnout {} has bad mode= {}", devName, mode);
                return null;
        }
        return new ConditionalVariable(negated, oper, type, devName, trigger);
    }

    /**
     * Respond to the CancelAdd button.
     *
     * @param e the action event
     */
    void cancelAddPressed(ActionEvent e) {
        cancelAdd();
    }

    /**
     * Cancel Add mode.
     */
    void cancelAdd() {
        if (routeDirty) {
            showReminderMessage();
        }
        curRoute = null;
        finishUpdate();
        status1.setText(createInst);
        status2.setText(editInst);
        routeDirty = false;
        // hide addFrame
        setVisible(false);
        _routeSensorModel.dispose();
        _routeTurnoutModel.dispose();
    }

    /**
     * Respond to the CancelEdit button.
     *
     * @param e the action event
     */
    void cancelPressed(ActionEvent e) {
        cancelEdit();
    }

    /**
     * Cancels edit mode
     */
    void cancelEdit() {
        if (editMode) {
            status1.setText(createInst);
            status2.setText(editInst);
            finishUpdate();
            // get out of edit mode
            editMode = false;
            curRoute = null;
        }
    }

    /**
     * Cancel included Turnouts only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Base table model for selecting outputs.
     */
    public abstract class RouteOutputModel extends AbstractTableModel implements PropertyChangeListener {

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        public void dispose() {
            InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        @Override
        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;

    }

    /**
     * Table model for selecting Turnouts and Turnout State.
     */
    class RouteTurnoutModel extends RouteOutputModel {

        RouteTurnoutModel() {
            InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
        }

        @Override
        public int getRowCount() {
            if (showAll) {
                return _turnoutList.size();
            } else {
                return _includedTurnoutList.size();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            ArrayList<RouteTurnout> turnoutList;
            if (showAll) {
                turnoutList = _turnoutList;
            } else {
                turnoutList = _includedTurnoutList;
            }
            // some error checking
            if (r >= turnoutList.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return turnoutList.get(r).isIncluded();
                case SNAME_COLUMN:  // slot number
                    return turnoutList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return turnoutList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return turnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            ArrayList<RouteTurnout> turnoutList;
            if (showAll) {
                turnoutList = _turnoutList;
            } else {
                turnoutList = _includedTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    turnoutList.get(r).setIncluded(((Boolean) type));
                    break;
                case STATE_COLUMN:
                    turnoutList.get(r).setSetToState((String) type);
                    break;
                default:
                    log.error("RouteTurnoutModel.setValueAt should not be called on column {}", c);
            }
        }

    }

    /**
     * Set up table for selecting Sensors and Sensor State.
     */
    class RouteSensorModel extends RouteOutputModel {

        RouteSensorModel() {
            InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        @Override
        public int getRowCount() {
            if (showAll) {
                return _sensorList.size();
            } else {
                return _includedSensorList.size();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            ArrayList<RouteSensor> sensorList;
            if (showAll) {
                sensorList = _sensorList;
            } else {
                sensorList = _includedSensorList;
            }
            // some error checking
            if (r >= sensorList.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return sensorList.get(r).isIncluded();
                case SNAME_COLUMN:  // slot number
                    return sensorList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return sensorList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return sensorList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            ArrayList<RouteSensor> sensorList;
            if (showAll) {
                sensorList = _sensorList;
            } else {
                sensorList = _includedSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    sensorList.get(r).setIncluded(((Boolean) type));
                    break;
                case STATE_COLUMN:
                    sensorList.get(r).setSetToState((String) type);
                    break;
                default:
                    log.error("RouteSensorModel.setValueAt should not be called on column {}", c);
            }
        }

    }



    private static String[] COLUMN_NAMES = {Bundle.getMessage("ColumnSystemName"),
            Bundle.getMessage("ColumnUserName"),
            Bundle.getMessage("Include"),
            Bundle.getMessage("ColumnLabelSetState")};
    private static String SET_TO_ACTIVE = Bundle.getMessage("Set") + " " + Bundle.getMessage("SensorStateActive");
    private static String SET_TO_INACTIVE = Bundle.getMessage("Set") + " " + Bundle.getMessage("SensorStateInactive");

    private static String SET_TO_TOGGLE = Bundle.getMessage("Set") + " " + Bundle.getMessage("Toggle");

    private static String[] sensorInputModes = new String[]{
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("SensorStateActive"),
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("SensorStateInactive"),
            Bundle.getMessage("OnConditionChange"),
            "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("SensorStateActive"),
            "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("SensorStateInactive")
    };
    private static int[] sensorInputModeValues = new int[]{Route.ONACTIVE, Route.ONINACTIVE, Route.ONCHANGE,
            Route.VETOACTIVE, Route.VETOINACTIVE};

    // This group will get runtime updates to system-specific contents at
    // the start of buildModel() above.  This is done to prevent
    // invoking the TurnoutManager at class construction time,
    // when it hasn't been configured yet
    private static String SET_TO_CLOSED = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateClosed");
    private static String SET_TO_THROWN = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateThrown");
    private static String[] turnoutInputModes = new String[]{
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("TurnoutStateClosed"),
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("TurnoutStateThrown"),
            Bundle.getMessage("OnConditionChange"),
            "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("TurnoutStateClosed"),
            "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("TurnoutStateThrown")
    };
    private static String[] lockTurnoutInputModes = new String[]{
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("TurnoutStateClosed"),
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("TurnoutStateThrown"),
            Bundle.getMessage("OnConditionChange")
    };

    // safe methods to set tho above 4 static field values
    private static int[] turnoutInputModeValues = new int[]{Route.ONCLOSED, Route.ONTHROWN, Route.ONCHANGE,
            Route.VETOCLOSED, Route.VETOTHROWN};

    private static void setClosedString(@Nonnull String newVal) {
        SET_TO_CLOSED = newVal;
    }

    private static void setThrownString(@Nonnull String newVal) {
        SET_TO_THROWN = newVal;
    }

    private static void setTurnoutInputModes(@Nonnull String[] newArray) {
        turnoutInputModes = newArray;
    }

    private static void setLockTurnoutModes(@Nonnull String[] newArray) {
        lockTurnoutInputModes = newArray;
    }

    private synchronized static void setRowHeight(int newVal) {
        ROW_HEIGHT = newVal;
    }

    private String getClassName() {
        return this.getClass().getName();
    }

    private static final Logger log = LoggerFactory.getLogger(RouteAddFrame.class);

}