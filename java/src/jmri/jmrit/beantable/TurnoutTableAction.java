package jmri.jmrit.beantable;

import apps.gui.GuiLafPreferencesManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor; // for iconLabel
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.implementation.SignalSpeedMap;
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
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 */
public class TurnoutTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public TurnoutTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary turnout manager available
        if (turnManager == null) {
            setEnabled(false);
        }

        //This following must contain the word Global for a correct match in the abstract turnout
        defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnManager.getDefaultThrownSpeed());
        defaultClosedSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnManager.getDefaultClosedSpeed());
        //This following must contain the word Block for a correct match in the abstract turnout
        useBlockSpeed = Bundle.getMessage("UseGlobal", "Block Speed");

        speedListClosed.add(defaultClosedSpeedText);
        speedListThrown.add(defaultThrownSpeedText);
        speedListClosed.add(useBlockSpeed);
        speedListThrown.add(useBlockSpeed);
        java.util.Vector<String> _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
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
    // I18N TODO but note storing in xml independent from Locale
    String useBlockSpeed = Bundle.getMessage("UseGlobal", "Block Speed");
    String bothText = "Both";
    String cabOnlyText = "Cab only";
    String pushbutText = "Pushbutton only";
    String noneText = "None";

    private java.util.Vector<String> speedListClosed = new java.util.Vector<String>();
    private java.util.Vector<String> speedListThrown = new java.util.Vector<String>();
    protected TurnoutManager turnManager = InstanceManager.turnoutManagerInstance();
    protected JTable table;
    // for icon state col
    protected boolean _graphicState = false; // updated from prefs

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
     * of Turnouts.
     */
    @Override
    protected void createModel() {
        // store the terminology
        closedText = turnManager.getClosedText();
        thrownText = turnManager.getThrownText();

        // load graphic state column display preference
        // from apps/GuiLafConfigPane.java
        _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();

        // create the data model object that drives the table
        // note that this is a class creation, and very long
        m = new BeanTableDataModel() {

            @Override
            public int getColumnCount() {
                return DIVERGCOL + 1;
            }

            @Override
            public String getColumnName(int col) {
                if (col == INVERTCOL) {
                    return Bundle.getMessage("Inverted");
                } else if (col == LOCKCOL) {
                    return Bundle.getMessage("Locked");
                } else if (col == KNOWNCOL) {
                    return Bundle.getMessage("Feedback");
                } else if (col == MODECOL) {
                    return Bundle.getMessage("ModeLabel");
                } else if (col == SENSOR1COL) {
                    return Bundle.getMessage("BlockSensor") + "1";
                } else if (col == SENSOR2COL) {
                    return Bundle.getMessage("BlockSensor") + "2";
                } else if (col == OPSONOFFCOL) {
                    return Bundle.getMessage("TurnoutAutomationMenu");
                } else if (col == OPSEDITCOL) {
                    return "";
                } else if (col == LOCKOPRCOL) {
                    return Bundle.getMessage("LockMode");
                } else if (col == LOCKDECCOL) {
                    return Bundle.getMessage("Decoder");
                } else if (col == DIVERGCOL) {
                    return Bundle.getMessage("ThrownSpeed");
                } else if (col == STRAIGHTCOL) {
                    return Bundle.getMessage("ClosedSpeed");
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
                } else if (col == VALUECOL && _graphicState) {
                    return JLabel.class; // use an image to show turnout state
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
                    // checkbox disabled unless current configuration allows locking
                    return t.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT);
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
                    // editable always so user can configure it, even if current configuration prevents locking now
                    return true;
                } else if (col == LOCKDECCOL) {
                    // editable always so user can configure it, even if current configuration prevents locking now
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
                    boolean val = t.getLocked(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT);
                    return Boolean.valueOf(val);
                } else if (col == KNOWNCOL) {
                    if (t.getKnownState() == Turnout.CLOSED) {
                        return closedText;
                    }
                    if (t.getKnownState() == Turnout.THROWN) {
                        return thrownText;
                    }
                    if (t.getKnownState() == Turnout.INCONSISTENT) {
                        return Bundle.getMessage("BeanStateInconsistent");
                    } else {
                        return Bundle.getMessage("BeanStateUnknown"); // "Unknown"
                    }
                } else if (col == MODECOL) {
                    JComboBox<String> c = new JComboBox<String>(t.getValidFeedbackNames());
                    c.setSelectedItem(t.getFeedbackModeName());
                    c.addActionListener(new ActionListener() {
                        @Override
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
                    JComboBox<String> c;
                    if ((t.getPossibleLockModes() & Turnout.PUSHBUTTONLOCKOUT) !=0 ) c = new JComboBox<String>(t.getValidDecoderNames());
                    else c = new JComboBox<String>(new String[]{t.getDecoderName()});
                    
                    c.setSelectedItem(t.getDecoderName());
                    c.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if (col == LOCKOPRCOL) {
                    java.util.Vector<String> lockOperations = new java.util.Vector<>();  // Vector is a JComboBox ctor; List is not
                    int modes = t.getPossibleLockModes();
                    if ( (modes & Turnout.CABLOCKOUT) !=0 && (modes & Turnout.PUSHBUTTONLOCKOUT) !=0 ) lockOperations.add(bothText);
                    if ( (modes & Turnout.CABLOCKOUT) !=0 ) lockOperations.add(cabOnlyText);
                    if ( (modes & Turnout.PUSHBUTTONLOCKOUT) !=0 ) lockOperations.add(pushbutText);
                    lockOperations.add(noneText);
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
                        @Override
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
                // } else if (col == VALUECOL && _graphicState) { // not neeeded as the
                //  graphic ImageIconRenderer uses the same super.getValueAt(row, col) as classic bean state text button
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
                    t.setLocked(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT, b);
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
                    log.debug("opsSelected = {}", getValueAt(row, OPSONOFFCOL).toString());
                    editTurnoutOperation(t, cb);
                } else if (col == EDITCOL) {
                    class WindowMaker implements Runnable {

                        Turnout t;

                        WindowMaker(Turnout t) {
                            this.t = t;
                        }

                        @Override
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
                        t.enableLockOperation(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT, true);
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
                } else if (col == VALUECOL && _graphicState) { // respond to clicking on ImageIconRenderer CellEditor
                    clickOn(t);
                    fireTableRowsUpdated(row, row);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
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

            @Override
            public Manager getManager() {
                return turnManager;
            }

            @Override
            public NamedBean getBySystemName(String name) {
                return turnManager.getBySystemName(name);
            }

            @Override
            public NamedBean getByUserName(String name) {
                return InstanceManager.getDefault(TurnoutManager.class).getByUserName(name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
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

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameTurnout");
            }

            /**
             * Customize the turnout table Value (State) column to show an appropriate graphic for the turnout state
             * if _graphicState = true, or (default) just show the localized state text
             * when the TableDataModel is being called from ListedTableAction.
             *
             * @param table a JTable of Turnouts
             */
            @Override
            protected void configValueColumn(JTable table) {
                // have the value column hold a JPanel (icon)
                //setColumnToHoldButton(table, VALUECOL, new JLabel("12345678")); // for larger, wide round icon, but cannot be converted to JButton
                // add extras, override BeanTableDataModel
                log.debug("Turnout configValueColumn (I am {})", super.toString());
                if (_graphicState) { // load icons, only once
                    table.setDefaultEditor(JLabel.class, new ImageIconRenderer()); // editor
                    table.setDefaultRenderer(JLabel.class, new ImageIconRenderer()); // item class copied from SwitchboardEditor panel
                } else {
                    super.configValueColumn(table); // classic text style state indication
                }
            }

            @Override
            public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @Nullable RowSorter<? extends TableModel> sorter) {
                JTable table = this.makeJTable(model);
                table.setName(name);
                table.setRowSorter(sorter);
                table.getTableHeader().setReorderingAllowed(true);
                table.setColumnModel(new XTableColumnModel());
                table.createDefaultColumnsFromModel();

                addMouseListenerToHeader(table);
                return table;
            }

            @Override
            public JTable makeJTable(TableSorter sorter) {
                JTable table = this.makeJTable((TableModel) sorter);
                table.getTableHeader().setReorderingAllowed(true);
                table.setColumnModel(new XTableColumnModel());
                table.createDefaultColumnsFromModel();

                addMouseListenerToHeader(table);
                return table;
            }

            private JTable makeJTable(TableModel model) {
                return new JTable(model) {

                    @Override
                    public TableCellRenderer getCellRenderer(int row, int column) {
                        // Convert the displayed index to the model index, rather than the displayed index
                        int modelColumn = this.convertColumnIndexToModel(column);
                        if (modelColumn == SENSOR1COL || modelColumn == SENSOR2COL) {
                            return getRenderer(row, modelColumn);
                        } else {
                            return super.getCellRenderer(row, column);
                        }
                    }

                    @Override
                    public TableCellEditor getCellEditor(int row, int column) {
                        //Convert the displayed index to the model index, rather than the displayed index
                        int modelColumn = this.convertColumnIndexToModel(column);
                        if (modelColumn == SENSOR1COL || modelColumn == SENSOR2COL) {
                            return getEditor(row, modelColumn);
                        } else {
                            return super.getCellEditor(row, column);
                        }
                    }

                    TableCellRenderer getRenderer(int row, int column) {
                        TableCellRenderer retval = null;
                        if (column == SENSOR1COL) {
                            retval = rendererMapSensor1.get(getModel().getValueAt(row, SYSNAMECOL));
                        } else if (column == SENSOR2COL) {
                            retval = rendererMapSensor2.get(getModel().getValueAt(row, SYSNAMECOL));
                        } else {
                            return null;
                        }

                        if (retval == null) {
                            Turnout t = turnManager.getBySystemName((String) getModel().getValueAt(row, SYSNAMECOL));
                            if (t == null) {
                                return null;
                            }
                            retval = new BeanBoxRenderer();
                            if (column == SENSOR1COL) {
                                ((JmriBeanComboBox) retval).setSelectedBean(t.getFirstSensor());
                                rendererMapSensor1.put(getModel().getValueAt(row, SYSNAMECOL), retval);
                            } else {
                                ((JmriBeanComboBox) retval).setSelectedBean(t.getSecondSensor());
                                rendererMapSensor2.put(getModel().getValueAt(row, SYSNAMECOL), retval);
                            }
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellRenderer> rendererMapSensor1 = new Hashtable<>();
                    Hashtable<Object, TableCellRenderer> rendererMapSensor2 = new Hashtable<>();

                    TableCellEditor getEditor(int row, int column) {
                        TableCellEditor retval = null;
                        switch (column) {
                            case SENSOR1COL:
                                retval = editorMapSensor1.get(getModel().getValueAt(row, SYSNAMECOL));
                                break;
                            case SENSOR2COL:
                                retval = editorMapSensor2.get(getModel().getValueAt(row, SYSNAMECOL));
                                break;
                            default:
                                return null;
                        }
                        if (retval == null) {
                            Turnout t = turnManager.getBySystemName((String) getModel().getValueAt(row, SYSNAMECOL));
                            if (t == null) {
                                return null;
                            }
                            JmriBeanComboBox c;
                            if (column == SENSOR1COL) {                             
                                c = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), t.getFirstSensor(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                                retval = new BeanComboBoxEditor(c);
                                editorMapSensor1.put(getModel().getValueAt(row, SYSNAMECOL), retval);
                            } else { //Must be two
                                c = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), t.getSecondSensor(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
                                retval = new BeanComboBoxEditor(c);
                                editorMapSensor2.put(getModel().getValueAt(row, SYSNAMECOL), retval);
                            }
                            c.setFirstItemBlank(true);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellEditor> editorMapSensor1 = new Hashtable<>();
                    Hashtable<Object, TableCellEditor> editorMapSensor2 = new Hashtable<>();
                };
            }

            /**
             * Visualize state in table as a graphic, customized for Turnouts (4 states).
             * Renderer and Editor are identical, as the cell contents are not actually edited,
             * only used to toggle state using {@link #clickOn(NamedBean)}.
             * @see jmri.jmrit.beantable.sensor.SensorTableDataModel.ImageIconRenderer
             * @see jmri.jmrit.beantable.BlockTableAction#createModel()
             * @see jmri.jmrit.beantable.LightTableAction#createModel()
             */
            class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

                protected JLabel label;
                protected String rootPath = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
                protected char beanTypeChar = 'T'; // for Turnout
                protected String onIconPath = rootPath + beanTypeChar + "-on-s.png";
                protected String offIconPath = rootPath + beanTypeChar + "-off-s.png";
                protected BufferedImage onImage;
                protected BufferedImage offImage;
                protected ImageIcon onIcon;
                protected ImageIcon offIcon;
                protected int iconHeight = -1;

                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    log.debug("Renderer Item = {}, State = {}", row, value);
                    if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                        loadIcons();
                        log.debug("icons loaded");
                    }
                    return updateLabel((String) value, row);
                }

                @Override
                public Component getTableCellEditorComponent(
                        JTable table, Object value, boolean isSelected,
                        int row, int column) {
                    log.debug("Renderer Item = {}, State = {}", row, value);
                    if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                        loadIcons();
                        log.debug("icons loaded");
                    }
                    return updateLabel((String) value, row);
                }

                public JLabel updateLabel(String value, int row) {
                    if (iconHeight > 0) { // if necessary, increase row height;
                        table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5));
                    }
                    if (value.equals(closedText) && offIcon != null) {
                        label = new JLabel(offIcon);
                        label.setVerticalAlignment(JLabel.BOTTOM);
                        log.debug("offIcon set");
                    } else if (value.equals(thrownText) && onIcon != null) {
                        label = new JLabel(onIcon);
                        label.setVerticalAlignment(JLabel.BOTTOM);
                        log.debug("onIcon set");
                    } else if (value.equals(Bundle.getMessage("BeanStateInconsistent"))) {
                        label = new JLabel("X", JLabel.CENTER); // centered text alignment
                        label.setForeground(Color.red);
                        log.debug("Turnout state inconsistent");
                        iconHeight = 0;
                    } else if (value.equals(Bundle.getMessage("BeanStateUnknown"))) {
                        label = new JLabel("?", JLabel.CENTER); // centered text alignment
                        log.debug("Turnout state unknown");
                        iconHeight = 0;
                    } else { // failed to load icon
                        label = new JLabel(value, JLabel.CENTER); // centered text alignment
                        log.warn("Error reading icons for TurnoutTable");
                        iconHeight = 0;
                    }
                    label.setToolTipText(value);
                    label.addMouseListener (new MouseAdapter ()
                    {
                        @Override
                        public final void mousePressed (MouseEvent evt)
                        {
                            log.debug("Clicked on icon in row {}", row);
                            stopCellEditing();
                        }
                    });
                    return label;
                }

                @Override
                public Object getCellEditorValue() {
                    log.debug("getCellEditorValue, me = {})", this.toString());
                    return this.toString();
                }

                /**
                 * Read and buffer graphics. Only called once for this table.
                 * @see #getTableCellEditorComponent(JTable, Object, boolean, int, int)
                 */
                protected void loadIcons() {
                    try {
                        onImage = ImageIO.read(new File(onIconPath));
                        offImage = ImageIO.read(new File(offIconPath));
                    } catch (IOException ex) {
                        log.error("error reading image from {} or {}", onIconPath, offIconPath, ex);
                    }
                    log.debug("Success reading images");
                    int imageWidth = onImage.getWidth();
                    int imageHeight = onImage.getHeight();
                    // scale icons 50% to fit in table rows
                    Image smallOnImage = onImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
                    Image smallOffImage = offImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
                    onIcon = new ImageIcon(smallOnImage);
                    offIcon = new ImageIcon(smallOffImage);
                    iconHeight = onIcon.getIconHeight();
                }

            } // end of ImageIconRenderer class

        }; // end of custom data model
    }

    private void updateClosedList() {
        speedListClosed.remove(defaultClosedSpeedText);
        defaultClosedSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnManager.getDefaultClosedSpeed());
        speedListClosed.add(0, defaultClosedSpeedText);
        m.fireTableDataChanged();
    }

    private void updateThrownList() {
        speedListThrown.remove(defaultThrownSpeedText);
        defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnManager.getDefaultThrownSpeed());
        speedListThrown.add(0, defaultThrownSpeedText);
        m.fireTableDataChanged();
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleTurnoutTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysNameTextField = new JTextField(40);
    JTextField userNameTextField = new JTextField(40);
    JComboBox<String> prefixBox = new JComboBox<String>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    String userNameError = this.getClass().getName() + ".DuplicateUserName";
    jmri.UserPreferencesManager p;

    @Override
    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddTurnout"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TurnoutAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener okListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            ActionListener cancelListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            };
            ActionListener rangeListener = new ActionListener() {
                @Override
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
            sysNameTextField.setName("sysNameTextField");
            userNameTextField.setName("userNameTextField");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysNameTextField, userNameTextField, prefixBox, numberToAdd, range, "ButtonOK", okListener, cancelListener, rangeListener));
            //sysNameTextField.setToolTipText(Bundle.getMessage("HardwareAddressToolTip")); // already assigned by AddNew...
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    /**
     * Create a {@literal JComboBox<String>} containing all the options for
     * turnout automation parameters for this turnout
     *
     * @param t the turnout
     * @return the JComboBox
     */
    protected JComboBox<String> makeAutomationBox(Turnout t) {
        String[] str = new String[]{"empty"};
        final JComboBox<String> cb = new JComboBox<String>(str);
        final Turnout myTurnout = t;
        updateAutomationBox(t, cb);
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTurnoutOperation(myTurnout, cb);
                cb.removeActionListener(this);  // avoid recursion
                updateAutomationBox(myTurnout, cb);
                cb.addActionListener(this);
            }
        });
        return cb;
    }

    /**
     * Create a JButton to edit a turnout's operation.
     *
     * @return the JButton
     */
    protected JButton editButton() {
        JButton editButton = new JButton(Bundle.getMessage("EditTurnoutOperation"));
        return (editButton);
    }

    /**
     * Add the content and make the appropriate selection to a combox box for a
     * turnout's automation choices.
     *
     * @param t  turnout
     * @param cb the JComboBox
     */
    public static void updateAutomationBox(Turnout t, JComboBox<String> cb) {
        TurnoutOperation[] ops = TurnoutOperationManager.getInstance().getTurnoutOperations();
        cb.removeAllItems();
        Vector<String> strings = new Vector<String>(20);
        Vector<String> defStrings = new Vector<String>(20);
        log.debug("opsCombo start {}", ops.length);
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
        log.debug("opsCombo end");
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
                // just catch it
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
     * Set the turnout's operation info based on the contents of the combo box.
     *
     * @param t  turnout being configured
     * @param cb JComboBox for ops for t in the TurnoutTable
     */
    protected void setTurnoutOperation(Turnout t, JComboBox<String> cb) {
        switch (cb.getSelectedIndex()) {
            case 0:   // Off
                t.setInhibitOperation(true);
                t.setTurnoutOperation(null);
                break;
            case 1:   // Default
                t.setInhibitOperation(false);
                t.setTurnoutOperation(null);
                break;
            default:  // named operation
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

    private static boolean editingOps = false;

    /**
     * Pop up a TurnoutOperationConfig for the turnout.
     *
     * @param t   turnout
     * @param box JComboBox that triggered the edit
     */
    protected void editTurnoutOperation(Turnout t, JComboBox<String> box) {
        if (!editingOps) { // don't open a second edit ops pane
            editingOps = true;
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
                log.debug("TurnoutOpsEditDialog starting");
                TurnoutOperationEditor dialog = new TurnoutOperationEditor(this, f, op, t, box);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(f, "There is no operation type suitable for this turnout",
                        "No operation type", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected static class TurnoutOperationEditor extends JDialog {

        TurnoutOperationConfig config;
        TurnoutOperation myOp;
        Turnout myTurnout;

        TurnoutOperationEditor(TurnoutTableAction tta, JFrame parent, TurnoutOperation op, Turnout t, JComboBox<String> box) {
            super(parent);
            final TurnoutOperationEditor self = this;
            myOp = op;
            myOp.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("Deleted")) {
                        setVisible(false);
                    }
                }
            });
            myTurnout = t;
            config = TurnoutOperationConfig.getConfigPanel(op);
            setTitle();
            log.debug("TurnoutOpsEditDialog title set");
            if (config != null) {
                log.debug("OpsEditDialog opening");
                Box outerBox = Box.createVerticalBox();
                outerBox.add(config);
                Box buttonBox = Box.createHorizontalBox();
                JButton nameButton = new JButton(Bundle.getMessage("NameSetting"));
                nameButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String newName = JOptionPane.showInputDialog(Bundle.getMessage("NameParameterSetting"));
                        if (newName != null && !newName.equals("")) {
                            if (!myOp.rename(newName)) {
                                JOptionPane.showMessageDialog(self, "This name is already in use",
                                        "Name already in use", JOptionPane.ERROR_MESSAGE);
                            }
                            setTitle();
                            myTurnout.setTurnoutOperation(null);
                            myTurnout.setTurnoutOperation(myOp); // no-op but updates display - have to <i>change</i> value
                        }
                    }
                });
                JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        config.endConfigure();
                        if (myOp.isNonce() && myOp.equivalentTo(myOp.getDefinitive())) {
                            myTurnout.setTurnoutOperation(null);
                            myOp.dispose();
                            myOp = null;
                        }
                        self.setVisible(false);
                        editingOps = false;
                    }
                });
                JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        self.setVisible(false);
                        editingOps = false;
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
                this.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        editingOps = false; // reset Editmarker
                    }
                });
            } else {
                log.error("Error opening Turnout automation edit pane");
            }
            pack();
        }

        private void setTitle() {
            String title = Bundle.getMessage("TurnoutOperationTitle") + " \"" + myOp.getName() + "\"";
            if (myOp.isNonce()) {
                title = Bundle.getMessage("TurnoutOperationForTurnout") + " " + myTurnout.getSystemName();
            }
            setTitle(title);
        }
    }

    /**
     * Show a pane to configure closed and thrown turnout speed defaults.
     *
     * @param _who parent JFrame to center the pane on
     */
    protected void setDefaultSpeeds(JFrame _who) {
        JComboBox<String> thrownCombo = new JComboBox<String>(speedListThrown);
        JComboBox<String> closedCombo = new JComboBox<String>(speedListClosed);
        thrownCombo.setEditable(true);
        closedCombo.setEditable(true);

        JPanel thrown = new JPanel();
        thrown.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ThrownSpeed"))));
        thrown.add(thrownCombo);

        JPanel closed = new JPanel();
        closed.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ClosedSpeed"))));
        closed.add(closedCombo);

        thrownCombo.removeItem(defaultThrownSpeedText);
        closedCombo.removeItem(defaultClosedSpeedText);

        thrownCombo.setSelectedItem(turnManager.getDefaultThrownSpeed());
        closedCombo.setSelectedItem(turnManager.getDefaultClosedSpeed());

        // block of options above row of buttons; gleaned from Maintenance.makeDialog()
        // can be accessed by Jemmy in GUI test
        String title = Bundle.getMessage("TurnoutGlobalSpeedMessageTitle");
        // build JPanel for comboboxes
        JPanel speedspanel = new JPanel();
        speedspanel.setLayout(new BoxLayout(speedspanel, BoxLayout.PAGE_AXIS));
        speedspanel.add(new JLabel(Bundle.getMessage("TurnoutGlobalSpeedMessage")));
        //default LEFT_ALIGNMENT
        thrown.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedspanel.add(thrown);
        closed.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedspanel.add(closed);

        JOptionPane pane = new JOptionPane(
                speedspanel,
                JOptionPane.INFORMATION_MESSAGE,
                0,
                null,
                new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")});
        //pane.setxxx(value); // Configure more?
        JDialog dialog = pane.createDialog(_who, title);
        dialog.pack();
        dialog.show();

        if(pane.getValue() == null) { // pane close button was clicked, check before assigning to retval
            return;
        }
        Object retval = pane.getValue();
        log.debug("Retval = {}", retval.toString());
        // only 2 buttons to choose from, OK = button 2
        if ( retval != Bundle.getMessage("ButtonOK")) { // Cancel button clicked
            return;
        }
        String closedValue = (String) closedCombo.getSelectedItem();
        String thrownValue = (String) thrownCombo.getSelectedItem();

        // We will allow the turnout manager to handle checking whether the values have changed
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

    JCheckBox showFeedbackBox = new JCheckBox(Bundle.getMessage("ShowFeedbackInfo"));
    JCheckBox showLockBox = new JCheckBox(Bundle.getMessage("ShowLockInfo"));
    JCheckBox showTurnoutSpeedBox = new JCheckBox(Bundle.getMessage("ShowTurnoutSpeedDetails"));
    JCheckBox doAutomationBox = new JCheckBox(Bundle.getMessage("AutomaticRetry"));

    /**
     * Add the check boxes to the table frame to show/hide extra columns.
     *
     * @param f a Turnout table frame
     */
    @Override
    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(doAutomationBox, this.getClass().getName());
        doAutomationBox.setSelected(TurnoutOperationManager.getInstance().getDoOperations());
        doAutomationBox.setToolTipText(Bundle.getMessage("TurnoutDoAutomationBoxTooltip"));
        doAutomationBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
            }
        });
        f.addToBottomBox(showFeedbackBox, this.getClass().getName());
        showFeedbackBox.setToolTipText(Bundle.getMessage("TurnoutFeedbackToolTip"));
        showFeedbackBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFeedbackChanged();
            }
        });
        f.addToBottomBox(showLockBox, this.getClass().getName());
        showLockBox.setToolTipText(Bundle.getMessage("TurnoutLockToolTip"));
        showLockBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLockChanged();
            }
        });
        f.addToBottomBox(showTurnoutSpeedBox, this.getClass().getName());
        showTurnoutSpeedBox.setToolTipText(Bundle.getMessage("TurnoutSpeedToolTip"));
        showTurnoutSpeedBox.addActionListener(new ActionListener() {
            @Override
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
            @Override
            public void actionPerformed(ActionEvent e) {
                TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
            }
        });
        f.addToBottomBox(showFeedbackBox, systemPrefix);
        showFeedbackBox.setToolTipText(Bundle.getMessage("TurnoutFeedbackToolTip"));
        showFeedbackBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFeedbackChanged();
            }
        });
        f.addToBottomBox(showLockBox, systemPrefix);
        showLockBox.setToolTipText(Bundle.getMessage("TurnoutLockToolTip"));
        showLockBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLockChanged();
            }
        });
        f.addToBottomBox(showTurnoutSpeedBox, systemPrefix);
        showTurnoutSpeedBox.setToolTipText(Bundle.getMessage("TurnoutSpeedToolTip"));
        showTurnoutSpeedBox.addActionListener(new ActionListener() {
            @Override
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

    /**
     * Insert a table specific Operations menu. Account for the Window and Help
     * menus, which are already added to the menu bar as part of the creation of
     * the JFrame, by adding the Operations menu 2 places earlier unless the
     * table is part of the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        final jmri.util.JmriJFrame finalF = f;   // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        // check for menu
        boolean menuAbsent = true;
        for (int m = 0; m < menuBar.getMenuCount(); ++m) {
            String name = menuBar.getMenu(m).getAccessibleContext().getAccessibleName();
            if (name.equals(Bundle.getMessage("TurnoutAutomationMenu"))) {
                // using first menu for check, should be identical to next JMenu Bundle
                menuAbsent = false;
                break;
            }
        }
        if (menuAbsent) { // create it
            int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
            int offset = 1;
            log.debug("setMenuBar number of menu items = " + pos);
            for (int i = 0; i <= pos; i++) {
                if (menuBar.getComponent(i) instanceof JMenu) {
                    if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                        offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                    }
                }
            }
            JMenu opsMenu = new JMenu(Bundle.getMessage("TurnoutAutomationMenu"));
            JMenuItem item = new JMenuItem(Bundle.getMessage("TurnoutAutomationMenuItemEdit"));
            opsMenu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new TurnoutOperationFrame(finalF);
                }
            });
            menuBar.add(opsMenu, pos + offset);

            JMenu speedMenu = new JMenu(Bundle.getMessage("SpeedsMenu"));
            item = new JMenuItem(Bundle.getMessage("SpeedsMenuItemDefaults"));
            speedMenu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDefaultSpeeds(finalF);
                }
            });
            menuBar.add(speedMenu, pos + offset + 1); // add this menu to the right of the previous
        }
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    void okPressed(ActionEvent e) {

        int numberOfTurnouts = 1;

        if (range.isSelected()) {
            numberOfTurnouts = (Integer) numberToAdd.getValue();
        }
        if (numberOfTurnouts >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Turnouts"), numberOfTurnouts),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }

        String sName = null;
        String curAddress = sysNameTextField.getText().trim();
        //String[] turnoutList = turnManager.formatRangeOfAddresses(sysNameTextField.getText().trim(), numberOfTurnouts, getTurnoutPrefixFromName());
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
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertHW", curAddress), "" + ex, "", true, false);
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
                            Bundle.getMessage("UseForAllTurnouts"), Bundle.getMessage("UseSetting"),
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

                String user = userNameTextField.getText().trim();
                if ((x != 0) && user != null && !user.equals("")) {
                    user = user + ":" + x;
                }
                if (user != null && !user.equals("") && (InstanceManager.turnoutManagerInstance().getByUserName(user) == null)) {
                    t.setUserName(user);
                } else if (user != null && !user.equals("") && InstanceManager.turnoutManagerInstance().getByUserName(user) != null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                    InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", user), getClassName(), "duplicateUserName", false, true);
                }
                t.setNumberOutputBits(iNum);
                // Ask about the type of turnout control if appropriate
                if (!useLastType) {
                    iType = InstanceManager.turnoutManagerInstance().askControlType(sName);
                    if ((InstanceManager.turnoutManagerInstance().isControlTypeSupported(sName)) && (range.isSelected())) {
                        if (JOptionPane.showConfirmDialog(addFrame,
                                "Do you want to use the last setting for all turnouts in this range? ", "Use Setting",
                                JOptionPane.YES_NO_OPTION) == 0) // Add a pop up here asking if the user wishes to use the same value for all, // I18N TODO see above for existing keys
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

    void handleCreateException(Exception ex, String sysNameTextField) {
        if (ex.getMessage() != null) {
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    ex.getMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(
                            Bundle.getMessage("ErrorTurnoutAddFailed"),
                            new Object[]{sysNameTextField}),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean noWarn = false;

    @Override
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

        public BeanBoxRenderer() {
            super(InstanceManager.sensorManagerInstance());
            setFirstItemBlank(true);
        }

        @Override
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

        public BeanComboBoxEditor(JmriBeanComboBox beanBox) {
            super(beanBox);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutTableAction.class.getName());
}
