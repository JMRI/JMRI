// EnginesTableModel.java
package jmri.jmrit.operations.rollingstock.engines;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of engines used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
public class EnginesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 6804454611283948123L;

    EngineManager manager = EngineManager.instance(); // There is only one manager

    // Defines the columns
    private static final int NUM_COLUMN = 0;
    private static final int ROAD_COLUMN = 1;
    private static final int MODEL_COLUMN = 2;
    private static final int HP_COLUMN = 3;
    private static final int TYPE_COLUMN = 4;
    private static final int LENGTH_COLUMN = 5;
    private static final int CONSIST_COLUMN = 6;
    private static final int LOCATION_COLUMN = 7;
    private static final int DESTINATION_COLUMN = 8;
    private static final int TRAIN_COLUMN = 9;
    private static final int MOVES_COLUMN = 10;
    private static final int SET_COLUMN = 11;
    private static final int EDIT_COLUMN = 12;

    private static final int HIGHEST_COLUMN = EDIT_COLUMN + 1;

    private static final int SHOWMOVES = 0;
    private static final int SHOWBUILT = 1;
    private static final int SHOWOWNER = 2;
    private static final int SHOWVALUE = 3;
    private static final int SHOWRFID = 4;
    private static final int SHOWLAST = 5;
    private int showMoveCol = SHOWMOVES;

    public EnginesTableModel() {
        super();
        manager.addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYNUMBER = 1;
    public final int SORTBYROAD = 2;
    public final int SORTBYMODEL = 3;
    public final int SORTBYLOCATION = 4;
    public final int SORTBYDESTINATION = 5;
    public final int SORTBYTRAIN = 6;
    public final int SORTBYMOVES = 7;
    public final int SORTBYCONSIST = 8;
    public final int SORTBYBUILT = 9;
    public final int SORTBYOWNER = 10;
    public final int SORTBYVALUE = 11;
    public final int SORTBYRFID = 12;
    public final int SORTBYLAST = 13;
    public final int SORTBYHP = 14;

    private int _sort = SORTBYNUMBER;

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        if (sort == SORTBYMOVES) {
            showMoveCol = SHOWMOVES;
            fireTableStructureChanged();
            initTable();
        } else if (sort == SORTBYBUILT) {
            showMoveCol = SHOWBUILT;
            fireTableStructureChanged();
            initTable();
        } else if (sort == SORTBYOWNER) {
            showMoveCol = SHOWOWNER;
            fireTableStructureChanged();
            initTable();
        } else if (sort == SORTBYVALUE) {
            showMoveCol = SHOWVALUE;
            fireTableStructureChanged();
            initTable();
        } else if (sort == SORTBYRFID) {
            showMoveCol = SHOWRFID;
            fireTableStructureChanged();
            initTable();
        } else if (sort == SORTBYLAST) {
            showMoveCol = SHOWLAST;
            fireTableStructureChanged();
            initTable();
        } else {
            fireTableDataChanged();
        }
    }

    String _roadNumber = "";
    int _index = 0;

    /**
     * Search for engine by road number
     *
     * @return -1 if not found, table row number if found
     */
    public int findEngineByRoadNumber(String roadNumber) {
        if (sysList != null) {
            if (!roadNumber.equals(_roadNumber)) {
                return getIndex(0, roadNumber);
            }
            int index = getIndex(_index, roadNumber);
            if (index > 0) {
                return index;
            }
            return getIndex(0, roadNumber);
        }
        return -1;
    }

    private int getIndex(int start, String roadNumber) {
        for (int index = start; index < sysList.size(); index++) {
            Engine e = (Engine) sysList.get(index);
            if (e != null) {
                String[] number = e.getNumber().split("-");
                // check for wild card '*'
                if (roadNumber.startsWith("*")) {
                    String rN = roadNumber.substring(1);
                    if (e.getNumber().endsWith(rN) || number[0].endsWith(rN)) {
                        _roadNumber = roadNumber;
                        _index = index + 1;
                        return index;
                    }
                } else if (roadNumber.endsWith("*")) {
                    String rN = roadNumber.substring(0, roadNumber.length() - 1);
                    if (e.getNumber().startsWith(rN)) {
                        _roadNumber = roadNumber;
                        _index = index + 1;
                        return index;
                    }
                } else if (e.getNumber().equals(roadNumber) || number[0].equals(roadNumber)) {
                    _roadNumber = roadNumber;
                    _index = index + 1;
                    return index;
                }
            }
        }
        _roadNumber = "";
        return -1;
    }

    synchronized void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeEngines();
        sysList = getSelectedEngineList();
        // and add listeners back in
        for (RollingStock rs : sysList) {
            rs.addPropertyChangeListener(this);
        }
    }

    public List<RollingStock> getSelectedEngineList() {
        List<RollingStock> list;
        if (_sort == SORTBYROAD) {
            list = manager.getByRoadNameList();
        } else if (_sort == SORTBYMODEL) {
            list = manager.getByModelList();
        } else if (_sort == SORTBYLOCATION) {
            list = manager.getByLocationList();
        } else if (_sort == SORTBYDESTINATION) {
            list = manager.getByDestinationList();
        } else if (_sort == SORTBYTRAIN) {
            list = manager.getByTrainList();
        } else if (_sort == SORTBYMOVES) {
            list = manager.getByMovesList();
        } else if (_sort == SORTBYCONSIST) {
            list = manager.getByConsistList();
        } else if (_sort == SORTBYOWNER) {
            list = manager.getByOwnerList();
        } else if (_sort == SORTBYBUILT) {
            list = manager.getByBuiltList();
        } else if (_sort == SORTBYVALUE) {
            list = manager.getByValueList();
        } else if (_sort == SORTBYRFID) {
            list = manager.getByRfidList();
        } else if (_sort == SORTBYLAST) {
            list = manager.getByLastDateList();
        } else {
            list = manager.getByNumberList();
        }
        return list;
    }

    List<RollingStock> sysList = null;

    JTable _table;
    EnginesTableFrame _frame;

    void initTable(JTable table, EnginesTableFrame frame) {
        _table = table;
        _frame = frame;
        initTable();
    }

    // Engines frame table column widths (12), starts with Number column and ends with Edit
    private int[] _enginesTableColumnWidths = {60, 60, 65, 50, 65, 35, 75, 190, 190, 65, 50, 65, 70};

    void initTable() {
        // Install the button handlers
        TableColumnModel tcm = _table.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(SET_COLUMN).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(SET_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);

        // set column preferred widths
        if (!_frame.loadTableDetails(_table)) {
            // load defaults, xml file data not found
            for (int i = 0; i < tcm.getColumnCount(); i++) {
                tcm.getColumn(i).setPreferredWidth(_enginesTableColumnWidths[i]);
            }
        }
        _table.setRowHeight(new JComboBox<>().getPreferredSize().height);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    public int getRowCount() {
        return sysList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHEST_COLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NUM_COLUMN:
                return Bundle.getMessage("Number");
            case ROAD_COLUMN:
                return Bundle.getMessage("Road");
            case MODEL_COLUMN:
                return Bundle.getMessage("Model");
            case HP_COLUMN:
                return Bundle.getMessage("HP");
            case TYPE_COLUMN:
                return Bundle.getMessage("Type");
            case LENGTH_COLUMN:
                return Bundle.getMessage("Len");
            case CONSIST_COLUMN:
                return Bundle.getMessage("Consist");
            case LOCATION_COLUMN:
                return Bundle.getMessage("Location");
            case DESTINATION_COLUMN:
                return Bundle.getMessage("Destination");
            case TRAIN_COLUMN:
                return Bundle.getMessage("Train");
            case MOVES_COLUMN: {
                if (showMoveCol == SHOWBUILT) {
                    return Bundle.getMessage("Built");
                } else if (showMoveCol == SHOWOWNER) {
                    return Bundle.getMessage("Owner");
                } else if (showMoveCol == SHOWVALUE) {
                    return Setup.getValueLabel();
                } else if (showMoveCol == SHOWRFID) {
                    return Setup.getRfidLabel();
                } else if (showMoveCol == SHOWLAST) {
                    return Bundle.getMessage("LastMoved");
                } else {
                    return Bundle.getMessage("Moves");
                }
            }
            case SET_COLUMN:
                return Bundle.getMessage("Set");
            case EDIT_COLUMN:
                return Bundle.getMessage("Edit");
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case NUM_COLUMN:
                return String.class;
            case ROAD_COLUMN:
                return String.class;
            case LENGTH_COLUMN:
                return String.class;
            case MODEL_COLUMN:
                return String.class;
            case HP_COLUMN:
                return String.class;
            case TYPE_COLUMN:
                return String.class;
            case CONSIST_COLUMN:
                return String.class;
            case LOCATION_COLUMN:
                return String.class;
            case DESTINATION_COLUMN:
                return String.class;
            case TRAIN_COLUMN:
                return String.class;
            case MOVES_COLUMN:
                return String.class;
            case SET_COLUMN:
                return JButton.class;
            case EDIT_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case SET_COLUMN:
            case EDIT_COLUMN:
            case MOVES_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= sysList.size()) {
            return "ERROR row " + row; // NOI18N
        }
        Engine eng = (Engine) sysList.get(row);
        if (eng == null) {
            return "ERROR engine unknown " + row; // NOI18N
        }
        switch (col) {
            case NUM_COLUMN:
                return eng.getNumber();
            case ROAD_COLUMN:
                return eng.getRoadName();
            case LENGTH_COLUMN:
                return eng.getLengthInteger();
            case MODEL_COLUMN:
                return eng.getModel();
            case HP_COLUMN:
                return eng.getHpInteger();
            case TYPE_COLUMN: {
                if (eng.isBunit()) {
                    return eng.getTypeName() + " " + Bundle.getMessage("(B)");
                }
                return eng.getTypeName();
            }
            case CONSIST_COLUMN: {
                if (eng.getConsist() != null && eng.getConsist().isLead(eng)) {
                    return eng.getConsistName() + "*";
                }
                return eng.getConsistName();
            }
            case LOCATION_COLUMN: {
                String s = eng.getStatus();
                if (!eng.getLocationName().equals(Engine.NONE)) {
                    s = eng.getStatus() + eng.getLocationName() + " (" + eng.getTrackName() + ")";
                }
                return s;
            }
            case DESTINATION_COLUMN: {
                String s = "";
                if (!eng.getDestinationName().equals(Engine.NONE)) {
                    s = eng.getDestinationName() + " (" + eng.getDestinationTrackName() + ")";
                }
                return s;
            }
            case TRAIN_COLUMN: {
                // if train was manually set by user add an asterisk
                if (eng.getTrain() != null && eng.getRouteLocation() == null) {
                    return eng.getTrainName() + "*";
                }
                return eng.getTrainName();
            }
            case MOVES_COLUMN: {
                if (showMoveCol == SHOWBUILT) {
                    return eng.getBuilt();
                } else if (showMoveCol == SHOWOWNER) {
                    return eng.getOwner();
                } else if (showMoveCol == SHOWVALUE) {
                    return eng.getValue();
                } else if (showMoveCol == SHOWRFID) {
                    return eng.getRfid();
                } else if (showMoveCol == SHOWLAST) {
                    return eng.getLastDate();
                } else {
                    return eng.getMoves();
                }
            }
            case SET_COLUMN:
                return Bundle.getMessage("Set");
            case EDIT_COLUMN:
                return Bundle.getMessage("Edit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    EngineEditFrame engineEditFrame = null;
    EngineSetFrame engineSetFrame = null;

    @Override
    public void setValueAt(Object value, int row, int col) {
        Engine engine = (Engine) sysList.get(row);
        switch (col) {
            case SET_COLUMN:
                log.debug("Set engine location");
                if (engineSetFrame != null) {
                    engineSetFrame.dispose();
                }
                // use invokeLater so new window appears on top
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        engineSetFrame = new EngineSetFrame();
                        engineSetFrame.initComponents();
                        engineSetFrame.loadEngine(engine);
                    }
                });
                break;
            case EDIT_COLUMN:
                log.debug("Edit engine");
                if (engineEditFrame != null) {
                    engineEditFrame.dispose();
                }
                // use invokeLater so new window appears on top
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        engineEditFrame = new EngineEditFrame();
                        engineEditFrame.initComponents();
                        engineEditFrame.loadEngine(engine);
                    }
                });
                break;
            case MOVES_COLUMN:
                if (showMoveCol == SHOWBUILT) {
                    engine.setBuilt(value.toString());
                } else if (showMoveCol == SHOWOWNER) {
                    engine.setOwner(value.toString());
                } else if (showMoveCol == SHOWVALUE) {
                    engine.setValue(value.toString());
                } else if (showMoveCol == SHOWRFID) {
                    engine.setRfid(value.toString());
                } else {
                    try {
                        engine.setMoves(Integer.parseInt(value.toString()));
                    } catch (NumberFormatException e) {
                        log.error("move count must be a number");
                    }
                }
                break;
            default:
                break;
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose EngineTableModel");
        }
        manager.removePropertyChangeListener(this);
        removePropertyChangeEngines();
        if (engineSetFrame != null) {
            engineSetFrame.dispose();
        }
        if (engineEditFrame != null) {
            engineEditFrame.dispose();
        }
    }

    private void removePropertyChangeEngines() {
        if (sysList != null) {
            for (RollingStock rs : sysList) {
                rs.removePropertyChangeListener(this);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(EngineManager.LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        } // Engine lengths are based on model, so multiple changes
        else if (e.getPropertyName().equals(Engine.LENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Engine.TYPE_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        } // must be a engine change
        else if (e.getSource().getClass().equals(Engine.class)) {
            Engine engine = (Engine) e.getSource();
            int row = sysList.indexOf(engine);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update engine table row: {}", row);
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EnginesTableModel.class.getName());
}
