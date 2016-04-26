package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Route;
import jmri.Sensor;
import jmri.Turnout;
import jmri.implementation.DefaultConditionalAction;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a Route Table
 *
 * Based in part on SignalHeadTableAction.java by Bob Jacobsen
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 *
 */
public class RouteTableAction extends AbstractTableAction {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s
     */
    public RouteTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Route manager available
        if (jmri.InstanceManager.routeManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public RouteTableAction() {
        this(Bundle.getMessage("TitleRouteTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Routes
     */
    protected void createModel() {

        // late initialization of string "constants" so that TurnoutManager 
        // has time to be fully configured
        SET_TO_CLOSED = Bundle.getMessage("Set") + " "
            + InstanceManager.turnoutManagerInstance().getClosedText();
        SET_TO_THROWN = Bundle.getMessage("Set") + " "
            + InstanceManager.turnoutManagerInstance().getThrownText();
        turnoutInputModes = new String[]{
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getClosedText(),
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getThrownText(),
                Bundle.getMessage("OnConditionChange"),
                "Veto Closed",
                "Veto Thrown"
        };
        lockTurnoutInputModes = new String[]{
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getClosedText(),
                Bundle.getMessage("OnCondition") + " " + InstanceManager.turnoutManagerInstance().getThrownText(),
                Bundle.getMessage("OnConditionChange")
        };

        m = new BeanTableDataModel() {
            static public final int ENABLECOL = NUMCOLUMN;
            static public final int LOCKCOL = ENABLECOL + 1;
            static public final int SETCOL = ENABLECOL + 2;

            public int getColumnCount() {
                return NUMCOLUMN + 3;
            }

            public String getColumnName(int col) {
                if (col == VALUECOL) {
                    return "";  // no heading on "Set"
                }
                if (col == SETCOL) {
                    return "";    // no heading on "Edit"
                }
                if (col == ENABLECOL) {
                    return Bundle.getMessage("ColumnHeadEnabled");
                }
                if (col == LOCKCOL) {
                    return Bundle.getMessage("Locked");
                } else {
                    return super.getColumnName(col);
                }
            }

            public Class<?> getColumnClass(int col) {
                if (col == SETCOL) {
                    return JButton.class;
                }
                if (col == ENABLECOL) {
                    return Boolean.class;
                }
                if (col == LOCKCOL) {
                    return Boolean.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            public int getPreferredWidth(int col) {
                if (col == SETCOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == ENABLECOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == LOCKCOL) {
                    return new JTextField(6).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            public boolean isCellEditable(int row, int col) {
                if (col == USERNAMECOL) {
                    return true;
                }
                if (col == SETCOL) {
                    return true;
                }
                if (col == ENABLECOL) {
                    return true;
                }
                // Route lock is available if turnouts are lockable
                if (col == LOCKCOL) {
                    Route r = (Route) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    return r.canLock();
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            public Object getValueAt(int row, int col) {
                if (col == SETCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == ENABLECOL) {
                    return Boolean.valueOf(((Route) getBySystemName((String) getValueAt(row, SYSNAMECOL))).getEnabled());
                } else if (col == LOCKCOL) {
                    Route r = (Route) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    if (r.canLock()) {
                        return Boolean.valueOf(((Route) getBySystemName((String) getValueAt(row, SYSNAMECOL))).getLocked());
                    } else {
                        // this covers the case when route was locked and lockable turnouts were removed from the route 
                        r.setLocked(false);
                        return Boolean.valueOf(false);
                    }
                } else {
                    return super.getValueAt(row, col);
                }
            }

            public void setValueAt(Object value, int row, int col) {
                if (col == USERNAMECOL) {
                    //Directly changing the username should only be possible if the username was previously null or ""
                    // check to see if user name already exists
                    if (((String) value).equals("")) {
                        value = null;
                    } else {
                        NamedBean nB = getByUserName((String) value);
                        if (nB != null) {
                            log.error("User Name is not unique " + value);
                            String msg;
                            msg = Bundle.getMessage("WarningUserName", new Object[]{("" + value)});
                            JOptionPane.showMessageDialog(null, msg,
                                    Bundle.getMessage("WarningTitle"),
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    NamedBean nBean = getBySystemName(sysNameList.get(row));
                    nBean.setUserName((String) value);
                    fireTableRowsUpdated(row, row);
                } else if (col == SETCOL) {
                    // set up to edit. Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            addPressed(null);
                            _systemName.setText((String) getValueAt(row, SYSNAMECOL));
                            editPressed(null); // don't really want to stop Route w/o user action
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else if (col == ENABLECOL) {
                    // alternate
                    Route r = (Route) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    boolean v = r.getEnabled();
                    r.setEnabled(!v);
                } else if (col == LOCKCOL) {
                    // alternate
                    Route r = (Route) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    boolean v = r.getLocked();
                    r.setLocked(!v);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            public void configureTable(JTable table) {
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                super.configureTable(table);
            }

            /**
             * Delete the bean after all the checking has been done.
             * <P>
             * Deactivate the Route, then use the superclass to delete it.
             */
            void doDelete(NamedBean bean) {
                ((Route) bean).deActivateRoute();
                super.doDelete(bean);
            }

            // want to update when enabled parameter changes
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Enabled")) { //NOI18N
                    return true;
                }
                if (e.getPropertyName().equals("Locked")) { //NOI18N
                    return true;
                } else {
                    return super.matchPropertyName(e);
                }
            }

            public Manager getManager() {
                return jmri.InstanceManager.routeManagerInstance();
            }

            public NamedBean getBySystemName(String name) {
                return jmri.InstanceManager.routeManagerInstance().getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return jmri.InstanceManager.routeManagerInstance().getByUserName(name);
            }

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                ((Route) t).setRoute();
            }

            public String getValue(String s) {
                return Bundle.getMessage("Set");
                //Title of Set button in Route table
            }

            //public JButton configureButton() {
            //    return new JButton("Foo"));
                //not used
            //}

            protected String getBeanType() {
                return Bundle.getMessage("BeanNameRoute");
            }
            /*Routes do not get references by other parts of the code, we therefore 
             do not need to worry about controlling how the username is changed
             */

            protected void showPopup(MouseEvent e) {
            }
        };
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleRouteTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.RouteTable";
    }

    int sensorModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in sensorMode: " + mode);
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
            log.warn("unexpected mode string in turnoutMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setTurnoutModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }

    JTextField _systemName = new JTextField(10);
    JTextField _userName = new JTextField(22);
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";
    jmri.UserPreferencesManager pref;

    JmriJFrame addFrame = null;
    RouteTurnoutModel _routeTurnoutModel;
    JScrollPane _routeTurnoutScrollPane;
    RouteSensorModel _routeSensorModel;
    JScrollPane _routeSensorScrollPane;

    JTextField soundFile = new JTextField(20);
    JTextField scriptFile = new JTextField(20);
    JmriBeanComboBox turnoutsAlignedSensor;

    JmriBeanComboBox sensor1;

    JComboBox<String> sensor1mode = new JComboBox<String>(sensorInputModes);
    JmriBeanComboBox sensor2;
    JComboBox<String> sensor2mode = new JComboBox<String>(sensorInputModes);
    JmriBeanComboBox sensor3;
    JComboBox<String> sensor3mode = new JComboBox<String>(sensorInputModes);

    JmriBeanComboBox cTurnout;
    JmriBeanComboBox cLockTurnout;
    JTextField timeDelay = new JTextField(5);

    JComboBox<String> cTurnoutStateBox = new JComboBox<String>(turnoutInputModes);
    JComboBox<String> cLockTurnoutStateBox = new JComboBox<String>(lockTurnoutInputModes);

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    JLabel nameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");

    JButton createButton = new JButton(Bundle.getMessage("ButtonCreate"));
    JButton editButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton cancelEditButton = new JButton(Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit")));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete") + " " + Bundle.getMessage("BeanNameRoute"));
    JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
    JButton exportButton = new JButton(Bundle.getMessage("ButtonExport"));

    static String createInst = Bundle.getMessage("RouteAddStatusInitial1", Bundle.getMessage("ButtonCreate"));
    static String editInst = Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit"));
    static String updateInst = Bundle.getMessage("RouteAddStatusInitial3", Bundle.getMessage("ButtonUpdate"));
    static String cancelInst = Bundle.getMessage("RouteAddStatusInitial4", Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit")));

    JLabel status1 = new JLabel(createInst);
    JLabel status2 = new JLabel(editInst);

    JPanel p2xt = null;   // Turnout list table
    JPanel p2xs = null;   // Sensor list table

    Route curRoute = null;
    boolean routeDirty = false;  // true to fire reminder to save work
    boolean editMode = false;

    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (editMode) {
            cancelEdit();
        }
        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        List<String> systemNameList = tm.getSystemNameList();
        _turnoutList = new ArrayList<RouteTurnout>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            String userName = tm.getBySystemName(systemName).getUserName();
            _turnoutList.add(new RouteTurnout(systemName, userName));
        }

        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        systemNameList = sm.getSystemNameList();
        _sensorList = new ArrayList<RouteSensor>(systemNameList.size());
        iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            String userName = sm.getBySystemName(systemName).getUserName();
            _sensorList.add(new RouteSensor(systemName, userName));
        }
        initializeIncludedList();

        // Set up window
        if (addFrame == null) {
            turnoutsAlignedSensor = new JmriBeanComboBox(InstanceManager.sensorManagerInstance());
            sensor1 = new JmriBeanComboBox(InstanceManager.sensorManagerInstance());
            sensor2 = new JmriBeanComboBox(InstanceManager.sensorManagerInstance());
            sensor3 = new JmriBeanComboBox(InstanceManager.sensorManagerInstance());
            cTurnout = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance());
            cLockTurnout = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance());
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddRoute"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.RouteAddEdit", true);
            addFrame.setLocation(100, 30);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            // add system name
            JPanel ps = new JPanel();
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(_systemName);
            ps.add(_autoSystemName);
            _autoSystemName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    autoSystemName();
                }
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
            allButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of all Turnouts, if needed
                    if (!showAll) {
                        showAll = true;
                        _routeTurnoutModel.fireTableDataChanged();
                        _routeSensorModel.fireTableDataChanged();
                    }
                }
            });
            includedButton = new JRadioButton(Bundle.getMessage("Included"), false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of included Turnouts only, if needed
                    if (showAll) {
                        showAll = false;
                        initializeIncludedList();
                        _routeTurnoutModel.fireTableDataChanged();
                        _routeSensorModel.fireTableDataChanged();
                    }
                }
            });
            py.add(new JLabel(Bundle.getMessage("_and_", Bundle.getMessage("Turnouts"), Bundle.getMessage("Sensors"))));
            // keys in jmri.jmrit.Bundle
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
            JTable routeTurnoutTable = jmri.util.JTableUtil.sortableDataModel(_routeTurnoutModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) routeTurnoutTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(RouteTurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {
            }  // if not a sortable table model
            routeTurnoutTable.setRowSelectionAllowed(false);
            routeTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 80));

            ROW_HEIGHT = routeTurnoutTable.getRowHeight();
            JComboBox<String> stateTCombo = new JComboBox<String>();
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
            JTable routeSensorTable = jmri.util.JTableUtil.sortableDataModel(_routeSensorModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) routeSensorTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(RouteSensorModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {
            }  // if not a sortable table model
            routeSensorTable.setRowSelectionAllowed(false);
            routeSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 80));
            JComboBox<String> stateSCombo = new JComboBox<String>();
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
            JButton ss = new JButton("...");
            ss.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSoundPressed();
                }
            });
            ss.setToolTipText(Bundle.getMessage("TooltipOpenFile", Bundle.getMessage("BeanNameAudio")));
            p25.add(ss);
            p25.add(new JLabel(Bundle.getMessage("LabelRunScript")));
            p25.add(scriptFile);
            ss = new JButton("...");
            ss.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setScriptPressed();
                }
            });
            ss.setToolTipText(Bundle.getMessage("TooltipOpenFile", Bundle.getMessage("Script")));
            p25.add(ss);
            contentPanel.add(p25);

            //add turnouts aligned Sensor
            JPanel p27 = new JPanel();
            p27.setLayout(new FlowLayout());
            p27.add(new JLabel(Bundle.getMessage("LabelEnterSensorAligned")));
            p27.add(turnoutsAlignedSensor);
            turnoutsAlignedSensor.setFirstItemBlank(true);
            turnoutsAlignedSensor.setSelectedBean(null);
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

            sensor1.setFirstItemBlank(true);
            sensor2.setFirstItemBlank(true);
            sensor3.setFirstItemBlank(true);
            sensor1.setSelectedBean(null);
            sensor2.setSelectedBean(null);
            sensor3.setSelectedBean(null);
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
            p34.add(new JLabel(Bundle.getMessage("BeanNameTurnout") + ":"));
            p34.add(cTurnout);
            cTurnout.setFirstItemBlank(true);
            cTurnout.setSelectedBean(null);
            cTurnout.setToolTipText(Bundle.getMessage("TooltipEnterTurnout"));
            p34.add(new JLabel("   " + Bundle.getMessage("LabelCondition") + ":"));
            cTurnoutStateBox.setToolTipText(Bundle.getMessage("TooltipTurnoutCondition"));
            p34.add(cTurnoutStateBox);
            p3.add(p34);
            // add added delay
            JPanel p36 = new JPanel();
            p36.add(new JLabel(Bundle.getMessage("LabelTurnoutDelay") + ":"));
            p36.add(timeDelay);
            timeDelay.setText("0");
            timeDelay.setToolTipText(Bundle.getMessage("TooltipTurnoutDelay"));
            p36.add(new JLabel(" " + Bundle.getMessage("LabelMilliseconds")));
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
            p44.add(new JLabel(Bundle.getMessage("BeanNameTurnout") + ":"));
            p44.add(cLockTurnout);
            cLockTurnout.setFirstItemBlank(true);
            cLockTurnout.setSelectedBean(null);
            cLockTurnout.setToolTipText(Bundle.getMessage("TooltipEnterTurnout"));
            p44.add(new JLabel("   " + Bundle.getMessage("LabelCondition") + ":"));
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
            p1.add(status1);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
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
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelAddPressed(e);
                }
            });
            // CancelEdit button
            pb.add(cancelEditButton);
            cancelEditButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancelEditButton.setToolTipText(Bundle.getMessage("TooltipCancelRoute"));
            // Add Create Route button
            pb.add(createButton);
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            createButton.setToolTipText(Bundle.getMessage("TooltipCreateRoute"));
            // Edit Route button 
            pb.add(editButton);
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editPressed(e);
                }
            });
            editButton.setToolTipText(Bundle.getMessage("TooltipEditRoute"));
            // Delete Route button
            pb.add(deleteButton);
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            deleteButton.setToolTipText(Bundle.getMessage("TooltipDeleteRoute"));
            // Update Route button
            pb.add(updateButton);
            updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e, false);
                }
            });
            updateButton.setToolTipText(Bundle.getMessage("TooltipUpdateRoute"));
            // Export button
            pb.add(exportButton);
            exportButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exportPressed(e);
                }
            });
            exportButton.setToolTipText(Bundle.getMessage("TooltipExportRoute"));

            // Show the initial buttons, and hide the others
            exportButton.setVisible(false);
            cancelButton.setVisible(false); // test without Cancel Add button
            cancelEditButton.setVisible(false);
            updateButton.setVisible(true);
            editButton.setVisible(true);
            createButton.setVisible(true);
            deleteButton.setVisible(false);
            contentPanel.add(pb);

            addFrame.getContentPane().add(new JScrollPane(contentPanel), BorderLayout.CENTER);

            // pack and release space
            addFrame.pack();
            p2xsSpace.setVisible(false);
            p2xtSpace.setVisible(false);
        }
        // set listener for window closing
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                // remind to save, if Route was created or edited
                if (routeDirty) {
                    InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemRouteTable")),
                                    getClassName(),
                                    "remindSaveRoute"); // NOI18N
                    routeDirty = false;
                }
                _autoSystemName.setSelected(false); // prevent automatic creation next time
                // hide addFrame
                if (addFrame != null) {
                    addFrame.setVisible(false);
                    addFrame.dispose();
                    addFrame = null;
                }
                // if in Edit, cancel edit mode
                if (editMode) {
                    cancelEdit();
                }
                _routeSensorModel.dispose();
                _routeTurnoutModel.dispose();
            }
        });
        // display the window
        addFrame.setVisible(true);
        autoSystemName();
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

    /**
     * Initialize list of included turnout positions
     */
    void initializeIncludedList() {
        _includedTurnoutList = new ArrayList<RouteTurnout>();
        for (int i = 0; i < _turnoutList.size(); i++) {
            if (_turnoutList.get(i).isIncluded()) {
                _includedTurnoutList.add(_turnoutList.get(i));
            }
        }
        _includedSensorList = new ArrayList<RouteSensor>();
        for (int i = 0; i < _sensorList.size(); i++) {
            if (_sensorList.get(i).isIncluded()) {
                _includedSensorList.add(_sensorList.get(i));
            }
        }
    }

    /**
     * Responds to the Add button
     */
    void createPressed(ActionEvent e) {

        if (!_autoSystemName.isSelected()) {
            if (!checkNewNamesOK()) {
                return;
            }
        }
        updatePressed(e, true);
        status2.setText(editInst);
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        // activate the route
    }

    boolean checkNewNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText();
        String uName = _userName.getText();
        if (sName.length() == 0) {
            status1.setText(Bundle.getMessage("RouteAddStatusEnter"));
            return false;
        }
        Route g = null;
        // check if a Route with the same user name exists
        if (!uName.equals("")) {
            g = jmri.InstanceManager.routeManagerInstance().getByUserName(uName);
            if (g != null) {
                // Route with this user name already exists
                status1.setText(Bundle.getMessage("LightError8"));
                return false;
            } else {
                return true;
            }
        }
        // check if a Route with this system name already exists
        g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g != null) {
            // Route already exists
            status1.setText(Bundle.getMessage("LightError1"));
            return false;
        }
        return true;
    }

    Route checkNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText();
        String uName = _userName.getText();
        Route g;
        if (_autoSystemName.isSelected() && !editMode) {
            // create new Route with auto system name
            g = jmri.InstanceManager.routeManagerInstance().newRoute(uName);
        } else {
            if (sName.length() == 0) {
                status1.setText(Bundle.getMessage("RouteAddStatusEnter"));
                return null;
            }
            g = jmri.InstanceManager.routeManagerInstance().provideRoute(sName, uName);
        }
        if (g == null) {
            // should never get here
            log.error("Unknown failure to create Route with System Name: " + sName); //NOI18N
        } else {
            g.deActivateRoute();
        }
        return g;
    }

    /**
     * Sets the Turnout information for adding or editting
     */
    int setTurnoutInformation(Route g) {
        for (int i = 0; i < _includedTurnoutList.size(); i++) {
            RouteTurnout t = _includedTurnoutList.get(i);
            g.addOutputTurnout(t.getSysName(), t.getState());
        }
        return _includedTurnoutList.size();
    }

    /**
     * Sets the Sensor information for adding or editting
     */
    int setSensorInformation(Route g) {
        for (int i = 0; i < _includedSensorList.size(); i++) {
            RouteSensor s = _includedSensorList.get(i);
            g.addOutputSensor(s.getDisplayName(), s.getState());
        }
        return _includedSensorList.size();
    }

    /**
     * Sets the Sensor, Turnout, and delay control information for adding or
     * editting if any
     */
    void setControlInformation(Route g) {
        // Get sensor control information if any
        if (sensor1.getSelectedBean() != null) {
            if ((!g.addSensorToRoute(sensor1.getSelectedDisplayName(), sensorModeFromBox(sensor1mode)))) {
                log.error("Unexpected failure to add Sensor '" + sensor1.getSelectedDisplayName()
                        + "' to Route '" + g.getSystemName() + "'.");
            }
        }

        if (sensor2.getSelectedBean() != null) {
            if ((!g.addSensorToRoute(sensor2.getSelectedDisplayName(), sensorModeFromBox(sensor2mode)))) {
                log.error("Unexpected failure to add Sensor '" + sensor2.getSelectedDisplayName()
                        + "' to Route '" + g.getSystemName() + "'.");
            }
        }

        if (sensor3.getSelectedBean() != null) {
            if ((!g.addSensorToRoute(sensor3.getSelectedDisplayName(), sensorModeFromBox(sensor3mode)))) {
                log.error("Unexpected failure to add Sensor '" + sensor3.getSelectedDisplayName()
                        + "' to Route '" + g.getSystemName() + "'.");
            }
        }

        //turnouts aligned sensor
        if (turnoutsAlignedSensor.getSelectedBean() != null) {
            g.setTurnoutsAlignedSensor(turnoutsAlignedSensor.getSelectedDisplayName());
        } else {
            g.setTurnoutsAlignedSensor("");
        }

        // Set turnout information if there is any
        if (cTurnout.getSelectedBean() != null) {
            g.setControlTurnout(cTurnout.getSelectedDisplayName());
            // set up control turnout state
            g.setControlTurnoutState(turnoutModeFromBox(cTurnoutStateBox));
        } else {
            // No control Turnout was entered
            g.setControlTurnout("");
        }
        // set delay information
        int addDelay = 0;
        try {
            addDelay = Integer.parseInt(timeDelay.getText());
        } catch (NumberFormatException e) {
            addDelay = 0;
            timeDelay.setText("0");
        }
        if (addDelay < 0) {
            // added delay must be a positive integer
            addDelay = 0;
            timeDelay.setText("0");
        }
        g.setRouteCommandDelay(addDelay);

        // Set lock turnout information if there is any
        if (cLockTurnout.getSelectedBean() != null) {
            g.setLockControlTurnout(cLockTurnout.getSelectedDisplayName());
            // set up control turnout state
            g.setLockControlTurnoutState(turnoutModeFromBox(cLockTurnoutStateBox));
        } else {
            // No control Turnout was entered
            g.setLockControlTurnout("");
        }
    }

    JFileChooser soundChooser = null;

    /**
     * Set the sound file
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
                log.error("exception setting sound file: " + e);
            }
        }
    }

    JFileChooser scriptChooser = null;

    /**
     * Set the script file
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
                log.error("exception setting script file: " + e);
            }
        }
    }

    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // identify the Route with this name if it already exists
        String sName = _systemName.getText();
        Route g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g == null) {
            sName = _userName.getText();
            g = jmri.InstanceManager.routeManagerInstance().getByUserName(sName);
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
        // get sound, script names
        scriptFile.setText(g.getOutputScriptName());
        soundFile.setText(g.getOutputSoundName());

        // get turnout aligned sensor
        turnoutsAlignedSensor.setSelectedBean(g.getTurnoutsAlgdSensor());

        // set up Sensors if there are any
        Sensor[] temNames = new Sensor[Route.MAX_CONTROL_SENSORS];
        int[] temModes = new int[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k < Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensor(k);
            temModes[k] = g.getRouteSensorMode(k);
        }
        sensor1.setSelectedBean(temNames[0]);
        setSensorModeBox(temModes[0], sensor1mode);

        sensor2.setSelectedBean(temNames[1]);
        setSensorModeBox(temModes[1], sensor2mode);

        sensor3.setSelectedBean(temNames[2]);
        setSensorModeBox(temModes[2], sensor3mode);

        // set up control Turnout if there is one
        cTurnout.setSelectedBean(g.getCtlTurnout());

        setTurnoutModeBox(g.getControlTurnoutState(), cTurnoutStateBox);

        // set up lock control Turnout if there is one
        cLockTurnout.setSelectedBean(g.getLockCtlTurnout());

        setTurnoutModeBox(g.getLockControlTurnoutState(), cLockTurnoutStateBox);

        // set up additional delay
        timeDelay.setText(Integer.toString(g.getRouteCommandDelay()));
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
        editMode = true;
    }   // editPressed

    /**
     * Responds to the Delete button
     */
    void deletePressed(ActionEvent e) {
        // route is already deactivated, just delete it
        InstanceManager.routeManagerInstance().deleteRoute(curRoute);

        curRoute = null;
        finishUpdate();
    }

    /**
     * Responds to the Update button - update to Route Table
     */
    void updatePressed(ActionEvent e, boolean newRoute) {
        // Check if the User Name has been changed
        String uName = _userName.getText();
        //String sName = _systemName.getText();
        Route g = checkNamesOK();
        if (g == null) {
            return;
        }
        // user name is unique, change it
        g.setUserName(uName);
        // clear the current output information for this Route
        g.clearOutputTurnouts();
        g.clearOutputSensors();
        // clear the current Sensor information for this Route
        g.clearRouteSensors();
        // add those indicated in the window
        initializeIncludedList();
        setTurnoutInformation(g);
        setSensorInformation(g);
        // set the current values of the filenames
        g.setOutputScriptName(scriptFile.getText());
        g.setOutputSoundName(soundFile.getText());
        // add control Sensors and a control Turnout if entered in the window
        setControlInformation(g);
        curRoute = g;
        finishUpdate();
        status1.setText((newRoute ? Bundle.getMessage("RouteAddStatusCreated") : Bundle.getMessage("RouteAddStatusUpdated")) + ": \""
                + uName + "\" (" + _includedTurnoutList.size() + " "
                + Bundle.getMessage("Turnouts") + ", " + _includedSensorList.size() + " " + Bundle.getMessage("Sensors") + ")");
    }

    void finishUpdate() {
        // move to show all turnouts if not there
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
        clearPage();
        _systemName.setVisible(true);
        // reactivate the Route
        routeDirty = true;
        // get out of edit mode
        editMode = false;
        if (curRoute != null) {
            curRoute.activateRoute();
        }
    }

    void clearPage() {
        _systemName.setVisible(true);
        _systemName.setText("");
        _userName.setText("");
        sensor1.setSelectedBean(null);
        sensor2.setSelectedBean(null);
        sensor3.setSelectedBean(null);
        cTurnout.setSelectedBean(null);
        cLockTurnout.setSelectedBean(null);
        turnoutsAlignedSensor.setSelectedBean(null);
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
     * Responds to the Export button - export to Logix
     */
    void exportPressed(ActionEvent e) {
        curRoute = checkNamesOK();
        String sName = _systemName.getText();
        if (sName.length() == 0) {
            sName = fixedSystemName.getText();
        }
        String uName = _userName.getText();
        String logixSystemName = LOGIX_SYS_NAME + sName;
        Logix logix = InstanceManager.logixManagerInstance().getBySystemName(logixSystemName);
        if (logix == null) {
            logix = InstanceManager.logixManagerInstance().createNewLogix(logixSystemName, uName);
            if (logix == null) {
                log.error("Failed to create Logix " + logixSystemName + ", " + uName);
                return;
            }
        }
        logix.deActivateLogix();
        initializeIncludedList();

        /////////////////// Construct output actions for change to true //////////////////////
        ArrayList<ConditionalAction> actionList = new ArrayList<ConditionalAction>();

        for (int i = 0; i < _includedSensorList.size(); i++) {
            RouteSensor rSensor = _includedSensorList.get(i);
            String name = rSensor.getUserName();
            if (name == null || name.length() == 0) {
                name = rSensor.getSysName();
            }
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.ACTION_SET_SENSOR, name, rSensor.getState(), ""));
        }
        for (int i = 0; i < _includedTurnoutList.size(); i++) {
            RouteTurnout rTurnout = _includedTurnoutList.get(i);
            String name = rTurnout.getUserName();
            if (name == null || name.length() == 0) {
                name = rTurnout.getSysName();
            }
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.ACTION_SET_TURNOUT, name, rTurnout.getState(), ""));
        }
        String file = soundFile.getText();
        if (file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.ACTION_RUN_SCRIPT, "", -1, file));
        }
        file = scriptFile.getText();
        if (file.length() > 0) {
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.ACTION_PLAY_SOUND, "", -1, file));
        }

        ///// Construct 'AND' clause from 'VETO' controls ////////
        ArrayList<ConditionalVariable> vetoList = new ArrayList<ConditionalVariable>();

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

        String cUserName = null;

        ///////////////// Make Trigger Conditionals //////////////////////
        //ArrayList <ConditionalVariable> onChangeList = new ArrayList<ConditionalVariable>();
        int numConds = 1; // passed through all these, with new value returned each time
        numConds = makeSensorConditional(sensor1, sensor1mode, numConds, false,
                actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor2, sensor2mode, numConds, false,
                actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor3, sensor3mode, numConds, false,
                actionList, vetoList, logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, false,
                actionList, vetoList, logix, logixSystemName, uName);

        ////// Construct actions for false from the 'any change' controls ////////////
        numConds = makeSensorConditional(sensor1, sensor1mode, numConds, true, actionList, vetoList,
                logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor2, sensor2mode, numConds, true, actionList, vetoList,
                logix, logixSystemName, uName);
        numConds = makeSensorConditional(sensor3, sensor3mode, numConds, true, actionList, vetoList,
                logix, logixSystemName, uName);
        numConds = makeTurnoutConditional(cTurnout, cTurnoutStateBox, numConds, true, actionList,
                vetoList, logix, logixSystemName, uName);
        log.debug("Final number of conditionals: {}", numConds);

        ///////////////// Set up Alignment Sensor, if there is one //////////////////////////
        //String sensorSystemName = turnoutsAlignedSensor.getText();
        if (turnoutsAlignedSensor.getSelectedBean() != null) {
            // verify name (logix doesn't use "provideXXX") 
            //Sensor s = turnoutsAlignedSensor.getSelectedBean();
            /*if (s == null) {
             s = InstanceManager.sensorManagerInstance().getBySystemName(sensorSystemName);
             }*/
            //if (s != null) {
            String sensorSystemName = turnoutsAlignedSensor.getSelectedDisplayName();
            cSystemName = logixSystemName + "1A"; // NOI18N
            cUserName = turnoutsAlignedSensor.getSelectedDisplayName() + "A " + uName; // NOI18N

            ArrayList<ConditionalVariable> variableList = new ArrayList<ConditionalVariable>();
            for (int i = 0; i < _includedTurnoutList.size(); i++) {
                RouteTurnout rTurnout = _includedTurnoutList.get(i);
                String name = rTurnout.getUserName();
                if (name == null || name.length() == 0) {
                    name = rTurnout.getSysName();
                }
                // exclude toggled outputs
                switch (rTurnout.getState()) {
                    case Turnout.CLOSED:
                        variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND,
                                Conditional.TYPE_TURNOUT_CLOSED, name, true));
                        break;
                    case Turnout.THROWN:
                        variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND,
                                Conditional.TYPE_TURNOUT_THROWN, name, true));
                        break;
                    default:
                        log.warn("Turnout {} was {}, neither CLOSED nor THROWN; not handled", name, rTurnout.getState()); // NOI18N
                }
            }
            actionList = new ArrayList<ConditionalAction>();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.ACTION_SET_SENSOR, sensorSystemName, Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                    Conditional.ACTION_SET_SENSOR, sensorSystemName, Sensor.INACTIVE, ""));

            Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            //}
        }

        ///////////////// Set lock turnout information if there is any //////////////////////////
        if (cLockTurnout.getSelectedBean() != null) {
            String turnoutLockSystemName = cLockTurnout.getSelectedDisplayName();
            // verify name (logix doesn't use "provideXXX") 
            cSystemName = logixSystemName + "1L"; // NOI18N
            cUserName = turnoutLockSystemName + "L " + uName; // NOI18N
            ArrayList<ConditionalVariable> variableList = new ArrayList<ConditionalVariable>();
            //String devName = cTurnout.getText();
            int mode = turnoutModeFromBox(cTurnoutStateBox);
            int type = Conditional.TYPE_TURNOUT_CLOSED;
            if (mode == Route.ONTHROWN) {
                type = Conditional.TYPE_TURNOUT_THROWN;
            }
            variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_NONE,
                    type, turnoutLockSystemName, true));

            actionList = new ArrayList<ConditionalAction>();
            int option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            type = Turnout.LOCKED;
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
                actionList.add(new DefaultConditionalAction(option, Conditional.ACTION_LOCK_TURNOUT,
                        name, type, ""));
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
                    actionList.add(new DefaultConditionalAction(option, Conditional.ACTION_LOCK_TURNOUT,
                            name, type, ""));
                }
            }

            // add new Conditionals for action on 'locks'
            Conditional c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
            c.setStateVariables(variableList);
            c.setLogicType(Conditional.ALL_AND, "");
            c.setAction(actionList);
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
        }
        logix.activateLogix();
        if (curRoute != null) {
            jmri.InstanceManager.routeManagerInstance().deleteRoute(curRoute);
            curRoute = null;
        }
        status1.setText(Bundle.getMessage("BeanNameRoute") + "\"" + uName + "\" " + Bundle.getMessage("RouteAddStatusExported") + " (" + _includedTurnoutList.size()
                + Bundle.getMessage("Turnouts") + ", " + _includedSensorList.size() + " " + Bundle.getMessage("Sensors") + ")");
        finishUpdate();
    }

    boolean removeConditionals(String cSystemName, Logix logix) {
        Conditional c = InstanceManager.conditionalManagerInstance().getBySystemName(cSystemName);
        if (c != null) {
            logix.deleteConditional(cSystemName);
            InstanceManager.conditionalManagerInstance().deleteConditional(c);
            return true;
        }
        return false;
    }

    /**
     * @throw IllegalArgumentException if "user input no good"
     * @return The number of conditionals after the creation.
     */
    int makeSensorConditional(JmriBeanComboBox jmriBox, JComboBox<String> sensorbox, int numConds,
            boolean onChange, ArrayList<ConditionalAction> actionList,
            ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlSensorVar(jmriBox, sensorbox, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<ConditionalVariable>();
            varList.add(cVar);
            for (int i = 0; i < vetoList.size(); i++) {
                varList.add(cloneVariable(vetoList.get(i)));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = jmriBox.getSelectedDisplayName() + numConds + "C " + uName;
            Conditional c = null;
            try {
                c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(cSystemName);
                // throw without creating any 
                throw new IllegalArgumentException("user input no good");
            }
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    /**
     * @throw IllegalArgumentException if "user input no good"
     * @return The number of conditionals after the creation.
     */
    int makeTurnoutConditional(JmriBeanComboBox jmriBox, JComboBox<String> box, int numConds,
            boolean onChange, ArrayList<ConditionalAction> actionList,
            ArrayList<ConditionalVariable> vetoList, Logix logix, String prefix, String uName) {
        ConditionalVariable cVar = makeCtrlTurnoutVar(jmriBox, box, false, onChange);
        if (cVar != null) {
            ArrayList<ConditionalVariable> varList = new ArrayList<ConditionalVariable>();
            varList.add(cVar);
            for (int i = 0; i < vetoList.size(); i++) {
                varList.add(cloneVariable(vetoList.get(i)));
            }
            String cSystemName = prefix + numConds + "T";
            String cUserName = jmriBox.getSelectedDisplayName() + numConds + "C " + uName;
            Conditional c = null;
            try {
                c = InstanceManager.conditionalManagerInstance().createNewConditional(cSystemName, cUserName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(cSystemName);
                // throw without creating any 
                throw new IllegalArgumentException("user input no good");
            }
            c.setStateVariables(varList);
            int option = onChange ? Conditional.ACTION_OPTION_ON_CHANGE : Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
            c.setAction(cloneActionList(actionList, option));
            c.setLogicType(Conditional.ALL_AND, "");
            logix.addConditional(cSystemName, 0);
            c.calculate(true, null);
            numConds++;
        }
        return numConds;
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorLightAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    ConditionalVariable cloneVariable(ConditionalVariable v) {
        return new ConditionalVariable(v.isNegated(), v.getOpern(), v.getType(), v.getName(), v.doTriggerActions());
    }

    ArrayList<ConditionalAction> cloneActionList(ArrayList<ConditionalAction> actionList, int option) {
        ArrayList<ConditionalAction> list = new ArrayList<ConditionalAction>();
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

    ConditionalVariable makeCtrlSensorVar(JmriBeanComboBox jmriBox, JComboBox<String> sensorbox,
            boolean makeVeto, boolean onChange) {
        String devName = jmriBox.getSelectedDisplayName();
        if (jmriBox.getSelectedBean() == null /*|| devName.length() == 0*/) {
            return null;
        }
        int oper = Conditional.OPERATOR_AND;
        int mode = sensorModeFromBox(sensorbox);
        boolean trigger = true;
        boolean negated = false;
        int type = 0;
        switch (mode) {
            case Route.ONACTIVE:    // route fires if sensor goes active
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_SENSOR_ACTIVE;
                break;
            case Route.ONINACTIVE:  // route fires if sensor goes inactive
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_SENSOR_INACTIVE;
                break;
            case Route.ONCHANGE:  // route fires if sensor goes active or inactive 
                if (makeVeto || !onChange) {
                    return null;
                }
                type = Conditional.TYPE_SENSOR_ACTIVE;
                break;
            case Route.VETOACTIVE:  // sensor must be active for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_SENSOR_ACTIVE;
                negated = true;
                trigger = false;
                break;
            case Route.VETOINACTIVE:
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_SENSOR_INACTIVE;
                negated = true;
                trigger = false;
                break;
            default:
                log.error("Control Sensor " + devName + " has bad mode= " + mode);
                return null;
        }
        return new ConditionalVariable(negated, oper, type, devName, trigger);
    }

    ConditionalVariable makeCtrlTurnoutVar(JmriBeanComboBox jmriBox, JComboBox<String> box,
            boolean makeVeto, boolean onChange) {

        if (jmriBox.getSelectedBean() == null) {
            return null;
        }
        String devName = jmriBox.getSelectedDisplayName();
        int mode = turnoutModeFromBox(box);
        int oper = Conditional.OPERATOR_AND;
        int type = 0;
        boolean negated = false;
        boolean trigger = true;
        switch (mode) {
            case Route.ONCLOSED:    // route fires if turnout goes closed
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_TURNOUT_CLOSED;
                break;
            case Route.ONTHROWN:  // route fires if turnout goes thrown
                if (makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_TURNOUT_THROWN;
                break;
            case Route.ONCHANGE:    // route fires if turnout goes active or inactive
                if (makeVeto || !onChange) {
                    return null;
                }
                type = Conditional.TYPE_TURNOUT_CLOSED;
                break;
            case Route.VETOCLOSED:  // turnout must be closed for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_TURNOUT_CLOSED;
                trigger = false;
                negated = true;
                break;
            case Route.VETOTHROWN:  // turnout must be thrown for route to fire
                if (!makeVeto || onChange) {
                    return null;
                }
                type = Conditional.TYPE_TURNOUT_THROWN;
                trigger = false;
                negated = true;
                break;
            default:
                log.error("Control Turnout " + devName + " has bad mode= " + mode);
                return null;
        }
        return new ConditionalVariable(negated, oper, type, devName, trigger);
    }

    /**
     * Responds to the CancelAdd button
     */
    void cancelAddPressed(ActionEvent e) {
        cancelAdd();
    }

    /**
     * Cancels Add mode
     */
    void cancelAdd() {
            curRoute = null;
            finishUpdate();
            Route g = null;
            status1.setText(createInst);
            status2.setText(editInst);
            routeDirty = false;
            _autoSystemName.setSelected(false); // prevent automatic creation next time
            // hide addFrame
            if (addFrame != null) {
            addFrame.setVisible(false);
            addFrame.dispose();
            addFrame = null;
            }
            _routeSensorModel.dispose();
            _routeTurnoutModel.dispose();
    }

    /**
     * Responds to the CancelEdit button
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
     * Cancels included Turnouts only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Base table model for selecting outputs
     */
    public abstract class RouteOutputModel extends AbstractTableModel implements PropertyChangeListener {

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        public void dispose() {
            InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

        public int getColumnCount() {
            return 4;
        }

        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
    }

    /**
     * Table model for selecting Turnouts and Turnout State
     */
    class RouteTurnoutModel extends RouteOutputModel {

        RouteTurnoutModel() {
            InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount() {
            if (showAll) {
                return _turnoutList.size();
            } else {
                return _includedTurnoutList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<RouteTurnout> turnoutList = null;
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

        public void setValueAt(Object type, int r, int c) {
            ArrayList<RouteTurnout> turnoutList = null;
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
                    log.error("RouteTurnoutModel.setValueAt should not be called on column {}", c);
            }
        }
    }

    /**
     * Set up table for selecting Sensors and Sensor State
     */
    class RouteSensorModel extends RouteOutputModel {

        RouteSensorModel() {
            InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount() {
            if (showAll) {
                return _sensorList.size();
            } else {
                return _includedSensorList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<RouteSensor> sensorList = null;
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

        public void setValueAt(Object type, int r, int c) {
            ArrayList<RouteSensor> sensorList = null;
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
                    log.error("RouteSensorModel.setValueAt should not be called on column {}", c);
            }
        }
    }

    private boolean showAll = true;   // false indicates show only included Turnouts

    public final static String LOGIX_SYS_NAME = "RTX";
    public final static String CONDITIONAL_SYS_PREFIX = LOGIX_SYS_NAME + "C";
    private static int ROW_HEIGHT;

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
            "Veto" + " " + Bundle.getMessage("SensorStateActive"),
            "Veto" + " " + Bundle.getMessage("SensorStateInactive")
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
            "Veto" + " " + Bundle.getMessage("TurnoutStateClosed"),
            "Veto" + " " + Bundle.getMessage("TurnoutStateThrown")
    };
    private static String[] lockTurnoutInputModes = new String[]{
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("TurnoutStateClosed"),
            Bundle.getMessage("OnCondition") + " " + Bundle.getMessage("TurnoutStateThrown"),
            Bundle.getMessage("OnConditionChange")
    };

    private static int[] turnoutInputModeValues = new int[]{Route.ONCLOSED, Route.ONTHROWN, Route.ONCHANGE,
        Route.VETOCLOSED, Route.VETOTHROWN};

    private ArrayList<RouteTurnout> _turnoutList;      // array of all Turnouts
    private ArrayList<RouteTurnout> _includedTurnoutList;

    private ArrayList<RouteSensor> _sensorList;        // array of all Sensorsy
    private ArrayList<RouteSensor> _includedSensorList;

    private abstract class RouteElement {

        String _sysName;
        String _userName;
        boolean _included;
        int _setToState;

        RouteElement(String sysName, String userName) {
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

        String getDisplayName() {
            String name = getUserName();
            if (name != null && name.length() > 0) {
                return name;
            } else {
                return getSysName();
            }

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

    private class RouteSensor extends RouteElement {

        RouteSensor(String sysName, String userName) {
            super(sysName, userName);
        }

        String getSetToState() {
            switch (_setToState) {
                case Sensor.INACTIVE:
                    return SET_TO_INACTIVE;
                case Sensor.ACTIVE:
                    return SET_TO_ACTIVE;
                case Route.TOGGLE:
                    return SET_TO_TOGGLE;
            }
            return "";
        }

        void setSetToState(String state) {
            if (SET_TO_INACTIVE.equals(state)) {
                _setToState = Sensor.INACTIVE;
            } else if (SET_TO_ACTIVE.equals(state)) {
                _setToState = Sensor.ACTIVE;
            } else if (SET_TO_TOGGLE.equals(state)) {
                _setToState = Route.TOGGLE;
            }
        }
    }

    private class RouteTurnout extends RouteElement {

        RouteTurnout(String sysName, String userName) {
            super(sysName, userName);
        }

        String getSetToState() {
            switch (_setToState) {
                case Turnout.CLOSED:
                    return SET_TO_CLOSED;
                case Turnout.THROWN:
                    return SET_TO_THROWN;
                case Route.TOGGLE:
                    return SET_TO_TOGGLE;
            }
            return "";
        }

        void setSetToState(String state) {
            if (SET_TO_CLOSED.equals(state)) {
                _setToState = Turnout.CLOSED;
            } else if (SET_TO_THROWN.equals(state)) {
                _setToState = Turnout.THROWN;
            } else if (SET_TO_TOGGLE.equals(state)) {
                _setToState = Route.TOGGLE;
            }
        }
    }

    public void setMessagePreferencesDetails() {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "remindSaveRoute", Bundle.getMessage("HideSaveReminder"));
        super.setMessagePreferencesDetails();
    }

    protected String getClassName() {
        return RouteTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleRouteTable");
    }

    private final static Logger log = LoggerFactory.getLogger(RouteTableAction.class.getName());
}
