package jmri.jmrit.beantable.turnout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.*;
import jmri.util.swing.*;

/**
 * Data model for a Turnout Table.
 * Code originally within TurnoutTableAction.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class TurnoutTableDataModel extends BeanTableDataModel<Turnout>{

    public static final int INVERTCOL = BeanTableDataModel.NUMCOLUMN;
    public static final int LOCKCOL = INVERTCOL + 1;
    public static final int EDITCOL = LOCKCOL + 1;
    public static final int KNOWNCOL = EDITCOL + 1;
    public static final int MODECOL = KNOWNCOL + 1;
    public static final int SENSOR1COL = MODECOL + 1;
    public static final int SENSOR2COL = SENSOR1COL + 1;
    public static final int OPSONOFFCOL = SENSOR2COL + 1;
    public static final int OPSEDITCOL = OPSONOFFCOL + 1;
    public static final int LOCKOPRCOL = OPSEDITCOL + 1;
    public static final int LOCKDECCOL = LOCKOPRCOL + 1;
    public static final int STRAIGHTCOL = LOCKDECCOL + 1;
    public static final int DIVERGCOL = STRAIGHTCOL + 1;
    public static final int FORGETCOL = DIVERGCOL + 1;
    public static final int QUERYCOL = FORGETCOL + 1;

    private boolean _graphicState;
    private TurnoutManager turnoutManager;


    String closedText;
    String thrownText;
    public String defaultThrownSpeedText;
    public String defaultClosedSpeedText;
    // I18N TODO but note storing in xml independent from Locale
    String useBlockSpeed;
    String bothText = "Both";
    String cabOnlyText = "Cab only";
    String pushbutText = "Pushbutton only";
    String noneText = "None";

    public final java.util.Vector<String> speedListClosed = new java.util.Vector<>();
    public final java.util.Vector<String> speedListThrown = new java.util.Vector<>();


    public TurnoutTableDataModel(){
        super();
        initTable();
    }

    public TurnoutTableDataModel(Manager<Turnout> mgr){
        super();
        setManager(mgr);
        initTable();
    }

    private void initTable() {

        // load graphic state column display preference
        _graphicState = InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).isGraphicTableState();

        closedText = turnoutManager.getClosedText();
        thrownText = turnoutManager.getThrownText();

        //This following must contain the word Global for a correct match in the abstract turnout
        defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnoutManager.getDefaultThrownSpeed());
        defaultClosedSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnoutManager.getDefaultClosedSpeed());

        //This following must contain the word Block for a correct match in the abstract turnout
        useBlockSpeed = Bundle.getMessage("UseGlobal", "Block Speed");

        speedListClosed.add(defaultClosedSpeedText);
        speedListThrown.add(defaultThrownSpeedText);
        speedListClosed.add(useBlockSpeed);
        speedListThrown.add(useBlockSpeed);
        java.util.Vector<String> _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (String s : _speedMap) {
            if (!speedListClosed.contains(s)) {
                speedListClosed.add(s);
            }
            if (!speedListThrown.contains(s)) {
                speedListThrown.add(s);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return QUERYCOL + getPropertyColumnCount() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case INVERTCOL:
                return Bundle.getMessage("Inverted");
            case LOCKCOL:
                return Bundle.getMessage("Locked");
            case KNOWNCOL:
                return Bundle.getMessage("Feedback");
            case MODECOL:
                return Bundle.getMessage("ModeLabel");
            case SENSOR1COL:
                return Bundle.getMessage("BlockSensor") + " 1";
            case SENSOR2COL:
                return Bundle.getMessage("BlockSensor") + " 2";
            case OPSONOFFCOL:
                return Bundle.getMessage("TurnoutAutomationMenu");
            case OPSEDITCOL:
                return "";
            case LOCKOPRCOL:
                return Bundle.getMessage("LockMode");
            case LOCKDECCOL:
                return Bundle.getMessage("Decoder");
            case DIVERGCOL:
                return Bundle.getMessage("ThrownSpeed");
            case STRAIGHTCOL:
                return Bundle.getMessage("ClosedSpeed");
            case FORGETCOL:
                return Bundle.getMessage("StateForgetHeader");
            case QUERYCOL:
                return Bundle.getMessage("StateQueryHeader");
            case EDITCOL:
                return "";
            default:
                return super.getColumnName(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTooltip(int col) {
        switch (col) {
            case SENSOR1COL:
                return Bundle.getMessage("Sensor1Tip", turnoutManager.getThrownText());
            case SENSOR2COL:
                return Bundle.getMessage("Sensor2Tip", turnoutManager.getClosedText());
            case OPSONOFFCOL:
                return Bundle.getMessage("TurnoutAutomationTip");
            case KNOWNCOL:
                return Bundle.getMessage("FeedbackTip");
            case MODECOL:
                return Bundle.getMessage("FeedbackModeTip");
            default:
                return super.getHeaderTooltip(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case INVERTCOL:
            case LOCKCOL:
                return Boolean.class;
            case KNOWNCOL:
                return String.class;
            case MODECOL:
            case SENSOR1COL:
            case SENSOR2COL:
            case OPSONOFFCOL:
            case LOCKOPRCOL:
            case LOCKDECCOL:
            case DIVERGCOL:
            case STRAIGHTCOL:
                return JComboBox.class;
            case OPSEDITCOL:
            case EDITCOL:
            case FORGETCOL:
            case QUERYCOL:
                return JButton.class;
            case VALUECOL: // may use an image to show turnout state
                return ( _graphicState ? JLabel.class : JButton.class );
            default:
                return super.getColumnClass(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case INVERTCOL:
            case LOCKCOL:
                return new JTextField(6).getPreferredSize().width;
            case LOCKOPRCOL:
            case LOCKDECCOL:
            case KNOWNCOL:
            case MODECOL:
                return new JTextField(10).getPreferredSize().width;
            case SENSOR1COL:
            case SENSOR2COL:
                return new JTextField(5).getPreferredSize().width;
            case OPSEDITCOL:
                return new JButton(Bundle.getMessage("EditTurnoutOperation")).getPreferredSize().width;
            case EDITCOL:
                return new JButton(Bundle.getMessage("ButtonEdit")).getPreferredSize().width+4;
            case OPSONOFFCOL:
                return new JTextField(Bundle.getMessage("TurnoutAutomationMenu")).getPreferredSize().width;
            case DIVERGCOL:
            case STRAIGHTCOL:
                return new JTextField(14).getPreferredSize().width;
            case FORGETCOL:
                return new JButton(Bundle.getMessage("StateForgetButton")).getPreferredSize().width;
            case QUERYCOL:
                return new JButton(Bundle.getMessage("StateQueryButton")).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        Turnout t = turnoutManager.getBySystemName(sysNameList.get(row));
        if (t == null){
            return false;
        }
        switch (col) {
            case INVERTCOL:
                return t.canInvert();
            case LOCKCOL:
                // checkbox disabled unless current configuration allows locking
                return t.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT);
            case OPSEDITCOL:
                return t.getTurnoutOperation() != null;
            case KNOWNCOL:
                return false;
            case MODECOL:
            case SENSOR1COL:
            case SENSOR2COL:
            case OPSONOFFCOL:
            case LOCKOPRCOL: // editable always so user can configure it, even if current configuration prevents locking now
            case LOCKDECCOL: // editable always so user can configure it, even if current configuration prevents locking now
            case DIVERGCOL:
            case STRAIGHTCOL:
            case EDITCOL:
            case FORGETCOL:
            case QUERYCOL:
                return true;
            default:
                return super.isCellEditable(row, col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()) {
            log.warn("row is greater than name list");
            return "error";
        }
        String name = sysNameList.get(row);
        TurnoutManager manager = turnoutManager;
        Turnout t = manager.getBySystemName(name);
        if (t == null) {
            log.debug("error null turnout!");
            return "error";
        }
        if (col == INVERTCOL) {
            return t.getInverted();
        } else if (col == LOCKCOL) {
            return t.getLocked(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT);
        } else if (col == KNOWNCOL) {
            return t.describeState(t.getKnownState());
        } else if (col == MODECOL) {
            JComboBox<String> c = new JComboBox<>(t.getValidFeedbackNames());
            c.setSelectedItem(t.getFeedbackModeName());
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
            if ((t.getPossibleLockModes() & Turnout.PUSHBUTTONLOCKOUT) != 0) {
                c = new JComboBox<>(t.getValidDecoderNames());
            } else {
                c = new JComboBox<>(new String[]{t.getDecoderName()});
            }

            c.setSelectedItem(t.getDecoderName());
            return c;
        } else if (col == LOCKOPRCOL) {

            java.util.Vector<String> lockOperations = new java.util.Vector<>();  // Vector is a JComboBox ctor; List is not
            int modes = t.getPossibleLockModes();
            if ((modes & Turnout.CABLOCKOUT) != 0 && (modes & Turnout.PUSHBUTTONLOCKOUT) != 0) {
                lockOperations.add(bothText);
            }
            if ((modes & Turnout.CABLOCKOUT) != 0) {
                lockOperations.add(cabOnlyText);
            }
            if ((modes & Turnout.PUSHBUTTONLOCKOUT) != 0) {
                lockOperations.add(pushbutText);
            }
            lockOperations.add(noneText);
            JComboBox<String> c = new JComboBox<>(lockOperations);

            if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                c.setSelectedItem(bothText);
            } else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                c.setSelectedItem(pushbutText);
            } else if (t.canLock(Turnout.CABLOCKOUT)) {
                c.setSelectedItem(cabOnlyText);
            } else {
                c.setSelectedItem(noneText);
            }
            return c;
        } else if (col == STRAIGHTCOL) {

            String speed = t.getStraightSpeed();
            if (!speedListClosed.contains(speed)) {
                speedListClosed.add(speed);
            }
            JComboBox<String> c = new JComboBox<>(speedListClosed);
            c.setEditable(true);
            c.setSelectedItem(speed);
            JComboBoxUtil.setupComboBoxMaxRows(c);
            return c;
        } else if (col == DIVERGCOL) {

            String speed = t.getDivergingSpeed();
            if (!speedListThrown.contains(speed)) {
                speedListThrown.add(speed);
            }
            JComboBox<String> c = new JComboBox<>(speedListThrown);
            c.setEditable(true);
            c.setSelectedItem(speed);
            JComboBoxUtil.setupComboBoxMaxRows(c);
            return c;
            // } else if (col == VALUECOL && _graphicState) { // not neeeded as the
            //  graphic ImageIconRenderer uses the same super.getValueAt(row, col) as
            // classic bean state text button
        } else if (col == FORGETCOL) {
            return Bundle.getMessage("StateForgetButton");
        } else if (col == QUERYCOL) {
            return Bundle.getMessage("StateQueryButton");
        }
        return super.getValueAt(row, col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        String name = sysNameList.get(row);
        Turnout t = turnoutManager.getBySystemName(name);
        if (t == null) {
            NullPointerException ex = new NullPointerException("Unexpected null turnout in turnout table");
            log.error("No Turnout with system name \"{}\" exists ", name , ex); // log with stack trace
            throw ex;
        }
        if (col == INVERTCOL) {
            if (t.canInvert()) {
                t.setInverted((Boolean) value);
            }
        } else if (col == LOCKCOL) {
            t.setLocked(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT, (Boolean) value);
        } else if (col == MODECOL) {
            @SuppressWarnings("unchecked")
            String modeName = (String) ((JComboBox<String>) value).getSelectedItem();
            assert modeName != null;
            t.setFeedbackMode(modeName);
        } else if (col == SENSOR1COL) {
            try {
                Sensor sensor = (Sensor) value;
                t.provideFirstFeedbackSensor(sensor != null ? sensor.getDisplayName() : null);
            } catch (jmri.JmriException e) {
                JmriJOptionPane.showMessageDialog(null, e.toString());
            }
        } else if (col == SENSOR2COL) {
            try {
                Sensor sensor = (Sensor) value;
                t.provideSecondFeedbackSensor(sensor != null ? sensor.getDisplayName() : null);
            } catch (jmri.JmriException e) {
                JmriJOptionPane.showMessageDialog(null, e.toString());
            }
        } else if (col == OPSONOFFCOL) {
            // do nothing as this is handled by the combo box listener
            // column still handled here to prevent call to super.setValueAt
        } else if (col == OPSEDITCOL) {
            t.setInhibitOperation(false);
            @SuppressWarnings("unchecked") // cast to JComboBox<String> required in OPSEDITCOL
            JComboBox<String> cb = (JComboBox<String>) getValueAt(row, OPSONOFFCOL);
            log.debug("opsSelected = {}", getValueAt(row, OPSONOFFCOL).toString());
            editTurnoutOperation(t, cb);
            fireTableRowsUpdated(row, row);
        } else if (col == EDITCOL) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                editButton(t);
            });
        } else if (col == LOCKOPRCOL) {
            @SuppressWarnings("unchecked")
            String lockOpName = (String) ((JComboBox<String>) value)
                    .getSelectedItem();
            assert lockOpName != null;
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
            fireTableRowsUpdated(row, row);
        } else if (col == LOCKDECCOL) {
            @SuppressWarnings("unchecked")
            String decoderName = (String) ((JComboBox<String>) value).getSelectedItem();
            t.setDecoderName(decoderName);
            fireTableRowsUpdated(row, row);
        } else if (col == STRAIGHTCOL) {
            @SuppressWarnings("unchecked")
            String speed = (String) ((JComboBox<String>) value).getSelectedItem();
            try {
                t.setStraightSpeed(speed);
            } catch (jmri.JmriException ex) {
                JmriJOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                return;
            }
            if ((!speedListClosed.contains(speed))) {
                assert speed != null;
                if (!speed.contains("Global")) {
                    speedListClosed.add(speed);
                }
            }
        } else if (col == DIVERGCOL) {

            @SuppressWarnings("unchecked")
            String speed = (String) ((JComboBox<String>) value).getSelectedItem();
            try {
                t.setDivergingSpeed(speed);
            } catch (jmri.JmriException ex) {
                JmriJOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                return;
            }
            if ((!speedListThrown.contains(speed))) {
                assert speed != null;
                if (!speed.contains("Global")) {
                    speedListThrown.add(speed);
                }
            }
        } else if (col == FORGETCOL) {
            t.setCommandedState(Turnout.UNKNOWN);
        } else if (col == QUERYCOL) {
            t.setCommandedState(Turnout.UNKNOWN);
            t.requestUpdateFromLayout();
        } else if (col == VALUECOL && _graphicState) { // respond to clicking on ImageIconRenderer CellEditor
            clickOn(t);
            fireTableRowsUpdated(row, row);
        } else {
            super.setValueAt(value, row, col);
            if (row < getRowCount()) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(@Nonnull String name) {
        Turnout turn = turnoutManager.getBySystemName(name);
        if (turn != null) {
            return turn.describeState(turn.getCommandedState());
        }
        return "Turnout not found";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Manager<Turnout> getManager() {
        if (turnoutManager == null) {
            turnoutManager = InstanceManager.getDefault(TurnoutManager.class);
        }
        return turnoutManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void setManager(@Nonnull Manager<Turnout> manager) {
        if (!(manager instanceof TurnoutManager)) {
            return;
        }
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
        turnoutManager = (TurnoutManager) manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    @Override
    public Turnout getBySystemName(@Nonnull String name) {
        return turnoutManager.getBySystemName(name);
    }

    @Override
    public Turnout getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(TurnoutManager.class).getByUserName(name);
    }

    @Override
    protected String getMasterClassName() {
        // Force message grouping
        return jmri.jmrit.beantable.TurnoutTableAction.class.getName();
    }

    protected String getClassName() {
        return jmri.jmrit.beantable.TurnoutTableAction.class.getName();
    }

    @Override
    public void clickOn(Turnout t) {
        t.setCommandedState( t.getCommandedState()== Turnout.CLOSED ? Turnout.THROWN : Turnout.CLOSED);
    }

    @Override
    public void configureTable(JTable tbl) {

        setColumnToHoldButton(tbl, EDITCOL, editButton());
        setColumnToHoldButton(tbl, OPSEDITCOL, editButton());

        //Hide the following columns by default
        XTableColumnModel columnModel = (XTableColumnModel) tbl.getColumnModel();
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
        column = columnModel.getColumnByModelIndex(FORGETCOL);
        columnModel.setColumnVisible(column, false);
        column = columnModel.getColumnByModelIndex(QUERYCOL);
        columnModel.setColumnVisible(column, false);


        // and then set user prefs
        super.configureTable(tbl);

        if (_graphicState) {
            // make the rows tall enough for the state icons, once and outside of the
            // renderer; must come after super.configureTable, which sets the row
            // height for the buttons
            tbl.setRowHeight(Math.max(tbl.getRowHeight(), ImageIconRenderer.getIconRowHeight()));
        }

        columnModel.getColumnByModelIndex(FORGETCOL).setHeaderValue(null);
        columnModel.getColumnByModelIndex(QUERYCOL).setHeaderValue(null);

    }

    // update table if turnout lock or feedback changes
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case Turnout.PROPERTY_LOCKED:
            case Turnout.PROPERTY_INVERTED:
            case Turnout.PROPERTY_FEEDBACK_MODE: // feedback type setting change, NOT Turnout feedback status
            case Turnout.PROPERTY_TURNOUT_DIVERGING_SPEED:
            case Turnout.PROPERTY_TURNOUT_STRAIGHT_SPEED:
            case Turnout.PROPERTY_TURNOUT_FEEDBACK_FIRST_SENSOR:
            case Turnout.PROPERTY_TURNOUT_FEEDBACK_SECOND_SENSOR:
            case Turnout.PROPERTY_DECODER_NAME:
            case Turnout.PROPERTY_TURNOUT_OPERATION_STATE:
            case Turnout.PROPERTY_KNOWN_STATE:
            case Turnout.PROPERTY_COMMANDED_STATE:
                return true;
            default:
                return super.matchPropertyName(e);
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case TurnoutManager.PROPERTY_DEFAULT_CLOSED_SPEED:
                updateClosedList();
                break;
            case TurnoutManager.PROPERTY_DEFAULT_THROWN_SPEED:
                updateThrownList();
                break;
            default:
                super.propertyChange(e);
                break;
        }
    }

    /**
     * Customize the turnout table Value (State) column to show an
     * appropriate graphic for the turnout state if _graphicState =
     * true, or (default) just show the localized state text when the
     * TableDataModel is being called from ListedTableAction.
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
            table.setDefaultEditor(JLabel.class, new ImageIconRenderer(closedText, thrownText)); // editor
            table.setDefaultRenderer(JLabel.class, new ImageIconRenderer(closedText, thrownText)); // item class copied from SwitchboardEditor panel
        } else {
            super.configValueColumn(table); // classic text style state indication
        }
    }

    @Override
    public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @CheckForNull RowSorter<? extends TableModel> sorter) {
        if (!(model instanceof TurnoutTableDataModel)){
            throw new IllegalArgumentException("Model is not a TurnoutTableDataModel");
        }
        return configureJTable(name, new TurnoutTableJTable((TurnoutTableDataModel)model), sorter);
    }

    @Override
    protected void setColumnIdentities(JTable table) {
        super.setColumnIdentities(table);
        java.util.Enumeration<TableColumn> columns;
        if (table.getColumnModel() instanceof XTableColumnModel) {
            columns = ((XTableColumnModel) table.getColumnModel()).getColumns(false);
        } else {
            columns = table.getColumnModel().getColumns();
        }
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            switch (column.getModelIndex()) {
                case FORGETCOL:
                    column.setIdentifier("ForgetState");
                    break;
                case QUERYCOL:
                    column.setIdentifier("QueryState");
                    break;
                case SENSOR1COL:
                    column.setIdentifier("Sensor1");
                    break;
                case SENSOR2COL:
                    column.setIdentifier("Sensor2");
                    break;
                default:
                // use existing value
            }
        }
    }

    /**
     * Pop up a TurnoutOperationConfig for the turnout.
     *
     * @param t   turnout
     * @param box JComboBox that triggered the edit
     */
    protected void editTurnoutOperation( @Nonnull Turnout t, JComboBox<String> box) {
        if (!editingOps.getAndSet(true)) { // don't open a second edit ops pane
            TurnoutOperation op = t.getTurnoutOperation();
            if (op == null) {
                TurnoutOperation proto = InstanceManager.getDefault(TurnoutOperationManager.class).getMatchingOperationAlways(t);
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
                java.awt.Window w = JmriJOptionPane.findWindowForObject(box);
                TurnoutOperationEditorDialog dialog = new TurnoutOperationEditorDialog(op, t, w);
                dialog.setVisible(true);
            } else {
                JmriJOptionPane.showMessageDialog(box, Bundle.getMessage("TurnoutOperationErrorDialog"),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Create a {@literal JComboBox<String>} containing all the options for
     * turnout automation parameters for this turnout.
     *
     * @param t the turnout
     * @return the JComboBox
     */
    protected JComboBox<String> makeAutomationBox(Turnout t) {
        String[] str = new String[]{"empty"};
        final JComboBox<String> cb = new JComboBox<>(str);
        final Turnout myTurnout = t;
        TurnoutTableAction.updateAutomationBox(t, cb);
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTurnoutOperation(myTurnout, cb);
                cb.removeActionListener(this);  // avoid recursion
                TurnoutTableAction.updateAutomationBox(myTurnout, cb);
                cb.addActionListener(this);
            }
        });
        return cb;
    }

    /**
     * Set the turnout's operation info based on the contents of the combo box.
     *
     * @param t  turnout being configured
     * @param cb JComboBox for ops for t in the TurnoutTable
     */
    protected void setTurnoutOperation( @Nonnull Turnout t, JComboBox<String> cb) {
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
                t.setTurnoutOperation(InstanceManager.getDefault(TurnoutOperationManager.class).
                        getOperation(((String) java.util.Objects.requireNonNull(cb.getSelectedItem()))));
                break;
        }
    }

    /**
     * Create action to edit a turnout in Edit pane. (also used in windowTest)
     *
     * @param t the turnout to be edited
     */
    void editButton(Turnout t) {
        jmri.jmrit.beantable.beanedit.TurnoutEditAction beanEdit = new jmri.jmrit.beantable.beanedit.TurnoutEditAction();
        beanEdit.setBean(t);
        beanEdit.actionPerformed(null);
    }

    /**
     * Create a JButton to edit a turnout's operation.
     *
     * @return the JButton
     */
    protected JButton editButton() {
        return new JButton(Bundle.getMessage("EditTurnoutOperation"));
    }

    private void updateClosedList() {
        speedListClosed.remove(defaultClosedSpeedText);
        defaultClosedSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnoutManager.getDefaultClosedSpeed());
        speedListClosed.add(0, defaultClosedSpeedText);
        fireTableDataChanged();
    }

    private void updateThrownList() {
        speedListThrown.remove(defaultThrownSpeedText);
        defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + turnoutManager.getDefaultThrownSpeed());
        speedListThrown.add(0, defaultThrownSpeedText);
        fireTableDataChanged();
    }

    public void showFeedbackChanged(boolean visible, JTable table ) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(KNOWNCOL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(MODECOL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(SENSOR1COL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(SENSOR2COL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(OPSONOFFCOL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(OPSEDITCOL);
        columnModel.setColumnVisible(column, visible);
    }

    public void showLockChanged(boolean visible, JTable table) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = ((XTableColumnModel) table.getColumnModel()).getColumnByModelIndex(LOCKDECCOL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(LOCKOPRCOL);
        columnModel.setColumnVisible(column, visible);
    }

    public void showTurnoutSpeedChanged(boolean visible, JTable table) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = ((XTableColumnModel) table.getColumnModel()).getColumnByModelIndex(STRAIGHTCOL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(DIVERGCOL);
        columnModel.setColumnVisible(column, visible);
    }

    public void showStateForgetAndQueryChanged(boolean visible, JTable table) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(FORGETCOL);
        columnModel.setColumnVisible(column, visible);
        column = columnModel.getColumnByModelIndex(QUERYCOL);
        columnModel.setColumnVisible(column, visible);
    }


    /**
     * Visualize state in table as a graphic, customized for Turnouts (4
     * states).
     * Renderer and Editor are identical, as the cell contents
     * are not actually edited, only used to toggle state using
     * {@link #clickOn(Turnout)}.
     * <p>
     * A single label is reused for every cell and the icons are loaded once
     * per JVM, so rendering a cell allocates nothing. The label ignores
     * revalidate/repaint requests and the row height is set once in
     * {@link #configureTable}: painting a cell must never schedule more
     * painting, or the table repaints itself forever.
     */
    static class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

        private static final String ROOT_PATH = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
        private static final char BEAN_TYPE_CHAR = 'T'; // for Turnout
        private static final String ON_ICON_PATH = ROOT_PATH + BEAN_TYPE_CHAR + "-on-s.png";
        private static final String OFF_ICON_PATH = ROOT_PATH + BEAN_TYPE_CHAR + "-off-s.png";
        private static ImageIcon onIcon = null;
        private static ImageIcon offIcon = null;
        private static int iconHeight = -1;

        private final StateLabel label = new StateLabel();
        private final Color defaultForeground = label.getForeground();
        private final String closedText;
        private final String thrownText;
        private final String unknownText = Bundle.getMessage("BeanStateUnknown");
        private final String inconsistentText = Bundle.getMessage("BeanStateInconsistent");
        private int row = -1; // row of the cell last rendered or edited

        ImageIconRenderer(String closedText, String thrownText) {
            this.closedText = closedText;
            this.thrownText = thrownText;
            label.setHorizontalAlignment(JLabel.CENTER);
            // must stay the only anonymous class in ImageIconRenderer, see jmri.ArchitectureTest
            label.addMouseListener(new MouseAdapter() {
                @Override
                public final void mousePressed(MouseEvent evt) {
                    log.debug("Clicked on icon in row {}", row);
                    stopCellEditing();
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            log.debug("Renderer Item = {}, State = {}", row, value);
            return updateLabel((String) value, row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            log.debug("Editor Item = {}, State = {}", row, value);
            return updateLabel((String) value, row);
        }

        protected JLabel updateLabel(String value, int row) {
            this.row = row;
            if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                loadIcons();
                log.debug("icons loaded");
            }
            label.setForeground(defaultForeground); // reset a foreground left over from an INCONSISTENT cell
            if (value.equals(closedText) && onIcon != null) {
                label.setIcon(onIcon);
                label.setText(null);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("onIcon set");
            } else if (value.equals(thrownText) && offIcon != null) {
                label.setIcon(offIcon);
                label.setText(null);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("offIcon set");
            } else if (value.equals(inconsistentText)) {
                label.setIcon(null);
                label.setText("X");
                label.setForeground(Color.red);
                label.setVerticalAlignment(JLabel.CENTER);
                log.debug("Turnout state inconsistent");
            } else if (value.equals(unknownText)) {
                label.setIcon(null);
                label.setText("?");
                label.setVerticalAlignment(JLabel.CENTER);
                log.debug("Turnout state unknown");
            } else { // failed to load icon
                label.setIcon(null);
                label.setText(value);
                label.setVerticalAlignment(JLabel.CENTER);
                log.warn("Error reading icons for TurnoutTable");
            }
            label.setToolTipText(value);
            return label;
        }

        @Override
        public Object getCellEditorValue() {
            log.debug("getCellEditorValue, me = {})", this.toString());
            return this.toString();
        }

        /**
         * Get the row height needed to fit the state icons, loading them if
         * required. Used by {@link #configureTable} to size the rows once,
         * outside of the render path.
         *
         * @return required row height in pixels, 0 if the icons could not be read
         */
        static int getIconRowHeight() {
            if (iconHeight < 0) {
                loadIcons();
            }
            return Math.max(iconHeight - 5, 0);
        }

        /**
         * Read and buffer graphics. Only called once per JVM, the icons are
         * shared by all instances of this renderer.
         */
        private static synchronized void loadIcons() {
            if (iconHeight >= 0) { // another instance already loaded the icons
                return;
            }
            BufferedImage onImage = null;
            BufferedImage offImage = null;
            try {
                onImage = ImageIO.read(new File(ON_ICON_PATH));
                offImage = ImageIO.read(new File(OFF_ICON_PATH));
            } catch (IOException ex) {
                log.error("error reading image from {} or {}", ON_ICON_PATH, OFF_ICON_PATH, ex);
            }
            if (onImage == null || offImage == null) { // ImageIO.read returns null for an unreadable file
                log.error("error reading image from {} or {}", ON_ICON_PATH, OFF_ICON_PATH);
                iconHeight = 0; // give up: render states as text, don't retry for every cell
                return;
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

        /**
         * Label that ignores revalidate and repaint requests.
         * <p>
         * The rendered component stays a child of the table's
         * CellRendererPane after painting, so a plain JLabel schedules a new
         * repaint of the whole table each time its icon or text changes
         * during a paint cycle. Same overrides as DefaultTableCellRenderer,
         * except firePropertyChange, which BasicLabelUI needs to keep its
         * view up to date.
         */
        private static class StateLabel extends JLabel {

            @Override
            public void revalidate() {
                // ignored, see class comment
            }

            @Override
            public void repaint() {
                // ignored, see class comment
            }

            @Override
            public void repaint(long tm) {
                // ignored, see class comment
            }

            @Override
            public void repaint(int x, int y, int width, int height) {
                // ignored, see class comment
            }

            @Override
            public void repaint(long tm, int x, int y, int width, int height) {
                // ignored, see class comment
            }

            @Override
            public void repaint(Rectangle r) {
                // ignored, see class comment
            }
        }

    } // end of ImageIconRenderer class

    protected static java.util.concurrent.atomic.AtomicBoolean editingOps = new java.util.concurrent.atomic.AtomicBoolean(false);

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutTableDataModel.class);

}
