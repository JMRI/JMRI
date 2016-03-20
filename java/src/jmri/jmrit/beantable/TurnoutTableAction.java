// TurnoutTableAction.java
package jmri.jmrit.beantable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;
import jmri.jmrit.turnoutoperations.TurnoutOperationFrame;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.JmriBeanComboBox;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a TurnoutTable GUI.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @version $Revision$
 */
public class TurnoutTableAction extends AbstractTableAction {

    /**
     *
     */
    private static final long serialVersionUID = -8221584673872246104L;

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName
     */
    public TurnoutTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary turnout manager available
        if (turnManager == null) {
            setEnabled(false);
        }

        //This following must contain the word Global for a correct match in the abstract turnout
        defaultThrownSpeedText = ("Use Global " + turnManager.getDefaultThrownSpeed());
        defaultClosedSpeedText = ("Use Global " + turnManager.getDefaultClosedSpeed());
        //This following must contain the word Block for a correct match in the abstract turnout
        useBlockSpeed = "Use Block Speed";

        speedListClosed.add(defaultClosedSpeedText);
        speedListThrown.add(defaultThrownSpeedText);
        speedListClosed.add(useBlockSpeed);
        speedListThrown.add(useBlockSpeed);
        java.util.Vector<String> _speedMap = jmri.implementation.SignalSpeedMap.getMap().getValidSpeedNames();
        for (int i = 0; i < _speedMap.size(); i++) {
            if (!speedListClosed.contains(_speedMap.get(i))) {
                speedListClosed.add(_speedMap.get(i));
            }
            if (!speedListThrown.contains(_speedMap.get(i))) {
                speedListThrown.add(_speedMap.get(i));
            }
        }
    }

    public TurnoutTableAction() {
        this(Bundle.getMessage("TitleTurnoutTable"));
    }

    String closedText;
    String thrownText;
    String defaultThrownSpeedText;
    String defaultClosedSpeedText;
    String useBlockSpeed = "Use Block Speed"; // I18N TODO
    String bothText = "Both";
    String cabOnlyText = "Cab only";
    String pushbutText = "Pushbutton only";
    String noneText = "None";
    String[] lockOperations = {bothText, cabOnlyText, pushbutText, noneText};
    private java.util.Vector<String> speedListClosed = new java.util.Vector<String>();
    private java.util.Vector<String> speedListThrown = new java.util.Vector<String>();
    protected TurnoutManager turnManager = InstanceManager.turnoutManagerInstance();
    protected JTable table;

    @Override
    public void setManager(Manager man) {
        turnManager = (TurnoutManager) man;
    }

    static public final int INVERTCOL = BeanTableDataModel.NUMCOLUMN;
    static public final int LOCKCOL = INVERTCOL + 1;
    static public final int EDITCOL = LOCKCOL + 1;
    static public final int KNOWNCOL = EDITCOL + 1;
    static public final int MODECOL = KNOWNCOL + 1;
    static public final int SENSOR1COL = MODECOL + 1;
    static public final int SENSOR2COL = SENSOR1COL + 1;
    static public final int OPSONOFFCOL = SENSOR2COL + 1;
    static public final int OPSEDITCOL = OPSONOFFCOL + 1;
    static public final int LOCKOPRCOL = OPSEDITCOL + 1;
    static public final int LOCKDECCOL = LOCKOPRCOL + 1;
    static public final int STRAIGHTCOL = LOCKDECCOL + 1;
    static public final int DIVERGCOL = STRAIGHTCOL + 1;

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Turnouts
     */
    protected void createModel() {
        // store the terminology
        closedText = turnManager.getClosedText();
        thrownText = turnManager.getThrownText();

        // create the data model object that drives the table;
        // note that this is a class creation, and very long
        m = new BeanTableDataModel() {

            /**
             *
             */
            private static final long serialVersionUID = -8822546005900067212L;

            @Override
            public int getColumnCount() {
                return DIVERGCOL + 1;
            }

            @Override
            public String getColumnName(int col) {
                if (col == INVERTCOL) {
                    return "Inverted";
                } else if (col == LOCKCOL) {
                    return "Locked";
                } else if (col == KNOWNCOL) {
                    return "Feedback";
                } else if (col == MODECOL) {
                    return "Mode";
                } else if (col == SENSOR1COL) {
                    return "Sensor 1";
                } else if (col == SENSOR2COL) {
                    return "Sensor 2";
                } else if (col == OPSONOFFCOL) {
                    return "Automate";
                } else if (col == OPSEDITCOL) {
                    return "";
                } else if (col == LOCKOPRCOL) {
                    return "Lock Mode";
                } else if (col == LOCKDECCOL) {
                    return "Decoder";
                } else if (col == DIVERGCOL) {
                    return "Thrown Speed";
                } else if (col == STRAIGHTCOL) {
                    return "Closed Speed";
                } else if (col == VALUECOL) {
                    return "Cmd";  // override default title
                } else if (col == EDITCOL) {
                    return "";
                } else {
                    return super.getColumnName(col);
                }
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == INVERTCOL) {
                    return Boolean.class;
                } else if (col == LOCKCOL) {
                    return Boolean.class;
                } else if (col == KNOWNCOL) {
                    return String.class;
                } else if (col == MODECOL) {
                    return JComboBox.class;
                } else if (col == SENSOR1COL) {
                    return JComboBox.class;
                } else if (col == SENSOR2COL) {
                    return JComboBox.class;
                } else if (col == OPSONOFFCOL) {
                    return JComboBox.class;
                } else if (col == OPSEDITCOL) {
                    return JButton.class;
                } else if (col == EDITCOL) {
                    return JButton.class;
                } else if (col == LOCKOPRCOL) {
                    return JComboBox.class;
                } else if (col == LOCKDECCOL) {
                    return JComboBox.class;
                } else if (col == DIVERGCOL) {
                    return JComboBox.class;
                } else if (col == STRAIGHTCOL) {
                    return JComboBox.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            @Override
            public int getPreferredWidth(int col) {
                switch (col) {
                    case INVERTCOL:
                        return new JTextField(6).getPreferredSize().width;
                    case LOCKCOL:
                        return new JTextField(6).getPreferredSize().width;
                    case LOCKOPRCOL:
                        return new JTextField(10).getPreferredSize().width;
                    case LOCKDECCOL:
                        return new JTextField(10).getPreferredSize().width;
                    case KNOWNCOL:
                        return new JTextField(10).getPreferredSize().width;
                    case MODECOL:
                        return new JTextField(10).getPreferredSize().width;
                    case SENSOR1COL:
                        return new JTextField(5).getPreferredSize().width;
                    case SENSOR2COL:
                        return new JTextField(5).getPreferredSize().width;
                    case OPSONOFFCOL:
                        return new JTextField(14).getPreferredSize().width;
                    case OPSEDITCOL:
                        return new JTextField(7).getPreferredSize().width;
                    case EDITCOL:
                        return new JTextField(7).getPreferredSize().width;
                    case DIVERGCOL:
                        return new JTextField(14).getPreferredSize().width;
                    case STRAIGHTCOL:
                        return new JTextField(14).getPreferredSize().width;
                    default:
                        super.getPreferredWidth(col);
                }
                return super.getPreferredWidth(col);
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                String name = sysNameList.get(row);
                TurnoutManager manager = turnManager;
                Turnout t = manager.getBySystemName(name);
                if (col == INVERTCOL) {
                    return t.canInvert();
                } else if (col == LOCKCOL) {
                    return t.canLock(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT);
                } else if (col == KNOWNCOL) {
                    return false;
                } else if (col == MODECOL) {
                    return true;
                } else if (col == SENSOR1COL) {
                    return true;
                } else if (col == SENSOR2COL) {
                    return true;
                } else if (col == OPSONOFFCOL) {
                    return true;
                } else if (col == OPSEDITCOL) {
                    return t.getTurnoutOperation() != null;
                } else if (col == LOCKOPRCOL) {
                    return true;
                } else if (col == LOCKDECCOL) {
                    return true;
                } else if (col == DIVERGCOL) {
                    return true;
                } else if (col == STRAIGHTCOL) {
                    return true;
                } else if (col == EDITCOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            @Override
            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= sysNameList.size()) {
                    log.debug("row is greater than name list");
                    return "error";
                }
                String name = sysNameList.get(row);
                TurnoutManager manager = turnManager;
                Turnout t = manager.getBySystemName(name);
                if (t == null) {
                    log.debug("error null turnout!");
                    return "error";
                }
                if (col == INVERTCOL) {
                    boolean val = t.getInverted();
                    return Boolean.valueOf(val);
                } else if (col == LOCKCOL) {
                    boolean val = t.getLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT);
                    return Boolean.valueOf(val);
                } else if (col == KNOWNCOL) {
                    if (t.getKnownState() == Turnout.CLOSED) {
                        return closedText;
                    }
                    if (t.getKnownState() == Turnout.THROWN) {
                        return thrownText;
                    }
                    if (t.getKnownState() == Turnout.INCONSISTENT) {
                        return "Inconsistent";
                    } else {
                        return "Unknown";
                    }
                } else if (col == MODECOL) {
                    JComboBox<String> c = new JComboBox<String>(t.getValidFeedbackNames());
                    c.setSelectedItem(t.getFeedbackModeName());
                    c.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if (col == SENSOR1COL) {
                    return t.getFirstSensor();
                } else if (col == SENSOR2COL) {
                    return t.getSecondSensor();
                } else if (col == OPSONOFFCOL) {
                    return makeAutomationBox(t);
                } else if (col == OPSEDITCOL) {
                    return Bundle.getMessage("EditTurnoutOperation");
                } else if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == LOCKDECCOL) {
                    JComboBox<String> c = new JComboBox<String>(t.getValidDecoderNames());
                    c.setSelectedItem(t.getDecoderName());
                    c.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if (col == LOCKOPRCOL) {
                    JComboBox<String> c = new JComboBox<String>(lockOperations);
                    if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        c.setSelectedItem(bothText);
                    } else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        c.setSelectedItem(pushbutText);
                    } else if (t.canLock(Turnout.CABLOCKOUT)) {
                        c.setSelectedItem(cabOnlyText);
                    } else {
                        c.setSelectedItem(noneText);
                    }
                    c.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if (col == STRAIGHTCOL) {

                    String speed = t.getStraightSpeed();
                    if (!speedListClosed.contains(speed)) {
                        speedListClosed.add(speed);
                    }
                    JComboBox<String> c = new JComboBox<String>(speedListClosed);
                    c.setEditable(true);
                    c.setSelectedItem(speed);

                    return c;
                } else if (col == DIVERGCOL) {

                    String speed = t.getDivergingSpeed();
                    if (!speedListThrown.contains(speed)) {
                        speedListThrown.add(speed);
                    }
                    JComboBox<String> c = new JComboBox<String>(speedListThrown);
                    c.setEditable(true);
                    c.setSelectedItem(speed);
                    return c;
                }
                return super.getValueAt(row, col);
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                String name = sysNameList.get(row);
                TurnoutManager manager = turnManager;
                Turnout t = manager.getBySystemName(name);
                if (col == INVERTCOL) {
                    if (t.canInvert()) {
                        boolean b = ((Boolean) value).booleanValue();
                        t.setInverted(b);
                    }
                } else if (col == LOCKCOL) {
                    boolean b = ((Boolean) value).booleanValue();
                    t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, b);
                } else if (col == MODECOL) {
                    @SuppressWarnings("unchecked")
                    String modeName = (String) ((JComboBox<String>) value).getSelectedItem();
                    t.setFeedbackMode(modeName);
                } else if (col == SENSOR1COL) {
                    try {
                        t.provideFirstFeedbackSensor((String) value);
                    } catch (jmri.JmriException e) {
                        JOptionPane.showMessageDialog(null, e.toString());
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == SENSOR2COL) {
                    try {
                        t.provideSecondFeedbackSensor((String) value);
                    } catch (jmri.JmriException e) {
                        JOptionPane.showMessageDialog(null, e.toString());
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == OPSONOFFCOL) {
                    // do nothing as this is handled by the combo box listener
                } else if (col == OPSEDITCOL) {
                    t.setInhibitOperation(false);
                    @SuppressWarnings("unchecked") // cast to JComboBox<String> required in OPSEDITCOL
                    JComboBox<String> cb = (JComboBox<String>) getValueAt(row, OPSONOFFCOL);
                    editTurnoutOperation(t, cb);
                } else if (col == EDITCOL) {
                    class WindowMaker implements Runnable {

                        Turnout t;

                        WindowMaker(Turnout t) {
                            this.t = t;
                        }

                        public void run() {
                            editButton(t);
                        }
                    }
                    WindowMaker w = new WindowMaker(t);
                    javax.swing.SwingUtilities.invokeLater(w);
                } else if (col == LOCKOPRCOL) {
                    @SuppressWarnings("unchecked")
                    String lockOpName = (String) ((JComboBox<String>) value)
                            .getSelectedItem();
                    if (lockOpName.equals(bothText)) {
                        t.enableLockOperation(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
                    }
                    if (lockOpName.equals(cabOnlyText)) {
                        t.enableLockOperation(Turnout.CABLOCKOUT, true);
                        t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
                    }
                    if (lockOpName.equals(pushbutText)) {
                        t.enableLockOperation(Turnout.CABLOCKOUT, false);
                        t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
                    }
                } else if (col == LOCKDECCOL) {
                    @SuppressWarnings("unchecked")
                    String decoderName = (String) ((JComboBox<String>) value).getSelectedItem();
                    t.setDecoderName(decoderName);
                } else if (col == STRAIGHTCOL) {
                    @SuppressWarnings("unchecked")
                    String speed = (String) ((JComboBox<String>) value).getSelectedItem();
                    try {
                        t.setStraightSpeed(speed);
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                        return;
                    }
                    if ((!speedListClosed.contains(speed)) && !speed.contains("Global")) {
                        speedListClosed.add(speed);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == DIVERGCOL) {

                    @SuppressWarnings("unchecked")
                    String speed = (String) ((JComboBox<String>) value).getSelectedItem();
                    try {
                        t.setDivergingSpeed(speed);
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                        return;
                    }
                    if ((!speedListThrown.contains(speed)) && !speed.contains("Global")) {
                        speedListThrown.add(speed);
                    }
                    fireTableRowsUpdated(row, row);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            public String getValue(String name) {
                int val = turnManager.getBySystemName(name).getCommandedState();
                switch (val) {
                    case Turnout.CLOSED:
                        return closedText;
                    case Turnout.THROWN:
                        return thrownText;
                    case Turnout.UNKNOWN:
                        return Bundle.getMessage("BeanStateUnknown");
                    case Turnout.INCONSISTENT:
                        return Bundle.getMessage("BeanStateInconsistent");
                    default:
                        return "Unexpected value: " + val;
                }
            }

            public Manager getManager() {
                return turnManager;
            }

            public NamedBean getBySystemName(String name) {
                return turnManager.getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return turnManager.getByUserName(name);
            }

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                int state = ((Turnout) t).getCommandedState();
                if (state == Turnout.CLOSED) {
                    ((Turnout) t).setCommandedState(Turnout.THROWN);
                } else {
                    ((Turnout) t).setCommandedState(Turnout.CLOSED);
                }
            }

            @Override
            public void configureTable(JTable tbl) {
                table = tbl;

                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                setColumnToHoldButton(table, OPSEDITCOL, editButton());
                setColumnToHoldButton(table, EDITCOL, editButton());
                //Hide the following columns by default
                XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
                TableColumn column = columnModel.getColumnByModelIndex(STRAIGHTCOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(DIVERGCOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(KNOWNCOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(MODECOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(SENSOR1COL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(SENSOR2COL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(OPSONOFFCOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(OPSEDITCOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(LOCKOPRCOL);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(LOCKDECCOL);
                columnModel.setColumnVisible(column, false);

                super.configureTable(table);
            }

            // update table if turnout lock or feedback changes
            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("locked")) {
                    return true;
                }
                if (e.getPropertyName().equals("feedbackchange")) {
                    return true;
                }
                if (e.getPropertyName().equals("TurnoutDivergingSpeedChange")) {
                    return true;
                }
                if (e.getPropertyName().equals("TurnoutStraightSpeedChange")) {
                    return true;
                } else {
                    return super.matchPropertyName(e);
                }
            }

            public void comboBoxAction(ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("Combobox change");
                }
                if (table != null && table.getCellEditor() != null) {
                    table.getCellEditor().stopCellEditing();
                }
            }

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("DefaultTurnoutClosedSpeedChange")) {
                    updateClosedList();
                } else if (e.getPropertyName().equals("DefaultTurnoutThrownSpeedChange")) {
                    updateThrownList();
                } else {
                    super.propertyChange(e);
                }
            }

            protected String getBeanType() {
                return Bundle.getMessage("BeanNameTurnout");
            }

            TableSorter sorter;

            public JTable makeJTable(TableSorter srtr) {
                this.sorter = srtr;
                JTable table = new JTable(srtr) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = -3082196412648458792L;

                    public TableCellRenderer getCellRenderer(int row, int column) {
                        //Convert the displayed index to the model index, rather than the displayed index
                        int modelColumn = getColumnModel().getColumn(column).getModelIndex();
                        if (modelColumn == SENSOR1COL || modelColumn == SENSOR2COL) {
                            return getRenderer(row, modelColumn);
                        } else {
                            return super.getCellRenderer(row, column);
                        }
                    }

                    public TableCellEditor getCellEditor(int row, int column) {
                        //Convert the displayed index to the model index, rather than the displayed index
                        int modelColumn = getColumnModel().getColumn(column).getModelIndex();
                        if (modelColumn == SENSOR1COL || modelColumn == SENSOR2COL) {
                            return getEditor(row, modelColumn);
                        } else {
                            return super.getCellEditor(row, column);
                        }
                    }

                    TableCellRenderer getRenderer(int row, int column) {
                        TableCellRenderer retval = null;
                        if (column == SENSOR1COL) {
                            retval = rendererMapSensor1.get(sorter.getValueAt(row, SYSNAMECOL));
                        } else if (column == SENSOR2COL) {
                            retval = rendererMapSensor2.get(sorter.getValueAt(row, SYSNAMECOL));
                        } else {
                            return null;
                        }

                        if (retval == null) {
                            Turnout t = turnManager.getBySystemName((String) sorter.getValueAt(row, SYSNAMECOL));
                            retval = new BeanBoxRenderer();
                            if (column == SENSOR1COL) {
                                ((JmriBeanComboBox) retval).setSelectedBean(t.getFirstSensor());
                                rendererMapSensor1.put(sorter.getValueAt(row, SYSNAMECOL), retval);
                            } else {
                                ((JmriBeanComboBox) retval).setSelectedBean(t.getSecondSensor());
                                rendererMapSensor2.put(sorter.getValueAt(row, SYSNAMECOL), retval);
                            }
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellRenderer> rendererMapSensor1 = new Hashtable<Object, TableCellRenderer>();
                    Hashtable<Object, TableCellRenderer> rendererMapSensor2 = new Hashtable<Object, TableCellRenderer>();

                    TableCellEditor getEditor(int row, int column) {
                        TableCellEditor retval = null;
                        if (column == SENSOR1COL) {
                            retval = editorMapSensor1.get(sorter.getValueAt(row, SYSNAMECOL));
                        } else if (column == SENSOR2COL) {
                            retval = editorMapSensor2.get(sorter.getValueAt(row, SYSNAMECOL));
                        } else {
                            return null;
                        }
                        if (retval == null) {
                            Turnout t = turnManager.getBySystemName((String) sorter.getValueAt(row, SYSNAMECOL));

                            JmriBeanComboBox c;

                            if (column == SENSOR1COL) {
                                c = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), t.getFirstSensor(), JmriBeanComboBox.DISPLAYNAME);
                                retval = new BeanComboBoxEditor(c);
                                editorMapSensor1.put(sorter.getValueAt(row, SYSNAMECOL), retval);
                            } else { //Must be two
                                c = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), t.getSecondSensor(), JmriBeanComboBox.DISPLAYNAME);
                                retval = new BeanComboBoxEditor(c);
                                editorMapSensor2.put(sorter.getValueAt(row, SYSNAMECOL), retval);
                            }
                            c.setFirstItemBlank(true);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellEditor> editorMapSensor1 = new Hashtable<Object, TableCellEditor>();
                    Hashtable<Object, TableCellEditor> editorMapSensor2 = new Hashtable<Object, TableCellEditor>();
                };
                table.getTableHeader().setReorderingAllowed(true);
                table.setColumnModel(new XTableColumnModel());
                table.createDefaultColumnsFromModel();

                addMouseListenerToHeader(table);
                return table;
            }

        };  // end of custom data model
    }

    private void updateClosedList() {
        speedListClosed.remove(defaultClosedSpeedText);
        defaultClosedSpeedText = ("Use Global " + turnManager.getDefaultClosedSpeed());
        speedListClosed.add(0, defaultClosedSpeedText);
        m.fireTableDataChanged();
    }

    private void updateThrownList() {
        speedListThrown.remove(defaultThrownSpeedText);
        defaultThrownSpeedText = ("Use Global " + turnManager.getDefaultThrownSpeed());
        speedListThrown.add(0, defaultThrownSpeedText);
        m.fireTableDataChanged();
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleTurnoutTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(40);
    JTextField userName = new JTextField(40);
    JComboBox<String> prefixBox = new JComboBox<String>();
    JTextField numberToAdd = new JTextField(5);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JLabel sysNameLabel = new JLabel("Hardware Address"); // I18N TODO
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    String userNameError = this.getClass().getName() + ".DuplicateUserName";
    jmri.UserPreferencesManager p;

    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddTurnout"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TurnoutAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };

            ActionListener rangeListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    canAddRange(e);
                }
            };
            /* We use the proxy manager in this instance so that we can deal with 
             duplicate usernames in multiple classes */
            if (InstanceManager.turnoutManagerInstance() instanceof jmri.managers.AbstractProxyManager) {
                jmri.managers.ProxyTurnoutManager proxy = (jmri.managers.ProxyTurnoutManager) InstanceManager.turnoutManagerInstance();
                List<Manager> managerList = proxy.getManagerList();
                for (int x = 0; x < managerList.size(); x++) {
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    //Simple test not to add a system with a duplicate System prefix
                    for (int i = 0; i < prefixBox.getItemCount(); i++) {
                        if (prefixBox.getItemAt(i).equals(manuName)) {
                            addToPrefix = false;
                        }
                    }
                    if (addToPrefix) {
                        prefixBox.addItem(manuName);
                    }
                }
                if (p.getComboBoxLastSelection(systemSelectionCombo) != null) {
                    prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
                }
            } else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(turnManager.getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonOK", listener, rangeListener));
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    /**
     * Create a JComboBox<String> containing all the options for turnout
     * automation parameters for this turnout
     *
     * @param t	the turnout
     * @return	the JComboBox
     */
    protected JComboBox<String> makeAutomationBox(Turnout t) {
        String[] str = new String[]{"empty"};
        final JComboBox<String> cb = new JComboBox<String>(str);
        final Turnout myTurnout = t;
        updateAutomationBox(t, cb);
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTurnoutOperation(myTurnout, cb);
                cb.removeActionListener(this);		// avoid recursion
                updateAutomationBox(myTurnout, cb);
                cb.addActionListener(this);
            }
        });
        return cb;
    }

    /**
     * Create a JButton to edit a turnout operation.
     *
     * @return	the JButton
     */
    protected JButton editButton() {
        JButton editButton = new JButton(Bundle.getMessage("EditTurnoutOperation"));
        return (editButton);
    }

    /**
     * Add the content and make the appropriate selection to a combox box for a
     * turnout's automation choices
     *
     * @param t	 turnout
     * @param cb	the JComboBox
     */
    public static void updateAutomationBox(Turnout t, JComboBox<String> cb) {
        TurnoutOperation[] ops = TurnoutOperationManager.getInstance().getTurnoutOperations();
        cb.removeAllItems();
        Vector<String> strings = new Vector<String>(20);
        Vector<String> defStrings = new Vector<String>(20);
        if (log.isDebugEnabled()) {
            log.debug("start " + ops.length);
        }
        for (int i = 0; i < ops.length; ++i) {
            if (log.isDebugEnabled()) {
                log.debug("isDef " + ops[i].isDefinitive()
                        + " mFMM " + ops[i].matchFeedbackMode(t.getFeedbackMode())
                        + " isNonce " + ops[i].isNonce());
            }
            if (!ops[i].isDefinitive()
                    && ops[i].matchFeedbackMode(t.getFeedbackMode())
                    && !ops[i].isNonce()) {
                strings.addElement(ops[i].getName());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("end");
        }
        for (int i = 0; i < ops.length; ++i) {
            if (ops[i].isDefinitive()
                    && ops[i].matchFeedbackMode(t.getFeedbackMode())) {
                defStrings.addElement(ops[i].getName());
            }
        }
        java.util.Collections.sort(strings);
        java.util.Collections.sort(defStrings);
        strings.insertElementAt(Bundle.getMessage("TurnoutOperationOff"), 0);
        strings.insertElementAt(Bundle.getMessage("TurnoutOperationDefault"), 1);
        for (int i = 0; i < defStrings.size(); ++i) {
            try {
                strings.insertElementAt(defStrings.elementAt(i), i + 2);
            } catch (java.lang.ArrayIndexOutOfBoundsException obe) {
                //	           strings.insertElementAt(defStrings.elementAt(i),i+2);
            }
        }
        for (int i = 0; i < strings.size(); ++i) {
            cb.addItem(strings.elementAt(i));
        }
        if (t.getInhibitOperation()) {
            cb.setSelectedIndex(0);
        } else if (t.getTurnoutOperation() == null) {
            cb.setSelectedIndex(1);
        } else if (t.getTurnoutOperation().isNonce()) {
            cb.setSelectedIndex(2);
        } else {
            cb.setSelectedItem(t.getTurnoutOperation().getName());
        }
    }

    /**
     * set the turnout's operation info based on the contents of the combo box
     *
     * @param t	 turnout
     * @param cb JComboBox
     */
    protected void setTurnoutOperation(Turnout t, JComboBox<String> cb) {
        switch (cb.getSelectedIndex()) {
            case 0:			// Off
                t.setInhibitOperation(true);
                t.setTurnoutOperation(null);
                break;
            case 1:			// Default
                t.setInhibitOperation(false);
                t.setTurnoutOperation(null);
                break;
            default:		// named operation
                t.setInhibitOperation(false);
                t.setTurnoutOperation(TurnoutOperationManager.getInstance().
                        getOperation(((String) cb.getSelectedItem())));
                break;
        }
    }

    void editButton(Turnout t) {
        jmri.jmrit.beantable.beanedit.TurnoutEditAction beanEdit = new jmri.jmrit.beantable.beanedit.TurnoutEditAction();
        beanEdit.setBean(t);
        beanEdit.actionPerformed(null);
    }

    /**
     * pop up a TurnoutOperationConfig for the turnout
     *
     * @param t   turnout
     * @param box JComboBox that triggered the edit
     */
    protected void editTurnoutOperation(Turnout t, JComboBox<String> box) {
        TurnoutOperation op = t.getTurnoutOperation();
        if (op == null) {
            TurnoutOperation proto = TurnoutOperationManager.getInstance().getMatchingOperationAlways(t);
            if (proto != null) {
                op = proto.makeNonce(t);
                t.setTurnoutOperation(op);
            }
        }
        if (op != null) {
            if (!op.isNonce()) {
                op = op.makeNonce(t);
            }
            // make and show edit dialog
            TurnoutOperationEditor dialog = new TurnoutOperationEditor(this, f, op, t, box);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(f, "There is no operation type suitable for this turnout",
                    "No operation type", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected static class TurnoutOperationEditor extends JDialog {

        /**
         *
         */
        private static final long serialVersionUID = 3523604197678096714L;
        TurnoutOperationConfig config;
        TurnoutOperation myOp;
        Turnout myTurnout;

        TurnoutOperationEditor(TurnoutTableAction tta, JFrame parent, TurnoutOperation op, Turnout t, JComboBox<String> box) {
            super(parent);
            final TurnoutOperationEditor self = this;
            myOp = op;
            myOp.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("Deleted")) {
                        setVisible(false);
                    }
                }
            });
            myTurnout = t;
            config = TurnoutOperationConfig.getConfigPanel(op);
            setTitle();
            if (config != null) {
                Box outerBox = Box.createVerticalBox();
                outerBox.add(config);
                Box buttonBox = Box.createHorizontalBox();
                JButton nameButton = new JButton("Give name to this setting"); // I18N TODO
                nameButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String newName = JOptionPane.showInputDialog("New name for this parameter setting:"); // I18N TODO
                        if (newName != null && !newName.equals("")) {
                            if (!myOp.rename(newName)) {
                                JOptionPane.showMessageDialog(self, "This name is already in use",
                                        "Name already in use", JOptionPane.ERROR_MESSAGE);
                            }
                            setTitle();
                            myTurnout.setTurnoutOperation(null);
                            myTurnout.setTurnoutOperation(myOp);	// no-op but updates display - have to <i>change</i> value
                        }
                    }
                });
                JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        config.endConfigure();
                        if (myOp.isNonce() && myOp.equivalentTo(myOp.getDefinitive())) {
                            myTurnout.setTurnoutOperation(null);
                            myOp.dispose();
                            myOp = null;
                        }
                        self.setVisible(false);
                    }
                });
                JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        self.setVisible(false);
                    }
                });
                buttonBox.add(Box.createHorizontalGlue());
                if (!op.isDefinitive()) {
                    buttonBox.add(nameButton);
                }
                buttonBox.add(okButton);
                buttonBox.add(cancelButton);
                outerBox.add(buttonBox);
                getContentPane().add(outerBox);
            }
            pack();
        }

        private void setTitle() {
            String title = "Turnout Operation \"" + myOp.getName() + "\""; // I18N TODO
            if (myOp.isNonce()) {
                title = "Turnout operation for turnout " + myTurnout.getSystemName();
            }
            setTitle(title);
        }
    }

    JCheckBox showFeedbackBox = new JCheckBox("Show Feedback information"); //I18N TODO
    JCheckBox showLockBox = new JCheckBox("Show Lock information");
    JCheckBox showTurnoutSpeedBox = new JCheckBox("Show Turnout Speed details");
    JCheckBox doAutomationBox = new JCheckBox("Automatic retry");

    protected void setDefaultSpeeds(JFrame _who) {
        JComboBox<String> thrownCombo = new JComboBox<String>(speedListThrown);
        JComboBox<String> closedCombo = new JComboBox<String>(speedListClosed);
        thrownCombo.setEditable(true);
        closedCombo.setEditable(true);

        JPanel thrown = new JPanel();
        thrown.add(new JLabel("Thrown Speed")); // I18N TODO
        thrown.add(thrownCombo);

        JPanel closed = new JPanel();
        closed.add(new JLabel("Closed Speed")); // I18N TODO
        closed.add(closedCombo);

        thrownCombo.removeItem(defaultThrownSpeedText);
        closedCombo.removeItem(defaultClosedSpeedText);

        thrownCombo.setSelectedItem(turnManager.getDefaultThrownSpeed());
        closedCombo.setSelectedItem(turnManager.getDefaultClosedSpeed());

        int retval = JOptionPane.showOptionDialog(_who,
                Bundle.getMessage("TurnoutGlobalSpeedMessage"), Bundle.getMessage("TurnoutGlobalSpeedMessageTitle"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{"Cancel", "OK", thrown, closed}, null); // I18N TODO
        if (retval != 1) {
            return;
        }

        String closedValue = (String) closedCombo.getSelectedItem();
        String thrownValue = (String) thrownCombo.getSelectedItem();
        //We will allow the turnout manager to handle checking if the values have changed
        try {
            turnManager.setDefaultThrownSpeed(thrownValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + thrownValue);
        }

        try {
            turnManager.setDefaultClosedSpeed(closedValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + closedValue);
        }
    }

    /**
     * Add the check boxes
     */
    @Override
    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(doAutomationBox, this.getClass().getName());
        doAutomationBox.setSelected(TurnoutOperationManager.getInstance().getDoOperations());
        doAutomationBox.setToolTipText(Bundle.getMessage("TurnoutDoAutomationBoxTooltip"));
        doAutomationBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
            }
        });
        f.addToBottomBox(showFeedbackBox, this.getClass().getName());
        showFeedbackBox.setToolTipText(Bundle.getMessage("TurnoutFeedbackToolTip"));
        showFeedbackBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFeedbackChanged();
            }
        });
        f.addToBottomBox(showLockBox, this.getClass().getName());
        showLockBox.setToolTipText(Bundle.getMessage("TurnoutLockToolTip"));
        showLockBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLockChanged();
            }
        });
        f.addToBottomBox(showTurnoutSpeedBox, this.getClass().getName());
        showTurnoutSpeedBox.setToolTipText(Bundle.getMessage("TurnoutSpeedToolTip"));
        showTurnoutSpeedBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTurnoutSpeedChanged();
            }
        });
    }

    @Override
    public void addToPanel(AbstractTableTabAction f) {
        String systemPrefix = ConnectionNameFromSystemName.getConnectionName(turnManager.getSystemPrefix());

        if (turnManager.getClass().getName().contains("ProxyTurnoutManager")) {
            systemPrefix = "All";
        }
        f.addToBottomBox(doAutomationBox, systemPrefix);
        doAutomationBox.setSelected(TurnoutOperationManager.getInstance().getDoOperations());
        doAutomationBox.setToolTipText(Bundle.getMessage("TurnoutDoAutomationBoxTooltip"));
        doAutomationBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
            }
        });
        f.addToBottomBox(showFeedbackBox, systemPrefix);
        showFeedbackBox.setToolTipText(Bundle.getMessage("TurnoutFeedbackToolTip"));
        showFeedbackBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFeedbackChanged();
            }
        });
        f.addToBottomBox(showLockBox, systemPrefix);
        showLockBox.setToolTipText(Bundle.getMessage("TurnoutLockToolTip"));
        showLockBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLockChanged();
            }
        });
        f.addToBottomBox(showTurnoutSpeedBox, systemPrefix);
        showTurnoutSpeedBox.setToolTipText(Bundle.getMessage("TurnoutSpeedToolTip"));
        showTurnoutSpeedBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTurnoutSpeedChanged();
            }
        });
    }

    void showFeedbackChanged() {
        boolean showFeedback = showFeedbackBox.isSelected();
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(KNOWNCOL);
        columnModel.setColumnVisible(column, showFeedback);
        column = columnModel.getColumnByModelIndex(MODECOL);
        columnModel.setColumnVisible(column, showFeedback);
        column = columnModel.getColumnByModelIndex(SENSOR1COL);
        columnModel.setColumnVisible(column, showFeedback);
        column = columnModel.getColumnByModelIndex(SENSOR2COL);
        columnModel.setColumnVisible(column, showFeedback);
        column = columnModel.getColumnByModelIndex(OPSONOFFCOL);
        columnModel.setColumnVisible(column, showFeedback);
        column = columnModel.getColumnByModelIndex(OPSEDITCOL);
        columnModel.setColumnVisible(column, showFeedback);
    }

    void showLockChanged() {
        boolean showLock = showLockBox.isSelected();
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = ((XTableColumnModel) table.getColumnModel()).getColumnByModelIndex(LOCKDECCOL);
        columnModel.setColumnVisible(column, showLock);
        column = columnModel.getColumnByModelIndex(LOCKOPRCOL);
        columnModel.setColumnVisible(column, showLock);
    }

    //boolean showTurnoutSpeed = false;
    public void showTurnoutSpeedChanged() {
        boolean showTurnoutSpeed = showTurnoutSpeedBox.isSelected();
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = ((XTableColumnModel) table.getColumnModel()).getColumnByModelIndex(STRAIGHTCOL);
        columnModel.setColumnVisible(column, showTurnoutSpeed);
        column = columnModel.getColumnByModelIndex(DIVERGCOL);
        columnModel.setColumnVisible(column, showTurnoutSpeed);
    }

    // Add Operations menu items
    @Override
    public void setMenuBar(BeanTableFrame f) {
        final jmri.util.JmriJFrame finalF = f;			// needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        // check for menu
        int menus = menuBar.getMenuCount();
        if (menus < 3) {

            JMenu opsMenu = new JMenu(Bundle.getMessage("TurnoutAutomationMenu"));
            JMenuItem item = new JMenuItem(Bundle.getMessage("TurnoutAutomationMenuItemEdit"));
            opsMenu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new TurnoutOperationFrame(finalF);
                }
            });
            menuBar.add(opsMenu);

            JMenu speedMenu = new JMenu(Bundle.getMessage("TurnoutSpeedsMenu"));
            item = new JMenuItem(Bundle.getMessage("TurnoutSpeedsMenuItemDefaults"));
            speedMenu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setDefaultSpeeds(finalF);
                }
            });
            menuBar.add(speedMenu);
        }
    }

    void okPressed(ActionEvent e) {
        // Test if bit already in use as a light
        //int iName=0;
        int numberOfTurnouts = 1;

        if (range.isSelected()) {
            try {
                numberOfTurnouts = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage("Error", "Number to turnouts to Add must be a number!", "" + ex, "", true, false); // I18N TODO
                return;
            }
        }
        if (numberOfTurnouts >= 65) {
            if (JOptionPane.showConfirmDialog(addFrame,
                    "You are about to add " + numberOfTurnouts + " Turnouts into the configuration\nAre you sure?", "Warning",
                    JOptionPane.YES_NO_OPTION) == 1) {
                // I18N TODO
                return;
            }
        }

        String sName = null;
        String curAddress = sysName.getText();
        //String[] turnoutList = turnManager.formatRangeOfAddresses(sysName.getText(), numberOfTurnouts, getTurnoutPrefixFromName());
        //if (turnoutList == null)
        //    return;
        int iType = 0;
        int iNum = 1;
        boolean useLastBit = false;
        boolean useLastType = false;
        String prefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        for (int x = 0; x < numberOfTurnouts; x++) {
            try {
                curAddress = InstanceManager.turnoutManagerInstance().getNextValidAddress(curAddress, prefix);
            } catch (jmri.JmriException ex) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage("Error", "Unable to convert '" + curAddress + "' to a valid Hardware Address", "" + ex, "", true, false);
                return;
            }
            if (curAddress == null) {
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName = prefix + InstanceManager.turnoutManagerInstance().typeLetter() + curAddress;
            String testSN = prefix + "L" + curAddress;
            jmri.Light testLight = InstanceManager.lightManagerInstance().
                    getBySystemName(testSN);
            if (testLight != null) {
                // Address is already used as a Light
                log.warn("Requested Turnout " + sName + " uses same address as Light " + testSN);
                if (!noWarn) {
                    int selectedValue = JOptionPane.showOptionDialog(addFrame,
                            Bundle.getMessage("TurnoutWarn1") + " " + sName + " " + Bundle.getMessage("TurnoutWarn2") + " "
                            + testSN + ".\n   " + Bundle.getMessage("TurnoutWarn3"), Bundle.getMessage("WarningTitle"),
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                                Bundle.getMessage("ButtonYesPlus")}, Bundle.getMessage("ButtonNo"));
                    if (selectedValue == 1) {
                        return;   // return without creating if "No" response
                    }
                    if (selectedValue == 2) {
                        // Suppress future warnings, and continue
                        noWarn = true;
                    }
                }
            }
            // Ask about two bit turnout control if appropriate

            if (!useLastBit) {
                iNum = InstanceManager.turnoutManagerInstance().askNumControlBits(sName);
                if ((InstanceManager.turnoutManagerInstance().isNumControlBitsSupported(sName)) && (range.isSelected())) {
                    if (JOptionPane.showConfirmDialog(addFrame,
                            "Do you want to use the last setting for all turnouts in this range? ", "Use Setting",
                            JOptionPane.YES_NO_OPTION) == 0) {
                        useLastBit = true;
                    }
                    // Add a pop up here asking if the user wishes to use the same value for all
                } else {
                    //as isNumControlBits is not supported, then we will always use the same value.
                    useLastBit = true;
                }
            }
            if (iNum == 0) {
                // User specified more bits, but bits are not available - return without creating
                return;
            } else {

                // Create the new turnout
                Turnout t;
                try {
                    t = InstanceManager.turnoutManagerInstance().provideTurnout(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(ex, sName);
                    return; // without creating       
                }

                if (t != null) {
                    String user = userName.getText();
                    if ((x != 0) && user != null && !user.equals("")) {
                        user = user + ":" + x;
                    }
                    if (user != null && !user.equals("") && (InstanceManager.turnoutManagerInstance().getByUserName(user) == null)) {
                        t.setUserName(user);
                    } else if (InstanceManager.turnoutManagerInstance().getByUserName(user) != null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showErrorMessage("Duplicate UserName", "The username " + user + " specified is already in use and therefore will not be set", getClassName(), "duplicateUserName", false, true);
                        //p.showErrorMessage("Duplicate UserName", "The username " + user + " specified is already in use and therefore will not be set", userNameError, "", false, true);
                    }
                    t.setNumberOutputBits(iNum);
                    // Ask about the type of turnout control if appropriate
                    if (!useLastType) {
                        iType = InstanceManager.turnoutManagerInstance().askControlType(sName);
                        if ((InstanceManager.turnoutManagerInstance().isControlTypeSupported(sName)) && (range.isSelected())) {
                            if (JOptionPane.showConfirmDialog(addFrame,
                                    "Do you want to use the last setting for all turnouts in this range? ", "Use Setting",
                                    JOptionPane.YES_NO_OPTION) == 0)// Add a pop up here asking if the user wishes to use the same value for all
                            {
                                useLastType = true;
                            }
                        } else {
                            useLastType = true;
                        }
                    }
                    t.setControlType(iType);
                }
            }
        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }

    private void canAddRange(ActionEvent e) {
        range.setEnabled(false);
        range.setSelected(false);
        if (turnManager.getClass().getName().contains("ProxyTurnoutManager")) {
            jmri.managers.ProxyTurnoutManager proxy = (jmri.managers.ProxyTurnoutManager) turnManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for (int x = 0; x < managerList.size(); x++) {
                jmri.TurnoutManager mgr = (jmri.TurnoutManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)) {
                    range.setEnabled(true);
                    return;
                }
            }
        } else if (turnManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))) {
            range.setEnabled(true);
        }
    }

    void handleCreateException(Exception ex, String sysName) {
        if (ex.getMessage() != null) { 
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    ex.getMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(
                            Bundle.getMessage("ErrorTurnoutAddFailed"),
                            new Object[]{sysName}),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean noWarn = false;

    protected String getClassName() {
        return TurnoutTableAction.class.getName();
    }

    @Override
    public void setMessagePreferencesDetails() {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "duplicateUserName", Bundle.getMessage("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleTurnoutTable");
    }

    static class BeanBoxRenderer extends JmriBeanComboBox implements TableCellRenderer {

        /**
         *
         */
        private static final long serialVersionUID = 7351045146446284915L;

        public BeanBoxRenderer() {
            super(InstanceManager.sensorManagerInstance());
            setFirstItemBlank(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            if (value instanceof NamedBean) {
                setSelectedBean((NamedBean) value);
            } else {
                setSelectedBean(null);
            }
            return this;
        }
    }

    static class BeanComboBoxEditor extends DefaultCellEditor {

        /**
         *
         */
        private static final long serialVersionUID = 1705620352249335223L;

        public BeanComboBoxEditor(JmriBeanComboBox beanBox) {
            super(beanBox);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutTableAction.class.getName());
}

/* @(#)TurnoutTableAction.java */
