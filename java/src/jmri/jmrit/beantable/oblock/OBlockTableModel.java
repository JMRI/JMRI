package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.RowComboBoxPanel;
import jmri.jmrit.beantable.block.BlockCurvatureJComboBox;
import jmri.jmrit.logix.*;
import jmri.util.IntlUtilities;
import jmri.util.NamedBeanComparator;

import jmri.util.gui.GuiLafPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks.
 * <p>
 * Duplicates the JTable model for BlockTableAction and adds a column for the
 * occupancy sensor. Configured for use within an internal frame.
 * <p>
 * Can be used with two interfaces:
 * <ul>
 *     <li>original "desktop" InternalFrames (parent class TableFrames, an extended JmriJFrame)
 *     <li>JMRI "standard" Tabbed tables (parent class JPanel)
 * </ul>
 * The _tabbed field decides, it is set in prefs (restart required).
 *
 * @author Pete Cressman (C) 2010
 * @author Egbert Broerse (C) 2020
 */
public class OBlockTableModel extends jmri.jmrit.beantable.BeanTableDataModel<OBlock> {

    static public final int SYSNAMECOL = 0;
    static public final int USERNAMECOL = 1;
    static public final int COMMENTCOL = 2;
    static public final int STATECOL = 3;
    static public final int SENSORCOL = 4;
    static public final int EDIT_COL = 5;   // Paths button
    static public final int DELETE_COL = 6;
    static public final int LENGTHCOL = 7;
    static public final int UNITSCOL = 8;
    static public final int REPORTERCOL = 9;
    static public final int REPORT_CURRENTCOL = 10;
    static public final int PERMISSIONCOL = 11;
    static public final int SPEEDCOL = 12;
    static public final int WARRANTCOL = 13;
    static public final int ERR_SENSORCOL = 14;
    static public final int CURVECOL = 15;
    static public final int NUMCOLS = 16;

    static String ZEROS = "000000000";      // 9 bits contain the OBlock state info

    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    OBlockManager _manager;
    private final String[] tempRow = new String[NUMCOLS];
    private float _tempLen = 0.0f;      // mm for length col of tempRow
    TableFrames _parent;
    private final boolean _tabbed; // updated from prefs (restart required)

    public OBlockTableModel(@Nonnull TableFrames parent) {
        super();
        _parent = parent;
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        if (_tabbed) {
            _manager = InstanceManager.getDefault(OBlockManager.class); // TODO also for _desktop?
            _manager.addPropertyChangeListener(this);
        }
        updateNameList();
        if (!_tabbed) {
            initTempRow();
        }
    }

    /**
     * Respond to mouse events to show/hide columns.
     * Has public access to allow setting from OBlockTableAction OBlock Panel.
     *
     * @param table the table based on this model
     */
    public void addHeaderListener(JTable table) {
        addMouseListenerToHeader(table);
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[LENGTHCOL] = twoDigit.format(0.0);
        tempRow[UNITSCOL] = Bundle.getMessage("in");
        tempRow[CURVECOL] = BlockCurvatureJComboBox.getStringFromCurvature(Block.NONE);
        tempRow[REPORT_CURRENTCOL] = Bundle.getMessage("Current");
        tempRow[PERMISSIONCOL] = Bundle.getMessage("Permissive");
        tempRow[SPEEDCOL] = "";
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    @Override
    public Manager<OBlock> getManager() {
        _manager = InstanceManager.getDefault(OBlockManager.class);
        return _manager;
    }

    @Override
    public OBlock getBySystemName(@Nonnull String name) {
        return _manager.getBySystemName(name);
    }

    @Override
    public OBlock getByUserName(@Nonnull String name) {
        return _manager.getByUserName(name);
    }

    @Override
    protected String getBeanType() {
        return "OBlock";
    }

    @Override
    public void clickOn(OBlock t) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMasterClassName() {
        return getClassName();
    }

    protected List<OBlock> getBeanList() {
        TreeSet<OBlock> ts = new TreeSet<>(new NamedBeanComparator<>());

        for (String s : sysNameList) {
            ts.add(getBySystemName(s));
        }
        ArrayList<OBlock> list = new ArrayList<>(sysNameList.size());

        list.addAll(ts);
        return list;
    }

    @Override
    public String getValue(String name) {
        OBlock bl = _manager.getBySystemName(name);
        if (bl !=null) {
            return getValue(bl.getState());
        }
        return "";
    }

    static protected String getValue(int state) {
        StringBuilder sb = new StringBuilder();
        if ((state & OBlock.UNDETECTED) != 0) {
            sb.append(Bundle.getMessage("Dark"));
        }
        if ((state & OBlock.OCCUPIED) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("Occupied"));
        }
        if ((state & OBlock.UNOCCUPIED) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("Unoccupied"));
        }
        if ((state & OBlock.INCONSISTENT) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("BeanStateInconsistent"));
        }
        if ((state & OBlock.UNKNOWN) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("BeanStateUnknown"));
        }
        if ((state & OBlock.ALLOCATED) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("Allocated"));
        }
        if ((state & OBlock.RUNNING) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("Running"));
        }
        if ((state & OBlock.OUT_OF_SERVICE) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("OutOfService"));
        }
        if ((state & OBlock.TRACK_ERROR) != 0) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(Bundle.getMessage("TrackError"));
        }
        if (sb.length() == 0) {
            sb.append(Bundle.getMessage("UnDefined"));
        }
        return sb.toString();
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public int getRowCount() {
        return super.getRowCount() + (_tabbed ? 0 : 1); // + 1 row in _desktop to create entry row
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row > sysNameList.size()) {
            return "";
        }
        OBlock b = null;
        if ((_tabbed && row <= sysNameList.size()) || (!_tabbed && row < sysNameList.size())) {
            String name = sysNameList.get(row);
            b = _manager.getBySystemName(name);
        }
        switch (col) {
            case SYSNAMECOL:
                if (b != null) {
                    return b.getSystemName();
                }
                return tempRow[col]; // this must be tempRow
            case USERNAMECOL:
                if (b != null) {
                    return b.getUserName();
                }
                return tempRow[col];
            case COMMENTCOL:
                if (b != null) {
                    return b.getComment();
                }
                return tempRow[col];
            case STATECOL:
                if (b != null) {
                    int state = b.getState();
                    int num = Integer.numberOfLeadingZeros(state) - 23;
                    if (num >= 0) {
                        return ZEROS.substring(0, num) + Integer.toBinaryString(state);                        
                    }
                }
                return ZEROS;
            case SENSORCOL:
                if (b != null) {
                    Sensor s = b.getSensor();
                    if (s == null) {
                        return "";
                    }
                    return s.getDisplayName();
                }
                return tempRow[col];
            case LENGTHCOL:
                if (b != null) {
                    if (b.isMetric()) {
                        return (twoDigit.format(b.getLengthCm()));
                    }
                    return (twoDigit.format(b.getLengthIn()));
                }
                if (tempRow[UNITSCOL].equals(Bundle.getMessage("cm"))) {
                    return (twoDigit.format(_tempLen/10));
                }
                return (twoDigit.format(_tempLen/25.4f));
            case UNITSCOL:
                if (b != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("getValueAt: row= {}, col= {}, \"{}\" isMetric= {}", row, col, b.getDisplayName(), b.isMetric());
                    }
                    return b.isMetric();
                }
                if (log.isDebugEnabled()) {
                    log.debug("getValueAt: row= {}, col= {}, isMetric= {}", row, col, tempRow[UNITSCOL].equals(Bundle.getMessage("cm")));
                }
                return tempRow[UNITSCOL].equals(Bundle.getMessage("cm"));
            case CURVECOL:
                if (b != null) {
                    return BlockCurvatureJComboBox.getStringFromCurvature(b.getCurvature());
                }
                return tempRow[col];
            case ERR_SENSORCOL:
                if (b != null) {
                    Sensor s = b.getErrorSensor();
                    if (s == null) {
                        return "";
                    }
                    return s.getDisplayName();
                }
                return tempRow[col];
            case REPORTERCOL:
                if (b != null) {
                    Reporter r = b.getReporter();
                    if (r == null) {
                        return "";
                    }
                    return r.getDisplayName();
                }
                return tempRow[col];
            case REPORT_CURRENTCOL:
                if (b != null) {
                    if (b.getReporter() != null) {
                        return b.isReportingCurrent();
                    }
                    return "";
                }
                return tempRow[REPORT_CURRENTCOL].equals(Bundle.getMessage("Current"));
            case PERMISSIONCOL:
                if (b != null) {
                    return b.getPermissiveWorking();
                }
                return tempRow[PERMISSIONCOL].equals(Bundle.getMessage("Permissive"));
            case SPEEDCOL:
                if (b != null) {
                    return b.getBlockSpeed();
                }
                return tempRow[col];
            case WARRANTCOL:
                if (b != null) {
                    Warrant w = b.getWarrant();
                    if (w != null) {
                        return w.getDisplayName();                        
                    }
                }
                return tempRow[col];
            case EDIT_COL:
                if (b != null) {
                    if (_tabbed) {
                        return Bundle.getMessage("ButtonEdit");
                    } else {
                        return Bundle.getMessage("ButtonEditPath");
                    }
                }
                return "";
            case DELETE_COL:
                if (b != null) {
                    return Bundle.getMessage("ButtonDelete");
                }
                return Bundle.getMessage("ButtonClear");
            default:
                // fall through
                break;
        }
        return super.getValueAt(row, col);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        log.debug("setValueAt: row= {}, col= {}, value= {}", row, col, value);
        if (!_tabbed && (super.getRowCount() == row)) { // editing tempRow
            switch (col) {
                case SYSNAMECOL:
                    OBlock block = _manager.createNewOBlock((String) value, tempRow[USERNAMECOL]);
                    if (block == null) { // an OBlock with the same systemName or userName already exists
                        block = _manager.getOBlock(tempRow[USERNAMECOL]);
                        String name = value + " / " + tempRow[USERNAMECOL];
                        if (block != null) {
                            name = block.getDisplayName();
                        } else {
                            block = _manager.getOBlock((String)value);
                            if (block != null) {
                                name = block.getDisplayName();
                            }
                        }
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("CreateDuplBlockErr", name),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (tempRow[SENSORCOL] != null) {
                        if (!sensorExists(tempRow[SENSORCOL])) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchSensorErr", tempRow[SENSORCOL]),
                                    Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    block.setComment(tempRow[COMMENTCOL]);
                    float len = 0.0f;
                    try {
                        len = IntlUtilities.floatValue(tempRow[LENGTHCOL]);
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("BadNumber", tempRow[LENGTHCOL]),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                    
                    }
                    if (tempRow[UNITSCOL].equals(Bundle.getMessage("cm"))) {
                        block.setLength(len * 10.0f);
                        block.setMetricUnits(true);
                    } else {
                        block.setLength(len * 25.4f);
                        block.setMetricUnits(false);
                    }
                    block.setCurvature(BlockCurvatureJComboBox.getCurvatureFromString(tempRow[CURVECOL]));
                    block.setPermissiveWorking(tempRow[PERMISSIONCOL].equals(Bundle.getMessage("Permissive")));
                    block.setBlockSpeedName(tempRow[SPEEDCOL]);
                    
                    if (tempRow[ERR_SENSORCOL] != null) {
                        if (tempRow[ERR_SENSORCOL].trim().length() > 0) {
                            if (!sensorExists(tempRow[ERR_SENSORCOL])) {
                                JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchSensorErr", tempRow[ERR_SENSORCOL]),
                                        Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                    if (tempRow[REPORTERCOL] != null) {
                        Reporter rep = null;
                        try {
                            rep = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(tempRow[REPORTERCOL]);
                            if (rep != null) {
                                block.setReporter(rep);
                                block.setReportingCurrent(tempRow[REPORT_CURRENTCOL].equals(Bundle.getMessage("Current")));
                            }
                        } catch (Exception ex) {
                            log.error("No Reporter named \"{}\" found. threw exception: {}", tempRow[REPORTERCOL], ex);
                        }
                        if (rep == null) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchReporterErr", tempRow[REPORTERCOL]),
                                    Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        }
                        block.setReporter(rep);
                    }
                    initTempRow();
                    fireTableDataChanged();
                    return;
                case DELETE_COL:            // "Clear"
                    initTempRow();
                    fireTableRowsUpdated(row, row);
                    return;
                case LENGTHCOL:
                    try {
                        _tempLen = IntlUtilities.floatValue(value.toString());
                        if (tempRow[UNITSCOL].equals(Bundle.getMessage("cm"))) {
                            _tempLen *= 10f;
                        } else {
                            _tempLen *= 25.4f;                            
                        }
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("BadNumber", tempRow[LENGTHCOL]),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                    
                    }
                    return;
                case UNITSCOL:
                    if (((Boolean) value)) {
                        tempRow[UNITSCOL] = Bundle.getMessage("cm");
                    } else {
                        tempRow[UNITSCOL] = Bundle.getMessage("in");
                    }
                    fireTableRowsUpdated(row, row); // recalculates length value as displayed
                    return;
                case REPORT_CURRENTCOL:
                    if ((Boolean) value) {//toggle
                        tempRow[REPORT_CURRENTCOL] = Bundle.getMessage("Current");
                    } else {
                        tempRow[REPORT_CURRENTCOL] = Bundle.getMessage("Last");
                    }
                    return;
                case PERMISSIONCOL:
                    if ((Boolean) value) {//toggle
                        tempRow[PERMISSIONCOL] = Bundle.getMessage("Permissive");
                    } else {
                        tempRow[PERMISSIONCOL] = Bundle.getMessage("Absolute");
                    }
                    return;
                default:
                    // fall though
                    break;
            }
            tempRow[col] = (String) value;
            return;
        }

        // Edit an existing row
        String name = sysNameList.get(row);
        OBlock block = _manager.getBySystemName(name);
        if (block == null) {
            log.error("OBlock named {} not found for OBlockTableModel", name);
            return;
        }
        switch (col) {
            case USERNAMECOL:
                OBlock b = _manager.getOBlock((String) value);
                if (b != null) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("CreateDuplBlockErr", block.getDisplayName()),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                block.setUserName((String) value);
                fireTableRowsUpdated(row, row);
                return;
            case COMMENTCOL:
                block.setComment((String) value);
                fireTableRowsUpdated(row, row);
                return;
            case STATECOL:
                return;     //  STATECOL is not editable
            case SENSORCOL:
                if (!block.setSensor((String) value)) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchSensorErr", value),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                }
                fireTableRowsUpdated(row, row);
                return;
            case LENGTHCOL:
                try {
                    float len = IntlUtilities.floatValue(value.toString());
                    if (block.isMetric()) {
                        block.setLength(len * 10.0f);
                    } else {
                        block.setLength(len * 25.4f);
                    }
                    fireTableRowsUpdated(row, row);                    
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("BadNumber", value),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                    
                }
                return;
            case UNITSCOL:
                block.setMetricUnits((Boolean) value);
                fireTableRowsUpdated(row, row);
                return;
            case CURVECOL:
                String cName = (String) value;
                if (cName == null) {
                    return;
                }
                block.setCurvature(BlockCurvatureJComboBox.getCurvatureFromString(cName));
                fireTableRowsUpdated(row, row);
                return;
            case ERR_SENSORCOL:
                boolean ok = false;
                try {
                    if (((String) value).trim().length() == 0) {
                        block.setErrorSensor(null);
                        ok = true;
                    } else {
                        ok = block.setErrorSensor((String) value);
                        fireTableRowsUpdated(row, row);
                    }
                } catch (Exception ex) {
                    log.error("getSensor({}) threw exception: {}", value, ex);
                }
                if (!ok) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchSensorErr", value),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                }
                fireTableRowsUpdated(row, row);
                return;
            case REPORTERCOL:
                Reporter rep = null;
                try {
                    rep = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter((String) value);
                    if (rep != null) {
                        block.setReporter(rep);
                        fireTableRowsUpdated(row, row);
                    }
                } catch (Exception ex) {
                    log.error("No Reporter named \"{}\" found. threw exception: {}", value, ex);
                }
                if (rep == null) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchReporterErr", tempRow[REPORTERCOL]),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                }
                block.setReporter(rep);
                fireTableRowsUpdated(row, row);
                return;
            case REPORT_CURRENTCOL:
                if (block.getReporter() != null) {
                    block.setReportingCurrent((Boolean) value);
                    fireTableRowsUpdated(row, row);
                }
                return;
            case PERMISSIONCOL:
                block.setPermissiveWorking((Boolean) value); // compare to REPORT_CURRENTCOL
                fireTableRowsUpdated(row, row);
                return;
            case WARRANTCOL:
                Warrant warrant = block .getWarrant();
                jmri.jmrit.logix.WarrantManager mgr = InstanceManager
                            .getDefault(jmri.jmrit.logix.WarrantManager.class);
                Warrant newWarrant = mgr.getWarrant((String)value);
                if (warrant != null && !warrant.equals(newWarrant)) {
                    block.deAllocate(warrant);
                    if (newWarrant != null) {
                        String msg = block.allocate(newWarrant);
                        if (msg != null) {
                            JOptionPane.showMessageDialog(null, msg,
                                    Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                    
                        }                    
                    }
                }
                fireTableRowsUpdated(row, row);
                return;
            case SPEEDCOL:
                block.setBlockSpeedName((String) value);
                fireTableRowsUpdated(row, row);
                return;
            case EDIT_COL:
                _parent.openBlockPathPane(block.getSystemName(), null); // interface is checked in TableFrames
                return;
            case DELETE_COL:
                deleteBean(block);
                return;
            default:
                // fall through
                break;
        }
        super.setValueAt(value, row, col);
    }

    private static boolean sensorExists(String name) {
        Sensor sensor = InstanceManager.sensorManagerInstance().getByUserName(name);
        if (sensor == null) {
            sensor = InstanceManager.sensorManagerInstance().getBySystemName(name);
        }
        return (sensor != null);
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COMMENTCOL:
                return Bundle.getMessage("Comment");
            case STATECOL:
                return Bundle.getMessage("ColumnState");
            case SENSORCOL:
                return Bundle.getMessage("BeanNameSensor");
            case CURVECOL:
                return Bundle.getMessage("BlockCurveColName");
            case LENGTHCOL:
                return Bundle.getMessage("BlockLengthColName");
            case UNITSCOL:
                return Bundle.getMessage("UnitsCol");
            case EDIT_COL:
                return Bundle.getMessage("MenuPaths");
            case DELETE_COL:
                return Bundle.getMessage("ColumnDelete");
            case ERR_SENSORCOL:
                return Bundle.getMessage("ErrorSensorCol");
            case REPORTERCOL:
                return Bundle.getMessage("ReporterCol");
            case REPORT_CURRENTCOL:
                return Bundle.getMessage("RepCurrentCol");
            case PERMISSIONCOL:
                return Bundle.getMessage("PermissionCol");
            case WARRANTCOL:
                return Bundle.getMessage("WarrantCol");
            case SPEEDCOL:
                return Bundle.getMessage("SpeedCol");
            default:
                // fall through
                break;
        }
        return super.getColumnName(col);
    }

    // Delete in row pressed, remove OBlock from manager etc. Works in both interfaces
    void deleteBean(OBlock bean) {
        StringBuilder sb = new StringBuilder(Bundle.getMessage("DeletePrompt", bean.getSystemName()));
        for (PropertyChangeListener listener : bean.getPropertyChangeListeners()) {
            if (!(listener instanceof OBlockTableModel) && 
                    !(listener instanceof BlockPathTableModel) && 
                    !(listener instanceof PathTurnoutTableModel) &&
                    !(listener instanceof jmri.jmrit.picker.PickListModel) &&
                    !(listener instanceof OBlockManager)) {
                sb.append("\n");
                sb.append(Bundle.getMessage("InUseBy", bean.getDisplayName(), listener.getClass().getName()));
            }
        }
        int val = _parent.verifyWarning(sb.toString());
        if (val == 2) {
            return;  // return without deleting
        }
        bean.dispose();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case CURVECOL:
                return CurveComboBoxPanel.class;
            case SPEEDCOL:
                return SpeedComboBoxPanel.class; // apply real combo renderer
            case DELETE_COL:
            case EDIT_COL:
                return JButton.class;
            case UNITSCOL:
                return JToggleButton.class;
            case REPORT_CURRENTCOL:
                return JRadioButton.class;
            case PERMISSIONCOL:
                return JCheckBox.class; // return Boolean.class;
            default:
                return String.class;
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL:
            case USERNAMECOL:
                return new JTextField(15).getPreferredSize().width;
            case STATECOL:
                return new JTextField(ZEROS).getPreferredSize().width;
            case COMMENTCOL:
            case SENSORCOL:
            case ERR_SENSORCOL:
            case REPORTERCOL:
            case WARRANTCOL:
                return new JTextField(12).getPreferredSize().width;
            case CURVECOL:
            case REPORT_CURRENTCOL:
            case PERMISSIONCOL:
            case SPEEDCOL:
                return new JTextField(10).getPreferredSize().width;
            case LENGTHCOL:
                return new JTextField(6).getPreferredSize().width;
            case UNITSCOL:
                return new JTextField(5).getPreferredSize().width;
            case EDIT_COL:
                return new JButton(Bundle.getMessage("ButtonEditPath")).getPreferredSize().width+4;
            case DELETE_COL:
                return new JButton(Bundle.getMessage("ButtonDelete")).getPreferredSize().width+4;
            default:
                // fall through
                break;
        }
        return 5;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (super.getRowCount() == row) {
            return true; // the new entry/bottom row is editable in all cells
        }
        return (col != SYSNAMECOL && col != STATECOL);
    }

    //*********************** combo box cell editors *********************************/

    /**
     * Provide a table cell renderer looking like a JComboBox as an
     * editor/renderer for the OBlock table SPEED column.
     * <p>
     * This is a lightweight version of the
     * {@link jmri.jmrit.beantable.RowComboBoxPanel} RowComboBox cell editor
     * class, some of the hashtables not needed here since we only need
     * identical options for all rows in a column.
     *
     * see jmri.jmrit.signalling.SignallingPanel.SignalMastModel.AspectComboBoxPanel for a full application with
     * row specific comboBox choices.
     */
    public static class SpeedComboBoxPanel extends RowComboBoxPanel {

        @Override
        protected final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            log.debug("eventEditorMousePressed in row: {})", this.currentRow);  // NOI18N
        }

        /**
         * Call the method in the surrounding method for the
         * OBlockTable.
         *
         * @param row the user clicked on in the table
         * @return an appropriate combobox for this signal head
         */
        @Override
        protected JComboBox<String> getEditorBox(int row) {
            return getSpeedEditorBox(row);
        }

    }
    // end of methods to display SPEED_COLUMN ComboBox

    /**
     * Provide a static JComboBox element to display inside the JPanel
     * CellEditor. When not yet present, create, store and return a new one.
     *
     * @param row Index number (in TableDataModel)
     * @return A JCombobox containing the valid curvature names.
     */
    static JComboBox<String> getCurveEditorBox(int row) {
        // create dummy comboBox, override in extended classes for each bean
        BlockCurvatureJComboBox j = new BlockCurvatureJComboBox();
        j.setJTableCellClientProperties();
        return j;
    }

    /**
     * Customize the Turnout column to show an appropriate ComboBox of
     * available options.
     *
     * @param table a JTable of beans
     */
    public void configCurveColumn(JTable table) {
        // have the state column hold a JPanel with a JComboBox for Curvature
        table.setDefaultEditor(OBlockTableModel.CurveComboBoxPanel.class, new OBlockTableModel.CurveComboBoxPanel());
        table.setDefaultRenderer(OBlockTableModel.CurveComboBoxPanel.class, new OBlockTableModel.CurveComboBoxPanel()); // use same class as renderer
        // Set more things?
    }

    /**
     * Provide a table cell renderer looking like a JComboBox as an
     * editor/renderer for the OBlock table CURVE column.
     * <p>
     * This is a lightweight version of the
     * {@link jmri.jmrit.beantable.RowComboBoxPanel} RowComboBox cell editor
     * class, some of the hashtables not needed here since we only need
     * identical options for all rows in a column.
     *
     * see jmri.jmrit.signalling.SignallingPanel.SignalMastModel.AspectComboBoxPanel for a full application with
     * row specific comboBox choices.
     */
    public static class CurveComboBoxPanel extends RowComboBoxPanel {

        @Override
        protected final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            log.debug("eventEditorMousePressed in row: {})", this.currentRow);  // NOI18N
        }

        /**
         * Call the method in the surrounding method for the
         * OBlockTable.
         *
         * @param row the user clicked on in the table
         * @return an appropriate combobox for this signal head
         */
        @Override
        protected JComboBox<String> getEditorBox(int row) {
            return getCurveEditorBox(row);
        }

    }
    // end of methods to display CURVE_COLUMN ComboBox

    /**
     * Provide a static JComboBox element to display inside the JPanel
     * CellEditor. When not yet present, create, store and return a new one.
     *
     * @param row Index number (in TableDataModel)
     * @return A combobox containing the valid aspect names for this mast
     */
    static JComboBox<String> getSpeedEditorBox(int row) {
        // create dummy comboBox, override in extended classes for each bean
        JComboBox<String> editCombo = new JComboBox<>(jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames());
        // item to reset speed notch to default, i.e. continue at current speed requirement.
        javax.swing.DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)editCombo.getModel();
        model.addElement("");
        editCombo.putClientProperty("JComponent.sizeVariant", "small");
        editCombo.putClientProperty("JComboBox.buttonType", "square");
        return editCombo;
    }

    /**
     * Customize the Turnout column to show an appropriate ComboBox of
     * available options.
     *
     * @param table a JTable of beans
     */
    public void configSpeedColumn(JTable table) {
        // have the state column hold a JPanel with a JComboBox for Speeds
        table.setDefaultEditor(OBlockTableModel.SpeedComboBoxPanel.class, new OBlockTableModel.SpeedComboBoxPanel());
        table.setDefaultRenderer(OBlockTableModel.SpeedComboBoxPanel.class, new OBlockTableModel.SpeedComboBoxPanel()); // use same class as renderer
        // Set more things?
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) log.debug("PropertyChange = {}", property);
        if (property.equals("length") || property.equals("UserName") || property.equals("state")) {
            _parent.updateOBlockTablesMenu();
            fireTableDataChanged();
        }
        _parent.getPortalXRefTableModel().propertyChange(e);
        _parent.getSignalTableModel().propertyChange(e);
        _parent.getPortalTableModel().propertyChange(e);

    }

    protected String getClassName() {
        return jmri.jmrit.beantable.OBlockTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(OBlockTableModel.class);

}
