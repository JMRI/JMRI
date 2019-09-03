package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalGroup;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.swing.RowSorterUtil;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignalGroup - Signal Head Edit Table.
 * <p>
 * Based in part on RouteTableAction.java and SignalGroupTableAction.java by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Egbert Broerse 2017

 */
public class SignalGroupSubTableAction {
    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */

    public SignalGroupSubTableAction(String s) {
    }

    public SignalGroupSubTableAction() {
        this("Signal Group Head Edit Table");
    } // NOI18N is never displayed on screen

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalGroupTable";
    }

    /**
     * Set choice for conditional evaluation.
     * <p>
     * Set to AND when you want all conditionals to be met for the Signal Head
     * to turn On when an included Aspect is shown on the main Mast.
     * Set to OR when you at least one of the conditionals to be met for the
     * Signal Head to turn On when an included Aspect is shown.
     *
     * See {@link #operFromBox}
     * 
     * @param mode True for AND
     * @param box the comboBox object to set
     */
    void setoperBox(boolean mode, JComboBox<String> box) {
        int _mode = 0; // OR
        if (mode) {
            _mode = 1; // AND
        }
        String result = jmri.util.StringUtil.getNameFromState(_mode, operValues, oper);
        box.setSelectedItem(result);
    }

    /**
     * Get the user choice for conditional evaluation.
     *
     * See {@link #setoperBox}
     * 
     * @param box the comboBox object containing the user choice
     * @return True for AND, False for OR
     */
    boolean operFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, operValues, oper);

        if (result < 0) {
            log.warn("unexpected mode string in Signal Head Appearance Mode: " + mode);
            throw new IllegalArgumentException();
        }
        if (result == 0) {
            return false;
        } else {
            return true;
        }
    }

    private static String[] oper = new String[]{"AND", "OR"};
    private static int[] operValues = new int[]{0x00, 0x01};

    /**
     * Get the user choice for a Signal Group Signal Head's On and Off Appearance
     * from a comboBox at the top of the Edit Head sub pane.
     *
     * @param box the comboBox object containing the user choice
     * @return Value for the Appearance (color) set i.e. 0 for DARK
     */
    int headStateFromBox(JComboBox<String> box) {
        SignalHead sig = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(curHeadName);
        int result;
        String mode;
        if (sig != null) {
            mode = (String) box.getSelectedItem();
            result = jmri.util.StringUtil.getStateFromName(mode, sig.getValidStates(), sig.getValidStateNames());
        } else {
            mode = (String) box.getSelectedItem();
            result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);
        }

        if (result < 0) {
            log.warn("unexpected mode string in signalHeadMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Set selected item in a Signal Group Signal Head's On and Off Appearance
     * in a comboBox at the top of the Edit Head sub pane.
     *
     * @param mode Value for an Appearance (color) i.e. 0 for DARK
     * @param box the comboBox object to set
     */
    void setSignalHeadStateBox(int mode, JComboBox<String> box) {
        SignalHead sig = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(curHeadName);
        if (sig != null) {
            String result = jmri.util.StringUtil.getNameFromState(mode, sig.getValidStates(), sig.getValidStateNames());
            box.setSelectedItem(result);
        } else {
            log.error("Failed to get signal head {}", curHeadName);
        }
    }

    /**
     * Get the user choice for a Sensor conditional's On state from the comboBox on the Edit Head sub pane.
     * See {@link #turnoutModeFromBox}
     * 
     * @param box the comboBox object containing the user choice
     * @return Value for ACTIVE/INACTIVE
     */
    int sensorModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in Signal Head Appearance: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Set selected item for a Sensor conditional's On state in the
     * comboBox on the Edit Head sub pane.
     *
     * See {@link #turnoutModeFromBox}
     * 
     * @param mode Value for ACTIVE/INACTIVE
     * @param box the comboBox object to set
     */
    void setSensorModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, sensorInputModeValues, sensorInputModes);
        box.setSelectedItem(result);
    }

    /**
     * Get the user choice for a Control Turnout conditional's On state
     * from the comboBox on the Edit Head sub pane.
     *
     * See {@link #sensorModeFromBox}
     * 
     * @param box the comboBox object containing the user choice
     * @return Value for CLOSED/THROWN
     */
    int turnoutModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutInputModeValues, turnoutInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Set selected item for a Control Turnout conditional's On state
     * in the comboBox on the Edit Head sub pane.
     *
     * See {@link #turnoutModeFromBox}
     * 
     * @param mode Value for CLOSED/THROWN
     * @param box the comboBox object to set
     */
    void setTurnoutModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }

    JLabel _systemName;
    JComboBox<String> _OnAppearance;
    JComboBox<String> _OffAppearance;
    JLabel spacer = new JLabel("       "); // to create space between On and Off Appearance choices
    JComboBox<String> _SensorTurnoutOper = new JComboBox<String>(oper);

    JmriJFrame addSubFrame = null;
    SignalGroupTurnoutModel _SignalGroupTurnoutModel;
    JScrollPane _SignalGroupTurnoutScrollPane;
    SignalGroupSensorModel _SignalGroupSensorModel;
    JScrollPane _SignalGroupSensorScrollPane;

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    JLabel nameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalHead")));
    JLabel signalOnStateLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OnAppearance")));
    JLabel signalOffStateLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OffAppearance")));
    JLabel userLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SelectConditionsOn")));

    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton updateSubButton = new JButton(Bundle.getMessage("ButtonApply"));

    static String updateInst = Bundle.getMessage("ClickToApply", Bundle.getMessage("ButtonApply"));

    JLabel status1 = new JLabel(updateInst);

    JPanel p2xt = null;   // Turnout list table
    JPanel p2xs = null;   // Sensor list table

    SignalGroup curSignalGroup = null;
    String curHeadName;
    SignalHead curSignalHead;

    /**
     * Open an editor to set the details of a Signal Head as part of a Signal Group.
     * Called when user clicks the Edit button for a Head in the Add/Edit Signal Group pane.
     *
     * @see SignalGroupTableAction#signalHeadEditPressed(int) SignalGroupTableAction.signalHeadEditPressed
     * @param g Parent Signal Head
     * @param headName System or User Name of this Signal Head
     */
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    void editHead(SignalGroup g, String headName) {
        curSignalGroup = g;
        curHeadName = headName;
        curSignalHead = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(curHeadName);
        if (curSignalHead != null) {
            _OnAppearance = new JComboBox<String>(curSignalHead.getValidStateNames()); // shows i18n strings from signal head definition
            _OffAppearance = new JComboBox<String>(curSignalHead.getValidStateNames());
        }
        _systemName = new JLabel(headName);
        _systemName.setVisible(true);

        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        List<String> systemNameList = tm.getSystemNameList();
        _turnoutList = new ArrayList<SignalGroupTurnout>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            Turnout turn = tm.getBySystemName(systemName);
            if (turn != null) {
                String userName = turn.getUserName();
                _turnoutList.add(new SignalGroupTurnout(systemName, userName));
            }
        }

        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        systemNameList = sm.getSystemNameList();
        _sensorList = new ArrayList<SignalGroupSensor>(systemNameList.size());
        iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            Sensor sen = sm.getBySystemName(systemName);
            if (sen != null) {
                String userName = sen.getUserName();
                _sensorList.add(new SignalGroupSensor(systemName, userName));
            }
        }
        initializeIncludedList();

        // Set up sub panel for editing of a Signal Group Signal Head item
        if (addSubFrame == null) { // create one if not yet available
            addSubFrame = new JmriJFrame((Bundle.getMessage("EditSignalGroup") + " - " + Bundle.getMessage("BeanNameSignalHead")), false, true);
            addSubFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalGroupAddEdit", true);
            addSubFrame.setLocation(100, 30);
            addSubFrame.getContentPane().setLayout(new BoxLayout(addSubFrame.getContentPane(), BoxLayout.Y_AXIS));
            Container contentPane = addSubFrame.getContentPane();
            // add system name label
            JPanel ps = new JPanel();
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(_systemName);
            contentPane.add(ps);
            // add user name label
            JPanel pc = new JPanel();
            pc.setLayout(new FlowLayout());
            pc.add(signalOnStateLabel);
            pc.add(_OnAppearance); // comboBox to set On Appearance
            _OnAppearance.setToolTipText(Bundle.getMessage("StateWhenMetTooltip"));
            pc.add(spacer);
            pc.add(signalOffStateLabel);
            pc.add(_OffAppearance); // comboBox to set On Appearance
            _OffAppearance.setToolTipText(Bundle.getMessage("StateWhenNotMetTooltip"));
            contentPane.add(pc);

            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(userLabel);
            contentPane.add(p);
            // fill in info for the Signal Head being configured
            if (curSignalHead.getClass().getName().contains("SingleTurnoutSignalHead")) {
                jmri.implementation.SingleTurnoutSignalHead stsh = (jmri.implementation.SingleTurnoutSignalHead) InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(curHeadName);
                // we may use a user name in the editing pane, so look for that first
                if (stsh == null) {
                    stsh = (jmri.implementation.SingleTurnoutSignalHead) InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(curHeadName);
                    // when user name is empty, get by user name
                }
                if (stsh != null) {
                    log.debug("SGsubTA #279 editHead: setting props for signal head {}", curHeadName);
                    if ((g.getHeadOnState(curSignalHead) == 0x00) && (g.getHeadOffState(curSignalHead) == 0x00)) {
                        g.setHeadOnState(curSignalHead, stsh.getOnAppearance());
                        g.setHeadOffState(curSignalHead, stsh.getOffAppearance());
                    }
                } else {
                    // nothing found
                    log.error("Failed to get signal head object named {}", curHeadName);
                }
            }
            setSignalHeadStateBox(g.getHeadOnState(curSignalHead), _OnAppearance);
            setSignalHeadStateBox(g.getHeadOffState(curSignalHead), _OffAppearance);

            // add Turnout Display Choice
            JPanel py = new JPanel();
            py.add(new JLabel(Bundle.getMessage("Show")));
            selGroup = new ButtonGroup();
            allButton = new JRadioButton(Bundle.getMessage("All"), true);
            selGroup.add(allButton);
            py.add(allButton);
            allButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of all Turnouts, if needed
                    if (!showAll) {
                        showAll = true;
                        _SignalGroupTurnoutModel.fireTableDataChanged();
                        _SignalGroupSensorModel.fireTableDataChanged();
                    }
                }
            });
            includedButton = new JRadioButton(Bundle.getMessage("Included"), false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of included Turnouts only, if needed
                    if (showAll) {
                        showAll = false;
                        initializeIncludedList();
                        _SignalGroupTurnoutModel.fireTableDataChanged();
                        _SignalGroupSensorModel.fireTableDataChanged();
                    }
                }
            });
            py.add(new JLabel("  " + Bundle.getMessage("_and_", Bundle.getMessage("Turnouts"), Bundle.getMessage("Sensors"))));
            contentPane.add(py);

            // add turnout table
            p2xt = new JPanel();
            JPanel p2xtSpace = new JPanel();
            p2xtSpace.setLayout(new BoxLayout(p2xtSpace, BoxLayout.Y_AXIS));
            p2xtSpace.add(new JLabel("XXX"));
            p2xt.add(p2xtSpace);

            JPanel p21t = new JPanel();
            p21t.setLayout(new BoxLayout(p21t, BoxLayout.Y_AXIS));
            p21t.add(new JLabel(Bundle.getMessage("SelectInGroup", Bundle.getMessage("Turnouts"))));
            p2xt.add(p21t);
            _SignalGroupTurnoutModel = new SignalGroupTurnoutModel();
            JTable SignalGroupTurnoutTable = new JTable(_SignalGroupTurnoutModel);
            TableRowSorter<SignalGroupTurnoutModel> sgtSorter = new TableRowSorter<>(_SignalGroupTurnoutModel);

            // use NamedBean's built-in Comparator interface for sorting the system name column
            RowSorterUtil.setSortOrder(sgtSorter, SignalGroupTurnoutModel.SNAME_COLUMN, SortOrder.ASCENDING);
            SignalGroupTurnoutTable.setRowSorter(sgtSorter);
            SignalGroupTurnoutTable.setRowSelectionAllowed(false);
            SignalGroupTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 80));

            SignalGroupSubTableAction.setRowHeight(SignalGroupTurnoutTable.getRowHeight());
            JComboBox<String> stateTCombo = new JComboBox<String>();
            stateTCombo.addItem(SET_TO_CLOSED);
            stateTCombo.addItem(SET_TO_THROWN);
            TableColumnModel SignalGroupTurnoutColumnModel = SignalGroupTurnoutTable.getColumnModel();
            TableColumn includeColumnT = SignalGroupTurnoutColumnModel.
                    getColumn(SignalGroupTurnoutModel.INCLUDE_COLUMN);
            includeColumnT.setResizable(false);
            includeColumnT.setMinWidth(50);
            includeColumnT.setMaxWidth(60);
            TableColumn sNameColumnT = SignalGroupTurnoutColumnModel.
                    getColumn(SignalGroupTurnoutModel.SNAME_COLUMN);
            sNameColumnT.setResizable(true);
            sNameColumnT.setMinWidth(75);
            sNameColumnT.setMaxWidth(95);
            TableColumn uNameColumnT = SignalGroupTurnoutColumnModel.
                    getColumn(SignalGroupTurnoutModel.UNAME_COLUMN);
            uNameColumnT.setResizable(true);
            uNameColumnT.setMinWidth(210);
            uNameColumnT.setMaxWidth(260);
            TableColumn stateColumnT = SignalGroupTurnoutColumnModel.
                    getColumn(SignalGroupTurnoutModel.STATE_COLUMN);
            stateColumnT.setCellEditor(new DefaultCellEditor(stateTCombo));
            stateColumnT.setResizable(false);
            stateColumnT.setMinWidth(90);
            stateColumnT.setMaxWidth(100);
            _SignalGroupTurnoutScrollPane = new JScrollPane(SignalGroupTurnoutTable);
            p2xt.add(_SignalGroupTurnoutScrollPane, BorderLayout.CENTER);
            contentPane.add(p2xt);
            p2xt.setVisible(true);

            JPanel po = new JPanel();
            po.setLayout(new FlowLayout());
            JLabel operLabel = new JLabel(Bundle.getMessage("ChooseOrAnd"));
            po.add(operLabel);
            po.add(_SensorTurnoutOper);
            contentPane.add(po);

            // add sensor table
            p2xs = new JPanel();
            JPanel p2xsSpace = new JPanel();
            p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
            p2xsSpace.add(new JLabel("XXX"));
            p2xs.add(p2xsSpace);

            JPanel p21s = new JPanel();
            p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
            p21s.add(new JLabel(Bundle.getMessage("SelectInGroup", Bundle.getMessage("Sensors"))));
            p2xs.add(p21s);
            _SignalGroupSensorModel = new SignalGroupSensorModel();
            JTable SignalGroupSensorTable = new JTable(_SignalGroupSensorModel);
            TableRowSorter<SignalGroupSensorModel> sgsSorter = new TableRowSorter<>(_SignalGroupSensorModel);

            // use NamedBean's built-in Comparator interface for sorting the system name column
            RowSorterUtil.setSortOrder(sgsSorter, SignalGroupSensorModel.SNAME_COLUMN, SortOrder.ASCENDING);
            SignalGroupSensorTable.setRowSorter(sgsSorter);
            SignalGroupSensorTable.setRowSelectionAllowed(false);
            SignalGroupSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 80));
            JComboBox<String> stateSCombo = new JComboBox<String>();
            stateSCombo.addItem(SET_TO_ACTIVE);
            stateSCombo.addItem(SET_TO_INACTIVE);
            TableColumnModel SignalGroupSensorColumnModel = SignalGroupSensorTable.getColumnModel();
            TableColumn includeColumnS = SignalGroupSensorColumnModel.
                    getColumn(SignalGroupSensorModel.INCLUDE_COLUMN);
            includeColumnS.setResizable(false);
            includeColumnS.setMinWidth(50);
            includeColumnS.setMaxWidth(60);
            TableColumn sNameColumnS = SignalGroupSensorColumnModel.
                    getColumn(SignalGroupSensorModel.SNAME_COLUMN);
            sNameColumnS.setResizable(true);
            sNameColumnS.setMinWidth(75);
            sNameColumnS.setMaxWidth(95);
            TableColumn uNameColumnS = SignalGroupSensorColumnModel.
                    getColumn(SignalGroupSensorModel.UNAME_COLUMN);
            uNameColumnS.setResizable(true);
            uNameColumnS.setMinWidth(210);
            uNameColumnS.setMaxWidth(260);
            TableColumn stateColumnS = SignalGroupSensorColumnModel.
                    getColumn(SignalGroupSensorModel.STATE_COLUMN);
            stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo));
            stateColumnS.setResizable(false);
            stateColumnS.setMinWidth(90);
            stateColumnS.setMaxWidth(100);
            _SignalGroupSensorScrollPane = new JScrollPane(SignalGroupSensorTable);
            p2xs.add(_SignalGroupSensorScrollPane, BorderLayout.CENTER);
            contentPane.add(p2xs);
            p2xs.setVisible(true);

            // add notes panel
            JPanel pa = new JPanel();
            pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            status1.setFont(status1.getFont().deriveFont(0.9f * nameLabel.getFont().getSize())); // a bit smaller
            status1.setForeground(Color.gray);
            p1.add(status1);
            pa.add(p1);
            Border pBorder = BorderFactory.createEtchedBorder();
            pa.setBorder(pBorder);
            contentPane.add(pa);

            // add buttons - Add SignalGroup button
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout(FlowLayout.TRAILING));
            // add Cancel button
            pb.add(cancelButton);
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelSubPressed(e);
                }
            });
            // add Update SignalGroup button
            pb.add(updateSubButton);
            updateSubButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateSubPressed(e, false);
                }
            });
            updateSubButton.setToolTipText(Bundle.getMessage("TooltipUpdateGroup"));

            p2xtSpace.setVisible(false);
            p2xsSpace.setVisible(false);
            updateSubButton.setVisible(true);
            contentPane.add(pb);
            addSubFrame.pack();
        }
        // set listener for window closing
        addSubFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                addSubFrame.setVisible(false);
                cancelSubEdit();
                _SignalGroupSensorModel.dispose();
                _SignalGroupTurnoutModel.dispose();
            }
        });
        addSubFrame.setVisible(true);
        // add AND/OR choice box
        setoperBox(curSignalGroup.getSensorTurnoutOper(curSignalHead), _SensorTurnoutOper);
        setSignalHeadStateBox(curSignalGroup.getHeadOnState(curSignalHead), _OnAppearance);
        setSignalHeadStateBox(curSignalGroup.getHeadOffState(curSignalHead), _OffAppearance);
        int setRow = 0;
        for (int i = _turnoutList.size() - 1; i >= 0; i--) {
            SignalGroupTurnout turnout = _turnoutList.get(i);
            Turnout tTurnout = turnout.getTurnout();
            if (curSignalGroup.isTurnoutIncluded(curSignalHead, tTurnout)) {
                turnout.setIncluded(true);
                turnout.setState(curSignalGroup.getTurnoutState(curSignalHead, tTurnout));
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
        _SignalGroupTurnoutScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _SignalGroupTurnoutModel.fireTableDataChanged();

        for (int i = _sensorList.size() - 1; i >= 0; i--) {
            SignalGroupSensor sensor = _sensorList.get(i);
            Sensor tSensor = sensor.getSensor();
            if (curSignalGroup.isSensorIncluded(curSignalHead, tSensor)) {
                sensor.setIncluded(true);
                sensor.setState(curSignalGroup.getSensorState(curSignalHead, tSensor));
                setRow = i;
            } else {
                sensor.setIncluded(false);
                sensor.setState(Sensor.INACTIVE);
            }
        }

        status1.setText(updateInst);
        updateSubButton.setVisible(true);
    }

    /**
     * Configure column widths for the Turnout and Sensor Conditional tables.
     *
     * @param table JTable to put button in
     * @param column index of column in table
     * @param sample sample button to use as spacer
     */
    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth((sample.getPreferredSize().width) + 4);
    }

    /**
     * Initialize the list of included turnouts and sensors for a
     * Signal Head item on the sub pane.
     */
    void initializeIncludedList() {
        _includedTurnoutList = new ArrayList<SignalGroupTurnout>();
        for (int i = 0; i < _turnoutList.size(); i++) {
            if (_turnoutList.get(i).isIncluded()) {
                _includedTurnoutList.add(_turnoutList.get(i));
            }
        }
        _includedSensorList = new ArrayList<SignalGroupSensor>();
        for (int i = 0; i < _sensorList.size(); i++) {
            if (_sensorList.get(i).isIncluded()) {
                _includedSensorList.add(_sensorList.get(i));
            }
        }
    }

    /**
     * Set the Turnout information for adding or editing.
     *
     * @param g The Signal Group being configured
     * @return total number of turnouts included in group
     */
    int setTurnoutInformation(SignalGroup g) {
        for (int i = 0; i < _includedTurnoutList.size(); i++) {
            SignalGroupTurnout t = _includedTurnoutList.get(i);
            g.setHeadAlignTurnout(curSignalHead, t.getTurnout(), t.getState());
        }
        return _includedTurnoutList.size();
    }

    /**
     * Set the Sensor information for adding or editing.
     *
     * @param g The Signal Group being configured
     * @return total number of sensors included in group
     */
    int setSensorInformation(SignalGroup g) {
        for (int i = 0; i < _includedSensorList.size(); i++) {
            SignalGroupSensor s = _includedSensorList.get(i);
            g.setHeadAlignSensor(curSignalHead, s.getSensor(), s.getState());
        }
        return _includedSensorList.size();
    }

    /**
     * Respond to the Cancel button - clean up.
     *
     * @param e the event heard
     */
    void cancelSubPressed(ActionEvent e) {
        log.debug("Edit Signal Group Head canceled in SGSTA line 569");
        cancelSubEdit();
        _SignalGroupSensorModel.dispose();
        _SignalGroupTurnoutModel.dispose();
    }

    /**
     * Respond to the Update button on the Edit Head sub pane - update to SignalGroup.
     *
     * @param e the event heard
     * @param newSignalGroup True if this is a newly created Signal
     *                       Group for which additional actions are required
     */
    void updateSubPressed(ActionEvent e, boolean newSignalGroup) {
        curSignalGroup.clearHeadTurnout(curSignalHead);
        curSignalGroup.clearHeadSensor(curSignalHead);
        // store the new configuration as entered by the user
        initializeIncludedList();
        setTurnoutInformation(curSignalGroup);
        setSensorInformation(curSignalGroup);
        curSignalGroup.setHeadOnState(curSignalHead, headStateFromBox(_OnAppearance));
        curSignalGroup.setHeadOffState(curSignalHead, headStateFromBox(_OffAppearance));
        // store AND/OR operand user choice
        curSignalGroup.setSensorTurnoutOper(curSignalHead, operFromBox(_SensorTurnoutOper));
        // add control Sensors and a control Turnouts if entered in the window
        finishUpdate();
    }

    /**
     * Clean up the interface and reset Included radio button to All for next use
     */
    void finishUpdate() {
        // show all turnouts and sensors if not yet set to All
        cancelIncludedOnly();
        updateSubButton.setVisible(false);
        addSubFrame.setVisible(false);
    }

    /**
     * Cancel edit mode
     */
    void cancelSubEdit() {
        // get out of edit mode
        curSignalGroup = null;
        finishUpdate();
    }

    /**
     * Cancel included Turnouts and Sensors only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Base table model for selecting Signal Group Head control Conditionals
     */
    public abstract class SignalGroupOutputModel extends AbstractTableModel implements PropertyChangeListener {

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

        public String getDisplayName(int r) {
            if (((String) getValueAt(r, UNAME_COLUMN) != null) || (!((String) getValueAt(r, UNAME_COLUMN)).equals(""))) {
                return (String) getValueAt(r, UNAME_COLUMN);
            } else {
                return (String) getValueAt(r, SNAME_COLUMN);
            }
        }

    }

    /**
     * Table model for selecting Turnouts and their On State
     * as Conditionals for a Signal Group Signal Head member.
     */
    class SignalGroupTurnoutModel extends SignalGroupOutputModel {

        SignalGroupTurnoutModel() {
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
            ArrayList<SignalGroupTurnout> turnoutList = null;
            if (showAll) {
                turnoutList = _turnoutList;
            } else {
                turnoutList = _includedTurnoutList;
            }
            // some error checking
            if (r >= turnoutList.size()) {
                log.debug("SGSTA getValueAt #703: row index is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(turnoutList.get(r).isIncluded());
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
            ArrayList<SignalGroupTurnout> turnoutList = null;
            if (showAll) {
                turnoutList = _turnoutList;
            } else {
                turnoutList = _includedTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    turnoutList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_COLUMN:
                    turnoutList.get(r).setSetToState((String) type);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Set up a table for selecting Sensors and Sensor On State
     * as Conditionals for a Signal Group Signal Head member.
     */
    class SignalGroupSensorModel extends SignalGroupOutputModel {

        SignalGroupSensorModel() {
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
            ArrayList<SignalGroupSensor> sensorList = null;
            if (showAll) {
                sensorList = _sensorList;
            } else {
                sensorList = _includedSensorList;
            }
            // some error checking
            if (r >= sensorList.size()) {
                log.debug("SGSTA getValueAt #766: row is greater than sensor list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(sensorList.get(r).isIncluded());
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
            ArrayList<SignalGroupSensor> sensorList = null;
            if (showAll) {
                sensorList = _sensorList;
            } else {
                sensorList = _includedSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    sensorList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_COLUMN:
                    sensorList.get(r).setSetToState((String) type);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean showAll = true; // false indicates: show only included Turnouts and Sensors

    private static int ROW_HEIGHT;

    private static String[] COLUMN_NAMES = {Bundle.getMessage("ColumnSystemName"),
            Bundle.getMessage("ColumnUserName"),
            Bundle.getMessage("Include"),
            Bundle.getMessage("ColumnLabelSetState")};
    private static String SET_TO_ACTIVE = Bundle.getMessage("SensorStateActive");
    private static String SET_TO_INACTIVE = Bundle.getMessage("SensorStateInactive");
    private static String SET_TO_CLOSED = InstanceManager.turnoutManagerInstance().getClosedText();
    private static String SET_TO_THROWN = InstanceManager.turnoutManagerInstance().getThrownText();

    private static String[] sensorInputModes = new String[]{Bundle.getMessage("SensorStateActive"), Bundle.getMessage("SensorStateInactive")};
    private static int[] sensorInputModeValues = new int[]{SignalGroup.ONACTIVE, SignalGroup.ONINACTIVE};

    private static String[] signalStates = new String[]{Bundle.getMessage("SignalHeadStateDark"), Bundle.getMessage("SignalHeadStateRed"), Bundle.getMessage("SignalHeadStateYellow"), Bundle.getMessage("SignalHeadStateGreen"), Bundle.getMessage("SignalHeadStateLunar")};
    private static int[] signalStatesValues = new int[]{SignalHead.DARK, SignalHead.RED, SignalHead.YELLOW, SignalHead.GREEN, SignalHead.LUNAR};

    private static String[] turnoutInputModes = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
        InstanceManager.turnoutManagerInstance().getThrownText()};
    private static int[] turnoutInputModeValues = new int[]{SignalGroup.ONCLOSED, SignalGroup.ONTHROWN};

    private ArrayList<SignalGroupTurnout> _turnoutList;      // array of all Turnouts
    private ArrayList<SignalGroupTurnout> _includedTurnoutList;

    private ArrayList<SignalGroupSensor> _sensorList;        // array of all Sensors
    private ArrayList<SignalGroupSensor> _includedSensorList;

    private synchronized static void setRowHeight(int newVal) {
        ROW_HEIGHT = newVal;
    }

    private abstract class SignalGroupElement {

        String _sysName;
        String _userName;
        boolean _included;
        int _setToState;

        SignalGroupElement(String sysName, String userName) {
            _sysName = sysName;
            _userName = userName;
            _included = false;
            _setToState = Sensor.INACTIVE;
        }

        String getSysName() {
            return _sysName;
        }

        String getUserName() {
            return _userName;
        }

        boolean isIncluded() {
            return _included;
        }

        void setIncluded(boolean include) {
            _included = include;
        }

        abstract String getSetToState();

        abstract void setSetToState(String state);

        int getState() {
            return _setToState;
        }

        void setState(int state) {
            _setToState = state;
        }

    }

    /**
     * Element containing Signal Group configuration information
     * for a Control Sensor as Conditional.
     */
    private class SignalGroupSensor extends SignalGroupElement {

        /**
         * Create a Sensor item for this Signal Head by the name of the Control Sensor
         * @param sysName system name for new signal group sensor
         * @param userName user name for new signal group sensor
         */
        SignalGroupSensor(String sysName, String userName) {
            super(sysName, userName);
        }

        /**
         * Get the configured On state for the Control Sensor Conditional to be True.
         *
         * @return A string describing the On state for use in the GUI
         * (read from a Properties file, localizable)
         */
        @Override
        String getSetToState() {
            switch (_setToState) {
                case Sensor.INACTIVE:
                    return SET_TO_INACTIVE;
                case Sensor.ACTIVE:
                    return SET_TO_ACTIVE;
                default:
                    // fall through
                    break;
            }
            return "";
        }

        /**
         * Store a uniform value for the On state of the Control Sensor Conditional.
         * <p>
         * Pairs should correspond with values in getSetToState()
         *
         * @param state Choice from the comboBox, localizable i.e. Active
         */
        @Override
        void setSetToState(String state) {
            if (SET_TO_INACTIVE.equals(state)) {
                _setToState = Sensor.INACTIVE;
            } else if (SET_TO_ACTIVE.equals(state)) {
                _setToState = Sensor.ACTIVE;
            }
        }

        /**
         * Get the Sensor object.
         *
         * @return The Sensor Bean acting as Control Sensor for this Head and Group
         */
        Sensor getSensor() {
            return jmri.InstanceManager.sensorManagerInstance().getSensor(_sysName);
        }
    }

    /**
     * Element containing Signal Group configuration information
     * for a Control Turnout as Conditional.
     */
    private class SignalGroupTurnout extends SignalGroupElement {

        /**
         * Create a Turnout item for this Signal Head by the name of the Control Turnout.
         *
         * @param sysName system name for new signal group turnout
         * @param userName user name for new signal group turnout
         */
        SignalGroupTurnout(String sysName, String userName) {
            super(sysName, userName);
        }

        /**
         * Get the configured On state for the Control Turnout Conditional to be True.
         *
         * @return A string describing the On state for use in the GUI
         */
        @Override
        String getSetToState() {
            switch (_setToState) {
                case Turnout.CLOSED:
                    return SET_TO_CLOSED;
                case Turnout.THROWN:
                    return SET_TO_THROWN;
                default:
                    // fall through
                    break;
            }
            return "";
        }

        /**
         * Store a uniform value for the On state of the Control Sensor Conditional.
         * Pairs should correspond with values in getSetToState().
         *
         * @param state Choice from the comboBox, localizable i.e. Thrown.
         */
        @Override
        void setSetToState(String state) {
            if (SET_TO_CLOSED.equals(state)) {
                _setToState = Turnout.CLOSED;
            } else if (SET_TO_THROWN.equals(state)) {
                _setToState = Turnout.THROWN;
            }
        }

        /**
         * Get the Turnout object.
         *
         * @return The Turnout Bean acting as Control Turnout for this Head and Group
         */
        Turnout getTurnout() {
            return jmri.InstanceManager.turnoutManagerInstance().getTurnout(_sysName);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalGroupSubTableAction.class);

}
