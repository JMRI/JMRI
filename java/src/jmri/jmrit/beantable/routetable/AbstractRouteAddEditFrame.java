package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.swing.NamedBeanComboBox;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.StringUtil;
import jmri.util.swing.JComboBoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Add/Edit frame for the Route Table.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2020
 */
public abstract class AbstractRouteAddEditFrame extends JmriJFrame {

    protected final RouteManager routeManager;

    static final String[] COLUMN_NAMES = {Bundle.getMessage("ColumnSystemName"),
            Bundle.getMessage("ColumnUserName"),
            Bundle.getMessage("Include"),
            Bundle.getMessage("ColumnLabelSetState")};
    private static final String SET_TO_ACTIVE = Bundle.getMessage("Set") + " " + Bundle.getMessage("SensorStateActive");
    private static final String SET_TO_INACTIVE = Bundle.getMessage("Set") + " " + Bundle.getMessage("SensorStateInactive");
    static final String SET_TO_TOGGLE = Bundle.getMessage("Set") + " " + Bundle.getMessage("Toggle");
    private static final String[] sensorInputModes = new String[]{
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("SensorStateActive"),
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("SensorStateInactive"),
            Bundle.getMessage("OnConditionChange"),
            "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("SensorStateActive"),
            "Veto " + Bundle.getMessage("WhenCondition") + " " + Bundle.getMessage("SensorStateInactive")
    };
    private static final int[] sensorInputModeValues = new int[]{Route.ONACTIVE, Route.ONINACTIVE, Route.ONCHANGE,
            Route.VETOACTIVE, Route.VETOINACTIVE};

    // safe methods to set the above 4 static field values
    private static final int[] turnoutInputModeValues = new int[]{Route.ONCLOSED, Route.ONTHROWN, Route.ONCHANGE,
            Route.VETOCLOSED, Route.VETOTHROWN};

    private static final Logger log = LoggerFactory.getLogger(AbstractRouteAddEditFrame.class);

    static int ROW_HEIGHT;
    // This group will get runtime updates to system-specific contents at
    // the start of buildModel() above.  This is done to prevent
    // invoking the TurnoutManager at class construction time,
    // when it hasn't been configured yet
    static String SET_TO_CLOSED = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateClosed");
    static String SET_TO_THROWN = Bundle.getMessage("Set") + " "
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
    final JTextField _systemName = new JTextField(10);
    final JTextField _userName = new JTextField(22);
    final JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    final JTextField soundFile = new JTextField(20);
    final JTextField scriptFile = new JTextField(20);
    final JComboBox<String> sensor1mode = new JComboBox<>(sensorInputModes);
    final JComboBox<String> sensor2mode = new JComboBox<>(sensorInputModes);
    final JComboBox<String> sensor3mode = new JComboBox<>(sensorInputModes);
    final JSpinner timeDelay = new JSpinner();
    final JComboBox<String> cTurnoutStateBox = new JComboBox<>(turnoutInputModes);
    final JComboBox<String> cLockTurnoutStateBox = new JComboBox<>(lockTurnoutInputModes);
    final JLabel nameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    final JLabel userLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    final JLabel status1 = new JLabel();
    final JLabel status2 = new JLabel();

    protected final String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    ArrayList<RouteTurnout> _turnoutList;      // array of all Turnouts
    ArrayList<RouteSensor> _sensorList;        // array of all Sensors
    RouteTurnoutModel _routeTurnoutModel;
    JScrollPane _routeTurnoutScrollPane;
    RouteSensorModel _routeSensorModel;
    JScrollPane _routeSensorScrollPane;
    NamedBeanComboBox<Sensor> turnoutsAlignedSensor;
    NamedBeanComboBox<Sensor> sensor1;
    NamedBeanComboBox<Sensor> sensor2;
    NamedBeanComboBox<Sensor> sensor3;
    NamedBeanComboBox<Turnout> cTurnout;
    NamedBeanComboBox<Turnout> cLockTurnout;
    Route curRoute = null;
    boolean editMode = false;
    protected ArrayList<RouteTurnout> _includedTurnoutList;
    protected ArrayList<RouteSensor> _includedSensorList;
    protected UserPreferencesManager pref;
    private JRadioButton allButton = null;
    protected boolean routeDirty = false;  // true to fire reminder to save work
    private boolean showAll = true;   // false indicates show only included Turnouts
    private JFileChooser soundChooser = null;
    private ScriptFileChooser scriptChooser = null;

    public AbstractRouteAddEditFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);

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

        routeManager = InstanceManager.getDefault(RouteManager.class);

    }

    protected static void setClosedString(@Nonnull String newVal) {
        SET_TO_CLOSED = newVal;
    }

    protected static void setThrownString(@Nonnull String newVal) {
        SET_TO_THROWN = newVal;
    }

    protected static void setTurnoutInputModes(@Nonnull String[] newArray) {
        turnoutInputModes = newArray;
    }

    protected static void setLockTurnoutModes(@Nonnull String[] newArray) {
        lockTurnoutInputModes = newArray;
    }

    private static synchronized void setRowHeight(int newVal) {
        ROW_HEIGHT = newVal;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        pref = InstanceManager.getDefault(UserPreferencesManager.class);
        if (editMode) {
            cancelEdit();
        }
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        _turnoutList = new ArrayList<>();
        for (Turnout t : tm.getNamedBeanSet()) {
            String systemName = t.getSystemName();
            String userName = t.getUserName();
            _turnoutList.add(new RouteTurnout(systemName, userName));
        }

        SensorManager sm = InstanceManager.sensorManagerInstance();
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
        nameLabel.setLabelFor(_systemName);
        ps.add(_systemName);
        ps.add(_autoSystemName);
        _autoSystemName.addActionListener((ActionEvent e1) -> autoSystemName());
        if (pref.getSimplePreferenceState(systemNameAuto)) {
            _autoSystemName.setSelected(true);
            _systemName.setEnabled(false);
        }
        _systemName.setToolTipText(Bundle.getMessage("TooltipRouteSystemName"));
        contentPanel.add(ps);
        // add user name
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(userLabel);
        userLabel.setLabelFor(_userName);
        p.add(_userName);
        _userName.setToolTipText(Bundle.getMessage("TooltipRouteUserName"));
        contentPanel.add(p);
        // add Turnout Display Choice
        JPanel py = new JPanel();
        py.add(new JLabel(Bundle.getMessage("Show") + ":"));
        ButtonGroup selGroup = new ButtonGroup();
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
        JRadioButton includedButton = new JRadioButton(Bundle.getMessage("Included"), false);
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

        contentPanel.add(getTurnoutPanel());
        contentPanel.add(getSensorPanel());
        contentPanel.add(getFileNamesPanel());
        contentPanel.add(getAlignedSensorPanel());
        contentPanel.add(getControlsPanel());
        contentPanel.add(getLockPanel());
        contentPanel.add(getNotesPanel());
        contentPanel.add(getButtonPanel());

        getContentPane().add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        pack();

        // set listener for window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeFrame();
            }
        });
    }

    protected abstract JPanel getButtonPanel();

    private JPanel getNotesPanel() {
        // add notes panel
        JPanel pa = new JPanel();
        pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        status1.setText(Bundle.getMessage("RouteAddStatusInitial1", Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
        status1.setFont(status1.getFont().deriveFont(0.9f * nameLabel.getFont().getSize())); // a bit smaller
        status1.setForeground(Color.gray);
        p1.add(status1);
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        status2.setText(Bundle.getMessage("RouteAddStatusInitial5", Bundle.getMessage("ButtonCancel","")));
        status2.setFont(status1.getFont().deriveFont(0.9f * nameLabel.getFont().getSize())); // a bit smaller
        status2.setForeground(Color.gray);
        p2.add(status2);
        pa.add(p1);
        pa.add(p2);
        Border pBorder = BorderFactory.createEtchedBorder();
        pa.setBorder(pBorder);
        return pa;
    }

    private JPanel getLockPanel() {
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
        return p4;
    }

    private JPanel getControlsPanel() {
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
        return p3;
    }

    private JPanel getAlignedSensorPanel() {
        //add turnouts aligned Sensor
        JPanel p27 = new JPanel();
        p27.setLayout(new FlowLayout());
        p27.add(new JLabel(Bundle.getMessage("LabelEnterSensorAligned")));
        p27.add(turnoutsAlignedSensor);
        turnoutsAlignedSensor.setAllowNull(true);
        turnoutsAlignedSensor.setSelectedItem(null);
        turnoutsAlignedSensor.setToolTipText(Bundle.getMessage("TooltipEnterSensor"));
        return p27;
    }

    private JPanel getFileNamesPanel() {
        // Enter filenames for sound, script
        JPanel p25 = new JPanel();
        p25.setLayout(new FlowLayout());
        p25.add(new JLabel(Bundle.getMessage("LabelPlaySound")));
        p25.add(soundFile);
        JButton ss = new JButton("..."); //NO18N
        ss.addActionListener((ActionEvent e1) -> setSoundPressed());
        ss.setToolTipText(Bundle.getMessage("TooltipOpenFile", Bundle.getMessage("BeanNameAudio")));
        p25.add(ss);
        p25.add(new JLabel(Bundle.getMessage("LabelRunScript")));
        p25.add(scriptFile);
        ss = new JButton("..."); //NO18N
        ss.addActionListener((ActionEvent e1) -> setScriptPressed());
        ss.setToolTipText(Bundle.getMessage("TooltipOpenFile", Bundle.getMessage("Script")));
        p25.add(ss);
        return p25;
    }

    private JPanel getTurnoutPanel(){
        // add Turnout table
        // Turnout list table
        JPanel p2xt = new JPanel();
        JPanel p2xtSpace = new JPanel();
        p2xtSpace.setLayout(new BoxLayout(p2xtSpace, BoxLayout.Y_AXIS));
        p2xtSpace.add(Box.createRigidArea(new Dimension(30,0)));
        p2xt.add(p2xtSpace);

        JPanel p21t = new JPanel();
        p21t.setLayout(new BoxLayout(p21t, BoxLayout.Y_AXIS));
        p21t.add(new JLabel(Bundle.getMessage("SelectInRoute", Bundle.getMessage("Turnouts"))));
        p2xt.add(p21t);
        _routeTurnoutModel = new RouteTurnoutModel(this);
        JTable routeTurnoutTable = new JTable(_routeTurnoutModel);
        TableRowSorter<RouteTurnoutModel> rtSorter = new TableRowSorter<>(_routeTurnoutModel);

        // Use AlphanumComparator for SNAME and UNAME columns.  Start with SNAME sort.
        rtSorter.setComparator(RouteOutputModel.SNAME_COLUMN, new AlphanumComparator());
        rtSorter.setComparator(RouteOutputModel.UNAME_COLUMN, new AlphanumComparator());
        RowSorterUtil.setSortOrder(rtSorter, RouteOutputModel.SNAME_COLUMN, SortOrder.ASCENDING);

        routeTurnoutTable.setRowSorter(rtSorter);
        routeTurnoutTable.setRowSelectionAllowed(false);
        routeTurnoutTable.setPreferredScrollableViewportSize(new Dimension(480, 80));

        setRowHeight(routeTurnoutTable.getRowHeight());
        JComboBox<String> stateTCombo = new JComboBox<>();
        stateTCombo.addItem(SET_TO_CLOSED);
        stateTCombo.addItem(SET_TO_THROWN);
        stateTCombo.addItem(SET_TO_TOGGLE);
        TableColumnModel routeTurnoutColumnModel = routeTurnoutTable.getColumnModel();
        TableColumn includeColumnT = routeTurnoutColumnModel.
                getColumn(RouteOutputModel.INCLUDE_COLUMN);
        includeColumnT.setResizable(false);
        includeColumnT.setMinWidth(50);
        includeColumnT.setMaxWidth(60);
        TableColumn sNameColumnT = routeTurnoutColumnModel.
                getColumn(RouteOutputModel.SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(75);
        sNameColumnT.setMaxWidth(95);
        TableColumn uNameColumnT = routeTurnoutColumnModel.
                getColumn(RouteOutputModel.UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(210);
        uNameColumnT.setMaxWidth(260);
        TableColumn stateColumnT = routeTurnoutColumnModel.
                getColumn(RouteOutputModel.STATE_COLUMN);
        stateColumnT.setCellEditor(new DefaultCellEditor(stateTCombo));
        stateColumnT.setResizable(false);
        stateColumnT.setMinWidth(90);
        stateColumnT.setMaxWidth(100);
        _routeTurnoutScrollPane = new JScrollPane(routeTurnoutTable);
        p2xt.add(_routeTurnoutScrollPane, BorderLayout.CENTER);
        p2xt.setVisible(true);
        return p2xt;
    }

    private JPanel getSensorPanel(){
        // add Sensor table
        // Sensor list table
        JPanel p2xs = new JPanel();
        JPanel p2xsSpace = new JPanel();
        p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
        p2xsSpace.add(Box.createRigidArea(new Dimension(30,0)));
        p2xs.add(p2xsSpace);

        JPanel p21s = new JPanel();
        p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
        p21s.add(new JLabel(Bundle.getMessage("SelectInRoute", Bundle.getMessage("Sensors"))));
        p2xs.add(p21s);
        _routeSensorModel = new RouteSensorModel(this);
        JTable routeSensorTable = new JTable(_routeSensorModel);
        TableRowSorter<RouteSensorModel> rsSorter = new TableRowSorter<>(_routeSensorModel);

        // Use AlphanumComparator for SNAME and UNAME columns.  Start with SNAME sort.
        rsSorter.setComparator(RouteOutputModel.SNAME_COLUMN, new AlphanumComparator());
        rsSorter.setComparator(RouteOutputModel.UNAME_COLUMN, new AlphanumComparator());
        RowSorterUtil.setSortOrder(rsSorter, RouteOutputModel.SNAME_COLUMN, SortOrder.ASCENDING);
        routeSensorTable.setRowSorter(rsSorter);
        routeSensorTable.setRowSelectionAllowed(false);
        routeSensorTable.setPreferredScrollableViewportSize(new Dimension(480, 80));
        JComboBox<String> stateSCombo = new JComboBox<>();
        stateSCombo.addItem(SET_TO_ACTIVE);
        stateSCombo.addItem(SET_TO_INACTIVE);
        stateSCombo.addItem(SET_TO_TOGGLE);
        TableColumnModel routeSensorColumnModel = routeSensorTable.getColumnModel();
        TableColumn includeColumnS = routeSensorColumnModel.
                getColumn(RouteOutputModel.INCLUDE_COLUMN);
        includeColumnS.setResizable(false);
        includeColumnS.setMinWidth(50);
        includeColumnS.setMaxWidth(60);
        TableColumn sNameColumnS = routeSensorColumnModel.
                getColumn(RouteOutputModel.SNAME_COLUMN);
        sNameColumnS.setResizable(true);
        sNameColumnS.setMinWidth(75);
        sNameColumnS.setMaxWidth(95);
        TableColumn uNameColumnS = routeSensorColumnModel.
                getColumn(RouteOutputModel.UNAME_COLUMN);
        uNameColumnS.setResizable(true);
        uNameColumnS.setMinWidth(210);
        uNameColumnS.setMaxWidth(260);
        TableColumn stateColumnS = routeSensorColumnModel.
                getColumn(RouteOutputModel.STATE_COLUMN);
        stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo));
        stateColumnS.setResizable(false);
        stateColumnS.setMinWidth(90);
        stateColumnS.setMaxWidth(100);
        _routeSensorScrollPane = new JScrollPane(routeSensorTable);
        p2xs.add(_routeSensorScrollPane, BorderLayout.CENTER);
        p2xs.setVisible(true);
        return p2xs;
    }

    /**
     * Initialize list of included turnout positions.
     */
    protected void initializeIncludedList() {
        _includedTurnoutList = new ArrayList<>();
        for (RouteTurnout routeTurnout : _turnoutList) {
            if (routeTurnout.isIncluded()) {
                _includedTurnoutList.add(routeTurnout);
            }
        }
        _includedSensorList = new ArrayList<>();
        for (RouteSensor routeSensor : _sensorList) {
            if (routeSensor.isIncluded()) {
                _includedSensorList.add(routeSensor);
            }
        }
    }

    private void autoSystemName() {
        if (_autoSystemName.isSelected()) {
            _systemName.setEnabled(false);
            nameLabel.setEnabled(false);
        } else {
            _systemName.setEnabled(true);
            nameLabel.setEnabled(true);
        }
    }

    protected void showReminderMessage() {
        InstanceManager.getDefault(UserPreferencesManager.class).
                showInfoMessage(Bundle.getMessage("ReminderTitle"),  // NOI18N
                        Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemRouteTable")),  // NOI18N
                        getClassName(), "remindSaveRoute"); // NOI18N
    }

    private int sensorModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        return sensorModeFromString(mode);
    }

    int sensorModeFromString(String mode) {
        int result = StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in sensorMode: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setSensorModeBox(int mode, JComboBox<String> box) {
        String result = StringUtil.getNameFromState(mode, sensorInputModeValues, sensorInputModes);
        box.setSelectedItem(result);
    }

    private int turnoutModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = StringUtil.getStateFromName(mode, turnoutInputModeValues, turnoutInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setTurnoutModeBox(int mode, JComboBox<String> box) {
        String result = StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }

    /**
     * Set the Turnout information for adding or editing.
     *
     * @param g the route to add the turnout to
     */
    protected void setTurnoutInformation(Route g) {
        for (RouteTurnout t : _includedTurnoutList) {
            g.addOutputTurnout(t.getDisplayName(), t.getState());
        }
    }

    /**
     * Sets the Sensor information for adding or editing.
     *
     * @param g the route to add the sensor to
     */
    protected void setSensorInformation(Route g) {
        for (RouteSensor s : _includedSensorList) {
            g.addOutputSensor(s.getDisplayName(), s.getState());
        }
    }

    /**
     * Set the Sensor, Turnout, and delay control information for adding or editing.
     *
     * @param g the route to configure
     */
    protected void setControlInformation(Route g) {
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

    /**
     * Set the sound file.
     */
    private void setSoundPressed() {
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

    /**
     * Set the script file.
     */
    private void setScriptPressed() {
        if (scriptChooser == null) {
            scriptChooser = ScriptFileChooser();
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


    protected void finishUpdate() {
        // move to show all Turnouts if not there
        cancelIncludedOnly();
        // Provide feedback to user
        // switch GUI back to selection mode
        //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
        status2.setVisible(true);
        autoSystemName();
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

    private void clearPage() {
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


    /**
     * Cancel included Turnouts only option
     */
    private void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    private String getClassName() {
        return this.getClass().getName();
    }

    List<RouteTurnout> get_turnoutList() {
        return _turnoutList;
    }

    List<RouteTurnout> get_includedTurnoutList() {
        return _includedTurnoutList;
    }

    List<RouteSensor> get_sensorList() {
        return _sensorList;
    }

    List<RouteSensor> get_includedSensorList() {
        return _includedSensorList;
    }

    public boolean isShowAll() {
        return showAll;
    }

    /**
     * Cancels edit mode
     */
    protected void cancelEdit() {
        if (editMode) {
            status1.setText(Bundle.getMessage("RouteAddStatusInitial1", Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
            //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
            finishUpdate();
            // get out of edit mode
            editMode = false;
            curRoute = null;
        }
        closeFrame();
    }

    /**
     * Respond to the Update button - update to Route Table.
     *
     * @param newRoute true if a new route; false otherwise
     */
    protected void updatePressed(boolean newRoute) {
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
        status1.setText((newRoute ? Bundle.getMessage("RouteAddStatusCreated") :
                Bundle.getMessage("RouteAddStatusUpdated")) + ": \"" + uName + "\" (" + _includedTurnoutList.size() + " "
                + Bundle.getMessage("Turnouts") + ", " + _includedSensorList.size() + " " + Bundle.getMessage("Sensors") + ")");
    }

    /**
     * Check name and return a new or existing Route object with the name as entered in the _systemName field on the
     * addFrame pane.
     *
     * @return the new/updated Route object
     */
    private Route checkNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText();
        String uName = _userName.getText();
        Route g;
        if (_autoSystemName.isSelected() && !editMode) {
            log.debug("checkNamesOK new autogroup");
            // create new Route with auto system name
            g = routeManager.newRoute(uName);
        } else {
            if (sName.length() == 0) {
                status1.setText(Bundle.getMessage("AddBeanStatusEnter"));
                status1.setForeground(Color.red);
                return null;
            }
            try {
                sName = routeManager.makeSystemName(sName);
                g = routeManager.provideRoute(sName, uName);
            } catch (IllegalArgumentException ex) {
                g = null; // for later check:
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

    protected void closeFrame(){
        // remind to save, if Route was created or edited
        if (routeDirty) {
            showReminderMessage();
            routeDirty = false;
        }
        // hide addFrame
        setVisible(false);

        // if in Edit, cancel edit mode
        if (editMode) {
            cancelEdit();
        }
        _routeSensorModel.dispose();
        _routeTurnoutModel.dispose();
        this.dispose();
    }
}
