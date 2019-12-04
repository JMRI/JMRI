package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.Reporter;
import jmri.Sensor;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.util.IntlUtilities;
import jmri.util.NamedBeanComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks
 * <p>
 * Duplicates the JTable model for BlockTableAction and adds a column for the
 * occupancy sensor. Configured for use within an internal frame.
 *
 * @author Pete Cressman (C) 2010
 */
public class OBlockTableModel extends jmri.jmrit.beantable.BeanTableDataModel<OBlock> {

    static public final int SYSNAMECOL = 0;
    static public final int USERNAMECOL = 1;
    static public final int COMMENTCOL = 2;
    static public final int STATECOL = 3;
    static public final int SENSORCOL = 4;
    static public final int EDIT_COL = 5;   // Path button
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

    static public final String noneText = Bundle.getMessage("BlockNone");
    static public final String gradualText = Bundle.getMessage("BlockGradual");
    static public final String tightText = Bundle.getMessage("BlockTight");
    static public final String severeText = Bundle.getMessage("BlockSevere");
    static final String[] curveOptions = {noneText, gradualText, tightText, severeText};

    static String ZEROS = "000000000";      // 9 bits have state info

    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    OBlockManager _manager;
    private final String[] tempRow = new String[NUMCOLS];
    private float _tempLen = 0.0f;      // mm for length col of tempRow
    TableFrames _parent;

    public OBlockTableModel(TableFrames parent) {
        super();
        _parent = parent;
        updateNameList();
        initTempRow();
    }

    void addHeaderListener(JTable table) {
        addMouseListenerToHeader(table);
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[LENGTHCOL] = twoDigit.format(0.0);
        tempRow[UNITSCOL] = Bundle.getMessage("in");
        tempRow[CURVECOL] = noneText;
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
    public OBlock getBySystemName(String name) {
        return _manager.getBySystemName(name);
    }

    @Override
    public OBlock getByUserName(String name) {
        return _manager.getByUserName(name);
    }

    @Override
    protected String getBeanType() {
        return "OBlock";
    }

    @Override
    public void clickOn(OBlock t) {
    }

    @Override
    protected String getMasterClassName() {
        return OBlockTableModel.class.getName();
    }

    protected List<OBlock> getBeanList() {
        TreeSet<OBlock> ts = new TreeSet<>(new NamedBeanComparator<>());

        Iterator<String> iter = sysNameList.iterator();
        while (iter.hasNext()) {
            ts.add(getBySystemName(iter.next()));
        }
        ArrayList<OBlock> list = new ArrayList<>(sysNameList.size());

        Iterator<OBlock> it = ts.iterator();
        while (it.hasNext()) {
            OBlock elt = it.next();
            list.add(elt);
        }
        return list;
    }

    @Override
    public String getValue(String name) {
        int state = _manager.getBySystemName(name).getState();
        return getValue(state);
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
        return super.getRowCount() + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row > sysNameList.size()) {
            return "";
        }
        OBlock b = null;
        if (row < sysNameList.size()) {
            String name = sysNameList.get(row);
            b = _manager.getBySystemName(name);
        }
        switch (col) {
            case SYSNAMECOL:
                if (b != null) {
                    return b.getSystemName();
                }
                return tempRow[col];
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
                    if (num>=0) {
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
                return Boolean.valueOf(tempRow[UNITSCOL].equals(Bundle.getMessage("cm")));
            case CURVECOL:
                if (b != null) {
                    String c = "";
                    if (b.getCurvature() == Block.NONE) {
                        c = noneText;
                    } else if (b.getCurvature() == Block.GRADUAL) {
                        c = gradualText;
                    } else if (b.getCurvature() == Block.TIGHT) {
                        c = tightText;
                    } else if (b.getCurvature() == Block.SEVERE) {
                        c = severeText;
                    }
                    return c;
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
                return Boolean.valueOf(tempRow[REPORT_CURRENTCOL].equals(Bundle.getMessage("Current")));
            case PERMISSIONCOL:
                if (b != null) {
                    return b.getPermissiveWorking();
                }
                return Boolean.valueOf(tempRow[PERMISSIONCOL].equals(Bundle.getMessage("Permissive")));
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
                    return Bundle.getMessage("ButtonEditPath");
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
        if (log.isDebugEnabled()) {
            log.debug("setValueAt: row= {}, col= {}, value= {}", row, col, value);
        }
        if (super.getRowCount() == row) {
            switch (col) {
                case SYSNAMECOL:
                    OBlock block = _manager.createNewOBlock((String) value, tempRow[USERNAMECOL]);
                    if (block == null) {
                        block = _manager.getOBlock(tempRow[USERNAMECOL]);
                        String name = (String)value + " / " + tempRow[USERNAMECOL];
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
                    if (tempRow[CURVECOL].equals(noneText)) {
                        block.setCurvature(Block.NONE);
                    } else if (tempRow[CURVECOL].equals(gradualText)) {
                        block.setCurvature(Block.GRADUAL);
                    } else if (tempRow[CURVECOL].equals(tightText)) {
                        block.setCurvature(Block.TIGHT);
                    } else if (tempRow[CURVECOL].equals(severeText)) {
                        block.setCurvature(Block.SEVERE);
                    }
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
                            log.error("No Reporter named \"" + tempRow[REPORTERCOL] + "\" found. threw exception: " + ex);
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
                case DELETE_COL:            // clear
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
                    if ((((Boolean) value).booleanValue())) {
                        tempRow[UNITSCOL] = Bundle.getMessage("cm");
                    } else {
                        tempRow[UNITSCOL] = Bundle.getMessage("in");
                    }
                    fireTableRowsUpdated(row, row);
                    return;
                case REPORT_CURRENTCOL:
                    if (((Boolean) value).booleanValue()) {//toggle
                        tempRow[REPORT_CURRENTCOL] = Bundle.getMessage("Current");
                    } else {
                        tempRow[REPORT_CURRENTCOL] = Bundle.getMessage("Last");
                    }
                    return;
                case PERMISSIONCOL:
                    if (((Boolean) value).booleanValue()) {//toggle
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
        String name = sysNameList.get(row);
        OBlock block = _manager.getBySystemName(name);
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
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchSensorErr", (String) value),
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
                block.setMetricUnits(((Boolean) value).booleanValue());
                fireTableRowsUpdated(row, row);
                return;
            case CURVECOL:
                String cName = (String) value;
                if (cName.equals(noneText)) {
                    block.setCurvature(Block.NONE);
                } else if (cName.equals(gradualText)) {
                    block.setCurvature(Block.GRADUAL);
                } else if (cName.equals(tightText)) {
                    block.setCurvature(Block.TIGHT);
                } else if (cName.equals(severeText)) {
                    block.setCurvature(Block.SEVERE);
                }
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
                    log.error("getSensor(" + (String) value + ") threw exception: " + ex);
                }
                if (!ok) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchSensorErr", (String) value),
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
                    log.error("No Reporter named \"" + (String) value + "\" found. threw exception: " + ex);
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
                    block.setReportingCurrent(((Boolean) value).booleanValue());
                    fireTableRowsUpdated(row, row);
                }
                return;
            case PERMISSIONCOL:
                block.setPermissiveWorking(((Boolean) value).booleanValue());
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
                _parent.openBlockPathFrame(block.getSystemName());
                return;
            case DELETE_COL:
                deleteBean(block);
                block = null;
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
                return "  ";
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
            case EDIT_COL:
                return Bundle.getMessage("ButtonEditPath");
            case DELETE_COL:
                return Bundle.getMessage("ButtonDelete");
            default:
                // fall through
                break;
        }
        return super.getColumnName(col);
    }

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
            case SPEEDCOL:
                return JComboBox.class;
            case DELETE_COL:
            case EDIT_COL:
                return JButton.class;
            case UNITSCOL:
            case REPORT_CURRENTCOL:
            case PERMISSIONCOL:
                return Boolean.class;
            default:
                // fall through
                break;
        }
        return String.class;
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL:
                return new JTextField(18).getPreferredSize().width;
            case USERNAMECOL:
                return new JTextField(18).getPreferredSize().width;
            case COMMENTCOL:
                return new JTextField(10).getPreferredSize().width;
            case STATECOL:
                return new JTextField(ZEROS).getPreferredSize().width;
            case SENSORCOL:
                return new JTextField(15).getPreferredSize().width;
            case CURVECOL:
                return new JTextField(6).getPreferredSize().width;
            case LENGTHCOL:
                return new JTextField(5).getPreferredSize().width;
            case UNITSCOL:
                return new JTextField(2).getPreferredSize().width;
            case ERR_SENSORCOL:
                return new JTextField(15).getPreferredSize().width;
            case REPORTERCOL:
                return new JTextField(15).getPreferredSize().width;
            case REPORT_CURRENTCOL:
                return new JTextField(6).getPreferredSize().width;
            case PERMISSIONCOL:
                return new JTextField(6).getPreferredSize().width;
            case SPEEDCOL:
                return new JTextField(8).getPreferredSize().width;
            case WARRANTCOL:
                return new JTextField(15).getPreferredSize().width;
            case EDIT_COL:
                return new JButton("DELETE").getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return 5;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (super.getRowCount() == row) {
            return true;
        }
        if (col == SYSNAMECOL || col == STATECOL) {
            return false;
        }
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) log.debug("PropertyChange = {}", property);
        _parent.getXRefModel().propertyChange(e);
        _parent.getSignalModel().propertyChange(e);
        _parent.getPortalModel().propertyChange(e);

        if (property.equals("length") || property.equals("UserName")) {
            _parent.updateOpenMenu();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(OBlockTableModel.class);
}
