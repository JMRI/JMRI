package jmri.jmrit.sensorgroup;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.implementation.DefaultConditionalAction;
import jmri.implementation.SensorGroupConditional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for creating and editing sensor groups.
 * <P>
 * Sensor groups are implemented by (groups) of Routes, not by any other object.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009
 */
public class SensorGroupFrame extends jmri.util.JmriJFrame {

    public SensorGroupFrame() {
        super();
    }

    private final static String namePrefix = "SENSOR GROUP:";  // should be upper case
    private final static String nameDivider = ":";
    public final static String logixSysName = "SYS";
    public final static String logixUserName = "System Logix";
    public final static String ConditionalSystemPrefix = logixSysName + "_SGC_";
    private final static String ConditionalUserPrefix = "Sensor Group ";
    private int rowHeight;

    SensorTableModel _sensorModel;
    JScrollPane _sensorScrollPane;
    JTextField _nameField;
    JList<String> _sensorGroupList;

    @Override
    public void initComponents() {
        addHelpMenu("package.jmri.jmrit.sensorgroup.SensorGroupFrame", true);

        setTitle("Define Sensor Group");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add the sensor table
        JPanel p2xs = new JPanel();

        JPanel p21s = new JPanel();
        p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
        p21s.add(new JLabel("Please select"));
        p21s.add(new JLabel("Sensors to "));
        p21s.add(new JLabel("be included "));
        p21s.add(new JLabel("in this group."));
        p2xs.add(p21s);
        _sensorModel = new SensorTableModel();
        JTable sensorTable = new JTable(_sensorModel);

        sensorTable.setRowSelectionAllowed(false);
        sensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(450, 200));
        TableColumnModel sensorColumnModel = sensorTable.getColumnModel();
        TableColumn includeColumnS = sensorColumnModel.
                getColumn(SensorTableModel.INCLUDE_COLUMN);
        includeColumnS.setResizable(false);
        includeColumnS.setMinWidth(50);
        includeColumnS.setMaxWidth(60);
        TableColumn sNameColumnS = sensorColumnModel.
                getColumn(SensorTableModel.SNAME_COLUMN);
        sNameColumnS.setResizable(true);
        sNameColumnS.setMinWidth(75);
        sNameColumnS.setPreferredWidth(95);
        TableColumn uNameColumnS = sensorColumnModel.
                getColumn(SensorTableModel.UNAME_COLUMN);
        uNameColumnS.setResizable(true);
        uNameColumnS.setMinWidth(210);
        uNameColumnS.setPreferredWidth(260);

        rowHeight = sensorTable.getRowHeight();
        _sensorScrollPane = new JScrollPane(sensorTable);
        p2xs.add(_sensorScrollPane, BorderLayout.CENTER);
        getContentPane().add(p2xs);
        p2xs.setVisible(true);

        // add name field
        JPanel p3 = new JPanel();
        p3.add(new JLabel("Group Name:"));
        _nameField = new JTextField(20);
        p3.add(_nameField);
        getContentPane().add(p3);

        // button
        JPanel p4 = new JPanel();
        JButton viewButton = new JButton(" View ");
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewPressed();
            }
        });
        p4.add(viewButton);
        JButton addButton = new JButton("Make Group");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPressed();
            }
        });
        p4.add(addButton);
        JButton undoButton = new JButton("Undo Group");
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoGroupPressed();
            }
        });
        p4.add(undoButton);
        getContentPane().add(p4);

        JPanel p5 = new JPanel();

        DefaultListModel<String> groupModel = new DefaultListModel<String>();
        // Look for Sensor group in Route table
        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);
        List<String> routeList = rm.getSystemNameList();
        int i = 0;
        while (i < routeList.size()) {
            String name = routeList.get(i);
            if (name.startsWith(namePrefix)) {
                name = name.substring(namePrefix.length());
                String group = name.substring(0, name.indexOf(nameDivider));
                String prefix = namePrefix + group + nameDivider;
                do {
                    i++;
                    if (i >= routeList.size()) {
                        break;
                    }
                    name = routeList.get(i);
                } while (name.startsWith(prefix));
                groupModel.addElement(group);
            }
            i++;
        }
        // Look for Sensor group in Logix
        Logix logix = getSystemLogix();
        for (i = 0; i < logix.getNumConditionals(); i++) {
            String name = logix.getConditionalByNumberOrder(i);
            Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(name);
            String uname = c.getUserName();
            if (uname != null) {
                groupModel.addElement(uname.substring(ConditionalUserPrefix.length()));
            }
        }
        _sensorGroupList = new JList<String>(groupModel);
        _sensorGroupList.setPrototypeCellValue(ConditionalUserPrefix + "XXXXXXXXXX");
        _sensorGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _sensorGroupList.setVisibleRowCount(5);
        JScrollPane scrollPane = new JScrollPane(_sensorGroupList);
        p5.add(scrollPane);
        p5.add(Box.createHorizontalStrut(10));
        JButton doneButton = new JButton(" Done ");
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                donePressed(e);
            }
        });
        p5.add(doneButton);
        getContentPane().add(p5);

        // pack to cause display
        pack();
    }

    void addPressed() {
        deleteGroup(false);
        String group = _nameField.getText();
        if (group == null || group.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please enter a name for this group.", "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        Logix logix = getSystemLogix();
        logix.deActivateLogix();
        String cSystemName = ConditionalSystemPrefix + group.toUpperCase();
        String cUserName = ConditionalUserPrefix + group;
        // add new Conditional
        ArrayList<ConditionalVariable> variableList = new ArrayList<>();
        ArrayList<ConditionalAction> actionList = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < _sensorModel.getRowCount(); i++) {
            if ((Boolean) _sensorModel.getValueAt(i, BeanTableModel.INCLUDE_COLUMN)) {
                String sensor = (String) _sensorModel.getValueAt(i, BeanTableModel.UNAME_COLUMN);
                if (sensor == null || sensor.length() == 0) {
                    sensor = (String) _sensorModel.getValueAt(i, BeanTableModel.SNAME_COLUMN);
                }
                variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_OR,
                        Conditional.TYPE_SENSOR_ACTIVE, sensor, true));
                actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                        Conditional.ACTION_SET_SENSOR, sensor,
                        Sensor.INACTIVE, ""));
                count++;
            }
        }
        if (count < 2) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "A Sensor Group needs to have at least 2 sensors to be useful.",
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        Conditional c = new SensorGroupConditional(cSystemName, cUserName);
        c.setStateVariables(variableList);
        c.setLogicType(Conditional.ALL_OR, "");
        c.setAction(actionList);
        logix.addConditional(cSystemName, 0);       // Update the Logix Conditional names list
        logix.addConditional(cSystemName, c);       // Update the Logix Conditional hash map
        logix.setEnabled(true);
        logix.activateLogix();
        ((DefaultListModel<String>) _sensorGroupList.getModel()).addElement(
                cUserName.substring(ConditionalUserPrefix.length()));
        clear();
    }

    void viewPressed() {
        for (int i = 0; i < _sensorModel.getRowCount(); i++) {
            _sensorModel.setValueAt(Boolean.FALSE, i, BeanTableModel.INCLUDE_COLUMN);
        }
        // look for name in List panel
        String group = _sensorGroupList.getSelectedValue();
        if (group == null) { // not there, look in text field
            group = _nameField.getText().toUpperCase().trim();
        }
        _nameField.setText(group);
        // Look for Sensor group in Route table
        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);
        List<String> l = rm.getSystemNameList();
        String prefix = (namePrefix + group + nameDivider).toUpperCase();
        boolean isRoute = false;
        int setRow = 0;
        for (int i = 0; i < l.size(); i++) {
            String name = l.get(i);
            if (name.startsWith(prefix)) {
                isRoute = true;
                String sensor = name.substring(prefix.length());
                // find and check that sensor
                for (int j = _sensorModel.getRowCount() - 1; j >= 0; j--) {
                    if (_sensorModel.getValueAt(j, BeanTableModel.SNAME_COLUMN).equals(sensor)) {
                        _sensorModel.setValueAt(Boolean.TRUE, j, BeanTableModel.INCLUDE_COLUMN);
                        setRow = j;
                    }
                }
            }
        }

        // look for  Sensor group in SYSTEM Logix
        if (!isRoute) {
            Logix logix = getSystemLogix();
            String cSystemName = (ConditionalSystemPrefix + group).toUpperCase();
            String cUserName = ConditionalUserPrefix + group;
            for (int i = 0; i < logix.getNumConditionals(); i++) {
                String name = logix.getConditionalByNumberOrder(i);
                if (cSystemName.equals(name) || cUserName.equals(name)) {
                    Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(name);
                    if (c == null) {
                        log.error("Conditional \"" + name + "\" expected but NOT found in Logix " + logix.getSystemName());
                    } else {
                        ArrayList<ConditionalVariable> variableList = c.getCopyOfStateVariables();
                        for (int k = 0; k < variableList.size(); k++) {
                            String sensor = variableList.get(k).getName();
                            if (sensor != null) {
                                for (int j = _sensorModel.getRowCount() - 1; j >= 0; j--) {
                                    if (sensor.equals(_sensorModel.getValueAt(j, BeanTableModel.UNAME_COLUMN))
                                            || sensor.equals(_sensorModel.getValueAt(j, BeanTableModel.SNAME_COLUMN))) {
                                        _sensorModel.setValueAt(Boolean.TRUE, j, BeanTableModel.INCLUDE_COLUMN);
                                        setRow = j;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        _sensorModel.fireTableDataChanged();
        setRow -= 9;
        if (setRow < 0) {
            setRow = 0;
        }
        _sensorScrollPane.getVerticalScrollBar().setValue(setRow * rowHeight);
    }

    Logix getSystemLogix() {
        Logix logix = InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(logixSysName);
        if (logix == null) {
            logix = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix(logixSysName, logixUserName);
        }
        return logix;
    }

    void clear() {
        _sensorGroupList.getSelectionModel().clearSelection();
        _nameField.setText("");
        for (int i = 0; i < _sensorModel.getRowCount(); i++) {
            _sensorModel.setValueAt(Boolean.FALSE, i, BeanTableModel.INCLUDE_COLUMN);
        }
        _sensorModel.fireTableDataChanged();
    }

    void donePressed(ActionEvent e) {
        _sensorModel.dispose();
        dispose();
    }

    void deleteGroup(boolean showMsg) {
        String group = _nameField.getText();

        if (group == null || group.equals("")) {
            if (showMsg) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "'View' the group or enter the group name in the 'Group Name' field before selecting 'Undo Group'",
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        String prefix = (namePrefix + group + nameDivider).toUpperCase();

        // remove the old routes
        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);
        List<String> l = rm.getSystemNameList();

        for (int i = 0; i < l.size(); i++) {
            String name = l.get(i);
            if (name.startsWith(prefix)) {
                // OK, kill this one
                Route r = rm.getBySystemName(l.get(i));
                r.deActivateRoute();
                rm.deleteRoute(r);
            }
        }
        String cSystemName = (ConditionalSystemPrefix + group).toUpperCase();
        String cUserName = ConditionalUserPrefix + group;
        Logix logix = getSystemLogix();
        for (int i = 0; i < logix.getNumConditionals(); i++) {
            String name = logix.getConditionalByNumberOrder(i);
            if (cSystemName.equals(name) || cUserName.equals(name)) {
                Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(name);
                if (c == null) {
                    log.error("Conditional \"" + name + "\" expected but NOT found in Logix " + logix.getSystemName());
                } else {
                    logix.deleteConditional(cSystemName);
                    break;
                }
            }
        }
        DefaultListModel<String> model = (DefaultListModel<String>) _sensorGroupList.getModel();
        int index = model.indexOf(group);
        if (index > -1) {
            model.remove(index);
        }

        index = _sensorGroupList.getSelectedIndex();
        if (index > -1) {
            String sysName = ConditionalSystemPrefix + model.elementAt(index);
            String[] msgs = logix.deleteConditional(sysName);
            if (msgs != null) {
                if (showMsg) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Conditional " + msgs[0] + " ("
                            + msgs[1] + ") is a Conditional Variable in the Conditional,\n"
                            + msgs[2] + " (" + msgs[3] + "), of Logix, " + msgs[4] + " (" + msgs[5]
                            + ").\nPlease remove that variable first.",
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else {
                model.remove(index);
            }
        }
    }

    void undoGroupPressed() {
        deleteGroup(true);
        clear();
    }

    private final static Logger log = LoggerFactory.getLogger(SensorGroupFrame.class);
}
